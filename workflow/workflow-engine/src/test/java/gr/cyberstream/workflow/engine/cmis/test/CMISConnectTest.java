package gr.cyberstream.workflow.engine.cmis.test;

import static org.junit.Assert.*;

import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.cmis.CMISSession;

@ContextConfiguration(classes = CmisConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CMISConnectTest {
	
	final static Logger logger = LoggerFactory.getLogger(CMISConnectTest.class);

	@Autowired
	private CMISSession cmisSession;

	@Test
	public void testCleanUp() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSession() {
		Session session = cmisSession.getSession();
		
		assertNotNull("CMIS Session should not be null", session);
		logger.info(session.toString());
	}

	@Test
	public void testSetSession() {
		fail("Not yet implemented");
	}

}
