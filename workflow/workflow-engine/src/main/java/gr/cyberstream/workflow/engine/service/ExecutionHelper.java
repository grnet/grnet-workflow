package gr.cyberstream.workflow.engine.service;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionHelper {

	@Autowired
	HistoryService activitiHistorySrv;

	@Autowired
	TaskService activitiTaskSrv;

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

}
