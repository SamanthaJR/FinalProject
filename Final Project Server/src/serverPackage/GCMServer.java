// http://www.xyzws.com/Javafaq/how-to-use-httpurlconnection-post-data-to-web-server/139 
// website with example


package serverPackage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.*;

public class GCMServer {

	public String API_KEY = "AIzaSyDp8owY4eIcngiuG5NPH92tVn9mwOUsRVI";
//	public String reg_id = "APA91bELztfJIQZF6HpjNrWS-0nG-pYo1xFx6IXq5Xe3O5FzdPcQnpf_JoFKb52BAexS-W6euYkaW_jsFlEcWkA07tejb6bfTIbV6YDOSnNs_3PulXTxAQJNABL39gielVC5HciVra-gWB8FDQr3Un9mayqq7Mzkce_flT26teQKRdHtncmu3sA";
	public String reg_id; // = "APA91bFiyte1lCvhlfa5JnSeCCia-jYxDyhbYEilMjmS3Zvxe8pSzg7NDvUTJYefCKt1WGzdk1eyK8NQqW7wvbh6YjKe9V-k2UCvpgAvolJCyO1gNPpImmVQMfmHxUlcQyksbUEBSw3KHArfwfE2MmELjblDAGUIblrlDQ1Fzi0iD1Ig65U9GR4";
	public String header = "Authorization:key=AIzaSyDp8owY4eIcngiuG5NPH92tVn9mwOUsRVI";
	public String GCMDestinationURL = "https://android.googleapis.com/gcm/send";

	public GCMServer() {

	}

	
	/**
	 * Posts to the GCM server, currently only one device is hard coded in.
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

		try {
			obj.put("regid", reg_id);
//			System.out.println(obj.toString());
			mess.put("delay_while_idle", false);
			mess.put("data", obj);
			mess.put("registration_ids", jsonArray);
//			System.out.println(mess.toString());
		} catch (JSONException e1) {
			System.out.println("GCMS: Error creating JSON Object");
			e1.printStackTrace();
		}
		
		try {
			url = new URL(GCMDestinationURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
		} catch (MalformedURLException e) {
			System.out.println("GCMS: Problem with malformed URL");
			e.printStackTrace();
		} catch (ProtocolException e) {
			System.out.println("GCMS: Problem with URL set request method (to POST)");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("GCMS: Problem with URL open connection");
			e.printStackTrace();
		}

		connection.setRequestProperty("Content-Type", "application/json");
		connection.addRequestProperty("Authorization", "key=AIzaSyCoDTYQxRLIH7P5jGTx6_Np4bV4E3M29b4");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
//		System.out.println("We got this far");

		//Send JSON Object.
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
		
		 //Get Response	
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
		      while((line = rd.readLine()) != null) {
		        response.append(line);
		        response.append('\r');
		      }

		      rd.close();
		      
		      return response.toString();
		      
		} catch (IOException e) {
			System.out.println("GCMS: Problem reading from Server");
			e.printStackTrace();
		} finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
		}
		return "If this is returning, something went wrong";
	}
	
//	/**
//	 * Main method TO DELETE. Just for testing purposes, later this object will be instantiated and called through the WebServer class.
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		GCMServer serv = new GCMServer();
//		String cat = serv.postToGCM();
//		System.out.println(cat);
//	}
}