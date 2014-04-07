package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.treshna.hornet.BookingPage.TagFoundListener;
import com.treshna.hornet.navigation.NavDrawerItem;
import com.treshna.hornet.navigation.NavDrawerListAdapter;
import com.treshna.hornet.navigation.SlideMenuClickListener;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class EmptyActivity extends NFCActivity{
	FragmentManager frm;
	//NfcAdapter mNfcAdapter;
	private String[][] mTechLists;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	private TagFoundListener tagFoundListener;
	private int view;
	private static final String TAG = "EmptyActivity";
	
	// nav drawer title
    private CharSequence mDrawerTitle;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons; 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter navadapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//check bundle for value. create fragment depending on the value called.
		Services.setContext(this);
		//this.setContentView(R.layout.empty_activity);
		this.setContentView(R.layout.drawer_layout);
		frm = getSupportFragmentManager();
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		view = intent.getIntExtra(Services.Statics.KEY, -1);
		
		Bundle bdl = null;
		if (view == Services.Statics.FragmentType.MembershipAdd.getKey()) {
			String memberid = intent.getStringExtra(Services.Statics.MID);
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberid);

		} else if (view == Services.Statics.FragmentType.MemberDetails.getKey()) {
			ArrayList<String> tag = intent.getStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID);
			
			bdl = new Bundle(2);
			bdl.putString(Services.Statics.MID, tag.get(0));
		    bdl.putString(Services.Statics.KEY, tag.get(1));
		} else if (view == Services.Statics.FragmentType.MemberGallery.getKey()) {
			String memberid = intent.getStringExtra(Services.Statics.MID);
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberid);
		} else if (view == Services.Statics.FragmentType.RollItemList.getKey()) {
			int rollid = intent.getIntExtra(Services.Statics.ROLLID, -1);
			bdl = new Bundle(1);
			bdl.putInt(Services.Statics.ROLLID, rollid);
		} else if (view == Services.Statics.FragmentType.MemberAddTag.getKey()) {
			int memberid = intent.getIntExtra(Services.Statics.MID, -1);
			bdl = new Bundle(1);
			bdl.putInt(Services.Statics.MID, memberid);
		}
		setupNavDrawer();
		setFragment(view, bdl);
	}
	
	public void setFragment(int theView, Bundle bdl) {
		this.view = theView;
		Fragment f;
		tagFoundListener = null;
		FragmentTransaction ft = frm.beginTransaction();
		if (view == Services.Statics.FragmentType.MembershipAdd.getKey()) {
			f = new MembershipAdd();
			f.setArguments(bdl);
			
		} else if (view == Services.Statics.FragmentType.MembershipComplete.getKey()) {
			f = new MembershipComplete();
			f.setArguments(bdl);
			tagFoundListener = (TagFoundListener) f;
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
				
				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				mTechLists = new String[][] { new String[] {NfcA.class.getName()}};
				IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
			    try {
			        tag.addDataType("*/*");
			    } catch (MalformedMimeTypeException e) {
			    	//ignore, it's probably not critical.
			    }
			    intentFiltersArray = new IntentFilter[] {tag};
			}
		
		} else if (view == Services.Statics.FragmentType.MemberDetails.getKey()) {
			f = new MemberDetailsFragment();
			f.setArguments(bdl);
			tagFoundListener = (TagFoundListener) f;
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
				
				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				mTechLists = new String[][] { new String[] {NfcA.class.getName()}};
				IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
			    try {
			        tag.addDataType("*/*");
			    } catch (MalformedMimeTypeException e) {
			    	//ignore, it's probably not critical.
			    }
			    intentFiltersArray = new IntentFilter[] {tag};
			}
			
		} else if (view == Services.Statics.FragmentType.MemberGallery.getKey()) {
			f = new MemberGalleryFragment();
			f.setArguments(bdl);
		}
		
		else if (view == Services.Statics.FragmentType.RollList.getKey()) {
			f = new RollListFragment();
		}
		else if (view == Services.Statics.FragmentType.RollItemList.getKey()) {
			f = new RollItemListFragment();
			f.setArguments(bdl);
		}
		else if (view == Services.Statics.FragmentType.MemberAddTag.getKey()) {
			f = new MemberAddTagFragment();
			f.setArguments(bdl);
			tagFoundListener = (TagFoundListener) f;
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
				
				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				mTechLists = new String[][] { new String[] {NfcA.class.getName()}};
				IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
			    try {
			        tag.addDataType("*/*");
			    } catch (MalformedMimeTypeException e) {
			    	//ignore, it's probably not critical.
			    }
			    intentFiltersArray = new IntentFilter[] {tag};
			}
		} else if (view == Services.Statics.FragmentType.KPIs.getKey()) {
			f = new KeyPerformanceIndexFragment();
		}
		
		else { //default!
			f = null;
		}
		//ft.replace(R.id.empty_layout, f);
		ft.replace(R.id.content_view, f);
		ft.commit();
		
	}
	
	public void changeFragment(Fragment f, String tag) {
		FragmentTransaction ft = frm.beginTransaction();
		ft.replace(R.id.content_view, f, tag);
		ft.commit();
	}
	
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
				+ContentDescriptor.Booking.Cols.ARRIVAL+" <= ?",new String[] {String.valueOf(daystart), String.valueOf(dayend)}, null);
		
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
                R.drawable.ic_action_add_to_queue, //nav menu toggle icon
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
        		SlideMenuClickListener.ActivityType.EmptyActivity));
	}

	
	public void setTitle(String title) {
		super.setTitle(title);
	}
	
	@Override
	public void onNewIntent(Intent i) {
		String id;
		Tag card;
		
		card = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		id = this.getID(card);
		
		if (tagFoundListener != null) {
			tagFoundListener.onNewTag(id);
		} else {
			super.onNewIntent(i);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			if (view == Services.Statics.FragmentType.MembershipAdd.getKey()) {
				NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
				if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
			}
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (view == Services.Statics.FragmentType.MembershipComplete.getKey()) {
			if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction())){
				this.onNewIntent(this.getIntent());
			}
			NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, mTechLists);
		}
		setupNavDrawer();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return Services.createOptionsMenu(getMenuInflater(), menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
	    case (R.id.action_home):{
	    	Intent i = new Intent (this, MainActivity.class);
	    	startActivity(i);
	    	return true;
	    }
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
}
