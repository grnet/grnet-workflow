package gr.cyberstream.workflow.engine.cmis;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements all business rules related to CMIS document operations
 * 
 * @author nlyk
 *
 */
@Service
public class CMISDocument {

	private static final Logger logger = LoggerFactory.getLogger(CMISDocument.class);

	@Autowired
	private CMISSession cmisSession;

	@Autowired
	private CMISFolder cmisFolder;

	/**
	 * Creates a new document and place it to the given folder
	 * 
	 * @param folder
	 *            Folder to save the document to
	 * 
	 * @param name
	 *            The document's name
	 * 
	 * @param mimeType
	 *            The document's mime type
	 * 
	 * @param inputStream
	 *            The document's input stream
	 * 
	 * @return The saved {@link Document}
	 */
	public Document createDocument(Folder folder, String name, String mimeType, InputStream inputStream) {

		Session session = cmisSession.getSession();

		if (folder == null)
			throw new CmisRuntimeException();

		// prepare document properties
		Map<String, String> props = new HashMap<String, String>();
		props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		props.put(PropertyIds.NAME, name);

		ContentStream contentStream = session.getObjectFactory().createContentStream(name, -1, mimeType, inputStream);
		Document newDoc = folder.createDocument(props, contentStream, VersioningState.MAJOR);

		return newDoc;
	}

	/**
	 * Creates a new document and place it to the given folder
	 * 
	 * @param path
	 *            Path to save the new document
	 * 
	 * @param name
	 *            The document's name
	 * 
	 * @param mimeType
	 *            The document's mime type
	 * 
	 * @param inputStream
	 *            The document's input stream
	 * 
	 * @return The saved {@link Document}
	 */
	public Document createDocument(String path, String name, String mimeType, InputStream inputStream) {

		Folder folder = cmisFolder.getFolderByPath(path);

		return createDocument(folder, name, mimeType, inputStream);
	}
	
	/**
	 * Deletes the given document
	 * 
	 * @param document
	 *            Document to be deleted
	 * 
	 * @return {@link Boolean} if the document was successfully deleted
	 */
	public Boolean deleteDocument(Document document) {

		try {
			document.delete(true);
			return true;
			
		} catch (CmisObjectNotFoundException e) {
			logger.error("Deleting document failed. Not found the document");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Deletes a document by the given path
	 * 
	 * @param path
	 *            Path for the document to be deleted
	 * 
	 * @return {@link Boolean} if the document was successfully deleted
	 */
	public Boolean deleteDocumentByPath(String path) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObjectByPath(path);
			return deleteDocument((Document) object);

		} catch (CmisObjectNotFoundException e) {
			logger.error("Deleting document failed. Not found the document's path " + path);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Deletes a document by id
	 * 
	 * @param id
	 *            Document's id to be deleted
	 * 
	 * @return {@link Boolean} if the document was successfully deleted
	 */
	public Boolean deleteDocumentById(String id) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObject(id);
			return deleteDocument((Document) object);

		} catch (CmisObjectNotFoundException e) {
			logger.error("Deleting document failed. Not found the document with id " + id);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Creates a new version for the document
	 * 
	 * @param document
	 *            The new document
	 * 
	 * @param newName
	 *            The document's new version name
	 * 
	 * @param mimeType
	 *            The document's mime type
	 * 
	 * @param inputStream
	 *            The document's input stream
	 * 
	 * @return The updated {@link Document}
	 */
	public Document updateDocument(Document document, String newName, String mimeType, InputStream inputStream) {

		Session session = cmisSession.getSession();

		if (document == null)
			throw new CmisRuntimeException();

		ObjectId nextVersion = document.checkOut();
		Document newDocument = (Document) session.getObject(nextVersion);

		ContentStream cos;
		cos = session.getObjectFactory().createContentStream(newName, -1, mimeType, inputStream);

		// prepare document properties - change name
		Map<String, String> props = null;

		if (newName != null && !newName.isEmpty()) {
			props = new HashMap<String, String>();
			props.put(PropertyIds.NAME, newName);
		}

		// add a timestamp as version comment
		newDocument.checkIn(true, props, cos, new Date().toString());

		return newDocument;
	}
	
	/**
	 * Update document identified by its path, creates a new version
	 * 
	 * @param path
	 *            The document's path to be updated
	 * 
	 * @param newName
	 *            The document's new version name
	 * 
	 * @param mimeType
	 *            The document's mime type
	 * 
	 * @param inputStream
	 *            The document's input stream
	 * 
	 * @return The updated {@link Document}
	 */
	public Document updateDocumentByPath(String path, String newName, String mimeType, InputStream inputStream) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObjectByPath(path);
			return updateDocument((Document) object, newName, mimeType, inputStream);

		} catch (CmisObjectNotFoundException e) {
			logger.error("Document is not found: " + path);
			throw e;
		}
	}
	
