package com.treshna.hornet.member;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.booking.BookingAddFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class MemberBookingsFragment extends Fragment implements TagFoundListener, OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	//private MemberActions mActions;
	
	//private static final String TAG = "MemberVisitHistory";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		//mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.fragment_member_details_bookings, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	
	private View setupView() {
		//we inflate the list here.
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.MID+" = ? AND "
		+ContentDescriptor.Booking.Cols.RESULT+" != 5", new String[] {memberID}, ContentDescriptor.Booking.Cols.ARRIVAL+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.booking_list);
		list.removeAllViews();
		
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.row_member_booking, null);
			
			if (cur.getPosition()%2!=0) { //we're doing it backwards ?
				row.setBackgroundColor(Color.WHITE);
			}
			
			TextView date_view = (TextView) row.findViewById(R.id.booking_row_date);
			Date arrival = new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL)));
			date_view.setText(Services.DateToString(arrival)+" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)));
			
			TextView resource_view = (TextView) row.findViewById(R.id.booking_resource_name);
			{
				Cursor cursor = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, new String[] {ContentDescriptor.Resource.Cols.NAME},
						ContentDescriptor.Resource.Cols.ID+" = ?", new String[] 
						{cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RID))}, null);
				if (cursor.moveToFirst()) {
					resource_view.setText(cursor.getString(0));
				}
				cursor.close();
			}
			
			
			TextView name_view = (TextView) row.findViewById(R.id.booking_row_name);
			{ //check the booking type, if it's a class we need to get the class name, other wise use the booking name.
				if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE)) == 0) { //it's a class, get class name.
					int classid = 0;
					
					Cursor cursor = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, new String[] {ContentDescriptor.Booking.Cols.CLASSID},
							ContentDescriptor.Booking.Cols.BID+" = ?", new String[] {cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.PARENTID))},
							null);
					if (cursor.moveToFirst()) {
						classid = cursor.getInt(0);
					} else {} //what do we do if we didn't find the parent id..?
					
					cursor.close();
					
					cursor = contentResolver.query(ContentDescriptor.Class.CONTENT_URI, new String[] {ContentDescriptor.Class.Cols.NAME},
							ContentDescriptor.Class.Cols.CID+" = ?", new String[] {String.valueOf(classid)}, null);
					if (cursor.moveToFirst()) {
						name_view.setText(cursor.getString(0));
					}
					
				} else { //it's a booking.
					name_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKING)));
				}
			}
			
			//change this from endtime to duration.
			TextView status_view = (TextView) row.findViewById(R.id.booking_row_status);
			{ //calculating the duration!
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
				java.util.Date start, end;
				long interval = 0;
				long HOUR = 3600000;
				
				try {
					start = format.parse(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				
				try {
					end = format.parse(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)));
				} catch (ParseException e) {
					throw new RuntimeException (e);
				}
				
				interval = end.getTime() - start.getTime();
				if (interval < HOUR) { // less than an hour.
					//show the minute amount!
					double minutes = Double.valueOf(new DecimalFormat("#").format(
							((interval/1000)/60)));
					status_view.setText((int)minutes+" mins");
				}
				else {
					double hours = Double.valueOf(new DecimalFormat("#").format(
							(((interval/1000)/60)/60)));
					double minutes = Double.valueOf(new DecimalFormat("#").format(
							(((interval - ((int)hours*60*60*1000))/1000)/60)));

					if ((int) minutes != 0 && (int) hours > 1) {
						status_view.setText((int) hours+" hours, "+(int)minutes+" mins");
					} else 
					if ((int) minutes != 0 && (int) hours == 1) {
						status_view.setText((int) hours+" hour, "+(int)minutes+" mins");
					} else 
					if ((int) hours == 1){
						status_view.setText((int) hours+" hour");
					} else {
						status_view.setText((int) hours+" hours");
					}
				}
			}
			
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
			
		LinearLayout addBooking = (LinearLayout) view.findViewById(R.id.button_add_booking);
		addBooking.setOnClickListener(this);

		return view;
	}

	@Override
	public boolean onNewTag(String serial) {
		//return mActions.onNewTag(serial);
		return false;
	}

	@Override
	public void onClick(View v) {
		Fragment fragment = new BookingAddFragment();
		((MainActivity)getActivity()).changeFragment(fragment, "bookingAdd");
	}	
}