package gr.cyberstream.workflow.engine.service;

import java.io.IOException;
import java.util.Calendar;

import org.stringtemplate.v4.ST;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gr.cyberstream.workflow.engine.customtypes.DocumentType;
import gr.cyberstream.workflow.engine.model.ExternalForm;
import gr.cyberstream.workflow.engine.model.Registry;
import gr.cyberstream.workflow.engine.model.api.WfFormProperty;
import gr.cyberstream.workflow.engine.model.api.WfProcessInstance;

public class TemplateHelper {

	public static String getReference(Registry registry) {
		
		int seq = registry.getNext();
		
		ST reference = new ST(registry.getTemplate());
		
		reference.add("id", registry.getId());
		reference.add("seq", seq);
		
		registry.setNext(++seq);
		
		Calendar cal = Calendar.getInstance();
		
		reference.add("year", cal.get(Calendar.YEAR));
		reference.add("month", cal.get(Calendar.MONTH) + 1);
		reference.add("day", cal.get(Calendar.DAY_OF_MONTH));
		
		return reference.render();
	}
	
	public static String getTitle(ExternalForm form, WfProcessInstance instanceData) {
		
		ST title = new ST(form.getTitleTemplate());
		
		for (WfFormProperty property :instanceData.getProcessForm()) {
			
			Object value = null;
			
			switch (property.getType()) {
			
			case "document":
				
				try {
					value = DocumentType.fromString(property.getValue());
					
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
				
			default:
				value = property.getValue();
			}
			
			title.add(property.getId(), value);
		}
		
		title.add("definitionVersion", form.getWorkflowDefinition().getActiveVersion());
		title.add("definitionKey", form.getWorkflowDefinition().getKey());
		title.add("definitionName", form.getWorkflowDefinition().getName());
		
		title.add("reference", instanceData.getReference());
		title.add("supervisor", instanceData.getSupervisor());
		
		Calendar cal = Calendar.getInstance();
		
		title.add("year", cal.get(Calendar.YEAR));
		title.add("month", cal.get(Calendar.MONTH) + 1);
		title.add("day", cal.get(Calendar.DAY_OF_MONTH));
		
		return title.render();
	}
}
