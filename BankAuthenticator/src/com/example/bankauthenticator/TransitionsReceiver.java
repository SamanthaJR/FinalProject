package com.example.bankauthenticator;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TransitionsReceiver extends BroadcastReceiver {
	
	String regid;
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("TBR: ", "Handling transition intent");
//		List<Geofence> triggersList = LocationClient
//				.getTriggeringGeofences(intent);
//		Log.d("TBR: ", triggersList.toString());
//		new debugToast().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Handling transition intent");
		this.context = context;
		// First check for errors
		if (LocationClient.hasError(intent)) {
			// Get the error code with a static method
			int errorCode = LocationClient.getErrorCode(intent);
			// Log the error
			Log.e("ReceiveTransitionsIntentService",
					"Location Services error: " + Integer.toString(errorCode));
			/*
			 * You can also send the error code to an Activity or Fragment with
			 * a broadcast Intent
			 */
			/*
			 * If there's no error, get the transition type and the IDs of the
			 * geofence or geofences that triggered the transition
			 */
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
					String[] temp = new String[] {triggerIds[0], "enter in"};
					Log.d("TBR: ", "Enter in");
					new connectGeoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, temp);
				} else {
					String[] temp = new String[] {triggerIds[0], "exit out"};
					Log.d("TBR: ", "Exit out");
					new connectGeoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,temp);
				}
			}
		}
	}

	/**
	 * AsyncTask class that creates a new AppClient on a new thread and sets it
	 * running to make the connection to the server. It initiates the object
	 * with the values required to send the correct messages for updating
	 * geofence info.
	 * 
	 * @author sjr090
	 * 
	 */
	class connectGeoTask extends AsyncTask<String, String, AppClient> {

		@Override
		protected AppClient doInBackground(String... message) {
			String name = message[0];
			String transType = message[1];
			AppClient mAppClient = new AppClient(context,
					"locationing", 0, regid, "", "", "", name, "", transType);
			mAppClient.run();

			return null;
		}
	}
	
	private class debugToast extends AsyncTask<String, Void, CharSequence> {

		@Override
		protected CharSequence doInBackground(String... params) {
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, params[0], duration);
			toast.show();
			return null;
		}
	}
}
