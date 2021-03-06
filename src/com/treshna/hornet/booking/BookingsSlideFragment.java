package com.treshna.hornet.booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treshna.hornet.R;
import com.treshna.hornet.services.Services;

public class BookingsSlideFragment extends Fragment {
	private static int NUM_PAGES = 5; //?
	
	@SuppressLint("UseSparseArrays")
	private Map<Integer, BookingsResourceFragment> fragmentlist = new HashMap<Integer, BookingsResourceFragment>();
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private String selectedDate;
    private boolean hasOverview;
    private String selectedTime;
    private View view;
    private static final String TAG = "BookingsSlideFragment";
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        
        selectedDate = Services.getAppSettings(getActivity(), "bookings_date");
    	if (selectedDate.compareTo("-1") == 0) {
    		//selectedDate = Services.dateFormat(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd");
    		selectedDate = Services.DateToString(new Date());
    	}
    	hasOverview = this.getArguments().getBoolean("hasOverview");
    	
    	if (this.getArguments().containsKey("selectedTime")) {
        	selectedTime = this.getArguments().getString("selectedTime");
        }
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    	super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.fragment_swipe_layout, container, false);
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
    	SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
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
    
    public boolean hasOverView(){
		return hasOverview;
	}
    
    
	public void setDate(String date) {
    	this.selectedDate = date;
    	
    	//mPager.getAdapter().notifyDataSetChanged();
    	mPager.invalidate();
    	mPager.setCurrentItem(2);
    	
    }
	
    class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
    	FragmentManager fragManager;
        public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.fragManager = fragmentManager; 
        }

        @Override
        public Fragment getItem(int position) {
        	BookingsResourceFragment page;
        	
        	page = new BookingsResourceFragment();
            if (fragmentlist.containsKey(position)) { //unused.
            	fragmentlist.remove(position);
            }
            fragmentlist.put(position, page);

        	Bundle bdl = new Bundle(3);
            bdl.putBoolean("hasOverview", hasOverview);
            String date = BookingsSlideFragment.this.getDate(position);
            if (selectedTime != null) {
            	bdl.putString("selectedTime", selectedTime);
            }
            bdl.putString("bookings_date", date);
            page.setArguments(bdl);
            
        	return page;
        }
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
