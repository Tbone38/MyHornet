package com.treshna.hornet.navigation;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.treshna.hornet.HornetApplication;
import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.booking.BookingDetailsSuperFragment;
import com.treshna.hornet.classes.ClassCreateFragment;
import com.treshna.hornet.form.FormFragment;
import com.treshna.hornet.lists.FormList;
import com.treshna.hornet.member.MemberAddFragment;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.report.KeyPerformanceIndexFragment;
import com.treshna.hornet.report.ReportListingActivity;
import com.treshna.hornet.roll.RollListFragment;
import com.treshna.hornet.services.Services;

public class SlideMenuClickListener implements OnItemClickListener, OnClickListener {

	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawerView;
    private Activity activity;
	
    
	public SlideMenuClickListener(DrawerLayout drawerLayout, ListView drawerList, Activity ma, LinearLayout drawer) {
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
        case 6: {
        	tag = "bookingAdd";
        	fragment = new BookingDetailsSuperFragment();
        	ArrayList<String> stuff = new ArrayList<String>();
        	stuff.add("-1");
        	stuff.add(null);
        	stuff.add(null);
        	Bundle bdl = new Bundle(1);
        	bdl.putStringArrayList(Services.Statics.KEY, stuff);
        	fragment.setArguments(bdl);
        	
        	break;
        }
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
        case 14: {
        	//fragment = new FormFragment();
        	fragment = new FormList();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.RESOURCE);
        	fragment.setArguments(bdl);
        	tag = "formFragment";
        	break;
        }
        case 15:{
        	fragment = new FormList();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.PROGRAMMEGROUP);
        	fragment.setArguments(bdl);
        	tag = "formFragment";
        	break;
        }
        case 16:{
        	fragment = new FormList();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.BOOKINGTYPE);
        	fragment.setArguments(bdl);
        	tag = "formFragment";
        	break;
        }
        default: 
            break;
        }
        
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
            
            SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName() + 
            		"_preferences", Context.MODE_MULTI_PROCESS);
            if (preferences.getBoolean("progress", false)) {
            	FreqSync task = new FreqSync();
            	task.execute(null, null);
            }
		}
		}
	}
	
	private class FreqSync extends AsyncTask<String, Integer, Boolean> {
		
		private long starttime;
		private long curtime;
		private static final long TENMINUTES = 600000;
		private String message = null;
		
		protected void onPreExecute() {
			 ProgressDialog progress = ProgressDialog.show(activity, "Syncing..", 
					 "Syncing your GymMaster Database.", true);
			 Services.setProgress(progress);
			 HornetApplication mApplication = ((HornetApplication) activity.getApplicationContext()).getInstance();
			 mApplication.setSyncStatus(true);
			 starttime = new Date().getTime();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			message = null;
			HornetApplication mApplication = ((HornetApplication) activity.getApplicationContext()).getInstance();
			boolean is_syncing = mApplication.getSyncStatus();
			while (is_syncing) {
				curtime = new Date().getTime();
				if ((curtime - starttime) > TENMINUTES) {
					message = "Syncing took longer than 10 minutes, progress has been hidden.";
					return false;
				}
				is_syncing = mApplication.getSyncStatus();
			}
			return mApplication.getSyncResult();
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			Services.getProgress().dismiss();
			Services.setProgress(null);
			if (success) {
				//we'll need to refresh all our cursors some how...
				/*ContentResolver contentResolver = activity.getContentResolver();
				contentResolver.notifyAll();*/
			} else {
				message = (message == null)? "Error Encountered" : message;
				Toast.makeText(activity, "Syncing took longer than 10 minutes, progress has been hidden.",Toast.LENGTH_LONG).show();
			}
	    }

	}
}