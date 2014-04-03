package com.treshna.hornet.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.treshna.hornet.BookingAddFragment;
import com.treshna.hornet.ClassCreateFragment;
import com.treshna.hornet.KeyPerformanceIndexFragment;
import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;

public class SlideMenuClickListener implements OnItemClickListener {

	private FragmentManager fm;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private MainActivity activity;
	
	public SlideMenuClickListener(FragmentManager fragmentManager, DrawerLayout drawerLayout, ListView drawerList, MainActivity ma) {
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
        Fragment fragment = null;
        String tag = null;
        switch (position) {
        case 1:
        	//Member Find
        	tag = "findmember";
            break;
        case 2: //last visitors;
        	tag = "lastvisitors";
            break;
        case 3: //bookings
        	tag = "bookings";
            break;
        case 5:
        	//TODO: make memberAdd a fragment;
            //fragment = new MembersAddFragment();
        	tag = "memberAdd";
            break;
        case 6:
        	fragment = new ClassCreateFragment();
        	tag = "classCreate";
        	break;
        case 7:
        	tag = "bookingAdd";
        	fragment = new BookingAddFragment();
        	break;
        case 9:
        	tag = "kpi";
        	fragment = new KeyPerformanceIndexFragment();
        default:
            break;
        }
        activity.changeFragment(fragment, tag);
        
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}