	/**
	 * Update document identified by its id, creates a new version
	 * 
	 * @param id
	 *            Document's id to be updated
	 * 
	 * @param newName
	 *            The document's new version name
	 * 
	 * @param mimeType
	 *            The document's mime type
	 * 
	 * @param inputStream
	 *            The document's input stream
	 * 
	 * @return The updated {@link Document}
	 */
	public Document updateDocumentById(String id, String newName, String mimeType, InputStream inputStream) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObject(id);
			return updateDocument((Document) object, newName, mimeType, inputStream);

		} catch (CmisObjectNotFoundException e) {
			logger.error("Document is not found: id(" + id + ")");
			throw e;
		}
	}
	
	/**
	 * Update document identified by its id, creating a new version
	 * 
	 * @param id
	 *            Document's id to be updated
	 * 
	 * @param newName
	 *            The document's new name
	 * 
	 * @return The updated {@link Document}
	 */
	public Document updateDocumentById(String id, String newName) {

		Session session = cmisSession.getSession();

		try {
			Document document = (Document) session.getObject(id);

			if (document == null)
				throw new CmisRuntimeException();

			// prepare document properties - change name
			Map<String, String> props = null;

			if (newName != null && !newName.isEmpty()) {
				props = new HashMap<String, String>();
				props.put(PropertyIds.NAME, newName);
			}

			document.updateProperties(props);

			return document;

		} catch (CmisObjectNotFoundException e) {

			logger.error("Document is not found: id(" + id + ")");
			throw e;
		}
	}
	
	/**
	 * Returns all available versions for the given document
	 * 
	 * @param document
	 *            The document to get versions from
	 * 
	 * @return A list of {@link Document}
	 */
	public List<Document> getDocumentVersions(Document document) {
		
		return document.getAllVersions();
	}

	/**
	 * Returns a document by a given path
	 * 
	 * @param path
	 *            Path to get document from
	 * 
	 * @return {@link Document}
	 */
	public Document getDocumentByPath(String path) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObjectByPath(path);
			return (Document) object;

		} catch (CmisObjectNotFoundException e) {
			logger.error("Document is not found: " + path);
			return null;
		}
	}
	
	/**
	 * Returns a document by a given id
	 * 
	 * @param id
	 *            Document's id
	 * 
	 * @return {@link Document}
	 */
	public Document getDocumentById(String id) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObject(id);
			return (Document) object;

		} catch (CmisObjectNotFoundException e) {
			logger.error("Document is not found: id(" + id + ")");
			return null;
		}
	}
	
	/**
	 * Deletes a document's version
	 * 
	 * @param document
	 *            Document to be deleted
	 * 
	 * @return {@link Boolean} if the document was successfully deleted
	 */
	public Boolean deleteDocumentVersion(Document document) {

		try {
			document.delete(false);
			
		} catch (CmisObjectNotFoundException e) {
			return false;
		}
		return true;
	}

	/**
	 * Get document's content stream
	 * 
	 * @param document
	 *            Document to get stream from
	 * 
	 * @return Document's {@link ContentStream}
	 */
	public ContentStream getDocumentContent(Document document) {

		return document.getContentStream();
	}
}
