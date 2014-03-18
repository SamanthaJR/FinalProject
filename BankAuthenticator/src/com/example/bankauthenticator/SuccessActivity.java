package com.example.bankauthenticator;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class SuccessActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_success);
		Intent intent = getIntent();
		String message = intent.getStringExtra(ButtonActivity.USER_RESPONSE);
		TextView t = (TextView) findViewById(R.id.success_text);
		TextView h = (TextView) findViewById(R.id.success_heading_text);
		if (message.equalsIgnoreCase("accept")) {
			t.setText("You have successfully allowed the login! Please return to your browser.");
			h.setText(R.string.login_accepted);
		} else {
			t.setText("You have successfully declined the login. " +
					  "Please contact your bank for more information on keeping your account secure.");
			h.setText(R.string.login_declined);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.success, menu);
		return true;
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

}
