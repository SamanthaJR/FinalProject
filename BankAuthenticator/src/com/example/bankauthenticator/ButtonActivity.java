package com.example.bankauthenticator;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ButtonActivity extends Activity {
	
	

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
				startMainAct();
			}

		});

		dec.setVisibility(0);
		dec.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startMainAct();
				GcmBroadcastReceiver.mAppClient.sendMessage("Declined");
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.button, menu);
		return true;
	}

	private void startMainAct() {
		Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.setClassName("com.example.bankauthenticator", "com.example.bankauthenticator.MainActivity");
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
	}
	
}
