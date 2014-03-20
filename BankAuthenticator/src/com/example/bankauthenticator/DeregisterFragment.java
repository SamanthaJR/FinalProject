package com.example.bankauthenticator;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DeregisterFragment extends Fragment implements OnClickListener {
	
	String regid, usernm, pass;
	EditText mDeregUsername, mDeregPass;
	Button mSubmitButn;
	int regLen;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		
		View fragView = inflater.inflate(R.layout.deregister_fragment_view, container, false);
		
		mDeregUsername = (EditText) fragView.findViewById(R.id.deregusername);
		mDeregPass = (EditText) fragView.findViewById(R.id.deregpassword);
		mSubmitButn = (Button) fragView.findViewById(R.id.dereg_submit_button);
		
		mSubmitButn.setOnClickListener(this);
		
		Bundle args = getArguments();
		regid = args.getString("REG_ID");
		
		return fragView;
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
			AppClient appClient = new AppClient(getActivity().getApplicationContext(), message[0], regLen,
					regid, usernm, pass, "", "", "", "");
			appClient.run();

			return null;
		}

	}

	@Override
	public void onClick(View v) {
		deregSubmitClick(v);
		
	}


}
