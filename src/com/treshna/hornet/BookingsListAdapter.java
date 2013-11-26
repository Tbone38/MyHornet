package com.treshna.hornet;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookingsListAdapter extends SimpleCursorAdapter implements OnClickListener {
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	Cursor cursor;
	String mDate;
	private ListView mList;
	private static final String TAG = "BookingsListAdapter";
	
	@SuppressWarnings("deprecation")
	public BookingsListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String date, ListView list) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
		this.cursor = c;
		this.mDate = date;
		this.mList = list;
	}
	
	public void updateDate(String date) {
		mDate = date;
	}
	
	@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);
		mList.setDividerHeight(0);
		View block = (View) rowLayout.findViewById(R.id.booking_resource_colour_block);
		
		switch (cursor.getInt(cursor.getColumnIndex("result"))){
		case (7):
		case (8):
			block.setBackgroundColor(context.getResources().getColor(R.color.lightgrey));
			break;
		case (10):
		case (11):
		case (12):
			block.setBackgroundColor(context.getResources().getColor(R.color.wheat));
			break;
		case (20):
		case (21):
		case (30):
			block.setBackgroundColor(context.getResources().getColor(R.color.palegreen));
			break;
		case (4):
		case (5):
			block.setBackgroundColor(context.getResources().getColor(R.color.navy));
			break;
		case (15):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.orangered));
			break;
		case (9):
			block.setBackgroundColor(context.getResources().getColor(R.color.slategrey));
			break;
		default:
			block.setBackgroundColor(context.getResources().getColor(R.color.member_blue));
		}
		
	
		TextView time_end = (TextView) rowLayout.findViewById(R.id.booking_resource_time_end);
		time_end.setText(cursor.getString(cursor.getColumnIndex("time")).substring(2, 5));
		
		TextView time_start = (TextView) rowLayout.findViewById(R.id.booking_resource_time_start);
		if (Integer.parseInt(cursor.getString(cursor.getColumnIndex("time")).substring(3,5)) == 0) {
			time_start.setText(cursor.getString(cursor.getColumnIndex("time")).substring(0, 2));
		} else {
			time_start.setText("");
		}
		
		if ( !cursor.isNull(7)) { //_id for booking
			TextView name = (TextView) rowLayout.findViewById(R.id.booking_resource_booking_name);
			name.setVisibility(View.INVISIBLE);
			String ntext = null;
			if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))) {
				ntext = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))+" ";
				if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME))) {
					ntext += cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
				}
			}
			
			RelativeLayout background = (RelativeLayout) rowLayout.findViewById(R.id.booking_resource_colour_background);
			background.setBackgroundColor(context.getResources().getColor(R.color.booking_resource_background));
			
			ImageView drawable = (ImageView) rowLayout.findViewById(R.id.booking_resource_drawable);
			drawable.setVisibility(View.VISIBLE);
			if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.BookingTime.Cols.TIMEID)) ==
					cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.STIMEID))) {
				
				switch (cursor.getInt(cursor.getColumnIndex("result"))) {
				case (20):{ //checked in
					drawable.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_previous_item));
					break;
				}
				case (21):{ //checked in late
					drawable.setImageDrawable(context.getResources().getDrawable(R.drawable.glyphicons_clock));
					break;
				}
				case (15):{ //noshow
					drawable.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_warning));
					break;
				}
				default:{
					drawable.setVisibility(View.INVISIBLE);
				}
				}
				
			} else if (cursor.getInt(0) <= cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.ETIMEID))
					&& cursor.getInt(0) > cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.STIMEID))) {
				drawable.setVisibility(View.INVISIBLE);
			} else {
				drawable.setVisibility(View.VISIBLE);
			}
				
			
			
			if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.BookingTime.Cols.TIMEID)) ==
					(cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.STIMEID))+1)) {
			
				switch (cursor.getInt(cursor.getColumnIndex("result"))) {
				case (8):{
					name.setVisibility(View.VISIBLE);
					name.setText("Not Available");
					break;
				}
				case (9):{ //wasn't 9 first assessment?
					name.setVisibility(View.VISIBLE);
					name.setText(cursor.getString(cursor.getColumnIndex("notes"))); //what should this be? membership name ? (notes =17)
					name.setTypeface(null, Typeface.NORMAL);
					break;
				}
				default:{
					name.setVisibility(View.VISIBLE);
					name.setText(cursor.getString(cursor.getColumnIndex("bookingdescription")));
					name.setTypeface(null, Typeface.NORMAL);
				}
				}
				
			} else if (cursor.getInt(0) == cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.ETIMEID)))  {//timeid?
				mList.setDividerHeight(1);
			} else {
				if (ntext != null) {
					if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.BookingTime.Cols.TIMEID)) ==
							cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.STIMEID))) {
						//mList.setDividerHeight(1);
						name.setVisibility(View.VISIBLE);
						name.setText(ntext);
						name.setTypeface(null, Typeface.BOLD);
					} else {
						name.setVisibility(View.INVISIBLE);
						name.setText("");
						mList.setDividerHeight(0);
					}
				} else {
					name.setVisibility(View.INVISIBLE);
					name.setText("");
					mList.setDividerHeight(0);
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
			name.setVisibility(View.INVISIBLE);
			
			ImageView drawable = (ImageView) rowLayout.findViewById(R.id.booking_resource_drawable);
			drawable.setVisibility(View.VISIBLE);
			drawable.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_new));
			
			RelativeLayout background = (RelativeLayout) rowLayout.findViewById(R.id.booking_resource_colour_background);
			background.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
			
			mList.setDividerHeight(1);
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
