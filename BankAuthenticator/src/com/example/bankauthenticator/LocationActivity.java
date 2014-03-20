package com.example.bankauthenticator;

import java.util.ArrayList;
import java.util.List;

import com.example.bankauthenticator.LaunchActivity.DemoCollectionPagerAdapter;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import android.location.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ActionBar.Tab;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;

public class LocationActivity extends FragmentActivity {

	String usernm, pass, regid, radius, name;
	Button mSubmitLcn;
	EditText mUsername, mPassword, mRadius, mName, mRemovUser, mRemovPass, mRemovLocName;
	private int locLen;
	private AppClient mAppClient;
	private Location mCurrentLocation;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public LocationClient mLocationClient;
	List<Geofence> mCurrentGeofences;
    private SimpleGeofenceStore mGeofenceStorage;
    PendingIntent mTransitionPendingIntent;
    IntentFilter mIntentFilter;
	DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
	ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		Intent intent = getIntent();
		regid = intent.getStringExtra("USER_REGID");
		
		// ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.location_pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

		
		
		
		

		
		
		 // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
	    	
			@Override
			public void onTabReselected(Tab arg0,
					android.app.FragmentTransaction arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTabSelected(Tab tab,
					android.app.FragmentTransaction arg1) {
				// When the tab is selected, switch to the
	            // corresponding page in the ViewPager.
				int t = tab.getPosition();
	            mViewPager.setCurrentItem(t);
			}

			@Override
			public void onTabUnselected(Tab arg0,
					android.app.FragmentTransaction arg1) {
				// TODO Auto-generated method stub
				
			}
	    };

	 
	        actionBar.addTab(
	                actionBar.newTab()
	                        .setText("Add Current Location")
	                        .setTabListener(tabListener));
	        
	        actionBar.addTab(
	                actionBar.newTab()
	                        .setText("Remove Location")
	                        .setTabListener(tabListener));
	  

		getActionBar().setDisplayHomeAsUpEnabled(true);

		
	}
	

	public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {
	    public DemoCollectionPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int i) {
	    	Fragment fragment;
	    	if(i == 0){
	    		fragment = new AddLocationFragment();
	        	Bundle args = new Bundle();
	        	args.putString("REG_ID", regid);
	        	fragment.setArguments(args);
	    	} else {
	    		fragment = new RemoveLocationFragment();
		        Bundle args = new Bundle();
		        args.putString("REG_ID", regid);
		        fragment.setArguments(args);	        
	    	}
	    	return fragment;
	    }

	    @Override
	    public int getCount() {
	        return 2;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	    	if(position == 0){
	    		return "Add Location";
	    	} else {
	    		return "Remove Location";
	    	}
	    }
	}

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
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


	
	
	
}
