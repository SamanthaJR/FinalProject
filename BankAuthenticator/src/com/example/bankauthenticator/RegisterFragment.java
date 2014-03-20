package com.example.bankauthenticator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterFragment extends Fragment implements OnClickListener{
	
	Button mSubmitBtn;
	EditText mUsername, mPassword, mConfPass;
	String usernm, pass, regid;
	int regLen;

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		
		Log.d("REG_FRAG", "On create called");
		
		View fragView = inflater.inflate(R.layout.register_fragment_view, container, false);
		
		Bundle args = getArguments();
		regid = args.getString("REG_ID");
		

		mUsername = (EditText) fragView.findViewById(R.id.username);
		mPassword = (EditText) fragView.findViewById(R.id.password);
		mConfPass = (EditText) fragView.findViewById(R.id.confirm_password);
		mSubmitBtn = (Button) fragView.findViewById(R.id.submit_button);
		
		mSubmitBtn.setOnClickListener(this);
		
        // Inflate the layout for this fragment
        return fragView;
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
	public void submitFragClick(View view) {

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

	/**
	 * Method launches a toast object.
	 * 
	 * @param toastMess
	 *            - the message to be displayed in the toast.
	 */
	public void launchToast(String toastMess) {
		Context context = getActivity().getApplicationContext();
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
			AppClient appclient = new AppClient(getActivity().getApplicationContext(), message[0], regLen,
					regid, usernm, pass, "", "", "", "");
			appclient.run();

			return null;
		}

	}

	@Override
	public void onClick(View v) {
		submitFragClick(v);
	}
	
}
