package gr.cyberstream.workflow.engine.persistence.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import gr.cyberstream.workflow.engine.model.DefinitionVersion;
import gr.cyberstream.workflow.engine.model.ExternalGroup;
import gr.cyberstream.workflow.engine.model.ExternalUser;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.persistence.Processes;

@ContextConfiguration(classes = PersistenceConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessesTest {

	final static Logger logger = LoggerFactory.getLogger(ProcessesTest.class);

	@Autowired
	private Processes processRepository;

	@PersistenceContext
	private EntityManager entityManager;

	/*
	 * @Test
	 * 
	 * @Transactional public void testGetAll() { private int dummyCount;
	 * 
	 * final Date nowDate = new Date();
	 * 
	 * WorkflowDefinition dummyProcess = new WorkflowDefinition();
	 * dummyProcess.setName("dummy name " + nowDate);
	 * 
	 * processRepository.save(dummyProcess); List<WorkflowDefinition> pl =
	 * processRepository.getAll(); int oldCount = pl.size();
	 * 
	 * entityManager.detach(dummyProcess); dummyProcess.setId(0);
	 * processRepository.save(dummyProcess); pl = processRepository.getAll();
	 * 
	 * logger.info("old count: " + oldCount + ", new count: " + pl.size());
	 * assertTrue((pl.size() == oldCount + 1));
	 * 
	 * dummyCount = 0; pl.forEach(new Consumer<WorkflowDefinition>() {
	 * 
	 * @Override public void accept(WorkflowDefinition t) { if
	 * (t.getName().equals("dummy name " + nowDate)) { logger.info("name: " +
	 * t.getName()); dummyCount++; } } }); assertTrue(dummyCount == 2); }
	 */

	/*
	 * @Test public void testSave() { WorkflowDefinition dummyProcess = new
	 * WorkflowDefinition(); dummyProcess.setName("dummy name");
	 * 
	 * dummyProcess = processRepository.save(dummyProcess);
	 * assertTrue(dummyProcess.getId() > 0); }
	 * 
	 * 
	 * @Test(expected = DataIntegrityViolationException.class) public void
	 * testProcessDefinitionRequiredName() { WorkflowDefinition dummyProcess =
	 * new WorkflowDefinition(); dummyProcess.setName(null);
	 * 
	 * dummyProcess = processRepository.save(dummyProcess); }
	 */

	@Test
	public void shouldGetAllDefinitions() {

		try {
			assertTrue(processRepository.getAll() != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	public void shouldGetActiveDefinitions() {

		try {
			assertTrue(processRepository.getActiveProcessDefintions() != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldSaveDefinition() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setId(9999);
		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition");
		workflowDefinition.setFolderId("A test folder id");

		try {
			assertTrue(processRepository.save(workflowDefinition) != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetDefinitionById() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition");
		workflowDefinition.setFolderId("A test folder id");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			assertTrue(workflowDefinitionId != 0);
			assertTrue(processRepository.getById(workflowDefinitionId) != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetDefinitionByName() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v1");
		workflowDefinition.setFolderId("A test folder id");

		try {
			String workflowDefinitionName = processRepository.save(workflowDefinition).getName();
			assertTrue(workflowDefinitionName != null);
			assertTrue(processRepository.getByName(workflowDefinitionName) != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldDeleteDefinitionById() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2");
		workflowDefinition.setFolderId("A test folder id");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			assertTrue(workflowDefinitionId > 0);
			processRepository.delete(workflowDefinitionId);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldsaveDefinitionVersion() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2");
		workflowDefinition.setFolderId("A test folder id");

		DefinitionVersion definitionVersion = new DefinitionVersion();

		definitionVersion.setId(101010);
		definitionVersion.setVersion(1);
		definitionVersion.setWorkflowDefinition(workflowDefinition);
		definitionVersion.setDeploymentId("2");
		definitionVersion.setDeploymentdate(new Date());
		definitionVersion.setStatus("active");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			assertTrue(processRepository.saveVersion(workflowDefinitionId, definitionVersion) != null);

		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetVersionByDeploymentId() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2");
		workflowDefinition.setFolderId("A test folder id");

		DefinitionVersion definitionVersion = new DefinitionVersion();

		definitionVersion.setId(101010);
		definitionVersion.setVersion(1);
		definitionVersion.setWorkflowDefinition(workflowDefinition);
		definitionVersion.setDeploymentId("2");
		definitionVersion.setDeploymentdate(new Date());
		definitionVersion.setStatus("active");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			assertTrue(processRepository.saveVersion(workflowDefinitionId, definitionVersion) != null);
			assertTrue(processRepository.getVersionByDeploymentId("2") != null);

		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetVersionById() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2s");
		workflowDefinition.setFolderId("A test folder id");

		DefinitionVersion definitionVersion = new DefinitionVersion();

		definitionVersion.setVersion(3);
		definitionVersion.setWorkflowDefinition(workflowDefinition);
		definitionVersion.setDeploymentId("3");
		definitionVersion.setDeploymentdate(new Date());
		definitionVersion.setStatus("active");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			int versionId = processRepository.saveVersion(workflowDefinitionId, definitionVersion).getId();
			assertTrue(versionId > 0);
			assertTrue(processRepository.getVersionById(versionId) != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetVersionsByProcessId() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2s");
		workflowDefinition.setFolderId("A test folder id");

		DefinitionVersion definitionVersion = new DefinitionVersion();

		definitionVersion.setVersion(3);
		definitionVersion.setWorkflowDefinition(workflowDefinition);
		definitionVersion.setDeploymentId("3");
		definitionVersion.setDeploymentdate(new Date());
		definitionVersion.setStatus("active");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			int versionId = processRepository.saveVersion(workflowDefinitionId, definitionVersion).getId();
			assertTrue(versionId > 0);
			assertTrue(processRepository.getVersionsByProcessId(workflowDefinitionId) != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void shouldSaveInstance() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2s");
		workflowDefinition.setFolderId("A test folder id");

		DefinitionVersion definitionVersion = new DefinitionVersion();

		definitionVersion.setVersion(3);
		definitionVersion.setWorkflowDefinition(workflowDefinition);
		definitionVersion.setDeploymentId("3");
		definitionVersion.setDeploymentdate(new Date());
		definitionVersion.setStatus("active");
		
		WorkflowInstance workflowInstance = new WorkflowInstance();
		workflowInstance.setClient("MOBILE");
		workflowInstance.setFolderId("folder id");
		workflowInstance.setId("2121212");
		workflowInstance.setReference("A References");
		workflowInstance.setStartDate(new Date());
		workflowInstance.setStatus("running");
		workflowInstance.setTitle("A Test instance");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			processRepository.saveVersion(workflowDefinitionId, definitionVersion);
			workflowInstance.setDefinitionVersion(definitionVersion);
			
			assertTrue(processRepository.save(workflowInstance) != null);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetInstanceById() {
		WorkflowDefinition workflowDefinition = new WorkflowDefinition();

		workflowDefinition.setAssignBySupervisor(false);
		workflowDefinition.setName("Test definition v2s");
		workflowDefinition.setFolderId("A test folder id");

		DefinitionVersion definitionVersion = new DefinitionVersion();

		definitionVersion.setVersion(3);
		definitionVersion.setWorkflowDefinition(workflowDefinition);
		definitionVersion.setDeploymentId("3");
		definitionVersion.setDeploymentdate(new Date());
		definitionVersion.setStatus("active");
		
		WorkflowInstance workflowInstance = new WorkflowInstance();
		workflowInstance.setClient("MOBILE");
		workflowInstance.setFolderId("folder id");
		workflowInstance.setId("2121212");
		workflowInstance.setReference("A References");
		workflowInstance.setStartDate(new Date());
		workflowInstance.setStatus("running");
		workflowInstance.setTitle("A Test instance");

		try {
			int workflowDefinitionId = processRepository.save(workflowDefinition).getId();
			processRepository.saveVersion(workflowDefinitionId, definitionVersion);
			workflowInstance.setDefinitionVersion(definitionVersion);
			String instanceId = processRepository.save(workflowInstance).getId();
			
			logger.info(processRepository.getInstanceById(instanceId).getStatus());
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		
	}

	@Test
	public void shouldCreateExternalGroup() {
		ExternalGroup group = new ExternalGroup();
		group.setName("Group name");
		group.setOrderCode(6);
		processRepository.createExternalGroup(group);
	}

	@Test
	public void shouldGetDefinition() {
		try {
			WorkflowDefinition def = processRepository.getProcessByDefinitionId("waterSupplyNetworkDamage:1:5004");
			logger.info(def.getName());
			assertTrue(def != null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldSaveMobileUser() {
		try {
			ExternalUser mobileUser = new ExternalUser();
			mobileUser.setId(2);
			mobileUser.setDeviceId("0101010101011101");
			mobileUser.setClient("ANDROID");
			mobileUser.setPhoneNumber("1234567890");

			processRepository.saveExternalUser(mobileUser);
			assertTrue(true);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}

	@Test
	@Transactional
	@Rollback(true)
	public void shouldGetDefinitionVersion() {
		
		WorkflowDefinition definition = processRepository.getDefinitionByKey("sidewalkDamageReport:5:50641");
		int version = 4;
		
		try {
			
			DefinitionVersion definitionVersion = processRepository.getDefinitionVersion(definition, version);
			assertTrue(definitionVersion != null);

		} catch (Exception e) {
			
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
}
