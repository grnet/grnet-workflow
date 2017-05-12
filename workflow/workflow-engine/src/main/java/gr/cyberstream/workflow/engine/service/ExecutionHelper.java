package gr.cyberstream.workflow.engine.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.cyberstream.workflow.engine.customtypes.DocumentListType;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.listeners.CustomTaskFormFields;
import gr.cyberstream.workflow.engine.model.WorkflowInstance;
import gr.cyberstream.workflow.engine.persistence.Processes;

@Service
public class ExecutionHelper {

	@Autowired
	private RuntimeService activitiRuntimeSrv;
	
	@Autowired
	HistoryService activitiHistorySrv;

	@Autowired
	TaskService activitiTaskSrv;

	@Autowired
	private Processes processRepository;

	/**
	 * Helper function to get assignee from a previous
	 * 
	 * @param instanceId
	 * @param taskDefId
	 * @return
	 */
	public String getTaskAssignee(String instanceId, String taskDefId) {

		List<HistoricTaskInstance> tasks = activitiHistorySrv.createHistoricTaskInstanceQuery()
				.processInstanceId(instanceId).taskDefinitionKey(taskDefId).orderByTaskCreateTime().desc().list();

		if (tasks.size() > 0) {
			return tasks.get(0).getAssignee();

		} else {
			List<Task> activeTasks = activitiTaskSrv.createTaskQuery().processInstanceId(instanceId)
					.taskDefinitionKey(taskDefId).orderByTaskCreateTime().desc().list();

			if (activeTasks.size() > 0)
				return activeTasks.get(0).getAssignee();

		}

		return null;
	}

	public String getTaskDueDateBasedOnInstance(String instanceId, int duedays) {
		WorkflowInstance instance = processRepository.getInstanceById(instanceId);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(instance.getStartDate());

		calendar.add(Calendar.DATE, duedays);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(CustomTaskFormFields.DATE_PATTERN_ISO8601);

		return dateFormat.format(calendar.getTime());
	}
	
	public void addToListAndClear(String instanceId, String documentListVar, String documentVar) {
		
		DocumentListType documentList = activitiRuntimeSrv.getVariable(instanceId, documentListVar, DocumentListType.class);
		DocumentType document = activitiRuntimeSrv.getVariable(instanceId, documentVar, DocumentType.class);
		
		List<DocumentType> list;
		
		if (documentList != null && documentList.getList() != null)
			 list = new ArrayList<DocumentType>(Arrays.asList(documentList.getList()));
		else {
			documentList = new DocumentListType();
			documentList.setSelection(new int[0]);
			list = new ArrayList<DocumentType>();
		}
		
		list.add(new DocumentType(document.getTitle(), document.getVersion(), document.getDocumentId(), document.getAuthor(),
				document.getAuthorId(), document.getSubmittedDate(), document.getRefNo()));
		
		DocumentType[] documentArray = new DocumentType[list.size()];
		documentList.setList(list.toArray(documentArray));
		
		activitiRuntimeSrv.setVariable(instanceId, documentListVar, documentList);
		activitiRuntimeSrv.setVariable(instanceId, documentVar, null);
	}
}
