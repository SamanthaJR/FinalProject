// http://www.xyzws.com/Javafaq/how-to-use-httpurlconnection-post-data-to-web-server/139 
// website with example

package serverPackage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import org.json.*;

public class GCMServer {

	public String API_KEY = "AIzaSyDp8owY4eIcngiuG5NPH92tVn9mwOUsRVI";
	public String reg_id;
	public String header = "Authorization:key=AIzaSyDp8owY4eIcngiuG5NPH92tVn9mwOUsRVI";
	public String GCMDestinationURL = "https://android.googleapis.com/gcm/send";

	public GCMServer() {

	}

	/**
	 * Posts message to the GCM server. Content of message is unimportant as
	 * only one type if message is ever sent.
	 * 
	 * @return the body of the GCM Server's response.
	 */
	public String postToGCM(String reg_id) {

		this.reg_id = reg_id;

		URL url;
		HttpsURLConnection connection = null;

		JSONObject mess = new JSONObject();
		JSONObject obj = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		jsonArray.put(reg_id);
		// Create the JSON Object with the required message data.
		try {
			obj.put("regid", reg_id);
			// System.out.println(obj.toString());
			mess.put("delay_while_idle", false);
			mess.put("data", obj);
			mess.put("registration_ids", jsonArray);
			// System.out.println(mess.toString());
		} catch (JSONException e1) {
			System.out.println("GCMS: Error creating JSON Object");
			e1.printStackTrace();
		}

		// Try and set up a connection to the GCM servers.
		try {
			url = new URL(GCMDestinationURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
		} catch (MalformedURLException e) {
			System.out.println("GCMS: Problem with malformed URL");
			e.printStackTrace();
		} catch (ProtocolException e) {
			System.out
					.println("GCMS: Problem with URL set request method (to POST)");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("GCMS: Problem with URL open connection");
			e.printStackTrace();
		}

		// Add appropriate headers.
		connection.setRequestProperty("Content-Type", "application/json");
		connection.addRequestProperty("Authorization",
				"key=AIzaSyCoDTYQxRLIH7P5jGTx6_Np4bV4E3M29b4");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		// System.out.println("We got this far");

		// Send JSON Object.
		try {
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			PrintWriter pw = new PrintWriter(wr);
			pw.println(mess.toString());
			pw.flush();
			pw.close();
		} catch (IOException e) {
			System.out.println("GCMS: Problem writing to server");
			e.printStackTrace();
		}

		// Get Response
		InputStream is;
		try {

			if (connection.getResponseCode() >= 400) {
				is = connection.getErrorStream();
			} else {
				is = connection.getInputStream();
			}

			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}

			rd.close();

			return response.toString();

		} catch (IOException e) {
			System.out.println("GCMS: Problem reading from Server");
			e.printStackTrace();
		} finally {

			if (connection != null) {
				connection.disconnect();
			}
		}
		return "If this is returning, something went wrong";
	}
}