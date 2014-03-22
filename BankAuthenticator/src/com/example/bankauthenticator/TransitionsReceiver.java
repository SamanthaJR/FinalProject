/**
 * Class triggered by a Geofence transition. It simply connects to the AppHomeServer
 * and alerts it, telling it to change the information present in its database.
 */
package com.example.bankauthenticator;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class TransitionsReceiver extends BroadcastReceiver {
	
	public String regid;
	public Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("TBR: ", "Handling transition intent");
		this.context = context;
		// First check for errors
		if (LocationClient.hasError(intent)) {
			// Get the error code
			int errorCode = LocationClient.getErrorCode(intent);
			
			Log.e("ReceiveTransitionsIntentService",
					"Location Services error: " + Integer.toString(errorCode));
		} else {
			// Get the type of transition (entry or exit)
			int transitionType = LocationClient.getGeofenceTransition(intent);
			// Test that a valid transition was reported
			if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
					|| (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				List<Geofence> triggerList = LocationClient
						.getTriggeringGeofences(intent);

				String[] triggerIds = new String[triggerList.size()];

				for (int i = 0; i < triggerIds.length; i++) {
					// Store the Id of each geofence
					triggerIds[i] = triggerList.get(i).getRequestId();
				}

				regid = intent.getStringExtra("REG_ID");
				
				if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
					// Let the AppHomeServer know that the user has entered a Geofence
					Log.d("TBR: ", "Enter in");
					ConnectTask connt = new ConnectTask(context, "locationing", 0, regid, "", "", "", triggerIds[0], "", "enter in");
					connt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
					
				} else {
					// Let the AppHomeServer know that the user has exited a Geofence
					Log.d("TBR: ", "Exit out");
					ConnectTask connt = new ConnectTask(context,
							"locationing", 0, regid, "", "", "", triggerIds[0], "", "exit out");			
					connt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
				}
			}
		}
	}
}
