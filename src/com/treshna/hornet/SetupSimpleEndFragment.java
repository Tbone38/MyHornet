package com.treshna.hornet;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;



public class SetupSimpleEndFragment extends Fragment implements OnClickListener, OnCheckedChangeListener {
	
	private View view;
	LayoutInflater mInflater;
		
	//private static final String TAG = "Setup";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.setup_page2_simple, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupSimpleTwo();
		return view;
	}

	private View setupSimpleTwo(){
		TextView button_accept = (TextView) view.findViewById(R.id.button_simple_accept);
		button_accept.setOnClickListener(this);
		
		TextView button_cancel = (TextView) view.findViewById(R.id.button_simple_cancel);
		button_cancel.setOnClickListener(this);
		
		CheckBox contactable = (CheckBox) view.findViewById(R.id.setup_contactable);
		contactable.setOnCheckedChangeListener(this);
		
		return view;
	}
	
	private ArrayList<String> validateSimpleTwo() {
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		EditText username_input = (EditText) view.findViewById(R.id.setup_username);
		if (username_input.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_simple_username));
		} else {
			TextView label = (TextView) view.findViewById(R.id.setup_simple_username);
			label.setTextColor(Color.BLACK);
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}
	
	private ArrayList<String> getSimpleTwoInput(){
		ArrayList<String> inputs = new ArrayList<String>();
		
		EditText username_input = (EditText) view.findViewById(R.id.setup_username);
		inputs.add(username_input.getText().toString().replace(" ", "%20"));	//position 0
		
		EditText location_input = (EditText) view.findViewById(R.id.setup_country);
		if (location_input.getText().toString().compareTo("")==0) {
			inputs.add("NZ"); //default NZ;										//position 1
		} else {
			inputs.add(location_input.getText().toString().replace(" ", "%20"));
		}
		
		CheckBox contactable = (CheckBox) view.findViewById(R.id.setup_contactable);
		inputs.add(String.valueOf(contactable.isChecked()));					//position 2
		if (contactable.isChecked()) {
			EditText contact_name, contact_number;
			contact_name = (EditText) view.findViewById(R.id.setup_contact_name);
			if (contact_name.getText().toString().compareTo("")==0) {
				inputs.add(null);												//position 3
			} else {
				inputs.add(contact_name.getText().toString().replace(" ", "%20"));
			}
			
			contact_number = (EditText) view.findViewById(R.id.setup_contact_number);
			if (contact_number.getText().toString().compareTo("")==0) {
				inputs.add(null);												//position 4
			} else {
				inputs.add(contact_number.getText().toString().replace(" ", "%20"));
			}
		} else {
			inputs.add(null);													//position 3
			inputs.add(null);													//position 4
		}
		
		return inputs;
	}
	
	private String getEmail(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		return preferences.getString("email", null);
	}
	
	private boolean updateUser() {
		ArrayList<String> inputs = getSimpleTwoInput();
		
		String email = this.getEmail();
		if (email == null) {
			return false;
		}
		JSONSync async = new JSONSync(SetupActivity.UPDATEUSER);
		async.execute(email, inputs.get(0), inputs.get(1), inputs.get(2), inputs.get(3), inputs.get(4));
		
		return false;
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		int i;
		for(i=1; i<emptyFields.size(); i+=1){
			TextView label = (TextView) view.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_simple_accept):{
			ArrayList<String> validation = validateSimpleTwo();
			boolean is_valid = Boolean.valueOf(validation.get(0));
			if (!is_valid) {
				updateView(validation);
			}
			updateUser(); 
			break;
		}case (R.id.button_simple_cancel):{
			SetupActivity s = (SetupActivity) getActivity();
			s.backFragment();
			break;
		}

		}
	}
	
	
	private class JSONSync extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private JSONHandler json;
		
		public JSONSync (int call) {
			json = new JSONHandler(getActivity());
		}
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(getActivity(), "Generating..", 
					 "Generating your GymMaster Database.", true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			boolean can_contact = Boolean.parseBoolean(params[3]);
			String contact_name, contact_number;
			contact_name = params[4];
			contact_number = params[5];
			
			return json.updateUser(params[0], params[1], params[2], can_contact, contact_name, contact_number);
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				Intent i = new Intent(getActivity(), MainActivity.class);
				startActivity(i);
				getActivity().finish();
			} else {
				//we SHOULD do different stuff depending on the error code.
				switch(json.getErrorCode()){
					default:
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("Error Occured");
						builder.setMessage(json.getError()+"\n\nError Code:"+json.getErrorCode());
						builder.setPositiveButton("OK", null);
						builder.show();
				}
			}
	    }
	 }


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		TextView label_contact_name, label_contact_number;
		EditText contact_name, contact_number;
		
		label_contact_name = (TextView) view.findViewById(R.id.setup_contact_name_label);
		contact_name = (EditText) view.findViewById(R.id.setup_contact_name);
		label_contact_number = (TextView) view.findViewById(R.id.setup_contact_number_label);
		contact_number = (EditText) view.findViewById(R.id.setup_contact_number);
		
		if (isChecked) {
			label_contact_name.setVisibility(View.VISIBLE);
			contact_name.setVisibility(View.VISIBLE);
			label_contact_number.setVisibility(View.VISIBLE);
			contact_number.setVisibility(View.VISIBLE);
		} else {
			label_contact_name.setVisibility(View.GONE);
			contact_name.setVisibility(View.GONE);
			label_contact_number.setVisibility(View.GONE);
			contact_number.setVisibility(View.GONE);
		}
	}

}