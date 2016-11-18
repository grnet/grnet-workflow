package gr.cyberstream.workflow.engine.listeners;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gr.cyberstream.workflow.engine.service.ProcessService;

@Component
public class ProcessEventListener implements ActivitiEventListener {

	@Autowired
	private ProcessService processService;

	private static final Logger logger = LoggerFactory.getLogger(ProcessEventListener.class);

	public ProcessEventListener() {

	}

	@Override
	public void onEvent(ActivitiEvent event) {

		if (event.getType().equals(ActivitiEventType.PROCESS_COMPLETED)) {
			logger.debug("*** Instance with id: " + event.getProcessInstanceId() + " has ended ***");
			processService.notifyInstanceEnding(event.getProcessInstanceId());

		} else if (event.getType().equals(ActivitiEventType.PROCESS_STARTED)) {
			logger.debug("*** Instance with id: " + event.getProcessInstanceId() + " started ***");
			processService.notifyInstanceStarted(event.getProcessInstanceId());

		} else if (event.getType().equals(ActivitiEventType.TASK_CREATED)) {
			logger.debug("Task created::ExecutionId::" + event.getExecutionId());
			ActivitiEntityEventImpl eventImpl = (ActivitiEntityEventImpl) event;
			Task task = (Task) eventImpl.getEntity();
			processService.applyTaskSettings(task);
		}
	}

	@Override
	public boolean isFailOnException() {
		return true;
	}
}
