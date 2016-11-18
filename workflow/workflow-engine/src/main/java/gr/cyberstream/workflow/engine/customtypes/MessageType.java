/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageType implements Serializable {

	private static final long serialVersionUID = -5702375453477740509L;
	
	private String message;
	private Date submittedDate;
	private String authorId;

	public MessageType() {
	}
	
	public MessageType(String message, Date submittedDate, String authorId) {
		this.message = message;
		this.submittedDate = submittedDate;
		this.authorId = authorId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(Date submitted) {
		this.submittedDate = submitted;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
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
	public static MessageType fromString(String jsonSerialization)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		MessageType message = mapper.readValue(jsonSerialization, MessageType.class);

		return message;
	}

}
