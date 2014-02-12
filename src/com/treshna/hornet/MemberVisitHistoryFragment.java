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


public class MemberVisitHistoryFragment extends Fragment implements TagFoundListener {
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
		//visitDate = this.getArguments().getString(Services.Statics.KEY);
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
		
		Cursor cur = contentResolver.query(ContentDescriptor.Visitor.VISITOR_PROGRAMME_URI, null, 
				"v."+ContentDescriptor.Visitor.Cols.MID+" = ?", new String[] {memberID}, ContentDescriptor.Visitor.Cols.DATETIME+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.visit_history_list);
		
		while (cur.moveToNext()) {
			
			View row = mInflater.inflate(R.layout.member_visit_history_row, null);
			if (cur.getPosition()%2 == 0) {
				row.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.US);
			Date date = null;
			try {
				date = format.parse(cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DATETIME)));	
			} catch (ParseException e) {
				date = null;
			}
			
			format = new SimpleDateFormat("dd MMM yyyy", Locale.US); //HH:mm:ss
			String outputdate, outputtime;
			if (date != null ) {
				outputdate = format.format(date);
				format = new SimpleDateFormat("HH:mm:ss", Locale.US); //HH:mm:ss
				outputtime = format.format(date);
			} else {
				outputdate = cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DATE));
				outputtime = cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.TIME));
			}
			
			TextView dateview = (TextView) row.findViewById(R.id.visit_history_date);
			dateview.setText(outputdate);
			
			TextView timeview = (TextView) row.findViewById(R.id.visit_history_time);
			timeview.setText(outputtime);
			
			TextView programme = (TextView) row.findViewById(R.id.visit_history_programme);
			programme.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY)).compareTo("Granted") == 0){
				View access = (View) row.findViewById(R.id.visit_history_colour_block);
				access.setBackgroundColor(this.getActivity().getResources().getColor(R.color.visitors_green));
				access.setVisibility(View.VISIBLE);
			} else {
				View access = (View) row.findViewById(R.id.visit_history_colour_block); 
				access.setBackgroundColor(this.getActivity().getResources().getColor(R.color.visitors_red));
				access.setVisibility(View.VISIBLE);
				
				programme.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY)));
			}
			
			
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