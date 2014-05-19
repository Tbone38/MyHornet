package com.treshna.hornet.member;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R;
import com.treshna.hornet.services.BitmapLoader;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

public class MemberSlideFragment extends Fragment implements TagFoundListener{

	public static final int MEMBER_BOOKINGS = 1;
	public static final int MEMBER_MEMBERSHIPS = 2;
	public static final int MEMBER_NOTES = 3;
	public static final int MEMBER_GALLERY = 4;
	public static final int MEMBER_VISITS = 5;
	public static final int MEMBER_FINANCE = 6;
	
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
    	try {
			selectedFragment = this.getArguments().getInt(Services.Statics.KEY);
		} catch (Exception e) {};
		
		//view = inflater.inflate(R.layout.swipe_layout, container, false);
    	view = inflater.inflate(R.layout.item_member_details_navigation, null);
		// Instantiate a ViewPager and a PagerAdapter.
    	if (container != null) {
	    	 TextView glyph = (TextView)container.findViewById(R.id.empty_glyph);
	    	 TextView glyphtext = (TextView) container.findViewById(R.id.empty_text);
	    	 if (glyph != null) {
	    		 glyph.setVisibility(View.GONE);
	    	 }
	    	 if (glyphtext != null) {
	    		 glyphtext.setVisibility(View.GONE);
	    	 }
    	}
	    mPager = (ViewPager) view.findViewById(R.id.pager);
	    mPagerAdapter = new MemberDetailsPagerAdapter(getChildFragmentManager());
	    mPager.setAdapter(mPagerAdapter);
	    mPager.setCurrentItem(2);
	    
	    PagerTabStrip strip = (PagerTabStrip) view.findViewById(R.id.pts_main);
	    strip.setDrawFullUnderline(false);
	    strip.setTabIndicatorColor(getActivity().getResources().getColor(R.color.android_blue));
	    strip.setTextSpacing(15);
	    //strip.setBackgroundColor(getResources().getColor(R.color.member_statusbar_background));
	    
