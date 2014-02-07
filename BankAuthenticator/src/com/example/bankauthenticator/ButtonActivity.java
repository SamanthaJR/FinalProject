/**
 * Activity class that is started when the user attempts to login. It
 * displays the login query and provides two buttons, accept or decline.
 */
package com.example.bankauthenticator;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ButtonActivity extends Activity {
	
	public final static String USER_RESPONSE = "com.example.bankauthenticator.RESPONSE";

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
				GcmBroadcastReceiver.mAppClient.sendMessage("Accepted");
				startSuccessAct("accept");
			}

		});

		dec.setVisibility(0);
		dec.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startSuccessAct("decline");
				GcmBroadcastReceiver.mAppClient.sendMessage("Declined");
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.button, menu);
		return true;
	}

	/**
	 * Method that starts a new activity to alert the user that they have
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
		myIntent.putExtra(USER_RESPONSE, response);
		startActivity(myIntent);
	}

}
