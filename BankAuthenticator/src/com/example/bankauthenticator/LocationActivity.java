package com.example.bankauthenticator;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import android.location.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;

public class LocationActivity extends Activity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

	String usernm, pass, regid, radius, name;
	Button mSubmitLcn;
	EditText mUsername, mPassword, mRadius, mName;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		// Show the Up button in the action bar.
		setupActionBar();
		Intent intent = getIntent();
		regid = intent.getStringExtra("USER_REGID");
		mLocationClient = new LocationClient(this, this, this);
		
		mUsername = (EditText) findViewById(R.id.location_username);
		mPassword = (EditText) findViewById(R.id.location_password);
		mRadius = (EditText) findViewById(R.id.location_radius);
		mName = (EditText) findViewById(R.id.location_name);
		
		// Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);

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
//    	mLocationClient.disconnect();
        super.onStop();
    }

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
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
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	  public void onBackPressed(){
		Intent myIntent = new Intent(this, MainActivity.class);
		myIntent.setClassName("com.example.bankauthenticator",
				"com.example.bankauthenticator.MainActivity");
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(myIntent);
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
					AsyncTask.THREAD_POOL_EXECUTOR, "");        

//			createGeofences(mCurrentLocation.getLatitude(),
//					mCurrentLocation.getLongitude(), Float.parseFloat(radius),
//					name);

////			launchToast("lat = "
//					+ String.valueOf(mCurrentLocation.getLatitude())
//					+ "long = "
//					+ String.valueOf(mCurrentLocation.getLongitude()));
			
			Intent nt = new Intent(this, GeoSetterActivity.class);
			nt.setClassName("com.example.bankauthenticator",
					"com.example.bankauthenticator.GeoSetterActivity");
			nt.putExtra("RADIUS_EXTRA", Float.parseFloat(radius));
			nt.putExtra("LOCATION_NAME", name);
			nt.putExtra("REG_ID", regid);
			nt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(nt);
//			
//			createGeofences(mCurrentLocation.getLatitude(),
//					mCurrentLocation.getLongitude(), 2000, "onConnect");
//			getTransitionPendingIntent(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 2000);
//			mLocationClient.addGeofences(mCurrentGeofences, mTransitionPendingIntent, this);
//			
//			getTransitionPendingIntent(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), Float.parseFloat(radius));
//			mLocationClient.addGeofences(mCurrentGeofences, mTransitionPendingIntent, this);
		}

	}

	/**
	 * Method launches a toast object.
	 * 
	 * @param toastMess
	 *            - the message to be displayed in the toast.
	 */
	public void launchToast(String toastMess) {
		Context context = getApplicationContext();
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
			mAppClient = new AppClient(getBaseContext(), "addlocation", locLen,
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
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
//		createGeofences(mCurrentLocation.getLatitude(),
//				mCurrentLocation.getLongitude(), 2000, "onConnect");
//		getTransitionPendingIntent(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 2000);
//		mLocationClient.addGeofences(mCurrentGeofences, mTransitionPendingIntent, this);
		
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Location Services disconnected.",
                Toast.LENGTH_SHORT).show();		
//		mLocationClient = null;
	}
	
	/**
//     * Get the geofence parameters for each geofence from the UI
//     * and add them to a List.
//     */
//    public void createGeofences(double latitude, double longitude, float rad, String name) {
//        /*
//         * Create an internal object to store the data. Set its
//         * ID to "1". This is a "flattened" object that contains
//         * a set of strings
//         */
//        SimpleGeofence mUIGeofence1 = new SimpleGeofence(name,latitude, longitude, rad);
//        // Store this flat version
//        mGeofenceStorage.setGeofence(name, mUIGeofence1);
//        mCurrentGeofences.add(mUIGeofence1.toGeofence());
//    }
//    
//    /*
//     * Create a PendingIntent that triggers an IntentService in your
//     * app when a geofence transition occurs.
//     */
//    private void getTransitionPendingIntent(double latitude, double longitude, float radius) {
//        // Create an explicit Intent
//        Intent intent = new Intent("com.aol.android.geofence.ACTION_RECEIVE_GEOFENCE");
//        intent.setClass(this, TransitionsReceiver.class);
//        intent.putExtra("REG_ID", regid);
//        
////        this.startService(intent);
//        
//        mTransitionPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        
////        mLocationManager.addProximityAlert(latitude, longitude, radius, -1, mTransitionPendingIntent);
//    }
//
//	@Override
//	public void onAddGeofencesResult(int statusCode, String[] arg1) {
//		if (LocationStatusCodes.SUCCESS == statusCode) {
//            Log.d("LocAct: ", "Succesful addition of Geofence");
//            launchToast("Successful addition of Geofence");
//            mLocationClient.disconnect();
//        } else {
//        	Log.e("LocAct:", "Error adding geofences");
//        	Log.e("LocAct: ", "StatusCode = " + statusCode);
//        	mLocationClient.disconnect();
//        }
//
//	}
}
