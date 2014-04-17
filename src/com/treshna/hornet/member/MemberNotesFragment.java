package com.treshna.hornet.member;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R.color;
import com.treshna.hornet.R.drawable;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.R.string;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.services.Services.Statics;
import com.treshna.hornet.services.Services.Typefaces;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.FreeIds;
import com.treshna.hornet.sqlite.ContentDescriptor.Member;
import com.treshna.hornet.sqlite.ContentDescriptor.MemberNotes;
import com.treshna.hornet.sqlite.ContentDescriptor.TableIndex;
import com.treshna.hornet.sqlite.ContentDescriptor.FreeIds.Cols;
import com.treshna.hornet.sqlite.ContentDescriptor.TableIndex.Values;


public class MemberNotesFragment extends Fragment implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	RadioGroup rg;
	String memberID;
	
	private View view;
	LayoutInflater mInflater;
	//private MemberActions mActions;
	
	//private static final String TAG = "MemberNotes";
	
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
	
		view = inflater.inflate(R.layout.member_details_notes, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	/*public MemberActions getMemberActions(){
		return this.mActions;
	}*/
	
	
	private View setupContact() {
		
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		LinearLayout contactHeading = (LinearLayout) view.findViewById(R.id.contactHeadingRow);
		contactHeading.setOnClickListener(this);
		
		TextView glyph = (TextView) contactHeading.findViewById(R.id.contactGlyph);
		glyph.setTypeface(Services.Typefaces.get(getActivity(), "fonts/glyphicons_regular.ttf"));
		
		boolean vis_cell = false, vis_home = false, vis_work = false, vis_email = false;
		
		vis_cell = (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)) &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)).isEmpty());
		
		vis_home = (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)) &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)).isEmpty());
		
		vis_work = (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)) &&
				!cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)).isEmpty());
		
		vis_email = (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)) &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)).isEmpty());
		
		if (vis_cell || vis_home || vis_work || vis_email) {
			
			
			EditText cellphone = (EditText) view.findViewById(R.id.member_cell_phone);
			EditText workphone = (EditText) view.findViewById(R.id.member_work_phone);
			EditText homephone = (EditText) view.findViewById(R.id.member_home_phone);
			EditText emailaddress = (EditText) view.findViewById(R.id.member_email);
			
			cellphone.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
			workphone.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)));
			homephone.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)));
			emailaddress.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)));
			
		} else {
			contactHeading.setVisibility(View.GONE);
			LinearLayout contactDetails = (LinearLayout) view.findViewById(R.id.contactDetails);
			contactDetails.setVisibility(View.GONE);
			/*ImageView expand_collapse = (ImageView) view.findViewById(R.id.emergency_expand_collapse);
			expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));*/
		}
		
		cur.close();
		
		return view;
	}
	
	private View setupActions() {
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		LinearLayout email = (LinearLayout) view.findViewById(R.id.button_email);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)) != null) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)).compareTo("null") != 0) {
				email.setOnClickListener(this);
				email.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)));
			} else {
				email.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
				email.setClickable(false);
				TextView text = (TextView) email.findViewById(R.id.button_email_text);
				text.setTextColor(getActivity().getResources().getColor(R.color.grey));
			}
		} else {
			email.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			email.setClickable(false);
			TextView text = (TextView) email.findViewById(R.id.button_email_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
		}
		
		LinearLayout call = (LinearLayout) view.findViewById(R.id.button_call);
		LinearLayout sms = (LinearLayout) view.findViewById(R.id.button_sms);
		ArrayList<String> callTag = new ArrayList<String>();
		boolean has_number = false;
		
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)) != null &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)).compareTo("null") != 0) {
			
			callTag.add("Home: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)));
			has_number = true;
		}
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)) != null &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)).compareTo("null") != 0) {
			
			callTag.add("Work: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)));
			has_number = true;
		}
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)) != null &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)).compareTo("null") !=0) {
			
			sms.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
			sms.setOnClickListener(this);
			
			callTag.add("Cell: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
			has_number = true;
		} else {
			sms.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			sms.setClickable(false);
			TextView text = (TextView) sms.findViewById(R.id.button_sms_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
		}
		
		if (has_number) {
			call.setTag(callTag);
			call.setOnClickListener(this);
		} else {
			call.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			call.setClickable(false);
			TextView text = (TextView) call.findViewById(R.id.button_call_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
		}
		return view;
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
		
		TextView glyph = (TextView) emergencyHeading.findViewById(R.id.emergencyContactGlyph);
		glyph.setTypeface(Services.Typefaces.get(getActivity(), "fonts/glyphicons_regular.ttf"));
		
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
		detailsHeading.setOnClickListener(this);
		TextView glyph = (TextView) detailsHeading.findViewById(R.id.memberDetailsGlyph);
		glyph.setTypeface(Services.Typefaces.get(getActivity(), "fonts/glyphicons_regular.ttf"));
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY))||
				!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL))||
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
		TextView glyph = (TextView) medicalHeading.findViewById(R.id.memberMedicalGlyph);
		glyph.setTypeface(Services.Typefaces.get(getActivity(), "fonts/glyphicons_regular.ttf"));
		
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
		setupActions();
		setupContact();
		
		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
				ContentDescriptor.MemberNotes.Cols.MID+" = ?", new String[] {memberID}, 
				ContentDescriptor.MemberNotes.Cols.MNID+" DESC LIMIT 10");
		
		LinearLayout notesHeading = (LinearLayout) view.findViewById(R.id.notesHeadingRow);
		notesHeading.setOnClickListener(this);
		TextView glyph = (TextView) notesHeading.findViewById(R.id.memberNotesGlyph);
		glyph.setTypeface(Services.Typefaces.get(getActivity(), "fonts/glyphicons_regular.ttf"));
		
		LinearLayout.LayoutParams llparams;
		LinearLayout notesGroup = (LinearLayout) view.findViewById(R.id.membernotes);
		notesGroup.removeAllViews();
		while (cur.moveToNext()) {
			
			LinearLayout row = new LinearLayout(getActivity());
			row.setOrientation(LinearLayout.VERTICAL);
			llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			/*if (cur.getPosition()%2==0) {
				row.setBackgroundColor(getResources().getColor(R.color.button_background_grey));
			}*/
			row.setPadding(5, 10, 5, 10);
			
			llparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llparams.setMargins(5, 0, 0, 0);
			TextView notesT = new TextView(getActivity());
			notesT.setPadding(10, 0, 0, 0);
			notesT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.NOTES)));
			notesT.setTextSize(18);
			notesT.setLayoutParams(llparams);
			
			row.addView(notesT);
			//notesGroup.addView(notesT);
			
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
			
			//notesGroup.addView(notesdetails);
			row.addView(notesdetails);
			
			notesGroup.addView(row);
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
		
		//mActions.setupActions(view, memberID);
		return view;
	}
	
	private String getNewNote() {
		String note;
		
		EditText note_view = (EditText) view.findViewById(R.id.addnote);
		note = note_view.getText().toString();
		if (note == null || note.isEmpty() || note.compareTo(" ")==0) {
			note = null;
		}
		
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case (R.id.button_add_note):{
			//get text from edit text, add it to cache/pending uploads.
			String note = getNewNote();
			if (note == null) {
				Toast.makeText(getActivity(), "Cannot save empty note", Toast.LENGTH_LONG).show();
				break;
			}
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
		case (R.id.contactHeadingRow):{
			LinearLayout contact = (LinearLayout) view.findViewById(R.id.contactDetails);
			ImageView expand_collapse = (ImageView) view.findViewById(R.id.contact_expand_collapse);
			if (contact.isShown()) {
				contact.setVisibility(View.GONE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_expand));
			} else {
				contact.setVisibility(View.VISIBLE);
				expand_collapse.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_collapse));
			}
			break;
		}
		
		case (R.id.button_email):{
			String email="mailto:"+Uri.encode(v.getTag().toString())+"?subject="+Uri.encode("Gym Details");
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse(email));
			try {
				getActivity().startActivity(intent);
			} catch (ActivityNotFoundException e) { 
				Toast.makeText(getActivity(), "Cannot send emails from this device.", Toast.LENGTH_LONG).show();
			}
			break;
		}
		case (R.id.button_sms):{
			String smsNo = (String) v.getTag();
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			smsIntent.putExtra("address",smsNo);
			try {
				getActivity().startActivity(smsIntent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(getActivity(), "Cannot send SMS from this device.", Toast.LENGTH_LONG).show();
			}
			break;
		}
		case (R.id.button_call):{
			ArrayList<String> tag = null;
			if (v.getTag() instanceof ArrayList<?>) {
				tag = (ArrayList<String>) v.getTag();
			}
			if (tag.size() == 1) {
				String ph ="tel:"+tag.get(0).substring(tag.get(0).indexOf(":")+1);
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(ph));
				try {
					getActivity().startActivity(intent);
				} catch(ActivityNotFoundException e) {
					Toast.makeText(getActivity(), "Cannot make call's from this device.", Toast.LENGTH_LONG).show();
				}
			} 
			else {
				//show popup window, let user select the number to call.
				showPhoneWindow(tag);
			}
			break;
		}
		default:
			//mActions.onClick(v);
			break;
		}
	}
	
	private void showPhoneWindow(ArrayList<String> phones) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		
		for (int i=0; i< phones.size(); i +=1) {
			RadioButton rb = new RadioButton(getActivity());
			rb.setText(phones.get(i));
			rb.setTag(phones.get(i).substring(phones.get(i).indexOf(":")+1));
			rg.addView(rb);
		}	
        builder.setView(layout);
        builder.setTitle("Select Number to Call");
        builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            		
            		String selectedNo = null;
	            	int cid = rg.getCheckedRadioButtonId();  
	            	if (cid == -1) {
	            		Toast.makeText(getActivity(), "Select a Phone Number", Toast.LENGTH_LONG).show();
	            		
	            	} else {
		            	RadioButton rb = (RadioButton) rg.findViewById(cid);
		            	selectedNo = (String) rb.getTag();
		    
		            	String ph ="tel:"+selectedNo;
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse(ph));
						getActivity().startActivity(intent);
	            	}
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}

	@Override
	public boolean onNewTag(String serial) {
	return false;
	}
}