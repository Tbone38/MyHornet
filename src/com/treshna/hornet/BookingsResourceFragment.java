package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
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
    	if (Integer.parseInt(selectedDate) == -1) {
    		selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
    	}
    	Log.w(TAG, "Selected Date OnCreate:"+selectedDate);
        hasOverview = this.getArguments().getBoolean("hasOverview");
    }
	
	public boolean hasOverView(){
		return hasOverview;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		try {
			view = inflater.inflate(R.layout.booking_resource, container, false);
		} catch (ClassCastException e) {
			Log.e(TAG, "OnCreate View, inflate", e);
		}
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
			mMonth.setTextSize(21);
			mMonth.setLayoutParams(params);
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    	params.addRule(RelativeLayout.RIGHT_OF, 10);
	    	mDay.setTextSize(21);
	    	mDay.setLayoutParams(params);
	    	
	    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    	params.addRule(RelativeLayout.RIGHT_OF, 20);
	    	
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
			selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
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
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMMM")+" ");
				mDay.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "dd")+",  ");
				mYear.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "yyyy"));
			} else {
				mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMM"));
				mDay.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "dd"));
				mYear.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "yyyy"));
			}
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
					int resourceid;
					
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
		 	//mLoader.restartLoader(0, null, this);	
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
		mAdapter = new BookingsListAdapter(getActivity(), R.layout.booking_resource_row, null, from, to, selectedDate, list);
		setListAdapter(mAdapter);
		
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (getActivity() != null) {
			//String rid = Services.getAppSettings(getActivity(), "resourcelist");
			String selection = "bt."+ContentDescriptor.BookingTime.Cols.RID+" = "+selectedResource+" AND "
	       			+"bt."+ContentDescriptor.BookingTime.Cols.ARRIVAL+" = "+selectedDate;
			String[] where = {selectedDate, ContentDescriptor.Booking.Cols.RESULT+" > 5 ", 
	       			" AND b."+ContentDescriptor.Booking.Cols.PARENTID+" <= 0"};
	       	Log.v(TAG,"Selected Date:"+selectedDate);
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
		selectedDate = Services.dateFormat(selectedDate, "yyyy MM dd", "yyyyMMdd");
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
