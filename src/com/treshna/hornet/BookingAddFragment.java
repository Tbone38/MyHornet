package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class BookingAddFragment extends Fragment implements OnClickListener {
	
	private Cursor cur;
	private ContentResolver contentResolver;
	public String bookingFName;
	public String bookingSName;
	private String msID;
	private String curdate;
	private Context ctx;
	private static final String TAG = "BookingAddFragment";
	private View page;
	
	String statusMessage;
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 String startid = getArguments().getString(Services.Statics.KEY);
		 if (getArguments().getString(Services.Statics.DATE)!= null) {
			 curdate = getArguments().getString(Services.Statics.DATE);
			 curdate = Services.dateFormat(curdate, "yyyyMMdd", "dd MMM yyyy");
		 }
		 //bookingFName = getArguments().getString(Services.Statics.IS_BOOKING_F);
		 //bookingSName = getArguments().getString(Services.Statics.IS_BOOKING_S);
		 //msID = getArguments().getString(Services.Statics.MSID);
		 contentResolver = getActivity().getContentResolver();
		 page = inflater.inflate(R.layout.booking_add, container, false);
		 if (curdate == null) {
			 curdate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "dd MMM yyyy");
		 }
		 setupView(startid);
		 return page;
	 }
	
	public void setName(String fname, String sname) {
		bookingFName = fname;
		bookingSName = sname;
	}
	
	public void setMembership(String msid) {
		this.msID = msid;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setupView(String startid) {		
		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.ID+" = "+startid, null, null);
		cur.moveToFirst();
		
		TextView date = (TextView) page.findViewById(R.id.bookingDate);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			date.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			date.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		date.setClickable(true);
		date.setText(curdate);
		date.setTag(Services.dateFormat(curdate, "dd MMM yyyy", "yyyyMMdd"));
		
		date.setOnClickListener(this);
		
		
		//select member from find-member list ?
		RelativeLayout member = (RelativeLayout) page.findViewById(R.id.textwrapper);
		
		if (bookingFName != null && bookingSName != null) {
			TextView firstname = (TextView) page.findViewById(R.id.bookingFName);
			firstname.setText(bookingFName);
			
			TextView surname = (TextView) page.findViewById(R.id.bookingSName);
			surname.setText(bookingSName);
			//member.setText(bookingName); //the selected value ? passed back from the find member list ?
		} else {
			//member.setText("");
		}
		member.setClickable(true);
		member.setOnClickListener(this);
		
		
		
		TextView start = (TextView) page.findViewById(R.id.bookingStartTime);
		start.setText(cur.getString(1));
		start.setClickable(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			start.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			start.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		//start.setOnClickListener(this);
		
		TextView duration = (TextView) page.findViewById(R.id.bookingEndTime);
		duration.setClickable(true);
		duration.setTag(cur.getString(1));
		duration.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			duration.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			duration.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		cur.close();
		
		TextView membership = (TextView) page.findViewById(R.id.bookingMembership);
		if (msID != null) {
			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MSID+" = ?",
					new String[] {msID}, null);
			if (cur.moveToFirst()) {
				membership.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			}
		} else {
			TextView membershipH = (TextView) page.findViewById((R.id.bookingMembershipH));
			membershipH.setVisibility(View.GONE);
			membership.setVisibility(View.GONE);
		}
		
		RelativeLayout layout = (RelativeLayout) page.findViewById(R.id.bookingLayout);
		
		TextView typelabel = new TextView(ctx);
		RelativeLayout.LayoutParams rlparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rlparams.addRule(RelativeLayout.BELOW, R.id.bookingResourceRow);

		typelabel.setId(10);
		typelabel.setLayoutParams(rlparams);
		typelabel.setText(R.string.bookingTypeH);
		typelabel.setTextSize(16);
		layout.addView(typelabel);
		
		Spinner typespinner = new Spinner(ctx);
		typespinner.setId(11);
		rlparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rlparams.addRule(RelativeLayout.BELOW, R.id.bookingResourceRow);
		rlparams.addRule(RelativeLayout.RIGHT_OF, 10);
		typespinner.setLayoutParams(rlparams);
		//Spinner typespinner = (Spinner) findViewById(R.id.bookingType);
		cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, null, null, null);
		List<String> typelist = new ArrayList<String>();
		while (cur.moveToNext()) {
			if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)) > 0) {
				typelist.add(cur.getString(1));
			}
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx,
				android.R.layout.simple_spinner_item, typelist);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			typespinner.setAdapter(dataAdapter);
			typespinner.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					// set the booking type, and update table;					
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//leave alone (doesn't matter?)
				}});
		cur.close();
		layout.addView(typespinner);
		
		LinearLayout buttonRow = new LinearLayout(ctx);
		//
		rlparams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rlparams.addRule(RelativeLayout.BELOW, 11);
		rlparams.setMargins(3, 3, 3, 3);
		buttonRow.setLayoutParams(rlparams);
		buttonRow.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView accept = new TextView(ctx);
		accept.setId(45);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, (float) .5);
		accept.setLayoutParams(params);
		accept.setPadding(5, 5, 5, 5);
		accept.setTextSize(22);
		accept.setText(R.string.bookingaccept);
		accept.setGravity(Gravity.CENTER);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			accept.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			accept.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		accept.setClickable(true);
		accept.setOnClickListener(this);
		
		TextView cancel = new TextView(ctx);
		cancel.setId(46);
		params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, (float) .5);
		cancel.setLayoutParams(params);
		cancel.setPadding(5, 5, 5, 5);
		cancel.setTextSize(22);
		cancel.setText(R.string.bookingcancel);
		cancel.setGravity(Gravity.CENTER);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			cancel.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		
		buttonRow.addView(accept);
		buttonRow.addView(cancel);
		layout.addView(buttonRow);
		
		Spinner resourcespinner = (Spinner) page.findViewById(R.id.bookingResourceS);
		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
		String rid = Services.getAppSettings(getActivity(), "resourcelist");
		ArrayList<String> resourcelist = new ArrayList<String>();
		int selectedResource = 0;
		while (cur.moveToNext()) {
			resourcelist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)) == Integer.parseInt(rid)) {
				selectedResource = cur.getPosition();
			}
			
		}
		ArrayAdapter<String> resourceAdapter = new ArrayAdapter<String>(ctx,
				android.R.layout.simple_spinner_item, resourcelist);
			resourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			resourcespinner.setAdapter(resourceAdapter);
			resourcespinner.setSelection(selectedResource);
			resourcespinner.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					//set end time.
					cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
					cur.moveToPosition(pos);
					
					String period = cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.PERIOD));
					TextView startview = (TextView) page.findViewById(R.id.bookingStartTime);
					String starttime = startview.getText().toString();
					
					int year = 2013, month = 10, day = 2;
					Date date = new Date(year, month, day, 0, 0);
					Date enddate = new Date(year, month, day, Integer.parseInt(period.substring(0, 2)), 
							Integer.parseInt(period.substring(3, 5)));
					Date startdate = new Date(year, month, day, Integer.parseInt(starttime.substring(0, 2)),
							Integer.parseInt(starttime.substring(3, 5)));
					Long difference = enddate.getTime() - date.getTime();
					difference = difference *2;
					
					TextView duration = (TextView) page.findViewById(R.id.bookingEndTime);
					duration.setText(Services.dateFormat(new Date((startdate.getTime()+difference)).toString(), 
							"EEE MMM dd HH:mm:ss zzz yyyy", "HH:mm:ss"));
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//leave alone (doesn't matter?)
				}});
		cur.close();
		
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		case (R.id.bookingDate):{
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) v.getTag());
			DialogFragment newFragment = new DatePickerFragment();
			newFragment.setArguments(bdl);
		    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
		    break;
		}
		case (R.id.bookingEndTime):{
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) v.getTag());
			//System.out.print("\n\n"+v.getTag());
			DialogFragment newFragment = new TimePickerFragment();
			newFragment.setArguments(bdl);
		    newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
		    break;
		}
		case (46):{ //cancel Add booking
			getActivity().onBackPressed();
			break;
		}
		case (45):{ //add book
			ArrayList<String> emptyFields = formCheck();

			if (emptyFields.get(0).compareTo("true") == 0){
			
				ArrayList<String> results = getInput();
			
				/* Need: 	BookingID,					- DONE
				 * 			Name,						- DONE 
				 * 			Booking Type AS (DESC),		- DONE
				 * 			starttime-id & endtime-id,  - DONE
				 * 			start-time & end-time,		- DONE
				 * 			notes,						- DONE
				 * 			memberID & membership ID,	- DONE & DONE
				 * 			result,						- DONE (DEFAULT 10)
				 * 			Last-Updated.				- DONE (DEFAULT new Date().getTime();)
				 * 
				 * 			Arrival,					- DONE
				 * 			Resource ID,				- DONE
				 * 			Time ID, 					- DONE
				 * 			Booking ID,					- DONE
				 */
				int inscount = insertBooking(results);
				if (inscount <= 0) {
					//an error occured.
					//display pop-up with error, highlight error field.
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Error Occured");
					builder.setMessage(statusMessage);
					builder.setPositiveButton("OK", null);
					builder.show();
					switch (inscount){
					case(0):{
						//update failed, probably no ID's available. consider syncing again.
						getActivity().finish();
						}
					case(-1):{
						//incorrect time slot set. highlight end time.
						ArrayList<String> error = new ArrayList<String>();
						error.add(null); //filler, where the boolean value was
						error.add(String.valueOf(R.id.bookingEndTimeH));
						updateView(error);
						break;
						}
					case(-2):{ //could not find member
						ArrayList<String> error = new ArrayList<String>();
						error.add(null);
						error.add(String.valueOf(R.id.bookingNameH));
						updateView(error);
						break;
						}
					case(-3):{ //could not find booking Type
						ArrayList<String> error = new ArrayList<String>();
						error.add(null);
						error.add(String.valueOf(10));
						updateView(error);
						break;
						}
					case(-4):{ //could not find resource
						ArrayList<String> error = new ArrayList<String>();
						error.add(null);
						error.add(String.valueOf(R.id.bookingResourceH));
						updateView(error);
						break;
						}
					case(-5):{ //a booking exists for this resource within that time range.
						ArrayList<String> error = new ArrayList<String>();
						error.add(null); //filler, where the boolean value was
						error.add(String.valueOf(R.id.bookingEndTimeH));
						updateView(error);
						break;
					}
					case (-6):{ //the booking duration is set incorrectly
						ArrayList<String> error = new ArrayList<String>();
						error.add(null); //filler, where the boolean value was
						error.add(String.valueOf(R.id.bookingEndTimeH));
						updateView(error);
						break;
					}
					case (-7):{
						ArrayList<String> error = new ArrayList<String>();
						error.add(null); //filler, where the boolean value was
						error.add(String.valueOf(R.id.bookingStartTimeH));
						updateView(error);
						break;
					}
					}
					
				} else {
					getActivity().finish();
				}
			} else {
				//notify of failure.
				updateView(emptyFields);
			}
			break;
		}
		case (R.id.textwrapper):{
			//show find member, but set onclick to link back here. destroy find member after passed back here.
			FragmentManager frm = getActivity().getSupportFragmentManager();
			FragmentTransaction ft = frm.beginTransaction();
			MembersFindFragment f = new MembersFindFragment(); // add a bundle argument, so the code knows what to do.
			Bundle b = new Bundle(1);
			b.putBoolean(Services.Statics.IS_BOOKING, true);
			f.setArguments(b);
			ft.replace(R.id.empty_layout, f);
			ft.addToBackStack(null);
			ft.commit();
			//this.
			break;
		}
		}
	}
	
	private int insertBooking(ArrayList<String> input) {
		int _id, result = 0;
		String bookingid, arrival, resourceid, offset;
		
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATE+" = 0",
				null, null);
		if (cur.getCount() == 0) {
			//no free booking id's.
			statusMessage = "No Free Booking ID's available. Consider ReSync-ing";
			return 0;
		}
		cur.moveToFirst();
		_id = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ID)); //this is where we put the booking.
		bookingid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
		cur.close();
		
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Booking.Cols.STIME, input.get(2));
		values.put(ContentDescriptor.Booking.Cols.ETIME, input.get(3));
		
		int etimeid = getTime(input.get(3), contentResolver);
		if (etimeid == -1) {
			statusMessage = "incorrect time selected, try 30 minute increments";
			return -1;
		}
		values.put(ContentDescriptor.Booking.Cols.ETIMEID, etimeid);
		int stimeid = getTime(input.get(2), contentResolver);
		values.put(ContentDescriptor.Booking.Cols.STIMEID, stimeid);
		
		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.NAME+" = ?", 
				new String[] {input.get(4)}, null);
		if (cur.getCount()<=0) {
			//bugger
			//this shouldn't happen
			System.out.print("\n\n Resource Not Found:"+input.get(4));
			statusMessage = "Resource Not Found:"+input.get(4);
			return -4;
		}
		cur.moveToFirst();
		resourceid = cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID));
		offset = cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.PERIOD));
		values.put(ContentDescriptor.Booking.Cols.RID, resourceid);
		values.put(ContentDescriptor.Booking.Cols.OFFSET, offset);
		//values.put(ContentDescriptor.Booking.Cols.IS_UPLOADED, 0);
		cur.close();
		int correctspace = get_mod(input.get(3), input.get(2),offset);
		if (correctspace != 0) {
			if (correctspace == -3) {
				statusMessage = "Cannot Create booking at that start time.";
				Log.e(TAG, "Creating booking at this time will only show on phone.");
				return -7;
			}
			statusMessage = "Booking duration does not match Resource's booking peroids.";
			Log.e(TAG, "Incorrect time amount selected");
			return -6;
		}
		arrival = Services.dateFormat(input.get(1), "dd MMM yyyy", "yyyyMMdd");
		
		//Check if the timeslots are available before attempting to insert bookings.
		for (int i=stimeid;i<etimeid;i +=1) {
			cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, "bt."+ContentDescriptor.BookingTime.Cols.TIMEID+" = ? AND "
					+"bt."+ContentDescriptor.BookingTime.Cols.ARRIVAL+" = ? AND bt."+ContentDescriptor.BookingTime.Cols.RID+" = ?"
					+" AND "+ContentDescriptor.Booking.Cols.RESULT+" !=5 ", 
					new String[] {String.valueOf(i),arrival, resourceid }, null);
			if (cur.getCount() > 0 ) {
				cur.moveToFirst();
				
				statusMessage = "A Booking already exists at "+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME))+" for "
						+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME))+" "
						+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME));
				return -5;
			}
			cur.close();
		}
		
		
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.FNAME+" = ? AND "
				+ContentDescriptor.Member.Cols.SNAME+" = ?",  //TODO:
				new String[] {input.get(0), input.get(7)}, null);
		if (cur.getCount()<= 0 ) {
			//bugger?
			// this shouldn't happen.
			System.out.print("\n\n Member not found:"+input.get(0)+" "+input.get(7));
			statusMessage = "Member not found:"+input.get(0)+" "+input.get(7);
			return -2;
		}
		cur.moveToFirst();
		values.put(ContentDescriptor.Booking.Cols.MID, cur.getString(0));//cur.getColumnIndex(ContentDescriptor.Member.Cols.MID))
		cur.close();
		values.put(ContentDescriptor.Booking.Cols.FNAME, input.get(0));
		values.put(ContentDescriptor.Booking.Cols.SNAME, input.get(7));
		
		values.put(ContentDescriptor.Booking.Cols.RESULT, 10); //10 for booking ?
		//values.put(ContentDescriptor.Booking.Cols.FNAME, input.get(0));
		if (input.get(6) != null) {
			values.put(ContentDescriptor.Booking.Cols.NOTES, input.get(6));
		}
		cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.NAME+" = ?",
				new String[] {input.get(5)}, null);
		if (cur.getCount()<=0) {
			//bugger
			//this shouldn't happen
			System.out.print("\n\n Booking Type Not Found:"+input.get(5));
			statusMessage = "Booking Type Not Found:"+input.get(5);
			return -3;
		}
		cur.moveToFirst();
		values.put(ContentDescriptor.Booking.Cols.BOOKING, input.get(5)); //this is just the bookingtype.name ?
		values.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)));
		cur.close();
		
		if (msID != null) {
			values.put(ContentDescriptor.Booking.Cols.MSID, msID);
		}
		
		
		
		values.put(ContentDescriptor.Booking.Cols.ARRIVAL, Services.dateFormat(input.get(1), "dd MMM yyyy", "yyyyMMdd"));
		System.out.print("\n\nCreating Booking at:"+System.currentTimeMillis());
		values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, System.currentTimeMillis()); //new Date().getTime();
		result = contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.ID+" = ?", 
				new String[] {String.valueOf(_id)});
		
		values = new ContentValues();
		values.put(ContentDescriptor.PendingUploads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Booking.getKey());
		values.put(ContentDescriptor.PendingUploads.Cols.ROWID, _id);
		
		contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
		
		if (result == 0){ //no free BID's found.
			statusMessage = "Insert Failed";
			return 0;
		}
		
		int curtimeid = stimeid;
		
		while (curtimeid < etimeid) {
		//add the details to the bookingTime composite table.
			values = new ContentValues();
			values.put(ContentDescriptor.BookingTime.Cols.BID, bookingid);
			values.put(ContentDescriptor.BookingTime.Cols.ARRIVAL, Services.dateFormat(input.get(1), "dd MMM yyyy", "yyyyMMdd"));
			cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.NAME+" = ?", 
					new String[] {input.get(4)}, null);
			if (cur.getCount()<=0) {
				//bugger
				//this shouldn't happen
				System.out.print("\n\n Resource Not Found:"+input.get(4));
				statusMessage = "Resource Not Found:"+input.get(4);
				return -4;
			}
			cur.moveToFirst();
			values.put(ContentDescriptor.BookingTime.Cols.RID, cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)));
			cur.close();
			
			values.put(ContentDescriptor.BookingTime.Cols.TIMEID, curtimeid);
			contentResolver.insert(ContentDescriptor.BookingTime.CONTENT_URI, values);
			
			
			
			curtimeid +=1;
		}
		return result;
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		int i;
		for(i=1; i<emptyFields.size(); i+=1){
			//get label, change colour?
			//String view = "R.id."+emptyFields.get(i);
			TextView label = (TextView) page.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private ArrayList<String> getInput(){
		//screen = getView();
		ArrayList<String> input = new ArrayList<String>();
		
		TextView fname = (TextView) page.findViewById(R.id.bookingFName);
		input.add(fname.getText().toString()); 																// 0
		
		TextView date = (TextView) page.findViewById(R.id.bookingDate);
		input.add(date.getText().toString()); 																// 1
		
		TextView starttime = (TextView) page.findViewById(R.id.bookingStartTime);
		input.add(starttime.getText().toString()); 															// 2
		
		TextView endtime = (TextView) page.findViewById(R.id.bookingEndTime);
		input.add(endtime.getText().toString()); 															// 3
		
		Spinner resource = (Spinner) page.findViewById(R.id.bookingResourceS);
		input.add((String)resource.getItemAtPosition(resource.getSelectedItemPosition())); 					// 4
		
		Spinner bookingtype = (Spinner) page.findViewById(11); //hard-coded value, see code in setupView()
		input.add((String) bookingtype.getItemAtPosition(bookingtype.getSelectedItemPosition())); 			// 5
		
		EditText notes = (EditText) page.findViewById(R.id.bookingNotes);
		if (notes.getEditableText().toString().compareTo("") != 0){
			input.add(notes.getEditableText().toString()); 													// 6
		} else {
			input.add(null); 																				// 6
		}
		
		TextView sname = (TextView) page.findViewById(R.id.bookingSName);
		input.add(sname.getText().toString());																// 7
		
		return input;
	}
	
	private ArrayList<String> formCheck(){
		
		boolean result = true;
		ArrayList<String> emptyFields = new ArrayList<String>();
		
		TextView name = (TextView) page.findViewById(R.id.bookingFName);
		System.out.print("\n\nNAME:"+name.getText().toString());
		if (name.getText().toString().compareTo("Select Member")==0){
			result = false;
			emptyFields.add(String.valueOf(R.id.bookingNameH));
		} else {
			TextView label = (TextView) page.findViewById(R.id.bookingNameH);
			label.setTextColor(Color.BLACK);
		}
		
		TextView date = (TextView) page.findViewById(R.id.bookingDate);
		System.out.print("\n\nDATE:"+date.getText().toString());
		if (date.getText().toString().compareTo(getString(R.string.addbookingDate)) ==0){
			System.out.print("\n\nDATE NOT SET \n");
			result = false;
			emptyFields.add(String.valueOf(R.id.bookingDateH));
		} else {
			TextView label = (TextView) page.findViewById(R.id.bookingDateH);
			label.setTextColor(Color.BLACK);
		}
		
		TextView endtime = (TextView) page.findViewById(R.id.bookingEndTime);
		System.out.print("\n\nEnd Time::"+endtime.getText().toString());
		if (endtime.getText().toString().compareTo(getString(R.string.bookingEndTime)) ==0){
			result = false;
			emptyFields.add(String.valueOf(R.id.bookingEndTimeH));
		} else {
			TextView starttime = (TextView) page.findViewById(R.id.bookingStartTime);
			int start = 0;
			int end = -1;
			try {
				start = Integer.parseInt(starttime.getText().toString().substring(0, 2)+starttime.getText().toString().substring(3, 5));
				end = Integer.parseInt(endtime.getText().toString().substring(0, 2)+endtime.getText().toString().substring(3, 5));
			} catch (Exception e) {
				//
			}
			if (start >= end){
				result = false;
				emptyFields.add(String.valueOf(R.id.bookingEndTimeH));
			} else {
				TextView label = (TextView) page.findViewById(R.id.bookingEndTimeH);
				label.setTextColor(Color.BLACK);
			}
		}
		//resource & booking type ?
		
		emptyFields.add(0,String.valueOf(result));
		return emptyFields;
	}
	
	public static class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

		@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		String date = (String) getArguments().get(Services.Statics.KEY);
		if (date.length() <8) date = date.substring(0, 4)+"0"+date.substring(4);
		final Calendar c = Calendar.getInstance();
		c.set(Integer.parseInt(date.substring(0, 4)), (Integer.parseInt(date.substring(4, 6))-1), Integer.parseInt(date.substring(6)));

		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		System.out.print("\n\n"+year+" "+month+" "+day);
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
		
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			Calendar cal = Calendar.getInstance();
			//cal.set(Calendar.YEAR, year);
			cal.set(year, month, day);
			//System.out.print("\n\n"+cal.getTime().toString());
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
			TextView date = (TextView) getActivity().findViewById(R.id.bookingDate);
			date.setText(format.format(cal.getTime()));
		}
	}
	
	public static class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String starttime = (String) getArguments().get(Services.Statics.KEY);
			//System.out.print("\n\n"+starttime);
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(starttime.substring(0, 2)));
			c.set(Calendar.MINUTE, (Integer.parseInt(starttime.substring(3, 5))+15));
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			//System.out.print("\n\n"+cal.getTime().toString());
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
			TextView time = (TextView) getActivity().findViewById(R.id.bookingEndTime);
			time.setText(format.format(cal.getTime()));
		}
	}
	
	 private int getTime(String time, ContentResolver contentResolver){
	    	int result = 0;
	    	if (cur != null){
	    		cur.close();
	    	}
	    	cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME
					+" = '"+time+"' ", null, null);
			if (cur.getCount() == 0) {
				//statusMessage = "incorrect Time slot set, please visit application settings";
				return -1;
			}
			cur.moveToFirst();
			
			result = cur.getInt(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID));
			cur.close();
			return result;
	    }
	 
	 /* get hours and minutes as separate values, check that the minutes match,
	  * convert hours into minutes. add the hours to the minutes
	  * subtract start from end, then modular against the minutes period.
	  */
	 private int get_mod(String etime, String stime, String period) {
		 int result = -1;
		 int iehour, iemin, ishour, ismin;
		 int iperiod, ietime, istime;

		 etime = etime.replace(":", "");
		 stime = stime.replace(":", "");
		 period = period.replace(":", "");
	
		 iehour = Integer.parseInt(etime.substring(0, 2));
		 iehour = (iehour * 60);
		 iemin = Integer.parseInt(etime.substring(2, 4));
		 ietime = (iehour + iemin);
		 ishour = Integer.parseInt(stime.substring(0, 2));
		 ishour = (ishour *60);
		 ismin = Integer.parseInt(stime.substring(2, 4));
		 istime = (ishour + ismin);
		 
		 iperiod = Integer.parseInt(period.substring(2, 4));
		 result = (ietime-istime)%iperiod;
		 
		 if ((ismin == 15 || ismin == 45) && (iperiod == 30 || iperiod == 60)) {
			 return -3;
		 }		 

		 //Log.v(TAG, "etime:"+etime+" , stime:"+stime+" , Period:"+period); 
		 //Log.v(TAG, "** MOD RESULT:"+String.valueOf(result));
		 //Log.v(TAG, "iehour:"+iehour+" iemin:"+iemin+" ishour:"+ishour+" ismin:"+ismin);
		 //Log.v(TAG, "ietime:"+ietime+" istime:"+istime+" iperiod:"+iperiod);
		
		 return result;
	 }
}
