package com.treshna.hornet;


import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MemberNotesFragment extends Fragment {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private String visitDate;
	private View view;
	LayoutInflater mInflater;
	
	private static final String TAG = "MemberNotes";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		visitDate = this.getArguments().getString(Services.Statics.KEY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_notes, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
		
	private View setupView() {
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, 
				"m."+ContentDescriptor.Member.Cols.MID+" = ?", new String[] {memberID}, null);
		if (!cur.moveToFirst()){
			return view;
		}
		
		LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		llparams.setMargins(7, 7, 7, 7);
		LinearLayout notesGroup = (LinearLayout) view.findViewById(R.id.membernotes);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)) != null) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)).compareTo("null") != 0) {
				
				TextView notesT = new TextView(getActivity());
				notesT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
				notesT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)));
				notesT.setTextSize(18);
				notesT.setLayoutParams(llparams);
				notesGroup.addView(notesT);
		} 	}
		/*
		 * The Below If-Statements might(?) hard crash the system if the item (e.g. string(17))
		 * is Null. Easiest Solution is nested IF's (see above), though best would be 
		 * to better handle null data on entry to database-cache. (so that it's an empty string)
		 */
		if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).compareTo("null") == 0)
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).length() == 0)){
			
			TextView tasks = new TextView(getActivity());
			tasks.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			tasks.setText("No Pending Tasks");
			tasks.setTextSize(13);
			tasks.setLayoutParams(llparams);
			notesGroup.addView(tasks);
			
		} else {
			TextView tasksH = new TextView(getActivity());
			tasksH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			tasksH.setText("Tasks");
			tasksH.setTextSize(13);
			tasksH.setLayoutParams(llparams);
			notesGroup.addView(tasksH);
			
			
			int l;
			for(l=12;l<=14;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)
				if (cur.getString(l) != null) {
					TextView tasks = new TextView(getActivity());
					tasks.setPadding(45, 0, 0, 10);
					tasks.setText(cur.getString(l));
					tasks.setTextSize(16);
					llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					llparams.setMargins(0, 0, 0, 5);
					tasks.setLayoutParams(llparams);
					notesGroup.addView(tasks);
				}
			}			
			
		}
		if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)).compareTo("null") == 0) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)).length() == 0)){
			
			TextView bookings = new TextView(getActivity());
			bookings.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			bookings.setText("No Pending Bookings");
			bookings.setTextSize(13);
			bookings.setLayoutParams(llparams);
			notesGroup.addView(bookings);

		} else {
			TextView bookingsH = new TextView(getActivity());
			bookingsH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			bookingsH.setText("Bookings");
			bookingsH.setTextSize(13);
			bookingsH.setLayoutParams(llparams);
			notesGroup.addView(bookingsH);
			
			int l;
			for(l=15;l<=17;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)
				if (cur.getString(l) != null) {
					TextView bookings = new TextView(getActivity());
					bookings.setPadding(45, 0, 0, 0);
					bookings.setText(cur.getString(l));
					bookings.setTextSize(16);
					llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					llparams.setMargins(0, 0, 0, 5);
					bookings.setLayoutParams(llparams);
					notesGroup.addView(bookings);
				}
			}
		}
		
		if (visitDate != null && visitDate.compareTo("") != 0) { //fix this
			TextView visitTH = new TextView(getActivity());
			visitTH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			visitTH.setText("Visit Time");
			visitTH.setTextSize(13);
			visitTH.setLayoutParams(llparams);
			notesGroup.addView(visitTH);
			
			TextView visitT = new TextView(getActivity());
			visitT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
			visitDate = Services.dateFormat(visitDate, "yyyy-MM-dd HH:mm", "dd MMM yy 'at' HH:mm aa");
			visitT.setText(visitDate);
			visitT.setTextSize(18);
			visitT.setLayoutParams(llparams);
			notesGroup.addView(visitT);
		}
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)) 
				&& cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)).compareTo("") != 0) {
			String lastVisit = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)), "yyyy-MM-dd HH:mm", "dd MMM yy 'at' HH:mm aa");
			
			TextView lastVH = new TextView(getActivity());
			lastVH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			lastVH.setText("Previous Visit");
			lastVH.setTextSize(13);
			lastVH.setLayoutParams(llparams);
			notesGroup.addView(lastVH);
			
			TextView lastVT = new TextView(getActivity());
			lastVT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
			lastVT.setText(lastVisit);
			lastVT.setTextSize(18);
			lastVT.setLayoutParams(llparams);
			notesGroup.addView(lastVT);
		}
		
		cur.close();
		
		return view;
	}
}