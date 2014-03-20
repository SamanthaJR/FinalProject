package serverPackage;

/**
 * Server Class that accepts direct connection with the Android devices.
 * Communicates login and registration requests with the same protocol.
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.sql.*;

import org.eclipse.jetty.server.Authentication.SendSuccess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppHomeServer {

	public static String decision;
	public static LinkedList<ServerProtocol> listenerQueue = new LinkedList<ServerProtocol>();
	private Connection dbConn;

	public void startServer() {
		// Setup a connection with the database that stores all necessary user
		// information.
		System.setProperty("jdbc.drivers", "org.postgresql.Driver");
		String dbName = "jdbc:postgresql://dbteach2/bankauth?" + "ssl=true&"+ "sslfactory=org.postgresql.ssl.NonValidatingFactory";
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("AHSQL: Could not find a Driver");
			e1.printStackTrace();
		}
		try {
			dbConn = DriverManager.getConnection(dbName, "sjr090", "nulumobo");
		} catch (SQLException e) {
			System.out.println("AHS: SQL database connection error");
			e.printStackTrace();
		}

		try {
//			System.setProperty("javax.net.ssl.trustStore", "/home/students/sjr090/.java/deployment/security/trusted.certs");
			System.setProperty("javax.net.ssl.trustStore", "/home/students/sjr090/work/SSC2/DBex2/Final Project Server/trusted.certs");
			
			// Setup a secure TCP socket.
			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			SSLServerSocket listener = (SSLServerSocket) factory
					.createServerSocket(4444);
			listener.setNeedClientAuth(true);
			System.out.println("AHS: Started, Listening to port 4444");
			SSLSocket server;

			// ServerSocket listener = new ServerSocket(4444);
			// Socket server = new Socket();

			while (true) {
				server = (SSLSocket) listener.accept();
				System.out.println("AHS: AppClient accepted.");

				ExecutorService executor = Executors.newSingleThreadExecutor();
				doComms t = new doComms(server, dbConn);
				Future<?> future = executor.submit(t);

				try {
					System.out.println("Started..");
					System.out.println(future.get(1, TimeUnit.MINUTES));
					System.out.println("Finished!");
				} catch (TimeoutException e) {
					System.out.println("Terminated!");
					t.fireResponseEvent(false, t.getRegid());
					t.removeElement();
					t.closeClientConnection();
				} catch (InterruptedException e) {
					System.out.println("Interrupted!");
					e.printStackTrace();
				} catch (ExecutionException e) {
					System.out.println("Execution.");
					e.printStackTrace();
				}
				executor.shutdownNow();
			}

		} catch (IOException ioe) {
			System.out.println("AHS: Listen error " + ioe);
			ioe.printStackTrace();
		}
	}

	/**
	 * Method called within the ServerProtocol class when the server protocol
	 * adds itself to the list of waiting login attempts upon its creation.
	 * 
	 * @param rl
	 *            - the waiting ServerProtocol to be added to the list
	 */
	public void setId(ServerProtocol rl) {
		listenerQueue.add(rl);
	}

	/**
	 * Getter method to return the database connection so that it may be used in
	 * other classes.
	 * 
	 * @return - the database connection.
	 */
	public Connection getDBConn() {
		return dbConn;
	}

}

/**
 * Threaded class that allows multiple connections to be accepted simultaneously
 * within this server.
 * 
 * @author sjr090
 * 
 */
class doComms implements Runnable {
	private Socket server;
	private char[] line, resp, task, regid, len, details, locInfo, radLen;
	private Connection dbConn;
	private InputStreamReader is;
	private PrintStream out;

	public doComms(Socket server, Connection dbConn) {
		this.server = server;
		this.dbConn = dbConn;
	}

