/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.service;

/**
 * Internal error exception. Commonly used in cases where the error is due to a
 * persistence handling problem and similar.
 * 
 * @author nlyk
 *
 */
public class InternalException extends CustomException {

	private static final long serialVersionUID = 1L;

	public InternalException(String message) {
		super(CustomException.INTERNAL_SERVER_ERROR, message);
	}
}
