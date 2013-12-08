package com.example.bankauthenticator;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class AppClient {

	private String serverMessage;
	public static final String SERVERIP = "147.188.196.137";
	public static final int SERVERPORT = 4444;
	private boolean mRun = false;
	private Socket socket;
	
	PrintWriter out;
	BufferedReader in;

	public AppClient() {
	}

	/**
	 * Sends the message entered by client to the server
	 * 
	 * @param message
	 *            text entered by client
	 */
	public void sendMessage(String message) {
		if (out != null && !out.checkError()) {
			out.print(message);
			out.flush();
		}
	}

	public void stopClient() {
		mRun = false;
		try {
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("AHS: Error closing socket");
			e.printStackTrace();
		}
	}

	public void run() {

		mRun = true;

		try {
			// here you must put your computer's IP address.
			InetAddress serverAddr = InetAddress.getByName(SERVERIP);

			Log.e("TCP Client", "C: Connecting...");

			// create a socket to make the connection with the server
			socket = new Socket(serverAddr, SERVERPORT);

			// send the message to the server
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

			Log.e("AppC", "C: Sent.");

			Log.e("AppC", "C: Done.");

			// receive the message which the server sends back
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// in this while the client listens for the messages sent by the
			// server
				Log.d("AC: ", "we got this far");
				serverMessage = in.readLine();
				Log.d("AC: ", serverMessage);

				if (serverMessage.equals("Hello Client")){
					sendMessage("Hello Server");
				} else if (serverMessage.equals("Goodbye Client")){
					Log.d("AC: ", serverMessage);
					stopClient();
				}

			Log.e("RESPONSE FROM SERVER", "S: Received Message: '"
					+ serverMessage + "'");

		} catch (Exception e) {
			System.out.println("AppClient: ");
			e.printStackTrace();
		}

	}
}