	public void run() {

		line = new char[12];
		resp = new char[8];
		regid = new char[183];
		task = new char[11];
		len = new char[3];
		radLen = new char[4];
		locInfo = new char[1000];

		try {
			// Setup input and output streams for the connection
			is = new InputStreamReader(server.getInputStream());
			out = new PrintStream(server.getOutputStream());

			// Initiate Handshake
			out.println("Hello Client");

			System.out.println("AHS: Handshake sent.");

			// Read Client response to Handshake.
			is.read(line, 0, 12);
			if (checkMessage(line, "Hello Server")) {
				System.out.println("Message received:" + String.valueOf(line));
			} else {
				System.out.println("Message received:" + String.valueOf(line));
				throw new UnexpectedClientMessageException(
						"Handshake incorrect.");
			}
			// Read if registering or logging in
			is.read(task, 0, 11);

			// If required to register:
			if (checkMessage(task, "registering")) {
				System.out.println("Registering");

				is.read(len, 0, 3);
				System.out.println(Integer.parseInt(new String(len)));
				int length = Integer.parseInt(new String(len));
				details = new char[length];
				is.read(details, 0, length);
				String regDetails = String.valueOf(details);
//				System.out.println(regDetails);
				String[] regDetArr = regDetails.split("\\^");

				ResultSet regidSet = checkRegDetails(regDetArr[0]);

				if (regidSet != null) {
					try {
						// if the device is already registered, statement is
						// true
						if (regidSet.next()) {
							out.println("Already Registered Client");
							System.out.println("Already Registered Client");
						} else {
							ResultSet userSet = checkUsernmDetails(regDetArr[1]);
							// if username already taken, statement is true
							if (userSet.next()) {
								out.println("Username Taken Client");
								System.out.println("Username Taken Client");
							} else {
								insertUserDetails(regDetArr[0], regDetArr[1],
										regDetArr[2]);
								out.println("Succesful Registration Client");
								System.out
										.println("Succesful Registration Client");
							}
						}
					} catch (SQLException e) {
						System.out.println("AHSQL: More SQL error catching");
						e.printStackTrace();
					}
				}
				closeClientConnection();

				// If required to remove registration details:
			} else if (checkMessage(task, "de-register")) {
				System.out.println("Removing client registration");
				
				is.read(len, 0, 3);
				System.out.println(Integer.parseInt(new String(len)));
				int length = Integer.parseInt(new String(len));
				details = new char[length];
				is.read(details, 0, length);
				String regDetails = String.valueOf(details);
//				System.out.println(regDetails);
				String[] regDetArr = regDetails.split("\\^");
				
				// check password regid and username all correct
				// if not, return a message to client saying to try again
				// if so, remove details, send message to client saying success
				if(passUserMatch(regDetArr[1], regDetArr[2], regDetArr[0])){
					//read all location names under this regid
					//send number of location names back to client
					//send all location names back to client
					//remove all location names under this regid from locations table
					ArrayList<String> allLocs = getAllUserLocationNames(regDetArr[0]);
					out.println("Successfully removed registration Client");
					int noOfLocs = allLocs.size();
					out.println(noOfLocs);
					for(int j = 0; j < noOfLocs; j++){
						out.println(allLocs.get(j));
					}
					removeAllUserLocs(regDetArr[0]);
					removeReg(regDetArr[0]);		
				} else {
					ResultSet rset = checkRegDetails(regDetArr[0]);
					try {
						if(rset.next()){
						out.println("Login details incorrect Client");
						} else {
							out.println("Successfully removed registration Client");
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				closeClientConnection();
				
			} else if (checkMessage(task, "user log in")) {
				System.out.println("Authenticating");
				// read registration id
				is.read(regid, 0, 183);
				String id = String.valueOf(regid);
//				System.out.println(id);
				System.out.println("Waiting for response");

				// read accept or decline
				is.read(resp, 0, 8);
				System.out.println(String.valueOf(resp));
				System.out.println("Message received:" + String.valueOf(resp));

				if (checkMessage(resp, "Accepted")) {
					AppHomeServer.decision = "true";
					fireResponseEvent(true, id);
					System.out.println("Accepted login!");
				} else if (checkMessage(resp, "Declined")) {
					AppHomeServer.decision = "false";
					fireResponseEvent(false, id);
					System.out.println("Declined login!");
				} else {
					fireResponseEvent(false, id);
				}
				closeClientConnection();

			} else if (checkMessage(task, "addLocation")) {
				// Add a new location to the database
				is.read(regid, 0, 183);

				String username = readLocVal();

				String password = readLocVal();

				String locName = readLocVal();

				String locRad = readLocVal();

				String location = readLocVal();

				ResultSet regidSet = checkRegDetails(String.valueOf(regid));

				if (regidSet != null) {
					try {
						if (!regidSet.next()) {
							out.println("Device not Registered Client");
							System.out.println("Device not Registered Client");
						} else {
							ResultSet userSet = checkUsernmDetails(username);
							// if username already taken, statement is true
							if (!userSet.next()) {
								out.println("Login Details Incorrect Client");
								System.out.println("Username Taken Client");
							} else {
								boolean check = passUserMatch(username,
										password, String.valueOf(regid));
								if (!check) {
									out.println("Login details incorrect Client");
									System.out
											.println("Adding location, Password and Username do not match.");
								} else {
									if (locNameTaken(locName,
											String.valueOf(regid))) {
										out.println("Location name taken Client");
										System.out
												.println("Location name taken.");
									} else {
										addLocation(String.valueOf(regid),
												username, password, locName,
												locRad, location);
										out.println("Location successfully added Client");
										System.out
												.println("Location successfully added Client");
									}
								}
							}
						}
					} catch (SQLException e) {
						System.out
								.println("AHS: SQL Exception when adding location");
						e.printStackTrace();
					}
				}
				closeClientConnection();
			} else if (checkMessage(task, "delLocation")) {
				is.read(regid, 0, 183);
				String username = readLocVal();
				String password = readLocVal();
				String locName = readLocVal();
				
				ResultSet regidSet = checkRegDetails(String.valueOf(regid));

				if (regidSet != null) {
					try {
						if (!regidSet.next()) {
							out.println("Device not Registered Client");
							System.out.println("Device not Registered Client");
						} else {
							ResultSet userSet = checkUsernmDetails(username);
							// if username already taken, statement is true
							if (!userSet.next()) {
								out.println("Login Details Incorrect Client");
								System.out.println("Username Taken Client");
							} else {
								boolean check = passUserMatch(username,
										password, String.valueOf(regid));
								if (!check) {
									out.println("Login details incorrect Client");
									System.out
											.println("Adding location, Password and Username do not match.");
								} else {
										deleteLocation(String.valueOf(regid),
												username, password, locName);
//										out.println("Location successfully deleted Client");
										System.out
												.println("Location successfully deleted");
									
								}
							}
						}
					} catch (SQLException e) {
						System.out
								.println("AHS: SQL Exception when adding location");
						e.printStackTrace();
					}
				}
				closeClientConnection();
				
				
			} else if (checkMessage(task, "locationing")) {
				System.out.println("locationing");
				is.read(regid, 0, 183);
				String reg_id = String.valueOf(regid);
				String locName = readLocVal();
				is.read(resp, 0, 8);
				String transType = String.valueOf(resp);
				System.out.println(transType);
				updateGeofenceTransition(reg_id, locName, transType);
				System.out.println("AHS: Geofence location updated");
				closeClientConnection();
			}
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		} catch (UnexpectedClientMessageException e) {
			System.out.println("Unexpected Client response");
			e.printStackTrace();
		}
	}
	
	private void removeAllUserLocs(String reg){
		try {
			PreparedStatement stmnt = dbConn
					.prepareStatement("DELETE FROM locations" + " WHERE reg_id = ?");
			stmnt.setString(1, reg);
			stmnt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("AHS: Problem deleting all user locations");
			e.printStackTrace();
		}
	}

	private ArrayList<String> getAllUserLocationNames(String reg) {
		PreparedStatement stmnt;
		try {
			stmnt = dbConn
					.prepareStatement("SELECT location_name FROM locations" + " WHERE reg_id= ?");
			stmnt.setString(1, reg);
			ResultSet set = stmnt.executeQuery();
			ArrayList<String> returnStrings = new ArrayList<String>();
			while(set.next()){
				returnStrings.add(set.getString("location_name"));
			}
			return returnStrings;
		} catch (SQLException e) {
			System.out.println("AHS: Problem selecting all user locations");
			e.printStackTrace();
			return null;		
		}
		
	}

	private void deleteLocation(String reg, String username,
			String password, String locName) {
		try {
			PreparedStatement stmnt = dbConn
					.prepareStatement("DELETE FROM locations WHERE reg_id = ? AND username = ? AND password = ? AND location_name = ?");
			stmnt.setString(1, reg);
			stmnt.setString(2, username);
			stmnt.setString(3, password);
			stmnt.setString(4, locName);
			stmnt.executeUpdate();
			out.println("Successfully removed location Client");
		} catch (SQLException e) {
			System.out
			.println("AHS: " + "Problem removing location info");
			e.printStackTrace();
			out.println("Error removing location Client");
		}
	}

	private void removeReg(String reg) {
		try {
			PreparedStatement stmnt = dbConn
					.prepareStatement("DELETE FROM userinfo WHERE reg_id = ?");
			stmnt.setString(1, reg);
			stmnt.executeUpdate();
			out.println("Successfully removed registration Client");
		} catch (SQLException e) {
			System.out
			.println("AHS: " + "Problem removing registration info");
			e.printStackTrace();
			out.println("Error removing registration Client");
		}
	}

	public String getRegid(){
		return String.valueOf(regid);
	}

	/**
	 * Method called when the server is alerted by the device that the user has
	 * entered or exited a Geofence, it updates the database
	 * 
	 * @param reg_id
	 *            - the registration id of the device that has made the
	 *            transition
	 * @param locName
	 *            - the name/id of the geofence that they have entered/exited.
	 * @param transType
	 *            - the transition: whether it is an entry or an exit.
	 */
	private void updateGeofenceTransition(String reg_id, String locName,
			String transType) {
		try {
			PreparedStatement stmnt = dbConn
					.prepareStatement("UPDATE locations "
							+ "SET in_geofence = ? "
							+ "WHERE reg_id = ? AND location_name = ?");
			stmnt.setString(2, reg_id);
			stmnt.setString(3, locName);
			if (transType.equalsIgnoreCase("enter in")) {
				stmnt.setBoolean(1, true);
			} else {
				stmnt.setBoolean(1, false);
			}
//			System.out.println(stmnt.toString());
			stmnt.executeUpdate();
		} catch (SQLException e) {
			System.out
					.println("AHS: " + "Problem updating geofence entry/exit");
			e.printStackTrace();
		}
	}

	/**
	 * Method reads values from the Client and stores them in a String
	 * 
	 * @return - the String value read from the Client.
	 */
	private String readLocVal() {
		try {
			is.read(radLen, 0, 4);
			int uLen = Integer.parseInt(String.valueOf(radLen));
			// System.out.println("uLen is: " + String.valueOf(uLen));
			is.read(locInfo, 0, uLen);
//			System.out.println(String.valueOf(locInfo));
			String info = "";
			for (int i = 0; i < uLen; i++) {
				info += locInfo[i];
			}
			locInfo = new char[1000];
			return info;
		} catch (IOException e) {
			System.out.println("Problem reading loc val");
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Adds new location information into the locations table in the database
	 * 
	 * @param valueOf
	 *            - the user's device's unique registration ID
	 * @param username
	 * @param password
	 * @param locName
	 *            - the name that the user has decided to call this new
	 *            location
	 * @param locRad
	 *            - the radius to set the size of the location
	 * @param location
	 *            - the toString() representation of a Location object that
	 *            represents the latitude and longitude at the centre of the
	 *            inputted area.
	 */
	private void addLocation(String valueOf, String username, String password,
			String locName, String locRad, String location) {
		try {
			PreparedStatement stmnt = dbConn
					.prepareStatement("INSERT INTO locations "
							+ "(reg_id, username, password, location, radius, in_geofence, location_name) "
							+ "VALUES (?, ?, ?, ?, ?, true, ?)");
			stmnt.setString(1, valueOf);
			stmnt.setString(2, username);
			stmnt.setString(3, password);
			stmnt.setString(4, location);
			stmnt.setInt(5, Integer.parseInt(locRad));
			stmnt.setString(6, locName);
			stmnt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Problem inserting new location");
			e.printStackTrace();
		}

	}

	/**
	 * Method tests whether to correct combination of username and password have
	 * been entered.
	 * 
	 * @param user
	 *            - the username
	 * @param pass
	 *            - the password
	 * @return - true if the password goes with the associated username, false
	 *         if they do not match or if the username doesn't exist.
	 */
	private boolean passUserMatch(String user, String pass, String regid) {
		try {
			PreparedStatement passDetails = dbConn.prepareStatement("SELECT * "
					+ "FROM userinfo "
					+ "WHERE password = ?  AND user_name = ? AND reg_id = ?");
			passDetails.setString(1, pass);
			passDetails.setString(2, user);
			passDetails.setString(3,  regid);
			ResultSet rs = passDetails.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Method called to terminate the connection with the Client once the
	 * communication has been completed.
	 */
	public void closeClientConnection() {
		// Signal Client to close the connection.
		out.println("Goodbye Client");

		out.flush();
		out.close();
		try {
			is.close();
			server.close();
		} catch (IOException e) {
			System.out.println("Exception when closing connection");
			e.printStackTrace();
		}
		System.out.println("Connection closed");
	}

	/**
	 * Method assesses both parameter Strings are equal.
	 * 
	 * @param string
	 *            The expected message from the Client.
	 * @param string1
	 *            The actual message from the Client.
	 * @return boolean true if they match, false if not.
	 */
	private boolean checkMessage(char[] ln, String string1) {
		if (String.valueOf(ln).equals(string1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method adds a newly registered user to the database.
	 * 
	 * @param regid
	 *            - the GCM servers unique Registration ID.
	 * @param username
	 *            - the new user's username.
	 * @param password
	 *            - the new user's password.
	 * @throws SQLException
	 */
	private void insertUserDetails(String regid, String username,
			String password) throws SQLException {
		PreparedStatement stmnt = dbConn
				.prepareStatement("INSERT INTO userinfo "
						+ "(reg_id, user_name, password) " + "VALUES (?, ?, ?)");
		stmnt.setString(1, regid);
		stmnt.setString(2, username);
		stmnt.setString(3, password);
		stmnt.executeUpdate();
	}

	/**
	 * Method checks the locations table in the database to see if the location
	 * name that the user has provided from a new location is present or not
	 * 
	 * @param locationName
	 *            - the name the user is attempting to create
	 * @param reg_id
	 *            - the unique registration id of the user's device
	 * @return - true if the name is already present in the database (only for
	 *         this user), false otherwise.
	 */
	public boolean locNameTaken(String locationName, String reg_id) {
		PreparedStatement locNameDetails;
		try {
			locNameDetails = dbConn.prepareStatement("SELECT * "
					+ "FROM locations "
					+ "WHERE reg_id = ?  AND location_name = ?");
			locNameDetails.setString(1, reg_id);
			locNameDetails.setString(2, locationName);
			ResultSet rs = locNameDetails.executeQuery();
			boolean b = rs.next();
			System.out.println(b);
			return (b);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * Method queries the database for all Registration IDs as specified by @param
	 * regid, mostly called when checking if a Registration ID has been entered
	 * into the database already or not.
	 * 
	 * @param regid
	 *            - the Registration ID we wish to lookup.
	 * @return - the ResultSet containing either the RegistrationID, if it is
	 *         present, or containing nothing if it is not.
	 */
	public ResultSet checkRegDetails(String regid) {
		try {
			PreparedStatement regDetails = dbConn.prepareStatement("SELECT * "
					+ "FROM userinfo " + "WHERE reg_id = ? ");
			regDetails.setString(1, regid);
			ResultSet rs = regDetails.executeQuery();
			return rs;

		} catch (SQLException e) {
			System.out
					.println("AHSThread: Problem creating or executing SQL Statement 1");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method that queries the database for all Registration IDs as specified by @param
	 * username, mostly called when checking if a username has been taken
	 * already.
	 * 
	 * @param username
	 *            - the username value that we wish to retrieve from the
	 *            database, if it is present.
	 * @return - the ResultSet containing either the username, if it is present,
	 *         or containing nothing if it is not.
	 */
	public ResultSet checkUsernmDetails(String username) {
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
	 * Method for firing the custom event to the correct waiting ServerProtocol
	 * Object. It searches the linkedList of waiting ServerProtocol Objects and
	 * when it reaches the ServerProtocol with the correct matching Registration
	 * ID it fires the ResponseEvent with the correct user accept or decline
	 * decision and removes this ServerProtocol Object from the list.
	 * 
	 * @param b
	 *            - a boolean representing the user's login choice. True if the
	 *            login is accepted, false if declined.
	 * @param id
	 *            - the ID of the login instance so that the correct user can be
	 *            identified and logged in (or not) without affecting the
	 *            others.
	 */
	public synchronized void fireResponseEvent(boolean b, String id) {

		ResponseEvent response = new ResponseEvent(this, id, b);
		ListIterator<ServerProtocol> it = AppHomeServer.listenerQueue
				.listIterator();
		if (it.hasNext()) {
			ServerProtocol tempProto = (ServerProtocol) it.next();
			while (it.hasNext() && !tempProto.getId().equalsIgnoreCase(id)) {
				tempProto = (ServerProtocol) it.next();
			}
			if (tempProto.getId().equalsIgnoreCase(id)) {
				// fire response event
				tempProto.responseReceived(response);
				// remove element
				it.remove();
			}
		}

	}

	/**
	 * Method removed a ServerProtocol from the list if it is still present
	 * after a timeout.
	 * 
	 * @param rl
	 *            - the ServerProtocol object to be removed.
	 */
	public void removeElement() {
		ListIterator<ServerProtocol> it = AppHomeServer.listenerQueue
				.listIterator();
		if (it.hasNext()) {
			ServerProtocol tempProto = (ServerProtocol) it.next();
			while (it.hasNext()
					&& !tempProto.getId().equalsIgnoreCase(
							String.valueOf(regid))) {
				tempProto = (ServerProtocol) it.next();
			}
			if (tempProto.getId().equalsIgnoreCase(String.valueOf(regid))) {
				// remove element
				it.remove();
				System.out.println("Login Request Removed from List");
			}
		}
	}
}