	    setupLayout();
		return view;

	  }
	
	@SuppressLint("NewApi")
	private View setupLayout() {
		/*Uri uri = Uri.withAppendedPath(ContentDescriptor.Image.IMAGE_JOIN_MEMBER_URI,
				memberID);*/
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID}, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		TextView memberName = (TextView) view.findViewById(R.id.member_navigation_name);
		memberName.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))
					+" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		
		TextView memberNumber = (TextView) view.findViewById(R.id.member_navigation_number);
		memberNumber.setText("#"+memberID);
		
		
		ImageView img = (ImageView) view.findViewById(R.id.member_image);
		String imgDir = getActivity().getExternalFilesDir(null)+"/"+cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IID))
				+"_"+cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID))+".jpg";
		File imgFile = null;
		imgFile = new File(imgDir);
		if (imgFile.exists() == true){
			img.setTag(Integer.parseInt(memberID));
			new BitmapLoader(imgFile,img, 80, 80, Integer.parseInt(memberID));
		    img.setClickable(true);
		//    img.setOnClickListener(this);
		    
		} else {
			img.setClickable(true);
		    //img.setOnClickListener(this);
		    img.setTag(2);
			Drawable imgDrawable = getActivity().getResources().getDrawable(R.drawable.nophotogrey);
			img.setImageDrawable(imgDrawable);
		}
	    
		String happiness = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.HAPPINESS));
		if (happiness != null) {
			if (happiness.compareTo(":(") == 0) {
				ImageView sad;
				sad = (ImageView) view.findViewById(R.id.member_status_sad);
				sad.setVisibility(View.VISIBLE);
			} else if (happiness.compareTo(":|") == 0) {
				ImageView plain;
				plain = (ImageView) view.findViewById(R.id.member_status_plain);
				plain.setVisibility(View.VISIBLE);
			} else if (happiness.compareTo(":)") == 0) {
				ImageView happy;
				happy = (ImageView) view.findViewById(R.id.member_status_happy);
				happy.setVisibility(View.VISIBLE);
			}
		} else {
			ImageView happy;
			happy = (ImageView) view.findViewById(R.id.member_status_happy);
			happy.setVisibility(View.VISIBLE);
		}

		int status = cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols.STATUS));
		if (status >= 0) {
			if (status == 0|| status == 5) { //Current
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_ok);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColour(getResources().getColor(R.color.booking_resource_green)));
			} else if (status == 1) { //on hold
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_hold);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColour(getResources().getColor(R.color.android_blue)));
			} else if (status == 2|| status == 3) { //expired
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_expired);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColour(getResources().getColor(R.color.visitors_red)));
			} else if (status == 4 ) { //promotion
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_casual);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColour(getResources().getColor(R.color.visitors_red)));
			}
		} else {
			ImageView statusView;
			statusView = (ImageView) view.findViewById(R.id.member_status_ok);
			statusView.setVisibility(View.VISIBLE);
			statusView.setColorFilter(Services.ColorFilterGenerator.setColour(Color.GREEN));
		}
		
		cur.close();
		
		TextView memberBalance = (TextView) view.findViewById(R.id.member_status_balance);
		cur = contentResolver.query(ContentDescriptor.MemberBalance.CONTENT_URI, null, 
				ContentDescriptor.MemberBalance.Cols.MID+" = ?", new String[] {memberID}, null);
		
		if (!cur.moveToNext()) {
			memberBalance.setVisibility(View.GONE);
		} else {
			String amount = cur.getString(cur.getColumnIndex(ContentDescriptor.MemberBalance.Cols.BALANCE));
			if (amount.compareTo("$0.00") == 0) {
				memberBalance.setText("Balance: "+amount);
			} else if (amount.substring(0, 1).compareTo("-") == 0) { //member in Credit
				memberBalance.setText("Credit: "+amount.substring(1));
				memberBalance.setTextColor(getActivity().getResources().getColor(R.color.android_blue));
			} else {
				memberBalance.setText("Owing: "+amount);
				memberBalance.setTextColor(getActivity().getResources().getColor(R.color.visitors_red));
			}
		}
		cur.close();
		
	   return view;
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
    	private static final int NUM_PAGES = 6;
    	
    	//Change the Order
    	//private String[] titles = {"Membership Information", "Member Details", "Visit History", "Bookings", "Finance", "Gallery"};
    	private String[] titles = {"Gallery", "Member Details", "Membership Information", "Visit History", "Bookings", "Transactions"};
    	
        public MemberDetailsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.fragManager = fragmentManager; 
        }

        @Override
        public Fragment getItem(int position) {
        	MemberDetailsFragment mainFragment = new MemberDetailsFragment();
        	Bundle bdl = new Bundle(2);
        	bdl.putString(Services.Statics.MID, memberID);
        	
        	switch (position){
        	case (0):
        			selectedFragment = MEMBER_GALLERY;
        			break;
        	case (1):
        			selectedFragment = MEMBER_NOTES;
        			break;
        	case (2):
        			selectedFragment = MEMBER_MEMBERSHIPS;
        			break;
        	case (3):
        			selectedFragment = MEMBER_VISITS;
        			break;
        	case (4):
        			selectedFragment = MEMBER_BOOKINGS;
        			break;
        	case (5):
        			selectedFragment = MEMBER_FINANCE;
        			break;
        	}
        	
        	//mTagListener = (TagFoundListener) mainFragment;
        	bdl.putInt(Services.Statics.KEY, selectedFragment);
        	mainFragment.setArguments(bdl);
        	
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
        
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
        
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
        	super.setPrimaryItem(container, position, object);
        	
        	mTagListener = (TagFoundListener) object;
        }
    }


	@Override
	public boolean onNewTag(String serial) {
		return mTagListener.onNewTag(serial);
	}
}
