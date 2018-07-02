package gr.cyberstream.workflow.engine.service.test;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAccount;
import gr.cyberstream.workflow.engine.config.test.MockKeycloakAuthenticationToken;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.service.DefinitionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DefinitionServiceTest {

	@Autowired
	private DefinitionService definitionService;

	private static final Logger logger = LoggerFactory.getLogger(DefinitionServiceTest.class);

	@Before
	public void setup() {
		String name = "Kostas Koutros";
		String email = "kostas.koutros@cyberstream.gr";
		Set<String> roles = Sets.newSet(new String[] { "ROLE_Admin" });
		List<String> groups = Arrays.asList(new String[] { "HR" });

		KeycloakAuthenticationToken authentication = new MockKeycloakAuthenticationToken(new MockKeycloakAccount(name, email, roles, groups));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void shouldGetProcessById() {
		int definitionId = 87;

		try {
			// should throw exception if not found
			WfProcess wfProcess = definitionService.getProcessById(definitionId);
			assertTrue(wfProcess != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	/**
	 * All processes based on logged in user
	 */
	@Test
	public void shouldGetAllDefinitionsBasedOnUser() {
		try {
			// if user is not admin or doesnt belong to same group as definition it will return an empty list
			List<WfProcess> wfProcesses = definitionService.getAllProcesses();
			assertTrue(wfProcesses != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetActiveDefinitions() {
		try {
			List<WfProcess> wfProcesses = definitionService.getActiveProcessDefinitions();
			assertTrue(wfProcesses != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetAllDefinitions() {
		try {
			List<WfProcess> wfProcesses = definitionService.getProcessDefinitions();
			assertTrue(wfProcesses != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetAllDefinitionsByOwner() {
		String owner = "Admin-Support";
		
		try {
			List<WfProcess> wfProcesses = definitionService.getDefinitionsByOwner(owner);
			assertTrue(wfProcesses != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetAllDefinitionsByOwners() {
		List<String> ownerList = new ArrayList<>();
		String adminSupport = "Admin-Support";
		String hr = "HR";
		
		ownerList.add(adminSupport);
		ownerList.add(hr);
		
		
		try {
			List<WfProcess> wfProcesses = definitionService.getDefinitionsByOwners(ownerList);
			assertTrue(wfProcesses != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldUpdateAProcess() {
		WfProcess processToUpdate = new WfProcess();
		processToUpdate.setActive(true);
		processToUpdate.setId(87);
		processToUpdate.setName("Αναφορά βλάβης δικτύου ύδρευσης");
		
		try {
			WfProcess wfProcess = definitionService.update(processToUpdate);
			assertTrue(wfProcess != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldCreateProcessDefinition() throws IOException {
		try {
			byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/integrationTest.bpmn"));
			MockMultipartFile file = new MockMultipartFile("file", "", MediaType.TEXT_XML_VALUE, content);
			String justification = "Test justification";
			WfProcess wfProcess = definitionService.createNewProcessDefinition(file.getInputStream(), file.getOriginalFilename(), justification);
			assertTrue(wfProcess != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetProcessDiagram() {
		int processId = 87;
		
		try {
			InputStreamResource inputStreamResource = definitionService.getProcessDiagram(processId);
			assertTrue(inputStreamResource != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldDeleteProcess() {
		int processId = 87;
		
		try {
			definitionService.deleteProcessDefinition(processId);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldCreateNewProcessVersion() {
		int definitionId = 87;
		
		try {
			byte[] content = Files.readAllBytes(Paths.get("C:/temp/bpmn/WaterSupplyDamageReport-Localhost.bpmn"));
			MockMultipartFile file = new MockMultipartFile("file", "", MediaType.TEXT_XML_VALUE, content);
			String justification = "Test justification";

			WfProcessVersion wfProcessVersion = definitionService.createNewProcessVersion(definitionId, file.getInputStream(), file.getOriginalFilename(),
					justification);
			assertTrue(wfProcessVersion != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldDeleteProcessDefinitionVersion() {
		int processId = 87;
		String activeDeploymentId = "227501";
		
		try {
			// it will throw an exception if there is only one version: (Trying to delete the last version. Delete the process definition instead)
			WfProcess wfProcess = definitionService.deleteProcessDefinitionVersion(processId, activeDeploymentId);
			assertTrue(wfProcess != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldSetActiveVersion() {
		int processId = 87;
		int versionId = 87;
		
		try {
			WfProcess wfProcess = definitionService.setActiveVersion(processId, versionId);
			assertTrue(wfProcess != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback
	public void shouldDeactivateVersion() {
		int processId = 87;
		int versionId = 87;
		
		try {
			WfProcessVersion wfProcessVersion = definitionService.deactivateVersion(processId, versionId);
			assertTrue(wfProcessVersion != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldGetProcessMetadata() {
		int processId = 87;
		String device = "MOBILE";
		
		try {
			WfProcess wfProcess = definitionService.getProcessMetadata(processId, device);
			assertTrue(wfProcess != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

}
