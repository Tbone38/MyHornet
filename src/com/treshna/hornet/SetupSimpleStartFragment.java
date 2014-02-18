package com.treshna.hornet;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;



public class SetupSimpleStartFragment extends Fragment implements OnClickListener {
	
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
	
		view = inflater.inflate(R.layout.setup_page_simple, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupSimple();
		return view;
	}
	
	private View setupSimple(){
		TextView button_accept = (TextView) view.findViewById(R.id.button_simple_continue);
		button_accept.setOnClickListener(this);
		
		TextView button_cancel = (TextView) view.findViewById(R.id.button_simple_cancel);
		button_cancel.setOnClickListener(this);
		
		TextView hint = (TextView) view.findViewById(R.id.setup_simple_hint);
		hint.setText(Html.fromHtml(this.getString(R.string.setup_simple_hint)));
		
		return view;
	}
	
	private ArrayList<String> validateSimple() {
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		EditText email_address, company_name;
		
		email_address = (EditText) view.findViewById(R.id.setup_email_address);
		if (email_address.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_simple_email_text));
		} else {
			TextView label = (TextView) view.findViewById(R.id.setup_simple_email_text);
			label.setTextColor(Color.BLACK);
		}
		
		company_name = (EditText) view.findViewById(R.id.setup_company_name);
		if (company_name.getText().toString().compareTo("")==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.setup_simple_company_name_text));
		} else {
			TextView label = (TextView) view.findViewById(R.id.setup_simple_company_name_text);
			label.setTextColor(Color.BLACK);
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}
	
	private ArrayList<String> getSimpleInput() {
		ArrayList<String> inputs = new ArrayList<String>();
		
		EditText email_address = (EditText) view.findViewById(R.id.setup_email_address);
		inputs.add(email_address.getText().toString().replace(" ", "")); 					//0
		
		EditText organisation = (EditText) view.findViewById(R.id.setup_company_name);
		inputs.add(organisation.getText().toString().replace(" ", "%20"));						//1
		
		return inputs;
	}
	
	private boolean storeEmail(String email) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString("email", email);
		
		return editor.commit();
	}
	
	private boolean addUser() {
		ArrayList<String> inputs = getSimpleInput();
		this.storeEmail(inputs.get(0));
		JSONSync async = new JSONSync(SetupActivity.ADDUSER);
		async.execute(inputs.get(0), inputs.get(1));
		
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
		case (R.id.button_simple_cancel):{
			SetupActivity s = (SetupActivity) getActivity();
			s.backFragment();
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
			 progress = ProgressDialog.show(getActivity(), "Adding..", 
					 "Adding user to cental database.", true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			return json.AddUser(params[0], params[1]);
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				Fragment f = new SetupSimpleEndFragment();
				SetupActivity s = (SetupActivity) getActivity();
				s.changeFragment(f);
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