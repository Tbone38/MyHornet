package com.treshna.hornet.membership;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
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

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.services.DatePickerFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

public class MembershipAdd extends Fragment implements OnClickListener, DatePickerFragment.DatePickerSelectListener {
	//private static final String TAG ="MembershipAdd";
	Context ctx;
	ContentResolver contentResolver;
	Cursor cur;
	View page;
	
	DatePickerFragment sDatePicker;
	DatePickerFragment eDatePicker;
	private String sdate = null;
	private String edate = null;
	private String currentPrice = null;
	private String currentSignup = null;
	private EditText mPrice;
	private EditText mSignup;
	private String memberid;
	private int groupid;
	private static long mLength;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 contentResolver = getActivity().getContentResolver();
		 
		 sDatePicker = new DatePickerFragment();
		 sDatePicker.setDatePickerSelectListener(this);
		 eDatePicker = new DatePickerFragment();
		 eDatePicker.setDatePickerSelectListener(this);
		 
		 //get memberID.
		 memberid = this.getArguments().getString(Services.Statics.MID);
		 
		 page = inflater.inflate(R.layout.fragment_membership_add, container, false);
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
					String replaceable = String.format(Locale.US,"[%s,.]", 
							NumberFormat.getCurrencyInstance(Locale.US).getCurrency().getSymbol());
					String cleanString = s.toString().replaceAll(replaceable, "");
					
				    double parsed = 0d;
				    try {
				    	parsed = Double.parseDouble(cleanString);
				    } catch ( NumberFormatException e) {
				    	//happens when the string is empty sometimes.
				    	parsed = 0d;
				    }
				    String formated = NumberFormat.getCurrencyInstance(Locale.US).format((parsed/100));

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
					String replaceable = String.format(Locale.US, "[%s,.]", 
							NumberFormat.getCurrencyInstance(Locale.US).getCurrency().getSymbol()); 
					String cleanString = s.toString().replaceAll(replaceable, "");
					
				    double parsed = 0d;
				    try {
				    	parsed = Double.parseDouble(cleanString);
				    } catch ( NumberFormatException e) {
				    	//happens when the string is empty sometimes.
				    	parsed = 0d;
				    }
				    String formated = NumberFormat.getCurrencyInstance(Locale.US).format((parsed/100));

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
		//cur = contentResolver.query(ContentDescriptor.Programme.GROUP_URI, null, null, null, ContentDescriptor.Programme.Cols.GID);
		cur = contentResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, null, null, ContentDescriptor.ProgrammeGroup.Cols.ID);
		if (cur.getCount() <= 1) {
			membershipgroup.setVisibility(View.GONE);
			TextView membership_label = (TextView) page.findViewById(R.id.membershipgrouptypeL);
			membership_label.setVisibility(View.GONE);
		}
		ArrayList<String> membershipgroups = new ArrayList<String>();
		while (cur.moveToNext()) {
			membershipgroups.add(cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.NAME)));
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
				cur = contentResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, null, null, ContentDescriptor.ProgrammeGroup.Cols.ID);
				if (!cur.moveToPosition(position)) {
					//error!
				}
				groupid = cur.getInt(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ID));
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
						 startdate.setText(Services.DateToString(start));
						 
						 TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
						 if ( mLength > 0) {
							 enddate.setText(Services.DateToString(end));
							 enddate.setTag(Services.dateFormat(Services.DateToString(end),
									 "dd MMM yyyy", "yyyyMMdd"));
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
		//startdate.setTag(Services.DateToString(new Date()));
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
		//enddate.setTag(Services.DateToString(new Date()));
		enddate.setClickable(true);
		enddate.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			enddate.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			enddate.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		
		
		LinearLayout accept = (LinearLayout) page.findViewById(R.id.acceptbutton);
		accept.setOnClickListener(this);
		
		LinearLayout cancel = (LinearLayout) page.findViewById(R.id.cancelbutton);
		cancel.setOnClickListener(this);
		
		return page;
	}
	
	private void setupDates(){
		TextView enddate = (TextView) page.findViewById(R.id.membershipedate);
		TextView startdate = (TextView) page.findViewById(R.id.membershipsdate);
		if (sdate != null) {
			startdate.setText(sdate);
			 if ( mLength > 0) {
				 Date start, end;
				 start = Services.StringToDate(sdate, "dd MMM yyyy");
				 end = new Date((start.getTime()+(mLength*1000)));
				 enddate.setText(Services.dateFormat(end.toString(), "EEE MMM dd HH:mm:ss zzz yyyy",
						 "dd MMM yyyy"));
				 enddate.setTag(Services.dateFormat(end.toString(),
						 "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
			 } else {
				 enddate.setText(getActivity().getString(R.string.membership_add_enddate));
			 }
		}
		if (edate != null) {
			
			enddate.setText(edate);
		}
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
		case (R.id.acceptbutton):{
			ArrayList<String> emptyviews = validate();
			boolean is_valid = Boolean.parseBoolean(emptyviews.get(0));
			if (!is_valid) {
				Log.w("MEMBERSHIPADD", "WAS NOT VALID");
				updateView(emptyviews);
			} else {
				Log.w("MEMBERSHIPADD", "WAS VALID");
				ArrayList<String> results = getInput();
				
				Fragment f = new MembershipComplete();
				Bundle bdl = new Bundle(1);
				bdl.putStringArrayList(Services.Statics.KEY, results);
				f.setArguments(bdl);
				((MainActivity)getActivity()).changeFragment(f, "MembershipComplete");
				
				/*EmptyActivity parent = (EmptyActivity) getActivity();
				parent.setFragment(Services.Statics.FragmentType.MembershipComplete.getKey(), bdl);*/
			}
			break;
		}
		case (R.id.cancelbutton):{
			getActivity().onBackPressed();
			break;
		}
		}
		
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
		
		emptyviews.add(0, String.valueOf(is_valid));
		return emptyviews;
	}
	
	private ArrayList<String> getInput(){
		ArrayList<String> input = new ArrayList<String>();
		
		input.add(0, memberid);
		
		Spinner group = (Spinner) page.findViewById(R.id.membershipgrouptype);
		cur = contentResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, null, null, ContentDescriptor.ProgrammeGroup.Cols.ID);
		cur.moveToPosition(group.getSelectedItemPosition());
		int pgid = cur.getInt(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ID));
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
		
		input.add(6, null); //payment date used to be here.
		
		EditText signupfee = (EditText) page.findViewById(R.id.membershipsignupfee);
		input.add(7, signupfee.getText().toString());
		
		
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, new String[] {ContentDescriptor.Membership.Cols.CARDNO},
				ContentDescriptor.Membership.Cols.MID+" = ? AND "+ContentDescriptor.Membership.Cols.CARDNO+" IS NOT NULL",
				new String[] {memberid}, null);
		if (cur.moveToFirst()) {
			input.add(8, cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.CARDNO)));
		} else {
			input.add(8, null); //cardid
		}
		
		return input;
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		
		if (sDatePicker == theDatePicker) {
			sdate = date;
			//sdate = Services.dateFormat(sdate, "yyyy MM dd", "dd MMM yyyy");
		}
		
		if (eDatePicker == theDatePicker) {
			edate = date;
			//edate = Services.dateFormat(edate, "yyyy MM dd", "dd MMM yyyy");
		}
		
		setupDates();
		
	}
	
}