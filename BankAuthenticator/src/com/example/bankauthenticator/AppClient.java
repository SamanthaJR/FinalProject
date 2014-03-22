/**
 * Client class used to connect to the AppHomeServer. It is either instantiated in 
 * the MainActivity when a call to register is made, or it is instantiated in the 
 * GcmBroadcastReceiver class when a login attempt is alerted to the app.
 */
package com.example.bankauthenticator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.net.ssl.*;

public class AppClient {

	private String serverMessage;
//	public static final String SERVERIP = "147.188.195.197"; // is -12038 IP
	public static final String SERVERIP = "147.188.195.196"; // is -12037 IP
	// public static final String SERVERIP = "147.188.195.146"; //is upstairs IP
	public static final int SERVERPORT = 4444;
	private boolean mRun = false;
	private SSLSocket socket;
	public Context cntx;
	public String regid, username, password, locRadius, locName, what,
			location, transType;
	public int length;

	private PrintWriter out;
	private BufferedReader in;

	public AppClient(Context cntx, String what, int length, String regid,
			String username, String password, String locRadius, String locName,
			String location, String transType) {
		this.cntx = cntx;
		this.what = what;
		this.regid = regid;
		this.length = length;
		this.username = username;
		this.password = password;
		this.locName = locName;
		this.locRadius = locRadius;
		this.location = location;
		this.transType = transType;
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
	 * Closes the streams and then the socket and sets the mRun variable to
	 * false, which alerts the rest of the class to stop listening for server
	 * messages.
	 */
	public void stopClient() {
		mRun = false;
		try {
			if (out != null)
				;
			out.close();
			if (in != null)
				;
			in.close();
			if (socket != null) {
				socket.close();
			}
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
			InetAddress serverAddr = InetAddress.getByName(SERVERIP);

			Log.e("TCP Client", "C: Connecting...");

			// Load the self-signed server certificate so it can be trusted
			char[] passphrase = "SecureLock".toCharArray();
			KeyStore ksTrust = KeyStore.getInstance("BKS");
			ksTrust.load(
					cntx.getResources().openRawResource(R.raw.clienttruststore),
					passphrase);
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(ksTrust);

			// Load the self-signed client certificate so it can be sent
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(cntx.getResources()
					.openRawResource(R.raw.clientptwel), passphrase);
			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, passphrase);

			// Create a SSLContext with the certificate
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
					new SecureRandom());

			// Create a secure socket, wrap input and output to PrintWriter and
			// BufferedReader.
			SSLSocketFactory factory = sslContext.getSocketFactory();
			SSLSocket socket = (SSLSocket) factory.createSocket(serverAddr,
					SERVERPORT);

			Log.d("AppClient", "created socket");

			// create input and output reader.
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			Log.d("AC: ", "we got this far");
			// read messages from Server until AppClient is prompted, by the
			// server, to end communication.
			while (mRun) {

				// Read first line of the communication protocol.
				serverMessage = in.readLine();
				Log.d("AC: ", serverMessage);

				if (serverMessage.equals("Hello Client")) {
					sendMessage("Hello Server");

					// If the task is to authenticate a login attempt.
					if (what.equalsIgnoreCase("authenticating")) {
						sendMessage("user log in");
						sendMessage(regid);
						// If the task is to register a new device/user.
					} else if (what.equalsIgnoreCase("registering")) {
						sendMessage("registering");
						Log.d("AC: ", Integer.toString(length));
						sendMessage(Integer.toString(length));
						sendMessage(regid + '^' + username + '^' + password);
						// If the task is to update the database after a
						// Geofence transition.
					} else if (what.equalsIgnoreCase("locationing")) {
						sendMessage(what);
						sendMessage(regid);
						sendLengthAndMessage(locName);
						sendMessage(transType);
						// If the task is to add a new Safe Location.
					} else if (what.equalsIgnoreCase("addLocation")) {
						sendMessage("addLocation");
						Log.d("AC: ", "Sending new Location");
						sendMessage(regid);
						sendLengthAndMessage(username);
						sendLengthAndMessage(password);
						sendLengthAndMessage(locName);
						sendLengthAndMessage(locRadius);
						sendLengthAndMessage(location);
						// If the task is to remove the registration of a user.
					} else if (what.equalsIgnoreCase("de-register")) {
						sendMessage(what);
						Log.d("AC: ", Integer.toString(length));
						sendMessage(Integer.toString(length));
						sendMessage(regid + '^' + username + '^' + password);
						// If the task is to delete a user's Safe Location.
					} else if (what.equalsIgnoreCase("delLocation")) {
						sendMessage(what);
						sendMessage(regid);
						sendLengthAndMessage(username);
						sendLengthAndMessage(password);
						sendLengthAndMessage(locName);
					}

					// If the server signals the end of communication.
				} else if (serverMessage.equals("Goodbye Client")) {
					Log.d("AC: ", serverMessage);
					stopClient();
					// If the server signals that the user is already registered
					// when they attempt to register.
				} else if (serverMessage.equals("Already Registered Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"Aready Registered");
					stopClient();
					// If the server signals that the username is already taken
					// when they attempt to register.
				} else if (serverMessage.equals("Username Taken Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, "Username Taken");
					stopClient();
					// If the server signals that a registration attempt is
					// successful.
				} else if (serverMessage
						.equals("Succesful Registration Client")) {
					Log.d("AC: ", serverMessage);
					startSuccessActivity("Registration");
					// If the server signals that a location is successfully
					// added.
				} else if (serverMessage
						.equals("Location successfully added Client")) {
					Log.d("AC: ", serverMessage);
					// Start new GeoSetterActivity to add the Geofence.
					ArrayList<String> locationName = new ArrayList<String>();
					locationName.add(locName);
					startGeoSetterActivity("add", locationName, null);
					// If the server signals that a location name is already
					// taken when a user tries to add a new one.
				} else if (serverMessage
						.equals("Location already taken Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							"Location already present");
					// If the server signals that the user's authentication
					// details were entered incorrectly.
				} else if (serverMessage
						.equals("Login details incorrect Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"Your Username or Password was entered incorrectly, please try again!");
					// If the server signals that a location name has been used
					// for a different location that the user has already
					// created.
				} else if (serverMessage.equals("Location name taken Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog()
							.executeOnExecutor(
									AsyncTask.THREAD_POOL_EXECUTOR,
									"This Location name is already being used for a different location, please choose another and try again.");
					// If the server signals that a device is not registered
					// when a user tries to add a location.
				} else if (serverMessage.equals("Device not Registered Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"Please register this device before adding locations.");
					// If the server signals that it can safely remove a device
					// registration once the app has removed all Geofences.
				} else if (serverMessage.equals("Removing registration Client")) {
					Log.d("AC: ", serverMessage);
					// read all location names in from server
					// these are the unique ids for the geofences that must be
					// removed
					// add ids to intent extras for GeoSetter Activity to use
					String numberOfLocations = in.readLine();
					int nol = Integer.parseInt(numberOfLocations);
					ArrayList<String> locationNameStrings = new ArrayList<String>();
					for (int i = 0; i < nol; i++) {
						String nextLoc = in.readLine();
						locationNameStrings.add(nextLoc);
					}
					if (nol > 0) {
						startGeoSetterActivity("remove", locationNameStrings,
								"all");
					} else {
						// If no Geofences need to be removed
						// Just alert user that registration removal was
						// successful
						startSuccessActivity("Dereg");
					}
					// If the server signals that it could not remove the users
					// registration details
				} else if (serverMessage
						.equals("Error removing registration Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"There was a problem removing your registration details, please try again.");
					// If the server signals that it could not remove a user's
					// Safe Location information
				} else if (serverMessage
						.equals("Error removing location Client")) {
					Log.d("AC: ", serverMessage);
					new ShowDialog()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"There was a problem removing your location details, please try again.");
					// If the server signals that it successfully removed a
					// user's specified Safe Location
				} else if (serverMessage
						.equals("Successfully removed location Client")) {
					ArrayList<String> locationName = new ArrayList<String>();
					locationName.add(locName);
					// Remove the associated Geofence with the location
					startGeoSetterActivity("remove", locationName, null);
				} else if (serverMessage
						.equals("Successfully removed registration Client")) {
					stopClient();
				} else if (serverMessage
						.equals("Location does not exist Client")) {
					new ShowDialog()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							"This location does not exist, please make sure you have typed it properly and try again.");
				}
			}

		} catch (Exception e) {
			Log.e("AppClient: ", e.toString());
			e.printStackTrace();
			startSuccessActivity("Fail");
		}

	}

	/**
	 * Method starts a new GeoSetter Activity which adds or removes Geofences as
	 * specified
	 * 
	 * @param task
	 *            - states whether to add the given locations as Geofences or
	 *            whether to remove them
	 * @param allLocations
	 *            - the locations to be added or removed
	 * @param all
	 *            - set only if the user is deregistering and more than one
	 *            Geofence needs to be removed
	 */
	private void startGeoSetterActivity(String task,
			ArrayList<String> allLocations, String all) {
		Intent nt = new Intent(cntx, GeoSetterActivity.class);
		nt.setClassName("com.example.bankauthenticator",
				"com.example.bankauthenticator.GeoSetterActivity");
		if (!locRadius.equals("")) {
			nt.putExtra("RADIUS_EXTRA", Float.parseFloat(locRadius));
		}
		nt.putExtra("LOCATION_NAMES", allLocations);
		nt.putExtra("REG_ID", regid);
		nt.putExtra("TASK", task);
		nt.putExtra("DEREG_ALERT", all);
		nt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		cntx.startActivity(nt);
	}

	/**
	 * Method starts a new SuccessActivity which just alerts the user to
	 * something that they need to know. Usually this is more important than the
	 * information encapsulated in a DialogFragment.
	 * 
	 * @param resp
	 *            - String to signal what message needs to be shown when the
	 *            Activity is shown.
	 */
	private void startSuccessActivity(String resp) {
		Intent nt = new Intent(cntx, SuccessActivity.class);
		nt.setClassName("com.example.bankauthenticator",
				"com.example.bankauthenticator.SuccessActivity");
		nt.putExtra("USER_RESPONSE", resp);
		nt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		cntx.startActivity(nt);
	}

	/**
	 * Method sends the length of a message to the server before sending the
	 * message itself.
	 * 
	 * @param mess
	 *            - the message to be sent.
	 */
	private void sendLengthAndMessage(String mess) {
		int uLen = mess.length();
		if (uLen < 10) {
			sendMessage("000" + Integer.toString(uLen));
		} else if (uLen >= 10 && uLen < 100) {
			sendMessage("00" + Integer.toString(uLen));
		} else if (uLen >= 100 && uLen < 1000) {
			sendMessage("0" + Integer.toString(uLen));
		} else {
			sendMessage(Integer.toString(uLen));
		}
		sendMessage(mess);
	}

	/**
	 * Class used to display a DialogFragment message on the UI thread. It takes
	 * in a String on creation that dictates the nature of the message
	 * displayed.
	 * 
	 * @author sjr090
	 * 
	 */
	private class ShowDialog extends AsyncTask<String, Void, CharSequence> {
		CharSequence text;
		AlertUserFragment aluf;

		// Set the message to be displayed
		@Override
		protected CharSequence doInBackground(String... params) {
			text = params[0];

			aluf = new AlertUserFragment();
			Bundle args = new Bundle();

			if (params[0].equalsIgnoreCase("Aready Registered")) {
				text = "This device has already been registered.";
			} else if (params[0].equalsIgnoreCase("Username Taken")) {
				text = "This username has already been taken, please select another and try again.";
			} else if (params[0].equalsIgnoreCase("Successfully Registered")) {
				text = "Registration Successful!";
			}

			args.putString("MESSAGE", String.valueOf(text));
			aluf.setArguments(args);

			return text;
		}

		// Show the DialogFragment.
		@Override
		protected void onPostExecute(CharSequence text) {
			if (cntx instanceof FragmentActivity) {
				FragmentActivity frag = (FragmentActivity) cntx;
				aluf.show(frag.getSupportFragmentManager(), "AppClient" + text);
			}
		}

	}

}
