/**
 * Activity class that controls the Register and Deregister functionality by loading two fragments.
 */
package com.example.bankauthenticator;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class LaunchActivity extends FragmentActivity {

	public String regid;
	private RegisterFragmentPagerAdapter mRegisterFragmentPagerAdapter;
	private ViewPager mViewPager;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		
		// Add tabs to the Actionbar
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Add the swipe functionality
        mRegisterFragmentPagerAdapter =
                new RegisterFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.register_pager);
        mViewPager.setAdapter(mRegisterFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
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
	                        .setText("Register")
	                        .setTabListener(tabListener));
	        
	        actionBar.addTab(
	                actionBar.newTab()
	                        .setText("De-register")
	                        .setTabListener(tabListener));
		
		
		
		Intent intent = getIntent();
		regid = intent.getStringExtra("USER_REGID");

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	

	/**
	 * FragmentPagerAdapter class that loads a RegisterFragment in the first tab
	 * and a DeregisterFragment in the second tab.
	 * 
	 * @author sjr090
	 *
	 */
	public class RegisterFragmentPagerAdapter extends FragmentPagerAdapter {
	    public RegisterFragmentPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int i) {
	    	Fragment fragment;
	    	if(i == 0){
	    		// return RegisterFragment.
	    		fragment = new RegisterFragment();
	        	Bundle args = new Bundle();
	        	args.putString("REG_ID", regid);
	        	fragment.setArguments(args);
	    	} else {
	    		// return DeregisterFragment.
	    		fragment = new DeregisterFragment();
		        Bundle args = new Bundle();
		        args.putString("REG_ID", regid);
		        fragment.setArguments(args);	        
	    	}
	    	return fragment;
	    }

	    // Number of tabs
	    @Override
	    public int getCount() {
	        return 2;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	    	if(position == 0){
	    		return "Register";
	    	} else {
	    		return "De-Register";
	    	}
	    }
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
	
	// Return to the MainActivity.
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
