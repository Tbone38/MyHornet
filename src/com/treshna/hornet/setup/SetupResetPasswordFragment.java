package com.treshna.hornet.setup;


import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.services.Services;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;



public class SetupResetPasswordFragment extends Fragment implements OnClickListener {
	
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
	
		view = inflater.inflate(R.layout.setup_reset_password, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupPage();
		return view;
	}

	private View setupPage() {
		TextView simple = (TextView) view.findViewById(R.id.button_simple_setup);
		simple.setOnClickListener(this);
		
		TextView advanced = (TextView) view.findViewById(R.id.button_advanced_setup);
		advanced.setOnClickListener(this);
		
		return view;
	}
	//TODO: create a callback that changes the fragment.
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_simple_setup):{
			//simple setup.
			Intent i = new Intent(getActivity(), SetupActivity.class);
			i.putExtra(Services.Statics.KEY, "simple");
			this.startActivity(i);
			break;
		}
		case (R.id.button_advanced_setup):{
			//advanced setup.
			Intent i = new Intent(getActivity(), SetupActivity.class);
			i.putExtra(Services.Statics.KEY, "advanced");
			this.startActivity(i);
			break;
		}
		}
	}
	
}