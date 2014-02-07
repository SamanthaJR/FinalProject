/**
 * Client class used to connect to the AppHomeServer. It is either instantiated in 
 * the MainActivity when a call to register is made, or it is instantiated in the 
 * GcmBroadcastReceiver class when a login attempt is alerted to the app.
 */
package com.example.bankauthenticator;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class AppClient {

	private String serverMessage;
	public static final String SERVERIP = "147.188.196.138"; // is -12038 IP
	// public static final String SERVERIP = "147.188.196.137"; // is downstairs
	// IP
	// public static final String SERVERIP = "147.188.195.146"; //is upstairs IP
	public static final int SERVERPORT = 4444;
	private boolean mRun = false;
	private Socket socket;
	public Context cntx;
	boolean authenticating;
	public String regid, username, password;
	int length;

	private PrintWriter out;
	private BufferedReader in;

	public AppClient(Context cntx, boolean authenticating, int length,
			String regid, String username, String password) {
		this.cntx = cntx;
		this.authenticating = authenticating;
		this.regid = regid;
		this.length = length;
		this.username = username;
		this.password = password;
	}

	/**
	 * Sends the String message to the server
	 * 
	 * @param message
	 *            - text to be sent
	 */
	public void sendMessage(String message) {
		if (out != null && !out.checkError()) {
			out.print(message);
			out.flush();
		}
	}

	/**
	 * Sends an integer value to the server.
	 * 
	 * @param message
	 *            - int to be sent.
	 */
	public void sendIntMessage(int message) {
		if (out != null && !out.checkError()) {
			out.print(message);
			out.flush();
		}
	}

	/**
	 * Closes the streams and then the socket and sets the mRun variable to
	 * false, which alerts the rest of the class to stop listening for server
	 * messages.
	 */
	public void stopClient() {
		mRun = false;
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("AHS: Error closing socket");
			e.printStackTrace();
		}
	}

	/**
	 * Method called when the AppClient is created to start it running
	 */
	public void run() {

		mRun = true;

		try {
			// here you must put your computer's IP address.
			InetAddress serverAddr = InetAddress.getByName(SERVERIP);

			Log.e("TCP Client", "C: Connecting...");

			// create a socket, wrap input and output to PrintWriter and
			// BufferedReader.
			socket = new Socket(serverAddr, SERVERPORT);

			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			Log.d("AC: ", "we got this far");
			// read messages from Server until AppClient is prompted, by the
			// server, to end communication.
			while (mRun) {

				serverMessage = in.readLine();
				Log.d("AC: ", serverMessage);

				if (serverMessage.equals("Hello Client")) {
					sendMessage("Hello Server");

					if (authenticating) {
						sendMessage("user log in");
						sendMessage(regid);
					} else {
						sendMessage("registering");
						Log.d("AC: ", Integer.toString(length));
						sendIntMessage(length);
						sendMessage(regid + '^' + username + '^' + password);
					}

				} else if (serverMessage.equals("Goodbye Client")) {
					Log.d("AC: ", serverMessage);
					stopClient();
				} else if (serverMessage.equals("Already Registered Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().execute("Aready Registered");
					stopClient();
				} else if (serverMessage.equals("Username Taken Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().execute("Username Taken");
					stopClient();
				} else if (serverMessage
						.equals("Succesful Registration Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().execute("Successfully Registered");
				}
			}

			// Log.e("RESPONSE FROM SERVER", "S: Received Message: '"
			// + serverMessage + "'");

		} catch (Exception e) {
			Log.e("AppClient: ", e.toString());
			e.printStackTrace();
		}

	}

	/**
	 * Class used to display a toast message on the UI thread. It takes in a
	 * String on creation that dictates the nature of the message displayed.
	 * 
	 * @author sjr090
	 * 
	 */
	private class showToast extends AsyncTask<String, Void, CharSequence> {
		CharSequence text;

		@Override
		protected CharSequence doInBackground(String... params) {
			if (params[0].equalsIgnoreCase("Aready Registered")) {
				text = "This device has already been registered.";
			} else if (params[0].equalsIgnoreCase("Username Taken")) {
				text = "This username has already been taken, please select another and try again.";
			} else if (params[0].equalsIgnoreCase("Successfully Registered")) {
				text = "Registration Successful!";
			}
			return text;
		}

		@Override
		protected void onPostExecute(CharSequence text) {
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(cntx, text, duration);
			toast.show();
		}

	}

}
