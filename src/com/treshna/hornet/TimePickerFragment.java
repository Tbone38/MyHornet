package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {
	
	String returnValue = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		int hour, minute;
		final Calendar c;
				
		// Use the current time as the default values for the picker
		c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		
		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
			DateFormat.is24HourFormat(getActivity()));
	}
	
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Intent bcIntent;
		Calendar cal;
		
		cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
		returnValue = format.format(cal.getTime());
		
		bcIntent = new Intent();
		bcIntent.setAction(ClassCreate.CLASSBROADCAST);
		getActivity().sendBroadcast(bcIntent);
	}
		
	public String getReturnValue() {
		return this.returnValue;
	}
}