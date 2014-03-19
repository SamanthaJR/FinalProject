package com.example.bankauthenticator;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LaunchActivity extends Activity {

	Button mSubmitBtn;
	EditText mUsername, mPassword, mConfPass, mDeregUsername, mDeregPass;
	private AppClient mAppClient;
	String usernm, pass, regid;
	int regLen;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		
		mUsername = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mConfPass = (EditText) findViewById(R.id.confirm_password);
		mSubmitBtn = (Button) findViewById(R.id.submit_button);
		
		mDeregUsername = (EditText) findViewById(R.id.deregusername);
		mDeregPass = (EditText) findViewById(R.id.deregpassword);
		
		Intent intent = getIntent();
		regid = intent.getStringExtra("USER_REGID");

		getActionBar().setDisplayHomeAsUpEnabled(true);

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.launch, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
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
	public void submitClick(View view) {

		usernm = mUsername.getText().toString();
		pass = mPassword.getText().toString();
		String cPass = mConfPass.getText().toString();

		if (usernm.length() == 0 || pass.length() == 0 || cPass.length() == 0) {
			launchToast("Please ensure all fields are filled out.");
		} else {

			if (!pass.equals(cPass)) {

				launchToast("Make sure you have typed your password correctly!");

				mPassword.setText("");
				mConfPass.setText("");
			} else {
				// calc lengths of vals from text zones
				int usernmLen = usernm.length();
				int passLen = pass.length();
				regLen = 183 + usernmLen + passLen + 2;

				new connectRegTask().execute("registering");
			}
		}
	}
	
	public void deregSubmitClick(View view) {

		usernm = mDeregUsername.getText().toString();
		pass = mDeregPass.getText().toString();

		if (usernm.length() == 0 || pass.length() == 0 ) {
			launchToast("Please ensure all fields are filled out.");
		} else {

			
				// calc lengths of vals from text zones
				int usernmLen = usernm.length();
				int passLen = pass.length();
				regLen = 183 + usernmLen + passLen + 2;
				
				new connectRegTask().execute("de-register");
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
	public class connectRegTask extends AsyncTask<String, String, AppClient> {

		@Override
		protected AppClient doInBackground(String... message) {

			// we create a TCPClient object and pass to it all the data it
			// needs.
			mAppClient = new AppClient(getApplicationContext(), message[0], regLen,
					regid, usernm, pass, "", "", "", "");
			mAppClient.run();

			return null;
		}

	}

}
