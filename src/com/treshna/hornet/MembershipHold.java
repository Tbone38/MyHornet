package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * TODO: 	- populate spinner #2.			-DONE
 * 			- date widget					-DONE
 * 			- handle switch					-DONE
 * 			- populate/handle spinner #1	-SKIPPED
 * 
 * @author callum
 *
 */

public class MembershipHold extends ActionBarActivity implements OnClickListener, DatePickerFragment.DatePickerSelectListener {
	
	private String datevalue;
	DatePickerFragment datePicker;
	private String mMemberId;
	private String mMembershipId = null;
	private static final String TAG = "MembershipHold";	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.membership_hold);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		datePicker = new DatePickerFragment();
		datePicker.setDatePickerSelectListener(this);
		datevalue = null;
		
		Intent creationIntent = getIntent();
		mMemberId = creationIntent.getStringExtra(Services.Statics.KEY);
		
		
		setupView();
	}
	
	
	
	@SuppressLint("NewApi")
	private void setupView(){		
		setupDate();
		
		TextView accept, cancel;
		
		accept = (TextView) this.findViewById(R.id.buttonaccept);
		accept.setClickable(true);
		accept.setOnClickListener(this);
		
		
		cancel = (TextView) this.findViewById(R.id.buttoncancel);
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		
	}
	
	private void setupDate(){
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		
		if (datevalue != null) {
			startdate.setText(datevalue);
		} else {
			startdate.setText(Services.DateToString(new Date()));
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
			ArrayList<String> result = validate();
			boolean validate_successful = Boolean.valueOf(result.get(0));
			if (!validate_successful) {
				updateView(result);
			} else {
				submit();
			}
			break;
		}
		}	
	}
	
	/**
	 * Gets the values from each of the fields,
	 * inserts them into the SQLite DB, (and que's pending uploads)
	 * 
	 */
	private void submit() {
		
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.MembershipSuspend.Cols.MID, mMemberId);
		
		ToggleButton gifttime = (ToggleButton) this.findViewById(R.id.gifttime);
		if (gifttime.isChecked()) {
			values.put(ContentDescriptor.MembershipSuspend.Cols.FREEZE, 1);
		} else {
			values.put(ContentDescriptor.MembershipSuspend.Cols.FREEZE, 0);
		}
		
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		values.put(ContentDescriptor.MembershipSuspend.Cols.STARTDATE,
				Services.dateFormat(startdate.getText().toString(), "dd MMM yyyy", "yyyMMdd"));

		Log.v(TAG, "Selected Startdate:"+startdate.getText().toString());
		Spinner duration = (Spinner) this.findViewById(R.id.hold_duration);
		String selection = String.valueOf(duration.getSelectedItem());
		Log.v(TAG, "Selected Duration: "+selection);
		values.put(ContentDescriptor.MembershipSuspend.Cols.LENGTH, selection);
		
		EditText reason = (EditText) this.findViewById(R.id.hold_reason);
		values.put(ContentDescriptor.MembershipSuspend.Cols.REASON, reason.getEditableText().toString());
		
		ContentResolver contentResolver = this.getContentResolver();
		/*Cursor cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, ContentDescriptor.MembershipSuspend.Cols.MID+" = 0",
				null, null);*/
		Cursor cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
				+ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey(), null, null);
		int sid;
		if (cur.getCount() <= 0) { 	//insert!
			sid = -1; 
		} else {					//update!
			cur.moveToFirst();
			sid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
		}
		values.put(ContentDescriptor.MembershipSuspend.Cols.SID, sid);
		values.put(ContentDescriptor.MembershipSuspend.Cols.DEVICESIGNUP, "t");
		contentResolver.insert(ContentDescriptor.MembershipSuspend.CONTENT_URI, values);
		//success! we should que an upload then leave the page.
		Intent suspend = new Intent(this, HornetDBService.class);
		suspend.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
	 	this.startService(suspend);
	 	Log.v(TAG, "Started Membership Suspend Update");
	 	//toast ?
		this.finish();
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		for(int i=1; i<emptyFields.size(); i+=1){
			//get label, change colour.
			TextView label = (TextView) this.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private ArrayList<String> validate() {
		ArrayList<String> emptyViews = new ArrayList<String>();
		boolean validated = true;
		
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		if (startdate.getText().toString().compareTo(this.getString(R.string.membership_default_startdate)) == 0) {
			emptyViews.add(String.valueOf(R.id.startdateL));
			validated = false;
		} else {
			TextView label = (TextView) this.findViewById(R.id.startdateL);
			label.setTextColor(Color.BLACK);
		}
		
		EditText reason = (EditText) this.findViewById(R.id.hold_reason);
		if (reason.getEditableText().toString().compareTo("") == 0) {
			emptyViews.add(String.valueOf(R.id.hold_reasonL));
			validated = false;
		} else {
			TextView label = (TextView) this.findViewById(R.id.hold_reasonL);
			label.setTextColor(Color.BLACK);
		}
		
		emptyViews.add(0, String.valueOf(validated));
		return emptyViews;
	}


	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		datevalue = date;
		setupDate();
	}

}
 