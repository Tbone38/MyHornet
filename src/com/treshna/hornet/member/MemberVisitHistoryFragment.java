package com.treshna.hornet.member;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.network.PollingHandler;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Member;



public class MemberVisitHistoryFragment extends Fragment implements TagFoundListener, OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	View checkinWindow;
	//private MemberActions mActions;
	
	//private static final String TAG = "MemberVisitHistory";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		//mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.fragment_member_details_visit_history, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	/*public MemberActions getMemberActions(){
		return this.mActions;
	}*/
	
	private void setupActions() {
		LinearLayout manualcheckin = (LinearLayout) view.findViewById(R.id.button_manual_checkin);
		manualcheckin.setOnClickListener(this);
	}
	
	private View setupView() {
		
		setupActions();
		Cursor cur = contentResolver.query(ContentDescriptor.Visitor.VISITOR_PROGRAMME_URI, null, 
				"v."+ContentDescriptor.Visitor.Cols.MID+" = ?", new String[] {memberID}, ContentDescriptor.Visitor.Cols.DATETIME+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.visit_history_list);
		
		if (cur.getCount() <= 0) {
			LinearLayout list_headings = (LinearLayout) view.findViewById(R.id.visit_list_headings);
			list_headings.setVisibility(View.INVISIBLE);
		}
		
		while (cur.moveToNext()) {
			
			View row = mInflater.inflate(R.layout.row_member_visit_history, null);
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
		
		//mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public boolean onNewTag(String serial) {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.button_manual_checkin):{
			//show a window which: 	lets the user select a membership to checkin,
			//						lets the user select a door to check in at.
			//						(shows the user the member name?)
			showCheckinWindow();
			break;
		}}
	}
	
	private void showCheckinWindow(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		checkinWindow = inflater.inflate(R.layout.alert_manual_checkin, null);
		String name = null;
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, new String[] {Member.Cols.FNAME, Member.Cols.SNAME},
				"m."+ContentDescriptor.Member.Cols.MID+" = ?", new String[] {String.valueOf(memberID)}, null);
		if (!cur.moveToFirst()) {
			return;
		}
		name = cur.getString(0)+" "+cur.getString(1);
		cur.close();
		
		EditText nameView = (EditText) checkinWindow.findViewById(R.id.manual_checkin_name);
		nameView.setText(name);

		ArrayList<String> doorlist = new ArrayList<String>();
		cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, null, null, null);
		while (cur.moveToNext()) {
			doorlist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORNAME)));
		}
		cur.close();
		Spinner doorspinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_door);
		ArrayAdapter<String> doorAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, doorlist);
		doorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		doorspinner.setAdapter(doorAdapter);
		
		ArrayList<String> membershiplist = new ArrayList<String>();
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ? AND "
				+ContentDescriptor.Membership.Cols.HISTORY+" = 'f'", new String[] {memberID}, null);
		while (cur.moveToNext()) {
			membershiplist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
		}
		cur.close();
		Spinner membershipSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_membership);
		ArrayAdapter<String> membershipAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_spinner_item, membershiplist);
		//membershipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		membershipSpinner.setAdapter(membershipAdapter);
		
		 	builder.setView(checkinWindow);
	        builder.setTitle("Manual Check-In");
	        builder.setPositiveButton("Check In", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//get spinner details.
					Spinner membershipSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_membership);
					cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, new String[] 
							{ContentDescriptor.Membership.Cols.MSID}, ContentDescriptor.Membership.Cols.MID+" = ?",
							new String[] {memberID}, null);
					int membershipid = -1;
					try {
						cur.moveToPosition(membershipSpinner.getSelectedItemPosition());
						membershipid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID));
					} catch (CursorIndexOutOfBoundsException e) {
						Toast.makeText(getActivity(), "No Membership Available for Member.", Toast.LENGTH_LONG).show();
						dialog.cancel();
						return;
					}
					cur.close();
					
					Spinner doorSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_door);
					cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, null, null, null);
					cur.moveToPosition(doorSpinner.getSelectedItemPosition());
					
					int doorid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORID));
					cur.close();
					
					ManualCheckin async = new ManualCheckin();
					async.execute(doorid, Integer.parseInt(memberID), membershipid);
					
				 	PollingHandler p = Services.getFreqPollingHandler();
				 	if (p != null && !p.getConStatus()) {
				 		Toast.makeText(getActivity(), "Could not check member in, Check that this device is connected"
				 				+ " to the internet.", Toast.LENGTH_LONG).show();
				 	}
				}
	        });
	        builder.show();
	}
	
	private class ManualCheckin extends AsyncTask<Integer, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;

		
		public ManualCheckin() {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(getActivity(), "Checking In..", 
					 "Manually checking Member In...");
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			int doorid, memberid, membershipid;
			doorid = params[0];
			memberid = params[1];
			membershipid = params[2];
			return sync.manualCheckin(doorid, memberid, membershipid, getActivity());
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
}