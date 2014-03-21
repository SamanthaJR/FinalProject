/**
 * Class is a Fragment that allows the user to add their current location to the central 
 * database so it can be saved as Safe Location. If this is successful, it triggers the 
 * GeoSetterActivity to create a new Geofence. 
 */
package com.example.bankauthenticator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

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

public class AddLocationFragment extends Fragment implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, OnClickListener {

	private String usernm, pass, regid, radius, name;
	private Button mSubmitLcn;
	private EditText mUsername, mPassword, mRadius, mName;
	private int locLen;
	private Location mCurrentLocation;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public LocationClient mLocationClient;
	public IntentFilter mIntentFilter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View fragView = inflater.inflate(R.layout.add_location_fragment_view,
				container, false);
		mLocationClient = new LocationClient(this.getActivity(), this, this);

		Bundle args = getArguments();
		regid = args.getString("REG_ID");

		mUsername = (EditText) fragView.findViewById(R.id.location_username);
		mPassword = (EditText) fragView.findViewById(R.id.location_password);
		mRadius = (EditText) fragView.findViewById(R.id.location_radius);
		mName = (EditText) fragView.findViewById(R.id.location_name);
		mSubmitLcn = (Button) fragView
				.findViewById(R.id.location_submit_button);

		mSubmitLcn.setOnClickListener(this);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

		// Action for broadcast Intents that report successful removal of
		// geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

		// Action for broadcast Intents containing various types of geofencing
		// errors
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

		// All Location Services sample apps use this category
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		return fragView;

	}

	// Connect to Location Services when Fragment becomes visible.
	@Override
	public void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	// Disconnect from Location Services when Fragment is closed.
	@Override
	public void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	/**
	 * Method called when the Submit button is clicked. Checks the values
	 * entered in the text zones. If the password and confirm password fields
	 * are not identical, it launches a warning DialogFragment and prompts
	 * re-entry. Otherwise it prompts an AppClient object to connect to the
	 * AppHomeServer with all the suitable details needed to register the
	 * device.
	 * 
	 * @param view
	 */
	public void submitLocationClick(View view) {

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
			// If any of the fields aren't filled out properly
			AlertUserFragment aluf = new AlertUserFragment();
			Bundle args = new Bundle();
			args.putString("MESSAGE",
					"Please ensure all fields have been filled out");
			aluf.setArguments(args);
			aluf.show(getActivity().getSupportFragmentManager(),
					"Fill out Location");

		} else {
			// Otherwise connect to AppHomeServer.
			Log.d("ALF: ", "Location is: " + mCurrentLocation.toString());

			ConnectTask connt = new ConnectTask(getActivity(), "addLocation",
					locLen, regid, usernm, pass, radius, name,
					mCurrentLocation.toString(), "");
			connt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);

		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this.getActivity(),
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			Log.e("LocAct", Integer.toString(connectionResult.getErrorCode()));
		}
	}

	// Retrieve the user's current position so that if the fields are filled out
	// and the button clicked, this information can be used to create a
	// Geofence.
	@Override
	public void onConnected(Bundle arg0) {
		mCurrentLocation = mLocationClient.getLastLocation();
	}

	// Method needs to be implemented for interface, but my code requires no
	// action when disconnected from Location Services.
	@Override
	public void onDisconnected() {

	}

	// Calls my customised onClick method.
	@Override
	public void onClick(View v) {
		submitLocationClick(v);
	}

}
