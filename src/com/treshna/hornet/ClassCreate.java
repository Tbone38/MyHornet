package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ClassCreate extends NFCActivity implements OnClickListener{
	
	public static final String CLASSBROADCAST = "com.treshna.hornet.createclass"; 
	
	String datevalue = null, resourcevalue = null, resourceid = null, starttimevalue = null, endtimevalue = null;
	DatePickerFragment datePicker;
	TimePickerFragment stimePicker, etimePicker;
	AlertDialog alertDialog;
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
            ClassCreate.this.receivedBroadcast(intent);
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.class_create);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		//TODO:
		//	- setup date-selector. 				-- DONE
		datePicker = new DatePickerFragment();
		
		//	- setup time-selector.				-- DONE
		stimePicker = new TimePickerFragment();
		etimePicker = new TimePickerFragment();
		//										
		//	- setup dialog for resource selection. (see membership selection for example).
		//										-- DONE
		setText();
	}
	
	private void setText() {
		TextView date, resource, starttime, endtime, buttonaccept, buttoncancel;		
		
		date = (TextView) this.findViewById(R.id.classDate);
		date.setTag(Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		date.setClickable(true);
		date.setOnClickListener(this);
		if (datevalue != null) {
			date.setText(datevalue);
		}
		
		starttime = (TextView) this.findViewById(R.id.classStartTime);
		starttime.setClickable(true);
		starttime.setOnClickListener(this);
		if (starttimevalue != null) {
			starttime.setText(starttimevalue);
		}
		
		endtime = (TextView) this.findViewById(R.id.classEndTime);
		endtime.setClickable(true);
		endtime.setOnClickListener(this);
		if (endtimevalue != null) {
			endtime.setText(endtimevalue);
		}
		
		resource = (TextView) this.findViewById(R.id.classResource);
		resource.setClickable(true);
		resource.setOnClickListener(this);
		if (resourcevalue != null) {
			resource.setText(resourcevalue);
		}
		
		buttonaccept = (TextView) this.findViewById(R.id.buttonSubmit);
		buttonaccept.setClickable(true);
		buttonaccept.setOnClickListener(this);
		
		buttoncancel = (TextView) this.findViewById(R.id.buttonCancel);
		buttoncancel.setClickable(true);
		buttoncancel.setOnClickListener(this);
	}
	
	private void receivedBroadcast(Intent intent) {
		//get all of the variables!
		datevalue = datePicker.getReturnValue();
		starttimevalue = stimePicker.getReturnValue();
		endtimevalue = etimePicker.getReturnValue();
		
		setText();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		this.unregisterReceiver(this.mBroadcastReceiver);
		
		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter();
	    iff.addAction(CLASSBROADCAST);
	    this.registerReceiver(this.mBroadcastReceiver,iff);
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
	
	private void buildResourceAlert(){
		AlertDialog.Builder builder;
		LayoutInflater inflater;
		View layout;
		final RadioGroup rg;
		Cursor cur;
		ContentResolver contentResolver;
		
		builder = new AlertDialog.Builder(this);
		inflater = this.getLayoutInflater();
		layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		contentResolver = this.getContentResolver();
		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
		
		while (cur.moveToNext())
		{
			RadioButton rb;
			
			rb = new RadioButton(this);
			rb.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			rb.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)));
			rg.addView(rb);
		}
		
		cur.close();
		builder.setView(layout);
		builder.setTitle("Select Optional Resource");
		builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				int selectedid;
				RadioButton selectedRadio;
				
				selectedid = rg.getCheckedRadioButtonId();
				
				if (selectedid < 0) { //no radio button selected.
					return;
				}
				
				selectedRadio = (RadioButton) rg.findViewById(selectedid);
				
				resourcevalue = selectedRadio.getText().toString();
				resourceid = (String) selectedRadio.getTag();
				
				setText();
			}
		});
	
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				alertDialog = null;
			}
		});
		
		//builder.show();
		alertDialog = builder.create();
		alertDialog.show();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.classDate:{
				//date-picker
				Bundle bdl;
				
				bdl = new Bundle(1);
				bdl.putString(Services.Statics.KEY, (String) view.getTag());
				//datePicker = new DatePickerFragment();
				datePicker.setArguments(bdl);
			    datePicker.show(this.getSupportFragmentManager(), "datePicker");
				break;
			}
			case R.id.classStartTime:{
			    stimePicker.show(this.getSupportFragmentManager(), "timePicker");
				break;
			}
			case R.id.classEndTime:{
			    etimePicker.show(this.getSupportFragmentManager(), "timePicker");
				break;
			}
			case R.id.classResource:{
				buildResourceAlert();
				break;
			}
			case R.id.buttonSubmit:{
				/* TODO:
				 * - check that required fields are filled in.		-- DONE
				 * - check that times are real (end > start)		-- DONE
				 * - highlight issues								-- DONE
				 * - submit to database								-- DONE
				 */
				ArrayList<String> emptyViews;
				boolean validate_successful;
				
				emptyViews = validate();
				validate_successful = Boolean.valueOf(emptyViews.get(0));
				if (!validate_successful) {
					updateView(emptyViews);
					break;
				} 
				else {
					submit();
				}
				break;
			}
			case R.id.buttonCancel:{
				this.finish();
				break;
			}
		
		}
	}
	
	
	/** TODO:
	 * Get the fields that have been filled in.		-- DONE
	 * put them in the SQLiteDatabase				-- DONE
	 * add to pending.					--
	 * 		-if failed:
	 * 			queue upload for later.				--
	 */
	private void submit() {
		
		ContentResolver contentResolver;
		ContentValues values;
		String id;
		
		values = getInput();
		contentResolver = this.getContentResolver();
		id = contentResolver.insert(ContentDescriptor.Class.CONTENT_URI, values).getLastPathSegment();
		
		values = new ContentValues();
		values.put(ContentDescriptor.PendingUploads.Cols.ROWID, id);
		values.put(ContentDescriptor.PendingUploads.Cols.TABLEID,
				ContentDescriptor.TableIndex.Values.Class.getKey());
		
		contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
		System.out.print("\n\nClass inserted\n\n");
		Toast.makeText(this, "Class Created!", Toast.LENGTH_LONG).show();;
		this.finish();
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		for(int i=1; i<emptyFields.size(); i+=1){
			//get label, change colour.
			TextView label = (TextView) this.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private ArrayList<String> validate(){
		boolean validated;
		ArrayList<String> emptyViews;
		TextView date, stime, etime, resource;
		EditText classname, classmemberlimit;
		
		validated = true;
		emptyViews = new ArrayList<String>();
		
		classname = (EditText) this.findViewById(R.id.className);
		if (classname.getEditableText().toString().compareTo("") == 0) 
		{
			emptyViews.add(String.valueOf(R.id.classNameL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) this.findViewById(R.id.classNameL);
			label.setTextColor(Color.BLACK);
		}
		
		date = (TextView) this.findViewById(R.id.classDate);
		if (date.getText().toString().compareTo(this.getString(R.string.classdatedefault)) == 0)
		{ //no date set.
			emptyViews.add(String.valueOf(R.id.classDateL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) this.findViewById(R.id.classDateL);
			label.setTextColor(Color.BLACK);
		}
		
		stime = (TextView) this.findViewById(R.id.classStartTime);
		etime = (TextView) this.findViewById(R.id.classEndTime);
		if ((stime.getText().toString().compareTo(this.getString(R.string.classstarttimedefault)) == 0)
				|| (etime.getText().toString().compareTo(this.getString(R.string.classendtimedefault)) == 0))
		{
			emptyViews.add(String.valueOf(R.id.classTimeL));
			validated = false;
		} 
		else 
		{ //check that endtime is after starttime.
			int start, end;
			
			start = Integer.valueOf(stime.getText().toString().replace(":", ""));
			end = Integer.valueOf(etime.getText().toString().replace(":", ""));
			
			if (start >= end) {
				emptyViews.add(String.valueOf(R.id.classTimeL));
				validated = false;
			}  else {
				TextView label;
				label = (TextView) this.findViewById(R.id.classTimeL);
				label.setTextColor(Color.BLACK);
			}
		}
		
		classmemberlimit = (EditText) this.findViewById(R.id.classMemberLimit);
		if (classmemberlimit.getText().toString().compareTo("") == 0)
		{
			emptyViews.add(String.valueOf(R.id.classMemberLimitL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) this.findViewById(R.id.classMemberLimitL);
			label.setTextColor(Color.BLACK);
		}
		
		resource = (TextView) this.findViewById(R.id.classResource);
		if (resource.getText().toString().compareTo(this.getString(R.string.classresourcedefault)) == 0)
		{
			emptyViews.add(String.valueOf(R.id.classResourceL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) this.findViewById(R.id.classResourceL);
			label.setTextColor(Color.BLACK);
		}
		
		emptyViews.add(0, String.valueOf(validated));
		return emptyViews;
	}
	
	private ContentValues getInput() {
		ContentValues input;
		TextView date, stime, etime;
		EditText classname, classmemberlimit;
		CheckBox repeating;
		
		input = new ContentValues();
		
		classname = (EditText) this.findViewById(R.id.className);
		input.put(ContentDescriptor.Class.Cols.NAME, classname.getEditableText().toString());
		
		date = (TextView) this.findViewById(R.id.classDate);
		input.put(ContentDescriptor.Class.Cols.SDATE, date.getText().toString().replace(" ", ""));
		
		stime = (TextView) this.findViewById(R.id.classStartTime);
		input.put(ContentDescriptor.Class.Cols.STIME, stime.getText().toString());
		
		etime = (TextView) this.findViewById(R.id.classEndTime);
		input.put(ContentDescriptor.Class.Cols.ETIME, etime.getText().toString());
		
		classmemberlimit = (EditText) this.findViewById(R.id.classMemberLimit);
		input.put(ContentDescriptor.Class.Cols.MAX_ST, classmemberlimit.getEditableText().toString());
		
		repeating = (CheckBox) this.findViewById(R.id.classRepeating);
		if (repeating.isChecked()) {
			input.put(ContentDescriptor.Class.Cols.FREQ, "7 days");
		} else {
			input.putNull(ContentDescriptor.Class.Cols.FREQ);
		}
		
		if (resourceid != null) { //is set when a resource is selected.
			input.put(ContentDescriptor.Class.Cols.RID, String.valueOf(resourceid));
		} else {
			input.putNull(ContentDescriptor.Class.Cols.RID);
		}
		
		return input;
	}

}
