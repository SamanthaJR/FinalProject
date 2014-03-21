/**
 * Activity class that is started when the user attempts to login. It
 * displays the login query and provides two buttons, accept or decline.
 */
package com.example.bankauthenticator;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ButtonActivity extends Activity {

	private final int delayTime = 60000;
	private Handler timeoutHandler = new Handler();

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_button);
		GcmBroadcastReceiver.loginAccepted = "wait";

		Button acc = (Button) findViewById(R.id.yes_button);
		Button dec = (Button) findViewById(R.id.no_button);

		acc.setVisibility(0);
		acc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Stop the timeout as the user has responded
				timeoutHandler.removeCallbacks(timeoutTask);
				// Signal the server that the user has accepted the login. This
				// must be done off the main UI Thread.
				SendTask st = new SendTask();
				st.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Accepted");
				startSuccessAct("accept");
			}

		});

		dec.setVisibility(0);
		dec.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Stop the timeout as the user has responded
				timeoutHandler.removeCallbacks(timeoutTask);
				// Signal the server that the user has declined the login. This
				// must be done off the main UI Thread.
				SendTask st = new SendTask();
				st.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Declined");
				startSuccessAct("decline");
			}
		});

	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// start the timeout feature
		timeoutHandler.postDelayed(timeoutTask, delayTime);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.button, menu);
		return true;
	}

	/**
	 * Method that starts a new SuccessActivity to alert the user that they have
	 * successfully input their response to the login.
	 * 
	 * @param response
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void startSuccessAct(String response) {
		Intent myIntent = new Intent(this, SuccessActivity.class);
		myIntent.setClassName("com.example.bankauthenticator",
				"com.example.bankauthenticator.SuccessActivity");
		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		myIntent.putExtra("USER_RESPONSE", response);
		startActivity(myIntent);
	}

	/**
	 * An Asynchronous Task that sends a message to the AppHomeServer containing
	 * the user's response to the login attempt.
	 * 
	 * @author sjr090
	 * 
	 */
	private class SendTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			Log.d("BAsync", "Executing message send");
			GcmBroadcastReceiver.mAppClient.sendMessage(params[0]);
			return null;
		}

	}

	/**
	 * The timer that runs in the background in order for the Activity to
	 * implement a Timeout in case the user abandons the login attempt.
	 */
	private Runnable timeoutTask = new Runnable() {
		public void run() {
			finish();
			startSuccessAct("Timeout");
		}
	};

}
