package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/*
 * 
 */
public class DisplayResultsActivity extends ListActivity {
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
            DisplayResultsActivity.this.receivedBroadcast(intent);
        }
    };
	
	private static ContentResolver contentResolver = null;
    private static Cursor cur = null;
    private SimpleCursorAdapter mAdapter;
	private static int currentDisplay = -1; //make this private.
    
	public static final int LASTVISITORS = 1;
	static public String PREF_NAME = "addMember";
	static public String PREF_KEY = "memberType";
	//public static final int DISPLAYERROR = -1;
	
	//NFC
	private String[][] mTechLists;
	private PendingIntent pendingIntent;
	private static Context context;	
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(this);
        Intent intent = getIntent();
        context = getApplicationContext();
        currentDisplay = intent.getIntExtra(Services.Statics.KEY, 1);
        contentResolver = this.getContentResolver();
        if (cur != null) cur.close();
        
        /**the below code needs to run on app start.
         */
        Intent updateInt = new Intent(this, HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
		PendingIntent pintent = PendingIntent.getService(this, 0, updateInt, PendingIntent.FLAG_UPDATE_CURRENT);
		//polling = new PollingHandler(this, pintent);
		Services.setPollingHandler(this, pintent);
		startReciever();
		/************************************/
		
		// what is this doing?
		SharedPreferences memberAdd = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		if (memberAdd.getInt(PREF_KEY, -1) == -1) {
			SharedPreferences.Editor editor = memberAdd.edit();
			editor.putInt(PREF_KEY, -1);
			editor.commit();
		}
        
        //NFC
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			//mTechLists = new String[][] { new String[] { MifareClassic.class.getName(), MifareUltralight.class.getName()}};
			mTechLists = new String[][] {new String[] {NfcA.class.getName()}};
			IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		    try {
		        tag.addDataType("*/*");
		    } catch (MalformedMimeTypeException e) {
		        throw new RuntimeException("fail", e);
		    }
		    //intentFiltersArray = new IntentFilter[] {tag};
		}
        //different views look different, build the views differently?
        getList();
    }
	
	public int getCurrentDisplay(){
		return currentDisplay;
	}
	
	public static Context getContext(){
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 * This is closing the cursor, it means when the application is returned to:
	 * no results will be displayed until a new cursor is opened.
	 */
	 public void onStop() {
		 super.onStop();
	     if(cur != null) cur.close();
	     try {
		    	unregisterReceiver(Services.getPollingHandler()); //?
		     } catch (Exception e) {
		    	 //doesn't matter.
		     }
	 }
	 
	 @Override
	 protected void onStart() {
	     super.onStart(); 
	 }
	 
	 protected void onRestart() {
		 super.onRestart();
	 }
	 
	 protected void onResume() {
		 super.onResume();
		 Services.setContext(this);
		 if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			 NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			 if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, mTechLists);
		 }
		 IntentFilter iff = new IntentFilter();
	     iff.addAction("com.treshna.hornet.serviceBroadcast");
	     this.registerReceiver(this.mBroadcastReceiver,iff);
		 getList();
	 }
	 
	 @Override
	 protected void onPause(){
		 super.onPause();
		 if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			 NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			 if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
		 }
		 this.unregisterReceiver(this.mBroadcastReceiver);
	 }
	
	 private void getList(){
		 mAdapter = null;
		 switch(currentDisplay){
	     case(LASTVISITORS):{
	       	System.out.println("Displaying Last Visitors"); //Display Last Visitors
	      
	       	
	       	setTitle("Last Visitors");
	       	try {
	       		cur.close();
	       	} catch (Exception e) {
	       		cur = null;
	       	}
	       	cur = contentResolver.query(ContentDescriptor.Visitor.VISITOR_JOIN_MEMBER_URI, null, null, null, ContentDescriptor.Visitor.Cols.DATETIME+" DESC limit 100");
			String[] from = {};
			int[] to = {};
			mAdapter = new VisitorsViewAdapter(this, R.layout.visitor_list, cur, from, to);
			setListAdapter(mAdapter);
			ListView listView = getListView();
			listView.setTextFilterEnabled(true);
			//TEXTFILTER + FILTER will help search?
	       	break;}
	     
	    /* case(DISPLAYERROR):
	       	break;
	     */}
	 }
		
	private void receivedBroadcast(Intent i) {
		System.out.println("*Refreshing List*");
		currentDisplay = LASTVISITORS;
		getList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void startReciever(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(Services.getPollingHandler(), intentFilter);
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
	    case (R.id.action_scan):
	    	Intent scanIntent = new Intent(this, HornetRFIDReader.class);
	    	startActivity(scanIntent);
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
	    case (R.id.action_visitors):{
	    	currentDisplay = LASTVISITORS;
			getList();
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
	    	Intent intent = new Intent(this, AddMember.class);
	    	startActivity(intent);
	    	return true;
	    }
	    case (R.id.action_findMember):{
	    	Intent i = new Intent(this, MemberFind.class);
	    	startActivity(i);
	    	return true;
	    }
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
    
    /** A tag has been discovered */
    @Override 
    public void onNewIntent(Intent intent){
    	if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
	    	System.out.print("intent Started"); 
	           	// get the tag object for the discovered tag
	    	Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    	NFCActivity nfc = new NFCActivity();
	    	String id = nfc.getID(tag); //this may not work?
	    	ContentResolver contentResolver = getContentResolver();
	    	Date today = new Date();
	    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);       
	       
	    	ContentValues values = new ContentValues();
	    	values.put(ContentDescriptor.Swipe.Cols.ID, id);
	    	values.put(ContentDescriptor.Swipe.Cols.DOOR, Services.getAppSettings(this, "door"));
	    	values.put(ContentDescriptor.Swipe.Cols.DATETIME, format.format(today));
	    	contentResolver.insert(ContentDescriptor.Swipe.CONTENT_URI, values);
	    	Intent updateInt = new Intent(this, HornetDBService.class);
			updateInt.putExtra(Services.Statics.KEY, Services.Statics.SWIPE);
			this.startService(updateInt);
    	}
    }
    
}

