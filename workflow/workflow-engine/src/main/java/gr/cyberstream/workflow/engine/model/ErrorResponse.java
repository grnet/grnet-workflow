package gr.cyberstream.workflow.engine.model;

/**
 * Utility function used to return an error response from a RESTful request
 * 
 * @author nlyk
 *
 */
public class ErrorResponse {

	private String code;
	private String message;
	
	public final static String noerror = "NOERROR";

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
