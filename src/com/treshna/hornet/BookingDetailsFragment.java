package com.treshna.hornet;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.treshna.hornet.R.color;

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
	
	private RadioGroup rg;
	
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 bookingID = getArguments().getString(Services.Statics.KEY);
		 System.out.print("\n\nBOOKINGID:"+bookingID+"\n\n");
		 contentResolver = getActivity().getContentResolver();
		 View page =inflater.inflate(R.layout.booking_details, container, false); 
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
		
		date = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.BookingTime.Cols.ARRIVAL)), "yyyyMMdd", "EEEE MMMM yy");
		starttime = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)), "HH:mm:ss", "hh:mmaa");
		
		TextView time = (TextView) page.findViewById(R.id.bookingtime);
		time.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)), "HH:mm:ss", "hh:mmaa")+" - " //start time is pos 15.
				+Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)), "HH:mm:ss", "hh:mmaa"));
		
		TextView date = (TextView) page.findViewById(R.id.bookingdate);
		date.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.BookingTime.Cols.ARRIVAL)), "yyyyMMdd", "EEEE MMMM yy"));

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
		
		TextView reschedule = (TextView) page.findViewById(R.id.button_booking_reschedule_text);
		reschedule.setTextColor(color.grey);
		
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
					
					contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, 
							ContentDescriptor.Booking.Cols.BID+" = ?", new String[] {bookingID});
					
					contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI, ContentDescriptor.BookingTime.Cols.BID+" = ?",
							new String[] {bookingID});
					getActivity().finish();

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
		}/*
		case(R.id.bookingreschedule):{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
		      alert.setMessage("Really Cancel Booking for "+bookingName+"?");
		      alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//change & update table.
				}});
		      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//do nothing.
				}});
		      alert.show();
		      break;
		}*/
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
