package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConversationType implements Serializable {

	private static final long serialVersionUID = 4297848987014826063L;
	
	private String comment;
	private List<MessageType> messages;
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<MessageType> getMessages() {
		return messages;
	}
	public void setMessages(List<MessageType> messages) {
		this.messages = messages;
	}
	
	public String toString() {
		
		ObjectMapper mapper = new ObjectMapper();

		String jsonSerialization = "";

		try {
			jsonSerialization = mapper.writeValueAsString(this);
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonSerialization;
	}

	/**
	 * return a ConversationType Object de-serializing a JSon string
	 * 
	 * @param jsonSerialization The serialized string
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ConversationType fromString(String jsonSerialization)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		ConversationType conversation = mapper.readValue(jsonSerialization, ConversationType.class);

		return conversation;
	}
}
