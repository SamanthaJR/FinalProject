/**
 * Class dedicated to receiving push notifications from the GCM servers.
 */
package com.example.bankauthenticator;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = "BankAuthenticator";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context context;
	public static AppClient mAppClient;
	Button acc;
	Button dec;
	static String loginAccepted = "Wait";
	String regid;

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("BroadcastReceiver", "onReceive method called");

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		this.context = context;
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			sendNotification("Send error: " + intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
				.equals(messageType)) {
			sendNotification("Deleted messages on server: "
					+ intent.getExtras().toString());
		} else {

			String temp = intent.getExtras().toString();
			String[] locateRegid = temp.split("regid=");
			regid = locateRegid[1].substring(0, 183); // 183 or 182?

			new connectTask().execute("");

			sendNotification("Please Accept or Decline attempt.");

			Intent myIntent = new Intent(context.getApplicationContext(),
					ButtonActivity.class);
			myIntent.setClassName("com.example.bankauthenticator",
					"com.example.bankauthenticator.ButtonActivity");
			myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(myIntent);

		}
		setResultCode(Activity.RESULT_OK);
	}

	/**
	 * Method called after the GCM message received which creates and posts a
	 * notification.
	 * 
	 * @param msg
	 *            - the message we wish to post in the notification.
	 */
	public void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_menu_securelock)
				.setContentTitle("New Log In Attempt").setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	/**
	 * AsyncTask class that creates a new AppClient on a new thread and sets it
	 * running to make the connection to the server. It initiates the object
	 * with the values required to send the correct messages for login.
	 * 
	 * @author sjr090
	 * 
	 */
	public class connectTask extends AsyncTask<String, String, AppClient> {

		@Override
		protected AppClient doInBackground(String... message) {
			mAppClient = new AppClient(context, "authenticating", 0, regid, "", "", "", "", "", "");
			mAppClient.run();

			return null;
		}
	}
}