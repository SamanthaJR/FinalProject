package com.example.bankauthenticator;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

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

public class AddLocationFragment extends Fragment implements
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

    View fragView = inflater.inflate(R.layout.add_location_fragment_view, container, false);
    mLocationClient = new LocationClient(this.getActivity(), this, this);
    
    Bundle args = getArguments();
	regid = args.getString("REG_ID");
	
	mUsername = (EditText) fragView.findViewById(R.id.location_username);
	mPassword = (EditText) fragView.findViewById(R.id.location_password);
	mRadius = (EditText) fragView.findViewById(R.id.location_radius);
	mName = (EditText) fragView.findViewById(R.id.location_name);
	mSubmitLcn = (Button) fragView.findViewById(R.id.location_submit_button);
	
	mSubmitLcn.setOnClickListener(this);
	
	// Instantiate a new geofence storage area
    mGeofenceStorage = new SimpleGeofenceStore(this.getActivity());

//     Instantiate the current List of geofences
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
    
    /**
	 * Method called when the Submit button is clicked. Checks the values
	 * entered in the text zones. If the password and confirm password fields
	 * are not identical, it launches a warning toast and prompts re-entry.
	 * Otherwise it prompts an AppClient object to connect to the AppHomeServer
	 * with all the suitable details needed to register the device.
	 * 
	 * @param view
	 */
	public void submitLocationClick(View view) {
		
		mLocationClient.connect();

		usernm = mUsername.getText().toString();
		pass = mPassword.getText().toString();
		radius = mRadius.getText().toString();
		name = mName.getText().toString();
		
		// calc lengths of vals from text zones
		int usernmLen = usernm.length();
		int passLen = pass.length();
		int radLen = radius.length();
		int nameLen = name.length();
		locLen = 183 + 1 + usernmLen + 1 + passLen + 1 + radLen + 1 + nameLen;

		if (usernmLen == 0 || passLen == 0 || radLen == 0 || nameLen == 0) {
			launchToast("Please ensure all fields have been filled out");
		} else {
	        
			new connectLocTask().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "addlocation");        
		}

	}
	
	
	/**
	 * Method launches a toast object.
	 * 
	 * @param toastMess
	 *            - the message to be displayed in the toast.
	 */
	public void launchToast(String toastMess) {
		Context context = getActivity();
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
			String r = regid;
			String use = usernm;
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
		
	}

	@Override
	public void onDisconnected() {
		
	}

	@Override
	public void onClick(View v) {
		submitLocationClick(v);
	}
	
}
