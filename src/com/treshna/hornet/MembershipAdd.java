package com.treshna.hornet;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.BookingPage.TagFoundListener;

public class MembershipAdd extends Fragment implements OnClickListener, TagFoundListener {
	private static final String TAG ="MembershipAdd";
	Context ctx;
	ContentResolver contentResolver;
	Cursor cur;
	View page;
	
	DatePickerFragment sDatePicker;
	DatePickerFragment eDatePicker;
	DatePickerFragment pDatePicker;
	private String sdate = null;
	private String edate = null;
	private String pdate = null;
	private String currentPrice = null;
	private String currentSignup = null;
	private EditText mPrice;
	private EditText mSignup;
	private String memberid;
	private String cardid = null;
	private AlertDialog alertDialog = null;
	private int groupid;
	private long mLength;
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the data-picker finishes, sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
            MembershipAdd.this.receivedBroadcast(intent);
        }	
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 contentResolver = getActivity().getContentResolver();
		 
		 sDatePicker = new DatePickerFragment();
		 eDatePicker = new DatePickerFragment();
		 pDatePicker = new DatePickerFragment();
		 
		 //get memberID.
		 memberid = this.getArguments().getString(Services.Statics.MID);
		 
		 page = inflater.inflate(R.layout.membership_add, container, false);
		 page = setupView();
		 
		 return page;
	}

	
	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private View setupView() {

		mPrice = (EditText) page.findViewById(R.id.membershipprice);
		mPrice.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(!s.toString().equals(currentPrice)){
					mPrice.removeTextChangedListener(this);
					String replaceable = String.format("[%s,.]", 
							NumberFormat.getCurrencyInstance().getCurrency().getSymbol()); 
					String cleanString = s.toString().replaceAll(replaceable, "");
					
				    double parsed = 0d;
				    try {
				    	parsed = Double.parseDouble(cleanString);
				    } catch ( NumberFormatException e) {
				    	//happens when the string is empty sometimes.
				    	parsed = 0d;
				    }
				    String formated = NumberFormat.getCurrencyInstance().format((parsed/100));

				    currentPrice = formated;
				    mPrice.setText(formated);
				    mPrice.setSelection(formated.length());
				    mPrice.addTextChangedListener(this);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		mSignup = (EditText) page.findViewById(R.id.membershipsignupfee);
		mSignup.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(!s.toString().equals(currentSignup)){
					mSignup.removeTextChangedListener(this);
					String replaceable = String.format("[%s,.]", 
							NumberFormat.getCurrencyInstance().getCurrency().getSymbol()); 
					String cleanString = s.toString().replaceAll(replaceable, "");
					
				    double parsed = 0d;
				    try {
				    	parsed = Double.parseDouble(cleanString);
				    } catch ( NumberFormatException e) {
				    	//happens when the string is empty sometimes.
				    	parsed = 0d;
				    }
				    String formated = NumberFormat.getCurrencyInstance().format((parsed/100));

				    currentSignup = formated;
				    mSignup.setText(formated);
				    mSignup.setSelection(formated.length());
				    mSignup.addTextChangedListener(this);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		
		Spinner membershipgroup = (Spinner) page.findViewById(R.id.membershipgrouptype);
		cur = contentResolver.query(ContentDescriptor.Programme.GROUP_URI, null, null, null, null);
		ArrayList<String> membershipgroups = new ArrayList<String>();
		while (cur.moveToNext()) {
			membershipgroups.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.GNAME)));
		}
		cur.close();
		
		ArrayAdapter<String> groupAdapter = new ArrayAdapter<String>(ctx,
				android.R.layout.simple_spinner_item, membershipgroups);
		groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		membershipgroup.setAdapter(groupAdapter);
		membershipgroup.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedView,
					int position, long id) {
				cur = contentResolver.query(ContentDescriptor.Programme.GROUP_URI, null, null, null, null);
				if (!cur.moveToPosition(position)) {
					//error!
				}
				groupid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Programme.Cols.GID));
				cur.close();
				
				Spinner membershiptype = (Spinner) page.findViewById(R.id.membershiptype);
				cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.GID+" = ?",
						new String[] {String.valueOf(groupid)}, null);
				ArrayList<String> membershiptypes = new ArrayList<String>();
				while (cur.moveToNext()) {
					membershiptypes.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)));
				}
				cur.close();
				
				ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(ctx,
						android.R.layout.simple_spinner_item, membershiptypes);
				typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				
				membershiptype.setAdapter(typeAdapter);
				membershiptype.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView, View selectedView,
							int position, long id) {
						cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.GID+" = ?",
								new String[] {String.valueOf(groupid)}, null);
						
						if (!cur.moveToPosition(position)){
							//we failed for some reason, what should I do?
						}
						mPrice.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.PRICE)));
						mSignup.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.SIGNUP)));
						
						 TextView payment_desc = (TextView) page.findViewById(R.id.membershippaymentdesc);
						 payment_desc.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.PRICE_DESC)));
						 
						 mLength = cur.getLong(cur.getColumnIndex(ContentDescriptor.Programme.Cols.MLENGTH));
						 
						 cur.close();
						 
						 Date start, end;
						 start = new Date();
						 end = new Date((start.getTime()+(mLength*1000)));
						 
						 TextView startdate = (TextView) page.findViewById(R.id.membershipsdate);
						 startdate.setText(Services.dateFormat(start.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", 
								 "dd MMM yyyy"));
						 
						 TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
						 Log.w(TAG, "Length IS:"+mLength);
						 if ( mLength > 0) {
							 enddate.setText(Services.dateFormat(end.toString(), "EEE MMM dd HH:mm:ss zzz yyyy",
									 "dd MMM yyyy"));
							 enddate.setTag(Services.dateFormat(end.toString(),
									 "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
						 } else {
							 enddate.setText(getActivity().getString(R.string.membership_add_enddate));
						 }
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
				
		
		TextView startdate = (TextView) page.findViewById(R.id.membershipsdate);
		startdate.setTag(Services.dateFormat(new Date().toString(),
				"EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		startdate.setClickable(true);
		startdate.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			startdate.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			startdate.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		
		TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
		enddate.setTag(Services.dateFormat(new Date().toString(), 
				"EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		enddate.setClickable(true);
		enddate.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			enddate.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			enddate.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		
		TextView paymentdate = (TextView) page.findViewById(R.id.membershippaymentdate);
		paymentdate.setTag(Services.dateFormat(new Date().toString(), 
				"EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
		paymentdate.setClickable(true);
		paymentdate.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			paymentdate.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			paymentdate.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		
		//actions
		LinearLayout addphoto = (LinearLayout) page.findViewById(R.id.membershipaddphoto_row);
		addphoto.setOnClickListener(this);
		
		LinearLayout addtag = (LinearLayout) page.findViewById(R.id.membershipaddtag_row);
		addtag.setOnClickListener(this);
		
		LinearLayout accept = (LinearLayout) page.findViewById(R.id.acceptbutton);
		accept.setOnClickListener(this);
		
		LinearLayout cancel = (LinearLayout) page.findViewById(R.id.cancelbutton);
		cancel.setOnClickListener(this);
		
		return page;
	}
	
	private void setupDates(){
		if (sdate != null) {
			TextView startdate = (TextView) page.findViewById(R.id.membershipsdate);
			startdate.setText(sdate);
		}
		if (edate != null) {
			TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
			enddate.setText(edate);
		}
		if (pdate != null) {
			TextView paymentdate = (TextView) page.findViewById(R.id.membershippaymentdate);
			paymentdate.setText(pdate);
		}
	}
	
	private void receivedBroadcast(Intent i) {
		sdate = sDatePicker.getReturnValue();
		if (sdate != null) {
			sdate = Services.dateFormat(sdate, "yyyy MM dd", "dd MMM yyyy");// TODO: apply this format everywhere.
		}
		edate = eDatePicker.getReturnValue();
		if (edate != null) {
			edate = Services.dateFormat(edate, "yyyy MM dd", "dd MMM yyyy");
		}
		pdate = pDatePicker.getReturnValue();
		if (pdate != null) {
			pdate = Services.dateFormat(pdate, "yyyy MM dd", "dd MMM yyyy");
		}
		setupDates();
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter();
	    iff.addAction(ClassCreate.CLASSBROADCAST);
	    getActivity().registerReceiver(this.mBroadcastReceiver,iff);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		getActivity().unregisterReceiver(this.mBroadcastReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.membershipsdate):{
			Bundle bdl;
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) v.getTag());
			
			sDatePicker.setArguments(bdl);
		    sDatePicker.show(this.getChildFragmentManager(), "datePicker");
		    break;
		}
		case (R.id.membershipedate):{
			Bundle bdl;
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) v.getTag());
			
			eDatePicker.setArguments(bdl);
		    eDatePicker.show(this.getChildFragmentManager(), "datePicker");
		    break;
		}
		case (R.id.membershippaymentdate):{
			Bundle bdl;
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) v.getTag());
			
			pDatePicker.setArguments(bdl);
		    pDatePicker.show(this.getChildFragmentManager(), "datePicker");
		    break;
		}
		case (R.id.acceptbutton):{
			ArrayList<String> emptyviews = validate();
			boolean is_valid = Boolean.parseBoolean(emptyviews.get(0));
			if (!is_valid) {
				updateView(emptyviews);
			} else {
				ArrayList<String> results = getInput();
				//insert results
				insertMembership(results);
				//continue to next page
			}
			break;
		}
		case (R.id.cancelbutton):{
			getActivity().finish();
			break;
		}
		case (R.id.membershipaddphoto_row): {
			Intent camera = new Intent(ctx, CameraWrapper.class);
			camera.putExtra(VisitorsViewAdapter.EXTRA_ID,memberid);
			ctx.startActivity(camera);
			break;
		}
		case (R.id.membershipaddtag_row):{
			//show alert box, indicating to swipe:
			//handle swipe event on this fragment differently.
			swipeBox();
			break;
		}
		}
		
	}
	
	private void swipeBox(){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		
		builder.setTitle("Swipe Tag")
		.setMessage("Please Swipe a tag against the device");
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	
	private void updateView(ArrayList<String> emptyFields) {
		for(int i=1; i<emptyFields.size(); i+=1){
			//get label, change colour.
			TextView label = (TextView) page.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private ArrayList<String> validate(){
		ArrayList<String> emptyviews = new ArrayList<String>();
		boolean is_valid = true;
		
		TextView startdate = (TextView) page.findViewById(R.id.membershipsdate);
		if (startdate.getText().toString().compareTo(this.getString(R.string.membership_add_startdate)) ==0) {
			//not changed!
			is_valid = false;
			emptyviews.add(String.valueOf(R.id.membershipsdate));
		} else {
			startdate.setTextColor(Color.BLACK);
		}
		
		TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
		if (enddate.getText().toString().compareTo(this.getString(R.string.membership_add_enddate))!=0) {
			if (startdate.getText().toString().compareTo(this.getString(R.string.membership_add_startdate)) !=0) {
				//compare the dates;
				SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
				Date sdate = null, edate = null;
				try {
					sdate = format.parse(startdate.getText().toString());
					edate = format.parse(enddate.getText().toString());
					if (sdate.getTime()>= edate.getTime()) {
						is_valid = false;
						emptyviews.add(String.valueOf(R.id.membershipedate));
						emptyviews.add(String.valueOf(R.id.membershipsdate));
					} else {
						startdate.setTextColor(Color.BLACK);
						enddate.setTextColor(Color.BLACK);
					}
					
				} catch (ParseException e) {
					is_valid = false;
					emptyviews.add(String.valueOf(R.id.membershipedate));
					emptyviews.add(String.valueOf(R.id.membershipsdate));
				}
			}
		}
		
		TextView paymentdate = (TextView) page.findViewById(R.id.membershippaymentdate);
		Log.v(TAG, paymentdate.getText().toString());
		Log.v(TAG, this.getString(R.string.membership_add_paymentdate));
		if (paymentdate.getText().toString().compareTo(this.getString(R.string.membership_add_paymentdate)) == 0) {
			is_valid = false;
			emptyviews.add(String.valueOf(R.id.membershippaymentdate));
		 // do all the crazy gymmaster payment checking here.
		} 
		/*
		 *First payment date after start date" CHECK (firstpayment >= startdate)
    	 *First payment date is too early" CHECK (firstpayment > '1990-01-01'::date)
    	 *First payment is too far in the future" CHECK (firstpayment < (startdate + '4 mons'::interval) OR startdate < now()) 
		 */
		else {
			paymentdate.setTextColor(Color.BLACK);
		}
		
		
		emptyviews.add(0, String.valueOf(is_valid));
		return emptyviews;
	}
	
	private ArrayList<String> getInput(){
		ArrayList<String> input = new ArrayList<String>();
		
		input.add(0, memberid);
		
		Spinner group = (Spinner) page.findViewById(R.id.membershipgrouptype);
		cur = contentResolver.query(ContentDescriptor.Programme.GROUP_URI, null, null, null, null);
		cur.moveToPosition(group.getSelectedItemPosition());
		int pgid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Programme.Cols.GID));
		cur.close();
		input.add(1, String.valueOf(pgid));
		
		Spinner	type = (Spinner) page.findViewById(R.id.membershiptype);
		cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.GID+" = ?",
				new String[] {String.valueOf(pgid)}, null);
		cur.moveToPosition(type.getSelectedItemPosition());
		int pid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Programme.Cols.PID));
		input.add(2, String.valueOf(pid));
		
		TextView startdate = (TextView) page.findViewById(R.id.membershipsdate);
		input.add(3, startdate.getText().toString());
		
		TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
		if (enddate.getText().toString().compareTo(getActivity().getString(R.string.membership_add_enddate)) ==0) {
			input.add(4, null);
		} else {
			input.add(4, enddate.getText().toString());
		}
		
		EditText price = (EditText) page.findViewById(R.id.membershipprice);
		input.add(5, price.getText().toString());
		
		TextView paymentdate = (TextView) page.findViewById(R.id.membershippaymentdate);
		input.add(6, paymentdate.getText().toString());
		
		EditText signupfee = (EditText) page.findViewById(R.id.membershipsignupfee);
		input.add(7, signupfee.getText().toString());
		
		if (cardid == null) {
			input.add(8, null);
		} else if (cardid != null) {
			input.add(8, cardid);
		}
		
		return input;
	}

	private void insertMembership(ArrayList<String> input) {
		//!!!TODO!!!
		
	}

	
	/** look up the serial in the idcard table,
	 * check that the cardno is not in use by anyone else.
	 * assign the cardno to the membership.
	 */
	@Override
	public void onNewTag(String serial) {
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cur;
		String message;
		
		cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols.SERIAL+" = ?",
				new String[] {serial}, null);
		if (!cur.moveToFirst()) {
			//tag not found in db. Tell them to swipe card at reception, then re-sync
			//the phone.
			//TODO:
			message = "Tag not found in db, please swipe tag at reception, then re-sync device";
			Log.v(TAG, message);
		} else {
			cardid = cur.getString(cur.getColumnIndex(ContentDescriptor.IdCard.Cols.CARDID));
			cur.close();
			
			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.CARDNO+" = ?",
					new String[] {cardid}, null);
			if (cur.getCount() > 0) {
				//id is in use, what should I do?
				message = "Tag already in use";
				Log.v(TAG, message);
				cardid = null;
			} else {
				message = "Assigning card No. "+cardid+" to member.";
				Log.v(TAG, message);
				if (alertDialog != null){
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		}
		cur.close();
		//TOAST!
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
	}
}