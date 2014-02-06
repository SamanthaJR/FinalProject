package serverPackage;

/**
 * Custom Exception class for exceptions thrown when Client sends messages not
 * in accordance with the defined communication protocol.
 * 
 * @author sjr090
 * 
 */
public class UnexpectedClientMessageException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnexpectedClientMessageException(String message) {
		super(message);
	}
}
