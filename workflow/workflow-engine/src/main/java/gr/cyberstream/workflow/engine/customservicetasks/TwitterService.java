package gr.cyberstream.workflow.engine.customservicetasks;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.social.twitter.api.TimelineOperations;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

public class TwitterService implements JavaDelegate{
	
	private Expression status;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		String tweet = (String) status.getValue(execution);

		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");
		
		String consumerKey = properties.getString("twitter.consumerKey");
		String consumerSecret = properties.getString("twitter.consumerSecret");
		String accessToken = properties.getString("twitter.accessToken");
		String accessTokenSecret = properties.getString("twitter.accessTokenSecret");
		
		Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret); 
				
		TimelineOperations timeLineOperations = twitter.timelineOperations();
		
		timeLineOperations.updateStatus(tweet);
		
	}

}
