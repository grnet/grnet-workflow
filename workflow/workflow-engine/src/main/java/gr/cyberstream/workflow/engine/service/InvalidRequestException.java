package gr.cyberstream.workflow.engine.service;

/**
 * Exception Object specific for invalid (bad) requests. I.e. invalid arguments,
 * invalid input files, etc.
 * 
 * @author nlyk
 *
 */
public class InvalidRequestException extends CustomException {

	private static final long serialVersionUID = 1L;

	public InvalidRequestException(String message) {
		super(CustomException.INVALID_REQUEST_ERROR, message);
	}
}
