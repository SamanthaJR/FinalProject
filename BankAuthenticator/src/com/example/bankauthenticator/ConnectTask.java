/**
 * Class that creates a new AppClient and tells it to run with the given information in the constructor.
 * This allows any class to connect to the AppHomeServer and provide it with the necessary information.
 */
package com.example.bankauthenticator;

import android.content.Context;
import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<Void, AppClient, Void> {
	
	Context cntx;
	String what, regid, username, password, locRadius, locName, location, transType;
	int length;
	
	public ConnectTask(Context cntx, String what, int length, String regid,
			String username, String password, String locRadius, String locName,
			String location, String transType){
		
		this.cntx = cntx;
		this.what = what;
		this.length = length;
		this.regid = regid;
		this.username = username;
		this.password = password;
		this.locRadius = locRadius;
		this.locName = locName;
		this.location = location;
		this.transType = transType;
	}

	@Override
	protected Void doInBackground(Void... params) {
		AppClient appClient = new AppClient(cntx, what, length,
				regid, username, password, locRadius, locName, location, transType);
		appClient.run();
		return null;
	}
	
	

}
