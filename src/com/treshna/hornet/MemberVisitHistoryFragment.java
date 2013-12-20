package com.treshna.hornet;


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


public class MemberVisitHistoryFragment extends Fragment implements TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private String visitDate;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	private static final String TAG = "MemberVisitHistory";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		visitDate = this.getArguments().getString(Services.Statics.KEY);
		mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_visit_history, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupView() {
		
		String first_sync = Services.getAppSettings(getActivity(), "first_sync");
		if (first_sync.compareTo("-1")==0) {
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
			first_sync = format.format(new Date());
		}
		TextView heading = (TextView) view.findViewById(R.id.member_visit_history_H);
		heading.setText(String.format(getActivity().getResources().getString(R.string.member_visit_history), first_sync));
		
		Cursor cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, 
				ContentDescriptor.Visitor.Cols.MID+" = ?", new String[] {memberID}, null);
		LinearLayout list = (LinearLayout) view.findViewById(R.id.visit_history_list);
		
		while (cur.moveToNext()) {
			View row = mInflater.inflate(R.layout.member_visit_history_row, null);
			TextView datetime = (TextView) row.findViewById(R.id.visit_history_datetime);
			datetime.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DATETIME)));
			
			TextView programme = (TextView) row.findViewById(R.id.visit_history_programme);
			//TODO: get the programme-name from a join on the programme table (using membership table).
			programme.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.MSID)));
			
			TextView door = (TextView) row.findViewById(R.id.visit_history_door);
			door.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DOORNAME)));
			
			TextView access = (TextView) row.findViewById(R.id.visit_history_access);
			access.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY)));
			
			list.addView(row);
		}
		
		mActions.setupActions(view, memberID);
		return view;
	}
	
	@Override
	public void onNewTag(String serial) {
		mActions.onNewTag(serial);
	}
	
	}