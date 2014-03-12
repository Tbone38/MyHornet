package com.treshna.hornet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * TODO: 	- populate spinner #2.			-DONE
 * 			- date widget					-DONE
 * 			- handle switch					-DONE
 * 
 * @author callum
 *
 */

public class MembershipHold extends ActionBarActivity implements OnClickListener, DatePickerFragment.DatePickerSelectListener,
		OnCheckedChangeListener{
	
	private String sdatevalue;
	DatePickerFragment sdatePicker;
	private String edatevalue;
	DatePickerFragment edatePicker;
	private String mMemberId;
	private String mMembershipId = null;
	private static final String TAG = "MembershipHold";
	private EditText input;
	private String currentPrice;
	private int theid;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.membership_hold);
		setContentView(R.layout.new_membership_hold);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		sdatePicker = new DatePickerFragment();
		sdatePicker.setDatePickerSelectListener(this);
		sdatevalue = null;
		
		edatePicker = new DatePickerFragment();
		edatePicker.setDatePickerSelectListener(this);
		edatevalue = null;
		
		Intent creationIntent = getIntent();
		mMemberId = creationIntent.getStringExtra(Services.Statics.KEY);
		theid = creationIntent.getIntExtra(ContentDescriptor.MembershipSuspend.Cols.SID, 0);
		
		
		setupView();
	}
	
	
	
	@SuppressLint("NewApi")
	private void setupView(){		
		setupDate();
		setupRadios();
			
		TextView accept, cancel;
		
		accept = (TextView) this.findViewById(R.id.buttonaccept);
		accept.setClickable(true);
		accept.setOnClickListener(this);
		
		
		cancel = (TextView) this.findViewById(R.id.buttoncancel);
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		
		if (theid > 0) {
			fillinViews();
		}
	}
	
	private void fillinViews() {
		ContentResolver contentResolver = this.getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, ContentDescriptor.MembershipSuspend.Cols.SID+" = ?",
				new String[] {String.valueOf(theid)}, null);
		if (!cur.moveToFirst()) {
			return;
		}
		
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		sdatevalue = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.STARTDATE));
		startdate.setText(sdatevalue);
		
		TextView enddate = (TextView) this.findViewById(R.id.hold_enddate);
		enddate.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ENDDATE)));
		
		String holdfee = null;
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE))) {
			holdfee = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE));
		}
		int radioid = 0;
		if (holdfee == null|| holdfee.compareTo("")==0) {
			//its a free time.
			radioid = R.id.holdfee_free_time;
		} else if (holdfee.compareTo("FREE")==0){
			radioid = R.id.holdfee_free;
		} else if (holdfee.compareTo("FULLCOST")==0){
			radioid = R.id.holdfee_fullcost;
		} else if (holdfee.compareTo("ONGOINGFEE")==0) {
			radioid = R.id.holdfee_ongoingfee;
		} else if (holdfee.compareTo("SETUPFEE")==0) {
			radioid = R.id.holdfee_setupcost;
		}
		
		RadioButton radio = (RadioButton) this.findViewById(radioid);
		//radio.setSelected(true);
		radio.setChecked(true);
		
		if (radioid == R.id.holdfee_ongoingfee) {
			
			input.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE)));
		} else if (radioid == R.id.holdfee_setupcost) {
			TextView input_heading = (TextView) this.findViewById(R.id.hold_fee_input_H);
			EditText input = (EditText) this.findViewById(R.id.hold_fee_input);
			
			input_heading.setVisibility(View.VISIBLE);
			input.setVisibility(View.VISIBLE);
			input.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ONEOFFFEE)));
		}
		
		CheckBox entry_check = (CheckBox) this.findViewById(R.id.hold_endonreturn);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ALLOWENTRY)).compareTo("t")==0) {
			entry_check.setChecked(true);
		} else {
			entry_check.setChecked(false);
		}
		
		CheckBox prorata_check = (CheckBox) this.findViewById(R.id.hold_prorata);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.PRORATA)).compareTo("t")==0) {
			prorata_check.setChecked(true);
		} else {
			prorata_check.setChecked(false);
		}
		
		EditText reason_view = (EditText) this.findViewById(R.id.hold_reason);
		reason_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.REASON)));
	}
	
	private void setupRadios() {
		RadioGroup holdfees = (RadioGroup) this.findViewById(R.id.holdfee);
		holdfees.setOnCheckedChangeListener(this);
		
		input = (EditText) this.findViewById(R.id.hold_fee_input);
		input.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(!s.toString().equals(currentPrice)){
					input.removeTextChangedListener(this);
					String replaceable = String.format(Locale.US,"[%s,.]", 
							NumberFormat.getCurrencyInstance(Locale.US).getCurrency().getSymbol());
					String cleanString = s.toString().replaceAll(replaceable, "");
					
				    double parsed = 0d;
				    try {
				    	parsed = Double.parseDouble(cleanString);
				    } catch ( NumberFormatException e) {
				    	//happens when the string is empty sometimes.
				    	parsed = 0d;
				    }
				    String formated = NumberFormat.getCurrencyInstance(Locale.US).format((parsed/100));

				    currentPrice = formated;
				    input.setText(formated);
				    input.setSelection(formated.length());
				    input.addTextChangedListener(this);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
		});
		input.setText("0.00");
	}
	
	private void setupDate(){
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		TextView enddate = (TextView) this.findViewById(R.id.hold_enddate);
		
		if (sdatevalue != null) {
			startdate.setText(sdatevalue);
		} else {
			startdate.setText(Services.DateToString(new Date()));
		}
		
		if (edatevalue != null) {
			enddate.setText(edatevalue);
		} else {
		}
		
		startdate.setTag(Services.dateFormat(new Date().toString(),
				"EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		startdate.setClickable(true);
		startdate.setOnClickListener(this);
		
		enddate.setClickable(true);
		enddate.setOnClickListener(this);
		
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
			sdatePicker.setArguments(bdl);
		    sdatePicker.show(this.getSupportFragmentManager(), "sdatePicker");
			
			break;
		}
		case (R.id.hold_enddate):{
			edatePicker.show(this.getSupportFragmentManager(), "edatePicker");
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
		String holdfee = null;
		String allow_entry, prorata;
		RadioGroup rg = (RadioGroup) this.findViewById(R.id.holdfee);
		int selectedRadio = rg.getCheckedRadioButtonId();
		switch (selectedRadio){
		case (R.id.holdfee_free_time):{
			values.put(ContentDescriptor.MembershipSuspend.Cols.ALLOWENTRY, "t");
			values.put(ContentDescriptor.MembershipSuspend.Cols.EXTEND_MEMBERSHIP, "t");
			values.put(ContentDescriptor.MembershipSuspend.Cols.FREEZE, "t");
			values.put(ContentDescriptor.MembershipSuspend.Cols.PROMOTION, "t");
			holdfee = null;
			break;
		} case (R.id.holdfee_free):{
			holdfee = "FREE";
			break;
		} case (R.id.holdfee_fullcost):{
			holdfee = "FULLCOST";
			break;
		} case (R.id.holdfee_ongoingfee):{
			holdfee = "ONGOINGFEE";
			break;
		} case (R.id.holdfee_setupcost):{
			holdfee = "SETUPFEE";
			break;
		}
		}

		if (holdfee != null && (holdfee.contains("SETUPFEE")|| holdfee.contains("ONGOINGFEE"))) {
			if (holdfee.contains("SETUPFEE")) {
				input = (EditText) this.findViewById(R.id.hold_fee_input);
				values.put(ContentDescriptor.MembershipSuspend.Cols.ONEOFFFEE, input.getEditableText().toString());
			} else {
				values.put(ContentDescriptor.MembershipSuspend.Cols.SUSPENDCOST, input.getEditableText().toString());
			}
		}

		if (holdfee != null) {
			CheckBox entry_check = (CheckBox) this.findViewById(R.id.hold_endonreturn);
			if (entry_check.isChecked()) allow_entry = "t";
			else allow_entry = "f";
			
			CheckBox prorata_check = (CheckBox) this.findViewById(R.id.hold_prorata);
			if (prorata_check.isChecked()) prorata = "t";
			else prorata = "f";
			
			values.put(ContentDescriptor.MembershipSuspend.Cols.ALLOWENTRY, allow_entry);
			values.put(ContentDescriptor.MembershipSuspend.Cols.PRORATA, prorata);
			values.put(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE, holdfee);
			values.put(ContentDescriptor.MembershipSuspend.Cols.FREEZE, "f");
		}

		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		values.put(ContentDescriptor.MembershipSuspend.Cols.STARTDATE, startdate.getText().toString());

		TextView enddate = (TextView) this.findViewById(R.id.hold_enddate);
		values.put(ContentDescriptor.MembershipSuspend.Cols.ENDDATE, enddate.getText().toString()); 
		
		EditText reason = (EditText) this.findViewById(R.id.hold_reason);
		values.put(ContentDescriptor.MembershipSuspend.Cols.REASON, reason.getEditableText().toString());

		ContentResolver contentResolver = this.getContentResolver();
		int sid;
		if (theid != 0) {
			sid = theid;
		} else {
			Cursor cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
					+ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey(), null, null);
			
			if (cur.getCount() <= 0) {
				sid = -1; 
			} else {
				cur.moveToFirst();
				sid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
			}
			cur.close();
		}
		values.put(ContentDescriptor.MembershipSuspend.Cols.SID, sid);
		values.put(ContentDescriptor.MembershipSuspend.Cols.DEVICESIGNUP, "t");
		if (theid != 0) {
			contentResolver.update(ContentDescriptor.MembershipSuspend.CONTENT_URI, values, ContentDescriptor.MembershipSuspend.Cols.SID+" = ?",
					new String[] {String.valueOf(sid)});
			
		} else {
			contentResolver.insert(ContentDescriptor.MembershipSuspend.CONTENT_URI, values);
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.TABLEID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.ROWID+" =  ?", new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
					String.valueOf(sid)});
		}
		
		//success! we should que an upload then leave the page.
		Intent suspend = new Intent(this, HornetDBService.class);
		suspend.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
	 	this.startService(suspend);
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
		
		TextView enddate = (TextView) this.findViewById(R.id.hold_enddate);
		if (enddate.getText().toString().compareTo(getString(R.string.membership_default_enddate)) == 0) {
			emptyViews.add(String.valueOf(R.id.hold_enddateL));
			validated = false;
		} else {
			TextView label = (TextView) this.findViewById(R.id.hold_enddateL);
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
		if (theDatePicker.getTag().compareTo("sdatePicker")==0) {
			sdatevalue = date;
		} else {
			edatevalue = date;
		}
		setupDate();
	}


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		TextView input_heading = (TextView) this.findViewById(R.id.hold_fee_input_H);
		EditText input = (EditText) this.findViewById(R.id.hold_fee_input);
		
		switch(checkedId){
		case (R.id.holdfee_ongoingfee):{
			input_heading.setText(getResources().getString(R.string.hold_fee_ongoing));
			input_heading.setVisibility(View.VISIBLE);
			
			input.setVisibility(View.VISIBLE);
			break;
		}
		case (R.id.holdfee_setupcost):{
			input_heading.setText(getResources().getString(R.string.hold_fee_initial));
			input_heading.setVisibility(View.VISIBLE);
			
			input.setVisibility(View.VISIBLE);
			break;
		}
		default:{
			input_heading.setText("");
			input_heading.setVisibility(View.GONE);
			input.getEditableText().clear();
			input.setVisibility(View.GONE);
			
			break;
		}
		}
		
	}

}
 