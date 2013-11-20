package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

	String returnValue = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		int year, month, day;
		String date;
		final Calendar c;
		
		date = (String) getArguments().get(Services.Statics.KEY);
		
		//ensure's 0's are in the correct place.
		if (date.length() <8) date = date.substring(0, 4)+"0"+date.substring(4);
		
		c = Calendar.getInstance();
		c.set(Integer.parseInt(date.substring(0, 4)), (Integer.parseInt(date.substring(4, 6))-1), Integer.parseInt(date.substring(6)));
	
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		//System.out.print("\n\n"+year+" "+month+" "+day);
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		
		Intent bcIntent;
		Calendar cal;
		
		cal = Calendar.getInstance();
		cal.set(year, month, day);
		
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd", Locale.US);
		returnValue = format.format(cal.getTime());
		
		bcIntent = new Intent();
		bcIntent.setAction(ClassCreate.CLASSBROADCAST);
		getActivity().sendBroadcast(bcIntent);
		this.dismiss();
	}
	
	public String getReturnValue() {
		return this.returnValue;
	}
}