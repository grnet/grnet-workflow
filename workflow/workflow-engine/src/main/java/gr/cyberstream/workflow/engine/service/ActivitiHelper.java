/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.form.DefaultStartFormHandler;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import gr.cyberstream.workflow.engine.util.string.StringUtil;

/**
 * Helper functions for simplifying common tasks for Activiti processes etc.
 * 
 * @author nlyk
 *
 */

public class ActivitiHelper {
	
	/**
	 * Get a process definition using the deployment id
	 * 
	 * @param activitiRepository
	 * @param deploymentId
	 * @return
	 */
	public static ProcessDefinition getProcessDefinitionByDeploymentId(RepositoryService activitiRepository,
			String deploymentId) throws ActivitiException {

		List<ProcessDefinition> processDefs = activitiRepository.createProcessDefinitionQuery()
				.deploymentId(deploymentId).list();

		if (processDefs == null || processDefs.size() == 0) {
			throw new ActivitiException("Invalid process definition");
		}

		return processDefs.get(0);
	}

	/**
	 * Create a new deployment from a BPMN file
	 * 
	 * @param inputStream
	 *            the input stream for the BPMN file
	 * @param filename
	 *            the original filename
	 * @return the new deployment
	 */
	public static Deployment createDeployment(RepositoryService activitiRepository, String input,
			String filename) throws ActivitiException {

		Deployment deployment;

		// 1. Deploy the BPMN file to Activiti repository service ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		filename = StringUtil.isEmpty(filename) ? "noname.bpmn20.xml" : filename;

		try {
			deployment = activitiRepository.createDeployment().addString("input.bpmn20.xml", input)
					.name(filename).deploy();
		} catch (XMLException ex) {
			String message = "The BPMN input is not valid. Error string: " + ex.getMessage();
			throw new ActivitiException(message);
		}

		// 2. Check deployment and get metadata from the deployed process definition ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (deployment == null) {
			throw new ActivitiException("The BPMN input is not valid");
		}

