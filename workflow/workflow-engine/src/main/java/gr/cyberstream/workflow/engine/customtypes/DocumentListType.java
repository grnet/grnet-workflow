package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocumentListType implements Serializable {

	private static final long serialVersionUID = -2593170675843007024L;
	
	private int[] selection;
	private DocumentType[] list;
	
	public DocumentListType() {
	}
	
	public DocumentListType(int[] selection, DocumentType[] list) {
		this.selection = selection;
		this.list = list;
	}

	public int[] getSelection() {
		
		return selection;
	}

	public void setSelection(int[] selection) {
	
		this.selection = selection;
	}

	public DocumentType[] getList() {
	
		return list;
	}

	public void setList(DocumentType[] list) {
	
		this.list = list;
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
	 * @param jsonSerialization
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static DocumentListType fromString(String jsonSerialization)
			throws JsonParseException, JsonMappingException, IOException {

		// de-serialize JSon representation
		ObjectMapper mapper = new ObjectMapper();
		DocumentListType docList = mapper.readValue(jsonSerialization, DocumentListType.class);

		return docList;
	}
}
