package com.treshna.hornet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.booking.BookingsListSuperFragment;
import com.treshna.hornet.booking.BookingsOverviewFragment;
import com.treshna.hornet.booking.BookingsSlideFragment;
import com.treshna.hornet.member.MembersFindSuperFragment;
import com.treshna.hornet.navigation.NavDrawerItem;
import com.treshna.hornet.navigation.NavDrawerListAdapter;
import com.treshna.hornet.navigation.SlideMenuClickListener;
import com.treshna.hornet.navigation.TabListener;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.network.PollingHandler;	
import com.treshna.hornet.report.ReportListingFragment;
import com.treshna.hornet.report.ReportNamesActivity;
import com.treshna.hornet.report.Report_Types_ListActivity;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.setup.SetupActivity;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.visitor.LastVisitorsSuperFragment;


public class MainActivity extends NFCActivity {
	
	private static Tab membertab;
	private static Tab visitortab;
	private static Tab bookingtab;
	private static Context context;
	private static int selectedTab;
	private Fragment cFragment;
	private TagFoundListener tagFoundListener;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawerView;
    private ActionBarDrawerToggle mDrawerToggle;
 
    // nav drawer title
    private CharSequence mDrawerTitle;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons; 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter navadapter;
    private int selectedNavItem = -1;
    
