package serverPackage;


public class ServerProtocol {

	private static GCMServer gcm;
	public String decision;
	public AppHomeServer ahs;
	

	public ServerProtocol(AppHomeServer ahs){
		this.ahs = ahs;
		gcm = new GCMServer();
	}
	
	/**
	 * Authenticate method that ensures that the correct username and passowrd combination have been entered
	 * @param u the username (Note this is currently hardcoded to expect only a single correct string of "SamanthJR")
	 * @param p the password (Note this is currently hardcoded to expect only a single correct string of "123"
	 * @return true if correct password/username combination. False if not.
	 */
	public boolean authenticate(String u, String p) {
		if(u.contentEquals("SamanthaJR") && p.contentEquals("123")){
			ahs.setProto(this);
			gcm.postToGCM();
			
			while (decision == null){
				System.out.println("Decision still equals NUll");
			}
			
			System.out.println(decision);
			if(decision.equalsIgnoreCase("true")){
//				setDecision("waiting");
				return true;
			} else {
//				setDecision("waiting");
				return false;
			}
		}else{
			return false;
		}
		
	}

	/**
	 * set method that changes the class variable 'decision' which encapulates the user's decision on whether or not they wish
	 * to allow login.
	 * @param b - the boolean we wish to set bool 'decision' to equal.
	 */
	public void setDecision(String b) {
		decision = b;
	}
	
	
}
