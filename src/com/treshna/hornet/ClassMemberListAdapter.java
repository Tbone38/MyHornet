package com.treshna.hornet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

//TODO:
//		- NFC handling
//		- Adding members to class.


public class ClassMemberListAdapter extends SimpleCursorAdapter implements CompoundButton.OnCheckedChangeListener{
	
	Context context;
	ContentResolver contentResolver;
	String stime;
	String sdate;
	public static final String TAG = "com.treshna.hornet.ClassMemberListAdapter";
	private Cursor cur;
	
	private int mLayout;
	
	@SuppressWarnings("deprecation")
	public ClassMemberListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
		contentResolver = context.getContentResolver();
		cur = c;
		mLayout = layout;
	}
	
	
	/*@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);
		int pos;
		TextView membername;
		CheckBox checkin;
		String fname, sname;
		ArrayList<String> tag;
		
		pos = cursor.getPosition();
		Log.v(TAG, "Cursor Position:"+pos);
		stime = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
		sdate = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
		
		membername = (TextView) rowLayout.findViewById(R.id.memberName);
		fname = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME));
		sname = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
		membername.setText(fname+" "+sname);
		
		//work out check-in based off of result or check-in (timestamp, if null not checked).
		
		checkin = (CheckBox) rowLayout.findViewById(R.id.classMemberCheckin);
		checkin.setChecked(false);
		if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN))) {
			checkin.setChecked(true);
			Log.v(TAG, "Checked = true for Member:"+fname+" "+sname);
			Log.v(TAG, "With Value:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN)));
		}
		
		tag = new ArrayList<String>();
		tag.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
		tag.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Booking.Cols.ID)));
		checkin.setTag(tag);
		checkin.setOnCheckedChangeListener(this);
	}*/
	
	
	@Override
	public void changeCursor(Cursor c) {
		cur = c;
		super.changeCursor(c);
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			
		LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
		View rowLayout = inflater.inflate(mLayout, parent, false);
		
		
		TextView membername;
		CheckBox checkin;
		String fname, sname;
		ArrayList<String> tag;
		
		//pos = cursor.getPosition();
		cur.moveToPosition(position);
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
			Log.v(TAG, "Checked = true for Member:"+fname+" "+sname);
			Log.v(TAG, "With Value:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN)));
		}
		
		tag = new ArrayList<String>();
		tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
		tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ID)));
		checkin.setTag(tag);
		checkin.setOnCheckedChangeListener(this);
			
		return rowLayout;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ContentValues values;
		String bookingid, rowid;
		ArrayList<String> tag = null;
		
		if (buttonView.getTag() instanceof ArrayList<?> ) {
			tag = (ArrayList<String>) buttonView.getTag();
		}
		bookingid = tag.get(0);
		Log.v(TAG, "Check Box Tag: BookingID:"+bookingid);
		rowid = tag.get(1);
		Log.v(TAG,"Check Box Tag: rowid:"+rowid);
		
		
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
			
			values.put(ContentDescriptor.Booking.Cols.CHECKIN, new Date().getTime());
		} else { //unchecked, remove checkin time.
			values = new ContentValues();
			values.putNull(ContentDescriptor.Booking.Cols.CHECKIN);
			values.put(ContentDescriptor.Booking.Cols.RESULT, 20);
		}
		values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
		contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
				new String[] {bookingid});
		
		/*********Add to pendingUploads********/
		//TODO: look into this; (new booking vs updated booking).
		values = new ContentValues();
		values.put(ContentDescriptor.PendingUploads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Booking.getKey());
		values.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);
		contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
	}
}