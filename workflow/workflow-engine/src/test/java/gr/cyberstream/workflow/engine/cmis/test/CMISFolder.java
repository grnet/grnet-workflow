package gr.cyberstream.workflow.engine.cmis.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.cmis.CMISSession;
import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CMISFolder {
	
	final static Logger logger = LoggerFactory.getLogger(CMISFolder.class);

	@Autowired
	private CMISSession cmisSession;

	@Test
	public void shouldCreateFolder() {
		Session session = cmisSession.getSession();
		
		Folder root = session.getRootFolder();

		// properties
		// (minimal set: name and object type id)
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, "test-folder");

		// create the folder
		Folder newFolder = root.createFolder(properties);		
		
		assertNotNull("new folder creation failed", newFolder);
		logger.info("New folder Id: " + newFolder.getId() + ", name: " + newFolder.getName() + ", path: " + newFolder.getPath());
		
		newFolder.delete();
	}

	@Test(expected=CmisObjectNotFoundException.class)
	public void shouldDeleteFolder() {
		Session session = cmisSession.getSession();
		
		Folder folder;
		
		try {
			folder = (Folder)session.getObjectByPath("/test-folder");
		}
		catch (CmisObjectNotFoundException e) {
			Folder root = session.getRootFolder();

			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, "test-folder");

			// create the folder
			folder = root.createFolder(properties);
		}
		
		folder.delete();
		folder = (Folder)session.getObjectByPath("/test-folder");
	}

	@Test
	public void shouldFindFoder() {
		Session session = cmisSession.getSession();
		
		Folder root = session.getRootFolder();

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, "test-folder");

		// create the folder
		Folder folder = root.createFolder(properties);
		folder = (Folder)session.getObjectByPath("/test-folder");
		assertNotNull("folder test-folder not found", folder);
		logger.info("folder test-folder Id: " + folder.getId() + ", name: " + folder.getName() + ", path: " + folder.getPath());
		
		folder.delete();
	}

}
