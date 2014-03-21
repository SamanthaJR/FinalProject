package com.example.bankauthenticator;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class RemoveLocationFragment extends Fragment implements OnClickListener{

	String usernm, pass, regid, radius, name;
	Button mSubmitLcn;
	EditText mUsername, mPassword, mRadius, mName, mRemovUser, mRemovPass, mRemovLocName;
	private int locLen;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	List<Geofence> mCurrentGeofences;
    PendingIntent mTransitionPendingIntent;
    IntentFilter mIntentFilter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	
    	Bundle args = getArguments();
		regid = args.getString("REG_ID");
    	
    	View fragView = inflater.inflate(R.layout.remove_location_fragment_view, container, false);
    	
    	mRemovUser = (EditText) fragView.findViewById(R.id.remove_location_username);
		mRemovPass = (EditText) fragView.findViewById(R.id.remove_location_password);
		mRemovLocName = (EditText) fragView.findViewById(R.id.remove_location_name);
		mSubmitLcn = (Button) fragView.findViewById(R.id.remove_location_submit_button);
		
		mSubmitLcn.setOnClickListener(this);

//         Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
    	
    	return fragView;
    }
    
    public void submitRemoveLocationClick(View view) {
		usernm = mRemovUser.getText().toString();
		pass = mRemovPass.getText().toString();
		name = mRemovLocName.getText().toString();
		
		radius = "0";
		locLen = 0;
		
		int usernmLen = usernm.length();
		int passLen = pass.length();
		int nameLen = name.length();
		
		if (usernmLen == 0 || passLen == 0 || nameLen == 0) {
			launchToast("Please ensure all fields have been filled out");
		} else {
			// Tell ApHS to remove Geofence data from database			
			ConnectTask connt = new ConnectTask(getActivity(), "delLocation", locLen,
					regid, usernm, pass, radius, name, "", "");
			
			connt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
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

	@Override
	public void onClick(View v) {
		submitRemoveLocationClick(v);
	}
	
}
