package gr.cyberstream.workflow.engine.customservicetasks;

import gr.cyberstream.workflow.engine.cmis.CMISDocument;
import gr.cyberstream.workflow.engine.cmis.MultipartFileResource;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.model.MailServiceResponse;
import gr.cyberstream.workflow.engine.model.RESTMail;
import gr.cyberstream.workflow.engine.model.RESTRecipient;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.chemistry.opencmis.client.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

@Component
@SuppressWarnings("unused")
public class DocumentMail implements JavaDelegate {

	private static final Logger logger = LoggerFactory.getLogger(DocumentMail.class);

	private Expression app;
	private Expression from;
	private Expression to;
	private Expression subject;
	private Expression body;
	private Expression attachment;
	private Expression bcc;
	private Expression cc;

	@Autowired
	CMISDocument cmisDocument;

	@Autowired
	RuntimeService activitiRuntimeSrv;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		// Set the custom service task property values
		String appName = (String) app.getValue(execution);
		logger.debug("app: " + appName);

		String fromRecipient = (String) from.getValue(execution);
		logger.debug("from: " + fromRecipient);

		String toRecipient = getStringFromField(to, execution);
		logger.debug("to: " + toRecipient);

		String msgSubject = (String) subject.getValue(execution);
		logger.debug("subject: " + msgSubject);

		String msgBody = (String) body.getValue(execution);
		logger.debug("body: " + msgBody);

		String documentVar = null;
		
		if (attachment != null)
			documentVar = (String) attachment.getValue(execution);

		// Add recipient to list
		List<RESTRecipient> recipients = new ArrayList<RESTRecipient>();

		RESTRecipient restRecipient = new RESTRecipient(toRecipient);
		recipients.add(restRecipient);

		Document document = null;
		MultipartFileResource multipartFileResource = null;

		// If a document id has been specified get the input stream of...
		// the file to be sent as attachment
		if (documentVar != null && !documentVar.isEmpty()) {
			DocumentType documentValue = (DocumentType) activitiRuntimeSrv.getVariable(execution.getId(), documentVar);
			if(documentValue != null){
				document = cmisDocument.getDocumentById(documentValue.getDocumentId());
				multipartFileResource = new MultipartFileResource(document);
			}
		}

		// Setup parameters
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters = execution.getVariables();

		// Instantiate and compose the RESTEmail object
		RESTMail mail = new RESTMail();

		mail.setFrom(fromRecipient);

		mail.setSubject(msgSubject);

		mail.setTo(recipients);

		mail.setParameters(parameters);

		boolean synchronous = true;

		// Create the rest template request
		RestTemplate rest = new RestTemplate();

		// build the url
		PropertyResourceBundle properties = (PropertyResourceBundle) ResourceBundle.getBundle("workflow-engine");
		String baseUrl = properties.getString("mailService.baseUrl");
		URI uri = new URI(baseUrl + "/v1/mail/" + appName + "/sync/" + synchronous);

		// decide whether we need to make a multipart request or not
		MailServiceResponse mailResponse = null;
		if (multipartFileResource == null)
			mailResponse = rest.postForObject(uri, mail, MailServiceResponse.class);
		
		else {

			FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
			formHttpMessageConverter.setMultipartCharset(Charset.forName("UTF-8"));
			rest.getMessageConverters().add(formHttpMessageConverter);
			rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

			HttpHeaders fileHeaders = new HttpHeaders();

			fileHeaders.set("Content-Type", document.getContentStreamMimeType());

			HttpEntity<MultipartFileResource> fileEntity = new HttpEntity<MultipartFileResource>(multipartFileResource,
					fileHeaders);
			HttpHeaders jsonHeader = new HttpHeaders();
			jsonHeader.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RESTMail> jsonEntity = new HttpEntity<RESTMail>(mail, jsonHeader);

			MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

			parts.add("json", jsonEntity);
			parts.add("file", fileEntity);

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

			// HttpEntity<HttpHeaders> httpEntity = new HttpEntity(requestHeaders, parts);

			mailResponse = rest.postForObject(uri, parts, MailServiceResponse.class);
		}

		if (mailResponse == null) {
			logger.debug("MailResponse is null");
			throw new InvalidRequestException("MailResponse is null");
		} else
			logger.debug("MailResponse: " + mailResponse.getCode() + ", " + mailResponse.getDescription());

	}

	protected String getStringFromField(Expression expression, DelegateExecution execution) {
		if (expression != null) {
			Object value = expression.getValue(execution);
			if (value != null) {
				return value.toString();
			}
		}
		return null;
	}

}
