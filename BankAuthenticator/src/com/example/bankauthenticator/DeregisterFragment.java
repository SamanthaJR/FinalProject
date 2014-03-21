/**
 * Class is a child class of the android Fragment that displays a layout that allows the 
 * user to request that the program removes all registration details for their device
 */
package com.example.bankauthenticator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class DeregisterFragment extends Fragment implements OnClickListener {
	
	private String mRegid, mUsernm, mPass;
	private EditText mDeregUsername, mDeregPass;
	private Button mSubmitButn;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		
		View fragView = inflater.inflate(R.layout.deregister_fragment_view, container, false);
		
		mDeregUsername = (EditText) fragView.findViewById(R.id.deregusername);
		mDeregPass = (EditText) fragView.findViewById(R.id.deregpassword);
		mSubmitButn = (Button) fragView.findViewById(R.id.dereg_submit_button);
		
		mSubmitButn.setOnClickListener(this);
		
		Bundle args = getArguments();
		mRegid = args.getString("REG_ID");
		
		return fragView;
	}
	
	/**
	 * Customised onClick method for the submit button. It creates an ConnectTask object,
	 * gives it all the correct information for a de-reigster action, and tells it to run.
	 * @param view
	 */
	public void deregSubmitClick(View view) {

		mUsernm = mDeregUsername.getText().toString();
		mPass = mDeregPass.getText().toString();

		if (mUsernm.length() == 0 || mPass.length() == 0 ) {
			
			AlertUserFragment aluf = new AlertUserFragment();
			Bundle args = new Bundle();
			args.putString("MESSAGE", "Please ensure all fields are filled out.");
			aluf.setArguments(args);
			aluf.show(getActivity().getSupportFragmentManager(), "Fill out de-reg");
			
		} else {

				ConnectTask connt = new ConnectTask(getActivity(), "de-register", 0,
						mRegid, mUsernm, mPass, "", "", "", "");
				
				connt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
		}
	}

	@Override
	public void onClick(View v) {
		deregSubmitClick(v);
		
	}


}
