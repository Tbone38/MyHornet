package com.treshna.hornet;

import java.util.Date;

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
        Log.v(TAG, "Creating Last Visitors");
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
        fragmentManager.popBackStackImmediate();
        cFragment = fragmentManager.findFragmentByTag("OverviewFragment");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setSelectedTab(2);
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

