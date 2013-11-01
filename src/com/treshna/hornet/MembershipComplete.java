package com.treshna.hornet;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MembershipComplete extends Fragment implements OnClickListener {
	private static final String TAG ="MembershipComplete";
	Context ctx;
	ContentResolver contentResolver;
	Cursor cur;
	View page;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 contentResolver = getActivity().getContentResolver();
		 
		 page = inflater.inflate(R.layout.membership_complete, container, false);
		 page = setupView();
		 
		 return page;
	}


	private View setupView() {

				
		return page;
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.acceptbutton):{
			break;
		}
		case (R.id.cancelbutton):{
			getActivity().finish();
			break;
		}
		}
	}
	
}