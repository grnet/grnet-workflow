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

	private Session session;

	public void cleanUp() {
		if (session != null) {
			session.clear();
			session = null;
		}
	}
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
