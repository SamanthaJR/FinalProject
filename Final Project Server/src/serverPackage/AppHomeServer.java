package serverPackage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Stack;

public class AppHomeServer {

	public static String decision;
	public static Stack <ServerProtocol>  listenerQueue = new Stack <ServerProtocol>();

	public void startServer() {

		while (true) {
			try {

				ServerSocket listener = new ServerSocket(4444);
				System.out.println("AHS: Started, Listening to port 4444");
				Socket server;

				// doComms connection;

				server = listener.accept();
				System.out.println("AHS: AppClient accepted.");

				doComms conn_c = new doComms(server);
				Thread t = new Thread(conn_c);
				t.start();

			} catch (IOException ioe) {
				System.out.println("AHS: Listen error " + ioe);
				ioe.printStackTrace();
			}
		}
	}

	public void setId(ServerProtocol rl) {
		listenerQueue.add(rl);
	}

}

class doComms implements Runnable {
	private Socket server;
	private char[] line, resp, regid;

	public doComms(Socket server) {
		this.server = server;
	}

	public void run() {

		line = new char[12];
		resp = new char[8];
		regid = new char[183];

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


			is.read(regid, 0, 183);
			
			String id = String.valueOf(regid);
			
			System.out.println(id);
			
			System.out.println("Waiting for response");

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

	private synchronized void fireResponseEvent(boolean b, String id) {
		String ident = id;
		Stack <ServerProtocol> temp = new Stack<ServerProtocol>();
		ResponseEvent response = new ResponseEvent(this, ident, b);
		ServerProtocol rl = AppHomeServer.listenerQueue.pop();
		while (rl.getId().equalsIgnoreCase(id) == false){
			temp.push(rl);
		}
		rl.responseReceived(response);
		while(temp.empty() == false){
			ServerProtocol next = temp.pop();
			AppHomeServer.listenerQueue.push(next);
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