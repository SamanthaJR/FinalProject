package serverPackage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

import javax.sql.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

public class AppHomeServer {

	public static String decision;
	public static LinkedList<ServerProtocol> listenerQueue = new LinkedList<ServerProtocol>();
	private Connection dbConn;

	public void startServer() {
		
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

			ServerSocket listener = new ServerSocket(4444);
			System.out.println("AHS: Started, Listening to port 4444");
			Socket server;
			while (true) {
				// doComms connection;

				server = listener.accept();
				System.out.println("AHS: AppClient accepted.");

				doComms conn_c = new doComms(server, dbConn);
				Thread t = new Thread(conn_c);
				t.start();
			}

		} catch (IOException ioe) {
			System.out.println("AHS: Listen error " + ioe);
			ioe.printStackTrace();
		}
	}

	/**
	 * Method called within the serverprotocol class where the server protocol adds itself to the list of waiting logins when it is 
	 * created
	 * @param rl - the waiting userlogin serverProtocol to be added to the list
	 */
	public void setId(ServerProtocol rl) {
		listenerQueue.add(rl);
	}

	public Connection getDBConn(){
		return dbConn;
	}
	
}

class doComms implements Runnable {
	private Socket server;
	private char[] line, resp, task, regid, len, details;
	private Connection dbConn;

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
			// Get input from the client
			InputStreamReader is = new InputStreamReader(
					server.getInputStream());

			PrintStream out = new PrintStream(server.getOutputStream());

			out.println("Hello Client");

			System.out.println("AHS: Handshake sent.");

			is.read(line, 0, 12);
			if (checkMessage(line, "Hello Server")) {
				System.out.println("Message received:" + String.valueOf(line));
			} else
				throw new UnexpectedClientMessageException(
						"Handshake incorrect.");

			// read if registering or logging in
			is.read(task, 0, 11);

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
				
				if (regidSet != null){
					try {
						// if the device is already registered, statement is true
						if(regidSet.next()){
							out.println("Already Registered Client");
							System.out.println("Already Registered Client");
						} else {
							ResultSet userSet = checkUsernmDetails(regDetArr[1]);
							//if username already taken, statement is true
							if(userSet.next()){
								out.println("Username Taken Client");
								System.out.println("Username Taken Client");
							} else {
								insertUserDetails(regDetArr[0], regDetArr[1], regDetArr[2]);
								out.println("Succesful Registration Client");
								System.out.println("Succesful Registration Client");
							}
						}
					} catch (SQLException e) {
						System.out.println("AHSQL: More SQL error catching");
						e.printStackTrace();
					}
				}
				
				// check database note that don't say username already taken if
				// it is that device already registered under it

				// send appropriate reply ("Already Registered Client" ,
				// "Username Taken Client", "Goodbye Client")

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
				}
			} else
				throw new UnexpectedClientMessageException("Task incorrect.");

			out.println("Goodbye Client");

			out.flush();
			out.close();
			is.close();
			server.close();

			System.out.println("Connection closed");

		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		} catch (UnexpectedClientMessageException e) {
			System.out.println("Unexpected Client response");
			e.printStackTrace();
		}
	}

	private void insertUserDetails(String regid, String username, String password) throws SQLException {
			PreparedStatement stmnt = dbConn.prepareStatement("INSERT INTO userinfo " + "(reg_id, user_name, password) " + "VALUES (?, ?, ?)");
			stmnt.setString(1, regid);
			stmnt.setString(2, username);
			stmnt.setString(3, password);
			stmnt.executeUpdate();
	}

	public ResultSet checkRegDetails(String regid) {
		try {
			PreparedStatement regDetails = dbConn.prepareStatement("SELECT * "
					+ "FROM userinfo " + "WHERE reg_id = ? ");
			regDetails.setString(1, regid);
			ResultSet rs = regDetails.executeQuery();
			return rs;

		} catch (SQLException e) {
			System.out.println("AHSThread: Problem creating or executing SQL Statement 1");
			e.printStackTrace();
		}
		return null;
	}
	
	public ResultSet checkUsernmDetails(String username) {
		try {
			PreparedStatement regDetails = dbConn.prepareStatement("SELECT * "
					+ "FROM userInfo " + "WHERE user_name = ? ");
			regDetails.setString(1, username);
			ResultSet rs = regDetails.executeQuery();
			return rs;

		} catch (SQLException e) {
			System.out.println("AHSThread: Problem creating or executing SQL Statement 2");
			e.printStackTrace();
		}
		return null;
	}

	private synchronized void fireResponseEvent(boolean b, String id) {
//		String ident = id;
//		Stack<ServerProtocol> temp = new Stack<ServerProtocol>();
//		ResponseEvent response = new ResponseEvent(this, id, b);
//		ServerProtocol rl = AppHomeServer.listenerQueue.pop();		// ERROR HERE - STACK IS A BAAAAAAD IDEA
//		while (rl.getId().equalsIgnoreCase(id) == false) {
//			temp.push(rl);
//		}
//		rl.responseReceived(response);
//		while (temp.empty() == false) {
//			ServerProtocol next = temp.pop();
//			AppHomeServer.listenerQueue.push(next);
//		}

		ResponseEvent response = new ResponseEvent(this, id, b);
		ListIterator<ServerProtocol> it = AppHomeServer.listenerQueue.listIterator();
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
}