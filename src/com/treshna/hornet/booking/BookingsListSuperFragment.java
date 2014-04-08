package com.treshna.hornet.booking;

import java.util.Date;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.services.Services;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
 * 
 */
public class BookingsListSuperFragment extends Fragment {
   
	private static final String TAG = "LastVisitorsFragment";
	private View view;
	private static Fragment cFragment;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.empty_activity, container, false);
		setFragment();
        return view;
    }
	
	public void onBackPressed(){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        fragmentManager.popBackStackImmediate(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-2).getId(),
        		FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (this.getCurrentFragment().getTag() == null) {
        	setFragment();
        }
		/*FUNCTIONAL!!!*/
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//((MainActivity) getActivity()).setSelectedTab(2);
		((MainActivity) getActivity()).changeFragment(null, "bookings");
	}
	
	
	public Fragment getCurrentFragment(){
		return cFragment;
	}
	
	public void setCurrentFragment(Fragment f) {
		cFragment = f;
	}

	private void setFragment(){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        int screenSize = getResources().getConfiguration().screenLayout &
    	        Configuration.SCREENLAYOUT_SIZE_MASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
        		screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
         
    		Fragment f = new BookingsOverviewFragment();
    		ft.replace(R.id.empty_layout, f, "OverviewFragment");
    		ft.addToBackStack(null);
    		cFragment = f;
        } else {
        	//Fragment f = new BookingsResourceFragment();
        	Fragment f = new BookingsSlideFragment();
        	Bundle bdl = new Bundle(2);
        	bdl.putLong("date", new Date().getTime()); //TODO: change this to putString("booking_dates", yyyyMMdd);
        	bdl.putBoolean("hasOverview", false);
        	f.setArguments(bdl);
        	ft.replace(R.id.empty_layout, f, "ResourceFragment");
        	ft.addToBackStack(null);
        	cFragment = f;
        }
        
        ft.commit();
	}
	
	public void changeFragment(Fragment f, String tag){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        cFragment = f;
        ft.replace(R.id.empty_layout, f, tag);
        ft.addToBackStack(null);
        ft.commit();
	}
	
}

