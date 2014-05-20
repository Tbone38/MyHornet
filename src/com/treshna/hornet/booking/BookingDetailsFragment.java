package com.treshna.hornet.booking;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.R.color;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

public class BookingDetailsFragment extends Fragment implements OnClickListener {
	
	private Cursor cur;
	private ContentResolver contentResolver;
	private String bookingID;
	private String bookingName;
	private String memberID;
	private String msID;
	private Context ctx;
	private String bookingtype;
	private String resourcename;
	private String date;
	private String starttime;
	private String endtime;
	
	private RadioGroup rg;
	private View rescheduleView;
	private LayoutInflater mInflater;
	
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 mInflater = inflater;
		 ctx = getActivity();
		 bookingID = getArguments().getString(Services.Statics.KEY);
		 System.out.print("\n\nBOOKINGID:"+bookingID+"\n\n");
		 contentResolver = getActivity().getContentResolver();
		 View page =inflater.inflate(R.layout.fragment_booking_details, container, false); 
		 setupView(page);
		 return page;
	 }
	 
	@Override
	public void onStop(){
		super.onStop();
		View page = getView();
		EditText notes = (EditText) page.findViewById(R.id.bookingnotes);
		if (notes.getEditableText().toString().compareTo("") !=0) {
			ContentValues values = new ContentValues();
			values.put(ContentDescriptor.Booking.Cols.NOTES, notes.getEditableText().toString());
			contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
					new String[] {bookingID});
		}
	}

	
	@SuppressLint("NewApi")
	private void setupView(View page) {
		String bookingtypeid = null;
		
		cur = contentResolver.query(ContentDescriptor.Booking.BOOKING_TIME_URI,null, "b."+ContentDescriptor.Booking.Cols.BID+"= "+bookingID, null, null);
		
		cur.moveToFirst();
		msID = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.MSID));

		if (cur.getCount() <= 0) {
			return;
		}
		bookingtypeid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE));
		
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))){
			bookingName = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME));
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME))){
				bookingName = bookingName +" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
			}
		}
		TextView name = (TextView) page.findViewById(R.id.bookingname);
		name.setText(bookingName);
		
		date = Services.DateToString(new Date(Long.parseLong(cur.getString(cur.getColumnIndex(ContentDescriptor.BookingTime.Cols.ARRIVAL)))));
		starttime = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)), "HH:mm:ss", "hh:mmaa");
		endtime = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)), "HH:mm:ss", "hh:mmaa");
		
		TextView time = (TextView) page.findViewById(R.id.bookingtime);
		time.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)), "HH:mm:ss", "hh:mmaa")+" - " //start time is pos 15.
				+Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)), "HH:mm:ss", "hh:mmaa"));
		
		TextView date_view = (TextView) page.findViewById(R.id.bookingdate);
		date_view.setText(date);

		EditText notes = (EditText) page.findViewById(R.id.bookingnotes);
		notes.setMinLines(2);
		notes.setMaxLines(5);
		notes.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.NOTES)));
		
		memberID = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.MID));
		
		int resourceid = cur.getInt(cur.getColumnIndex(ContentDescriptor.BookingTime.Cols.RID));
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE))) {//bookingtypid, if null then this is an appointment?
			cur.close();
			cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID+" = "
					+resourceid, null, null);
			cur.moveToFirst();
			TextView resource = (TextView) page.findViewById(R.id.bookingresourcename);
			resourcename = cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME));
			resource.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME))+" - "
					+cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.RTNAME)));
			cur.close();
			
			cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.BTID+" = "
					+bookingtypeid, null, null);
			cur.moveToFirst();
			int type = 0;
			if (cur.getCount()> 0 && !cur.isNull(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID))) 
				type = cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)); // ?
			cur.close();
			
			Spinner typespinner = (Spinner) page.findViewById(R.id.bookingselect);
			cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, null, null, null);
			
			List<String> typelist = new ArrayList<String>();
			int i = 0;
			int pos = 0;
			while (cur.moveToNext()) {
				if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)) > 0) {
					typelist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.NAME)));
				}
				if (type == cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID))) {
					pos = i;
					bookingtype = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.NAME));
				}
				i +=1;
			}
			if (pos >= typelist.size()) pos = typelist.size() -1;
			
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx,
					android.R.layout.simple_spinner_item, typelist);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				typespinner.setAdapter(dataAdapter);
				typespinner.setSelection(pos);
				
				typespinner.setOnItemSelectedListener(new OnItemSelectedListener(){
					@Override
					public void onItemSelected(AdapterView<?> parent, View view,
							int pos, long id) {
						// set the booking type, and update table;
						cur.close();
						cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.ID+" = "+(pos+1), null, null);
						cur.moveToFirst();
						int newbookingtypeid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)); //6
						cur.close();
						
						cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, new String[] {
								ContentDescriptor.Booking.Cols.BOOKINGTYPE}, ContentDescriptor.Booking.Cols.BID+" = "+bookingID,
								null, null);
						cur.moveToFirst();
						int oldbookingtypeid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE));
						cur.close();
						
						if (oldbookingtypeid != newbookingtypeid) {
							ContentValues values = new ContentValues();
							values.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, newbookingtypeid);
							values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
							contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = "+bookingID, null);
						}
					}
	
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						//leave alone (doesn't matter?)
					}});
			LinearLayout cancel = (LinearLayout) page.findViewById(R.id.button_booking_cancel);
			cancel.setClickable(true);
			cancel.setOnClickListener(this);
			LinearLayout checkin = (LinearLayout) page.findViewById(R.id.button_booking_checkin);
			checkin.setClickable(true);
			checkin.setOnClickListener(this);
			LinearLayout noshow = (LinearLayout) page.findViewById(R.id.button_booking_noshow);
			noshow.setClickable(true);
			noshow.setOnClickListener(this);
			LinearLayout reschedule = (LinearLayout) page.findViewById(R.id.button_booking_reschedule);
			reschedule.setClickable(true);
			reschedule.setOnClickListener(this);
			/*TextView reschedule = (TextView) page.findViewById(R.id.button_booking_reschedule_text);
			reschedule.setTextColor(color.grey);*/
			
			cur.close();
			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.MID+" = "+memberID, null, null);
			LinearLayout sms = (LinearLayout) page.findViewById(R.id.button_booking_sms);
			String smsno = "";
			cur.moveToFirst();
			if (cur.getCount()>0) {
				smsno = (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)))?
						cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)): "";
				sms.setTag(smsno);
				if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL))) {
					if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)).compareTo("") != 0) {
						sms.setTag(smsno);
						sms.setClickable(true);
						sms.setOnClickListener(this);
					} else {
						sms.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
						TextView smstext = (TextView)sms.findViewById(R.id.button_booking_sms_text);
						smstext.setTextColor(color.grey);
						sms.setClickable(false);
					}
				}else {
					sms.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
					TextView smstext = (TextView)sms.findViewById(R.id.button_booking_sms_text);
					smstext.setTextColor(color.grey);
					sms.setClickable(false);
				}
				/*for (int l=0; l<cur.getColumnCount(); l+=1){
					System.out.print("\nColumn: "+l+"  Name:"+cur.getColumnName(l)+"  Value:"+cur.getString(l));
				}*/
				LinearLayout call = (LinearLayout) page.findViewById(R.id.button_booking_call);
				// onclick, alert dialog box, select * number to call ?
				String[] callNo = new String[3];
				int l = 0;
				for (int k=7;k<=9;k +=1) { //give options for number to call
					if (!cur.isNull(k)){
						if (cur.getString(k).compareTo(" ") != 0) {
							String pht = "";
							switch (k){
							case (7):{
								pht = "Home";
								break;
							} case (8):{
								pht = "Cell";
								break;
							} case (9):{
								pht = "Work";
								break;
							}
							}
							callNo[l] = pht+" - "+cur.getString(k);
							l +=1;
						}
					}
				}
				if (callNo[0].isEmpty() && callNo[1].isEmpty() && callNo[2].isEmpty()) {
					call.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
					TextView calltext = (TextView) call.findViewById(R.id.button_booking_call_text);
					calltext.setTextColor(color.grey);
					call.setClickable(false);
				} else {
					call.setTag(callNo);
					call.setClickable(true);
					call.setOnClickListener(this);
				}
			}
		} else {
			Spinner typespinner = (Spinner) page.findViewById(R.id.bookingselect);
			typespinner.setVisibility(View.GONE);
			TextView bookingtype = (TextView) page.findViewById(R.id.bookingtype);
			bookingtype.setVisibility(View.GONE);
			
		}
		
		cur.close();
		if (msID != null) {
			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MSID+" = ?",
					new String[] {msID}, null);
			
			TextView programme = (TextView) page.findViewById(R.id.bookingmembership);
			if (cur.getCount() >0){
				cur.moveToFirst();
				programme.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			} else {
				cur.close();
				programme.setVisibility(View.GONE);
			}
		} else {
			TextView programme = (TextView) page.findViewById(R.id.bookingmembership);
			programme.setVisibility(View.GONE);
		}
		
		//SHOW IMAGE;
				String imgDir = getActivity().getExternalFilesDir(null)+"/0_"+memberID+".jpg"; //cursor.getColumnIndex(ContentDescriptor.Member.Cols.MID)
				File imgFile = new File(imgDir);
				ImageView imageView = (ImageView) page.findViewById(R.id.image);
				if (imgFile.exists() == true){
					imageView.setVisibility(View.VISIBLE);
					final BitmapFactory.Options options = new BitmapFactory.Options();
				    options.inJustDecodeBounds = true;
				    BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
				    // Calculate inSampleSize
				    options.inSampleSize = Services.calculateInSampleSize(options,80, 80);
				    // Decode bitmap with inSampleSize set
				    options.inJustDecodeBounds = false;
				    Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
				    imageView.setImageBitmap(bm);
				}
				else {
					imageView.setVisibility(View.INVISIBLE);
				}
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case(R.id.button_booking_cancel):{
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		      alert.setMessage("Really Cancel Booking for "+bookingName+"?");
		      alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//set result-status = 5; and lastupdate = now()
					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.Booking.Cols.RESULT, 5);
					values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
					values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
					
					contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, 
							ContentDescriptor.Booking.Cols.BID+" = ?", new String[] {bookingID});
					
					contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI, ContentDescriptor.BookingTime.Cols.BID+" = ?",
							new String[] {bookingID});
					getActivity().onBackPressed();

				}});
		      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//do nothing.
				}});
		      alert.show();
		      break;
		}
		case(R.id.button_booking_checkin):{
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		      alert.setMessage("Check-In "+bookingName+"?");
		      alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//set result = 20, lastupdate = now()
					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.Booking.Cols.RESULT, 20);
					values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
					values.put(ContentDescriptor.Booking.Cols.CHECKIN, new Date().getTime());
					values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
					
					contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, 
							ContentDescriptor.Booking.Cols.BID+" = ?", new String[] {bookingID});
				}});
		      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//do nothing.
				}});
		      alert.show();
		      break;
		}
		case(R.id.button_booking_noshow):{
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		      alert.setMessage("Set no-show for "+bookingName+"?");
		      alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//set result = 15, lastupdate = now()
					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.Booking.Cols.RESULT, 15);
					values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
					values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
					
					contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, 
							ContentDescriptor.Booking.Cols.BID+" = ?", new String[] {bookingID});
				}});
		      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//do nothing.
				}});
		      alert.show();
		      break;
		}
		case(R.id.button_booking_reschedule):{
			// we need an alert Dialog with a date & time picker.
			// then we delete all the bookingtime entries for the booking.
			// and then re-enter them.
			// we also need to set the booking as a pending Update.
			
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle("Reschedule Booking");
			rescheduleView = mInflater.inflate(R.layout.alert_reschedule, null);
			DatePicker datePicker = (DatePicker) rescheduleView.findViewById(R.id.datePicker1);
			TimePicker timePicker = (TimePicker) rescheduleView.findViewById(R.id.timePicker1);
			
			Date theDate = Services.StringToDate(date, "dd MMM yyyy");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(theDate.getTime());
			datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
			
			theDate = Services.StringToDate(starttime, "hh:mmaa");
			cal.setTimeInMillis(theDate.getTime());
			timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
			timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			
			alert.setView(rescheduleView);
			alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//check if the time is even available.
					//if it is, update the booking.
					DatePicker datePicker = (DatePicker) rescheduleView.findViewById(R.id.datePicker1);
					TimePicker timePicker = (TimePicker) rescheduleView.findViewById(R.id.timePicker1);
					
					Calendar cal = Calendar.getInstance();
					cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
					Date theDate = cal.getTime();
					
					cal.set(0, 0, 0, timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
					Date theTime = cal.getTime();
					SimpleDateFormat format = new SimpleDateFormat("hh:mmaa", Locale.US);
					
					if (!checkScheduleAvailable(format.format(theTime), Services.DateToString(theDate))) {
						Toast.makeText(getActivity(), "Cannot Move Booking to that time, It's already in use.", Toast.LENGTH_LONG).show(); 
					}
				}});
		    alert.setNegativeButton("Cancel", null);
			alert.show();
			
			break;
		}
		case (R.id.button_booking_sms):{
			//send a text reminder
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			String num = (String) v.getTag();
			System.out.print("\n\nNumber:"+num);
			smsIntent.setData(Uri.parse("sms:" + num)); 
			smsIntent.putExtra("sms_body","You have a "+bookingtype+" booking with "+resourcename+" at "+starttime+" on "+date);
			startActivity(smsIntent);
			break;
		}
		case (R.id.button_booking_call):{
			//show a pop-up to select the number to call (work, home, cell)
			String[] numbers = (String[]) v.getTag();
			selectCall(numbers);
			break;
		}
		}
	}
	
	private boolean checkScheduleAvailable(String thestarttime, String date) {
		// get ResourceID.
		// start time.
		// end time.
		int resourceid = -1, startid = 0, endid = 0;
		long interval = 0, datelong = 0;
		boolean can_set = true;
		
		Cursor cur2 = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" = ?",
				new String[] {String.valueOf(bookingID)}, null);
		
		if (!cur2.moveToFirst()) {
			return false;
		}
		
		datelong = Services.StringToDate(date, "dd MMM yyyy").getTime();
		interval = Services.StringToDate(endtime, "hh:mmaa").getTime() - Services.StringToDate(starttime, "hh:mmaa").getTime();

		resourceid = cur2.getInt(cur2.getColumnIndex(ContentDescriptor.Booking.Cols.RID));
		cur2.close();
		startid = HornetDBService.getTime(Services.dateFormat(thestarttime, "hh:mmaa", "HH:mm:ss"), contentResolver, false);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
		endid = HornetDBService.getTime(format.format(new Date(Services.StringToDate(thestarttime, "hh:mmaa").getTime()+interval)),
				contentResolver, true);
		
		int curid = startid;
		while (curid<= endid) { //we are inclusive. 
			Cursor cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI,null, ContentDescriptor.BookingTime.Cols.RID+" = ? AND ("
					+ContentDescriptor.Booking.Cols.STIMEID+" = ? OR "+ContentDescriptor.Booking.Cols.ETIMEID+" = ?) "
					+ "AND "+ContentDescriptor.Booking.Cols.ARRIVAL+" >= ? AND "+ContentDescriptor.Booking.Cols.ARRIVAL
					+" <=  strftime('%s',datetime((?/1000), 'unixepoch', '+23 hours', '+59 minutes'))*1000",
					new String[] {String.valueOf(resourceid), String.valueOf(curid), String.valueOf(curid),
					String.valueOf(datelong), String.valueOf(datelong)}, null);
			
			if (cur.moveToFirst()) {
				can_set = false;
			}
			cur.close();
			curid+=1;
		}
		
		if (can_set) {
			contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI,ContentDescriptor.BookingTime.Cols.BID+" = ? ",
					new String[] {String.valueOf(bookingID)});
			
			ContentValues values = new ContentValues();
			values.put(ContentDescriptor.Booking.Cols.STIME, Services.dateFormat(thestarttime, "hh:mmaa", "HH:mm:ss"));
			values.put(ContentDescriptor.Booking.Cols.STIMEID, startid);
			
			values.put(ContentDescriptor.Booking.Cols.ETIME, 
					format.format(new Date(Services.StringToDate(thestarttime, "hh:mmaa").getTime()+interval)));
			values.put(ContentDescriptor.Booking.Cols.ETIMEID, endid);
			values.put(ContentDescriptor.Booking.Cols.ARRIVAL, datelong);
			values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
			values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t"); //this IS needed.
			
			//Log.d("BOOKING DETAILS", Arrays.deepToString(values.valueSet().toArray()));
			contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
					new String[] {String.valueOf(bookingID)});
			
			curid = startid;
			while (curid <= endid) {
    			values = new ContentValues();
    			values.put(ContentDescriptor.BookingTime.Cols.BID, bookingID);
    			values.put(ContentDescriptor.BookingTime.Cols.RID, resourceid);
    			values.put(ContentDescriptor.BookingTime.Cols.TIMEID, curid);
    			values.put(ContentDescriptor.BookingTime.Cols.ARRIVAL, datelong);

				contentResolver.insert(ContentDescriptor.BookingTime.CONTENT_URI, values);
				curid +=1;
			}
			return true;
		}
		return false;
	}
	
	
	private void selectCall(String[] numbers){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		for (int i=0; i <numbers.length; i +=1){
			if (numbers[i] != null && numbers[i].compareTo("") !=0) {
				RadioButton rb = new RadioButton(ctx);
				rb.setText(numbers[i]);
				rg.addView(rb);
			}
		}
		
        builder.setView(layout);
        builder.setTitle("Select Number ");
        builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	//start intent
            	int cid = rg.getCheckedRadioButtonId();
            	RadioButton rb = (RadioButton) rg.findViewById(cid);
            	String selected = (String) rb.getText();
            	//System.out.print("\n\nWhole Selection:"+selected);
            	int pos = selected.indexOf("-");
            	selected = selected.substring(pos+1);
            	String ph ="tel:"+ selected;
				Intent intent = new Intent(Intent.ACTION_DIAL); //ACTION_DIAL, OR ACTION_CALL
				intent.setData(Uri.parse(ph));
				startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}
}
