package com.treshna.hornet;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
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
public class LastVisitorsSuperFragment extends Fragment {
	
	private static ContentResolver contentResolver = null;
    private static Cursor cur = null;
    
	public static final int LASTVISITORS = 1;
	static public String PREF_NAME = "addMember";
	static public String PREF_KEY = "memberType";
	private static final String TAG = "LastVisitorsFragment";
	//public static final int DISPLAYERROR = -1;
	
	//NFCs
	private static Context context;	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        Log.v(TAG, "Creating Last Visitors");
        context = getActivity().getApplicationContext();
        contentResolver = getActivity().getContentResolver();
        if (cur != null) cur.close();        
    }
	
	@Override
	public void onResume(){
		super.onResume();
		
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        LastVisitorsFragment f = new LastVisitorsFragment();
        ft.replace(R.id.frame_right, f);
        ft.commit();
        ((MainActivity) getActivity()).setSelectedTab(1);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.visitor_frame, container, false);
		
        return view;
    }
	
	public void setMemberDisplay(ArrayList<String> tag){
		
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        MemberDetailsFragment f = new MemberDetailsFragment();
        Bundle bdl = new Bundle(2);
        bdl.putString(Services.Statics.MID, tag.get(0));
        bdl.putString(Services.Statics.KEY, tag.get(1));
        f.setArguments(bdl);
        ft.replace(R.id.frame_left, f);
        ft.commit();		
	}
}

