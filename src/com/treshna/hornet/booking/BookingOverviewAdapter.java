package com.treshna.hornet.booking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.treshna.hornet.R;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

public class BookingOverviewAdapter extends ArrayAdapter<String[]> implements OnClickListener {
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	ArrayList<String[]> resource;
	String mDate;
	private LayoutInflater mInflater;
	private ContentResolver contentResolver; 
	private BookingsOverviewFragment fragment;
	
	public BookingOverviewAdapter(Context context, int layout, ArrayList<String[]> list, String date,  LayoutInflater inflater, BookingsOverviewFragment f) {
		super(context, layout, list);
		
		this.resource = list;
		this.context = context;
		this.mDate = date;
		this.mInflater = inflater;
		this.fragment = f;
		contentResolver = context.getContentResolver();
	}
	

	public void updateDate(String date) {
		mDate = date;
	}
	
	private RelativeLayout setSlotText(View convertView, int layout, String text) {
		RelativeLayout slot = (RelativeLayout) convertView.findViewById(layout);
		slot.setClickable(true);
		slot.setOnClickListener(this);
		TextView timeText1 = (TextView) slot.findViewById(R.id.booking_overview_time);
		//timeText1.setText("5:00");
		timeText1.setText(text);
		
		return slot;
	}
	
	private void resetColor(RelativeLayout layout) {
		View v1 = layout.findViewById(R.id.overview_timeslot_mid1), v2 = layout.findViewById(R.id.overview_timeslot_mid2),
				v3 = layout.findViewById(R.id.overview_timeslot_mid3), v4 = layout.findViewById(R.id.overview_timeslot_mid4);
		setColour(0, v1);
		setColour(0, v2);
		setColour(0, v3);
		setColour(0, v4);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		//efficiency?
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_booking_overview, parent, false);
			
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.booking_resource_name);
			holder.slot1 = setSlotText(convertView, R.id.booking_timeslot_1, "5 am"); 
			holder.slot2 = setSlotText(convertView,R.id.booking_timeslot_2, "6 am");
			holder.slot3 = setSlotText(convertView, R.id.booking_timeslot_3, "7 am");
			holder.slot4 = setSlotText(convertView,R.id.booking_timeslot_4, "8 am");
			holder.slot5 = setSlotText(convertView,R.id.booking_timeslot_5, "9 am");
			holder.slot6 = setSlotText(convertView,R.id.booking_timeslot_6, "10 am");
			holder.slot7 = setSlotText(convertView,R.id.booking_timeslot_7, "11 am");
			holder.slot8 = setSlotText(convertView,R.id.booking_timeslot_8, "12 pm");
			holder.slot9 = setSlotText(convertView,R.id.booking_timeslot_9, "1 pm");
			holder.slot10 = setSlotText(convertView,R.id.booking_timeslot_10, "2 pm");
			holder.slot11 = setSlotText(convertView, R.id.booking_timeslot_11, "3 pm");
			holder.slot12 = setSlotText(convertView,R.id.booking_timeslot_12, "4 pm");
			holder.slot13 = setSlotText(convertView, R.id.booking_timeslot_13, "5 pm");
			holder.slot14 = setSlotText(convertView, R.id.booking_timeslot_14, "6 pm");
			holder.slot15 = setSlotText(convertView,R.id.booking_timeslot_15, "7 pm");
			holder.slot16 = setSlotText(convertView,R.id.booking_timeslot_16, "8 pm");
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			resetColor(holder.slot1);
			resetColor(holder.slot2);
			resetColor(holder.slot3);
			resetColor(holder.slot4);
			resetColor(holder.slot5);
			resetColor(holder.slot6);
			resetColor(holder.slot7);
			resetColor(holder.slot8);
			resetColor(holder.slot9);
			resetColor(holder.slot10);
			resetColor(holder.slot11);
			resetColor(holder.slot12);
			resetColor(holder.slot13);
			resetColor(holder.slot14);
			resetColor(holder.slot15);
			resetColor(holder.slot16);
		}
		
		//doing actual data binding..
		holder.title.setText(resource.get(position)[1]);
		holder.slot1.setTag(new String[] {resource.get(position)[0], String.valueOf(50000)});
		holder.slot2.setTag(new String[] {resource.get(position)[0], String.valueOf(60000)});
		holder.slot3.setTag(new String[] {resource.get(position)[0], String.valueOf(70000)});
		holder.slot4.setTag(new String[] {resource.get(position)[0], String.valueOf(80000)});
		holder.slot5.setTag(new String[] {resource.get(position)[0], String.valueOf(90000)});
		holder.slot6.setTag(new String[] {resource.get(position)[0], String.valueOf(100000)});
		holder.slot7.setTag(new String[] {resource.get(position)[0], String.valueOf(110000)});
		holder.slot8.setTag(new String[] {resource.get(position)[0], String.valueOf(120000)});
		holder.slot9.setTag(new String[] {resource.get(position)[0], String.valueOf(130000)});
		holder.slot10.setTag(new String[] {resource.get(position)[0], String.valueOf(140000)});
		holder.slot11.setTag(new String[] {resource.get(position)[0], String.valueOf(150000)});
		holder.slot12.setTag(new String[] {resource.get(position)[0], String.valueOf(160000)});
		holder.slot13.setTag(new String[] {resource.get(position)[0], String.valueOf(170000)});
		holder.slot14.setTag(new String[] {resource.get(position)[0], String.valueOf(180000)});
		holder.slot15.setTag(new String[] {resource.get(position)[0], String.valueOf(190000)});
		holder.slot16.setTag(new String[] {resource.get(position)[0], String.valueOf(200000)});
		
		Date theDate = Services.StringToDate(mDate, "dd MMM yyyy");
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(theDate);
		cal.set(Calendar.HOUR_OF_DAY, 0); 
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long start,end;
		start = cal.getTime().getTime();
		cal.add(Calendar.MINUTE, 1439);
		end = cal.getTime().getTime();
		
		Cursor cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, "bt."+ContentDescriptor.BookingTime.Cols.RID+" = ? AND bt."
				+ContentDescriptor.BookingTime.Cols.ARRIVAL+" BETWEEN ? AND ? AND "+ContentDescriptor.Booking.Cols.PARENTID+" = 0", 
				new String[] {resource.get(position)[0], String.valueOf(start), String.valueOf(end)}, null);
		ArrayList<int[]> bookingtimes = new ArrayList<int[]>();
		
		while (cur.moveToNext()){
			int[] bookingdetails = new int[2];
			bookingdetails[0] = Integer.parseInt(cur.getString(cur.getColumnIndex(ContentDescriptor.Time.Cols.TIME)).replace(":", ""));
			bookingdetails[1] = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT)); 
			bookingtimes.add(bookingdetails);
		}
		cur.close();
		
		//I need to reset all the colours before doing this.
		//what's the most efficient way to do it?
		
		for (int j = 0; j < bookingtimes.size(); j +=1) {
			int[] booking = bookingtimes.get(j); 
			switch (booking[0]){
			//	  HHmmss
			case (50000):{
				View time = holder.slot1.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (51500):{
				View time = holder.slot1.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (53000):{
				View time = holder.slot1.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (54500):{
				View time = holder.slot1.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (60000):{
				View time = holder.slot2.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (61500):{
				View time = holder.slot2.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (63000):{
				View time = holder.slot2.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (64500):{
				View time = holder.slot2.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (70000):{
				View time = holder.slot3.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (71500):{
				View time = holder.slot3.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (73000):{
				View time = holder.slot3.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (74500):{
				View time = holder.slot3.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (80000):{
				View time = holder.slot4.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (81500):{
				View time = holder.slot4.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (83000):{
				View time = holder.slot4.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (84500):{
				View time = holder.slot4.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (90000):{
				View time = holder.slot5.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (91500):{
				View time = holder.slot5.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (93000):{
				View time = holder.slot5.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (94500):{
				View time = holder.slot5.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (100000):{
				View time = holder.slot6.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (101500):{
				View time = holder.slot6.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (103000):{
				View time = holder.slot6.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (104500):{
				View time = holder.slot6.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (110000):{
				View time = holder.slot7.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (111500):{
				View time = holder.slot7.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (113000):{
				View time = holder.slot7.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (114500):{
				View time = holder.slot7.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (120000):{
				View time = holder.slot8.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (121500):{
				View time = holder.slot8.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (123000):{
				View time = holder.slot8.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (124500):{
				View time = holder.slot8.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (130000):{
				View time = holder.slot9.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (131500):{
				View time = holder.slot9.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (133000):{
				View time = holder.slot9.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (134500):{
				View time = holder.slot9.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (140000):{
				View time = holder.slot10.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (141500):{
				View time = holder.slot10.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (143000):{
				View time = holder.slot10.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (144500):{
				View time = holder.slot10.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (150000):{
				View time = holder.slot11.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (151500):{
				View time = holder.slot11.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (153000):{
				View time = holder.slot11.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (154500):{
				View time = holder.slot11.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (160000):{
				View time = holder.slot12.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (161500):{
				View time = holder.slot12.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (163000):{
				View time = holder.slot12.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (164500):{
				View time = holder.slot12.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (170000):{
				View time = holder.slot13.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (171500):{
				View time = holder.slot13.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (173000):{
				View time = holder.slot13.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (174500):{
				View time = holder.slot13.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (180000):{
				View time = holder.slot14.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (181500):{
				View time = holder.slot14.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (183000):{
				View time = holder.slot14.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (184500):{
				View time = holder.slot14.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (190000):{
				View time = holder.slot15.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (191500):{
				View time = holder.slot15.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (193000):{
				View time = holder.slot15.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (194500):{
				View time = holder.slot15.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			case (200000):{
				View time = holder.slot16.findViewById(R.id.overview_timeslot_mid1);
				setColour(booking[1], time);
				break;
			}
			case (201500):{
				View time = holder.slot16.findViewById(R.id.overview_timeslot_mid2);
				setColour(booking[1], time);
				break;
			}
			case (203000):{
				View time = holder.slot16.findViewById(R.id.overview_timeslot_mid3);
				setColour(booking[1], time);
				break;
			}
			case (204500):{
				View time = holder.slot16.findViewById(R.id.overview_timeslot_mid4);
				setColour(booking[1], time);
				break;
			}
			default:
				//ignore
				break;
			}
		}
		
		return convertView;
	}
	
	private void setColour(int result, View time) {
		switch (result){
		case (7):
		case (8):
			time.setBackgroundColor(context.getResources().getColor(R.color.lightgrey));
			break;
		case (10):
		case (11):
		case (12):
			time.setBackgroundColor(context.getResources().getColor(R.color.wheat));
			break;
		case (20):
		case (21):
		case (30):
			time.setBackgroundColor(context.getResources().getColor(R.color.palegreen));
			break;
		case (4):
		case (5):
			time.setBackgroundColor(context.getResources().getColor(R.color.navy));
			break;
		case (15):
			time.setBackgroundColor(context.getResources().getColor(R.color.orangered));
			break;
		case (9):
			time.setBackgroundColor(context.getResources().getColor(R.color.slategrey));
			break;
		default:
			time.setBackgroundColor(context.getResources().getColor(R.color.overview_timeslot_mid_grey));
		}
	}
	
	@Override
	public void onClick(View v) {
		String [] tag = null; //0 is the resource name/id, 1 is the time we've clicked.
		if (v.getTag() instanceof String[]) {
			tag = (String[]) v.getTag();
		} else {
			return;
		}
		Services.setPreference(context, "resourcelist", tag[0]);
		
		Fragment f = new BookingsSlideFragment();
		Bundle bdl = new Bundle(3);
		
    	bdl.putString("booking_dates", mDate);
    	bdl.putBoolean("hasOverview", true);
    	bdl.putString("selectedTime", tag[1]);
    	f.setArguments(bdl);
		((BookingsListSuperFragment)fragment.getParentFragment()).changeFragment(f, "ResourceFragment");
	}
	
	private static class ViewHolder {
		TextView title;
		RelativeLayout slot1;
		RelativeLayout slot2;
		RelativeLayout slot3;
		RelativeLayout slot4;
		RelativeLayout slot5;
		RelativeLayout slot6;
		RelativeLayout slot7;
		RelativeLayout slot8;
		RelativeLayout slot9;
		RelativeLayout slot10;
		RelativeLayout slot11;
		RelativeLayout slot12;
		RelativeLayout slot13;
		RelativeLayout slot14;
		RelativeLayout slot15;
		RelativeLayout slot16;
		
	}
}
