package gr.cyberstream.workflow.engine.service;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import gr.cyberstream.workflow.engine.model.WorkflowDefinition;
import gr.cyberstream.workflow.engine.model.api.WfDocument;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;

public interface ExecutionService {

	/**
	 * Return all running instances for a process with the specified id.
	 * 
	 * @param id
	 *            The process definition id
	 * 
	 * @return List of {@link WfProcessInstance}
	 */
	public List<WfProcessInstance> getActiveProcessInstances(int id);

	/**
	 * Delete process instance
	 * 
	 * @param id
	 *            The process instance id
	 * 
	 * @throws InvalidRequestException
	 */
	public void cancelProcessInstance(String id) throws InvalidRequestException;

	/**
	 * Suspend process instance
	 * 
	 * @param id
	 *            The process instance id
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 */
	public WfProcessInstance suspendProcessInstance(String id) throws InvalidRequestException;

	/**
	 * Resume a suspended process instance
	 * 
	 * @param id
	 *            The process instance id
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 */
	public WfProcessInstance resumeProcessInstance(String id) throws InvalidRequestException;

	/**
	 * Creates an image based on the instance progress
	 * 
	 * @param instanceId
	 *            The process instance id
	 * @return {@link InputStreamResource}
	 */
	public InputStreamResource getInstanceProgressDiagram(String instanceId);

	/**
	 * Start a new process instance with form data
	 * 
	 * @param processId
	 *            The process definition id
	 * 
	 * @param instanceData
	 *            The form data in key-value pairs
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData)
			throws InvalidRequestException, InternalException;

	/**
	 * Start a new process instance with form data and files
	 * 
	 * @param processId
	 *            The process definition id
	 * 
	 * @param instanceData
	 *            The form data in key-value pairs
	 * 
	 * @param files
	 *            Files in order to start the instance
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	public WfProcessInstance startProcess(int processId, WfProcessInstance instanceData, MultipartFile[] files)
			throws InvalidRequestException, InternalException;

	/**
	 * Start a new process instance with form data
	 * 
	 * @param definition
	 *            The workflow definition
	 * 
	 * @param instanceData
	 *            The form data in key-value pairs
	 * 
	 * @param userId
	 *            The user who is starting the instance
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InternalException
	 * @throws InvalidRequestException
	 */
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance instanceData, String userId)
			throws InternalException, InvalidRequestException;

	/**
	 * Start a new process instance with form data and files
	 * 
	 * @param definition
	 *            The workflow definition
	 * 
	 * @param instanceData
	 *            The form data in key-value pairs
	 * 
	 * @param userId
	 *            he user who is starting the instance
	 * 
	 * @param user
	 *            ??
	 * 
	 * @param files
	 *            Files in order to start the instance
	 * 
	 * @return {@link WfProcessInstance}
	 * @throws InvalidRequestException
	 * @throws InternalException
	 */
	public WfProcessInstance startProcess(WorkflowDefinition definition, WfProcessInstance instanceData, String userId,
			String user, MultipartFile[] files) throws InvalidRequestException, InternalException;

	/**
	 * Return all process definitions for the processes supervised by the
	 * authenticated user
	 * 
	 * @return List of {@link WfProcessInstance}
	 */
	public List<WfProcessInstance> getSupervisedInstances();

	/**
	 * Get documents by instance id
	 * 
	 * @param id
	 *            The instance id
	 * 
	 * @return List of {@link WfDocument}
	 * @throws InvalidRequestException
	 */
	public List<WfDocument> getProcessInstanceDocuments(int id) throws InvalidRequestException;
	
	/**
	 * Problably @deprecated!!!!
	 * Same as {@link ExecutionService#getProcessInstanceDocuments(int)}
	 * 
	 * @param instanceId
	 * @return
	 * @throws InvalidRequestException
	 */
	public List<WfDocument> getDocumentsByInstance(String instanceId) throws InvalidRequestException;
	
	/**
	 * Get the user's completed instances
	 * 
	 * @return List of {@link WfProcessInstance}
	 */
	public List<WfProcessInstance> getUserCompletedInstances();
	
	/**
	 * Set the instance version
	 * 
	 * @param instanceId
	 * @param version
	 */
	public void setInstancesVersion(String instanceId, int version);
}
