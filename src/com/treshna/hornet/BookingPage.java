package com.treshna.hornet;

import java.util.ArrayList;

import com.treshna.hornet.MemberFindFragment.OnMemberSelectListener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class BookingPage extends FragmentActivity implements OnMemberSelectListener{
	
	private String bookingID;
	private String starttime;
	
	private String selectedID;
	private String selectedMS;
	private String selectedMSID;
	
	FragmentManager frm;
	RadioGroup rg;
	ContentResolver contentResolver;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Services.setContext(this);
		ArrayList<String> tagInfo = intent.getStringArrayListExtra(Services.Statics.KEY);
		bookingID = tagInfo.get(0);
		
		contentResolver = getContentResolver();
		frm = getSupportFragmentManager();
		FragmentTransaction ft = frm.beginTransaction();
	
		setContentView(R.layout.booking_page);
		
		if (Integer.parseInt(bookingID) > 0) {
			//setContentView(R.layout.booking_details);
			bookingID = tagInfo.get(1);
			BookingDetailsFragment f = new BookingDetailsFragment();
			Bundle bdl = new Bundle(1);
			System.out.print("\n\nPage BookingID:"+bookingID);
            bdl.putString(Services.Statics.KEY, bookingID);
            f.setArguments(bdl);
			ft.add(R.id.bookingframe, f);
			bookingID = tagInfo.get(1);
			//showBooking();
		} else {
			//add Member!
			//setContentView(R.layout.booking_add);
			BookingAddFragment f = new BookingAddFragment();
			starttime = tagInfo.get(1);
			Bundle bdl = new Bundle(1);
			System.out.print("\n\nSTART ID:"+starttime);
            bdl.putString(Services.Statics.KEY, starttime);
            f.setArguments(bdl);
			ft.add(R.id.bookingframe,f);
			//addBooking(tagInfo.get(1), savedInstanceState);
		}
		ft.commit();
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
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


	@Override
	public void onMemberSelect(String id) {
		//TODO: create an alert dialog with member's memberships in it.
		selectedID = id;
		Cursor cur;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ?",
				new String[] {id}, null);
		
		while (cur.moveToNext()) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)) != null 
					&& cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)).compareTo("") !=0) {
				RadioButton rb = new RadioButton(this);
				rb.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
				rb.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
				rg.addView(rb);
			}
		}	
		cur.close();
        builder.setView(layout);
        builder.setTitle("Select Membership for Booking");
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	
            	if (rg.getChildCount()<=0) {
            		//no memberships found for this member, ignore and continue?
            		selectedMSID = null;
            	} else if (rg.getChildCount() > 0) {
	            	
	            	int cid = rg.getCheckedRadioButtonId();     	
	            	RadioButton rb = (RadioButton) rg.findViewById(cid);
	            	selectedMS = (String) rb.getText();
	            	selectedMSID = (String) rb.getTag();
	            	System.out.print("\n\nSelected Membership:"+selectedMS+" with ID:"+selectedMSID);
            	}
	            /** Rabbit Hole: fix this by changing the member name handling as well.
	             * 
	             */
            	Cursor cur;
            	cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.MID+" = ?", 
        				new String[] {selectedID}, null);
        		if (cur.getCount() <= 0) {
        			// what should I do?
        		}
        		String fname = null;
        		String sname = null;
        		cur.moveToFirst();
        		fname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME));
        		sname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME));
        		
        		frm = getSupportFragmentManager();
        		FragmentTransaction ft = frm.beginTransaction();
        		BookingAddFragment f = new BookingAddFragment();
        		Bundle bdl = new Bundle(4);
        		//System.out.print("\n\nSTART ID:"+starttime);
                bdl.putString(Services.Statics.KEY, starttime);
                bdl.putString(Services.Statics.IS_BOOKING_F, fname);
                bdl.putString(Services.Statics.IS_BOOKING_S, sname);
                bdl.putString(Services.Statics.MSID, selectedMSID);
                f.setArguments(bdl);
                ft.replace(R.id.bookingframe, f);
        		ft.commit();
        		frm.popBackStack();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
		
		
	}
}
