package com.treshna.hornet;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.treshna.hornet.BookingPage.TagFoundListener;


public class MemberBookingsFragment extends Fragment implements TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	//private static final String TAG = "MemberVisitHistory";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_bookings, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupView() {
		//we inflate the list here.
				
		mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public void onNewTag(String serial) {
		mActions.onNewTag(serial);
	}	
}