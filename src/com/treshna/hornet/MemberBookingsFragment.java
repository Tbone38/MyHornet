package com.treshna.hornet;

import java.sql.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.treshna.hornet.MainActivity.TagFoundListener;


public class MemberBookingsFragment extends Fragment implements TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	//private static final String TAG = "MemberVisitHistory";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_bookings, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupView() {
		//we inflate the list here.
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.MID+" = ? AND "
		+ContentDescriptor.Booking.Cols.RESULT+" != 5", new String[] {memberID}, ContentDescriptor.Booking.Cols.ARRIVAL+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.booking_list);
		list.removeAllViews();
		
		if (cur.getCount() <= 0) {
			TextView heading = (TextView) view.findViewById(R.id.member_booking_H);
			heading.setText("No Recent Bookings");
		}
		
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.member_booking_row, null);
			
			if (cur.getPosition()%2!=0) { //we're doing it backwards ?
				row.setBackgroundColor(Color.WHITE);
			}
			
			TextView date_view = (TextView) row.findViewById(R.id.booking_row_date);
			Date arrival = new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL)));
			date_view.setText(Services.DateToString(arrival)+" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)));
			
			TextView name_view = (TextView) row.findViewById(R.id.booking_row_name);
			name_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKING)));
			
			TextView status_view = (TextView) row.findViewById(R.id.booking_row_status);
			status_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)));
			
			View colour_block = (View) row.findViewById(R.id.member_booking_colour_block);
			switch (cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT))){
			case (20):
			case (21):{
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_green));
				break;
			}
			case (15):{
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_red));
				break;
			}
			default:
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.wheat));
				break;
			}
			list.addView(row);
		}
				
		mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public boolean onNewTag(String serial) {
		return mActions.onNewTag(serial);
	}	
}