    private static final String TAG = "MainActivity";
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.receivedBroadcast(intent);
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null)
	    {
			//savedInstanceState.remove ("android:support:fragments");
			selectedTab = savedInstanceState.getInt("selectedTab");
			//savedInstanceState.clear();
	    }
		HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
		mApplication.setActivityStatus(true);
		mApplication.setCurrentActivity(this);
		
		super.onCreate(savedInstanceState);

		//this.setContentView(R.layout.main_activity);
		this.setContentView(R.layout.activity_main);
		
		this.setTitle("GymMaster");
		this.setTitleColor(getResources().getColor(R.color.gym));
		context = getApplicationContext();
		setSelectedNavItem(1);
		setupNavDrawer();
		navadapter.notifyDataSetChanged();
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
	
	protected void receivedBroadcast(Intent intent) {
		
		if (intent.getAction().compareTo(HornetDBService.FINISHBROADCAST) == 0) {
			HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
			mApplication.setSyncResult(intent.getBooleanExtra(Services.Statics.IS_RESTART, false));
			mApplication.setSyncStatus(false);
			setupNavDrawer();
		} else if (intent.getAction().compareTo(HornetDBService.STARTBROADCAST) == 0) {
			HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
			mApplication.setSyncStatus(true);
			setupNavDrawer();
		}
	}
	
	private void setNavSync(SlideMenuClickListener mClicker) {
		TextView sync_text = (TextView) mDrawerView.findViewById(R.id.app_sync_time);
		ImageView sync_drawable = (ImageView) mDrawerView.findViewById(R.id.app_sync_drawable);
		HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
		
		RelativeLayout button_sync = (RelativeLayout) mDrawerView.findViewById(R.id.button_lastsync);
		button_sync.setOnClickListener(mClicker);
		
		if (mApplication.getSyncStatus() == true) {
			sync_text.setText(getString(R.string.sync_now));
			AnimationDrawable sync_anim = (AnimationDrawable) getResources().getDrawable(R.drawable.animation_sync);
			sync_drawable.setImageDrawable(sync_anim);
			if (!sync_anim.isRunning()) {
				sync_anim.start();
			}
		} else {
			Log.d(TAG, "SYNC STATUS != TRUE");
			sync_drawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_refresh));
			long last_sync = Long.parseLong(Services.getAppSettings(this, "last_freq_sync"));
			long current_time = new Date().getTime(); 
			long interval = (current_time - last_sync);
			
			if ( interval < 300000) 	{ //less than 5 minutes
				sync_text.setText(getString(R.string.sync_just_now));
			} else 
			if (interval < 3600000) 	{ //less than an hour ago
				double minutes = Double.valueOf(new DecimalFormat("#").format(
						((interval/1000)/60)));
				sync_text.setText(String.format(getString(R.string.sync_minutes), (int) minutes));
			} else
			if (interval < 7200000) 	{ //less than two hours ago
				sync_text.setText(getString(R.string.sync_one_hour));
			} else
			if (interval < 86400000) 	{ //less than a day, show us how many hours its been.
				double hours = Double.valueOf(new DecimalFormat("#").format(
						(((interval/1000)/60)/60)));
				sync_text.setText(String.format(getString(R.string.sync_hours), (int) hours));
			} else 
			if (interval < 172800000) 	{ //It's been like, a day!
				sync_text.setText(getString(R.string.sync_one_day));
			}
			else 						{ //it's been forever!
				double days = Double.valueOf(new DecimalFormat("#").format(
						(interval/86400000)));
				sync_text.setText(String.format(getString(R.string.sync_days), (int) days));
			}
		}
	}

	public void setSelectedNavItem(int navitem) {
		this.selectedNavItem = navitem;
	}
	
	public int getSelectedNavItem() {
		return this.selectedNavItem;
	}
	
	//we need to update this after syncs...
	@SuppressLint("NewApi")
	public void setupNavDrawer() {
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerView = (LinearLayout) findViewById(R.id.slider_wrapper);
        mDrawerList = (ListView) findViewById(R.id.slider_menu);
        mDrawerList.removeAllViewsInLayout();
        
		SlideMenuClickListener mClicker = new SlideMenuClickListener(mDrawerLayout, mDrawerList, this,
        		mDrawerView);
		mDrawerTitle = "GymMaster";
		mDrawerList.setItemChecked(getSelectedNavItem(), true);
        mDrawerList.setSelection(getSelectedNavItem());
		
		int membercount = -1, visitcount = -1, bookingcount = -1;
		ContentResolver contentResolver = this.getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, null, null, null);
		
		membercount = cur.getCount();
		cur.close();
		
		cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, ContentDescriptor.Visitor.Cols.DATETIME+" >= "
				+ "strftime('%s', current_date)*1000",null, null);
		
		visitcount = cur.getCount();
		cur.close();
		
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.ARRIVAL+" >= "
				+ "strftime('%s', current_date)*1000 AND "+ContentDescriptor.Booking.Cols.ARRIVAL+" <= "
				+ "strftime('%s', current_date, '+1 day', '-1 minute')*1000 AND "+ContentDescriptor.Booking.Cols.CLASSID+" = 0 ",
				null, null);
		
		bookingcount = cur.getCount();
		cur.close();
		
		// load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_main);
        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
        navDrawerItems = new ArrayList<NavDrawerItem>();
        // Main
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true));
        if (membercount <= 0) {
        	navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));         // Find Member
        } else {
        	navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, String.valueOf(membercount)));         // Find Member
        }
        if (visitcount <= 0) {
        	navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));         // Last Visitors
        } else {
        	navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, String.valueOf(visitcount)));         // Last Visitors
        }
        if (bookingcount <= 0) {
        	navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));			 // Bookings
        } else {
        	navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, String.valueOf(bookingcount)));			 // Bookings
        }
        // Create
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1), true));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        // Reports
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1), true));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(9, -1)));
        // Other
        int use_roll = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "use_roll"));
		if (use_roll > 0) {
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[10], navMenuIcons.getResourceId(10, -1), true));
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[11], navMenuIcons.getResourceId(11, -1)));
		}else {
			navDrawerItems.add(new NavDrawerItem(null, -1));
			navDrawerItems.add(new NavDrawerItem(null, -1));
		}
		//Setup
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[12], navMenuIcons.getResourceId(12, -1), true));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[13], navMenuIcons.getResourceId(13, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[14], navMenuIcons.getResourceId(14, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[15], navMenuIcons.getResourceId(15, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[16], navMenuIcons.getResourceId(16, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[17], navMenuIcons.getResourceId(17, -1)));
 
        // Recycle the typed array
        navMenuIcons.recycle();
        
        setNavSync(mClicker);
 
        // setting the nav drawer list adapter
        if (mDrawerList.getAdapter() != null) {
        	navadapter = (NavDrawerListAdapter) mDrawerList.getAdapter();
        	navadapter.updateItems(navDrawerItems);
        	navadapter.notifyDataSetChanged();
        } else {
        	navadapter = new NavDrawerListAdapter(getApplicationContext(),
        			navDrawerItems);
        }
        mDrawerList.setAdapter(navadapter);
        mDrawerList.setItemChecked(getSelectedNavItem(), true);
        mDrawerList.setSelection(getSelectedNavItem());
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, 
                R.string.app_name){
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
        mDrawerList.setOnItemClickListener(mClicker);
        mDrawerList.setItemChecked(getSelectedNavItem(), true);
        mDrawerList.setSelection(getSelectedNavItem());
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //getActionBar().setHomeAsUpIndicator(null);
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/
	}
	
	private void firstSetup() {
		//take me to the magic page.
		Intent i = new Intent(this, SetupActivity.class);
		this.startActivity(i);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
		mApplication.setActivityStatus(true);
		mApplication.setCurrentActivity(this);
		//add back in at a later date.
		IntentFilter finishFilter = new IntentFilter();
	    finishFilter.addAction(HornetDBService.FINISHBROADCAST);
	    
	    IntentFilter startFilter = new IntentFilter();
	    startFilter.addAction(HornetDBService.STARTBROADCAST);
	    
	    this.registerReceiver(this.mBroadcastReceiver, finishFilter);
	    this.registerReceiver(this.mBroadcastReceiver, startFilter);
	    
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("firstrun", true)) {
    		firstSetup();
            prefs.edit().putBoolean("firstrun", false).commit();
        } else {
        	doSync();
        }
		Services.setContext(this);
		if (Services.getProgress() != null) {
    		Services.getProgress().show();
		}
		setupNavDrawer();
		//changeFragment(cFragment, "onResume");
	}
	
	private void doSync() {
		boolean dosync = false;
		
		int show_count = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "show_count"));
		String first_sync = Services.getAppSettings(getApplicationContext(), "first_sync");
		dosync = (first_sync.compareTo("-1")==0);
		
		if (dosync) {
        	Intent updateInt = new Intent(this, HornetDBService.class);
    		updateInt.putExtra(Services.Statics.KEY, Services.Statics.FIRSTRUN);
    	 	this.startService(updateInt);
        	FullSync task = new FullSync();
        	task.execute(null, null);
		} else if (show_count < 3) { //we really only want show this the first few times we load up.
			new Handler().postDelayed(openDrawerRunnable(), 000);
			new Handler().postDelayed(closeDrawerRunnable(), 1750);
			Services.setPreference(getApplicationContext(), "show_count", String.valueOf((show_count+1)));
		}
	}
	
	private Runnable openDrawerRunnable() {
	    return new Runnable() {
	        @Override
	        public void run() {
	        	mDrawerLayout.openDrawer(Gravity.START);
	        }
	    };
	 }
	
	private Runnable closeDrawerRunnable() {
	    return new Runnable() {
	        @Override
	        public void run() {
	        	mDrawerLayout.closeDrawers();
	        }
	    };
	 }
	
	@Override
	public void onPause() {
		super.onPause();
		HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
		mApplication.setActivityStatus(false);
		if (Services.getProgress() != null && Services.getProgress().isShowing()) {
    		Services.getProgress().dismiss();
    		//Services.setProgress(null);
    	}
		this.unregisterReceiver(this.mBroadcastReceiver);
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
				//do normal back pop
				Log.v(TAG, "was OverView Fragment");
				ActionBar ab = this.getSupportActionBar();
				ab.setSelectedNavigationItem(0); //just go back to the Find Member View?
			} else if (f.getCurrentFragment() instanceof BookingsSlideFragment) {
				Log.v(TAG, "was Slider Fragment");
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
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putInt("selectedTab", selectedTab);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
		setupNavDrawer();
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerView);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
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
    
    public void setTagListener(Fragment f, String tag) {
    	if (tag.compareTo("memberDetails")==0) {
    		tagFoundListener = (TagFoundListener) f;
    	}
    }
    
    public void setCFragment(Fragment f) {
    	cFragment = f;
    }
	
	public void changeFragment(Fragment f, String tag) {
		FragmentManager fm = this.getSupportFragmentManager();
		ActionBar ab = getSupportActionBar();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		
		if (f != null) {
			cFragment = f;
			//tagFoundListener = (TagFoundListener) f;
			setTagListener(f, tag);
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.content_view, f, tag);
			ft.addToBackStack(null);
			ft.commit();
			
			ab.removeAllTabs();
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		} else {
			if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
				fm.popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
		getMenuInflater().inflate(R.menu.main, menu);
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
	    case (R.id.action_settings):
	    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	return true;
	    /*case (R.id.action_update): {
	    	setupNavDrawer();
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
		 		Services.setPreference(this, "sync_frequency", "5");
		 	}
		 	PollingHandler polling = Services.getFreqPollingHandler();
	    	polling.startService();
	    	return true;
	    }*/
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getFreqPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
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
	
	@Override
	public void onNewIntent(Intent i) {
		boolean swipe_handled = false;
		if (tagFoundListener != null) {
			Tag card = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String id = this.getID(card);
			swipe_handled = tagFoundListener.onNewTag(id);
			if (!swipe_handled) {
				super.onNewIntent(i);
			}
		} else {
			super.onNewIntent(i);
		}
	}
	
	private class FullSync extends AsyncTask<String, Integer, Boolean> {
		
		private long starttime;
		private long curtime;
		private static final long TENMINUTES = 600000;
		private String message = null;
		
		protected void onPreExecute() {
			 ProgressDialog progress = ProgressDialog.show(MainActivity.this, "Syncing..", 
					 "Syncing your GymMaster Database.", true);
			 Services.setProgress(progress);
			 HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
			 mApplication.setSyncStatus(true);
			 starttime = new Date().getTime();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
			boolean is_syncing = mApplication.getSyncStatus();
			while (is_syncing) {
				curtime = new Date().getTime();
				if ((curtime - starttime) > TENMINUTES) {
					message = "Syncing took longer than 10 minutes, progress has been hidden.";
					return false;
				}
				is_syncing = mApplication.getSyncStatus();
			}
			
			return mApplication.getSyncResult();
		}
		

		@SuppressLint("NewApi")
		protected void onPostExecute(Boolean success) {
			if (Services.getProgress() != null) {
				Services.getProgress().dismiss();
			}
			Services.setProgress(null);
			if (success) {
				// refresh the page.
				//find a way to force refresh the activity.
				Intent intent = getIntent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			    MainActivity.this.finish();
			    startActivity(intent);
			} else {
				message = (message == null)? "Error Encountered" : message;
				Toast.makeText(MainActivity.this, "Syncing took longer than 10 minutes, progress has been hidden.",Toast.LENGTH_LONG).show();
			}
	    }
	 }
	
	 public interface TagFoundListener {
	    public boolean onNewTag(String serial);

	}

}

	
