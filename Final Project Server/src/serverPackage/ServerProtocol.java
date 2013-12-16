package serverPackage;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


public class ServerProtocol {

	private static GCMServer gcm;
	public boolean decision;
	public AppHomeServer ahs;
	public ArrayList<ResponseEvent> responses = new ArrayList<ResponseEvent>();
	private CountDownLatch block;
	public String id = "APA91bFiyte1lCvhlfa5JnSeCCia-jYxDyhbYEilMjmS3Zvxe8pSzg7NDvUTJYefCKt1WGzdk1eyK8NQqW7wvbh6YjKe9V-k2UCvpgAvolJCyO1gNPpImmVQMfmHxUlcQyksbUEBSw3KHArfwfE2MmELjblDAGUIblrlDQ1Fzi0iD1Ig65U9GR4";
	
	public ServerProtocol(AppHomeServer ahs){
		this.ahs = ahs;
		gcm = new GCMServer();
		block = new CountDownLatch(1);
	}
	
	/**
	 * Authenticate method that ensures that the correct username and password combination have been entered
	 * @param u the username (Note this is currently hardcoded to expect only a single correct string of "SamanthJR")
	 * @param p the password (Note this is currently hardcoded to expect only a single correct string of "123"
	 * @return true if correct password/username combination. False if not.
	 */
	public boolean authenticate(String u, String p) {
		if (u.contentEquals("SamanthaJR") && p.contentEquals("123")) {
			ahs.setId(this);
			gcm.postToGCM();
			
			try {
				block.await();
			} catch (InterruptedException e) {
				System.out.println("Proto: ");
				e.printStackTrace();
				return false;
			}
			
		}
		
		return decision;
	}

	/**
	 * set method that changes the class variable 'decision' which encapulates the user's decision on whether or not they wish
	 * to allow login.
	 * @param b - the boolean we wish to set bool 'decision' to equal.
	 */
	public void setDecision(boolean b) {
		decision = b;
	}
	
	public void responseReceived(ResponseEvent resp) {
		setDecision(resp.getResponse());
		block.countDown();
	}

	public String getId() {
		return id;
	}
}
