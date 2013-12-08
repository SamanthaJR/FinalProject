package serverPackage;


public class ServerProtocol {

	private static GCMServer gcm;
	private AppHomeServer ahs;

	public ServerProtocol(AppHomeServer ahs){
		
		gcm = new GCMServer();
		this.ahs = ahs;
	}
	
	/**
	 * Authenticate method that ensures that the correct username and passowrd combination have been entered
	 * @param u the username (Note this is currently hardcoded to expect only a single correct string of "SamanthJR")
	 * @param p the password (Note this is currently hardcoded to expect only a single correct string of "123"
	 * @return true if correct password/username combination. False if not.
	 */
	public boolean authenticate(String u, String p) {
		if(u.contentEquals("SamanthaJR") && p.contentEquals("123")){
			String response = gcm.postToGCM();
//			System.out.println(response);
			while(ahs.decision == null){}
			if(ahs.decision.equalsIgnoreCase("true")){
				return true;
			} else {
				return false;
			}
		}else{
			return false;
		}
		
	}
	
	
}
