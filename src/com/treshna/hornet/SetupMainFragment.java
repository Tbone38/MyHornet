package com.treshna.hornet;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;



public class SetupMainFragment extends Fragment implements OnClickListener {
	
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
	
		view = inflater.inflate(R.layout.setup_page_one, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupPageOne();
		return view;
	}

	private View setupPageOne() {
		TextView simple = (TextView) view.findViewById(R.id.button_simple_setup);
		simple.setOnClickListener(this);
		simple.setVisibility(View.INVISIBLE);
		
		View simpleline = (View) view.findViewById(R.id.button_simple_underline);
		simpleline.setVisibility(View.INVISIBLE);
		
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