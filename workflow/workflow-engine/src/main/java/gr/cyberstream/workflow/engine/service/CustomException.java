package gr.cyberstream.workflow.engine.service;

import gr.cyberstream.workflow.engine.model.api.ErrorResponse;

/**
 * Abstract exception class used as base for creating the specialized
 * application Exceptions
 * 
 * @author nlyk
 *
 */
public abstract class CustomException extends Exception {

	private static final long serialVersionUID = 1L;

	public static final String INVALID_REQUEST_ERROR = "INVALID_REQUEST_ERROR";
	public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

	private ErrorResponse error;

	public CustomException() {
		this.error = new ErrorResponse();
	}

	public CustomException(String code, String message) {
		this.error = new ErrorResponse();
		this.error.setCode(code);
		this.error.setMessage(message);
	}

	public ErrorResponse getError() {
		return this.error;
	}

	public String getCode() {
		return this.error.getCode();
	}

	public void setCode(String code) {
		this.error.setCode(code);
	}

	public String getMessage() {
		return this.error.getMessage();
	}

	public void setMessage(String message) {
		this.error.setMessage(message);
	}

}
