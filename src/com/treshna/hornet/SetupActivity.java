package com.treshna.hornet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
/**
 * Here there 'be magic.
 */
public class SetupActivity extends FragmentActivity {
	public static final int ADDUSER=11;
	public static final int UPDATEUSER=12;
	
	private FragmentManager fm;
	/** 
	 * 
	 * We need a way to check if the app is being run for the first time.							-DONE
	 * If so, we need to offer either advanced or simple setup.										-DONE
	 * 	Advanced being something like what's in the settings at the moment.							-DONE
	 * 
	 * 	Simple being 'enter email address', setup username & password.								-DONE
	 * 		then we magically generate a Database on one of the servers.							-TODO
	 * 		Said magical database will need a unique name based on what the company? is called.		-DONE
	 * 
	 * 		this all needs to go through a webpage, to ensure data security.						-DONE
	 * 
	 * If it's not the first run, we need to prompt for a username & password. OR retrieve a saved	-DONE
	 * username & password from the app settings. 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_activity);
		fm = this.getSupportFragmentManager();
		
		Intent caller = this.getIntent();
		String theview = caller.getStringExtra(Services.Statics.KEY);
		Fragment f= null;
		if (theview == null) {
			f = new SetupMainFragment();
		} else if (theview.compareTo("simple")==0) {
			f = new SetupSimpleStartFragment();
		} else if (theview.compareTo("simple2")==0) {
			f = new SetupSimpleEndFragment();
		} else if (theview.compareTo("advanced")==0) {
			f = new SetupAdvancedFragment();
		}
		changeFragment(f);
	}
	public void changeFragment(Fragment f){
		
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.empty_layout, f);
		ft.commit();
	}
	
	public void backFragment(){
		//fm.popBackStack();
		this.finish();
	}
	
	public void close() {
		this.finish();
	}
}
