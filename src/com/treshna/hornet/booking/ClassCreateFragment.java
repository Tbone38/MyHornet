package com.treshna.hornet.booking;

import java.util.ArrayList;
import java.util.Date;

import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.R.string;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.DatePickerFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.services.TimePickerFragment;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Class;
import com.treshna.hornet.sqlite.ContentDescriptor.Resource;
import com.treshna.hornet.sqlite.ContentDescriptor.Class.Cols;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ClassCreateFragment extends Fragment implements OnClickListener, DatePickerFragment.DatePickerSelectListener, 
	TimePickerFragment.TimePickerSelectListener{
	
	public static final String CLASSBROADCAST = "com.treshna.hornet.createclass"; 
	
	String datevalue = null, resourcevalue = null, resourceid = null, starttimevalue = null, 
			endtimevalue = null, period = null;
	DatePickerFragment datePicker;
	TimePickerFragment stimePicker, etimePicker;
	AlertDialog alertDialog;
	LayoutInflater mInflater;
	
	private View view;
	
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mInflater = inflater;
		view = mInflater.inflate(R.layout.class_create, null);
		
		datePicker = new DatePickerFragment();
		datePicker.setDatePickerSelectListener(this);
		
		stimePicker = new TimePickerFragment();
		stimePicker.setTimePickerSelectListener(this);
		etimePicker = new TimePickerFragment();
		etimePicker.setTimePickerSelectListener(this);
		
		setText();
		return view;
	}
	
	
	@SuppressWarnings("deprecation")
	private void setText() {
		TextView date, resource, starttime, endtime, buttonaccept, buttoncancel;
		LinearLayout setdate, setresource, setstarttime;
		
		setdate = (LinearLayout) view.findViewById(R.id.button_class_date);	
		setdate.setTag(Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		//setdate.setTag(Services.DateToString(new Date()));
		setdate.setClickable(true);
		setdate.setOnClickListener(this);
		if (datevalue != null) {
			date = (TextView) view.findViewById(R.id.classDate);
			date.setText(datevalue);
		}
		
		setstarttime = (LinearLayout) view.findViewById(R.id.button_start_time);
		setstarttime.setClickable(true);
		setstarttime.setOnClickListener(this);
		if (starttimevalue != null) {
			starttime = (TextView) view.findViewById(R.id.classStartTime);
			starttime.setText(starttimevalue);
			if ( period != null) {
				int year = 2013, month = 10, day = 2;
				Date tempdate = new Date(year, month, day, 0, 0);
				Date enddate = new Date(year, month, day, Integer.parseInt(period.substring(0, 2)), 
						Integer.parseInt(period.substring(3, 5)));
				Date startdate = new Date(year, month, day, Integer.parseInt(starttimevalue.substring(0, 2)),
						Integer.parseInt(starttimevalue.substring(3, 5)));
				Long difference = enddate.getTime() - tempdate.getTime();
				difference = difference *2;
			
				endtimevalue = Services.dateFormat(new Date((startdate.getTime()+difference)).toString(), 
						"EEE MMM dd HH:mm:ss zzz yyyy", "HH:mm:ss");
			}
		}
		
		endtime = (TextView) view.findViewById(R.id.classEndTime);
		if (endtimevalue != null) {
			endtime.setText(endtimevalue);
		}
		
		setresource = (LinearLayout) view.findViewById(R.id.button_class_resource);
		setresource.setClickable(true);
		setresource.setOnClickListener(this);
		if (resourcevalue != null) {
			resource = (TextView) view.findViewById(R.id.classResource);
			resource.setText(resourcevalue);
		}
		
		buttonaccept = (TextView) view.findViewById(R.id.buttonSubmit);
		buttonaccept.setClickable(true);
		buttonaccept.setOnClickListener(this);
		
		buttoncancel = (TextView) view.findViewById(R.id.buttonCancel);
		buttoncancel.setClickable(true);
		buttoncancel.setOnClickListener(this);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}
	
	private void buildResourceAlert(){
		AlertDialog.Builder builder;
		
		View layout;
		final RadioGroup rg;
		Cursor cur;
		ContentResolver contentResolver;
		
		builder = new AlertDialog.Builder(getActivity());
		
		layout = mInflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		contentResolver = getActivity().getContentResolver();
		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
		
		while (cur.moveToNext())
		{
			RadioButton rb;
			
			rb = new RadioButton(getActivity());
			rb.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			ArrayList<String> tag = new ArrayList<String>();
			tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)));
			tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.PERIOD)));
			rb.setTag(tag);
			rg.addView(rb);
		}
		
		cur.close();
		builder.setView(layout);
		builder.setTitle("Select Optional Resource");
		builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {		
			@SuppressWarnings("unchecked")
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
				ArrayList<String> tag = null;
				if (selectedRadio.getTag() instanceof ArrayList<?>) {
					 tag = (ArrayList<String>) selectedRadio.getTag();
				}
				resourceid = (String) tag.get(0);
				period = tag.get(1);
				
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
			case R.id.button_class_date:{
				//date-picker
				Bundle bdl;
				
				bdl = new Bundle(1);
				bdl.putString(Services.Statics.KEY, (String) view.getTag());
				//datePicker = new DatePickerFragment();
				datePicker.setArguments(bdl);
			    datePicker.show(this.getChildFragmentManager(), "datePicker");
			    
				break;
			}
			case R.id.button_start_time:{
			    stimePicker.show(this.getChildFragmentManager(), "timePicker");
				break;
			}
			case R.id.classEndTime:{
			    etimePicker.show(this.getChildFragmentManager(), "timePicker");
				break;
			}
			case R.id.button_class_resource:{
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
				//this.finish();
				getActivity().onBackPressed();
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
		
		values = getInput();
		values.put(ContentDescriptor.Class.Cols.DEVICESIGNUP, "t");
		contentResolver = getActivity().getContentResolver();
		contentResolver.insert(ContentDescriptor.Class.CONTENT_URI, values).getLastPathSegment();
		
		
		System.out.print("\n\nClass inserted\n\n");
		Toast.makeText(getActivity(), "Class Created!", Toast.LENGTH_LONG).show();
		Intent updateInt = new Intent(getActivity(), HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
	 	getActivity().startService(updateInt);
		
		//this.finish();
	 	getActivity().onBackPressed();
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		for(int i=1; i<emptyFields.size(); i+=1){
			//get label, change colour.
			TextView label = (TextView) view.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private ArrayList<String> validate(){
		boolean validated;
		ArrayList<String> emptyViews;
		TextView date, stime, etime, resource;
		EditText classname;
		
		validated = true;
		emptyViews = new ArrayList<String>();
		
		classname = (EditText) view.findViewById(R.id.className);
		if (classname.getEditableText().toString().compareTo("") == 0) 
		{
			emptyViews.add(String.valueOf(R.id.classNameL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) view.findViewById(R.id.classNameL);
			label.setTextColor(Color.BLACK);
		}
		
		date = (TextView) view.findViewById(R.id.classDate);
		if (date.getText().toString().compareTo(this.getString(R.string.defaultDate)) == 0)
		{ //no date set.
			emptyViews.add(String.valueOf(R.id.classDateL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) view.findViewById(R.id.classDateL);
			label.setTextColor(Color.BLACK);
		}
		
		stime = (TextView) view.findViewById(R.id.classStartTime);
		etime = (TextView) view.findViewById(R.id.classEndTime);
		if ((stime.getText().toString().compareTo(this.getString(R.string.defaultStartTime)) == 0)
				|| (etime.getText().toString().compareTo(this.getString(R.string.defaultEndTime)) == 0))
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
				label = (TextView) view.findViewById(R.id.classTimeL);
				label.setTextColor(Color.BLACK);
			}
		}
		
		
		
		resource = (TextView) view.findViewById(R.id.classResource);
		if (resource.getText().toString().compareTo(this.getString(R.string.defaultResource)) == 0)
		{
			emptyViews.add(String.valueOf(R.id.classResourceL));
			validated = false;
		} else {
			TextView label;
			label = (TextView) view.findViewById(R.id.classResourceL);
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
		
		classname = (EditText) view.findViewById(R.id.className);
		input.put(ContentDescriptor.Class.Cols.NAME, classname.getEditableText().toString());
		
		date = (TextView) view.findViewById(R.id.classDate);
		input.put(ContentDescriptor.Class.Cols.SDATE, Services.dateFormat(date.getText().toString(),
				"dd MMM yyyy", "yyyyMMdd"));
		
		stime = (TextView) view.findViewById(R.id.classStartTime);
		input.put(ContentDescriptor.Class.Cols.STIME, stime.getText().toString());
		
		etime = (TextView) view.findViewById(R.id.classEndTime);
		input.put(ContentDescriptor.Class.Cols.ETIME, etime.getText().toString());
		
		classmemberlimit = (EditText) view.findViewById(R.id.classMemberLimit);
		if (classmemberlimit.getText().toString().compareTo("") == 0) {
			input.put(ContentDescriptor.Class.Cols.MAX_ST, 25);
		} else {
			input.put(ContentDescriptor.Class.Cols.MAX_ST, classmemberlimit.getEditableText().toString());
		}
		
		repeating = (CheckBox) view.findViewById(R.id.classRepeating);
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

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		if (theDatePicker == datePicker) {
			datevalue = date;
		}
		setText();
	}

	@Override
	public void onTimeSelect(String time, TimePickerFragment theTimePicker) {
		if (theTimePicker == stimePicker) {
			starttimevalue = time;
		}
		if (theTimePicker == etimePicker) {
			endtimevalue = time;
		}
		setText();
	}

}
