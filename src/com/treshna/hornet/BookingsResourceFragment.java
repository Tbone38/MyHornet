package com.treshna.hornet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;


public class BookingsResourceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	
    private static final String TAG = "BookingsResourceFragment";
    private View view;
    private String selectedDate;
    private LoaderManager mLoader;
    private BookingsListAdapter mAdapter;
    private ContentResolver mResolver;
    private Cursor cur;
    private CalendarView calendar;
    private boolean hasOverview = false; 
    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ON CREATE");
        Services.setContext(getActivity());
        mLoader = getLoaderManager();
        mResolver = this.getActivity().getContentResolver();
        
        selectedDate = Services.dateFormat(new Date(this.getArguments().getLong("date")).toString(), 
        		"EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
        hasOverview = this.getArguments().getBoolean("hasOverview");
    }
	
	public boolean hasOverView(){
		return hasOverview;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		Log.d(TAG, "ON CREATE VIEW");
		try {
			view = inflater.inflate(R.layout.booking_resource, container, false);
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
	
	
	@SuppressLint("NewApi")
	private void setupCalendar(){
		Log.d(TAG, "SETUP CALENDAR");
		RelativeLayout calendarwrapper = (RelativeLayout) view.findViewById(R.id.booking_resource_calendar_wrapper);
		calendarwrapper.setGravity(Gravity.CENTER);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	//this calendar widget breaks things when view is in portrait mode. Seems to be a bug with something,
        	//not sure where the bug is yet.
        	if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
	        	CalendarView calendar = null;
	        	calendar = new CalendarView(getActivity());
		        		        
		        Calendar cal = Calendar.getInstance(Locale.US);
		        if (selectedDate == null) {
			        calendar.setDate(cal.getTime().getTime());
			        selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
		        } else {
		        	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
		        	Long time;
		        	try {
		        		time = format.parse(selectedDate).getTime();
		        	} catch (ParseException e) {
		        		time = new Date().getTime();
		        	}
		        	calendar.setDate(time);
		        }
		        calendar.setMinimumHeight(300);
		        cal.add(Calendar.MONTH, -1);
		        calendar.setMinDate(cal.getTimeInMillis());
		        cal.add(Calendar.MONTH, 2);
		        calendar.setMaxDate(cal.getTimeInMillis());
		        
		        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500);
		        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(), 200);
		        calendar.setLayoutParams(params);
		        
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
		       	calendarwrapper.addView(calendar);
		        
        	} else {
        		int year, month, day;
        		DatePicker date = new DatePicker(getActivity());
        		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        		date.setLayoutParams(params);
        		
            	Calendar cal = Calendar.getInstance(Locale.US);
            	if (selectedDate == null) {
            		year = cal.get(Calendar.YEAR);
        			month = cal.get(Calendar.MONTH);
        			day = cal.get(Calendar.DAY_OF_MONTH);
			        selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
		        } else {
		        	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
		        	Long time;
		        	try {
		        		time = format.parse(selectedDate).getTime();
		        	} catch (ParseException e) {
		        		time = new Date().getTime();
		        	}
		        	cal.setTimeInMillis(time);
		        	year = cal.get(Calendar.YEAR);
        			month = cal.get(Calendar.MONTH);
        			day = cal.get(Calendar.DAY_OF_MONTH);
		        }
            	date.setCalendarViewShown(false);
            	date.init(year, month, day, 
            			new OnDateChangedListener() {
    						@Override
    						public void onDateChanged(DatePicker view, int year,
    								int monthOfYear, int dayOfMonth) {
    							Calendar cal = Calendar.getInstance();
    							cal.set(year, monthOfYear, dayOfMonth);
    							Log.d(TAG, "Calendar Time:"+cal.getTime());
    							Date date = cal.getTime();
    							selectedDate = Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
    							updateSelection(-1);
    						}
            	});
            	calendarwrapper.addView(date);
        	}
	        
        } else {
        	int year, month, day;
        	DatePicker date = new DatePicker(getActivity());
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    		date.setLayoutParams(params);
        	Calendar cal = Calendar.getInstance(Locale.US);
        	if (selectedDate == null) {
        		year = cal.get(Calendar.YEAR);
    			month = cal.get(Calendar.MONTH);
    			day = cal.get(Calendar.DAY_OF_MONTH);
		        selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
	        } else {
	        	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
	        	Long time;
	        	try {
	        		time = format.parse(selectedDate).getTime();
	        	} catch (ParseException e) {
	        		time = new Date().getTime();
	        	}
	        	cal.setTimeInMillis(time);
	        	year = cal.get(Calendar.YEAR);
    			month = cal.get(Calendar.MONTH);
    			day = cal.get(Calendar.DAY_OF_MONTH);
	        }
        	date.setCalendarViewShown(false);
        	date.init(year, month, day, 
        			new OnDateChangedListener() {
						@Override
						public void onDateChanged(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Calendar cal = Calendar.getInstance();
							cal.set(year, monthOfYear, dayOfMonth);
							Log.d(TAG, "Calendar Time:"+cal.getTime());
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
		 	mAdapter.updateDate(selectedDate);
    }
	
	private void setupList() {
		Log.d(TAG, "SETTING UP LIST");
		String[] from = {ContentDescriptor.Time.Cols.TIME};
       	int[] to = {R.id.booking_resource_time};
  
		//mLoader.restartLoader(0, null, this);
       	mLoader.initLoader(0, null, this);
		mAdapter = new BookingsListAdapter(getActivity(), R.layout.booking_resource_row, null, from, to, selectedDate);
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
