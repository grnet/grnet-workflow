/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.cmis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CMISFolder {

	final static Logger logger = LoggerFactory.getLogger(CMISFolder.class);

	@Autowired
	private CMISSession cmisSession;

	/**
	 * Creates a new folder in the CMIS repository
	 * 
	 * @param folderName
	 * @return
	 */
	public Folder createFolder(Folder parent, String folderName) {

		Session session = cmisSession.getSession();

		if (parent == null) {
			parent = session.getRootFolder();
		}

		// prepare folder properties
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, folderName);

		Folder newFolder;

		// create the folder
		try {
			newFolder = parent.createFolder(properties);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}

		return newFolder;
	}

	/**
	 * Delete the folder by the given path. The folder must be empty
	 * 
	 * @param folderPath
	 * @return
	 */
	public Boolean deleteFolderByPath(String folderPath) {

		Session session = cmisSession.getSession();

		try {
			Folder folder;

			folder = (Folder) session.getObjectByPath(folderPath);
			folder.delete();
		} catch (CmisObjectNotFoundException e) {
			// no need to delete
			return false;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Delete the folder for the given id. The folder must be empty.
	 * 
	 * @param folderId
	 * @return
	 */
	public Boolean deleteFolderById(String folderId) {

		Session session = cmisSession.getSession();

		try {
			Folder folder;

			folder = (Folder) session.getObject(folderId);
			folder.delete();
		} catch (CmisObjectNotFoundException e) {
			// no need to delete
			return false;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Deletes the folder and all of its content
	 * 
	 * @param folderPath
	 * @return
	 */
	public Boolean deleteTree(String folderPath) {
		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObjectByPath(folderPath);

			Folder delFolder = (Folder) object;
			delFolder.deleteTree(true, UnfileObject.DELETE, true);

		} catch (CmisObjectNotFoundException e) {
			// no need to clean up
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Returns the root folder of the repository
	 * 
	 * @return
	 */
	public Folder getRootFolder() {
		Session session = cmisSession.getSession();

		return session.getRootFolder();
	}

	/**
	 * Return the folder corresponding to the given path
	 * 
	 * @param path
	 * @return
	 */
	public Folder getFolderByPath(String path) {

		Session session = cmisSession.getSession();

		try {
			Folder folder = null;
			CmisObject object = session.getObjectByPath(path);
			folder = (Folder) object;
			return folder;

		} catch (CmisObjectNotFoundException e) {
			return null;
		}
	}

	/**
	 * Get all documents of a folder
	 * @param folder
	 * @return
	 */
	public List<Document> getFolderDocuments(Folder folder) {

		ArrayList<Document> docs = new ArrayList<Document>();
		Iterable<CmisObject> children = folder.getChildren();

		for (CmisObject child : children) {

			if (BaseTypeId.CMIS_DOCUMENT.equals(child.getBaseTypeId())) {
				docs.add((Document) child);
			}
		}

		return docs;
	}
}
