package gr.cyberstream.workflow.engine.customtypes;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PositionType implements Serializable {

	private static final long serialVersionUID = 7254062815823630898L;

	private double latitude;
	private double longitude;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
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
	 * Return a new PositionType Object de-serializing a JSon representation
	 * 
	 * @param jsonSerialization
	 *            The serialized string
	 *            
	 * @return {@link PositionType} from serialized string
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static PositionType fromString(String jsonSerialization)
			throws JsonParseException, JsonMappingException, IOException {

		// de-serialize JSon representation
		ObjectMapper mapper = new ObjectMapper();
		PositionType position = mapper.readValue(jsonSerialization, PositionType.class);

		return position;
	}
}
