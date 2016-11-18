/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.cmis;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class OpenCMISSessionFactory {

	public static Session createOpenCMISSession(String url, String repository, String username, String password) {

		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);
		parameter.put(SessionParameter.BROWSER_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
		parameter.put(SessionParameter.REPOSITORY_ID, repository);

		SessionFactory factory = SessionFactoryImpl.newInstance();

		return factory.createSession(parameter);
	}

}
