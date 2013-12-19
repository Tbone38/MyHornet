package com.treshna.hornet;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * TODO: 	- populate spinner #2.			-Done
 * 			- date widget					-
 * 			- handle switch					-
 * 			- populate/handle spinner #1	-
 * 
 * @author callum
 *
 */

public class MembershipHold extends ActionBarActivity implements OnClickListener {
	
	private String datevalue;
	DatePickerFragment datePicker;
	private String mMemberId;
	private String mMembershipId;
	

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
            MembershipHold.this.receivedBroadcast(intent);
        }	
    };
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.membership_hold);
		
		
		datePicker = new DatePickerFragment();
		datevalue = null;
		
		Intent creationIntent = getIntent();
		mMemberId = creationIntent.getStringExtra(Services.Statics.KEY);
		mMembershipId = creationIntent.getStringExtra(Services.Statics.MSID);
		
		setupView();
	}
	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setupView(){		
		setupDate();
		
		TextView accept, cancel;
		
		accept = (TextView) this.findViewById(R.id.buttonaccept);
		accept.setClickable(true);
		accept.setOnClickListener(this);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			accept.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			accept.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		
		cancel = (TextView) this.findViewById(R.id.buttoncancel);
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			cancel.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
	}
	
	private void setupDate(){
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		
		if (datevalue != null) {
			startdate.setText(datevalue);
		}
		
		startdate.setTag(Services.dateFormat(new Date().toString(),
				"EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		startdate.setClickable(true);
		startdate.setOnClickListener(this);
		
		TextView membername = (TextView) this.findViewById(R.id.membername);
		
		if (mMemberId != null) {
			ContentResolver contentResolver = getContentResolver();
			Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, new String[] {ContentDescriptor.Member.Cols.FNAME,
					ContentDescriptor.Member.Cols.SNAME}, "m."+ContentDescriptor.Member.Cols.MID+" = ?" ,new String[] {mMemberId}, null);
			
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				membername.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))+" "
						+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
			}
			cur.close();
		}
		
		TextView membershipname = (TextView) this.findViewById(R.id.membershipname);
		
		if (mMembershipId != null) {
			ContentResolver contentResolver = getContentResolver();
			Cursor cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, new String[] {ContentDescriptor.Membership.Cols.PNAME},
					ContentDescriptor.Membership.Cols.MSID+" = ?", new String[] {mMembershipId}, null);
			
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				membershipname.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			}
			cur.close();
		}
	}
	
	
	private void receivedBroadcast(Intent intent) {	
		datevalue = datePicker.getReturnValue();
		setupDate();
	}
	
	
	@Override 
	public void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter();
	    iff.addAction(ClassCreate.CLASSBROADCAST);
	    this.registerReceiver(this.mBroadcastReceiver,iff);
	}
	
	
	@Override
	public void onPause(){
		super.onPause();
		this.unregisterReceiver(this.mBroadcastReceiver);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.not_main, menu);
		return true;
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

	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case (R.id.startdate):{
			Bundle bdl;
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) view.getTag());
			//datePicker = new DatePickerFragment();
			datePicker.setArguments(bdl);
		    datePicker.show(this.getSupportFragmentManager(), "datePicker");
			
			break;
		}
		case (R.id.buttoncancel):{
			this.finish();
			break;
		}
		case (R.id.buttonaccept):{
			//do input-checking.
			
		}
		}	
	}

}
 