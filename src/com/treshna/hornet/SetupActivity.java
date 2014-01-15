package com.treshna.hornet;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
/**
 * Here there 'be magic.
 */
public class SetupActivity extends FragmentActivity implements OnClickListener {
	private static final int ADDUSER=11;
	private static final int UPDATEUSER=12;
	/** 
	 * 
	 * We need a way to check if the app is being run for the first time.							-DONE
	 * If so, we need to offer either advanced or simple setup.										-DONE
	 * 	Advanced being something like what's in the settings at the moment.
	 * 
	 * 	Simple being 'enter email address', setup username & password.
	 * 		then we magically generate a Database on one of the servers.
	 * 		Said magical database will need a unique name based on what the company? is called.
	 * 
	 * 		this all needs to go through a webpage, to ensure data security.
	 * 
	 * If it's not the first run, we need to prompt for a username & password. OR retrieve a saved
	 * username & password from the app settings. 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_activity);
		Intent caller = this.getIntent();
		String theview = caller.getStringExtra(Services.Statics.KEY);
		
		if (theview == null) {
			//do we want to have these as fragments? or just reuse the one Activity?
			setContentView(R.layout.setup_page_one);
			setupPageOne();
		} else if (theview.compareTo("simple")==0) {
			setContentView(R.layout.setup_page_simple);
			setupSimple();
		} else if (theview.compareTo("advanced")==0) {
			setContentView(R.layout.setup_page_advanced);
			setupAdvanced();
		} else if (theview.compareTo("simple2")==0) {
			setContentView(R.layout.setup_page2_simple);
			setupSimpleTwo();
		}
	}
	
	
	private void setupPageOne() {
		TextView simple = (TextView) this.findViewById(R.id.button_simple_setup);
		simple.setOnClickListener(this);
		
		TextView advanced = (TextView) this.findViewById(R.id.button_advanced_setup);
		advanced.setOnClickListener(this);
	}
	
	private void setupSimple(){
		TextView button_accept = (TextView) this.findViewById(R.id.button_simple_continue);
		button_accept.setOnClickListener(this);
		
		TextView button_cancel = (TextView) this.findViewById(R.id.button_simple_cancel);
		button_cancel.setOnClickListener(this);
		
		// then we prompt them to select a username (or we generate one),
		// we then email them there password.
		// while they retrieve there password, we generate the Database on a server.
		// we save the name of the database, as well as the server it's on, in the central database.
		// we tell the app the name and server.
		// probably going to want a crazy YAPF website to pass and return JSON?
	}
	
	private ArrayList<String> validateSimple() {
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		EditText email_address, company_name;
		
		email_address = (EditText) this.findViewById(R.id.setup_email_address);
		if (email_address.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_simple_email_text));
		} else {
			TextView label = (TextView) this.findViewById(R.id.setup_simple_email_text);
			label.setTextColor(Color.BLACK);
		}
		
		company_name = (EditText) this.findViewById(R.id.setup_company_name);
		if (company_name.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_simple_company_name_text));
		} else {
			TextView label = (TextView) this.findViewById(R.id.setup_simple_company_name_text);
			label.setTextColor(Color.BLACK);
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}
	
	private ArrayList<String> getSimpleInput() {
		ArrayList<String> inputs = new ArrayList<String>();
		
		EditText email_address = (EditText) this.findViewById(R.id.setup_email_address);
		inputs.add(email_address.getText().toString()); 									//0
		
		EditText organisation = (EditText) this.findViewById(R.id.setup_company_name);
		inputs.add(organisation.getText().toString());										//1
		
		return inputs;
	}
	
	private boolean addUser() {
		ArrayList<String> inputs = getSimpleInput();
		this.storeEmail(inputs.get(0));
		JSONSync async = new JSONSync(SetupActivity.ADDUSER);
		async.execute(inputs.get(0), inputs.get(1));
		
		return false;
	}
	
	private void setupSimpleTwo(){
		TextView button_accept = (TextView) this.findViewById(R.id.button_simple_accept);
		button_accept.setOnClickListener(this);
		
		TextView button_cancel = (TextView) this.findViewById(R.id.button_simple_cancel);
		button_cancel.setOnClickListener(this);
	}
	
	private ArrayList<String> validateSimpleTwo() {
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		EditText username_input = (EditText) this.findViewById(R.id.setup_username);
		if (username_input.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_simple_username));
		} else {
			TextView label = (TextView) this.findViewById(R.id.setup_simple_username);
			label.setTextColor(Color.BLACK);
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}
	
	private ArrayList<String> getSimpleTwoInput(){
		ArrayList<String> inputs = new ArrayList<String>();
		
		EditText username_input = (EditText) this.findViewById(R.id.setup_username);
		inputs.add(username_input.getText().toString());						//position 0
		
		EditText location_input = (EditText) this.findViewById(R.id.setup_country);
		if (location_input.getText().toString().compareTo("")==0) {
			inputs.add("NZ"); //default NZ;										//position 1
		} else {
			inputs.add(location_input.getText().toString());
		}
		
		return inputs;
	}
	
	private boolean updateUser() {
		ArrayList<String> inputs = getSimpleTwoInput();
		
		String email = this.getEmail();
		if (email == null) {
			return false;
		}
		JSONSync async = new JSONSync(SetupActivity.UPDATEUSER);
		async.execute(email, inputs.get(0), inputs.get(1));
		
		return false;
	}
	
	private void setupAdvanced(){
		TextView button_accept = (TextView) this.findViewById(R.id.button_advanced_accept);
		button_accept.setOnClickListener(this);
		
		TextView button_cancel = (TextView) this.findViewById(R.id.button_advanced_cancel);
		button_cancel.setOnClickListener(this);
	}
	
	private ArrayList<String> validateAdvanced() {
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		EditText db_address = (EditText) this.findViewById(R.id.setup_database_address);
		if (db_address.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_advanced_server_address));
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}
	
	private boolean storeEmail(String email) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString("email", email);
		
		return editor.commit();
	}
	
	private String getEmail(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		return preferences.getString("email", null);
	}
	
	private boolean getAdvancedInput(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		
		EditText db_address, db_name, db_port, db_user, db_password;
		
		db_address = (EditText) this.findViewById(R.id.setup_database_address);
		editor.putString("address", db_address.getText().toString());
		
		db_name = (EditText) this.findViewById(R.id.setup_database_name);
		if (db_name.getText().toString().compareTo("")==0){ //no input, use defaults.
			editor.putString("database", "gymmaster");
		} else {
			editor.putString("database", db_name.getText().toString());
		}
		
		db_port = (EditText) this.findViewById(R.id.setup_database_port);
		if (db_port.getText().toString().compareTo("")==0){ //no input, use defaults.
			editor.putString("port", "5432");
		} else {
			editor.putString("port", db_port.getText().toString());
		}
		
		db_user = (EditText) this.findViewById(R.id.setup_user_name);
		if (db_user.getText().toString().compareTo("")==0){ //no input, use default.
			editor.putString("username", "gymmaster");
		} else {
			editor.putString("username", db_user.getText().toString());
		}
		
		db_password = (EditText) this.findViewById(R.id.setup_user_password);
		if (db_password.getText().toString().compareTo("")==0) { //no input, use defaults.
			editor.putString("password", "7urb0");
		} else {
			editor.putString("password", db_password.getText().toString());
		}
		
		return editor.commit();
	}
	
	/**
	 * this function really should be moved to services, to reduce repetition.
	 * because I use it for every input-page.
	 * @param emptyFields
	 */
	private void updateView(ArrayList<String> emptyFields) {
		int i;
		for(i=1; i<emptyFields.size(); i+=1){
			TextView label = (TextView) this.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private void showPage2(){
		Intent i = new Intent(this, SetupActivity.class);
		i.putExtra(Services.Statics.KEY, "simple2");
		this.startActivity(i);
		this.finish();
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_simple_setup):{
			//simple setup.
			Intent i = new Intent(this, SetupActivity.class);
			i.putExtra(Services.Statics.KEY, "simple");
			this.startActivity(i);
			this.finish();
			break;
		}
		case (R.id.button_advanced_setup):{
			//advanced setup.
			Intent i = new Intent(this, SetupActivity.class);
			i.putExtra(Services.Statics.KEY, "advanced");
			this.startActivity(i);
			this.finish();
			break;
		}
		case (R.id.button_advanced_cancel):
		case (R.id.button_simple_cancel):{
			this.finish();
			break;
		}
		case (R.id.button_simple_continue):{
			//do validation.
			ArrayList<String> validation = validateSimple();
			boolean is_valid = Boolean.valueOf(validation.get(0));
			if (!is_valid) {
				updateView(validation);
				break;
			}
			addUser();
			this.finish();
			break;
		}
		case (R.id.button_simple_accept):{
			ArrayList<String> validation = validateSimpleTwo();
			boolean is_valid = Boolean.valueOf(validation.get(0));
			if (!is_valid) {
				updateView(validation);
			}
			updateUser(); 
			break;
		}
		
		case (R.id.button_advanced_accept):{
			//check inputs, and do validation.
			ArrayList<String> validation = validateAdvanced();
			boolean is_valid = Boolean.valueOf(validation.get(0));
			if (!is_valid) {
				updateView(validation);
				break;
			}
			getAdvancedInput();
			//go to mainActivity?
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			break;
		}
		}
	}
	
	private class JSONSync extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private int theCall;
		
		public JSONSync (int call) {
			this.theCall = call;
		}
		
		protected void onPreExecute() {
			if (theCall == SetupActivity.ADDUSER) {
				 progress = ProgressDialog.show(SetupActivity.this, "Adding..", 
						 "Adding user to cental database.", true);
			 } else if (theCall == SetupActivity.UPDATEUSER) {
				 progress = ProgressDialog.show(SetupActivity.this, "Retrieving..", 
						 "Retrieving user settings from central database.", true);
			 }
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			JSONHandler json = new JSONHandler(SetupActivity.this);
			if (theCall == SetupActivity.ADDUSER) {
				return json.AddUser(params[0], params[1]);
			} else if (theCall == SetupActivity.UPDATEUSER) {
				return json.updateUser(params[0], params[1], params[2]);
			}
			return null;
		}
		

		protected void onPostExecute(Boolean result) {
			//showDialog("Downloaded " + result + " bytes");
			progress.dismiss();
			if (theCall == SetupActivity.ADDUSER) {
				showPage2();
			} else {
				Intent i = new Intent(SetupActivity.this, MainActivity.class);
				startActivity(i);
				SetupActivity.this.finish();
			}
	    }
	 }
	
}
