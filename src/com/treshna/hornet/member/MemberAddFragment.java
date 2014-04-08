package com.treshna.hornet.member;

import java.util.ArrayList;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.R.string;
import com.treshna.hornet.membership.MembershipAdd;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.DatePickerFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.services.DatePickerFragment.DatePickerSelectListener;
import com.treshna.hornet.services.Services.Statics;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.FreeIds;
import com.treshna.hornet.sqlite.ContentDescriptor.Member;
import com.treshna.hornet.sqlite.ContentDescriptor.TableIndex;
import com.treshna.hornet.sqlite.ContentDescriptor.Member.Cols;
import com.treshna.hornet.sqlite.ContentDescriptor.TableIndex.Values;
import com.treshna.hornet.visitor.VisitorsViewAdapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MemberAddFragment extends Fragment implements OnClickListener, DatePickerFragment.DatePickerSelectListener{

	DatePickerFragment mDatePicker;
	private View view;
	private LayoutInflater mInflater;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.member_add);
		mInflater = inflater;
		view = (View) mInflater.inflate(R.layout.member_add, null); 
		Services.setContext(getActivity());
		
		/*
		 * This was the private-hidden setting used for determining if the last
		 * insert was a member or a prospect.
		 */
		setupView();
		return view;
	}
	
	private void setupView() {
	    RadioButton radio = (RadioButton) view.findViewById(R.id.radioMember);
		radio.setChecked(true);

		TextView accept = (TextView) view.findViewById(R.id.buttonAccept);
		TextView cancel = (TextView) view.findViewById(R.id.buttonCancel);
		//TextView clear = (TextView) this.findViewById(R.id.buttonClear);
		//click-handling
		accept.setClickable(true);
		accept.setOnClickListener(this);
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		/*clear.setClickable(true);
		clear.setOnClickListener(this);*/
		
		LinearLayout buttondob = (LinearLayout) view.findViewById(R.id.button_member_dob);
		buttondob.setOnClickListener(this);
		mDatePicker = new DatePickerFragment();
		mDatePicker.setDatePickerSelectListener(this);
		
		displayID();
		
		// Show the Up button in the action bar.
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case (R.id.buttonAccept):
			//do input checking (mandatory fields are filled in).
			//pass off to something else to do escaping (Services?)
			ArrayList<String> emptyFields = formCheck();
			if (emptyFields.get(0).compareTo("true") == 0){
				Toast.makeText(getActivity(), "Information Received!", Toast.LENGTH_LONG).show();
				ArrayList<String> input = getInput();
				System.out.print("\n*input: "+input.toString());
				
				//do if check to make sure a memberid was received.
				String memberid = null;
				if (input.size() == 14){
					memberid = input.get(13);
				}
				Intent intent = new Intent(getActivity(), HornetDBService.class);
				
				intent.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
				new InsertMember(getActivity(), intent, input.get(0), input.get(1), input.get(2), input.get(3), input.get(4),
						input.get(5), input.get(6), input.get(7), input.get(8), input.get(9), input.get(10), input.get(11), input.get(12), memberid);
				//InsertMember member = above line
				clearForm();
				
				ArrayList<String> tag = new ArrayList<String>();
				tag.add(memberid);
				tag.add(null);
				
				Fragment f = new MemberDetailsFragment();
				Bundle bdl = new Bundle(1);
				bdl.putStringArrayList(VisitorsViewAdapter.EXTRA_ID, tag);
				f.setArguments(bdl);
				((MainActivity)getActivity()).changeFragment(f, "memberDetails");
				/*Intent i = new Intent(getActivity(), EmptyActivity.class);
				i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MemberDetails.getKey());
				i.putStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID, tag);
				this.startActivity(i);*/
				
				f = new MembershipAdd();
				bdl = new Bundle(1);
				bdl.putString(Services.Statics.MID, memberid);
				f.setArguments(bdl);
				((MainActivity)getActivity()).changeFragment(f, "MembershipAdd");
				/*i = new Intent(getActivity(), EmptyActivity.class);
				i.putExtra(Services.Statics.MID, memberid);
				i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MembershipAdd.getKey());
				this.startActivity(i);*/
				
				
			}else {
				updateView(emptyFields);
				Toast.makeText(getActivity(), "please fill out the high-lighted fields", Toast.LENGTH_LONG).show();
			}
			break;
		case (R.id.buttonCancel):
			getActivity().onBackPressed();
			break;
	/*case (R.id.buttonClear):
			clearForm();
			break;*/
		case (R.id.button_member_dob):{
			//mDatePicker.show(this.getSupportFragmentManager(), "datePicker");
			mDatePicker.show(this.getChildFragmentManager(), "datePicker");
			break;
		}
		}
		
	}
	
	private void displayID(){
		Cursor cur = null;
		ContentResolver contentResolver = getActivity().getContentResolver();
		//String[] projection = {ContentDescriptor.Member.Cols.MID};
		String[] projection = {ContentDescriptor.FreeIds.Cols.ROWID};
		
		/*cur = contentResolver.query(ContentDescriptor.Member.URI_FREE_IDS, projection, 
				ContentDescriptor.Member.Cols.STATUS+" = -1", null, null);*/
		cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, projection, 
				ContentDescriptor.FreeIds.Cols.TABLEID+" = "+ContentDescriptor.TableIndex.Values.Member.getKey(),
				null, null);
		if (cur.moveToFirst()) {
			TextView memberid = (TextView) view.findViewById(R.id.memberNo);
			memberid.setText(cur.getString(0));
		}
		cur.close();
	}
	/* 
	 * for each input field in view, check if it's empty/not-set
	 * return list of containing the empty/not-set fields
	 * list[0] is boolean status, true on no-empties, else false.
	 * Also needs to set getSharedPreferences(MainActivity.PREF_NAME) -->MainActivity.PREF_KEY
	 * 
	 * It also needs to reset text to black on "OK"
	 */
	private ArrayList<String> formCheck(){
		boolean result = true;
		ArrayList<String> emptyFields = new ArrayList<String>();
		
		EditText memberFirstName = (EditText) view.findViewById(R.id.memberFirstName);
		if (memberFirstName.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberFirstName");
			emptyFields.add(String.valueOf(R.id.labelFirstName));
		} else {
			TextView label = (TextView) view.findViewById(R.id.labelFirstName);
			label.setTextColor(Color.BLACK);
		}
		
		EditText memberSurname = (EditText) view.findViewById(R.id.memberSurname);
		if (memberSurname.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberSurname");
			emptyFields.add(String.valueOf(R.id.labelSurname));
		} else {
			TextView label = (TextView) view.findViewById(R.id.labelSurname);
			label.setTextColor(Color.BLACK);
		}
		
		TextView memberDoB = (TextView) view.findViewById(R.id.member_dob_text);
		String dob = memberDoB.getText().toString();
		if (dob.compareTo("") == 0 || dob.compareTo(getString(R.string.defaultDoB)) ==0) {
			result = false;
			emptyFields.add("memberDoB");
			emptyFields.add(String.valueOf(R.id.labelDoB));
		} else {
			TextView label = (TextView) view.findViewById(R.id.labelDoB);
			label.setTextColor(Color.BLACK);
		}
		
		RadioButton male = (RadioButton) view.findViewById(R.id.radioMale);
		RadioButton female = (RadioButton) view.findViewById(R.id.radioFemale);
		if (male.isChecked() != true && female.isChecked() != true) {
			result = false;
			emptyFields.add("memberGender");
			emptyFields.add(String.valueOf(R.id.labelGender));
		} else {
			TextView label = (TextView) view.findViewById(R.id.labelGender);
			label.setTextColor(Color.BLACK);
		}
		
		
		EditText memberHome = (EditText) view.findViewById(R.id.memberHomePhone);
		if (memberHome.getText().toString().compareTo("") == 0) {
			EditText memberCell = (EditText) view.findViewById(R.id.memberCellPhone);
			if (memberCell.getText().toString().compareTo("") ==0) {
				//neither home or cell are set. which one should I highlight?
				result = false;
				emptyFields.add("memberCellPhone");
				emptyFields.add(String.valueOf(R.id.labelCellPhone));
			} else {
				TextView label = (TextView) view.findViewById(R.id.labelCellPhone);
				label.setTextColor(Color.BLACK);
			}
		} else {
			TextView label = (TextView) view.findViewById(R.id.labelCellPhone);
			label.setTextColor(Color.BLACK);
		}
		
		RadioButton member = (RadioButton) view.findViewById(R.id.radioMember);
		RadioButton prospect = (RadioButton) view.findViewById(R.id.radioProspect);
		if (member.isChecked() != true && prospect.isChecked() != true) {
			result = false;
			emptyFields.add("memberSignupType");
			emptyFields.add(String.valueOf(R.id.labelSignupType));
		} else {
			SharedPreferences memberAdd = getActivity().getSharedPreferences(Services.Statics.PREF_NAME, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = memberAdd.edit();
			if (member.isChecked()) editor.putInt(Services.Statics.PREF_KEY, R.id.radioMember);
			else if (prospect.isChecked()) editor.putInt(Services.Statics.PREF_KEY, R.id.radioProspect);
			editor.commit();
		}
		// rest of the fields optional?
		
		EditText email = (EditText) view.findViewById(R.id.memberEmail);
		String memberEmail = email.getText().toString(); 
		if (memberEmail.compareTo("") != 0) {
			// below regex checks that the email looks valid
			if (memberEmail.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
					) == false) {
				result = false;
				emptyFields.add("memberEmail");
				emptyFields.add(String.valueOf(R.id.labelEmail));
			} else {
				TextView label = (TextView) view.findViewById(R.id.labelEmail);
				label.setTextColor(Color.BLACK);
			}
		} else {
			TextView label = (TextView) view.findViewById(R.id.labelEmail);
			label.setTextColor(Color.BLACK);
		}
		
		emptyFields.add(0,String.valueOf(result));
		return emptyFields;
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		int i;
		for(i=1; i<emptyFields.size(); i+=2){
			//get label, change colour?
			//String view = "R.id."+emptyFields.get(i);
			TextView label = (TextView) view.findViewById(Integer.parseInt(emptyFields.get(i+1)));
			label.setTextColor(Color.RED);
		}
	}
	
	private void clearForm() {
		ArrayList<Integer> viewlist = new ArrayList<Integer>();
		viewlist.add(R.id.memberFirstName);
		viewlist.add(R.id.memberSurname);
		viewlist.add(R.id.memberMedical);
		viewlist.add(R.id.memberStreet);
		viewlist.add(R.id.memberSuburb);
		viewlist.add(R.id.memberCity);
		viewlist.add(R.id.memberPostal);
		viewlist.add(R.id.memberEmail);
		viewlist.add(R.id.memberHomePhone);
		viewlist.add(R.id.memberCellPhone);
		for (int i = 0; i<viewlist.size(); i+=1){
			EditText edit_view = (EditText) view.findViewById(viewlist.get(i));
			edit_view.setText(null);
		}
		RadioGroup gender = (RadioGroup) view.findViewById(R.id.memberGender);
		gender.clearCheck();
		
		// label-list
		viewlist.clear();
		viewlist.add(R.id.labelFirstName);
		viewlist.add(R.id.labelSurname);
		viewlist.add(R.id.labelDoB);
		viewlist.add(R.id.labelGender);
		viewlist.add(R.id.labelStreet);
		viewlist.add(R.id.labelCity);
		viewlist.add(R.id.labelPostal);
		viewlist.add(R.id.labelEmail);
		viewlist.add(R.id.labelSignupType);
		viewlist.add(R.id.labelCellPhone);
		viewlist.add(R.id.member_dob_text);
		for (int i = 0; i<viewlist.size(); i+=1){
			TextView label = (TextView) view.findViewById(viewlist.get(i));
			label.setTextColor(Color.BLACK);
		}
	}
	
	private ArrayList<String> getInput(){
		ArrayList<String> inputData = new ArrayList<String>();
		ArrayList<Integer> fields = new ArrayList<Integer>();
		fields.add(R.id.memberFirstName);
		fields.add(R.id.memberSurname);
		//dob
		//gender
		fields.add(R.id.memberMedical);
		fields.add(R.id.memberStreet);
		fields.add(R.id.memberSuburb);
		fields.add(R.id.memberCity);
		fields.add(R.id.memberPostal);
		fields.add(R.id.memberEmail);
		fields.add(R.id.memberHomePhone);
		fields.add(R.id.memberCellPhone);
		//signup type
		// member id
		int i = 0;
		for (i = 0; i < fields.indexOf(R.id.memberMedical); i +=1){
			EditText edit_view = (EditText) view.findViewById(fields.get(i));
			String input = edit_view.getText().toString();
			if (input.compareTo("") !=0) {
				inputData.add(input);
			} else {
				inputData.add(null);
		}	}
		//get date of birth
		TextView dob = (TextView) view.findViewById(R.id.member_dob_text);
		inputData.add(Services.dateFormat(dob.getText().toString(), "dd MMM yyyy", "dd/MM/yyyy"));
		//inputData.add(dob.getText().toString());
		
		RadioGroup rgroup = (RadioGroup) view.findViewById(R.id.memberGender);
		int id = rgroup.getCheckedRadioButtonId();
		RadioButton gender = (RadioButton) view.findViewById(id);
		inputData.add(gender.getText().toString());
		
		for (i = fields.indexOf(R.id.memberMedical); i < fields.size(); i +=1) {
			EditText edit_view = (EditText) view.findViewById(fields.get(i));
			String input = edit_view.getText().toString();
			if (input.compareTo("") !=0) {
				inputData.add(input);
			} else {
				inputData.add(null);
		}	}
		rgroup = (RadioGroup) view.findViewById(R.id.memberSignupType);
		id = rgroup.getCheckedRadioButtonId();
		RadioButton signup = (RadioButton) view.findViewById(id);
		inputData.add(signup.getText().toString());
		
		TextView idview = (TextView) view.findViewById(R.id.memberNo);
		String memberid = idview.getText().toString();
		if (memberid.compareTo(getString(R.string.errorMemberNo)) != 0) {
			inputData.add(memberid);
		}

		return inputData;
	}
	
	private class InsertMember {
			
		private Context cntxt = null;
			
		public InsertMember(Context context, Intent intent, String firstName, String surname, String dob, String gender, String medical,
				String street, String suburb, String city, String postal, String email, String homePh,
				String cellPh, String signup, String memberid ){

			this.cntxt = context;
			ContentResolver contentResolver = cntxt.getContentResolver();
			
			// add prospect/member to local database, attempt to upload to server.
			ContentValues val = new ContentValues();
			
			val.put(ContentDescriptor.Member.Cols.FNAME, firstName);
			val.put(ContentDescriptor.Member.Cols.SNAME, surname);
			val.put(ContentDescriptor.Member.Cols.DOB, dob);
			val.put(ContentDescriptor.Member.Cols.GENDER, gender);
			val.put(ContentDescriptor.Member.Cols.MEDICAL, medical);
			val.put(ContentDescriptor.Member.Cols.STREET, street);
			val.put(ContentDescriptor.Member.Cols.SUBURB, suburb);
			val.put(ContentDescriptor.Member.Cols.CITY, city);
			val.put(ContentDescriptor.Member.Cols.POSTAL, postal);
			val.put(ContentDescriptor.Member.Cols.EMAIL, email);
			val.put(ContentDescriptor.Member.Cols.PHHOME, homePh);
			val.put(ContentDescriptor.Member.Cols.PHCELL, cellPh);
			val.put(ContentDescriptor.Member.Cols.DEVICESIGNUP, "t");

			if (memberid == null){
				
				val.put(ContentDescriptor.Member.Cols.MID, -1);
				contentResolver.insert(ContentDescriptor.Member.CONTENT_URI, val);
				
			} else {
				val.put(ContentDescriptor.Member.Cols.MID, memberid);
				contentResolver.insert(ContentDescriptor.Member.CONTENT_URI, val);
				
				contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
						+ContentDescriptor.FreeIds.Cols.TABLEID+" = "+ContentDescriptor.TableIndex.Values.Member.getKey(), 
						new String[] {memberid});
				/*String[] selection = {memberid};
				Cursor cur = contentResolver.query(ContentDescriptor.Member.URI_FREE_IDS, null,
						ContentDescriptor.Member.Cols.MID+" = ?", selection, null);
				
				cur.moveToFirst();
				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols._ID));
				cur.close();
				
				contentResolver.update(ContentDescriptor.Member.CONTENT_URI, val, 
						ContentDescriptor.Member.Cols.MID+" = ?", selection);
				val = new ContentValues();
				val.put(ContentDescriptor.PendingUploads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Member.getKey());
				val.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);
				contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, val);*/
			}			
			
			getActivity().startService(intent);
 		}
	}
	
	private void setText(String date) {
		TextView dob = (TextView) view.findViewById(R.id.member_dob_text);
		dob.setText(date);
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		if (theDatePicker == mDatePicker) {
			setText(date);
		}
		
	}
}
