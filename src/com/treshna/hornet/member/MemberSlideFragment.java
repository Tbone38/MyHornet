package com.treshna.hornet.member;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R;
import com.treshna.hornet.services.Services;

public class MemberSlideFragment extends Fragment implements TagFoundListener{

	private View view;
	private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private TagFoundListener mTagListener;
    
    String memberID;
    int selectedFragment;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    	super.onCreateView(inflater, container, savedInstanceState);
	
    	memberID = this.getArguments().getString(Services.Statics.MID);
    	
		view = inflater.inflate(R.layout.swipe_layout, container, false);
		// Instantiate a ViewPager and a PagerAdapter.
	    mPager = (ViewPager) view.findViewById(R.id.pager);
	    mPagerAdapter = new MemberDetailsPagerAdapter(getChildFragmentManager());
	    mPager.setAdapter(mPagerAdapter);
	    mPager.setCurrentItem(0);
		
		return view;

	  }
	
	private Fragment noteFragment(){
		Fragment f = new MemberNotesFragment();
		Bundle bdl = new Bundle(2);
		
        bdl.putString(Services.Statics.MID, memberID);
        f.setArguments(bdl);
        
        selectedFragment = R.id.button_member_navigation_notes;
        return f;
	}
	
	private Fragment membershipFragment() {
		Fragment f = new MemberMembershipFragment();
		Bundle bdl = new Bundle(1);
        bdl.putString(Services.Statics.MID, memberID);
        f.setArguments(bdl);
        
        selectedFragment = R.id.button_member_navigation_memberships;
        return f;
	}
	
	private Fragment visitFragment() {
		Fragment f = new MemberVisitHistoryFragment();
		Bundle bdl = new Bundle(2);
        bdl.putString(Services.Statics.MID, memberID);
        f.setArguments(bdl);
        
        selectedFragment = R.id.button_member_navigation_visits;
        return f;
	}
	
	private Fragment financeFragment() {
		Fragment f = new MemberFinanceFragment();
		Bundle bdl = new Bundle(1);
		bdl.putString(Services.Statics.MID, memberID);
		f.setArguments(bdl);
		
		selectedFragment = R.id.button_member_navigation_finance;
		return f;
	}
	
	private Fragment bookingFragment() {
		Fragment f = new MemberBookingsFragment();
		Bundle bdl = new Bundle(1);
		bdl.putString(Services.Statics.MID, memberID);
		f.setArguments(bdl);
		
		selectedFragment = R.id.button_member_navigation_booking;
		return f;
	}
	/**
	 * Changes the Current Fragment to match the selection.
	 * @param position
	 */
	public void changeFragment(int position) {
		//we need to do a look up so we know which position to set to..?
		mPager.setCurrentItem(position);
	}
	
	
	class MemberDetailsPagerAdapter extends FragmentStatePagerAdapter{
    	FragmentManager fragManager;
    	private static final int NUM_PAGES = 5;
    	
        public MemberDetailsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.fragManager = fragmentManager; 
        }

        @Override
        public Fragment getItem(int position) {
        	MemberDetailsFragment mainFragment = new MemberDetailsFragment();
        	Bundle bdl = new Bundle(2);
        	bdl.putString(Services.Statics.MID, memberID);
        	
        	
        	Fragment f = null;
        	
        	switch (position){
        		case(0):
        			f = membershipFragment();
        			break;
        		case (1):
        			f = noteFragment();
        			break;
        		case (2):
        			f = visitFragment();
        			break;
        		case (3):
        			f = bookingFragment();
        			break;
        		case(4):
        			f = financeFragment();
        			break;
        		default:
        			f = membershipFragment();
        	}
        	
        	mTagListener = (TagFoundListener) mainFragment;
        	bdl.putInt(Services.Statics.KEY, selectedFragment);
        	mainFragment.setArguments(bdl);
        	//mainFragment.setupFragment(f);
        	
        	return mainFragment;
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


	@Override
	public boolean onNewTag(String serial) {
		return mTagListener.onNewTag(serial);
	}
}
