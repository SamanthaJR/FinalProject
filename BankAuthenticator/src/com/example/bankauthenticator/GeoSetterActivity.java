/**
 * Activity Class adds or removes Geofences and then displays text to notify the user that this has been done.
 */
package com.example.bankauthenticator;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

import android.location.*;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;

public class GeoSetterActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		OnAddGeofencesResultListener, OnRemoveGeofencesResultListener {

	public TextView mResult, mSubHeading;
	private Location mCurrentLocation;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public LocationClient mLocationClient;
	public List<Geofence> mCurrentGeofences;
	private SimpleGeofenceStore mGeofenceStorage;
	public PendingIntent mTransitionPendingIntent;
	public IntentFilter mIntentFilter;
	private String task, regid, all;
	private ArrayList<String> locName;
	public float radius;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geo_setter);
		// Show the Up button in the action bar.
		setupActionBar();
		// Get all the extra information from the Intent that launched this
		// Activity.
		Intent intent = getIntent();
		radius = intent.getFloatExtra("RADIUS_EXTRA", 100);
		locName = (ArrayList<String>) intent
				.getSerializableExtra("LOCATION_NAMES");
		regid = intent.getStringExtra("REG_ID");
		task = intent.getStringExtra("TASK");
		all = intent.getStringExtra("DEREG_ALERT");
		mLocationClient = new LocationClient(this, this, this);
		mResult = (TextView) findViewById(R.id.geofence_result);
		mSubHeading = (TextView) findViewById(R.id.rereg_hint);
		// Instantiate a new geofence storage area
		mGeofenceStorage = new SimpleGeofenceStore(this);

		// Instantiate the current List of geofences
		mCurrentGeofences = new ArrayList<Geofence>();
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

	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.;
		mLocationClient.connect();
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		// mLocationClient.disconnect();
		super.onStop();
	}

	/**
	 * Set up the ActionBar if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// Return to MainActivity
		Intent myIntent = new Intent(this, MainActivity.class);
		myIntent.setClassName("com.example.bankauthenticator",
				"com.example.bankauthenticator.MainActivity");
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(myIntent);
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
				connectionResult.startResolutionForResult(this,
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

	@Override
	public void onConnected(Bundle arg0) {
		// launchToast("Location Services connected!");

		if (task.contentEquals("add")) {
			// Get the details of the current location
			mCurrentLocation = mLocationClient.getLastLocation();

			// launchToast("lat = "
			// + String.valueOf(mCurrentLocation.getLatitude())
			// + "long = "
			// + String.valueOf(mCurrentLocation.getLongitude()));

			// Use the location information to make a Geofence Object
			createGeofences(mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude(), radius, locName.get(0));
			// create a PendingIntent that is triggered when the user enters or
			// exits a Geofence.
			getTransitionPendingIntent(mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude(), radius);
			// Add the Geofences so that the Location Services can monitor them.
			mLocationClient.addGeofences(mCurrentGeofences,
					mTransitionPendingIntent, this);
		} else if (task.contentEquals("remove")) {
			// remove the Geofence from Location Services using its unique
			// location name.
			mLocationClient.removeGeofences(locName, this);
		}

	}

	@Override
	public void onDisconnected() {
	}

	/**
	 * Get the geofence parameters for each geofence from the UI and add them to
	 * a List.
	 */
	public void createGeofences(double latitude, double longitude, float rad,
			String name) {
		// Create a SimpleGeofence Object which holds all the necessary
		// information to create a Geofence Object.
		SimpleGeofence mUIGeofence1 = new SimpleGeofence(name, latitude,
				longitude, rad);
		// Store the SimpleGeofence
		mGeofenceStorage.setGeofence(name, mUIGeofence1);
		// Turn the flattened SimpleGeofence into an actual Geofence by calling
		// its toGeofence method and then add this to an ArrayList which can be
		// later sent to Location Services to be monitored.
		mCurrentGeofences.add(mUIGeofence1.toGeofence());
	}

	/**
	 * Create a PendingIntent that triggers the TransitionsReceiver class when a
	 * user triggers a Geofence event.
	 */
	private void getTransitionPendingIntent(double latitude, double longitude,
			float radius) {
		// Create an explicit Intent and add the registrationId as an Extra
		Intent intent = new Intent(
				"com.aol.android.geofence.ACTION_RECEIVE_GEOFENCE");
		intent.setClass(this, TransitionsReceiver.class);
		intent.putExtra("REG_ID", regid);

		// Wrap the Intent in a pending Intent so it can be triggered later.
		mTransitionPendingIntent = PendingIntent.getBroadcast(getBaseContext(),
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] arg1) {
		if (LocationStatusCodes.SUCCESS == statusCode) {
			Log.d("LocAct: ", "Succesful addition of Geofence");
			mResult.setText(R.string.success_add_geofence);
			mLocationClient.disconnect();
		} else {
			Log.e("LocAct:", "Error adding geofences");
			Log.e("LocAct: ", "StatusCode = " + statusCode);
			mResult.setText(R.string.fail_add_geofence);
			mLocationClient.disconnect();
		}

	}

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode,
			PendingIntent arg1) {
		if (LocationStatusCodes.SUCCESS == statusCode) {
			Log.d("LocAct: ", "Succesful removal of Geofence");
			mResult.setText(R.string.success_remove_geofence);
			mLocationClient.disconnect();
		} else {
			Log.e("LocAct:", "Error removing geofences");
			Log.e("LocAct: ", "StatusCode = " + statusCode);
			mResult.setText(R.string.fail_remove_geofence);
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode,
			String[] arg1) {
		if (LocationStatusCodes.SUCCESS == statusCode && all == null) {
			Log.d("LocAct: ", "Succesful removal of Geofence");
			mResult.setText(R.string.success_remove_geofence);
			mLocationClient.disconnect();
		} else if (LocationStatusCodes.SUCCESS == statusCode
				&& all.equalsIgnoreCase("all")) {
			Log.d("LocAct: ", "Succesful removal of all Geofences");
			mResult.setText(R.string.success_dereg);
			mSubHeading.setVisibility(View.VISIBLE);
			mLocationClient.disconnect();
		} else {
			Log.e("LocAct:", "Error removing geofences");
			Log.e("LocAct: ", "StatusCode = " + statusCode);
			mResult.setText(R.string.fail_remove_geofence);
			mLocationClient.disconnect();
		}

	}
}
