/**
 * Class that implements all the authentication logic of ensuring usernames match etc...
 * An instance is passed as an object to the AppHomeServer every time a new login is attempted 
 * and this instance waits for a custom even to be fired in order to establish whether the user
 * has accepted or declined the login.
 */
package serverPackage;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.sql.*;
import javax.sql.*;

public class ServerProtocol {

	private static GCMServer gcm;
	public boolean decision;
	public AppHomeServer ahs;
	public ArrayList<ResponseEvent> responses = new ArrayList<ResponseEvent>();
	private CountDownLatch block;
	public String id;

	public ServerProtocol(AppHomeServer ahs) {
		this.ahs = ahs;
		gcm = new GCMServer();
		block = new CountDownLatch(1);
	}

	/**
	 * Authenticate method that ensures that the correct username and password
	 * combination have been entered
	 * 
	 * @param u
	 *            the username (Note this is currently hardcoded to expect only
	 *            a single correct string of "SamanthJR")
	 * @param p
	 *            the password (Note this is currently hardcoded to expect only
	 *            a single correct string of "123"
	 * @return true if correct password/username combination. False if not.
	 */
	public boolean authenticate(String u, String p) {

		// check username and password match up correctly
		// if not, not allowed in
		// else get regid for username (this is the google specified
		// registration ID for GCM services)
		// postToGCM with this regid
		// wait for event to fire with user response
		// allow or disallow login accordingly
		Connection dbConn = ahs.getDBConn();

		ResultSet usrnm = getUsernameAndPassFromDB(u, dbConn);

		try {
			if (usrnm.next()) {
				id = usrnm.getString(1);
				String user = usrnm.getString(2);
				String pass = usrnm.getString(3);
				if (u.contentEquals(user) && p.contentEquals(pass)) {
					ahs.setId(this);
					gcm.postToGCM(id);

					try {
						block.await();
					} catch (InterruptedException e) {
						System.out.println("Proto: ");
						e.printStackTrace();
						return false;
					}
				}
				return decision;
			} else {
				return false;
			}
		} catch (SQLException e) {
			System.out.println("Proto: ");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Method to retrieve all data of a specified username from the database.
	 * 
	 * @param username
	 *            - The String value of the username we wish to retrieve from.
	 * @param dbConn
	 *            - The connection to the database.
	 * @return The ResultSet containing the username's information (whether it
	 *         is empty or not) or null if there was an error.
	 */
	private ResultSet getUsernameAndPassFromDB(String username,
			Connection dbConn) {
		try {
			PreparedStatement regDetails = dbConn.prepareStatement("SELECT * "
					+ "FROM userInfo " + "WHERE user_name = ? ");
			regDetails.setString(1, username);
			ResultSet rs = regDetails.executeQuery();
			return rs;

		} catch (SQLException e) {
			System.out
					.println("AHSThread: Problem creating or executing SQL Statement 2");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Set method that changes the class variable 'decision' which encapulates
	 * the user's decision on whether or not they wish to allow login.
	 * 
	 * @param b
	 *            - the boolean we wish to set bool 'decision' to equal.
	 */
	public void setDecision(boolean b) {
		decision = b;
	}

	/**
	 * The method called when a ResponseEvent is fired (i.e. when the user
	 * selects their decision on whether they will allow a login
	 * 
	 * @param resp
	 *            - the fired ResponseEvent
	 */
	public void responseReceived(ResponseEvent resp) {
		setDecision(resp.getResponse());
		block.countDown();
	}

	/**
	 * Getter method for the device ID which is uniquely associated with each
	 * ServerProtocol object
	 * 
	 * @return - the ID. It is also the value of the device Registration ID as
	 *         this is guaranteed to be unique.
	 */
	public String getId() {
		return id;
	}
}
