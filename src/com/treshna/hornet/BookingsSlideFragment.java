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
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class BookingsSlideFragment extends Fragment  implements BookingsListFragment.OnCalChangeListener {
	private static int NUM_PAGES = 7; //?
	
	private Map<Integer, BookingsListFragment> fragmentlist = new HashMap<Integer, BookingsListFragment>();
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private Date date;
    
    private Cursor cur;
    private ContentResolver contentResolver;
    private View view;
    private static final String TAG = "BookingsSlideFragment";
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	//System.out.println("*INTENT RECIEVED*");
        	Log.v(TAG, "Intent Recieved");
        	BookingsSlideFragment.this.receivedBroadcast(intent);
        }
    };
    
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		  super.onCreateView(inflater, container, savedInstanceState);
		  Log.i(TAG, "Creating View");
	
		  view = inflater.inflate(R.layout.swipe_layout, container, false);
		// Instantiate a ViewPager and a PagerAdapter.
	        mPager = (ViewPager) view.findViewById(R.id.pager);
	        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
	        mPager.setAdapter(mPagerAdapter);
	        date = new Date();
	        mPager.setCurrentItem(2);
	        
	        contentResolver = getActivity().getContentResolver();
	        String rid = Services.getAppSettings(getActivity(), "resourcelist");
	        
	       	RelativeLayout resourceline = (RelativeLayout) view.findViewById(R.id.resourceline);
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
			ArrayAdapter<String> resourceAdapter = new ArrayAdapter<String>(getActivity(),
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
						/*cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.NAME+" = ?", 
								new String[] {resourcename}, null);
						cur.moveToFirst();*/ 
						//TODO: check this aligns properly (starts and ends)
						cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
						cur.moveToPosition(pos);
						resourceid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID));
						
						updateSelection(resourceid);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						//leave alone (doesn't matter?)
					}});
			cur.close();
		  
		  return view;

	  }
    
    private void updateSelection(int value){
    	if (value > 0 && value != Integer.valueOf(Services.getAppSettings(getActivity(), "resourcelist"))) {
	    	Services.setPreference(getActivity(), "resourcelist", String.valueOf(value));
	    	
	    	Intent updateInt = new Intent(getActivity(), HornetDBService.class);
			updateInt.putExtra(Services.Statics.KEY, Services.Statics.RESOURCESELECTED);
			updateInt.putExtra("newtime", String.valueOf(value));
			getActivity().startService(updateInt);
		 	
		 	Intent bookings = new Intent(getActivity(), HornetDBService.class);
			bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
			getActivity().startService(bookings);
		 	
		 	//just change the cursor.
		 	BookingsListFragment page = fragmentlist.get(mPager.getCurrentItem());
		 	if (page != null) {
		 		page.loadermanager.restartLoader(0, null, page);
		 	} else {
		 		//page is null, what should I do instead?
		 		Intent i = new Intent(getActivity(), BookingsSlideFragment.class);
		       	startActivity(i);
		       	getActivity().finish();
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
    			Intent i = new Intent(getActivity(), BookingsSlideFragment.class);
		       	startActivity(i);
		       	getActivity().finish();
    		}
    	}
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    	Services.setContext(getActivity());
    	IntentFilter iff = new IntentFilter();
	    iff.addAction("com.treshna.hornet.serviceBroadcast");
	    getActivity().registerReceiver(this.mBroadcastReceiver,iff);
	    if (Services.getProgress() != null ) {
    		Services.getProgress().show();
    	}
    }
    
    @Override
	public void onPause() {
		//super.onPause();
    	if (Services.getProgress() != null && Services.getProgress().isShowing()) {
    		Services.getProgress().dismiss();
    		//Services.setProgress(null);
    	}
    	getActivity().unregisterReceiver(this.mBroadcastReceiver);
		super.onPause();
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
		mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
		//get bookings for this date ?
	    mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(1);
		
	}

}
