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
		if (message.equalsIgnoreCase("accept")) {
			t.setText("You have successfully allowed the login! Please return to your browser.");
		} else {
			t.setText("You have successfully declined the login. " +
					  "Please contact your bank for more information on keeping your account secure.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.success, menu);
		return true;
	}

}
