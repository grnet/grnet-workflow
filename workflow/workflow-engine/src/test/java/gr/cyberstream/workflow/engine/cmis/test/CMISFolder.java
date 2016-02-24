package gr.cyberstream.workflow.engine.cmis.test;

import static org.junit.Assert.*;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.util.FileUtils;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CMISFolder {
	
	final static Logger logger = LoggerFactory.getLogger(CMISFolder.class);

	@Autowired
	private gr.cyberstream.workflow.engine.cmis.CMISFolder cmisFolder;
	
	@Autowired
	private gr.cyberstream.workflow.engine.cmis.CMISSession cmisSession;

	@Test
	public void shouldCreateFolder() {
		
		Folder root = FileUtils.getFolder("/", cmisSession.getSession());
		
		Folder newFolder = FileUtils.createFolder(root, "Test Folder", BaseTypeId.CMIS_FOLDER.value());
		
		//Folder newFolder = cmisFolder.createFolder(null, "Test Folder");
		
		assertNotNull("New folder creation failed", newFolder);
		logger.info("New folder Id: " + newFolder.getId() + ", name: " + newFolder.getName() + ", path: " + newFolder.getPath());
		
		//cmisFolder.deleteFolderByPath("Test Folder");
	}
	
	@Test
	public void shouldCreateSubFolder() {
		
		Folder folder = FileUtils.getFolder("/Test Workflow Definition", cmisSession.getSession());
		
		//Folder newFolder = FileUtils.createFolder(folder, "Test Folder", BaseTypeId.CMIS_FOLDER.value());
		
		Folder newFolder = cmisFolder.createFolder(folder, "Test Folder");
		
		assertNotNull("New folder creation failed", newFolder);
		logger.info("New folder Id: " + newFolder.getId() + ", name: " + newFolder.getName() + ", path: " + newFolder.getPath());
		
		//cmisFolder.deleteFolderByPath("Test Folder");
	}

	@Test(expected=CmisObjectNotFoundException.class)
	public void shouldDeleteFolder() {
		
		Session session = cmisSession.getSession();
		
		Folder folder;
		
		try {
			
			folder = FileUtils.getFolder("/Test Folder", session);
			
			//folder = cmisFolder.getFolderByPath("/Test Folder");
			
		} catch (CmisObjectNotFoundException e) {
			
			Folder root = FileUtils.getFolder("/", cmisSession.getSession());
			
			folder = FileUtils.createFolder(root, "Test Folder", BaseTypeId.CMIS_FOLDER.value());
			
			//folder = cmisFolder.createFolder(null, "Test Folder");
		}
		
		FileUtils.delete(folder.getId(), session);
				
		//cmisFolder.deleteFolderByPath(folder.getId());
		
		folder = FileUtils.getFolder("/Test Folder", session);
		
		//folder = cmisFolder.getFolderByPath("/Test Folder");	
	}

	@Test
	public void shouldFindFolder() {
		
		Session session = cmisSession.getSession();
		
		Folder folder = null;
		
		try {
			
			FileUtils.getFolder("/Test Folder", session);
			
			//folder = cmisFolder.getFolderByPath("/Test Folder");
			
		} catch (CmisObjectNotFoundException e) {
			
			Folder root = FileUtils.getFolder("/", cmisSession.getSession());
			
			folder = FileUtils.createFolder(root, "Test Folder", BaseTypeId.CMIS_FOLDER.value());
			
			//folder = cmisFolder.createFolder(null, "Test Folder");
		}
		
		try {
			
			FileUtils.getFolder(folder.getId(), session);
			
			//folder = cmisFolder.getFolderById(folder.getId());
		
			cmisFolder.deleteFolderById(folder.getId());
			
		} catch (CmisObjectNotFoundException e) {
			
			assert false;
		}
	}

}
