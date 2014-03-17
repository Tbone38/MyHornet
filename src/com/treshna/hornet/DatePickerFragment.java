package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

	String returnValue = null;
	DatePickerSelectListener mCallback;
	private static final String TAG = "DatePickerFragment";
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		int year, month, day;
		String date = null;
		final Calendar c;
		try {
			date = (String) getArguments().get(Services.Statics.KEY);
		} catch (NullPointerException e) {
			date = null;
		}
		
		if (date == null || !(date instanceof String) || date.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			date = Services.dateFormat(format.format(new Date()), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
		} else {
			String outdate = Services.dateFormat(date, "dd MMM yyyy", "yyyyMMdd");
			if (outdate != null) {
				date = outdate;
			}
		}
		//ensure's 0's are in the correct place.
		if (date.length() <8) {
			date = date.substring(0, 4)+"0"+date.substring(4);
		}
		
		c = Calendar.getInstance();
		c.set(Integer.parseInt(date.substring(0, 4)), (Integer.parseInt(date.substring(4, 6))-1), Integer.parseInt(date.substring(6)));
	
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		
		Calendar cal;
		
		cal = Calendar.getInstance();
		cal.set(year, month, day);
		
		
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		returnValue = format.format(cal.getTime());
		mCallback.onDateSelect(returnValue, this);
	}
		
	 public interface DatePickerSelectListener {
	        public void onDateSelect(String date, DatePickerFragment theDatePicker);
	 }
	 
	 @Override
	 public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        
	        // This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            mCallback = (DatePickerSelectListener) activity;
	        } catch (ClassCastException e) {
	            //mCallback not set
	        	Log.w(TAG, "WARNING, Class not using interface, this may cause issues if it doesn't manually call"
	        			+ " setDatePickerSelectListener(DatePickerSelectListener);");
	        }
	    }
	 
	 public void setDatePickerSelectListener(DatePickerSelectListener theListener) {
		 this.mCallback = theListener;
	 }
	
}