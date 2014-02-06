package serverPackage;

/**
 * Custom EventObject class for when the user selects their login choice.
 */

import java.util.EventObject;

public class ResponseEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	public String id;
	public boolean response;

	public ResponseEvent(Object source, String id, boolean response) {
		super(source);
		this.id = id;
		this.response = response;
	}

	/**
	 * Getter method for unique login ID.
	 * 
	 * @return the login ID for the specific instance of user login that we wish
	 *         to alert that a decision has been made for it.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Getter method to return the users decision on whether they wanted to
	 * accept or decline login.
	 * 
	 * @return - true if login accepted, false if declined.
	 */
	public boolean getResponse() {
		return response;
	}

}
