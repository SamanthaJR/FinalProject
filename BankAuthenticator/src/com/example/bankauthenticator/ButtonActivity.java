package com.example.bankauthenticator;

import android.os.Bundle;
import android.app.Activity;
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
				GcmBroadcastReceiver.mAppClient.sendMessage("Accepted");;
			}
		});

		dec.setVisibility(0);
		dec.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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

}
