package gr.cyberstream.auth.util;

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class FacebookAuthHelper {
	
	private PropertyResourceBundle properties;
	private PropertyResourceBundle show;
	private final String CLIENT_ID;	
	private final String CLIENT_SECRET;
	private final String CALLBACK_URI;
	private FacesMessage message = new FacesMessage();
	
	public FacebookAuthHelper(){
		properties = (PropertyResourceBundle) ResourceBundle.getBundle ("auth");
		show = (PropertyResourceBundle) ResourceBundle.
				getBundle("gr.cyberstream.auth.resources.show",FacesContext.getCurrentInstance().getViewRoot().getLocale());
		CLIENT_ID = properties.getString("facebook.clientId");
		CLIENT_SECRET=properties.getString("facebook.clientSecret");
		CALLBACK_URI = properties.getString("appServer") + properties.getString("facebook.register");		
	}
	
	
	public String getUserInfoJson(final String accessToken) throws ClientProtocolException, IOException{
		String response="";
		if(accessToken!=null){
			String tokenURL = "https://graph.facebook.com/me?access_token=" + accessToken;
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(tokenURL);
			ResponseHandler<String>	responseHandler = new BasicResponseHandler();
			response = httpClient.execute(httpget, responseHandler);			
		}
		return response;
	}
	
	public String getFacebookAccessToken(final String authCode){
		
		String response = null;
		String accessToken = null;

		if(authCode!=null){
			String accessTokenUrl = "https://graph.facebook.com/oauth/access_token?client_id="
                + CLIENT_ID + "&redirect_uri=" + CALLBACK_URI + "&client_secret="
                + CLIENT_SECRET + "&code=" + authCode;
			
			HttpClient httpclient = new DefaultHttpClient();
			
			try {
                HttpGet httpget = new HttpGet(accessTokenUrl);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = httpclient.execute(httpget, responseHandler);
                int a = response.indexOf("=");
                int b = response.indexOf("&");
                accessToken = response.substring(a+1,b);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setSummary(show.getString("internalError"));
				FacesContext.getCurrentInstance().addMessage("regFacebookForm:errorReg", message);	

            } catch (IOException e) {
                e.printStackTrace();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setSummary(show.getString("internalError"));
				FacesContext.getCurrentInstance().addMessage("regFacebookForm:errorReg", message);	

            } finally {
                httpclient.getConnectionManager().shutdown();
            }
		}
		return accessToken;
	}

}
