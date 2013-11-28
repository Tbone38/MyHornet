package com.treshna.hornet;


import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MemberNotesFragment extends MemberActionsFragment{
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
		
		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
				ContentDescriptor.MemberNotes.Cols.MID+" = ?", new String[] {memberID}, 
				ContentDescriptor.MemberNotes.Cols.MNID+" DESC");
		
		LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		llparams.setMargins(5, 5, 5, 5);
		LinearLayout notesGroup = (LinearLayout) view.findViewById(R.id.membernotes);
		while (cur.moveToNext()) {	
			TextView notesT = new TextView(getActivity());
			notesT.setPadding(10, 0, 0, 0);
			notesT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.NOTES)));
			notesT.setTextSize(18);
			notesT.setLayoutParams(llparams);
			notesGroup.addView(notesT);
		}
		cur.close();
		
		
		
		/*
		 * The Below If-Statements might(?) hard crash the system if the item (e.g. string(17))
		 * is Null. Easiest Solution is nested IF's (see above), though best would be 
		 * to better handle null data on entry to database-cache. (so that it's an empty string)
		 */
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, 
				"m."+ContentDescriptor.Member.Cols.MID+" = ?", new String[] {memberID}, null);
		if (!cur.moveToFirst()){
			return view;
		}
		
		if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).compareTo("null") == 0)
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).length() == 0)){
			
			TextView tasks = (TextView) view.findViewById(R.id.membertasksH);
			tasks.setVisibility(View.GONE);
			
			
		} else {
			LinearLayout tasksGroup = (LinearLayout) view.findViewById(R.id.membertasks);			
			int l;
			for(l=13;l<=15;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)
				if (cur.getString(l) != null) {
					TextView tasks = new TextView(getActivity());
					tasks.setPadding(15, 0, 0, 10);
					tasks.setText(cur.getString(l));
					tasks.setTextSize(16);
					llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					llparams.setMargins(0, 0, 0, 5);
					tasks.setLayoutParams(llparams);
					tasksGroup.addView(tasks);
				}
			}			
			
		}
				
		if (visitDate != null && visitDate.compareTo("") != 0) { //fix this
			TextView visitTH = new TextView(getActivity());
			visitTH.setPadding(5, 0, 0, 0);
			visitTH.setText("Visit Time");
			visitTH.setTextSize(13);
			visitTH.setLayoutParams(llparams);
			notesGroup.addView(visitTH);
			
			TextView visitT = new TextView(getActivity());
			visitT.setPadding(15, 0, 0, 0);
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
			lastVH.setPadding(5, 0, 0, 0);
			lastVH.setText("Previous Visit");
			lastVH.setTextSize(13);
			lastVH.setLayoutParams(llparams);
			notesGroup.addView(lastVH);
			
			TextView lastVT = new TextView(getActivity());
			lastVT.setPadding( 15, 0, 0, 0);
			lastVT.setText(lastVisit);
			lastVT.setTextSize(18);
			lastVT.setLayoutParams(llparams);
			notesGroup.addView(lastVT);
		}
		
		cur.close();
		
		super.setupActions(view, memberID);
		return view;
	}
	
}