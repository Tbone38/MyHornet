package com.treshna.hornet.setup;


import java.util.ArrayList;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.Services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class SetupAdvancedFragment extends Fragment implements OnClickListener {
	
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
	
		view = inflater.inflate(R.layout.setup_page_advanced, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupAdvanced();
		return view;
	}

	private View setupAdvanced(){
		TextView button_accept = (TextView) view.findViewById(R.id.button_advanced_accept);
		button_accept.setOnClickListener(this);
		
		TextView button_cancel = (TextView) view.findViewById(R.id.button_advanced_cancel);
		button_cancel.setOnClickListener(this);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		
		EditText address = (EditText) view.findViewById(R.id.setup_database_address);
		if (preferences.getString("address", "-1").compareTo("-1")!=0) {
			address.setText(preferences.getString("address", "-1"));
		}
		
		EditText dbname = (EditText) view.findViewById(R.id.setup_database_name);
		if (preferences.getString("database", "-1").compareTo("-1") !=0) {
			dbname.setText(preferences.getString("database", "-1"));
		}
		
		EditText port = (EditText) view.findViewById(R.id.setup_database_port);
		if (preferences.getString("port", "-1").compareTo("-1")!=0) {
			port.setText(preferences.getString("port", "-1"));
		}
		
		EditText username = (EditText) view.findViewById(R.id.setup_user_name);
		if (preferences.getString("username", "-1").compareTo("-1")!=0) {
			username.setText(preferences.getString("username", "-1"));
		}
		
		EditText password = (EditText) view.findViewById(R.id.setup_user_password);
		if (preferences.getString("password", "-1").compareTo("-1") != 0){
			password.setText(preferences.getString("password", "-1"));
		}
		
		return view;
	}
	
	private ArrayList<String> validateAdvanced() {
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		EditText db_address = (EditText) view.findViewById(R.id.setup_database_address);
		if (db_address.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_advanced_server_address));
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}
	
	private boolean getAdvancedInput(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = preferences.edit();
		
		EditText db_address, db_name, db_port, db_user, db_password;
		
		db_address = (EditText) view.findViewById(R.id.setup_database_address);
		editor.putString("address", db_address.getText().toString());
		
		db_name = (EditText) view.findViewById(R.id.setup_database_name);
		if (db_name.getText().toString().compareTo("")==0){ //no input, use defaults.
			editor.putString("database", "gymmaster");
		} else {
			editor.putString("database", db_name.getText().toString());
		}
		
		db_port = (EditText) view.findViewById(R.id.setup_database_port);
		if (db_port.getText().toString().compareTo("")==0){ //no input, use defaults.
			editor.putString("port", "5432");
		} else {
			editor.putString("port", db_port.getText().toString());
		}
		
		db_user = (EditText) view.findViewById(R.id.setup_user_name);
		if (db_user.getText().toString().compareTo("")==0){ //no input, use default.
			editor.putString("username", "gymmaster");
		} else {
			editor.putString("username", db_user.getText().toString());
		}
		
		db_password = (EditText) view.findViewById(R.id.setup_user_password);
		if (db_password.getText().toString().compareTo("")==0) { //no input, use defaults.
			editor.putString("password", "7urb0");
		} else {
			editor.putString("password", db_password.getText().toString());
		}
		
		return editor.commit();
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		int i;
		for(i=1; i<emptyFields.size(); i+=1){
			TextView label = (TextView) view.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (Services.getProgress() != null && Services.getProgress().isShowing()) {
    		Services.getProgress().dismiss();
    		//Services.setProgress(null);
    	}		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Services.setContext(getActivity());
		if (Services.getProgress() != null) {
    		Services.getProgress().show();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_advanced_cancel):{
			SetupActivity s = (SetupActivity) getActivity();
			s.backFragment();
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
			Intent i = new Intent(getActivity(), MainActivity.class);
			startActivity(i);
			getActivity().finish();
			
			Intent updateInt = new Intent(getActivity(), HornetDBService.class);
			updateInt.putExtra(Services.Statics.KEY, Services.Statics.FIRSTRUN);
		 	getActivity().startService(updateInt);
			break;
		}
		}
	}
	
}