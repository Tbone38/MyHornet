package com.treshna.hornet;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.BookingPage.TagFoundListener;


public class KeyPerformanceIndexFragment extends Fragment implements OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	
	private View view;
	LayoutInflater mInflater;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.key_performace_index_fragment, container, false);
		
		TextView get_kpi = (TextView) view.findViewById(R.id.button_get_kpi);
		get_kpi.setOnClickListener(this);
		
		updateList();
		
		return view;
	}
	
	private void updateList() {
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case (R.id.button_get_kpi):{
			
			break;
		}
		}
	}
}