package gr.cyberstream.workflow.engine.customservicetasks;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Component;

import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.service.ProcessService;

@Component
public class FacebookService implements JavaDelegate{

	private Expression status;
	private Expression page;
	
	@Autowired
	private ProcessService processService;
	
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		String statusToPost = (String) status.getValue(execution);
		String facebookPage = (String) page.getValue(execution);
		
		WorkflowSettings settings = processService.getSettings();
		Map<String,String> tokensMap = settings.fetchFacebookTokensAsMap();
		
		if(tokensMap==null)	return;
		
		String[] pageInfo = tokensMap.get(facebookPage).split(",");
		String token = pageInfo[0];
		
		if(token==null || token.isEmpty())	return;
		
		Facebook facebook = new FacebookTemplate(token);
		facebook.feedOperations().updateStatus(statusToPost);
			
	}

	
}
