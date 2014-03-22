package com.example.bankauthenticator;

import java.util.List;
import com.google.android.gms.location.*;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.app.ActionBar.Tab;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public class LocationActivity extends FragmentActivity {

	String usernm, pass, regid, radius, name;
	Button mSubmitLcn;
	EditText mUsername, mPassword, mRadius, mName, mRemovUser, mRemovPass, mRemovLocName;
	public LocationClient mLocationClient;
	List<Geofence> mCurrentGeofences;
    PendingIntent mTransitionPendingIntent;
    IntentFilter mIntentFilter;
	LocationFragmentPagerAdapter mLocationPagerAdapter;
	ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		// Enable the Actionbar to load tabs
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		Intent intent = getIntent();
		regid = intent.getStringExtra("USER_REGID");
		
		// Enable the swipe functionality
        mLocationPagerAdapter =
                new LocationFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.location_pager);
        mViewPager.setAdapter(mLocationPagerAdapter);
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

	/**
	 * FragmentPagerAdapter class that loads an AddLocationFragment in the first tab
	 * and a RemoveLocationFragment in the second tab.
	 * @author sjr090
	 *
	 */
	public class LocationFragmentPagerAdapter extends FragmentPagerAdapter {
	    public LocationFragmentPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int i) {
	    	Fragment fragment;
	    	if(i == 0){
	    		// Return an AddLocationFragment
	    		fragment = new AddLocationFragment();
	        	Bundle args = new Bundle();
	        	args.putString("REG_ID", regid);
	        	fragment.setArguments(args);
	    	} else {
	    		// Return a RemoveLocationFragment
	    		fragment = new RemoveLocationFragment();
		        Bundle args = new Bundle();
		        args.putString("REG_ID", regid);
		        fragment.setArguments(args);	        
	    	}
	    	return fragment;
	    }

	    // There are 2 tabs.
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Return to MainActivity
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
