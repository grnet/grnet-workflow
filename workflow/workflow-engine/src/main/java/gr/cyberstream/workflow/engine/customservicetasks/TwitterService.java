package gr.cyberstream.workflow.engine.customservicetasks;

import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

import gr.cyberstream.workflow.engine.model.WorkflowSettings;
import gr.cyberstream.workflow.engine.service.ProcessService;

@Component
public class TwitterService implements JavaDelegate{
	
	private Expression account;
	private Expression status;
	
	@Autowired
	private ProcessService processService;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		String accountName = (String) account.getValue(execution);
		String tweet = (String) status.getValue(execution);
		
		WorkflowSettings settings = processService.getSettings();
		Map<String,String> tokensMap = settings.fetchTwitterTokensAsMap();
		
		if(tokensMap==null)	return;

		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");		
		String consumerKey = properties.getString("twitter.consumerKey");
		String consumerSecret = properties.getString("twitter.consumerSecret");

		if(tokensMap.get(accountName)==null)	return;
		String tokenpair = tokensMap.get(accountName);
		
		String[] tokens = tokenpair.split(",");
		
		String accessToken = tokens[0];
		String accessTokenSecret = tokens[1];
		
		Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret); 
		twitter.timelineOperations().updateStatus(tweet);		
	}

}
