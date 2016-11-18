package gr.cyberstream.workflow.engine.persistence.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.persistence.Processes;

@ContextConfiguration(classes = PersistenceConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessesTest {

	final static Logger logger = LoggerFactory.getLogger(ProcessesTest.class);

	@Autowired
	private Processes processRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private int dummyCount;

	@Test
	@Transactional
	public void testGetAll() {
		final Date nowDate = new Date();

		WorkflowDefinition dummyProcess = new WorkflowDefinition();
		dummyProcess.setName("dummy name " + nowDate);

		processRepository.save(dummyProcess);
		List<WorkflowDefinition> pl = processRepository.getAll();
		int oldCount = pl.size();

		entityManager.detach(dummyProcess);
		dummyProcess.setId(0);
		processRepository.save(dummyProcess);
		pl = processRepository.getAll();

		logger.info("old count: " + oldCount + ", new count: " + pl.size());
		assertTrue((pl.size() == oldCount + 1));

		dummyCount = 0;
		pl.forEach(new Consumer<WorkflowDefinition>() {
			@Override
			public void accept(WorkflowDefinition t) {
				if (t.getName().equals("dummy name " + nowDate)) {
					logger.info("name: " + t.getName());
					dummyCount++;
				}
			}
		});
		assertTrue(dummyCount == 2);
	}

	/**
	 * Verifies that processDefinition saves a new ProcessDefinition object
	 */
	@Test
	public void testSave() {
		WorkflowDefinition dummyProcess = new WorkflowDefinition();
		dummyProcess.setName("dummy name");

		dummyProcess = processRepository.save(dummyProcess);
		assertTrue(dummyProcess.getId() > 0);
	}

	/**
	 * Verifies that processDefinition saves a new ProcessDefinition object
	 */
	@Test(expected = DataIntegrityViolationException.class)
	public void testProcessDefinitionRequiredName() {
		WorkflowDefinition dummyProcess = new WorkflowDefinition();
		dummyProcess.setName(null);

		dummyProcess = processRepository.save(dummyProcess);
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

}
