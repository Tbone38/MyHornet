package com.treshna.hornet.navigation;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.booking.BookingAddFragment;
import com.treshna.hornet.booking.ClassCreateFragment;
import com.treshna.hornet.member.MemberAddFragment;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.report.KeyPerformanceIndexFragment;
import com.treshna.hornet.report.ReportListingActivity;
import com.treshna.hornet.roll.RollListFragment;
import com.treshna.hornet.services.Services;

public class SlideMenuClickListener implements OnItemClickListener, OnClickListener {

	private FragmentManager fm;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawerView;
    private Activity activity;
    private int currentselection = 0;
	
    
	public SlideMenuClickListener(FragmentManager fragmentManager, DrawerLayout drawerLayout, ListView drawerList, Activity ma, LinearLayout drawer) {
		this.fm = fragmentManager;
		this.mDrawerLayout = drawerLayout;
		this.mDrawerList = drawerList;
		this.mDrawerView = drawer;
		this.activity = ma;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		displayView(position, view);
	}
	
	private void displayView(int position, View view) {
        // update the main content by replacing fragments
		if (((MainActivity)activity).getSelectedNavItem() == position) {
			return; //does this break anything?
		}
        Fragment fragment = null;
        String tag = null;
        switch (position) {
        case 1:
        	//Member Find
        	((MainActivity)activity).genTabs();
        	tag = "findmember";
            break;
        case 2: //last visitors;
    		((MainActivity)activity).genTabs();
        	tag = "lastvisitors";
            break;
        case 3: //bookings
        	tag = "bookings";
        	((MainActivity)activity).genTabs();
            break;
        case 5:
            fragment = new MemberAddFragment();
        	tag = "memberAdd";
            break;
        case 6:
        	tag = "bookingAdd";
        	fragment = new BookingAddFragment();
        	break;
        case 8:
        	tag = "kpi";
        	fragment = new KeyPerformanceIndexFragment();
        	break;
        case 9:
        	tag = "reports";
        	fragment = new ReportListingActivity(); 
        	break;
        case 11:
        	tag = "rolllist";
        	fragment = new RollListFragment();
        	break;
        case 13:
        	fragment = new ClassCreateFragment();
        	tag = "classCreate";
        	break;
        default: 
            break;
        }
        
        currentselection = position;
    	((MainActivity)activity).changeFragment(fragment, tag);
    	((MainActivity)activity).setSelectedNavItem(position);

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        //mDrawerLayout.closeDrawer(mDrawerList);
        mDrawerLayout.closeDrawer(mDrawerView);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_lastsync):{
			Intent updateInt = new Intent(activity, HornetDBService.class);
            updateInt.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
            activity.startService(updateInt);
		}
		}
	}
}