package serverPackage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AppHomeServer {

	public static String decision;
	public ServerProtocol proto;

	public void startServer() {

		try {
			ServerSocket listener = new ServerSocket(4444);
			System.out.println("AHS: Started, Listening to port 4444");
			Socket server;

			// doComms connection;

			server = listener.accept();
			System.out.println("AHS: AppClient accepted.");
			if (proto != null) {
				doComms conn_c = new doComms(server, proto);
				Thread t = new Thread(conn_c);
				t.start();
				proto = null;
			}

		} catch (IOException ioe) {
			System.out.println("AHS: Listen error " + ioe);
			ioe.printStackTrace();
		}
	}

	public void setProto(ServerProtocol p) {
		proto = p;
	}

}

class doComms implements Runnable {
	private Socket server;
	private ServerProtocol proto;
	private char[] line, resp;

	public doComms(Socket server, ServerProtocol proto) {
		this.server = server;
		this.proto = proto;
	}

	public void run() {

		line = new char[12];
		resp = new char[8];

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

			System.out.println("Waiting for response");

			is.read(resp, 0, 8);

			System.out.println(String.valueOf(resp));

			System.out.println("Message received:" + String.valueOf(resp));

			if (checkMessage(resp, "Accepted")) {
				AppHomeServer.decision = "true";
				proto.setDecision("true");
				System.out.println("Accepted login!");
			} else if (checkMessage(resp, "Declined")) {
				AppHomeServer.decision = "false";
				proto.setDecision("false");
				System.out.println("Declined login!");
			}

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

//
// public class AppHomeServer{
//
// private ServerSocket serverSocket;
// private Socket clientSocket;
// private InputStreamReader inputStreamReader;
// private BufferedReader bufferedReader;
// private PrintWriter printWriter;
// private String message;
// public boolean decision;
//
// public void startServer() throws UnexpectedClientMessageException {
//
// try {
// serverSocket = new ServerSocket(4444);
//
// } catch (IOException e) {
// System.out.println("Could not listen on port: 4444");
// }
//
// System.out.println("AppHomeServer started. Listening to the port 4444");
//
// while (true) {
// try {
// clientSocket = serverSocket.accept();
// System.out.println("AppClient accepted.");
// inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
// bufferedReader = new BufferedReader(inputStreamReader);
// printWriter= new PrintWriter(new BufferedWriter(new
// OutputStreamWriter(clientSocket.getOutputStream())));
//
// sendServerMessage("Hello Client");
// System.out.println("Handshake sent.");
//
// message = bufferedReader.readLine();
//
// System.out.println(message);
//
// if (checkMessage("Hello Server", message)){
// System.out.println(message);
// } else {
// throw new UnexpectedClientMessageException("Unexpected message from Client."
// + message);
// }
//
// decision = loginAcceptedByUser(bufferedReader.readLine());
//
// sendServerMessage("Goodbye Client//
// public class AppHomeServer{
//
// private ServerSocket serverSocket;
// private Socket clientSocket;
// private InputStreamReader inputStreamReader;
// private BufferedReader bufferedReader;
// private PrintWriter printWriter;
// private String message;
// public boolean decision;
//
// public void startServer() throws UnexpectedClientMessageException {
//
// try {
// serverSocket = new ServerSocket(4444);
//
// } catch (IOException e) {
// System.out.println("Could not listen on port: 4444");
// }
//
// System.out.println("AppHomeServer started. Listening to the port 4444");
//
// while (true) {
// try {
// clientSocket = serverSocket.accept();
// System.out.println("AppClient accepted.");
// inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
// bufferedReader = new BufferedReader(inputStreamReader);
// printWriter= new PrintWriter(new BufferedWriter(new
// OutputStreamWriter(clientSocket.getOutputStream())));
//
// sendServerMessage("Hello Client");
// System.out.println("Handshake sent.");
//
// message = bufferedReader.readLine();
//
// System.out.println(message);
//
// if (checkMessage("Hello Server", message)){
// System.out.println(message);
// } else {
// throw new UnexpectedClientMessageException("Unexpected message from Client."
// + message);
// }
//
// decision = loginAcceptedByUser(bufferedReader.readLine());
//
// sendServerMessage("Goodbye Client");
//
// inputStreamReader.close();
// clientSocket.close();
//
// } catch (IOException ex) {
// System.out.println("Problem in message reading");
// }
// }
//
// }
//
// /**
// * Method assesses both parameter Strings are equal.
// * @param string The expected message from the Client.
// * @param string1 The actual message from the Client.
// * @return boolean true if they match, false if not.
// */
// private boolean checkMessage(String string, String string1) throws
// UnexpectedClientMessageException {
// if (string.equals(string1)){
// return true;
// } else {");
//
// inputStreamReader.close();
// clientSocket.close();
//
// } catch (IOException ex) {
// System.out.println("Problem in message reading");
// }
// }
//
// }
//
// /**
// * Method assesses both parameter Strings are equal.
// * @param string The expected message from the Client.
// * @param string1 The actual message from the Client.
// * @return boolean true if they match, false if not.
// */
// private boolean checkMessage(String string, String string1) throws
// UnexpectedClientMessageException {
// if (string.equals(string1)){
// return true;
// } else {
// return false;
// }
// }
//
// /**
// * Sends a message to the client
// * @param message - the message we wish to send
// */
// public void sendServerMessage(String message){
// printWriter.print(message);
// }
//
// public boolean loginAcceptedByUser(String message) {
// try {
// if(checkMessage("Accepted", message)){
// return true;
// } else {
// return false;
// }
// } catch (Exception e) {
// System.out.println("AHS: ");
// e.printStackTrace();
// return false;
// }
// }
//
//
//
// // public static void main(String[] args){
// // AppHomeServer ahs = new AppHomeServer();
// // try {
// // ahs.startServer();
// // } catch (UnexpectedClientMessageException e) {
// // System.out.println("AHS Main: ");
// // e.printStackTrace();
// // }
// // }
// //
//
//
// }