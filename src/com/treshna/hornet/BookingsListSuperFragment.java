package com.treshna.hornet;

import java.util.ArrayList;

import com.treshna.hornet.MemberFindFragment.OnMemberSelectListener;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/*
 * 
 */
public class BookingsListSuperFragment extends Fragment {
   
	private static final String TAG = "LastVisitorsFragment";
	private View view;
	
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
	
	private void setFragment(){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        int screenSize = getResources().getConfiguration().screenLayout &
    	        Configuration.SCREENLAYOUT_SIZE_MASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
        		screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
        	//show the Overview TODO: fix this.
    		//getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    		//the above orientation works, but the main page can't handle it.
    		Fragment f = new BookingsOverviewFragment();
    		ft.replace(R.id.empty_layout, f);
        
        } else {
        	//show the normal bookings fragment.
        	//Fragment f = new BookingsSlideFragment();
        	Fragment f = new BookingsResourceFragment();
        	ft.replace(R.id.empty_layout, f);
        }
        
        ft.commit();
	}
	
	public void changeFragment(Fragment f){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        
        ft.replace(R.id.empty_layout, f);
        ft.commit();
	}
	
}

