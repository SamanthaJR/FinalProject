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
		String dbName = "jdbc:postgresql://dbteach2/bankauth";
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
			//setup properties to allow SSLSocket
//			System.setProperty("javax.net.ssl.keyStore", "/home/students/sjr090/work/SSC2/DBex2/Final\\ Project\\ Server//ApHSstore.jks");
//			System.setProperty("javax.net.ssl.keyStorePassword","SecureLock");
//			System.setProperty("javax.net.ssl.trustStore", "/home/students/sjr090/work/SSC2/DBex2/Final\\ Project\\ Server//ApHSstore.jks");
//			System.setProperty("javax.net.ssl.trustStorePassword","SecureLock");
			// Setup a secure TCP socket.
			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket listener = (SSLServerSocket) factory.createServerSocket(4444);
			System.out.println("AHS: Started, Listening to port 4444");
			SSLSocket server;
			
//			ServerSocket listener = new ServerSocket(4444);
//			Socket server = new Socket();
			
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
	private char[] line, resp, task, regid, len, details;
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
				System.out.println(regDetails);
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

				// If required to authenticate:
			} else if (checkMessage(task, "user log in")) {
				System.out.println("Authenticating");
				// read registration id
				is.read(regid, 0, 183);
				String id = String.valueOf(regid);
				System.out.println(id);
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
					removeElement();
					// throw new
					// UnexpectedClientMessageException("Task incorrect.");
				}
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
	private synchronized void fireResponseEvent(boolean b, String id) {

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
			}
		}
	}
}