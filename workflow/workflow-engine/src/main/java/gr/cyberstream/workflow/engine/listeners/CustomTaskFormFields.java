package gr.cyberstream.workflow.engine.listeners;

import java.util.List;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractActivityBpmnParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTaskFormFields extends AbstractActivityBpmnParseHandler<UserTask> {

	final static Logger logger = LoggerFactory.getLogger(CustomTaskFormFields.class);
	
	private final String APPROVE_DOC_EXTENSION_ID = "gr.cyberstream.workflow.designer.usertasks.ApproveDocument";
	private final String SUBMIT_DOC_EXTENSION_ID = "gr.cyberstream.workflow.designer.usertasks.SubmitDocument";
	
	private final String NAMESPACE = "wf_custom_";
	
	private final String DOCUMENT_TITLE = "documentTitle";
	private final String DOCUMENT_VAR = "documentVar";
	private final String APPROVAL_VAR = "approvalVar";
		
	@Override
	protected Class<? extends BaseElement> getHandledType() {
		return UserTask.class;
	}

	@Override
	protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {

		if (userTask.getExtensionId() != null && userTask.getExtensionElements() != null) {
		
			switch (userTask.getExtensionId()) {
			
			case SUBMIT_DOC_EXTENSION_ID:
				
				logger.info("Parsing Submit Document Custom Task");
				
				FormProperty documentProperty = getDocumentProperty(
						userTask.getExtensionElements().get(DOCUMENT_VAR),
						userTask.getExtensionElements().get(DOCUMENT_TITLE));
				
				if (documentProperty != null) {
					userTask.getFormProperties().add(0, documentProperty);
				}
			
				break;
			
			case APPROVE_DOC_EXTENSION_ID:
				
				logger.info("Parsing Approve Document Custom Task");
				
				FormProperty approvalDocumentProperty = getDocumentProperty(
						userTask.getExtensionElements().get(DOCUMENT_VAR), null);
				
				if (approvalDocumentProperty != null) {
					
					FormProperty approvalProperty = getApprovalProperty(
							userTask.getExtensionElements().get(APPROVAL_VAR), approvalDocumentProperty.getVariable());
					
					if (approvalProperty != null) {
						userTask.getFormProperties().add(0, approvalProperty);
					}
					
					userTask.getFormProperties().add(0, approvalDocumentProperty);
				}
				
				break;
			default:
			}
		}
	}
	
	private FormProperty getDocumentProperty(List<ExtensionElement> documentVarElements, 
			List<ExtensionElement> documentTitleElements) {
		
		String documentTitle = "";
		
		if (documentTitleElements != null && !documentTitleElements.isEmpty()) {
			
			ExtensionElement documentTitleElement = documentTitleElements.get(0);
			
			documentTitle = documentTitleElement.getElementText();
		}
		
		if (documentVarElements != null && !documentVarElements.isEmpty()) {
			
			ExtensionElement documentVar = documentVarElements.get(0);
			
			FormProperty property = new FormProperty();
			property.setId(NAMESPACE + documentVar.getName());
			property.setName(documentTitle);
			property.setType("document");
			
			property.setVariable(documentVar.getElementText());
			
			return property;
		}
		
		return null;	
	}
	
	private FormProperty getApprovalProperty(List<ExtensionElement> approvalVarElements, String documentVar) {
		
		if (approvalVarElements != null && !approvalVarElements.isEmpty()) {
			
			ExtensionElement approvalVar = approvalVarElements.get(0);
			
			FormProperty property = new FormProperty();
			property.setId(NAMESPACE + approvalVar.getName());
			property.setName(approvalVar.getElementText());
			property.setType("approve");
			
			property.setVariable(approvalVar.getElementText());
			
			return property;
		}
		
		return null;
	}
}
