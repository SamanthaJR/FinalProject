package com.example.bankauthenticator;

/**
 * Main Activity class for the app which is launched on app start.
 */
import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bankauthenticator.GcmBroadcastReceiver.connectTask;
import com.google.android.gms.common.*;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

	String SENDER_ID = "21030741354";
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
	public static String loginAccepted;
	static final String TAG = "BankAuthenticator";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		context = getApplicationContext();
		regid = getRegistrationId(context);
		System.out.println(regid);
		TextView dispID = (TextView) findViewById(R.id.display);
		dispID.setText(regid);

		if (checkPlay()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			Context context = getApplicationContext();
			String registrationID = getRegistrationId(context);

			if (registrationID.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i("BankAuthenticator",
					"No valid Google Play Services APK found.");
		}
	}

	/**
	 * Gets the registration ID of the Device
	 * 
	 * @param applicationContext
	 * @return the ID or an empty String if registration Id is not present.
	 */
	private String getRegistrationId(Context applicationContext) {

		final SharedPreferences prefs = getGCMPreferences(applicationContext);
		String regId = prefs.getString(PROPERTY_REG_ID, "");
		if (regId.length() == 0) {
			Log.v("BankAuthenticator", "Registration not found.");
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(applicationContext);
		if (registeredVersion != currentVersion || isRegistrationExpired()) {
			Log.v("BankAuthenticator",
					"App version changed or registration expired.");
			return "";
		}
		return regId;
	}

	/**
	 * Registers to the GCM servers in an AsyncTask if the device is not already
	 * registered.
	 */
	private void registerInBackground() {
		new AsyncTask() {
			@Override
			protected String doInBackground(Object... params) { // NOTE this
																// line is
																// different
																// from tutorial
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration id=" + regid;

					// Save the regid - no need to register again.
					setRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			// NOTE this line is different from tutorial
			protected void onPostExecute(String msg) {
				mDisplay.append(msg + "\n");
			}
		}.execute(null, null, null);
	}

	/**
	 * Gets the GCM preferences.
	 * 
	 * @param context
	 * @return
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// guide suggests this should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private boolean isRegistrationExpired() {
		final SharedPreferences prefs = getGCMPreferences(context);
		// checks if the information is not stale
		long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME,
				-1);
		return System.currentTimeMillis() > expirationTime;
	}

	private void setRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.v("BankAuthenticator", "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		long expirationTime = System.currentTimeMillis()
				+ REGISTRATION_EXPIRY_TIME_MS;

		Log.v("BankAuthenticator", "Setting registration expiry time to "
				+ new Timestamp(expirationTime));
		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * Method that ensures the device always has the most up-to-date Google Play
	 * Services APK installed includes dialogue box to prompt user to update if
	 * necessary.
	 * 
	 * @return boolean true if play services available and up to date. False
	 *         otherwise.
	 */
	private boolean checkPlay() {
		int temp = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (temp != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(temp)) {
				GooglePlayServicesUtil.getErrorDialog(temp, this, 1).show();

			} else {
				Log.i("BankAuthenticator", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	public void registerClick(View view){
		Intent myIntent = new Intent(this, LaunchActivity.class);
		myIntent.setClassName("com.example.bankauthenticator",
				"com.example.bankauthenticator.LaunchActivity");
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		myIntent.putExtra("USER_REGID", regid);
		startActivity(myIntent);
	}
	
}
