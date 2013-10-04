package com.treshna.hornet;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * TODO: 	- populate spinner #2.			-Done
 * 			- date widget					-
 * 			- handle switch					-
 * 			- populate/handle spinner #1	-
 * 
 * @author callum
 *
 */

public class MembershipHold extends FragmentActivity implements OnClickListener {
	
	private String datevalue;
	DatePickerFragment datePicker;
	

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
            MembershipHold.this.receivedBroadcast(intent);
        }	
    };
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.membership_hold);
		
		datePicker = new DatePickerFragment();
		datevalue = null;
		
		setupView();
	}
	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setupView(){
		TextView accept, cancel;
		
		setupDate();
		
		accept = (TextView) this.findViewById(R.id.buttonaccept);
		accept.setClickable(true);
		accept.setOnClickListener(this);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			accept.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			accept.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
		
		cancel = (TextView) this.findViewById(R.id.buttoncancel);
		cancel.setClickable(true);
		cancel.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			cancel.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
	}
	
	private void setupDate(){
		TextView startdate = (TextView) this.findViewById(R.id.startdate);
		
		if (datevalue != null) {
			startdate.setText(datevalue);
		}
		
		startdate.setClickable(true);
		startdate.setOnClickListener(this);
	}
	
	
	private void receivedBroadcast(Intent intent) {	
		datevalue = datePicker.getReturnValue();
		setupDate();
	}
	
	
	@Override 
	public void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter();
	    iff.addAction(ClassCreate.CLASSBROADCAST);
	    this.registerReceiver(this.mBroadcastReceiver,iff);
	}
	
	
	@Override
	public void onPause(){
		super.onPause();
		this.unregisterReceiver(this.mBroadcastReceiver);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.membership_hold, menu);
		return true;
	}

	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case (R.id.startdate):{
			Bundle bdl;
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, (String) view.getTag());
			//datePicker = new DatePickerFragment();
			datePicker.setArguments(bdl);
		    datePicker.show(this.getSupportFragmentManager(), "datePicker");
			
			break;
		}
		case (R.id.buttoncancel):{
			this.finish();
			break;
		}
		case (R.id.buttonaccept):{
			//do input-checking.
			
		}
		}	
	}

}
 