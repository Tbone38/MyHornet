package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class BookingsSlidePager extends FragmentActivity  implements BookingsListFragment.OnCalChangeListener {
	private static int NUM_PAGES = 7; //?
	
	private Map<Integer, BookingsListFragment> fragmentlist = new HashMap<Integer, BookingsListFragment>();
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private Date date;
    
    private Cursor cur;
    private ContentResolver contentResolver;
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
           BookingsSlidePager.this.receivedBroadcast(intent);
        }
    };
    
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(this);
              
        setContentView(R.layout.swipe_layout);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        date = new Date();
        mPager.setCurrentItem(2);
        
        contentResolver = getContentResolver();
        String rid = Services.getAppSettings(this, "resourcelist");
        
       	RelativeLayout resourceline = (RelativeLayout) findViewById(R.id.resourceline);
        Spinner name = (Spinner) resourceline.findViewById(R.id.resourcename);
        cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
		
        int pos= 0, i = 0;
        
		ArrayList<String> resourcelist = new ArrayList<String>();
		while (cur.moveToNext()) {
			resourcelist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)));
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)).compareTo(rid) ==0){
				pos = i;
			}
			i +=1;
		}
		ArrayAdapter<String> resourceAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, resourcelist);
			resourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			name.setAdapter(resourceAdapter);
			name.setSelection(pos);
			name.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					String resourcename;
					int resourceid;
					resourcename = String.valueOf(parent.getItemAtPosition(pos));
					
					if (cur != null){
						cur.close();
					}
					cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.NAME+" = ?", 
							new String[] {resourcename}, null);
					cur.moveToFirst();
					resourceid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID));
					
					updateSelection(resourceid);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//leave alone (doesn't matter?)
				}});
		cur.close();
    }
    
    private void updateSelection(int value){
    	if (value > 0 && value != Integer.valueOf(Services.getAppSettings(this, "resourcelist"))) {
	    	Services.setPreference(this, "resourcelist", String.valueOf(value));
	    	
	    	Intent updateInt = new Intent(this, HornetDBService.class);
			updateInt.putExtra(Services.Statics.KEY, Services.Statics.RESOURCESELECTED);
			updateInt.putExtra("newtime", String.valueOf(value));
		 	this.startService(updateInt);
		 	
		 	Intent bookings = new Intent(this, HornetDBService.class);
			bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
		 	this.startService(bookings);
		 	
		 	//just change the cursor.
		 	BookingsListFragment page = fragmentlist.get(mPager.getCurrentItem());
		 	if (page != null) {
		 		page.loadermanager.restartLoader(0, null, page);
		 	} else {
		 		//page is null, what should I do instead?
		 		Intent i = new Intent(this, BookingsSlidePager.class);
		       	startActivity(i);
		       	this.finish();
		 	}
		 
    	}
    }
    
    protected void receivedBroadcast(Intent intent) {
    	if (intent.getBooleanExtra(Services.Statics.IS_SUCCESSFUL, false) == true) {
    		/* The above if ensures that the fragment has the correct bundle details?
    		 */
    		if (Services.getProgress() != null && Services.getProgress().isShowing()) {
        		Services.getProgress().dismiss();
        		Services.setProgress(null);
        	}
    		BookingsListFragment page = fragmentlist.get(mPager.getCurrentItem());
    		if (page != null) {
    			page.loadermanager.restartLoader(0, null, page);
    		} else {
    			// orientation changed/or page otherwise couln't be found,
    			// instead just restart the activity intent.
    			Intent i = new Intent(this, BookingsSlidePager.class);
		       	startActivity(i);
		       	this.finish();
    		}
    	}
	}
    
    @Override
    public void onBackPressed() {
        /*if (mPager.getCurrentItem() == 0) {
            //super.onBackPressed();
        	finish();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }*/
    	finish();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Services.setContext(this);
    	IntentFilter iff = new IntentFilter();
	    iff.addAction("com.treshna.hornet.serviceBroadcast");
	    this.registerReceiver(this.mBroadcastReceiver,iff);
	    if (Services.getProgress() != null ) {
    		Services.getProgress().show();
    	}
    }
    
    @Override
	protected void onPause() {
		//super.onPause();
    	if (Services.getProgress() != null && Services.getProgress().isShowing()) {
    		Services.getProgress().dismiss();
    		//Services.setProgress(null);
    	}
		this.unregisterReceiver(this.mBroadcastReceiver);
		super.onPause();
	}
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		    // Handle item selection
		    switch (item.getItemId()) {
		    case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		    case (R.id.action_settings):
		    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
		    	startActivity(settingsIntent);
		    	return true;
		    case (R.id.action_scan):
		    	Intent scanIntent = new Intent(this, HornetRFIDReader.class);
		    	startActivity(scanIntent);
		    	return true;
		    case (R.id.action_update): { //this updates last visitors.
		    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
			 		Services.setPreference(this, "sync_frequency", "5");
			 	}
			 	PollingHandler polling = Services.getPollingHandler();
		    	polling.startService();
		    	return true;
		    }
		    case (R.id.action_halt): {
		    	PollingHandler polling = Services.getPollingHandler();
		    	polling.stopPolling(false);
		    	Services.setPreference(this, "sync_frequency", "-1");
		    	return true;
		    }
		    case (R.id.action_bookings):{
		    	Intent bookings = new Intent(this, HornetDBService.class);
				bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
			 	this.startService(bookings);
		    	
			 	Intent intent = new Intent(this, BookingsSlidePager.class);
		       	startActivity(intent);
		       	return true;
		    }
		    case (R.id.action_addMember):{
		    	Intent intent = new Intent(this, AddMember.class);
		    	startActivity(intent);
		    	return true;
		    }
		    case (R.id.action_findMember):{
		    	Intent i = new Intent(this, MemberFind.class);
		    	startActivity(i);
		    	return true;
		    }
		    case (R.id.action_visitors):{
		    	Intent intent = new Intent(this, DisplayResultsActivity.class);
				intent.putExtra(Services.Statics.KEY,DisplayResultsActivity.LASTVISITORS); 
				startActivity(intent);
		    	return true;
		    }
		    default:
		    	return super.onOptionsItemSelected(item);
		}       
	 }

    class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
    	
        public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public BookingsListFragment getItem(int position) {
        	
        	BookingsListFragment page = new BookingsListFragment();
        	Bundle bdl = new Bundle(2);
            bdl.putInt(Services.Statics.KEY, position);
            bdl.putLong("date", date.getTime()); //time since epoch
            page.setArguments(bdl);
            
            if (fragmentlist.containsKey(position)) {
            	fragmentlist.remove(position);
            }
            fragmentlist.put(position, page);
      
        	return page;
        }
        
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

	@Override
	public void onDateChange(Date newdate) {
		//the widget has selected a new date, rebuild the fragments using that date.
		date = newdate;
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		//get bookings for this date ?
	    mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(1);
		
	}

}
