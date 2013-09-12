package com.treshna.hornet;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends NFCActivity {
	public int DOOR;
    private Cursor cur = null; 
    private static Context context;
	
	
    static public String PREF_NAME = "addMember";
	static public String PREF_KEY = "memberType";
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Services.setContext(this);
		System.out.println("App Started");
		
		context = getApplicationContext();
		DOOR = Integer.parseInt(Services.getAppSettings(this, "door"));
		if (DOOR == -1){
			TextView tview = (TextView) this.findViewById(R.id.welcome);
			tview.setText("This is your first time running the application, \n Please visits the settings"
					+" option before continuing buttons");
		}
		
		Intent updateInt = new Intent(this, HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
		PendingIntent pintent = PendingIntent.getService(this, 0, updateInt, PendingIntent.FLAG_UPDATE_CURRENT);
		//polling = new PollingHandler(this, pintent);
		Services.setPollingHandler(this, pintent);
		startReciever();
		
		
		SharedPreferences memberAdd = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		if (memberAdd.getInt(PREF_KEY, -1) == -1) {
			SharedPreferences.Editor editor = memberAdd.edit();
			editor.putInt(PREF_KEY, -1);
			editor.commit();
		}
		
		 if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		        final ActionBar actionBar = getActionBar();
		        actionBar.setDisplayShowHomeEnabled(true);
		        
		 }
	}
	public void startReciever(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(Services.getPollingHandler(), intentFilter);
	}
	
	@Override
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
	     //getAppSettings(this);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		DOOR = Integer.parseInt(Services.getAppSettings(this, "door"));
		//unregisterReceiver(polling);
		startReciever();
	}
	
	public static Context getContext(){
		return context;
	}
	

	public void lastVisitorsList(View view) {
		System.out.println("Query ALL Content Provider Button Pressed");
		Intent intent = new Intent(this, DisplayResultsActivity.class);
		intent.putExtra(Services.Statics.KEY,DisplayResultsActivity.LASTVISITORS); 
		startActivity(intent);
	}
	
	public void scanTag(View view) {
		if ((Build.VERSION.RELEASE.startsWith("1")) ||(Build.VERSION.RELEASE.startsWith("2"))) {
			Toast.makeText(getApplicationContext(),
	        		"This phone is not NFC enabled\n\r", Toast.LENGTH_LONG).show();
		} else {
			Intent intent = new Intent(this, HornetRFIDReader.class);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case (R.id.action_settings):
	    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	return true;
	    case (R.id.action_scan):
	    	Intent scanIntent = new Intent(this, HornetRFIDReader.class);
	    	startActivity(scanIntent);
	    	return true;
	    case (R.id.action_update):
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
		 		Services.setPreference(this, "sync_frequency", "5");
		 	}
		 	Intent updateInt = new Intent(this, HornetDBService.class);
			updateInt.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
		 //	this.startService(updateInt);
	    	startReciever();
	    	PollingHandler polling = Services.getPollingHandler();
	    	polling.startService();
	    	return true;
	    case (R.id.action_halt):
	    	PollingHandler poll = Services.getPollingHandler();
	    	poll.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    case (R.id.action_visitors):{
	    	Intent intent = new Intent(this, DisplayResultsActivity.class);
			intent.putExtra(Services.Statics.KEY,DisplayResultsActivity.LASTVISITORS); 
			startActivity(intent);
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
}
