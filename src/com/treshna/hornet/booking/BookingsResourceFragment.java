package com.treshna.hornet.booking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.treshna.hornet.R;
import com.treshna.hornet.R.color;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.services.DatePickerFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Booking;
import com.treshna.hornet.sqlite.ContentDescriptor.BookingTime;
import com.treshna.hornet.sqlite.ContentDescriptor.Resource;
import com.treshna.hornet.sqlite.ContentDescriptor.Time;
import com.treshna.hornet.sqlite.ContentDescriptor.BookingTime.Cols;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;


public class BookingsResourceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
	OnClickListener, DatePickerFragment.DatePickerSelectListener{
	
    private static final String TAG = "BookingsResourceFragment";
    private View view;
    private String selectedDate;
    private int selectedResource;
    private LoaderManager mLoader;
    private BookingsListAdapter mAdapter;
    private ContentResolver mResolver;
    private Cursor cur;
    private boolean hasOverview = false; 
    private TextView mMonth, mDay, mYear;
    private DatePickerFragment mDatePicker;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        mLoader = getLoaderManager();
        mResolver = this.getActivity().getContentResolver();
        selectedDate = this.getArguments().getString("bookings_date");
        selectedResource = Integer.parseInt(Services.getAppSettings(getActivity(), "resourcelist"));
    	if (selectedDate.compareTo("-1") == 0) {
    		//selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
    		selectedDate = Services.DateToString(new Date());
    	}
        hasOverview = this.getArguments().getBoolean("hasOverview");
    }
	
	public boolean hasOverView(){
		return hasOverview;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		view = inflater.inflate(R.layout.fragment_booking_resource, container, false);
		
		setupCalendar();
		setupSpinner();
		setupList();
		
		return view;
	}
	
	
	@SuppressLint("NewApi")
	private void setupCalendar(){
		RelativeLayout calendarwrapper = (RelativeLayout) view.findViewById(R.id.booking_resource_calendar_wrapper);
		
		mDatePicker = new DatePickerFragment();
        mDatePicker.setDatePickerSelectListener(this);
        
        mMonth = new TextView(getActivity());
    	mDay = new TextView(getActivity());
    	mYear = new TextView(getActivity());
    	
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.RIGHT_OF, 20);
			params.setMargins(10, 0, 10, 0);
			mMonth.setTextSize(21);
			mMonth.setLayoutParams(params);
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    	
	    	mDay.setTextSize(21);
	    	mDay.setLayoutParams(params);
	    	
	    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    	params.addRule(RelativeLayout.RIGHT_OF, 10);
	    	mYear.setTextSize(21);
	    	mYear.setLayoutParams(params);
			
		} else {
	    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    	
	    	mMonth.setTextSize(28);
	    	mMonth.setLayoutParams(params);
	    	
	    	params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    	params.addRule(RelativeLayout.BELOW, 10);
	    	
	    	mDay.setTextSize(34);
	    	mDay.setLayoutParams(params);
	    	
	    	params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    	params.addRule(RelativeLayout.BELOW, 20);
	    	
	    	mYear.setTextSize(24);
	    	mYear.setLayoutParams(params);
	    	
		}
		
		mMonth.setTextColor(this.getResources().getColor(R.color.android_blue));
		mMonth.setId(10);
    	mMonth.setGravity(Gravity.CENTER_HORIZONTAL);
    	
    	mDay.setTextColor(this.getResources().getColor(R.color.android_blue));
    	mDay.setId(20);
    	mDay.setGravity(Gravity.CENTER_HORIZONTAL);
    	
		mYear.setTextColor(this.getResources().getColor(R.color.android_blue));
		mYear.setId(30);
    	mYear.setGravity(Gravity.CENTER_HORIZONTAL);
    	
		if (selectedDate == null) {
			//selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
			selectedDate = Services.DateToString(new Date());
		}
    	updateDate();
        calendarwrapper.addView(mMonth);
        calendarwrapper.addView(mDay);
        calendarwrapper.addView(mYear);
        calendarwrapper.setClickable(true);
        calendarwrapper.setOnClickListener(this);
        
	}
	
	private void updateDate(){
		if (mMonth != null && mDay != null && mYear != null) {
			/*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				//mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMMM")+" ");
				mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMMM")+" ");
				mDay.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "dd")+",  ");
				mYear.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "yyyy"));
			} else {
				//mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMM"));
				mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMM"));
				mDay.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "dd"));
				mYear.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "yyyy"));
			}*/
			mMonth.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "MMM"));
			mDay.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "dd"));
			mYear.setText(Services.dateFormat(selectedDate, "dd MMM yyyy", "yyyy"));
		}
	}
	
	private void updateSpinner() {
		Spinner name = (Spinner) view.findViewById(R.id.booking_resource_resource_name);
        cur = mResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
        //String rid = Services.getAppSettings(getActivity(), "resourcelist");
        int pos= 0, i = 0;
        
		ArrayList<String> resourcelist = new ArrayList<String>();
		while (cur.moveToNext()) {
			resourcelist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)) == selectedResource){
				pos = i;
			}
			i +=1;
		}
		cur.close();
        
        name.setSelection(pos);
	}
	
	private void setupSpinner() {
		mResolver = getActivity().getContentResolver();
		
		Spinner name = (Spinner) view.findViewById(R.id.booking_resource_resource_name);
        cur = mResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
        //String rid = Services.getAppSettings(getActivity(), "resourcelist");
        int pos= 0, i = 0;
        
		ArrayList<String> resourcelist = new ArrayList<String>();
		while (cur.moveToNext()) {
			resourcelist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)) == selectedResource){
				pos = i;
			}
			i +=1;
		}
		ArrayAdapter<String> resourceAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, resourcelist);
			resourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			name.setAdapter(resourceAdapter);
			name.setSelection(pos);
			name.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					
					
					if (cur != null){
						cur.close();
					}
					cur = mResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
					cur.moveToPosition(pos);
					selectedResource = cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID));
					
					updateSelection();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//leave alone (doesn't matter?)
				}});
		cur.close();
	}
	
	private void updateSelection(){
			Services.setPreference(getActivity(), "resourcelist", String.valueOf(selectedResource));
			updateDate();
			
		 	//just change the cursor.
		 	mLoader.restartLoader(0, null, this);	
		 	mAdapter.updateDate(selectedDate);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		//selectedDate = Services.getAppSettings(getActivity(), "bookings_date");
		selectedResource = Integer.parseInt(Services.getAppSettings(getActivity(), "resourcelist"));
		updateSelection();
		updateSpinner();
	}
	
	private void setupList() {
		String[] from = {};
       	int[] to = {};
       	ListView list = (ListView) view.findViewById(android.R.id.list);
		
       	mLoader.initLoader(0, null, this);
		mAdapter = new BookingsListAdapter(getActivity(), R.layout.row_booking_resource, null, from, to, selectedDate, list, getActivity());
		setListAdapter(mAdapter);
		
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (getActivity() != null) {
			//String rid = Services.getAppSettings(getActivity(), "resourcelist");
			//String date = Services.dateFormat(selectedDate, "dd MMM yyyy", "yyyyMMdd");
			String date = selectedDate;
			if (date == null) {
				date = selectedDate;
			}
			
			Date theDate = Services.StringToDate(date, "dd MMM yyyy");
			Calendar cal = Calendar.getInstance(Locale.US);
			cal.setTime(theDate);
			cal.set(Calendar.HOUR_OF_DAY, 0); 
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			long start, end;
			start = cal.getTime().getTime();
			cal.add(Calendar.MINUTE, 1439);
			end = cal.getTime().getTime();
			
			String selection = "bt."+ContentDescriptor.BookingTime.Cols.RID+" = "+selectedResource+" AND "
	       			+"bt."+ContentDescriptor.BookingTime.Cols.ARRIVAL+" BETWEEN "+start+" AND "+end;
			String[] where = {date, ContentDescriptor.Booking.Cols.RESULT+" > 5 ",
	       			" AND b."+ContentDescriptor.Booking.Cols.PARENTID+" <= 0"};
			return new CursorLoader(getActivity(), ContentDescriptor.Time.TIME_BOOKING_URI, null, selection, where, "_id ASC");
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		selectedDate = date;
		//selectedDate = Services.dateFormat(selectedDate, "dd MMM yyyy", "yyyyMMdd");
		Services.setPreference(getActivity(), "bookings_date", selectedDate);
		((BookingsSlideFragment) this.getParentFragment()).setDate(selectedDate);
		updateSelection();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.booking_resource_calendar_wrapper):{
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, selectedDate);
			
			mDatePicker.setArguments(bdl);
			mDatePicker.show(this.getChildFragmentManager(), "DatePicker");
		}
		}
		
	}
}
