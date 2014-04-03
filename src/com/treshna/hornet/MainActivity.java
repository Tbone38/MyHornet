package com.treshna.hornet;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.treshna.hornet.R.color;
import com.treshna.hornet.navigation.NavDrawerItem;
import com.treshna.hornet.navigation.NavDrawerListAdapter;
import com.treshna.hornet.navigation.SlideMenuClickListener;
import com.treshna.hornet.navigation.TabListener;


public class MainActivity extends NFCActivity {
	
	private static Tab membertab;
	private static Tab visitortab;
	private static Tab bookingtab;
	private static Context context;
	private static int selectedTab;
	private Fragment cFragment;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
 
    // nav drawer title
    private CharSequence mDrawerTitle;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter navadapter;
    private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null)
	    {
			savedInstanceState.remove ("android:support:fragments");
			selectedTab = savedInstanceState.getInt("selectedTab");
			//savedInstanceState.clear();
	    }
		
		super.onCreate(savedInstanceState);

		//this.setContentView(R.layout.main_activity);
		this.setContentView(R.layout.drawer_layout);
		
		this.setTitle("GymMaster");
		this.setTitleColor(color.gym);
		context = getApplicationContext();
		
		mDrawerTitle = "GymMaster";
		 
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_main);
 
        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.slider_menu);
 
        navDrawerItems = new ArrayList<NavDrawerItem>();
        //Header
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true));
        // Find Member
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, "50+"));
        // Last Visitors
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, "50+"));
        // Bookings
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "22"));
        // Header
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1), true));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        
        //header
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1), true));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
        
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(9, -1), true));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[10], navMenuIcons.getResourceId(10, -1)));
 
        // Recycle the typed array
        navMenuIcons.recycle();
 
        // setting the nav drawer list adapter
        navadapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(navadapter);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_action_add_to_queue, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            @SuppressLint("NewApi")
			public void onDrawerClosed(View view) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }
 
            @SuppressLint("NewApi")
			public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener(this.getSupportFragmentManager(), mDrawerLayout, mDrawerList, this));
        
        if (savedInstanceState == null) { //needs to be done after the super.OnCreate call.
        	try {
        		FragmentManager fm = this.getSupportFragmentManager();
        		genTabs();
        		cFragment = fm.findFragmentByTag("findmember");
            	addTabs();
            	ActionBar ab = this.getSupportActionBar();
                ab.setSelectedNavigationItem(selectedTab);
            } catch (IllegalStateException e) {
            	//we've already attached the tabs.
            	Log.w(TAG, "IllegalStateException thrown", e);
            	//this.finish();
            }
	    }
        
		/**the below code needs to run on app start.
         */
		{
			Intent updateInt = new Intent(this, HornetDBService.class);
            updateInt.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
            PendingIntent pintent = PendingIntent.getService(this, 0, updateInt, PendingIntent.FLAG_UPDATE_CURRENT);
            Services.setFreqPollingHandler(this, pintent);
		}
		
		
        startReciever();
                /************************************/
	}
	
	/*@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		try {
        	addTabs();
        	ActionBar ab = this.getSupportActionBar();
            ab.setSelectedNavigationItem(selectedTab);
        } catch (IllegalStateException e) {
        	//we've already attached the tabs.
        	Log.w(TAG, "IllegalStateException thrown", e);
        	//this.finish();
        }
	}*/
	
	private void firstSetup() {
		//take me to the magic page.
		Intent i = new Intent(this, SetupActivity.class);
		this.startActivity(i);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		//add back in at a later date.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("firstrun", true)) {
    		firstSetup();
            prefs.edit().putBoolean("firstrun", false).commit();
        }
		Services.setContext(this);
		if (Services.getProgress() != null) {
    		Services.getProgress().show();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (Services.getProgress() != null && Services.getProgress().isShowing()) {
    		Services.getProgress().dismiss();
    		//Services.setProgress(null);
    	}
		FragmentManager fm = this.getSupportFragmentManager();
		fm.popBackStackImmediate(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		fm.beginTransaction().commitAllowingStateLoss();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		try {
        	unregisterReceiver(Services.getFreqPollingHandler()); //?
        } catch (Exception e) {
        	//doesn't matter.
        }
	}
	
	@Override
	public void onBackPressed() {
		//this needs some minor tweaks. consider adding a queue.
		//also: this is disgusting. MAKE IT LOOK NICE.
		if (cFragment instanceof BookingsListSuperFragment) {
			BookingsListSuperFragment f = (BookingsListSuperFragment) cFragment;
			if (f.getCurrentFragment() instanceof BookingsOverviewFragment) {
				ActionBar ab = this.getSupportActionBar();
				ab.setSelectedNavigationItem(0); //just go back to the Find Member View?
			} else if (f.getCurrentFragment() instanceof BookingsSlideFragment) {
				BookingsSlideFragment slideFragment = (BookingsSlideFragment) f.getCurrentFragment();
				if (slideFragment.hasOverView()) {
					f.onBackPressed();
				} else {
					super.onBackPressed();
				}
			} else {
				//what fragment were we displaying??
				super.onBackPressed();
			}
		} else if (cFragment instanceof LastVisitorsSuperFragment){
			ActionBar ab = this.getSupportActionBar();
			ab.setSelectedNavigationItem(0);
		} else {
			super.onBackPressed();
		}
		//super.onBackPressed();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		//super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putInt("selectedTab", selectedTab);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        menu.findItem(R.id.action_addMember).setVisible(!drawerOpen);
        menu.findItem(R.id.action_addMember).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	/*public void setSelectedTab(int tab) {
		selectedTab = tab;
		ActionBar ab = getSupportActionBar();
		if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			//fm.popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			addTabs();
		}
		ab.setSelectedNavigationItem(selectedTab);
	}*/
	
	public void changeFragment(Fragment f, String tag) {
		FragmentManager fm = this.getSupportFragmentManager();
		ActionBar ab = getSupportActionBar();
		
		
		if (f != null) {
			cFragment = f;
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.content_view, f, tag);
			ft.addToBackStack(null);
			ft.commit();
			
			ab.removeAllTabs();
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		} else {
			if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
				//fm.popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				addTabs();
			}
			if (tag.compareTo("bookings")== 0) {
				selectedTab = 2;
			} else if (tag.compareTo("lastvisitors") ==0) {
				selectedTab = 1;
			} else {
				selectedTab = 0;
			}
			ab.setSelectedNavigationItem(selectedTab);
		}
	}
	
	public void genTabs() {
		ActionBar ab = getSupportActionBar();
		
		membertab = ab.newTab()
                .setText("Find Member")
                .setTabListener(new TabListener<MembersFindSuperFragment>(
                        this, "findmember", MembersFindSuperFragment.class));
		
		visitortab = ab.newTab()
				.setText("Last Visitors")
				.setTabListener(new TabListener<LastVisitorsSuperFragment>(
						this, "lastvisitors", LastVisitorsSuperFragment.class));
		
		bookingtab = ab.newTab()
				.setText("Bookings")
				.setTabListener(new TabListener<BookingsListSuperFragment>(
						this, "bookings", BookingsListSuperFragment.class));
	}
	
	public void addTabs() {
		ActionBar ab = getSupportActionBar();
		if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			ab.setDisplayShowTitleEnabled(true);
		}

		if ((membertab != null) && (ab.getTabCount() < 3 || ab.getTabAt(0) != membertab)) {
			ab.addTab(membertab);
		}
		if ((visitortab != null) && (ab.getTabCount() < 3 || ab.getTabAt(1) != visitortab)) {
			ab.addTab(visitortab);
		}
		if ((bookingtab != null) && (ab.getTabCount() < 3 || ab.getTabAt(2) != bookingtab)) {
			ab.addTab(bookingtab);
		}
	}
	
	public void startReciever(){
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(Services.getFreqPollingHandler(), intentFilter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int use_roll = Integer.parseInt(Services.getAppSettings(getContext(), "use_roll"));
		if (use_roll > 0) {
			getMenuInflater().inflate(R.menu.main_roll, menu);
		} else {
			getMenuInflater().inflate(R.menu.main, menu);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
		 	PollingHandler polling = Services.getFreqPollingHandler();
	    	polling.startService();
	    	return true;
	    }
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getFreqPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    }
	    case (R.id.action_addMember):{
	    	Intent intent = new Intent(this, MemberAdd.class);
	    	startActivity(intent);
	    	return true;
	    }
	    case (R.id.action_rollcall):{
	    	Intent i = new Intent(this, EmptyActivity.class);
	    	i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.RollList.getKey());
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_kpi):{
	    	Intent i = new Intent(this, EmptyActivity.class);
	    	i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.KPIs.getKey());
	    	startActivity(i);
	    	return true;
	    }
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	public static Context getContext(){
		return context;
	}
	
	public ListView getDrawerList() {
		return this.mDrawerList;
	}
	
	public Fragment getCurrentFragment() {
		return this.cFragment;
	}
}
