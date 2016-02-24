package gr.cyberstream.workflow.engine.model.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class FormPropertyDeserializer extends JsonDeserializer<WfFormProperty> {

	@Override
	public WfFormProperty deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		JsonNode node = parser.getCodec().readTree(parser);
		
		String value = "";
		
		if (node.get("value").isObject()) {
			
			value = node.get("value").toString();
			
		} else {
			
			value = node.get("value").asText();
		}
		
		return new WfFormProperty(node.get("id").asText(), node.get("name").asText(),
				node.get("type").asText(), value, 
				node.get("readable").asBoolean(), node.get("writable").asBoolean(), 
				node.get("required").asBoolean(), null);
	}
	
}
