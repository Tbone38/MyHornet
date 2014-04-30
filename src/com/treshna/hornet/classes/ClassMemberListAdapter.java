package com.treshna.hornet.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.treshna.hornet.R;
import com.treshna.hornet.R.color;
import com.treshna.hornet.R.id;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Booking;
import com.treshna.hornet.sqlite.ContentDescriptor.Booking.Cols;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

//TODO:
//		- NFC handling
//		- Adding members to class.


public class ClassMemberListAdapter extends SimpleCursorAdapter implements CompoundButton.OnCheckedChangeListener,
		View.OnClickListener {
	
	Context context;
	ContentResolver contentResolver;
	String stime;
	String sdate;
	public static final String TAG = "com.treshna.hornet.ClassMemberListAdapter";
	private Cursor cur;
	
	private String deleteid;
	
	
	@SuppressWarnings("deprecation")
	public ClassMemberListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
		contentResolver = context.getContentResolver();
		cur = c;
	}
	
	
	@Override
	public void changeCursor(Cursor c) {
		cur = c;
		super.changeCursor(c);
	}
	
	@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);
		cur = cursor;
		TextView membername;
		CheckBox checkin;
		String fname, sname;
		ArrayList<String> tag;
		
		//pos = cursor.getPosition();
		
		//Log.v(TAG, "Cursor Position:"+pos);
		stime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
		sdate = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
		
		membername = (TextView) rowLayout.findViewById(R.id.memberName);
		fname = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME));
		sname = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
		membername.setText(fname+" "+sname);
		
		//work out check-in based off of result or check-in (timestamp, if null not checked).
		
		checkin = (CheckBox) rowLayout.findViewById(R.id.classMemberCheckin);
		checkin.setChecked(false);
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN))) {
			checkin.setChecked(true);
			//Log.v(TAG, "Checked = true for Member:"+fname+" "+sname);
			//Log.v(TAG, "With Value:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN)));
		}
		
		tag = new ArrayList<String>();
		tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
		checkin.setTag(tag);
		checkin.setOnCheckedChangeListener(this);
		
		ImageView cancel_booking = (ImageView) rowLayout.findViewById(R.id.classMemberCancel);
		cancel_booking.setColorFilter(Services.ColorFilterGenerator.setColour(context.getResources().getColor(R.color.visitors_red)));
		cancel_booking.setClickable(true);
		tag = new ArrayList<String>();
		tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
		cancel_booking.setTag(tag);
		cancel_booking.setOnClickListener(this);
			
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ContentValues values;
		String bookingid;
		ArrayList<String> tag = null;
		
		if (buttonView.getTag() instanceof ArrayList<?> ) {
			tag = (ArrayList<String>) buttonView.getTag();
		}
		bookingid = tag.get(0);
		
		if (isChecked) {//is checked, set checkin time.
			long tenminutes = 600000;
			Date now, start = null;
			SimpleDateFormat format;
			
			format = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.US);
			try {
				start = format.parse(sdate+" "+stime);
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			now = new Date();
			values = new ContentValues();
			
			if (now.getTime() > (start.getTime()+tenminutes)) { //greater than 10 minutes after start.
				values.put(ContentDescriptor.Booking.Cols.RESULT, 21);
			} else {
				values.put(ContentDescriptor.Booking.Cols.RESULT, 20);
			}
			
			values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
			values.put(ContentDescriptor.Booking.Cols.CHECKIN, new Date().getTime());
		} else { //unchecked, remove checkin time.
			values = new ContentValues();
			values.putNull(ContentDescriptor.Booking.Cols.CHECKIN);
			values.put(ContentDescriptor.Booking.Cols.RESULT, 20);
		}
		values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
		contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
				new String[] {bookingid});
		
	}


	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.classMemberCancel):{
			ArrayList<String> tag = null;
			
			if (v.getTag() instanceof ArrayList<?> ) {
				tag = (ArrayList<String>) v.getTag();
			}
			deleteid = tag.get(0);
			
			//show a popup before we delete stuff!.
			Cursor cur2 = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, new String[] {ContentDescriptor.Booking.Cols.FNAME,
					ContentDescriptor.Booking.Cols.SNAME}, ContentDescriptor.Booking.Cols.BID+" = ?",new String[] {deleteid}, null);
			if (!cur2.moveToFirst()) {
				break;
			}
			String membername = cur2.getString(cur2.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))+" "
					+cur2.getString(cur2.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Confirm...");
			builder.setMessage("Are you sure you want to remove "+membername+" from the class?");
			builder.setNegativeButton("Cancel", null);
			builder.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.Booking.Cols.RESULT, 5);
					values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
					
					contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
							new String[] {deleteid});
					
					Cursor cur2 = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, new String[] {ContentDescriptor.Booking.Cols.PARENTID},
							ContentDescriptor.Booking.Cols.BID+" = ?",new String[] {deleteid}, null);
					if (!cur2.moveToFirst()) {
						return;
					}
					
					String classid = cur2.getString(cur2.getColumnIndex(ContentDescriptor.Booking.Cols.PARENTID));
					Intent bcIntent = new Intent();
					bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
					bcIntent.putExtra(Services.Statics.IS_CLASSSWIPE, classid);
					context.sendBroadcast(bcIntent);
					
				}
			});
			builder.show();
			break;
		}
		}
		
	}
}