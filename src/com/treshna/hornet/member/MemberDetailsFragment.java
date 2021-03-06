package com.treshna.hornet.member;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.services.BitmapLoader;
import com.treshna.hornet.services.CameraWrapper;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.visitor.VisitorsViewAdapter;


	//TODO: more null handling
public class MemberDetailsFragment extends Fragment implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	private int selectedFragment = 0;
	private TagFoundListener tagFoundListener;
	private static final String TAG = "MemberDetails";
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
	
		memberID = this.getArguments().getString(Services.Statics.MID);
		try {
			selectedFragment = this.getArguments().getInt(Services.Statics.KEY);
		} catch (Exception e) {};
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		  super.onCreateView(inflater, container, savedInstanceState);
	
		  if (selectedFragment != MemberSlideFragment.MEMBER_GALLERY) {
			  view = inflater.inflate(R.layout.fragment_member_details_scroll, container, false);
		  } else {
			  view = inflater.inflate(R.layout.fragment_member_details, container, false);
		  }
		  
		  setupFragment(null);
		  return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//setupLayout();
	}
	
	private Fragment generateFragment() {
		/*Log.w(TAG, "WE'RE GENERATING A FRAG");
		Log.w(TAG, "OUR SELECTED VALUE IS:"+selectedFragment);*/
		Fragment f = null;
		switch (selectedFragment){
		case (MemberSlideFragment.MEMBER_NOTES):{
			//Log.w(TAG, "NOTES!");
			f = new MemberNotesFragment();
			Bundle bdl = new Bundle(2);
	        bdl.putString(Services.Statics.MID, memberID);
	        f.setArguments(bdl);
			break;
		}
		case (MemberSlideFragment.MEMBER_MEMBERSHIPS):{
			//Log.w(TAG, "Membership!");
			f = new MemberMembershipFragment();
			Bundle bdl = new Bundle(1);
	        bdl.putString(Services.Statics.MID, memberID);
	        f.setArguments(bdl);
	        break;
		}
		case (MemberSlideFragment.MEMBER_VISITS):{
			//Log.w(TAG, "Visits!");
			f = new MemberVisitHistoryFragment();
			Bundle bdl = new Bundle(2);
	        bdl.putString(Services.Statics.MID, memberID);
	        f.setArguments(bdl);
			break;
		}
		case (MemberSlideFragment.MEMBER_FINANCE):{
			//Log.w(TAG, "finance!");
			f = new MemberFinanceFragment();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberID);
			f.setArguments(bdl);
			break;
		}
		case (MemberSlideFragment.MEMBER_BOOKINGS):{
			//Log.w(TAG, "Booking!");
			f = new MemberBookingsFragment();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberID);
			f.setArguments(bdl);
			break;
		}
		case (MemberSlideFragment.MEMBER_GALLERY):{
			f = new MemberGalleryFragment();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberID);
			f.setArguments(bdl);
		}
		default:
			//Log.w(TAG, "NULL?!!");
		}
		
		return f;
	}
	
	public void setupFragment(Fragment f) {
		if (f == null) {
			if (selectedFragment > 0) {
				f = generateFragment();
			} else {
				f = new MemberMembershipFragment();
				Bundle bdl = new Bundle(1);
		        bdl.putString(Services.Statics.MID, memberID);
		        f.setArguments(bdl);
		        selectedFragment = MemberSlideFragment.MEMBER_MEMBERSHIPS;
			}
		}
		tagFoundListener = (TagFoundListener) f;
		FragmentManager fm = this.getChildFragmentManager();		
		FragmentTransaction ft = fm.beginTransaction();

		ft.replace(R.id.frame_bottom, f);
		ft.commit();
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case (R.id.member_image):{
			int state = Integer.parseInt(v.getTag().toString());
			if (state == 1) {
				//do nothing.
			} else {
				Intent camera = new Intent(getActivity(), CameraWrapper.class);
				camera.putExtra(VisitorsViewAdapter.EXTRA_ID,memberID);
				getActivity().startActivity(camera);
			}
			break;
		}
		}
	}

	@Override
	public boolean onNewTag(String serial) {
		return tagFoundListener.onNewTag(serial);
	}
	 
}
