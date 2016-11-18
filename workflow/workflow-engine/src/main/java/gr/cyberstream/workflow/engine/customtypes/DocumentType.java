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

public class DocumentType implements Serializable {
		private static final long serialVersionUID = 6967189692701470467L;

	private String title;
	private String version;
	private String documentId;
	private String authorId;
	private String author;
	private Date submittedDate;
	private String refNo;
	
	public DocumentType() {
	}
	
	public DocumentType(String title, String version, String documentId, String author, String authorId, Date submittedDate, String refNo) {
		
		this.title = title;
		this.version = version;
		this.documentId = documentId;
		this.authorId = authorId;
		this.author = author;
		this.submittedDate = submittedDate;
		this.refNo = refNo;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}	

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public Date getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
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
	 * Return a new DocumentType Object de-serializing a JSon representation
	 * 
	 * @param jsonSerialization The serialized string
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static DocumentType fromString(String jsonSerialization)
			throws JsonParseException, JsonMappingException, IOException {

		// de-serialize JSon representation
		ObjectMapper mapper = new ObjectMapper();
		DocumentType doc = mapper.readValue(jsonSerialization, DocumentType.class);

		return doc;
	}	
}
