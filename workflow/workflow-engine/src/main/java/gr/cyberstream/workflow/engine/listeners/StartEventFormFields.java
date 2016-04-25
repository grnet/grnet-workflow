package gr.cyberstream.workflow.engine.listeners;

import java.util.List;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractActivityBpmnParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartEventFormFields extends AbstractActivityBpmnParseHandler<StartEvent> {

	final static Logger logger = LoggerFactory.getLogger(StartEventFormFields.class);

	public final static String DATE_PATTERN_ISO8601 = "yyyy-MM-dd";
	public final static String DATETIME_PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public final static String TIME_PATTERN_ISO8601 = "HH:mm:ss.SSS";

	public final static String DATETIME_PATTERN_PRESENTATION = "dd/MM/yyyy HH:mm";

	@Override
	protected Class<? extends BaseElement> getHandledType() {
		return StartEvent.class;
	}

	@Override
	protected void executeParse(BpmnParse bpmnParse, StartEvent startEvent) {

		// get form properties from task
		List<FormProperty> formProperties = startEvent.getFormProperties();

		// for each form property check type
		for (FormProperty formProperty : formProperties) {

			String datePattern = formProperty.getDatePattern();
			datePattern = datePattern == null ? "date" : datePattern;

			// if type== date set date pattern
			switch (formProperty.getType()) {

			case "date":
				switch (datePattern) {
				case "time":
					formProperty.setDatePattern(TIME_PATTERN_ISO8601);
					break;
				case "datetime":
					formProperty.setDatePattern(DATETIME_PATTERN_ISO8601);
					break;
				case "date":
					formProperty.setDatePattern(DATE_PATTERN_ISO8601);
					break;
				default:
					formProperty.setDatePattern(DATE_PATTERN_ISO8601);
					break;
				}
				break;
			default:
				break;
			}
			
			//startEvent.getFormProperties().add(0, approvalProperty);
		}
	}
}
