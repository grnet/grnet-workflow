package gr.cyberstream.workflow.engine.servlet;

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cyberstream.workflow.engine.cmis.CMISSession;

public class DocumentServlet extends HttpServlet {

	private static final long serialVersionUID = -6329740391120749101L;

	final static Logger logger = LoggerFactory.getLogger(DocumentServlet.class);

	private CMISSession cmisSession;
	
	private PropertyResourceBundle properties;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		super.init(config);
		
		properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");
		
		cmisSession = new CMISSession(properties.getString("cmis.service.url"),
				properties.getString("cmis.repository.id"), properties.getString("cmis.username"),
				properties.getString("cmis.password"));
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String uri = request.getRequestURI();
    	String[] uriParts = uri.split("/document/");
    	
    	String referenceUID = null;
		
    	if (uriParts.length > 1) {
    		
    		String parameters = uriParts[1];
    		String[] parts = parameters.split("/");
    		
    		if (parts.length == 1) {
    			
    			referenceUID = parts[0];
    		}
    	}
    	
    	ServletOutputStream out = response.getOutputStream();
    	
    	Document document = null;
    	
    	try {
			
			document = (Document) cmisSession.getSession().getObject(referenceUID);

		} catch (CmisObjectNotFoundException e) {
			
			logger.error("Document  " + referenceUID + " does not exist.");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			out.flush();
			out.close();
			return;
		}
    	
    	try {
			
			if (document != null) {
				
				String mimeType = document.getContentStreamMimeType();
				
				response.setContentType(mimeType);
				response.setHeader("Content-disposition", "inline; filename=\"" + document.getName() + ""
						+ MimeTypes.getExtension(mimeType) + "\"");
				
				IOUtils.copy(document.getContentStream().getStream(), out);
				
			} else {
				
				logger.error("Document  " + referenceUID + " does not exist.");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			
		} catch (IOException e) {
			
			logger.error("Unable to write Document. " + e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
		} finally {
			
			out.flush();
			out.close();
		}
	}

	@Override
	public void destroy() {
		
		cmisSession.cleanUp();
		
		super.destroy();
	}
}
