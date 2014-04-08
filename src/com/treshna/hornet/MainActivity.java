package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
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
import com.treshna.hornet.report.ReportDateOptionsActivity;
import com.treshna.hornet.report.ReportListingActivity;
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
    private ActionBarDrawerToggle mDrawerToggle;
 
    // nav drawer title
    private CharSequence mDrawerTitle;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons; 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter navadapter;
    
    private static final String TAG = "MainActivity";
    private boolean is_syncing = false;
    private boolean sync_result = false;
    
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
			savedInstanceState.remove ("android:support:fragments");
			selectedTab = savedInstanceState.getInt("selectedTab");
			//savedInstanceState.clear();
	    }
		
		super.onCreate(savedInstanceState);

		//this.setContentView(R.layout.main_activity);
		this.setContentView(R.layout.drawer_layout);
		
		this.setTitle("GymMaster");
		this.setTitleColor(getResources().getColor(R.color.gym));
		context = getApplicationContext();
		setupNavDrawer();
		        
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
		this.sync_result = intent.getBooleanExtra(Services.Statics.IS_RESTART, false);
		this.is_syncing = false;
		setupNavDrawer();
	}

	//we need to update this after syncs...
	@SuppressLint("NewApi")
	private void setupNavDrawer() {
		mDrawerTitle = "GymMaster";
		int membercount = -1, visitcount = -1, bookingcount = -1;
		long daystart, dayend;
		ContentResolver contentResolver = this.getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, null, null, null);
		
		membercount = cur.getCount();
		cur.close();
		
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 1);
		daystart = cal.getTimeInMillis();
		cal.add(Calendar.HOUR, 23);
		cal.add(Calendar.MINUTE, 59);
		dayend = cal.getTimeInMillis();
		cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, ContentDescriptor.Visitor.Cols.DATETIME+" >= ?",
				new String[] {String.valueOf(daystart)}, null);
		
		visitcount = cur.getCount();
		cur.close();
		
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.ARRIVAL+" >= ? AND "
				+ContentDescriptor.Booking.Cols.ARRIVAL+" <= ? AND "+ContentDescriptor.Booking.Cols.CLASSID+" = 0 ",
				new String[] {String.valueOf(daystart), String.valueOf(dayend)}, null);
		
		bookingcount = cur.getCount();
		cur.close();
		
		// load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_main);
 
        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.slider_menu);
 
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
 
        // Recycle the typed array
        navMenuIcons.recycle();
 
        // setting the nav drawer list adapter
        navadapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(navadapter);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_action_opennav, //nav menu toggle icon
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
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener(this.getSupportFragmentManager(), mDrawerLayout, mDrawerList, this, 
        		SlideMenuClickListener.ActivityType.MainActivity));
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setHomeAsUpIndicator(null);

	}
	
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
        } else {
        	doSync();
        }
		Services.setContext(this);
		if (Services.getProgress() != null) {
    		Services.getProgress().show();
		}
		setupNavDrawer();
		
		IntentFilter iff = new IntentFilter();
	    iff.addAction("com.treshna.hornet.serviceBroadcast");
	    this.registerReceiver(this.mBroadcastReceiver,iff);
	}
	
	private void doSync() {
		boolean dosync = false;
		/*ContentResolver contentResolver = this.getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, null, null, null);
		dosync = (cur.getCount() <= 0);
		cur.close();*/
		String first_sync = Services.getAppSettings(getApplicationContext(), "first_sync");
		dosync = (first_sync.compareTo("-1")==0);
		
		if (dosync) {
        	Intent updateInt = new Intent(this, HornetDBService.class);
    		updateInt.putExtra(Services.Statics.KEY, Services.Statics.FIRSTRUN);
    	 	this.startService(updateInt);
        	FullSync task = new FullSync();
        	task.execute(null, null);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        /*menu.findItem(R.id.action_addMember).setVisible(!drawerOpen);
        menu.findItem(R.id.action_addMember).setVisible(!drawerOpen);*/
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
    
    private void setupFragment(Fragment f, String tag) {
    	if (tag.compareTo("memberDetails")==0) {
    		tagFoundListener = (TagFoundListener) f;
    	}
    }
	
	public void changeFragment(Fragment f, String tag) {
		FragmentManager fm = this.getSupportFragmentManager();
		ActionBar ab = getSupportActionBar();
		
		
		if (f != null) {
			cFragment = f;
			setupFragment(f, tag);
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
	    case (R.id.report_types_and_names):{
	    	this.startReportTypesAndNamesActivity(this);
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
		boolean tag_used = false;
		if (tagFoundListener != null) {
			Tag card = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String id = this.getID(card);
			tag_used = tagFoundListener.onNewTag(id);
			if (!tag_used) {
				super.onNewIntent(i);
			}
		} else {
			super.onNewIntent(i);
		}
	}

	private void startReportTypesActivity (Context view)
	{
		Intent intent = new Intent(view,Report_Types_ListActivity.class);
		this.startActivity(intent);
	}
	private void startReportNamesActivity (Context view)
	{
		Intent intent = new Intent(view,ReportNamesActivity.class);
		this.startActivity(intent);
	}
	private void startReportTypesAndNamesActivity (Context view)
	{
		Intent intent = new Intent(view,ReportListingActivity.class);
		this.startActivity(intent);
	}
	private void startReportDateOptionsActivity (Context view)
	{
		Intent intent = new Intent(view,ReportDateOptionsActivity.class);
		intent.putExtra("report_name", "Expiring Members");
		this.startActivity(intent);
	}
	
	private class FullSync extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private long starttime;
		private long curtime;
		private static final long TENMINUTES = 600000;
		private String message = null;
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(MainActivity.this, "Syncing..", 
					 "Syncing your GymMaster Database.", true);
			 MainActivity.this.is_syncing = true;
			 starttime = new Date().getTime();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			while (MainActivity.this.is_syncing) {
				curtime = new Date().getTime();
				if ((curtime - starttime) > TENMINUTES) {
					message = "Syncing took longer than 10 minutes, progress has been hidden.";
					return false;
				}
			}
			
			return MainActivity.this.sync_result;
		}
		

		@SuppressLint("NewApi")
		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				// refresh the page.
				//find a way to force refresh the activity.
				Intent intent = getIntent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			    finish();
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
