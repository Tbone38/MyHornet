package com.treshna.hornet;

import java.sql.ResultSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class HornetRFIDReader extends Activity {

	NfcAdapter mNfcAdapter;
	private String[][] mTechLists;
	PendingIntent pendingIntent;
	Tag tag;
	private JDBCConnection connection = null;
	Context context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(this);
		setContentView(R.layout.activity_rfid_reader);
		Button bview = (Button) this.findViewById(R.id.lastvisitors);
		bview.setVisibility(View.GONE);
		context = this;
		// Initialize the NFC adapter
		if ((Build.VERSION.RELEASE.startsWith("1")) ||(Build.VERSION.RELEASE.startsWith("2"))) {
			Toast.makeText(getApplicationContext(),
	        		"This phone is not NFC enabled\n\r", Toast.LENGTH_LONG).show();
			finish();
		}
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null) {
			Toast.makeText(getApplicationContext(),
				  "Tap an NFC tag for access\n\r", Toast.LENGTH_LONG).show();
		} else {
			TextView tview = (TextView) this.findViewById(R.id.rfidmessage);
	        tview.setText("No NFC capabilities on this Device.\nThe RFID-Reader ability will not be available.");
			Toast.makeText(getApplicationContext(),
        		"This phone is not NFC enabled\n\r", Toast.LENGTH_LONG).show();
		}
		// Create the PendingIntent object which will contain the details of the tag that has been scanned
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// Setup a tech list for all desired tag types
		mTechLists = new String[][] { new String[] { NfcA.class.getName()}};
	}

    /** Re-enable the tag dispatch if the app is in the foreground */
	@Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, mTechLists);
	}

   /** Disable the tag dispatch when the app is no longer in the foreground */
    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }
    
   
	@SuppressLint("DefaultLocale")
	public final String getID(){
    	StringBuilder sb = new StringBuilder();
       	for (byte b : tag.getId()) {
       		sb.append(String.format("%02X", b));
       	}
       	System.out.println("**TAG ID: "+sb.toString());
       	String cardID = null;
       	if(tag.getId().length == 4) {
       		String temp = sb.toString().substring(0, sb.toString().length() - 2).toLowerCase();
    	   	 cardID = "Mx"+temp;
       	} else if(tag.getId().length == 7){
       		String temp = sb.toString().toLowerCase();
       		 cardID = "Mv"+temp;
       	}
       	System.out.println(cardID);
    	return cardID;
    }

    /** A tag has been discovered */
    @Override 
    public void onNewIntent(Intent intent){
    	System.out.print("intent Started"); 
           	// get the tag object for the discovered tag
       tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
       // Start connection thread
       // at end of connection thread, start the list display!
       // **Consider moving this connection to the DB Service.
       new Thread(new Runnable() { //threading because Android blocks networking on main thread.
   			public void run() {
   				
		        //updates the view text
		        runOnUiThread(new Runnable() {
					  public void run() {
						  getView(1);
					  }	});
		              
   				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
   				connection = new JDBCConnection(preferences.getString("address", "-1"),preferences.getString("port", "-1"),
   					 preferences.getString("database", "-1"), preferences.getString("username", "-1"),
   					 preferences.getString("password", "-1"));
   				int door = Integer.parseInt(preferences.getString("door", "-1"));
   				System.out.println("*Door: "+door);
   				
   				try {
					connection.openConnection();
					String id = getID();
					System.out.print("\n\nID:"+id);
					ResultSet rs = connection.tagInsert(door, id);
					rs.close();
					connection.closePreparedStatement();
					rs = connection.getTagUpdate(door);
					//ResultSetMetaData rsmd = rs.getMetaData();
					/*int count = 1;
					while (count < rsmd.getColumnCount()){
						System.out.println(rsmd.getColumnName(count));
						count +=1;
					}
					*/
					String tempmess = null;
					while (rs.next()){
						tempmess = rs.getString("message")+" "+rs.getString("message2");
						System.out.println(tempmess);
						break;
					}
					
					rs.close();
					connection.closePreparedStatement();
					//final String insertMessage = tempmess;
   				
					/*runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(),
								  insertMessage, Toast.LENGTH_SHORT).show();
					}	});*/
					connection.closeConnection();
   				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
   			contProc();
   			}
       	}).start();
   }
    
    public void getView(int type){
    	TextView tview = (TextView) this.findViewById(R.id.rfidmessage);
    	switch(type){
    	case(1):{    		
    		tview.setText("Swiping Member In..");
    	}case(2):{
    		tview.setText("Swipe Complete. Please Navigate to Last Visitors or swipe another tag.");
    		Button bview = (Button) this.findViewById(R.id.lastvisitors);
            bview.setVisibility(View.VISIBLE);
    	}}
    }
    
    public void contProc(){
    	Intent serviceIntent = new Intent(this, HornetDBService.class);
    	serviceIntent.putExtra(Services.Statics.KEY,Services.Statics.LASTVISITORS);
    	startService(serviceIntent);
    	runOnUiThread(new Runnable() {
			  public void run() {
				  getView(2);
			  }	});
	   /* Intent newIntent = new Intent(this, DisplayResultsActivity.class);
	    newIntent.putExtra(MainActivity.EXTRA_RESULTS,DisplayResultsActivity.DISPLAYALL);
		startActivity(newIntent);
		finish();*/
    }
    public void lastVisitorsList(View v){
		Intent intent = new Intent(this, DisplayResultsActivity.class);
		intent.putExtra(Services.Statics.KEY,DisplayResultsActivity.LASTVISITORS); 
		startActivity(intent);
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