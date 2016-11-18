package gr.cyberstream.workflow.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
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
	 * @param repositoryService
	 *            {@link RepositoryService}
	 * 
	 * @param deploymentId
	 *            The deployment id
	 *            
	 * @return {@link ProcessDefinition}
	 * @throws ActivitiException
	 */
	public static ProcessDefinition getProcessDefinitionByDeploymentId(RepositoryService repositoryService, String deploymentId) throws ActivitiException {
		
		List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list();

		if (processDefinitions == null || processDefinitions.size() == 0)
			throw new ActivitiException("Invalid process definition");

		return processDefinitions.get(0);
	}

	/**
	 * Creates a new deployment using BPMN file
	 * 
	 * @param repositoryService
	 *            {@link RepositoryService}
	 * 
	 * @param input
	 *            The input stream of the BPMN file
	 * 
	 * @param filename
	 *            The original file name
	 * 
	 * @return {@link Deployment}
	 * @throws ActivitiException
	 */
	public static Deployment createDeployment(RepositoryService repositoryService, String input, String filename) throws ActivitiException {

		Deployment deployment;

		// set a default file name if is not present
		filename = StringUtil.isEmpty(filename) ? "noname.bpmn20.xml" : filename;

		// deploy the BPMN file to Activiti repository service
		try {
			deployment = repositoryService.createDeployment().addString("input.bpmn20.xml", input).name(filename).deploy();

		} catch (XMLException ex) {
			String message = "The BPMN input is not valid. Error string: " + ex.getMessage();
			throw new ActivitiException(message);
		}

		// check deployment and get metadata from the deployed process
		if (deployment == null)
			throw new ActivitiException("The BPMN input is not valid");

		return deployment;
	}
	
	/**
	 * Returns task's form definition.<br>
	 * Used for completed tasks only.<br>
	 * What it does, is to get the task's form elements by its bpmn model and fill the value of each property.<br>
	 * Thats not available from any of activiti service, so we had to make one.
	 * 
	 * @param repositoryService {@link RepositoryService}
	 * 
	 * @param processDefinitionId The process definition id
	 * 
	 * @param taskId The task id
	 * 
	 * @return A list of {@link FormProperty}
	 */
	public static List<FormProperty> getTaskFormDefinition(RepositoryService repositoryService, String processDefinitionId, String taskId) {
		
		List<FormProperty> formProperties = new ArrayList<FormProperty>();
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);
		TaskFormHandler taskFormHandler = pde.getTaskDefinitions().get(taskId).getTaskFormHandler();

		if (taskFormHandler instanceof DefaultTaskFormHandler) {
			DefaultTaskFormHandler defaultTaskFormHandler = (DefaultTaskFormHandler) taskFormHandler;
			List<FormPropertyHandler> formPropertyHandlers = defaultTaskFormHandler.getFormPropertyHandlers();
			if (formPropertyHandlers != null && !formPropertyHandlers.isEmpty()) {
				for (FormPropertyHandler formPropertyHandler : formPropertyHandlers) {

					FormProperty formProperty = new FormProperty();
					formProperty.setId(formPropertyHandler.getId());
					formProperty.setName(formPropertyHandler.getName());
					formProperty.setType(formPropertyHandler.getType().getName());
					formProperty.setReadable(formPropertyHandler.isReadable());
					formProperty.setWriteable(formPropertyHandler.isWritable());
					formProperty.setRequired(formPropertyHandler.isRequired());

					// for enumarated form properties
					if (formProperty.getType().equals("enum")) {
						// get the form values for the enumeration
						List<FormValue> formValues = getBpmnFormProperty(taskId, formProperty.getId(), repositoryService, processDefinitionId).getFormValues();

						formProperty.setFormValues(formValues);

					} else if (formProperty.getType().equals("date")) {
						List<FormValue> formValues = getBpmnFormProperty(taskId, formProperty.getId(), repositoryService, processDefinitionId).getFormValues();

						formProperty.setFormValues(formValues);
					}
					// add the form property to the return list
					formProperties.add(formProperty);
				}
			}
		}
		return formProperties;
	}

	/**
	 * Returns the task's form definition for the start event.<br>
	 * No task id is presented, since the start event is only one
	 * 
	 * @param repositoryService
	 *            {@link RepositoryService}
	 * 
	 * @param processDefinitionId
	 *            The process definition id
	 * 
	 * @return A list of {@link FormProperty}
	 */
	public static List<FormProperty> getTaskFormDefinition(RepositoryService repositoryService, String processDefinitionId) {

		List<FormProperty> formProperties = new ArrayList<FormProperty>();
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);
		StartFormHandler startFormHandler = pde.getStartFormHandler();

		if (startFormHandler instanceof DefaultStartFormHandler) {
			DefaultStartFormHandler defaultStartFormHandler = (DefaultStartFormHandler) startFormHandler;
			List<FormPropertyHandler> formPropertyHandlers = defaultStartFormHandler.getFormPropertyHandlers();
			if (formPropertyHandlers != null && !formPropertyHandlers.isEmpty()) {
				for (FormPropertyHandler formPropertyHandler : formPropertyHandlers) {

					FormProperty formProperty = new FormProperty();
					formProperty.setId(formPropertyHandler.getId());
					formProperty.setName(formPropertyHandler.getName());
					formProperty.setType(formPropertyHandler.getType().getName());
					formProperty.setReadable(formPropertyHandler.isReadable());
					formProperty.setWriteable(formPropertyHandler.isWritable());
					formProperty.setRequired(formPropertyHandler.isRequired());

					if (formProperty.getType().equals("enum")) {
						List<FormValue> formValues = getBpmnStartEventFormProperty(formProperty.getId(), repositoryService, processDefinitionId).getFormValues();
						formProperty.setFormValues(formValues);

					} else if (formProperty.getType().equals("date")) {
						List<FormValue> formValues = getBpmnStartEventFormProperty(formProperty.getId(), repositoryService, processDefinitionId).getFormValues();
						formProperty.setFormValues(formValues);
					}
					// add the form property to the return list
					formProperties.add(formProperty);
				}
			}
		}
		return formProperties;
	}

	public static FormProperty getBpmnStartEventFormProperty(String formEntryId, RepositoryService repositoryService, String processDefinitionId) {
		BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
		List<org.activiti.bpmn.model.Process> processes = model.getProcesses();

		for (org.activiti.bpmn.model.Process p : processes) {
			List<StartEvent> startEvents = p.findFlowElementsOfType(StartEvent.class);

			if (startEvents != null && startEvents.size() > 0) {
				for (StartEvent startEvent : startEvents) {
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
