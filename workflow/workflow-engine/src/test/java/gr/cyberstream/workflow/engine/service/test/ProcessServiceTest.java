package gr.cyberstream.workflow.engine.service.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gr.cyberstream.workflow.engine.config.test.ApplicationConfiguration;
import gr.cyberstream.workflow.engine.model.TaskPath;
import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;
import gr.cyberstream.workflow.engine.model.api.WfTask;
import gr.cyberstream.workflow.engine.model.api.WfTaskDetails;
import gr.cyberstream.workflow.engine.persistence.Processes;
import gr.cyberstream.workflow.engine.service.CustomException;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import gr.cyberstream.workflow.engine.service.ProcessService;

@ContextConfiguration(classes = ApplicationConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessServiceTest {

	final static Logger logger = LoggerFactory.getLogger(ProcessServiceTest.class);

	@Autowired
	private ProcessService processService;
	
	@Autowired
	private Processes processRepository;
		
	@Test
	public void shouldCreateNewProcessDefinition() {
		
		WorkflowDefinition process = new WorkflowDefinition();
		process.setName("Test Workflow Definition");
		process.setDescription("Test Workflow Definition Description");
		process.setIcon("box.svg");
		process.setKey(null);
		process.setOwner("Procurements");
		process.setAssignBySupervisor(true);
		
		WfProcess wfProcess = new WfProcess(process);
		
		try {
			
			processService.createNewProcessDefinition(wfProcess);
			
		} catch (InvalidRequestException e) {
			
			logger.error(e.getMessage());

			assertTrue(false);
		}
	}
	
	@Test
	public void shouldUpdateProcessDefinition() {
		
		WorkflowDefinition process = processRepository.getByName("process_pool");
		
		process.setName("Test Workflow Definition");
		process.setDescription("Test Workflow Definition Description");
		process.setIcon("box.svg");
		
		process.setOwner("Procurements");
		process.setAssignBySupervisor(true);
		
		WfProcess wfProcess = new WfProcess(process);
		
		try {
			
			processService.update(wfProcess);
			
		} catch (InvalidRequestException e) {

			assertTrue(false);
		}
	}
	
	@Test
	public void shouldCreateNewProcessDefinitionWithBPMN() {
		
		String filename = "temporary-public-tender-test.v0.2.bpmn";
		
		ClassPathResource bpmnFile = new ClassPathResource(filename);
		
		try {
			
			processService.createNewProcessDefinition(bpmnFile.getInputStream(), filename);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} catch (InvalidRequestException e) {
			
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldStartProcess() {
		
		int processId = 3;
		
		WfProcessInstance wfProcessInstance = new WfProcessInstance();
		
		wfProcessInstance.setTitle("Three in a row");
				
		try {	
			processService.startProcess(processId, wfProcessInstance);
			
		} catch (CustomException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldSaveDocument() {
		
		String instanceId = "32501";
		
		WfDocument document = new WfDocument();
		
		document.setTitle("RFP Document");
		document.setDocumentId("56b53cec-9405-4395-9fcf-1a506d5bfc8b");
		document.setRefNo("12341234-12341234");
		
		ClassPathResource rfpFile = new ClassPathResource("Test.pdf");
		String contentType = "application/pdf";
		
		try {
			
			processService.saveDocument(instanceId, "rfp", document, rfpFile.getInputStream(), contentType);
			
		} catch (InvalidRequestException e) {
			
			assertTrue(false);
			
		} catch (IOException e) {
			
			assertTrue(false);
		}
	}
	
	//TODO:vpap
	@Test
	public void shouldReturnTaskDetailsOfVersion(){
		int versionId = 9;
		
		List<WfTaskDetails> wfTaskDetails;
		try{
			wfTaskDetails = processService.getVersionTaskDetails(versionId);
			assertTrue(wfTaskDetails!=null);
			for(WfTaskDetails wf : wfTaskDetails){
				if(wf.getDefinitionVersionId()!=versionId)	assertTrue(false);
			}
		}
		catch(Exception e){
			assertTrue(false);
		}		
	}
	
//	@Test
//	public void getTaskForm(){
//		
//		List<WfTask> tasks = processService.getUnassingedTasksByInstanceIds(Arrays.asList(new String[]{"5001"}));
//		
//		assertTrue(tasks.size() > 0);
//	}
	
	@Test
	public void shouldClaimTasksAccordingUser(){
		
		String userId = "e5738b21-34b8-4d22-a195-b87f447b5ae9";
		
		try{
			
			assertTrue(processService.getCandidateUserTasks() != null);
		}
		catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(false);
		}	
		
	}
	
	@Test
	public void shouldCreateTaskPath(){
		
		try{
			TaskPath path = processRepository.getTaskPath("7502", "theTask");
			logger.info("Definition name : " + path.getDefinition().getName());
			assertTrue(processRepository.getTaskPath("7502", "theTask") != null);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(false);
		}
	}
}
