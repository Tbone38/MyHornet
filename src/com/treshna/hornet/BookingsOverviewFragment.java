package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.treshna.hornet.R.color;

/*
 * 
 */
public class BookingsOverviewFragment extends Fragment implements OnClickListener {
	
	private static ContentResolver contentResolver = null;
    private static Cursor cur = null;
	private static final String TAG = "LastVisitorsFragment";
	private View view;
	private String selectedDate;
	private LayoutInflater mInflater;
	TextView mMonth = null, mDay = null, mYear = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        
        contentResolver = getActivity().getContentResolver();
        if (cur != null) cur.close();
        	
    }
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.new_booking_overview, container, false);
        mInflater = inflater;
        
        RelativeLayout calendarwrapper = (RelativeLayout) view.findViewById(R.id.booking_overview_calendar_wrapper);
        
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	
        	mMonth = new TextView(getActivity());
        	mMonth.setTextSize(18);
        	mMonth.setTextColor(this.getResources().getColor(R.color.android_blue));
        	mMonth.setLayoutParams(params);
        	mMonth.setId(10);
        	mMonth.setGravity(Gravity.CENTER_HORIZONTAL);
        	
        	
        	params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        	params.addRule(RelativeLayout.BELOW, 10);
        	
        	mDay = new TextView(getActivity());
        	mDay.setTextSize(24);
        	mDay.setTextColor(this.getResources().getColor(R.color.android_blue));
        	mDay.setLayoutParams(params);
        	mDay.setId(20);
        	mDay.setGravity(Gravity.CENTER_HORIZONTAL);
        	
        	
        	params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        	params.addRule(RelativeLayout.BELOW, 20);
        	
        	mYear = new TextView(getActivity());
        	mYear.setTextSize(16);
        	mYear.setTextColor(this.getResources().getColor(R.color.android_blue));
        	mYear.setLayoutParams(params);
        	mYear.setId(30);
        	mYear.setGravity(Gravity.CENTER_HORIZONTAL);
        	
	        CalendarView calendar = new CalendarView(getActivity());
	        
	        Calendar cal = Calendar.getInstance(Locale.US);
	        calendar.setDate(cal.getTime().getTime());
	        selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
	        updateDate();
	        
	        params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500);
	        params.addRule(RelativeLayout.BELOW, 30);
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
						getList(mInflater);
						updateDate();
				}
	        	
	        });
	        calendarwrapper.addView(mMonth);
	        calendarwrapper.addView(mDay);
	        calendarwrapper.addView(mYear);
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
							getList(mInflater);
							
						}
        	});
        	calendarwrapper.addView(date);
        }
        
        getList(inflater);
        
        return view;
    }
	
	private void updateDate(){
		if (mMonth != null && mDay != null && mYear != null) {
			mMonth.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "MMM"));
			mDay.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "dd"));
			mYear.setText(Services.dateFormat(selectedDate, "yyyyMMdd", "yyyy"));
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void getList(LayoutInflater inflater){
		
       	 getActivity().setTitle("Bookings Overview");
       	 try {
       		 cur.close();
       	 } catch (Exception e) {
       		 cur = null;
       	 }
       	 cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
		
		 ArrayList<String[]> resource = new ArrayList<String[]>();
		 while (cur.moveToNext()) {
			 resource.add(new String[] {String.valueOf(cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID))),
					 cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)) });
		 }
		 cur.close();
		 
		 LinearLayout list = (LinearLayout) view.findViewById(R.id.booking_resource_list); 
		 list.removeAllViews();
		 for (int i=0; i<resource.size(); i++) {
			 View row = inflater.inflate(R.layout.new_booking_overview_row, null);
			 row.setClickable(true);
			 row.setTag(resource.get(i)[0]);
			 row.setOnClickListener(this);
			 
			 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				 row.setBackground(getResources().getDrawable(R.drawable.button));
			 } else {
				 row.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
			 }
			
			 TextView resourcename = (TextView) row.findViewById(R.id.booking_resource_name);
				resourcename.setText(resource.get(i)[1]);
				
				cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, "bt."+ContentDescriptor.BookingTime.Cols.RID+" = ? AND bt."
						+ContentDescriptor.BookingTime.Cols.ARRIVAL+" = ?", new String[] {resource.get(i)[0],
						selectedDate}, null);
				ArrayList<Integer> bookingtimes = new ArrayList<Integer>();
				
				while (cur.moveToNext()){
					/*for (int column = 0; column < cur.getColumnCount(); column++) {
						try {
							Log.e(TAG, "Column:"+column+" Name:"+cur.getColumnName(column)+" Value:"+cur.getString(column));
						} catch (NullPointerException e) {
							Log.e(TAG, "error at column:"+column);
						}
					}*/
					bookingtimes.add(Integer.parseInt(cur.getString(cur.getColumnIndex(ContentDescriptor.Time.Cols.TIME)).replace(":", "")));
				}
				cur.close();
								
					RelativeLayout timeslot1 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_1);
					TextView timeText1 = (TextView) timeslot1.findViewById(R.id.booking_overview_time);
					timeText1.setText("5:00");

					RelativeLayout timeslot2 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_2);
					TextView timeText2 = (TextView) timeslot2.findViewById(R.id.booking_overview_time);
					timeText2.setText("6:00");
				
					RelativeLayout timeslot3 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_3);
					TextView timeText3 = (TextView) timeslot3.findViewById(R.id.booking_overview_time);
					timeText3.setText("7:00");
				
					RelativeLayout timeslot4 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_4);
					TextView timeText4 = (TextView) timeslot4.findViewById(R.id.booking_overview_time);
					timeText4.setText("8:00");
				
					RelativeLayout timeslot5 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_5);
					TextView timeText5 = (TextView) timeslot5.findViewById(R.id.booking_overview_time);
					timeText5.setText("9:00");
				
					RelativeLayout timeslot6 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_6);
					TextView timeText6 = (TextView) timeslot6.findViewById(R.id.booking_overview_time);
					timeText6.setText("10:00");
				
					RelativeLayout timeslot7 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_7);
					TextView timeText7 = (TextView) timeslot7.findViewById(R.id.booking_overview_time);
					timeText7.setText("11:00");
				
					RelativeLayout timeslot8 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_8);
					TextView timeText8 = (TextView) timeslot8.findViewById(R.id.booking_overview_time);
					timeText8.setText("12:00");
				
					RelativeLayout timeslot9 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_9);
					TextView timeText9 = (TextView) timeslot9.findViewById(R.id.booking_overview_time);
					timeText9.setText("13:00");
				
					RelativeLayout timeslot10 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_10);
					TextView timeText10 = (TextView) timeslot10.findViewById(R.id.booking_overview_time);
					timeText10.setText("14:00");
				
					RelativeLayout timeslot11 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_11);
					TextView timeText11 = (TextView) timeslot11.findViewById(R.id.booking_overview_time);
					timeText11.setText("15:00");
				
					RelativeLayout timeslot12 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_12);
					TextView timeText12 = (TextView) timeslot12.findViewById(R.id.booking_overview_time);
					timeText12.setText("16:00");
				
					RelativeLayout timeslot13 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_13);
					TextView timeText13 = (TextView) timeslot13.findViewById(R.id.booking_overview_time);
					timeText13.setText("17:00");
				
					RelativeLayout timeslot14 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_14);
					TextView timeText14 = (TextView) timeslot14.findViewById(R.id.booking_overview_time);
					timeText14.setText("18:00");
				
					RelativeLayout timeslot15 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_15);
					TextView timeText15 = (TextView) timeslot15.findViewById(R.id.booking_overview_time);
					timeText15.setText("19:00");
				
					RelativeLayout timeslot16 = (RelativeLayout) row.findViewById(R.id.booking_timeslot_16);
					TextView timeText16 = (TextView) timeslot16.findViewById(R.id.booking_overview_time);
					timeText16.setText("20:00");
				
				/* for int time in bookingtime list
				 * 	switch: time
				 * 		case (12:15):
				 * 			//highlight colour
				 */
				for (int j = 0; j < bookingtimes.size(); j +=1) {
					
					switch (bookingtimes.get(j)){
					case (50000):{
						View time = timeslot1.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (51500):{
						View time = timeslot1.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (53000):{
						View time = timeslot1.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (54500):{
						View time = timeslot1.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (60000):{
						View time = timeslot2.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (61500):{
						View time = timeslot2.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (63000):{
						View time = timeslot2.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (64500):{
						View time = timeslot2.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (70000):{
						View time = timeslot3.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (71500):{
						View time = timeslot3.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (73000):{
						View time = timeslot3.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (74500):{
						View time = timeslot3.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (80000):{
						View time = timeslot4.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (81500):{
						View time = timeslot4.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (83000):{
						View time = timeslot4.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (84500):{
						View time = timeslot4.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (90000):{
						View time = timeslot5.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (91500):{
						View time = timeslot5.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (93000):{
						View time = timeslot5.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (94500):{
						View time = timeslot5.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (100000):{
						View time = timeslot6.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (101500):{
						View time = timeslot6.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (103000):{
						View time = timeslot6.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (104500):{
						View time = timeslot6.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (110000):{
						View time = timeslot7.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (111500):{
						View time = timeslot7.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (113000):{
						View time = timeslot7.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (114500):{
						View time = timeslot7.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (120000):{
						View time = timeslot8.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (121500):{
						View time = timeslot8.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (123000):{
						View time = timeslot8.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (124500):{
						View time = timeslot8.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (130000):{
						View time = timeslot9.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (131500):{
						View time = timeslot9.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (133000):{
						View time = timeslot9.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (134500):{
						View time = timeslot9.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (140000):{
						View time = timeslot10.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (141500):{
						View time = timeslot10.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (143000):{
						View time = timeslot10.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (144500):{
						View time = timeslot10.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (150000):{
						View time = timeslot11.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (151500):{
						View time = timeslot11.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (153000):{
						View time = timeslot11.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (154500):{
						View time = timeslot11.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (160000):{
						View time = timeslot12.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (161500):{
						View time = timeslot12.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (163000):{
						View time = timeslot12.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (164500):{
						View time = timeslot12.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (170000):{
						View time = timeslot13.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (171500):{
						View time = timeslot13.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (173000):{
						View time = timeslot13.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (174500):{
						View time = timeslot13.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (180000):{
						View time = timeslot14.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (181500):{
						View time = timeslot14.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (183000):{
						View time = timeslot14.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (184500):{
						View time = timeslot14.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (190000):{
						View time = timeslot15.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (191500):{
						View time = timeslot15.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (193000):{
						View time = timeslot15.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (194500):{
						View time = timeslot15.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (200000):{
						View time = timeslot16.findViewById(R.id.overview_timeslot_mid1);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (201500):{
						View time = timeslot16.findViewById(R.id.overview_timeslot_mid2);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (203000):{
						View time = timeslot16.findViewById(R.id.overview_timeslot_mid3);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					case (204500):{
						View time = timeslot16.findViewById(R.id.overview_timeslot_mid4);
						time.setBackgroundColor(this.getResources().getColor(R.color.gym));
						break;
					}
					default:
						//ignore
						break;
					}
				}
				list.addView(row);
		 }
	 }

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.listRow):{
			String resourceid = (String) v.getTag();
			//new Fragment, replace this fragment.
			//Fragment f = new BookingsSlideFragment();
			Fragment f = new BookingsResourceFragment();
			((BookingsListSuperFragment)this.getParentFragment()).changeFragment(f);
		}
		default:
			break;
		}
		
	}
		    
}

