package com.example.bankauthenticator;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class RemoveLocationFragment extends Fragment implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
OnClickListener{

	String usernm, pass, regid, radius, name;
	Button mSubmitLcn;
	EditText mUsername, mPassword, mRadius, mName, mRemovUser, mRemovPass, mRemovLocName;
	private int locLen;
	private AppClient mAppClient;
	private Location mCurrentLocation;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public LocationClient mLocationClient;
	List<Geofence> mCurrentGeofences;
    private SimpleGeofenceStore mGeofenceStorage;
    PendingIntent mTransitionPendingIntent;
    IntentFilter mIntentFilter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	
    	Bundle args = getArguments();
		regid = args.getString("REG_ID");
    	
    	View fragView = inflater.inflate(R.layout.remove_location_fragment_view, container, false);
    	
    	mRemovUser = (EditText) fragView.findViewById(R.id.remove_location_username);
		mRemovPass = (EditText) fragView.findViewById(R.id.remove_location_password);
		mRemovLocName = (EditText) fragView.findViewById(R.id.remove_location_name);
		mSubmitLcn = (Button) fragView.findViewById(R.id.remove_location_submit_button);
		
		mSubmitLcn.setOnClickListener(this);
		
		// Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this.getActivity());

//         Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
    	
    	return fragView;
    }

    
    public void submitRemoveLocationClick(View view) {
		usernm = mRemovUser.getText().toString();
		pass = mRemovPass.getText().toString();
		name = mRemovLocName.getText().toString();
		
		radius = "0";
		locLen = 0;
		
		int usernmLen = usernm.length();
		int passLen = pass.length();
		int nameLen = name.length();
		
		if (usernmLen == 0 || passLen == 0 || nameLen == 0) {
			launchToast("Please ensure all fields have been filled out");
		} else {
			// Tell ApHS to remove Geofence data from database
			new connectLocTask().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "delLocation");
		}
	}

	/**
	 * Method launches a toast object.
	 * 
	 * @param toastMess
	 *            - the message to be displayed in the toast.
	 */
	public void launchToast(String toastMess) {
		Context context = getActivity().getApplicationContext();
		CharSequence text = toastMess;
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	/**
	 * External class that is removed from the UI thread that instantiates a new
	 * AppClient object and calls its run() method.
	 * 
	 * @author sjr090
	 * 
	 */
	public class connectLocTask extends AsyncTask<String, String, AppClient> {

		@Override
		protected AppClient doInBackground(String... message) {

			// we create a TCPClient object and pass to it all the data it
			// needs.
			mAppClient = new AppClient(getActivity().getBaseContext(), message[0], locLen,
					regid, usernm, pass, radius, name, mCurrentLocation.toString(), "");
			mAppClient.run();

			return null;
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this.getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e("LocAct", Integer.toString(connectionResult.getErrorCode()));
        }
    }


	@Override
	public void onConnected(Bundle arg0) {
//		launchToast("Location Services connected!");

		mCurrentLocation = mLocationClient.getLastLocation();
//		launchToast("lat = "
//					+ String.valueOf(mCurrentLocation.getLatitude())
//					+ "long = "
//					+ String.valueOf(mCurrentLocation.getLongitude()));
		
	}

	@Override
	public void onDisconnected() {
	}


	@Override
	public void onClick(View v) {
		submitRemoveLocationClick(v);
	}
	
}
