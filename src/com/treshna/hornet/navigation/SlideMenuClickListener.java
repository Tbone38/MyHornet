package com.treshna.hornet.navigation;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.booking.BookingAddFragment;
import com.treshna.hornet.booking.ClassCreateFragment;
import com.treshna.hornet.member.MemberAddFragment;
import com.treshna.hornet.report.KeyPerformanceIndexFragment;
import com.treshna.hornet.report.ReportListingActivity;
import com.treshna.hornet.roll.RollListFragment;

public class SlideMenuClickListener implements OnItemClickListener {

	private FragmentManager fm;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Activity activity;
    private int currentselection = 0;
	
    
	public SlideMenuClickListener(FragmentManager fragmentManager, DrawerLayout drawerLayout, ListView drawerList, Activity ma) {
		this.fm = fragmentManager;
		this.mDrawerLayout = drawerLayout;
		this.mDrawerList = drawerList;
		this.activity = ma;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		displayView(position);
	}
	
	private void displayView(int position) {
        // update the main content by replacing fragments
		/*if (currentselection == position) {
			return;
		}*/
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
        	//TODO: make memberAdd a fragment;
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

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}