package gr.cyberstream.workflow.engine.customservicetasks;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

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

import gr.cyberstream.workflow.engine.cmis.CMISDocument;
import gr.cyberstream.workflow.engine.cmis.MultipartFileResource;
import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.model.MailServiceResponse;
import gr.cyberstream.workflow.engine.model.RESTMail;
import gr.cyberstream.workflow.engine.model.RESTRecipient;
import gr.cyberstream.workflow.engine.service.InvalidRequestException;

import static org.springframework.http.converter.StringHttpMessageConverter.DEFAULT_CHARSET;

@Component
public class DocumentMail implements JavaDelegate {

	private static final Logger logger = LoggerFactory.getLogger(DocumentMail.class);

	private Expression app;
	private Expression from;
	private Expression to;
	private Expression subject;
	private Expression attachment;
	private Expression bcc;
	private Expression cc;

	@Autowired
	private CMISDocument cmisDocument;

	@Autowired
	private RuntimeService activitiRuntimeSrv;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		// Set the custom service task property values
		String appName = (String) app.getValue(execution);
		logger.debug("app " + appName);

		String fromRecipient = (String) from.getValue(execution);
		logger.debug("from " + fromRecipient);

		String toRecipient = getStringFromField(to, execution);
		logger.debug("to " + toRecipient);

		String msgSubject = (String) subject.getValue(execution);
		logger.debug("subject " + msgSubject);

		String blindCarbonCopyRecipient = getStringFromField(bcc, execution);
		logger.debug("Blind Copy to: " + blindCarbonCopyRecipient);

		String carbonCopyRecipient = getStringFromField(cc, execution);
		logger.debug("Carbon Copy to: " + carbonCopyRecipient);

		String documentVar = null;

		if (attachment != null)
			documentVar = (String) attachment.getValue(execution);

		/** To Recipients split by comma **/
		List<RESTRecipient> recipients = new ArrayList<RESTRecipient>();

		if (toRecipient != null) {

			RESTRecipient resetRecipient = new RESTRecipient();
			String[] separatedRecipients = toRecipient.split(",");

			if (separatedRecipients.length > 1) {

				for (int i = 0; i < separatedRecipients.length; i++) {
					resetRecipient = new RESTRecipient(separatedRecipients[i].trim());
					recipients.add(resetRecipient);
				}
			} else if (separatedRecipients.length == 1) {
				resetRecipient = new RESTRecipient(toRecipient.trim());
				recipients.add(resetRecipient);
			}
		}

		// Add blind carbon copy recipients
		List<RESTRecipient> blindCopyRecipients = new ArrayList<RESTRecipient>();

		if (blindCarbonCopyRecipient != null) {
			RESTRecipient restblindCopyRecipient = new RESTRecipient();

			String[] bccRecipients = blindCarbonCopyRecipient.split(",");

			if (bccRecipients.length > 1) {

				for (int i = 0; i < bccRecipients.length; i++) {
					restblindCopyRecipient = new RESTRecipient(bccRecipients[i].trim());
					blindCopyRecipients.add(restblindCopyRecipient);
				}
			} else if (bccRecipients.length == 1) {
				restblindCopyRecipient = new RESTRecipient(blindCarbonCopyRecipient.trim());
				blindCopyRecipients.add(restblindCopyRecipient);
			}
		}

		// Add carbon copy recipients
		List<RESTRecipient> carbonCopyRecipients = new ArrayList<RESTRecipient>();

		if (carbonCopyRecipient != null) {
			RESTRecipient restCarbonCopyRecipient = new RESTRecipient();

			String[] ccRecipients = carbonCopyRecipient.split(",");

			if (ccRecipients.length > 1) {

				for (int i = 0; i < ccRecipients.length; i++) {
					restCarbonCopyRecipient = new RESTRecipient(ccRecipients[i].trim());
					carbonCopyRecipients.add(restCarbonCopyRecipient);
				}

			} else if (ccRecipients.length == 1) {
				restCarbonCopyRecipient = new RESTRecipient(carbonCopyRecipient.trim());
				carbonCopyRecipients.add(restCarbonCopyRecipient);
			}
		}

		Document document = null;
		MultipartFileResource multipartFileResource = null;

		// If a document id has been specified get the input stream of...
		// the file to be sent as attachment
		if (documentVar != null && !documentVar.isEmpty()) {
			DocumentType documentValue = (DocumentType) activitiRuntimeSrv.getVariable(execution.getId(), documentVar);
			if (documentValue != null) {
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

		mail.setBcc(blindCopyRecipients);

		mail.setCc(carbonCopyRecipients);

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
			formHttpMessageConverter.setCharset(Charset.forName("UTF-8"));
			rest.getMessageConverters().add(formHttpMessageConverter);
			MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
			rest.getMessageConverters().add(converter);

			HttpHeaders fileHeaders = new HttpHeaders();
			fileHeaders.set("Content-Type", document.getContentStreamMimeType());
			HttpEntity<MultipartFileResource> fileEntity = new HttpEntity<>(multipartFileResource, fileHeaders);

			HttpHeaders jsonHeader = new HttpHeaders();
			jsonHeader.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RESTMail> jsonEntity = new HttpEntity<RESTMail>(mail, jsonHeader);

			MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

			parts.add("json", jsonEntity);
			parts.add("file", fileEntity);
			logger.warn("This is the file name inside DocumentMail: " + fileEntity.getBody().getOriginalFilename());

			mailResponse = rest.postForObject(uri, parts, MailServiceResponse.class);
		}

		if (mailResponse == null) {
			logger.debug("MailResponse is null");
			throw new InvalidRequestException("MailResponse is null");
		} else
			logger.debug("MailResponse: " + mailResponse.getCode() + ", " + mailResponse.getDescription());

	}

	/**
	 * Evaluates an expression or returns the value as value as string without
	 * evaluate it
	 * 
	 * @param expression
	 *            The value to evaluate
	 * 
	 * @param execution
	 * @return {@link String} The evaluated or no value
	 */
	protected String getStringFromField(Expression expression, DelegateExecution execution) {

		if (expression != null) {
			Object value = expression.getValue(execution);

			if (value != null)
				return value.toString();
		}
		return null;
	}
}
