package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RelativeLayout;


public class BookingsResourceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	
    private static final String TAG = "BookingsResourceFragment";
    private View view;
    private String selectedDate;
    private LoaderManager mLoader;
    private BookingsListAdapter mAdapter;
    private ContentResolver mResolver;
    private Cursor cur;
    private CalendarView calendar;
    
    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ON CREATE");
        Services.setContext(getActivity());
        mLoader = getLoaderManager();
        mResolver = this.getActivity().getContentResolver();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		Log.d(TAG, "ON CREATE VIEW");
		try {
			view = inflater.inflate(R.layout.new_booking_resource, container, false);
		} catch (ClassCastException e) {
			Log.e(TAG, "OnCreate View, inflate", e);
		}
		setupCalendar();
		setupSpinner();
		setupList();
		
		Log.d(TAG, "FINSHED ON CREATE VIEW");
		try {
			return view;
		} catch (ClassCastException e) {
			Log.e(TAG, "ERROR RETURNING FROM ON CREATE VIEW", e);
		}
		return null;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		Log.d(TAG, "STARTING ACTIVITY CREATION");
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "FINISHED ACTIVITY CREATION");
	}
	
	@Override
	public void onStart() {
		Log.d(TAG, "STARTING ON START");
		super.onStart();
		Log.d(TAG, "FINISHING ON START");
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "STARTING ON RESUME");
		super.onResume();
		Log.d(TAG, "FINISHING ON RESUME");
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "STARTING ON PAUSE");
		super.onPause();
		Log.d(TAG, "FINISHING ON PAUSE");
	}
	
	@SuppressLint("NewApi")
	private void setupCalendar(){
		Log.d(TAG, "SETUP CALENDAR");
		RelativeLayout calendarwrapper = (RelativeLayout) view.findViewById(R.id.booking_resource_calendar_wrapper_3);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	CalendarView calendar = null;
        	try {
	        	calendar = new CalendarView(getActivity());
	        } catch (ClassCastException e) {
	        	e.printStackTrace();
	        	Log.e(TAG, "WHY IS THIS HAPPENING??!!!!", e);
	        }
	        
	        Calendar cal = Calendar.getInstance(Locale.US);
	        calendar.setDate(cal.getTime().getTime());
	        selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
	        calendar.setMinimumHeight(200);
	        cal.add(Calendar.MONTH, -1);
	        calendar.setMinDate(cal.getTimeInMillis());
	        cal.add(Calendar.MONTH, 2);
	        calendar.setMaxDate(cal.getTimeInMillis());
	        
	        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500);
	        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
	        try {
	        	calendar.setLayoutParams(params);
	        } catch (Exception e) {
	        	Log.e(TAG, "Still looking for where the error is being thrown", e);
	        }
	        calendar.setShowWeekNumber(false);
	        //calendar.setFocusedMonthDateColor(color.member_blue);
	        calendar.setOnDateChangeListener(new OnDateChangeListener() {
				@Override
				public void onSelectedDayChange(CalendarView view, int year,
						int month, int dayOfMonth) {
						Calendar cal = Calendar.getInstance();
						cal.set(year, month, dayOfMonth);
						Date date = cal.getTime();
						selectedDate = Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
						updateSelection(-1);
				}
	        	
	        });
	        try {
	        	calendarwrapper.addView(calendar);
	        } catch	(ClassCastException e) {
	        	e.printStackTrace();
	        	Log.e(TAG, "SERIOUSLY, WHY MEEE?!!", e);
	        }
	        
        } else {
        	DatePicker date = new DatePicker(getActivity());
        	Calendar cal = Calendar.getInstance(Locale.US);
        	date.init(cal.get(Calendar.YEAR),
        			cal.get(Calendar.MONTH), 
        			cal.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
						@Override
						public void onDateChanged(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Calendar cal = Calendar.getInstance();
							cal.set(year, monthOfYear, dayOfMonth);
							Date date = cal.getTime();
							selectedDate = Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
							updateSelection(-1);
						}
        	});
        	calendarwrapper.addView(date);
        }
	}
	
	private void setupSpinner() {
		Log.d(TAG, "SETTING UP SPINNER");
		mResolver = getActivity().getContentResolver();
		
		Spinner name = (Spinner) view.findViewById(R.id.booking_resource_resource_name);
        cur = mResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
        String rid = Services.getAppSettings(getActivity(), "resourcelist");
        int pos= 0, i = 0;
        
		ArrayList<String> resourcelist = new ArrayList<String>();
		while (cur.moveToNext()) {
			resourcelist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)).compareTo(rid) ==0){
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
					resourceid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID));
					
					updateSelection(resourceid);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//leave alone (doesn't matter?)
				}});
		cur.close();
	}
	
	private void updateSelection(int value){
		Log.d(TAG, "UPDATING SELECTED");
			if (value >= 0) {
		    	Services.setPreference(getActivity(), "resourcelist", String.valueOf(value));
			}
		 	
		 	//just change the cursor.
		 	mLoader.restartLoader(0, null, this);		 
    }
	
	private void setupList() {
		Log.d(TAG, "SETTING UP LIST");
		String[] from = {ContentDescriptor.Time.Cols.TIME};
       	int[] to = {R.id.booking_resource_time};
  
		//mLoader.restartLoader(0, null, this);
       	mLoader.initLoader(0, null, this);
		mAdapter = new BookingsListAdapter(getActivity(), R.layout.new_booking_resource_row, null, from, to);
		setListAdapter(mAdapter);
		
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(TAG, "ON CREATE LOADER");
		if (getActivity() != null) {
			String rid = Services.getAppSettings(getActivity(), "resourcelist");
			String selection = "bt."+ContentDescriptor.BookingTime.Cols.RID+" = "+rid+" AND "
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
		Log.d(TAG, "ON LOAD FINISHED");
		try {
			mAdapter.changeCursor(cursor);		
			mAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Log.e(TAG, "Error in Load Finished", e);
		}
		Log.d(TAG, "FINISHED ON LOAD FINISHED");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "ON LOADER RESET");
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}
}
