package com.treshna.hornet;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.treshna.hornet.R.color;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RelativeLayout.LayoutParams;

public class BookingsResourceFragment extends ListFragment {
	
    private static final String TAG = "BookingsResourceFragment";
    private View view;
    private String selectedDate;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		view = inflater.inflate(R.layout.new_booking_resource, container, false);
		setupCalendar();
		return view;
		
	}
	
	@SuppressLint("NewApi")
	private void setupCalendar(){
RelativeLayout calendarwrapper = (RelativeLayout) view.findViewById(R.id.booking_resource_calendar_wrapper);
        
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        CalendarView calendar = new CalendarView(getActivity());
	        
	        Calendar cal = Calendar.getInstance(Locale.US);
	        calendar.setDate(cal.getTime().getTime());
	        selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
	        
	        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500);
	        calendar.setLayoutParams(params);
	        calendar.setShowWeekNumber(false);
	        calendar.setFocusedMonthDateColor(color.member_blue);
	        calendar.setOnDateChangeListener(new OnDateChangeListener() {
				@Override
				public void onSelectedDayChange(CalendarView view, int year,
						int month, int dayOfMonth) {
						Calendar cal = Calendar.getInstance();
						cal.set(year, month, dayOfMonth);
						Date date = cal.getTime();
						selectedDate = Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
				}
	        	
	        });
	        calendarwrapper.addView(calendar);
	        
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
						}
        	});
        	calendarwrapper.addView(date);
        }
	}
}
