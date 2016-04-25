/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.cmis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
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
	 * Creates a new instance folder in the CMIS repository
	 * organized under a year-month folder
	 * 
	 * @param folderName
	 * @return
	 */
	public Folder createInstanceFolder(Folder parent, String folderName) {

		Session session = cmisSession.getSession();

		if (parent == null) {
			parent = session.getRootFolder();
		}
		
		// Create year-month super folder
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String dateString = "" + now.get(Calendar.YEAR) + (month < 10 ? "0" + month : "" + month);
		Folder dateFolder = null;
		
		// Use existing year-month if found 
		ItemIterable<CmisObject> children = parent.getChildren();

		for (CmisObject child : children) {
			
			if (BaseTypeId.CMIS_FOLDER.equals(child.getBaseTypeId())) {
				
				if (child.getName().equals(dateString)) {
					
					dateFolder = (Folder) child;
				}
			}
		}
		
		// Create new year-month folder if not found
		if (dateFolder == null) {
			
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, dateString);
			
			dateFolder = parent.createFolder(properties);
		}
		
		// prepare folder properties
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, folderName);

		Folder newFolder;

		// create the folder
		try {
			newFolder = dateFolder.createFolder(properties);
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
			Folder folder = (Folder) session.getObjectByPath(folderPath);
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
			Folder folder = (Folder) session.getObject(folderId);
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
			
			return (Folder) session.getObjectByPath(path);

		} catch (CmisObjectNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Return the folder corresponding to the given id
	 * 
	 * @param id
	 * @return
	 */
	public Folder getFolderById(String id) {

		Session session = cmisSession.getSession();

		try {
			
			return (Folder) session.getObject(id);

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
	
	/**
	 * Get all documents of a folder
	 * @param folder
	 * @return
	 */
	public boolean updateFolderName(String folderId, String name) {

		Session session = cmisSession.getSession();

		try {
			
			Folder folder = (Folder) session.getObject(folderId);
			
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.NAME, name);
			
			folder.updateProperties(properties);
			
			return true;

		} catch (CmisObjectNotFoundException e) {
			
			return false;
		}
	}
}
