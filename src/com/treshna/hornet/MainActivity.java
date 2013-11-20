package com.treshna.hornet;

import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.treshna.hornet.R.color;


public class MainActivity extends NFCActivity {
	
	private static Tab membertab;
	private static Tab visitortab;
	private static Tab bookingtab;
	private static Context context;
	private static int selectedTab;
	private static String TAG = "MainActivity";
	private static Fragment cFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null)
	    {
			savedInstanceState.remove ("android:support:fragments");
			selectedTab = savedInstanceState.getInt("selectedTab");
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
                .setTabListener(new TabListener<MembersFindSuperFragment>(
                        this, "findmember", MembersFindSuperFragment.class));
		ab.addTab(membertab);
		
		visitortab = ab.newTab()
				.setText("Last Visitors")
				.setTabListener(new TabListener<LastVisitorsSuperFragment>(
						this, "lastvisitors", LastVisitorsSuperFragment.class));
		ab.addTab(visitortab);
		
		bookingtab = ab.newTab()
				.setText("Bookings")
				.setTabListener(new TabListener<BookingsListSuperFragment>(
						this, "bookings", BookingsListSuperFragment.class));
		ab.addTab(bookingtab);
		
		ab.setSelectedNavigationItem(selectedTab);
		
		/**the below code needs to run on app start.
         */
        Intent updateInt = new Intent(this, HornetDBService.class);
                updateInt.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
                PendingIntent pintent = PendingIntent.getService(this, 0, updateInt, PendingIntent.FLAG_UPDATE_CURRENT);
                //polling = new PollingHandler(this, pintent);
                Services.setPollingHandler(this, pintent);
                startReciever();
                /************************************/
		Log.v("MainActivity", "Finished onCreate");
	}
	
	@Override
	public void onBackPressed() {
		//this needs some minor tweaks. consider adding a queue.
		if (cFragment instanceof BookingsListSuperFragment) {
			BookingsListSuperFragment f = (BookingsListSuperFragment) cFragment;
			if (f.getCurrentFragment() instanceof BookingsOverviewFragment) {
				//do normal back pop
				Log.v(TAG, "was OverView Fragment");
				ActionBar ab = this.getSupportActionBar();
				ab.setSelectedNavigationItem(0); //just go back to the Find Member View?
			} else if (f.getCurrentFragment() instanceof BookingsResourceFragment){
				Log.v(TAG, "was Resource Fragment");
				BookingsResourceFragment resourceFragment = (BookingsResourceFragment) f.getCurrentFragment();
				if (resourceFragment.hasOverView()) {
					f.onBackPressed();
					
				} else {
					ActionBar ab = this.getSupportActionBar();
					ab.setSelectedNavigationItem(0); //just go back to the Find Member View?
				}
			} else {
				Log.v(TAG, "was Neither Fragment");
			}
		} else if (cFragment instanceof LastVisitorsSuperFragment){
			ActionBar ab = this.getSupportActionBar();
			ab.setSelectedNavigationItem(0); //just go back to the Find Member View?
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putInt("selectedTab", selectedTab);
	}
	
	public void startReciever(){
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(Services.getPollingHandler(), intentFilter);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		try {
        	unregisterReceiver(Services.getPollingHandler()); //?
        } catch (Exception e) {
        	//doesn't matter.
        }
	}
	
	@Override
	public void onPostResume() {
		Log.d(TAG, "STARTING ACTIVITY ON POST RESUME");
		super.onPostResume();
		Log.d(TAG, "FINISHING ACTIVITY ON POST RESUME");
	}
	
	@Override
	public void onResume(){
		Log.d(TAG, "STARTING ACTIVITY ON RESUME");
		super.onResume();
		Log.d(TAG, "FINISHING ACTIVITY ON RESUME");
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
	               //android.R.id.content : R.id.action_bar_activity_content;
	    			android.R.id.content : android.R.id.content;
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
                //ft.remove(mFragment);
                ft.detach(mFragment);
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
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            cFragment = mFragment;
	            ft.replace(getContentViewCompat(), mFragment, mTag);
	            //ft.addToBackStack(null);
	            selectedTab = tab.getPosition();
	        } else {
	            // If it exists, simply attach it in order to show it
	        	//ft.replace(getContentViewCompat(), mFragment, mTag);
	        	ft.attach(mFragment);
	        	//ft.addToBackStack(null);
	        	cFragment = mFragment;
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	    	mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
	    	if (mFragment != null) {
	            // Detach the fragment, because another one is being attached	    		
	    		ft.detach(mFragment);
	        } else {
	        	Log.i("TabListener", "Fragment not dettached, for tab:"+tab.getText());
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}	
}
