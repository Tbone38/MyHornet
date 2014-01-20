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
import android.widget.EditText;
import android.widget.TextView;



public class SetupSimpleEndFragment extends Fragment implements OnClickListener {
	
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
		inputs.add(username_input.getText().toString());						//position 0
		
		EditText location_input = (EditText) view.findViewById(R.id.setup_country);
		if (location_input.getText().toString().compareTo("")==0) {
			inputs.add("NZ"); //default NZ;										//position 1
		} else {
			inputs.add(location_input.getText().toString());
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
		async.execute(email, inputs.get(0), inputs.get(1));
		
		return false;
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		int i;
		for(i=1; i<emptyFields.size(); i+=1){
			TextView label = (TextView) view.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	//TODO: create a callback that changes the fragment.
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
			 progress = ProgressDialog.show(getActivity(), "Retrieving..", 
					 "Retrieving user settings from central database.", true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			return json.updateUser(params[0], params[1], params[2]);
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

}