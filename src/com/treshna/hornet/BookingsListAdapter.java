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

	@SuppressWarnings("deprecation")
	public BookingsListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
		this.cursor = c;
		
	}
	
	@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);
		/*for (int j=0;j<cursor.getColumnCount();j+=1){
			System.out.print("\n column:"+j+" title: "+cursor.getColumnName(j)+" Value:"+cursor.getString(j));
		}*/
		//System.out.print(cursor.getInt(15));
		switch (cursor.getInt(cursor.getColumnIndex("result"))){
		case (7):
		case (8):
			rowLayout.setBackgroundColor(context.getResources().getColor(R.color.lightgrey));
			break;
		case (10):
		case (11):
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
		
		TextView time = (TextView) rowLayout.findViewById(R.id.bookingstart);
		time.setText(cursor.getString(cursor.getColumnIndex("time")));
		//time.setGravity(Gravity.CENTER_VERTICAL);
		
		//fill in non-empty rows with details.
		if ( !cursor.isNull(7)) { //_id for booking
			TextView name = (TextView) rowLayout.findViewById(R.id.bookingname);
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
			
			
			TextView booking = (TextView) rowLayout.findViewById(R.id.bookingtype);
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
			TextView name = (TextView) rowLayout.findViewById(R.id.bookingname);
			name.setText("");
			TextView booking = (TextView) rowLayout.findViewById(R.id.bookingtype);
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
		case(101):{ //addphoto
			String id = v.getTag().toString();
	        System.out.println("Add Photo for ID: "+id+" Pushed");
	        Intent camera = new Intent(context, CameraWrapper.class);
	        camera.putExtra(EXTRA_ID,id);
	        context.startActivity(camera);
	        break;} 
		case(R.id.listRow):{
			System.out.println("**Row Selected, Displaying More Info:");
			ArrayList<String> tagInfo;
			if (v.getTag() instanceof ArrayList<?>) {
				tagInfo = (ArrayList<String>) v.getTag();
			} else {
				break;
			}
			
			//if (Integer.valueOf(tagInfo.get(0)) == 1) {
				Intent intent = new Intent(context, BookingPage.class);
				intent.putStringArrayListExtra(Services.Statics.KEY, tagInfo);
				//intent.putExtra(Services.Statics.KEY,tagInfo.get(1));
				context.startActivity(intent);
			//} else {
				//add member!
				//System.out.println("**Add member Selected");
				//Intent intent = new Intent(context, BookingDetails.class);
				//intent.putExtra(Services.Statics.KEY, "-1");
				//context.startActivity(intent);
			//}
			break; }
		}	
    }
}
