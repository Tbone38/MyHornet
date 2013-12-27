package com.treshna.hornet;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.treshna.hornet.BookingPage.TagFoundListener;


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
	
	private View setupEmergency() {
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		LinearLayout emergencyHeading = (LinearLayout) view.findViewById(R.id.emergencyHeadingRow);
		emergencyHeading.setOnClickListener(this);
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYNAME)) &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYNAME)).isEmpty()) {
			
			EditText emergencyname = (EditText) view.findViewById(R.id.emergencyContactName);
			EditText emergencyrelationship = (EditText) view.findViewById(R.id.emergencyContactRelationship);
			EditText emergencycell = (EditText) view.findViewById(R.id.emergencyContactCell);
			EditText emergencywork = (EditText) view.findViewById(R.id.emergencyContactWork);
			EditText emergencyhome = (EditText) view.findViewById(R.id.emergencyContactHome);
			
			emergencyname.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYNAME)));
			emergencyrelationship.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYRELATIONSHIP)));
			emergencycell.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYCELL)));
			emergencywork.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYWORK)));
			emergencyhome.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYHOME)));
			
		} else {
			LinearLayout emergencyDetails = (LinearLayout) view.findViewById(R.id.emergencyContactDetails);
			emergencyDetails.setVisibility(View.GONE);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.emergency_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
		}
		
		return view;
	}
	
	private View setupMedical() {
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		
		TextView mStaff = (TextView) view.findViewById(R.id.medicationByStaffL);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATIONBYSTAFF)).compareTo("f")==0) {
			mStaff.setText("No "+getActivity().getResources().getString(R.string.label_member_medication_bystaff));
			mStaff.setTextColor(getActivity().getResources().getColor(R.color.visitors_red));
		} else {
			mStaff.setTextColor(getActivity().getResources().getColor(R.color.visitors_green));
		}
		
		LinearLayout medicalHeading = (LinearLayout) view.findViewById(R.id.medicalHeadingRow);
		medicalHeading.setOnClickListener(this);
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL)) &&
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION)) &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL)).isEmpty() &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION)).isEmpty()) {
			
			EditText mConditions = (EditText) view.findViewById(R.id.medicalConditions);
			EditText medication = (EditText) view.findViewById(R.id.medication);
			EditText mDosage = (EditText) view.findViewById(R.id.medicationDosage);
		
			mConditions.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL)));
			medication.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION)));
			mDosage.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICALDOSAGE)));
			
		} else {
			LinearLayout medicalDetails = (LinearLayout) view.findViewById(R.id.medicalDetails);
			medicalDetails.setVisibility(View.GONE);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.medical_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
		}
		cur.close();
		
		return view;
	}
		
	private View setupView() {
		
		setupEmergency();
		setupMedical();
		
		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
				ContentDescriptor.MemberNotes.Cols.MID+" = ?", new String[] {memberID}, 
				ContentDescriptor.MemberNotes.Cols.MNID+" DESC LIMIT 10");
		
		LinearLayout notesHeading = (LinearLayout) view.findViewById(R.id.notesHeadingRow);
		notesHeading.setOnClickListener(this);
		
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
		
		
		LinearLayout taskHeading = (LinearLayout) view.findViewById(R.id.tasksHeadingRow);
		taskHeading.setOnClickListener(this);
		
		if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).compareTo("null") == 0)
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).length() == 0)){
			
			LinearLayout tasks = (LinearLayout) view.findViewById(R.id.membertasks);
			tasks.setVisibility(View.GONE);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.tasks_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			
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
		case (R.id.medicalHeadingRow):{
			LinearLayout medicalDetails = (LinearLayout) view.findViewById(R.id.medicalDetails);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.medical_expand_collapse);
			if (medicalDetails.isShown()) {
				medicalDetails.setVisibility(View.GONE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			} else {
				medicalDetails.setVisibility(View.VISIBLE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_collapse));
			}
			break;
		}
		case (R.id.emergencyHeadingRow):{
			LinearLayout emergencyDetails = (LinearLayout) view.findViewById(R.id.emergencyContactDetails);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.emergency_expand_collapse);
			if (emergencyDetails.isShown()) {
				emergencyDetails.setVisibility(View.GONE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			} else {
				emergencyDetails.setVisibility(View.VISIBLE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_collapse));
			}
			break;
		}
		case (R.id.notesHeadingRow):{
			LinearLayout notesgroup = (LinearLayout) view.findViewById(R.id.membernotes);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.notes_expand_collapse);
			if (notesgroup.isShown()) {
				notesgroup.setVisibility(View.GONE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			} else {
				notesgroup.setVisibility(View.VISIBLE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_collapse));
			}
			break;
		}
		case (R.id.tasksHeadingRow):{
			LinearLayout tasks = (LinearLayout) view.findViewById(R.id.membertasks);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.tasks_expand_collapse);
			if (tasks.isShown()) {
				tasks.setVisibility(View.GONE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			} else {
				tasks.setVisibility(View.VISIBLE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_collapse));
			}
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