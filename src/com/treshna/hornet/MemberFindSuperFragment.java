package com.treshna.hornet;

import java.util.ArrayList;

import com.treshna.hornet.MemberFindFragment.OnMemberSelectListener;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
 * 
 */
public class MemberFindSuperFragment extends Fragment implements OnMemberSelectListener{
   
	private static final String TAG = "LastVisitorsFragment";
	private View view;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        Log.v(TAG, "Creating Last Visitors");
    }
	
	@Override
	public void onResume(){
		super.onResume();
		
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        MemberFindFragment f = new MemberFindFragment();
        f.setMemberSelectListener(this);
        ft.replace(R.id.frame_right, f);
        ft.commit();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.new_visitor_frame, container, false);
		
        return view;
    }
	

	@Override
	public void onMemberSelect(String id) {
		
		if (view.getTag().toString().compareTo("single_panel") == 0) {
			ArrayList<String> tag = new ArrayList<String>();
			tag.add(id);
			tag.add(null);
			Intent intent = new Intent(getActivity(), EmptyActivity.class);
			intent.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MemberDetails.getKey());
			intent.putStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID, tag);
			getActivity().startActivity(intent);
		} else {
			FragmentManager fragmentManager = this.getChildFragmentManager();
	        FragmentTransaction ft = fragmentManager.beginTransaction();
	        MemberDetailsFragment f = new MemberDetailsFragment();
	        Bundle bdl = new Bundle(1);
	        bdl.putString(Services.Statics.MID, id);
	        f.setArguments(bdl);
	        ft.replace(R.id.frame_left, f);
	        ft.commit();
		}
				
	}
}

