/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;
import java.util.Date;

import org.apache.chemistry.opencmis.client.api.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cyberstream.workflow.engine.cmis.CMISDocument;

public class DocumentType {

	private Document document;
	private String documentId;
	private String authorId;
	private Date submittedDate;
	private String refNo;

	public String getDocumentId() {
		return documentId;
	}

	public void setDocuemntId(String documentId) {
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

	@JsonIgnore
	public Document getDocument() {
		return document;
	}

	@JsonIgnore
	public void setDocument(Document document) {
		this.document = document;
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
	 * @param jsonSerialization
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

		// get the CMIS document using the documentId
		CMISDocument cmisDoc = new CMISDocument();
		doc.setDocument(cmisDoc.getDocumentById(doc.getDocumentId()));

		return doc;
	}

}
