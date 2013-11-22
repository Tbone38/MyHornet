package com.treshna.hornet;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookingsListAdapter extends SimpleCursorAdapter implements OnClickListener {
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	Cursor cursor;
	String mDate;

	@SuppressWarnings("deprecation")
	public BookingsListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String date) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
		this.cursor = c;
		this.mDate = date;
	}
	
	public void updateDate(String date) {
		mDate = date;
	}
	
	@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);

		switch (cursor.getInt(cursor.getColumnIndex("result"))){
		case (7):
		case (8):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.lightgrey));
			break;
		case (10):
		case (11):
		case (12):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.wheat));
			break;
		case (20):
		case (21):
		case (30):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.palegreen));
			break;
		case (4):
		case (5):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.navy));
			break;
		case (15):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.orangered));
			break;
		case (9):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.slategrey));
			break;
		default:
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
		}
		
	
		TextView time = (TextView) rowLayout.findViewById(R.id.booking_resource_time);
		time.setText(cursor.getString(cursor.getColumnIndex("time")).substring(0, 5));
		
		if ( !cursor.isNull(7)) { //_id for booking
			TextView name = (TextView) rowLayout.findViewById(R.id.booking_resource_booking_name);
			name.setVisibility(View.VISIBLE);
			String ntext = null;
			if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))) {
				ntext = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))+" ";
				if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME))) {
					ntext += cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
				}
			}
			if (ntext != null) {
				name.setText(ntext);
			} else {
				name.setText("");
			}
			
			TextView booking = (TextView) rowLayout.findViewById(R.id.booking_resource_booking_time);
			switch (cursor.getInt(cursor.getColumnIndex("result"))) {
			case (8):{
				booking.setText("Not Available");
				break;
			}
			case (9):{ //wasn't 9 first assessment?
				booking.setText(cursor.getString(cursor.getColumnIndex("notes"))); //what should this be? membership name ? (notes =17)
				break;
			}
			default:{
				booking.setText(cursor.getString(cursor.getColumnIndex("bookingdescription")));
			}
			}
			
			ArrayList<String> tagInfo = new ArrayList<String>();
			tagInfo.add(String.valueOf(1));
			tagInfo.add(cursor.getString(cursor.getColumnIndex("bookingid"))); //booking bookingid //14?
			rowLayout.setTag(tagInfo);
			rowLayout.setClickable(true);
			rowLayout.setOnClickListener(this);
		} else {
			TextView name = (TextView) rowLayout.findViewById(R.id.booking_resource_booking_name);
			name.setVisibility(View.GONE);
			TextView booking = (TextView) rowLayout.findViewById(R.id.booking_resource_booking_time);
			booking.setText("Click to Add Booking");
			ArrayList<String> tagInfo = new ArrayList<String>();
			tagInfo.add(String.valueOf(0));
			tagInfo.add(cursor.getString(0)); //time _id
			rowLayout.setTag(tagInfo);
			rowLayout.setClickable(true);
			rowLayout.setOnClickListener(this);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * Handles Photos
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		//do Photo taking stuff here.
		switch(v.getId()){ 
		case(R.id.listRow):{
			System.out.println("**Row Selected, Displaying More Info:");
			ArrayList<String> tagInfo;
			if (v.getTag() instanceof ArrayList<?>) {
				tagInfo = (ArrayList<String>) v.getTag();
			} else {
				break;
			}
			Intent intent = new Intent(context, BookingPage.class);
			tagInfo.add(mDate);
			intent.putStringArrayListExtra(Services.Statics.KEY, tagInfo);
			context.startActivity(intent);
			break; 
			}
		}	
    }
}
