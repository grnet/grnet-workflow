package gr.cyberstream.workflow.engine.cmis.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
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
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
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
public class CMISDocument {

	final static Logger logger = LoggerFactory.getLogger(CMISDocument.class);

	@Autowired
	private CMISSession cmisSession;

	private void cleanUp() {
		try {
			Session session = cmisSession.getSession();

			CmisObject object = session.getObjectByPath("/test-folder");
			Folder delFolder = (Folder) object;
			delFolder.deleteTree(true, UnfileObject.DELETE, true);
		} catch (CmisObjectNotFoundException e) {
			System.err.println("No need to clean up.");
		}
	}

	@Test
	public void shouldCreateDocument() {
		Session session = cmisSession.getSession();

		cleanUp();

		Folder root = session.getRootFolder();

		// create folder
		// properties
		// (minimal set: name and object type id)
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, "test-folder");

		// create the folder
		Folder newFolder = root.createFolder(properties);

		// create document
		Map<String, String> props = new HashMap<String, String>();
		props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		props.put(PropertyIds.NAME, "test-doc");
		String content = "This is a test document";
		byte[] buf = null;
		try {
			buf = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream input = new ByteArrayInputStream(buf);
		ContentStream contentStream = session.getObjectFactory().createContentStream("test-doc", buf.length,
				"text/plain; charset=UTF-8", input);
		Document newDoc = newFolder.createDocument(props, contentStream, VersioningState.MAJOR);

		assertNotNull("new document creation failed", newDoc);
		logger.info("New doc Id: " + newDoc.getId() + ", name: " + newDoc.getName() + ", path: "
				+ newDoc.getPaths().get(0));
	}

	@Test
	public void shouldDeleteDocument() {
		Session session = cmisSession.getSession();

		try {
			CmisObject object = session.getObjectByPath("/test-folder/test-doc");
			Document delDoc = (Document) object;
			delDoc.delete(true);
		} catch (CmisObjectNotFoundException e) {
			System.err.println("Document is not found: test-doc");
		} catch (Exception e) {
			fail("Error while deleting test-doc");
			e.printStackTrace();
		}
	}

	@Test
	public void testVersioning() throws Exception {
		Session session = cmisSession.getSession();

		cleanUp();
		
		// Create test folder
		Map<String, Object> folderProperties = new HashMap<String, Object>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		folderProperties.put(PropertyIds.NAME, "test-folder");
		Folder folder = session.getRootFolder().createFolder(folderProperties);

		// Create the document from the test file
		Map<String, Object> fileProperties = new HashMap<String, Object>();
		fileProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		fileProperties.put(PropertyIds.NAME, "file");
		fileProperties.put(PropertyIds.VERSION_LABEL, "initial");

		String[] contents = new String[] { "Hello World!", "Hello World", "Hello" };

		ContentStream cos = session.getObjectFactory().createContentStream("file", contents[2].length(), "text/plain",
				new ByteArrayInputStream(contents[2].getBytes()));

		// create a major version
		Document file = folder.createDocument(fileProperties, cos, VersioningState.MAJOR);

		// ---------------------------------
		ObjectId nextVersion = file.checkOut();
		Document file2 = (Document) session.getObject(nextVersion);

		cos = session.getObjectFactory().createContentStream("file", contents[1].length(), "text/plain",
				new ByteArrayInputStream(contents[1].getBytes()));

		file2.checkIn(true, null, cos, "version1");

		// ---------------------------------
		nextVersion = file.checkOut();
		Document file3 = (Document) session.getObject(nextVersion);

		cos = session.getObjectFactory().createContentStream("file", contents[0].length(), "text/plain",
				new ByteArrayInputStream(contents[0].getBytes()));

		file3.checkIn(true, null, cos, "version2");

		// ---------------------------------------
		List<Document> versions = file.getAllVersions();
		assertEquals(3, versions.size());

		int i = 0;
		for (Document doc : versions) {
			String verId = doc.getId();
			Document versDoc = (Document) session.getObject(verId);
			String s = IOUtils.toString(versDoc.getContentStream().getStream());

			assertEquals(contents[i], s);
			i++;
		}

	}
}