		return deployment;
	}
	
	/**
	 * 
	 * @param activitiRepository
	 * @param processDefinitionId
	 * @param taskId
	 * @param task 
	 * @return
	 */
	public static List<FormProperty> getTaskFormDefinition(RepositoryService activitiRepository, 
			String processDefinitionId, String taskId){
		
		List<FormProperty> formProperties = new ArrayList<FormProperty>();
		
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl)activitiRepository).getDeployedProcessDefinition(processDefinitionId);
		
		TaskFormHandler taskFormHandler = pde.getTaskDefinitions().get(taskId).getTaskFormHandler();
		
		if(taskFormHandler instanceof DefaultTaskFormHandler) {
			DefaultTaskFormHandler defaultTaskFormHandler = (DefaultTaskFormHandler) taskFormHandler;
			List<FormPropertyHandler> formPropertyHandlers = defaultTaskFormHandler.getFormPropertyHandlers();
			if(formPropertyHandlers != null && !formPropertyHandlers.isEmpty()) {
				for(FormPropertyHandler formPropertyHandler : formPropertyHandlers){
					
					FormProperty formProperty = new FormProperty();
					formProperty.setId(formPropertyHandler.getId());
					formProperty.setName(formPropertyHandler.getName());
					formProperty.setType(formPropertyHandler.getType().getName());
					formProperty.setReadable(formPropertyHandler.isReadable());
					formProperty.setWriteable(formPropertyHandler.isWritable());
					formProperty.setRequired(formPropertyHandler.isRequired());
					formProperty.setDatePattern((String)formPropertyHandler.getType().getInformation("datePattern"));
					
					if(formProperty.getType().equals("enum")){
						formProperty.setFormValues(
								getBpmnFormProperty(
										taskId, 
										formProperty.getId(),
										activitiRepository,
										processDefinitionId)
								.getFormValues()
						);						
					}else if (formProperty.getType().equals("date")){
						formProperty.setFormValues(
								getBpmnFormProperty(
										taskId, 
										formProperty.getId(),
										activitiRepository,
										processDefinitionId)
								.getFormValues()
						);	
					}
					
					formProperties.add(formProperty);
				}
				
			}
		}
				
		
		return formProperties;
	}
	
	/**
	 * Returns the task form definition for the start event
	 * 
	 * @param activitiRepository
	 * @param processDefinitionId
	 * @return
	 */
	public static List<FormProperty> getTaskFormDefinition(RepositoryService activitiRepository, String processDefinitionId) {
		
		List<FormProperty> formProperties = new ArrayList<FormProperty>();
		
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl)activitiRepository).getDeployedProcessDefinition(processDefinitionId);
		
		StartFormHandler startFormHandler = pde.getStartFormHandler();
		
		if(startFormHandler instanceof DefaultStartFormHandler) {
			DefaultStartFormHandler defaultStartFormHandler = (DefaultStartFormHandler) startFormHandler;
			List<FormPropertyHandler> formPropertyHandlers = defaultStartFormHandler.getFormPropertyHandlers();
			if(formPropertyHandlers != null && !formPropertyHandlers.isEmpty()) {
				for(FormPropertyHandler formPropertyHandler : formPropertyHandlers){
					
					FormProperty formProperty = new FormProperty();
					formProperty.setId(formPropertyHandler.getId());
					formProperty.setName(formPropertyHandler.getName());
					formProperty.setType(formPropertyHandler.getType().getName());
					formProperty.setReadable(formPropertyHandler.isReadable());
					formProperty.setWriteable(formPropertyHandler.isWritable());
					formProperty.setRequired(formPropertyHandler.isRequired());
					formProperty.setDatePattern((String)formPropertyHandler.getType().getInformation("datePattern"));
					
					if(formProperty.getType().equals("enum")){
						formProperty.setFormValues(
								
								getBpmnStartEventFormProperty(
										formProperty.getId(),
										activitiRepository,
										processDefinitionId)
								.getFormValues()
						);						
					}else if (formProperty.getType().equals("date")){
						formProperty.setFormValues(
								getBpmnStartEventFormProperty(
										formProperty.getId(),
										activitiRepository,
										processDefinitionId)
								.getFormValues()
						);	
					}
					
					formProperties.add(formProperty);
				}
				
			}
		}
				
		
		return formProperties;
	}
	
	
	public static FormProperty getBpmnStartEventFormProperty(String formEntryId, RepositoryService activitiRepository, String processDefinitionId) {
		
		BpmnModel model = activitiRepository.getBpmnModel(processDefinitionId);
		
		List<org.activiti.bpmn.model.Process> processes = model.getProcesses();
		
		for (org.activiti.bpmn.model.Process p : processes) {
			List<StartEvent> startEvents = p.findFlowElementsOfType(StartEvent.class);
			
			if(startEvents != null && startEvents.size() > 0){
				for(StartEvent startEvent : startEvents) {
					StartEvent itemDefinitions = (StartEvent) model.getFlowElement(startEvent.getId());
					List<FormProperty> formProperties = itemDefinitions.getFormProperties();
					
					for (FormProperty formProperty : formProperties) {
						if (formProperty.getId().equals(formEntryId)) {
							return formProperty;
						}
					}
				}
			}
		}
		return null;
	}

	public static FormProperty getBpmnFormProperty(String taskId, String formEntryId, RepositoryService activitiRepository, String processDefinitionId) {
		
		BpmnModel model = activitiRepository.getBpmnModel(processDefinitionId);
		
		UserTask itemDefinitions = (UserTask) model.getFlowElement(taskId);
		
		List<FormProperty> formProperties = itemDefinitions.getFormProperties();		
		for (FormProperty formProperty : formProperties) {
			if (formProperty.getId().equals(formEntryId)) {
				return formProperty;
			}
		}
		
		return null;
	}
}
