package gr.cyberstream.auth.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.json.JSONObject;

public class GoogleAuthHelper {

	private PropertyResourceBundle properties;
	private final String CLIENT_ID;	
	private final String CLIENT_SECRET;
	private final String CALLBACK_URI;
	
	private final Collection<String> SCOPE;
	private final HttpTransport HTTP_TRANSPORT;
	private final JsonFactory JSON_FACTORY;
	private final String USER_INFO_URL;
	private final String TOKEN_INFO_URL;
	
	private String stateToken;
	private GoogleTokenResponse token;
	private Credential credential;
	//private String refreshToken;
	private final GoogleAuthorizationCodeFlow flow;
	
	public GoogleAuthHelper(){
		properties = (PropertyResourceBundle) ResourceBundle.getBundle ("auth");
		
		CLIENT_ID = properties.getString("google.clientId");
		CLIENT_SECRET=properties.getString("google.clientSecret");
		CALLBACK_URI = properties.getString("appServer") + properties.getString("google.register");
	
		SCOPE = Arrays.asList(properties.getString("google.scope").split(";"));
		USER_INFO_URL = properties.getString("google.userInfo");
		TOKEN_INFO_URL = properties.getString("google.tokenInfo");
		HTTP_TRANSPORT = new NetHttpTransport();
		JSON_FACTORY = new JacksonFactory();
		
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
				JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPE).build();
		
		generateStateToken();
	}
	
	
	/**
	 * Generates a secure state token
	 */
	public void generateStateToken(){

		SecureRandom sr1 = new SecureRandom();

		stateToken = "google;" + sr1.nextInt();
		
	}
		
	/**
	 * Accessor for state token
	 */
	public String getStateToken(){
	return stateToken;
	}
	
	/**
	 * Builds a login URL based on client ID, secret, callback URI, and scope
	 */
	public String buildLoginUrl() {

	GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();	
	String response = url.setRedirectUri(CALLBACK_URI).setState(stateToken).build(); 
	return response;
	
	}
		
	/**
	 * Creates the access token using the authorization code returned from Google
	 * @param authCode
	 * @throws Exception
	 */
	public GoogleTokenResponse getToken(String authCode)throws Exception{
		
		token = flow.newTokenRequest(authCode).setRedirectUri(CALLBACK_URI).execute();
		credential = flow.createAndStoreCredential(token, null); 
		return token;
	
	}
	
	/*public String getRefreshToken()throws Exception{
		
		if(token!=null){
			if(validateToken(token)){
				
				String tokenStr = token.toPrettyString();
				JSONObject json = new JSONObject(tokenStr);
				
				refreshToken = json.getString("google.refresh_token");
				
				return refreshToken;		
			}
			else return null;
		}
		else return null;
		
	}*/
	
	/**
	 * Expects an Authentication Code, and makes an authenticated request for the user's profile information
	 * @return JSON formatted user profile information
	 * @param authCode authentication code provided by google
	 */
	public String getUserInfoJson() throws Exception {
				
		HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
		// Make an authenticated request
		GenericUrl url = new GenericUrl(USER_INFO_URL);
		HttpRequest request = requestFactory.buildGetRequest(url);
		request.getHeaders().setContentType("application/json");
		String jsonIdentity = request.execute().parseAsString();
		
		return jsonIdentity;

	}
	
	
	/**
	 * Expects access token from Google and validates it 
	 * @param authCode
	 * @return 
	 */
	public boolean validateToken(GoogleTokenResponse token) throws Exception{
		
		String tokenStr = token.toPrettyString();
		JSONObject json = new JSONObject(tokenStr);
		
		String accessToken = json.getString("access_token");
				
		//create a URL a TokenInfo Validation URL		
		HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
		GenericUrl url = new GenericUrl(TOKEN_INFO_URL+"?access_token="+accessToken);
		
		HttpRequest request = requestFactory.buildGetRequest(url);	
		request.getHeaders().setContentType("application/json");
		String jsonIdentity = request.execute().parseAsString();
		
		JSONObject json2 = new JSONObject(jsonIdentity);

		//check if audience parameter matches CLIENT_ID
		String audience = json2.getString("audience");
		
		if(audience!=null){
			
			if(audience.equals(CLIENT_ID)) return true;
			else return false;
		}
		else return false;
	}
	
	/**
	 * Gets User Info using refresh token
	 * @return
	 * @throws Exception
	 */
	/*public String getUserInfoJsonRefresh() throws Exception{
		if(refreshToken!=null){
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
						
			GenericUrl url = new GenericUrl(properties.getString("google.token_url")+"?refresh_token="+refreshToken
					+"&client_id="+CLIENT_ID+"&client_secret="+CLIENT_SECRET+"&grant_type=refresh_token");
			HttpRequest request = requestFactory.buildPostRequest(url,null);
			
			request.getHeaders().setContentType("application/json");
			String jsonIdentity = request.execute().parseAsString();
			return jsonIdentity;
		}
		else return null;
	}
	*/


}
