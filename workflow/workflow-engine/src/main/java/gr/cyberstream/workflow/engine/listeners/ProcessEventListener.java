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
public class ProcessEventListener implements ActivitiEventListener{
	
	final static Logger logger = LoggerFactory.getLogger(ProcessService.class);
	
	@Autowired
	private ProcessService processService;
	
	public ProcessEventListener(){
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		
		if (event.getType().equals(ActivitiEventType.PROCESS_COMPLETED)) {
			
			logger.info("*** processInstanceId:: " + event.getProcessInstanceId() + " has ended ***");
			processService.notifyInstanceEnding(event.getProcessInstanceId());
			
		} else if (event.getType().equals(ActivitiEventType.PROCESS_STARTED)) {			
			logger.info("*** processInstanceId:: " + event.getProcessInstanceId() + " started ***");
			//processService.setInstanceVariable(event.getExecutionId());
			
		} else if(event.getType().equals(ActivitiEventType.TASK_CREATED)){
			logger.info("Task created::ExecutionId::" + event.getExecutionId());
			ActivitiEntityEventImpl eventImpl = (ActivitiEntityEventImpl) event;
			Task task = (Task) eventImpl.getEntity();
			processService.applyTaskSettings(task);
		}
	}

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}
}
