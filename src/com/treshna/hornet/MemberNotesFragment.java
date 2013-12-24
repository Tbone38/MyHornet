package com.treshna.hornet;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.treshna.hornet.BookingPage.TagFoundListener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MemberNotesFragment extends Fragment implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private String visitDate;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	private static final String TAG = "MemberNotes";
	
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
	
		view = inflater.inflate(R.layout.member_details_notes, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
		
	private View setupView() {
		
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		EditText mConditions = (EditText) view.findViewById(R.id.medicalConditions);
		EditText medication = (EditText) view.findViewById(R.id.medication);
		EditText mDosage = (EditText) view.findViewById(R.id.medicationDosage);
		TextView mStaff = (TextView) view.findViewById(R.id.medicationByStaffL);
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL)) &&
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION)) &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL)).isEmpty() &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION)).isEmpty()) {
			
			mConditions.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL)));
			
			medication.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION)));
			
			mDosage.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICALDOSAGE)));
			
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATIONBYSTAFF)).compareTo("f")==0) {
				mStaff.setText("No "+getActivity().getResources().getString(R.string.label_member_medication_bystaff));
				mStaff.setTextColor(getActivity().getResources().getColor(R.color.visitors_red));
			} else {
				mStaff.setTextColor(getActivity().getResources().getColor(R.color.visitors_green));
			}
		}  else {
			TextView heading = (TextView) view.findViewById(R.id.medicalH);
			TextView labelConditions = (TextView) view.findViewById(R.id.medicalConditionsL);
			TextView labelMedication = (TextView) view.findViewById(R.id.medicationL);
			TextView labelDosage = (TextView) view.findViewById(R.id.medicationDosageL);
			heading.setVisibility(View.GONE);
			labelConditions.setVisibility(View.GONE);
			labelMedication.setVisibility(View.GONE);
			labelDosage.setVisibility(View.GONE);
			mConditions.setVisibility(View.GONE);
			medication.setVisibility(View.GONE);
			mDosage.setVisibility(View.GONE);
			mStaff.setVisibility(View.GONE);
		}
		cur.close();
		
		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
				ContentDescriptor.MemberNotes.Cols.MID+" = ?", new String[] {memberID}, 
				ContentDescriptor.MemberNotes.Cols.MNID+" DESC LIMIT 10");
		
		LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		llparams.setMargins(5, 5, 5, 5);
		LinearLayout notesGroup = (LinearLayout) view.findViewById(R.id.membernotes);
		notesGroup.removeAllViews();
		while (cur.moveToNext()) {	
			TextView notesT = new TextView(getActivity());
			notesT.setPadding(10, 0, 0, 0);
			notesT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.NOTES)));
			notesT.setTextSize(18);
			notesT.setLayoutParams(llparams);
			notesGroup.addView(notesT);
		}
		cur.close();
		
		
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
			tasksGroup.removeAllViews();
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
		
		TextView add_note = (TextView) view.findViewById(R.id.button_add_note);
		add_note.setOnClickListener(this);
		
		mActions.setupActions(view, memberID);
		return view;
	}
	
	private String getNewNote() {
		String note;
		
		EditText note_view = (EditText) view.findViewById(R.id.addnote);
		note = note_view.getText().toString();
		
		return note;
	}
	
	private void updateNote(String note) {
		ContentValues values = new ContentValues();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		values.put(ContentDescriptor.MemberNotes.Cols.MID, memberID);
		values.put(ContentDescriptor.MemberNotes.Cols.NOTES, note);
		values.put(ContentDescriptor.MemberNotes.Cols.OCCURRED, format.format(new Date()));
		
		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, ContentDescriptor.MemberNotes.Cols.MID+" = 0",
				null, null);
		if (!cur.moveToFirst()) {
			values.put(ContentDescriptor.MemberNotes.Cols.MNID, 0);
			
			contentResolver.insert(ContentDescriptor.MemberNotes.CONTENT_URI, values);
		} else {
			int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols._ID));
			contentResolver.update(ContentDescriptor.MemberNotes.CONTENT_URI, values, ContentDescriptor.MemberNotes.Cols.MNID+" = ?",
					new String[] {cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.MNID))});
			
			values = new ContentValues();
			values.put(ContentDescriptor.PendingUploads.Cols.TABLEID, 
					ContentDescriptor.TableIndex.Values.MemberNotes.getKey());
			values.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);

			contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
		}
		cur.close();
		
		EditText note_view = (EditText) view.findViewById(R.id.addnote);
		note_view.setText("");
		//why aren't either of these working ?
		//should we try redrawing the fragment ?
		setupView();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case (R.id.button_add_note):{
			//get text from edit text, add it to cache/pending uploads.
			String note = getNewNote();
			updateNote(note);
			break;
		}
		default:
			mActions.onClick(v);
			break;
		}
	}

	@Override
	public void onNewTag(String serial) {
	mActions.onNewTag(serial);
	}
}