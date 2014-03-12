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
	
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	//private static final String TAG = "MemberNotes";
	
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
		
		//YMCA specific Parent Name.
		int use_roll = Integer.parseInt(Services.getAppSettings(getActivity(), "use_roll"));
		if (use_roll > 0) {
			LinearLayout parentRow = (LinearLayout) view.findViewById(R.id.memberParentRow);
			parentRow.setVisibility(View.VISIBLE);
			
			EditText parentname = (EditText) view.findViewById(R.id.memberParentName);
			parentname.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PARENTNAME)));
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
			emergencyHeading.setVisibility(View.GONE);
			LinearLayout emergencyDetails = (LinearLayout) view.findViewById(R.id.emergencyContactDetails);
			emergencyDetails.setVisibility(View.GONE);
			/*ImageView expand_collapse = (ImageView) view.findViewById(R.id.emergency_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));*/
		}
		
		return view;
	}
	
	private View setupDetails() {
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		LinearLayout detailsHeading = (LinearLayout) view.findViewById(R.id.memberDetailsHeadingRow);
		detailsHeading.setOnClickListener(this); //TODO
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.COUNTRY))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.DOB))) {
			
			EditText street_view, suburb_view, city_view, postal_view, country_view, dob_view, gender_view;
			String gender = "";
			street_view = (EditText) view.findViewById(R.id.member_address_street);
			street_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET)));
			
			suburb_view = (EditText) view.findViewById(R.id.member_address_suburb);
			suburb_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB)));
			
			city_view = (EditText) view.findViewById(R.id.member_address_city);
			city_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY)));
			
			postal_view = (EditText) view.findViewById(R.id.member_area_code);
			postal_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL)));
			
			country_view = (EditText) view.findViewById(R.id.member_country);
			country_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.COUNTRY)));
			
			dob_view = (EditText) view.findViewById(R.id.member_dateofbirth);
			dob_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.DOB)));
			
			gender_view = (EditText) view.findViewById(R.id.member_gender);
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.GENDER))&&
					cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.GENDER)).compareTo("f")==0) {
				gender = getResources().getString(R.string.gender_female);
			} else {
				gender = getResources().getString(R.string.gender_male);
			}
			gender_view.setText(gender);
			
		} else {
			LinearLayout memberDetails = (LinearLayout) view.findViewById(R.id.memberDetails);
			memberDetails.setVisibility(View.GONE);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.member_details_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
		}
		cur.close();
		return view;
	}
	
	private View setupMedical() {
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		
		TextView mStaff = (TextView) view.findViewById(R.id.medicationByStaffL);
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATIONBYSTAFF))&& 
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATIONBYSTAFF)).compareTo("f")==0) {
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
			medicalHeading.setVisibility(View.GONE);
			LinearLayout medicalDetails = (LinearLayout) view.findViewById(R.id.medicalDetails);
			medicalDetails.setVisibility(View.GONE);
			/*ImageView expand_collapse = (ImageView) view.findViewById(R.id.medical_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));*/
		}
		cur.close();
		
		return view;
	}
		
	private View setupView() {
		setupDetails();
		setupEmergency();
		setupMedical();
		
		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
				ContentDescriptor.MemberNotes.Cols.MID+" = ?", new String[] {memberID}, 
				ContentDescriptor.MemberNotes.Cols.MNID+" DESC LIMIT 10");
		
		LinearLayout notesHeading = (LinearLayout) view.findViewById(R.id.notesHeadingRow);
		notesHeading.setOnClickListener(this);
		
		LinearLayout.LayoutParams llparams;
		LinearLayout notesGroup = (LinearLayout) view.findViewById(R.id.membernotes);
		notesGroup.removeAllViews();
		while (cur.moveToNext()) {
			llparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llparams.setMargins(5, 0, 0, 0);
			TextView notesT = new TextView(getActivity());
			notesT.setPadding(10, 0, 0, 0);
			notesT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.NOTES)));
			notesT.setTextSize(18);
			notesT.setLayoutParams(llparams);
			notesGroup.addView(notesT);
			
			TextView notesdetails = new TextView(getActivity());
			notesdetails.setPadding(10, 0, 0, 0);
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.UPDATEUSER))) {
				notesdetails.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.OCCURRED)));
			} else {
				notesdetails.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.OCCURRED))
						+"   by "+cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.UPDATEUSER)));
			}
			notesdetails.setTextSize(14);
			notesdetails.setTextColor(getResources().getColor(R.color.button_block_label));
			llparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llparams.setMargins(20, 0, 0, 0);
			notesdetails.setLayoutParams(llparams);
			notesGroup.addView(notesdetails);
		}
		cur.close();
		
		
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, 
				"m."+ContentDescriptor.Member.Cols.MID+" = ?", new String[] {memberID}, null);
		if (!cur.moveToFirst()){
			return view;
		}
		
		
		LinearLayout taskHeading = (LinearLayout) view.findViewById(R.id.tasksHeadingRow);
		taskHeading.setVisibility(View.GONE);
		LinearLayout tasks = (LinearLayout) view.findViewById(R.id.membertasks);
		tasks.setVisibility(View.GONE);
		
		/*if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).compareTo("null") == 0)
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).length() == 0)){
			
			
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
			
		}*/
			
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
		values.put(ContentDescriptor.MemberNotes.Cols.DEVICESIGNUP, "t");
		
		cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
				+ContentDescriptor.TableIndex.Values.MemberNotes.getKey(), null, null);
		int mnid = 0;
		if (cur.moveToFirst()) {
			mnid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
		}
		values.put(ContentDescriptor.MemberNotes.Cols.MNID, mnid);
		contentResolver.insert(ContentDescriptor.MemberNotes.CONTENT_URI, values);
		cur.close();
		
		if (mnid > 0) {
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(mnid),
					String.valueOf(ContentDescriptor.TableIndex.Values.MemberNotes.getKey())});
		}
		
		EditText note_view = (EditText) view.findViewById(R.id.addnote);
		note_view.setText("");
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
		case (R.id.memberDetailsHeadingRow):{
			LinearLayout details = (LinearLayout) view.findViewById(R.id.memberDetails);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.member_details_expand_collapse);
			if (details.isShown()) {
				details.setVisibility(View.GONE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			} else {
				details.setVisibility(View.VISIBLE);
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