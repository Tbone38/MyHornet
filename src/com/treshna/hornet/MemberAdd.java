package com.treshna.hornet;

import java.util.ArrayList;

import android.annotation.TargetApi;
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
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MemberAdd extends NFCActivity implements OnClickListener{

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_add);
		Services.setContext(this);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		/*
		 * This is the private-hidden setting used for determining if the last
		 * insert was a member or a prospect.
		 */
		SharedPreferences preferences = this.getSharedPreferences(Services.Statics.PREF_NAME, MODE_PRIVATE);
		int id = preferences.getInt(Services.Statics.PREF_KEY, -1);
		if (id != -1){
			RadioButton radio = (RadioButton) this.findViewById(id);
			radio.setChecked(true);
		}
		else {
			RadioButton radio = (RadioButton) this.findViewById(R.id.radioMember);
			radio.setChecked(true);
		}

		TextView accept = (TextView) this.findViewById(R.id.buttonAccept);
		TextView cancel = (TextView) this.findViewById(R.id.buttonCancel);
		TextView clear = (TextView) this.findViewById(R.id.buttonClear);
		//click-handling
		accept.setClickable(true);
		accept.setOnClickListener(this);
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		clear.setClickable(true);
		clear.setOnClickListener(this);
		
		displayID();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.not_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
	    case (R.id.action_home):{
	    	Intent i = new Intent (this, MainActivity.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_createclass):{
	    	Intent i = new Intent(this, ClassCreate.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_settings):
	    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	return true;
	    case (R.id.action_update): {
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
		 		Services.setPreference(this, "sync_frequency", "5");
		 	}
		 	PollingHandler polling = Services.getPollingHandler();
	    	polling.startService();
	    	return true;
	    }
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    }
	    /*case (R.id.action_bookings):{
	    	Intent bookings = new Intent(this, HornetDBService.class);
			bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
		 	this.startService(bookings);
	    	
		 	Intent intent = new Intent(this, BookingsSlidePager.class);
	       	startActivity(intent);
	       	return true;
	    }*/
	    case (R.id.action_addMember):{
	    	Intent intent = new Intent(this, MemberAdd.class);
	    	startActivity(intent);
	    	return true;
	    }	    
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case (R.id.buttonAccept):
			//do input checking (mandatory fields are filled in).
			//pass off to something else to do escaping (Services?)
			ArrayList<String> emptyFields = formCheck();
			if (emptyFields.get(0).compareTo("true") == 0){
				Toast.makeText(this, "Information Received!", Toast.LENGTH_LONG).show();
				ArrayList<String> input = getInput();
				System.out.print("\n*input: "+input.toString());
				
				//do if check to make sure a memberid was received.
				String memberid = null;
				if (input.size() == 14){
					memberid = input.get(13);
				}
				Intent intent = new Intent(this, HornetDBService.class);
				//intent.putExtra(Services.Statics.KEY,Services.Statics.UPLOAD);
				intent.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
				new InsertMember(this, intent, input.get(0), input.get(1), input.get(2), input.get(3), input.get(4),
						input.get(5), input.get(6), input.get(7), input.get(8), input.get(9), input.get(10), input.get(11), input.get(12), memberid);
				//InsertMember member = above line
				clearForm();
				displayID();
			}else {
				updateView(emptyFields);
				Toast.makeText(this, "please fill out the high-lighted fields", Toast.LENGTH_LONG).show();
			}
			break;
		case (R.id.buttonCancel):
			NavUtils.navigateUpFromSameTask(this);
			break;
		case (R.id.buttonClear):
			clearForm();
			break;
		}
	}
	
	private void displayID(){
		Cursor cur = null;
		ContentResolver contentResolver = this.getContentResolver();
		String[] projection = {ContentDescriptor.Pending.Cols.MID};
		cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, projection, ContentDescriptor.Pending.Cols.ISUSED
				+" = 0", null, null);
		cur.moveToFirst();
		if (!cur.isBeforeFirst()) {
			TextView memberid = (TextView) this.findViewById(R.id.memberNo);
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
		
		EditText memberFirstName = (EditText) this.findViewById(R.id.memberFirstName);
		if (memberFirstName.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberFirstName");
			emptyFields.add(String.valueOf(R.id.labelFirstName));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelFirstName);
			label.setTextColor(Color.BLACK);
		}
		
		EditText memberSurname = (EditText) this.findViewById(R.id.memberSurname);
		if (memberSurname.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberSurname");
			emptyFields.add(String.valueOf(R.id.labelSurname));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelSurname);
			label.setTextColor(Color.BLACK);
		}
		
		EditText memberDoB = (EditText) this.findViewById(R.id.memberDoB);
		String dob = memberDoB.getText().toString();
		if (dob.compareTo("") == 0 || !Services.validDate(dob, "\\d{2}/\\d{2}/\\d{4}", "dd/MM/yyyy")) {
			result = false;
			emptyFields.add("memberDoB");
			emptyFields.add(String.valueOf(R.id.labelDoB));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelDoB);
			label.setTextColor(Color.BLACK);
		}
		
		RadioButton male = (RadioButton) this.findViewById(R.id.radioMale);
		RadioButton female = (RadioButton) this.findViewById(R.id.radioFemale);
		if (male.isChecked() != true && female.isChecked() != true) {
			result = false;
			emptyFields.add("memberGender");
			emptyFields.add(String.valueOf(R.id.labelGender));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelGender);
			label.setTextColor(Color.BLACK);
		}
		/******************* probably don't need street, city, postal************************/
		/*EditText memberStreet = (EditText) this.findViewById(R.id.memberStreet);
		if (memberStreet.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberStreet");
			emptyFields.add(String.valueOf(R.id.labelStreet));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelStreet);
			label.setTextColor(Color.BLACK);
		}
		
		EditText memberCity = (EditText) this.findViewById(R.id.memberCity);
		if (memberCity.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberCity");
			emptyFields.add(String.valueOf(R.id.labelCity));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelCity);
			label.setTextColor(Color.BLACK);
		}
		
		EditText memberPostal = (EditText) this.findViewById(R.id.memberPostal);
		if (memberPostal.getText().toString().compareTo("") == 0) {
			result = false;
			emptyFields.add("memberPostal");
			emptyFields.add(String.valueOf(R.id.labelPostal));
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelPostal);
			label.setTextColor(Color.BLACK);
		}*/
		/**** REPLACE above with phone (need either home or cell) ****/
		
		EditText memberHome = (EditText) this.findViewById(R.id.memberHomePhone);
		if (memberHome.getText().toString().compareTo("") == 0) {
			EditText memberCell = (EditText) this.findViewById(R.id.memberCellPhone);
			if (memberCell.getText().toString().compareTo("") ==0) {
				//neither home or cell are set. which one should I highlight?
				result = false;
				emptyFields.add("memberCellPhone");
				emptyFields.add(String.valueOf(R.id.labelCellPhone));
			} else {
				TextView label = (TextView) this.findViewById(R.id.labelCellPhone);
				label.setTextColor(Color.BLACK);
			}
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelCellPhone);
			label.setTextColor(Color.BLACK);
		}
		
		RadioButton member = (RadioButton) this.findViewById(R.id.radioMember);
		RadioButton prospect = (RadioButton) this.findViewById(R.id.radioProspect);
		if (member.isChecked() != true && prospect.isChecked() != true) {
			result = false;
			emptyFields.add("memberSignupType");
			emptyFields.add(String.valueOf(R.id.labelSignupType));
		} else {
			SharedPreferences memberAdd = this.getSharedPreferences(Services.Statics.PREF_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = memberAdd.edit();
			if (member.isChecked()) editor.putInt(Services.Statics.PREF_KEY, R.id.radioMember);
			else if (prospect.isChecked()) editor.putInt(Services.Statics.PREF_KEY, R.id.radioProspect);
			editor.commit();
		}
		// rest of the fields optional?
		
		EditText email = (EditText) this.findViewById(R.id.memberEmail);
		String memberEmail = email.getText().toString(); 
		if (memberEmail.compareTo("") != 0) {
			// below regex checks that the email looks valid
			if (memberEmail.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
					) == false) {
				result = false;
				emptyFields.add("memberEmail");
				emptyFields.add(String.valueOf(R.id.labelEmail));
			} else {
				TextView label = (TextView) this.findViewById(R.id.labelEmail);
				label.setTextColor(Color.BLACK);
			}
		} else {
			TextView label = (TextView) this.findViewById(R.id.labelEmail);
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
			TextView label = (TextView) this.findViewById(Integer.parseInt(emptyFields.get(i+1)));
			label.setTextColor(Color.RED);
		}
	}
	
	private void clearForm() {
		ArrayList<Integer> viewlist = new ArrayList<Integer>();
		viewlist.add(R.id.memberFirstName);
		viewlist.add(R.id.memberSurname);
		viewlist.add(R.id.memberDoB);
		viewlist.add(R.id.memberMedical);
		viewlist.add(R.id.memberStreet);
		viewlist.add(R.id.memberSuburb);
		viewlist.add(R.id.memberCity);
		viewlist.add(R.id.memberPostal);
		viewlist.add(R.id.memberEmail);
		viewlist.add(R.id.memberHomePhone);
		viewlist.add(R.id.memberCellPhone);
		for (int i = 0; i<viewlist.size(); i+=1){
			EditText view = (EditText) this.findViewById(viewlist.get(i));
			view.setText(null);
		}
		RadioGroup gender = (RadioGroup) this.findViewById(R.id.memberGender);
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
		for (int i = 0; i<viewlist.size(); i+=1){
			TextView label = (TextView) this.findViewById(viewlist.get(i));
			label.setTextColor(Color.BLACK);
		}
	}
	
	private ArrayList<String> getInput(){
		ArrayList<String> inputData = new ArrayList<String>();
		ArrayList<Integer> fields = new ArrayList<Integer>();
		fields.add(R.id.memberFirstName);
		fields.add(R.id.memberSurname);
		fields.add(R.id.memberDoB);
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
		for (i = 0; i <= fields.indexOf(R.id.memberDoB); i +=1){
			EditText view = (EditText) this.findViewById(fields.get(i));
			String input = view.getText().toString();
			if (input.compareTo("") !=0) {
				inputData.add(input);
			} else {
				inputData.add(null);
		}	}
		RadioGroup rgroup = (RadioGroup) this.findViewById(R.id.memberGender);
		int id = rgroup.getCheckedRadioButtonId();
		RadioButton gender = (RadioButton) this.findViewById(id);
		inputData.add(gender.getText().toString());
		
		for (i = fields.indexOf(R.id.memberDoB)+1; i < fields.size(); i +=1) {
			EditText view = (EditText) this.findViewById(fields.get(i));
			String input = view.getText().toString();
			if (input.compareTo("") !=0) {
				inputData.add(input);
			} else {
				inputData.add(null);
		}	}
		rgroup = (RadioGroup) this.findViewById(R.id.memberSignupType);
		id = rgroup.getCheckedRadioButtonId();
		RadioButton signup = (RadioButton) this.findViewById(id);
		inputData.add(signup.getText().toString());
		
		TextView idview = (TextView) this.findViewById(R.id.memberNo);
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
			val.put(ContentDescriptor.Pending.Cols.FNAME, firstName);
			val.put(ContentDescriptor.Pending.Cols.SNAME, surname);
			val.put(ContentDescriptor.Pending.Cols.DOB, dob);
			val.put(ContentDescriptor.Pending.Cols.GENDER, gender);
			val.put(ContentDescriptor.Pending.Cols.MEDICAL, medical);
			val.put(ContentDescriptor.Pending.Cols.STREET, street);
			val.put(ContentDescriptor.Pending.Cols.SUBURB, suburb);
			val.put(ContentDescriptor.Pending.Cols.CITY, city);
			val.put(ContentDescriptor.Pending.Cols.POSTAL, postal);
			val.put(ContentDescriptor.Pending.Cols.EMAIL, email);
			val.put(ContentDescriptor.Pending.Cols.HPHONE, homePh);
			val.put(ContentDescriptor.Pending.Cols.CPHONE, cellPh);
			val.put(ContentDescriptor.Pending.Cols.SIGNUP, signup);
			//is this safe from SQL injection?

			if (memberid == null){
				
				val.put(ContentDescriptor.Pending.Cols.ISUSED, 2);
				contentResolver.insert(ContentDescriptor.Pending.CONTENT_URI, val);
				
			} else {
				if (signup.compareTo(getString(R.string.radioProspect)) == 0){
					val.put(ContentDescriptor.Pending.Cols.ISUSED, 1);
					contentResolver.insert(ContentDescriptor.Pending.CONTENT_URI, val);
				}
				else {
					val.put(ContentDescriptor.Pending.Cols.ISUSED, 1);
					String[] selection = {memberid};
					contentResolver.update(ContentDescriptor.Pending.CONTENT_URI, val, ContentDescriptor.Pending.Cols.MID+" = ?", selection); 
				}
			}			
			// notify user of pending uploads.
			Cursor cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 1", null, null);
			if (cur.getCount() > 0){
				Toast.makeText(getApplicationContext(),cur.getCount()+" Sign-Ups pending, please sync to resolve.", Toast.LENGTH_LONG).show();
			}
			cur.close();
			
			startService(intent);
		  // TESTING
		  	cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, null, null, null);
			cur.moveToFirst();
			for (int i=0;i<cur.getCount();i+=1){
				for (int j=0;j<cur.getColumnCount();j+=1){
					System.out.print("\n Row:"+i+"  column:"+j+" title: "+cur.getColumnName(j)+" Value:"+cur.getString(j));
				}
				cur.moveToNext();
			}
			cur.close();
		}
	}
}
