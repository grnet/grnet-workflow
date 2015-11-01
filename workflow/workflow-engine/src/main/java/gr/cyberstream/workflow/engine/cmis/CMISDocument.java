/**
 * @author nlyk
 */
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

@Service
public class CMISDocument {

	final static Logger logger = LoggerFactory.getLogger(CMISDocument.class);

	@Autowired
	private CMISSession cmisSession;

	@Autowired
	private CMISFolder cmisFolder;

	/**
	 * Creates a new folder in the CMIS repository
	 * 
	 * @param folderName
	 * @return
	 */
	public Document createDocument(Folder folder, String name, String mimeType, InputStream inputStream) {

		Session session = cmisSession.getSession();

		if (folder == null) {
			throw new CmisRuntimeException();
		}

		// prepare document properties
		Map<String, String> props = new HashMap<String, String>();
		props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		props.put(PropertyIds.NAME, name);

		ContentStream contentStream = session.getObjectFactory().createContentStream(name, -1, mimeType, inputStream);
		Document newDoc = folder.createDocument(props, contentStream, VersioningState.MAJOR);

		return newDoc;
	}

	/**
	 * Creates a new folder in the CMIS repository
	 * 
	 * @param folderName
	 * @return
	 */
	public Document createDocument(String path, String name, String mimeType, InputStream inputStream) {

		Folder folder = cmisFolder.getFolderByPath(path);

		return createDocument(folder, name, mimeType, inputStream);
	}

	/**
	 * Delete the document
	 * 
	 * @param document
	 * @return
	 */
	public Boolean deleteDocument(Document document) {

		try {
			document.delete(true);
			return true;
		} catch (CmisObjectNotFoundException e) {
			logger.error("document not fount");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Delete the document for the given path
	 * 
	 * @param path
	 * @return
	 */
	public Boolean deleteDocumentByPath(String path) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObjectByPath(path);
			return deleteDocument((Document) object);

		} catch (CmisObjectNotFoundException e) {
			logger.error("Document is not found: " + path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Delete document By Id
	 * 
	 * @param id
	 * @return
	 */
	public Boolean deleteDocumentById(String id) {

		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObject(id);
			return deleteDocument((Document) object);

		} catch (CmisObjectNotFoundException e) {
			logger.error("Document is not found: id(" + id + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Updates the document creating a new version
	 * 
	 * @param folder
	 * @param name
	 * @param mimeType
	 * @param inputStream
	 * @return
	 */
	public Document updateDocument(Document document, String newName, String mimeType, InputStream inputStream) {

		Session session = cmisSession.getSession();

		if (document == null) {
			throw new CmisRuntimeException();
		}

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
	 * Update document identified by its path, creating a new version 
	 * @param path
	 * @param newName
	 * @param mimeType
	 * @param inputStream
	 * @return
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
	 * Update document identified by its id, creating a new version 
	 * @param path
	 * @param newName
	 * @param mimeType
	 * @param inputStream
	 * @return
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
	 * Return all document versions
	 * @param document
	 * @return
	 */
	public List<Document> getDocumentVersions(Document document) {
		return document.getAllVersions();
	}
	
	/**
	 * Return the document object given its path
	 * @param path
	 * @return
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
	 * Return the document object given its id
	 * @param path
	 * @return
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
	 * Return the specific document version
	 * @param document
	 * @return
	 */
	public Boolean deleteDocumentVersion(Document document) {
		try {
			document.delete(false);
		}
		catch (CmisObjectNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get document content stream
	 * @param document
	 * @return
	 */
	public ContentStream getDocumentContent(Document document) {
		return document.getContentStream();
	}
}
