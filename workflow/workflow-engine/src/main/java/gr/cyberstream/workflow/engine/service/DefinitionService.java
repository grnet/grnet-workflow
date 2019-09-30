package gr.cyberstream.workflow.engine.service;

import gr.cyberstream.workflow.engine.model.FBLoginResponse;
import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.model.api.WfProcess;
import gr.cyberstream.workflow.engine.model.api.WfProcessVersion;
import gr.cyberstream.workflow.engine.model.api.WfSettings;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.List;

public interface DefinitionService {

	/**
	 * Returns a {@link WfProcess} by its id
	 * 
	 * @param processId
	 *            The process's id to be returned
	 * 
	 * @return {@link WfProcess}
	 */
	public WfProcess getProcessById(int processId) throws InvalidRequestException;

	/**
	 * Returns a list of all WfProcess depending on user. <br>
	 * If for example user has role admin then, all available processes will be
	 * returned else processes where belong to same group as the user's will be
	 * returned
	 * 
	 * @return
	 */
	public List<WfProcess> getAllProcesses();

	/**
	 * Returns a list of active WfProcess depending on user.
	 * 
	 * 
	 * @return
	 */
	public List<WfProcess> getActiveProcessDefinitions();

	/**
	 * Rertuns a list of workflow definition API models
	 * 
	 * @return
	 */
	public List<WfProcess> getProcessDefinitions();

	/**
	 * Returns WfProcesses by owner
	 * 
	 * @param owner
	 * @return
	 */
	public List<WfProcess> getDefinitionsByOwner(String owner);

	/**
	 * Returns WfProcesses by a list of owners
	 * 
	 * @param owners
	 * @return
	 */
	public List<WfProcess> getDefinitionsByOwners(List<String> owners);

	/**
	 * UPdate a process definition from just its metadata. No BPMN definition is
	 * attached.
	 * 
	 * @param process
	 *            the metadata of the process
	 * @return the saved process definition
	 * @throws InvalidRequestException
	 */
	public WfProcess update(WfProcess process) throws InvalidRequestException;

	/**
	 * Creates a new process definition based on an uploaded BPMN file. If the
	 * BPMN definition deploys successfully to Activiti repository service, a
	 * new ProcessDefinition object is created and saved in Process definitions
	 * repository.
	 * 
	 * @param inputStream
	 *            the input BPMN XML definition
	 * @param filename
	 * @param justification
	 * @return the newly created process definition
	 * @throws InvalidRequestException
	 */
	public WfProcess createNewProcessDefinition(InputStream inputStream, String filename, String justification)
			throws InvalidRequestException;

	/**
	 * 
	 * @param id
	 * @param inputStream
	 * @param filename
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfProcessVersion createNewProcessVersion(int id, InputStream inputStream, String filename,
													String justification) throws InvalidRequestException;

	/**
	 * Updates version of a definition
	 * 
	 * @param processId
	 *            the if of definition
	 * @param version
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfProcessVersion updateVersion(int processId, WfProcessVersion version) throws InvalidRequestException;

	/**
	 * Create an image with the diagram of the process definition
	 * 
	 * @param processId
	 * @return
	 */
	public InputStreamResource getProcessDiagram(int processId);

	/**
	 * Deletes all versions of the process. Throw exception if there are
	 * instances (active or old ones)
	 * 
	 * @param processId
	 * @throws InvalidRequestException
	 */
	public void deleteProcessDefinition(int processId) throws InvalidRequestException;
	
	public void deleteProcessDefinitionFull(int processId) throws InvalidRequestException;

	/**
	 * Deletes the specific version of the process definition. Fail if instances
	 * are found
	 * 
	 * @param processId
	 * @param deploymentId
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfProcess deleteProcessDefinitionVersion(int processId, String deploymentId) throws InvalidRequestException;

	/**
	 * Sets the active version for the workflow definition
	 * 
	 * @param processId
	 *            the id of the proccess definition
	 * @param versionId
	 *            the id of the version to become active
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfProcess setActiveVersion(int processId, int versionId) throws InvalidRequestException;

	/**
	 * Deactivate the version of the workflow definition
	 * 
	 * @param processId
	 *            the process id
	 * @param versionId
	 *            the of the id to be deactivated
	 * @return the modified workflow definition
	 * @throws InvalidRequestException
	 */
	public WfProcessVersion deactivateVersion(int processId, int versionId) throws InvalidRequestException;

	/**
	 * Return the full metadata set for the workflow definition
	 * 
	 * @param id
	 *            the id of the workflow definition
	 * @param device
	 * @return
	 * @throws InvalidRequestException
	 */
	public WfProcess getProcessMetadata(int id, String device) throws InvalidRequestException;

	/**
	 * Return the system settings
	 * 
	 * @return
	 */
	public WorkflowSettings getSettings();

	/**
	 * Update the system settings using the settings
	 * 
	 * @param wfSettings
	 * @return
	 */
	public WorkflowSettings updateSettings(WfSettings wfSettings);

	/**
	 * 
	 * Retrieves and stores a permanent token for a facebook page
	 * 
	 * @param fbResponse
	 * @throws InvalidRequestException
	 */
	public boolean claimPermanentAccessToken(FBLoginResponse fbResponse) throws InvalidRequestException;

	/**
	 * 
	 * @param settings
	 *            updates settings {@link WorkflowSettings}
	 * @return {@link WorkflowSettings}
	 */
	public WorkflowSettings updateSettings(WorkflowSettings settings);
}