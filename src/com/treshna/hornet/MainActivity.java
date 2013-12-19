package com.treshna.hornet;

import java.util.Date;

import com.treshna.hornet.R.color;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends NFCActivity implements BookingsListFragment.OnCalChangeListener{
	
	private static Tab membertab;
	private static Tab visitortab;
	private static Tab bookingtab;
	private static Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null)
	    {
			savedInstanceState.remove ("android:support:fragments");
	    }
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);
		this.setTitle("GymMaster");
		this.setTitleColor(color.gym);
		context = getApplicationContext();
		
		
		ActionBar ab = getSupportActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ab.setDisplayShowTitleEnabled(false);
		
		membertab = ab.newTab()
                .setText("Find Member")
                .setTabListener(new TabListener<MemberFindFragment>(
                        this, "findmember", MemberFindFragment.class));
		ab.addTab(membertab);
		
		visitortab = ab.newTab()
				.setText("Last Visitors")
				.setTabListener(new TabListener<LastVisitorsFragment>(
						this, "lastvisitors", LastVisitorsFragment.class));
		ab.addTab(visitortab);
		/**
		 * TODO: refactor bookings so that they work from a tab.
		 *  (too hard basket atm).
		bookingtab = ab.newTab()
				.setText("Bookings")
				.setTabListener(new TabListener<BookingsSlideFragment>(
						this, "bookings", BookingsSlideFragment.class));
		ab.addTab(bookingtab);*/
		
		Log.v("MainActivity", "Finished onCreate");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
	    case (R.id.action_createclass):{
	    	Intent i = new Intent(this, ClassCreate.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_settings):
	    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	return true;
	    case (R.id.action_update): {
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
		 		Services.setPreference(this, "sync_frequency", "5");
		 	}
		 	PollingHandler polling = Services.getPollingHandler();
	    	polling.startService();
	    	return true;
	    }
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    }
	    case (R.id.action_bookings):{
	    	Intent bookings = new Intent(this, HornetDBService.class);
			bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
		 	this.startService(bookings);
	    	
		 	Intent intent = new Intent(this, BookingsSlidePager.class);
	       	startActivity(intent);
	       	return true;
	    }
	    case (R.id.action_addMember):{
	    	Intent intent = new Intent(this, MemberAdd.class);
	    	startActivity(intent);
	    	return true;
	    }
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	public static int getContentViewCompat() {
	    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
	               android.R.id.content : R.id.action_bar_activity_content;
	}
	
	public static Context getContext(){
		return context;
	}
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final ActionBarActivity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(ActionBarActivity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	        Log.i("TabListener", "Creating Fragment:"+mTag);
	        mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
            	Log.d("TabListener", "Fragment already existsed and was visible.");
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.remove(mFragment);
                ft.commit();
                mFragment = null;
            }
	        
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	    	 mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
	         if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	        	//mActivity.getSupportFragmentManager().executePendingTransactions();
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            Log.i("TabListener", "Adding Fragment:"+mTag);
	          
	            ft.replace(getContentViewCompat(), mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	        	//mActivity.getSupportFragmentManager().executePendingTransactions();
	        	Log.i("TabListener", "Attaching Fragment:"+mTag);
	        	ft.replace(getContentViewCompat(), mFragment, mTag);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	    	mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
	    	if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	    		//mActivity.getSupportFragmentManager().executePendingTransactions();
	    		Log.i("TabListener", "Detaching Fragment:"+mTag);
	    		ft.detach(mFragment);
	        } else {
	        	Log.i("TabListener", "Fragment not dettached, for tab:"+tab.getText());
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}

	@Override
	public void onDateChange(Date date) {
				
	}
		
}
