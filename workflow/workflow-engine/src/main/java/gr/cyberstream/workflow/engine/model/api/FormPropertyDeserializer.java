package gr.cyberstream.workflow.engine.model.api;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class FormPropertyDeserializer extends JsonDeserializer<WfFormProperty> {

	@Override
	public WfFormProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {

		JsonNode node = parser.getCodec().readTree(parser);

		String value = "";

		if (node.get("value").isObject()) {
			value = node.get("value").toString();

		} else if (node.get("type").asText().equals("date")) {

			if (node.get("value").asText() != null && !node.get("value").asText().isEmpty()) {
				Calendar dt = Calendar.getInstance();
				Calendar refDt = javax.xml.bind.DatatypeConverter.parseDateTime(node.get("value").asText());
				dt.setTimeInMillis(refDt.getTimeInMillis());
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				value = df.format(dt.getTime());
			}

		} else
			value = node.get("value").asText();

		WfFormProperty wfFormProperty = new WfFormProperty();
		wfFormProperty.setId(node.get("id").asText());
		wfFormProperty.setName(node.get("name").asText());
		wfFormProperty.setType(node.get("type").asText());
		wfFormProperty.setValue(value);
		wfFormProperty.setReadable(node.get("readable").asBoolean());
		wfFormProperty.setWritable(node.get("writable").asBoolean());
		wfFormProperty.setRequired(node.get("required").asBoolean());
		wfFormProperty.setFormValues(null);
		wfFormProperty.setFormat(node.get("format").asText());
		wfFormProperty.setDescription(node.get("description").asText());
		wfFormProperty.setDevice(node.get("device").asText());
		
		return wfFormProperty;
	}

}
