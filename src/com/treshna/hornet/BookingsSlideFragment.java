package com.treshna.hornet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
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

public class BookingsSlideFragment extends Fragment {
	private static int NUM_PAGES = 5; //?
	
	@SuppressLint("UseSparseArrays")
	private Map<Integer, Fragment> fragmentlist = new HashMap<Integer, Fragment>();
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private String selectedDate;
    private boolean hasOverview;
    
    private View view;
    private static final String TAG = "BookingsSlideFragment";
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        
        selectedDate = Services.getAppSettings(getActivity(), "bookings_date");
    	if (Integer.parseInt(selectedDate) == -1) {
    		selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
    	}
    	hasOverview = this.getArguments().getBoolean("hasOverview");
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
	    mPager.setCurrentItem(2);
		  
		return view;

	  }
    
    /**
     * Known Issues: when the date changes on a fragment, all the dates change.
     * However this code assume the middle fragment is the point of origin, if it's not the point of origin
     * (for the date change) then some of the other pages will get the same date. (switch-case: 2)
     * 
     * To fix I could reset/redraw all the fragments, moving the user back to fragment 2?
     * 
     * @param position
     * @return
     */
    private String getDate(int position){
    	selectedDate = Services.getAppSettings(getActivity(), "bookings_date");
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
    	Calendar cal = Calendar.getInstance();
    	Date date;
    	try {
    		date = format.parse(selectedDate);
    	} catch (ParseException e) {
    		date = new Date();
    	}
    	cal.setTime(date);
    	switch (position){
    		case 0:{
    			cal.add(Calendar.DATE, -2);
    			String formatteddate = format.format(cal.getTime());
    			return formatteddate;
    		}
    		case 1:{
    			cal.add(Calendar.DATE, -1);
    			String formatteddate = format.format(cal.getTime());
    			return formatteddate;
    		}
    		case 2:{
    			return selectedDate;
    		}
    		case 3:{
    			cal.add(Calendar.DATE, +1);
    			String formatteddate = format.format(cal.getTime());
    			return formatteddate;
    		}
    		case 4:{
    			cal.add(Calendar.DATE, +2);
    			String formatteddate = format.format(cal.getTime());
    			return formatteddate;
    		}
    	}
    	return selectedDate;
    }
    
    class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
    	
        public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
        	
        	//create fragment
        	BookingsResourceFragment page = new BookingsResourceFragment();
            Bundle bdl = new Bundle(2);
            bdl.putBoolean("hasOverview", hasOverview);
            String date = BookingsSlideFragment.this.getDate(position);
            Log.w(TAG, "getItem Date:"+date);
            bdl.putString("bookings_date", date);
            page.setArguments(bdl);
            
            //this is unused
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

}
