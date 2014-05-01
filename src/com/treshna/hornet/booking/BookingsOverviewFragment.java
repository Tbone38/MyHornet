package com.treshna.hornet.booking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.treshna.hornet.R;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.DatePickerFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

/*
 * 
 */
public class BookingsOverviewFragment extends Fragment implements OnClickListener, DatePickerFragment.DatePickerSelectListener {
	
	private static ContentResolver contentResolver = null;
    private static Cursor cur = null;
	private View view;
	private String selectedDate;
	private LayoutInflater mInflater;
	private DatePickerFragment mDatePicker;
	TextView mMonth = null, mDay = null, mYear = null, mTuesday = null;
	private ArrayList<String[]> resource;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        
        contentResolver = getActivity().getContentResolver();
        if (cur != null) cur.close();
        	
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (mDatePicker != null && mDatePicker.getDialog() != null) {
			mDatePicker.dismiss();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_booking_overview, container, false);
        mInflater = inflater;
        mDatePicker = new DatePickerFragment();
        mDatePicker.setDatePickerSelectListener(this);
        if (mDatePicker.getDialog() != null){
        	mDatePicker.getDialog().dismiss();
        }
        RelativeLayout calendarwrapper = (RelativeLayout) view.findViewById(R.id.booking_overview_calendar_wrapper);
        
        RelativeLayout.LayoutParams params;
        
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        	params.setMargins(10, 0, 10, 0);
        	params.addRule(RelativeLayout.LEFT_OF, 20);
        } else {
        	params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        
        mTuesday = new TextView(getActivity());
        mTuesday.setTextSize(45f);
        mTuesday.setTextColor(this.getResources().getColor(R.color.android_blue));
    	mTuesday.setLayoutParams(params);
    	mTuesday.setId(40);
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		mTuesday.setGravity(Gravity.CENTER);
    	} else {
    		mTuesday.setGravity(Gravity.CENTER_HORIZONTAL);
    	}
    	
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        	params.addRule(RelativeLayout.CENTER_VERTICAL);
    		params.addRule(RelativeLayout.RIGHT_OF, 20);
    		params.setMargins(10, 0, 10, 0);
    	} else {
    		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
    		params.addRule(RelativeLayout.BELOW, 40);
    	}
    	
    	mMonth = new TextView(getActivity());
    	mMonth.setTextSize(49);
    	mMonth.setTextColor(this.getResources().getColor(R.color.android_blue));
    	mMonth.setLayoutParams(params);
    	mMonth.setId(10);
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		mMonth.setGravity(Gravity.CENTER);
    	} else {
    		mMonth.setGravity(Gravity.CENTER_HORIZONTAL);
    	}
    	
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        	params.addRule(RelativeLayout.CENTER_VERTICAL);
        	params.addRule(RelativeLayout.CENTER_IN_PARENT);
    		params.setMargins(10, 0, 10, 0);
    	} else {
    		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
    		params.addRule(RelativeLayout.BELOW, 10);
    	}
    	
    	mDay = new TextView(getActivity());
    	mDay.setTextSize(55);
    	mDay.setTextColor(this.getResources().getColor(R.color.android_blue));
    	mDay.setLayoutParams(params);
    	mDay.setId(20);
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		mDay.setGravity(Gravity.CENTER);
    	} else {
    		mDay.setGravity(Gravity.CENTER_HORIZONTAL);
    	}
    	
    	
    	
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        	params.addRule(RelativeLayout.CENTER_VERTICAL);
    		params.addRule(RelativeLayout.RIGHT_OF, 10);
    		params.setMargins(10, 0, 10, 0);
    	} else {
    		params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
    		params.addRule(RelativeLayout.BELOW, 20);
    	}
    	
    	mYear = new TextView(getActivity());
    	mYear.setTextSize(47);
    	mYear.setTextColor(this.getResources().getColor(R.color.android_blue));
    	mYear.setLayoutParams(params);
    	mYear.setId(30);
    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		mYear.setGravity(Gravity.CENTER_VERTICAL);
    	} else {
    		mYear.setGravity(Gravity.CENTER_HORIZONTAL);
    	}
    	selectedDate = Services.getAppSettings(getActivity(), "bookings_date");
    	if (selectedDate.compareTo("-1") == 0) {
    		selectedDate = Services.DateToString(new Date());
    	}
        updateDate();
       
        calendarwrapper.addView(mTuesday);
        calendarwrapper.addView(mMonth);
        calendarwrapper.addView(mDay);
        calendarwrapper.addView(mYear);
        calendarwrapper.setClickable(true);
        calendarwrapper.setOnClickListener(this);

        setupList(inflater);
        
        return view;
    }
	
	private void updateDate(){
		if (mMonth != null && mDay != null && mYear != null) {
			mTuesday.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "EEEE, "));
			mDay.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "dd"));
			mMonth.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "MMM"));
			mYear.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "yyyy"));
		}
	}
	
	private void setupList(LayoutInflater inflater) {
		getActivity().setTitle("Bookings Overview");
		try {
			cur.close();
		} catch (Exception e) {
			cur = null;
		}
		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.HISTORY+" = 'f'", null, null);
		
		resource = new ArrayList<String[]>();
		while (cur.moveToNext()) {
			resource.add(new String[] {String.valueOf(cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID))),
					cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)) });
		}
		cur.close();
		
		BookingOverviewAdapter adapter = new BookingOverviewAdapter(getActivity(), R.layout.row_booking_overview, resource, selectedDate,
				 mInflater, this);
		ListView list = (ListView) view.findViewById(R.id.booking_resource_list);
		list.setAdapter(adapter);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.booking_overview_calendar_wrapper):{
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, selectedDate);
			
			mDatePicker.setArguments(bdl);
			mDatePicker.show(this.getChildFragmentManager(), "DatePicker");
		}
		default:
			break;
		}
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		selectedDate = date;
		Services.setPreference(getActivity(), "bookings_date", selectedDate);
		updateDate();

		setupList(mInflater);
	}
		    
}

