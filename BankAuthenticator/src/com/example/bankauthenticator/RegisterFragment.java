/**
 * Class is a child class of the android Fragment that displays a layout that allows the 
 * user to register their device
 */
package com.example.bankauthenticator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterFragment extends Fragment implements OnClickListener{
	
	private Button mSubmitBtn;
	private EditText mUsername, mPassword, mConfPass;
	public String nUsernm, mPass, mRegid;
	private int mRegLen;

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		
		Log.d("REG_FRAG", "On create called");
		
		View fragView = inflater.inflate(R.layout.register_fragment_view, container, false);
		
		Bundle args = getArguments();
		mRegid = args.getString("REG_ID");
		

		mUsername = (EditText) fragView.findViewById(R.id.username);
		mPassword = (EditText) fragView.findViewById(R.id.password);
		mConfPass = (EditText) fragView.findViewById(R.id.confirm_password);
		mSubmitBtn = (Button) fragView.findViewById(R.id.submit_button);
		
		mSubmitBtn.setOnClickListener(this);
		
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

		nUsernm = mUsername.getText().toString();
		mPass = mPassword.getText().toString();
		String cPass = mConfPass.getText().toString();

		if (nUsernm.length() == 0 || mPass.length() == 0 || cPass.length() == 0) {
			// Tell the user that all EditText fields need to be filled out.
			AlertUserFragment aluf = new AlertUserFragment();
			Bundle args = new Bundle();
			args.putString("MESSAGE", "Please ensure all fields are filled out.");
			aluf.setArguments(args);
			aluf.show(getActivity().getSupportFragmentManager(), "Fill out");
		} else {

			if (!mPass.equals(cPass)) {
				// If the user has entered two different values for their password,
				// prompt re-entry of information.
				AlertUserFragment aluf = new AlertUserFragment();
				Bundle args = new Bundle();
				args.putString("MESSAGE", "Make sure you have typed your password correctly!");
				aluf.setArguments(args);
				aluf.show(getActivity().getSupportFragmentManager(), "Fill out");
				// Clear EditText fields.
				mPassword.setText("");
				mConfPass.setText("");
			} else {
				// calculate the lengths of the values in the EditText fields.
				int usernmLen = nUsernm.length();
				int passLen = mPass.length();
				mRegLen = 183 + usernmLen + passLen + 2;

				ConnectTask connt = new ConnectTask(getActivity(), "registering", mRegLen,
						mRegid, nUsernm, mPass, "", "", "", "");
				
				connt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
			}
		}
	}
	@Override
	public void onClick(View v) {
		submitFragClick(v);
	}
	
}
