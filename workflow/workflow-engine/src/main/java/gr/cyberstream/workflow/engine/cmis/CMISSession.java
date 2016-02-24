/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.cmis;

import org.apache.chemistry.opencmis.client.api.Session;

/**
 * Manages the CMIS session for the document handling
 * @author nlyk
 *
 */
public class CMISSession {

	private String cmisServerUrl;
	private String repository;
	private String username;
	private String password;
	
	private Session session;
	
	public CMISSession(String cmisServerUrl, String repository, String username, String password) {
	
		this.cmisServerUrl = cmisServerUrl;
		this.repository = repository;
		this.username = username;
		this.password = password;
	}
	
	public void cleanUp() {
		if (session != null) {
			session.clear();
			session = null;
		}
	}
	
	public Session getSession() {
		
		if (session == null)
			session = OpenCMISSessionFactory.createOpenCMISSession(cmisServerUrl, repository, username, password);
		
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getCmisServerUrl() {
		return cmisServerUrl;
	}

	public void setCmisServerUrl(String cmisServerUrl) {
		this.cmisServerUrl = cmisServerUrl;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
