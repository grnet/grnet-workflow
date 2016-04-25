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
			
			if(node.get("value").asText() != null && !node.get("value").asText().isEmpty()) {
				Calendar dt = Calendar.getInstance();
				Calendar refDt = javax.xml.bind.DatatypeConverter.parseDateTime(node.get("value").asText());
				dt.setTimeInMillis(refDt.getTimeInMillis());
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				value = df.format(dt.getTime());
			}
			
		} else {
			value = node.get("value").asText();
		}
		

		return new WfFormProperty(node.get("id").asText(), 
				node.get("name").asText(), 
				node.get("type").asText(), value,
				node.get("readable").asBoolean(),
				node.get("writable").asBoolean(),
				node.get("required").asBoolean(),
				null, node.get("format").asText(),
				node.get("description").asText());
	}

}
