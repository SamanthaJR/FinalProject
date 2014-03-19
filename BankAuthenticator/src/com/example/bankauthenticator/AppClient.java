/**
 * Client class used to connect to the AppHomeServer. It is either instantiated in 
 * the MainActivity when a call to register is made, or it is instantiated in the 
 * GcmBroadcastReceiver class when a login attempt is alerted to the app.
 */
package com.example.bankauthenticator;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.net.ssl.*;

public class AppClient {

	private String serverMessage;
	public static final String SERVERIP = "147.188.195.197"; // is -12038 IP
	// public static final String SERVERIP = "147.188.195.196"; // is downstairs
	// IP
	// public static final String SERVERIP = "147.188.195.146"; //is upstairs IP
	public static final int SERVERPORT = 4444;
	private boolean mRun = false;
	private SSLSocket socket;
	public Context cntx;
	public String regid, username, password, locRadius, locName, what,
			location, transType;
	int length;

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

	private InetAddress getLocalAddress() throws IOException {

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						// return inetAddress.getHostAddress().toString();
						return inetAddress;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("AC: ", ex.toString());
		}
		return null;
	}

	/**
	 * Method called when the AppClient is created to start it running
	 */
	public void run() {

		mRun = true;

		try {
			// here you must put your computer's IP address.
			InetAddress serverAddr = InetAddress.getByName(SERVERIP);
			InetAddress localAddr = getLocalAddress();

			Log.e("TCP Client", "C: Connecting...");

			// Load the self-signed server certificate
			char[] passphrase = "SecureLock".toCharArray();
			KeyStore ksTrust = KeyStore.getInstance("BKS");
			ksTrust.load(
					cntx.getResources().openRawResource(R.raw.clienttruststore),
					passphrase);
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(ksTrust);

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

			// Socket socket = new Socket(serverAddr, SERVERPORT);

			Log.d("AppClient", "created socket");

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

					if (what.equalsIgnoreCase("authenticating")) {
						sendMessage("user log in");
						sendMessage(regid);
					} else if (what.equalsIgnoreCase("registering")) {
						sendMessage("registering");
						Log.d("AC: ", Integer.toString(length));
						sendMessage(Integer.toString(length));
						sendMessage(regid + '^' + username + '^' + password);
					} else if (what.equalsIgnoreCase("locationing")) {
						sendMessage(what);
						sendMessage(regid);
						sendLengthAndMessage(locName);
						sendMessage(transType);
					} else if (what.equalsIgnoreCase("addLocation")) {
						sendMessage("addLocation");
						Log.d("AC: ", "Sending new Location");
						sendMessage(regid);
						sendLengthAndMessage(username);
						sendLengthAndMessage(password);
						sendLengthAndMessage(locName);
						sendLengthAndMessage(locRadius);
						sendLengthAndMessage(location);
					} else if (what.equalsIgnoreCase("de-register")) {
						sendMessage(what);
						Log.d("AC: ", Integer.toString(length));
						sendMessage(Integer.toString(length));
						sendMessage(regid + '^' + username + '^' + password);
					} else if (what.equalsIgnoreCase("delLocation")) {
						sendMessage(what);
						sendMessage(regid);
						sendLengthAndMessage(username);
						sendLengthAndMessage(password);
						sendLengthAndMessage(locName);
					}

				} else if (serverMessage.equals("Goodbye Client")) {
					Log.d("AC: ", serverMessage);
					stopClient();
				} else if (serverMessage.equals("Already Registered Client")) {
					Log.d("AC: ", serverMessage);
					new showToast()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"Aready Registered");
					stopClient();
				} else if (serverMessage.equals("Username Taken Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, "Username Taken");
					stopClient();
				} else if (serverMessage
						.equals("Succesful Registration Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							"Successfully Registered");
				} else if (serverMessage
						.equals("Location successfully added Client")) {
					Log.d("AC: ", serverMessage);
					// new
					// showToast().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					// "New Location Added");
					Location loc = new Location(location);
					// Start new successfully added geofences
					ArrayList<String> locationName = new ArrayList<String>();
					locationName.add(locName);
					startGeoSetterActivity("add", locationName, null);

				} else if (serverMessage
						.equals("Location already taken Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							"Location already present");
				} else if (serverMessage.equals("Radius updated Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							"Location Radius updated");
				} else if (serverMessage
						.equals("Login details incorrect Client")) {
					Log.d("AC: ", serverMessage);
					new showToast()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"Your Username or Password was entered incorrectly, please try again!");
				} else if (serverMessage.equals("Location name taken Client")) {
					Log.d("AC: ", serverMessage);
					new showToast()
							.executeOnExecutor(
									AsyncTask.THREAD_POOL_EXECUTOR,
									"This Location name is already being used for a different location, please choose another and try again.");
				} else if (serverMessage.equals("Device not Registered Client")) {
					Log.d("AC: ", serverMessage);
					new showToast()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"Please register this device before adding locations.");
				} else if (serverMessage
						.equals("Successfully removed registration Client")) {
					Log.d("AC: ", serverMessage);
					new showToast().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							"Device Registration successfully removed.");
					// read all location names in
					// put in a list
					// add list to intent extras for GeoSetter Activity
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
						
						// TODO start successact to confirm device de-reg.
						//startSuccessActivity
					}
				} else if (serverMessage
						.equals("Error removing registration Client")) {
					Log.d("AC: ", serverMessage);
					new showToast()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"There was a problem removing your registration details, please try again.");
				} else if (serverMessage
						.equals("Error removing location Client")) {
					Log.d("AC: ", serverMessage);
					new showToast()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"There was a problem removing your location details, please try again.");
				} else if (serverMessage
						.equals("Successfully removed location Client")) {
					ArrayList<String> locationName = new ArrayList<String>();
					locationName.add(locName);
					startGeoSetterActivity("remove", locationName, null);
				}
			}

			// Log.e("RESPONSE FROM SERVER", "S: Received Message: '"
			// + serverMessage + "'");

		} catch (Exception e) {
			Log.e("AppClient: ", e.toString());
			e.printStackTrace();
		}

	}

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
			text = params[0];
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
