package com.treshna.hornet.member;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.member.MembersFindFragment.OnMemberSelectListener;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.services.Services.Statics;
import com.treshna.hornet.services.Services.Typefaces;

/*
 * 
 */
public class MembersFindSuperFragment extends Fragment implements OnMemberSelectListener{
   
	private View view;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
    }
	
	@Override
	public void onResume(){
		super.onResume();
		
		refresh();
	}
	
	public void refresh(){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        MembersFindFragment f = new MembersFindFragment();
        f.setMemberSelectListener(this);
        ft.replace(R.id.frame_right, f);
        ft.commit();
        
        ((MainActivity) getActivity()).changeFragment(null, "findmember");
        if (view.getTag().toString().compareTo("single_panel") != 0) {
        	TextView empty_glyph = (TextView) view.findViewById(R.id.empty_glyph);
        	empty_glyph.setTypeface(Services.Typefaces.get(getActivity(), "fonts/glyphicons_regular.ttf"));
        }
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.visitor_frame, container, false);
		
        return view;
    }
	

	@Override
	public void onMemberSelect(String id) {
		
		if (view.getTag().toString().compareTo("single_panel") == 0) {
			ArrayList<String> tag = new ArrayList<String>();
			tag.add(id);
			tag.add(null);
			
			//Fragment f = new MemberDetailsFragment();
			Fragment f = new MemberSlideFragment();
			
			Bundle bdl = new Bundle(1);
	        bdl.putString(Services.Statics.MID, id);
			f.setArguments(bdl);
			((MainActivity)getActivity()).changeFragment(f, "memberDetails");
			
		} else { //double panel
			//redraw the list view
			ListView theList = (ListView) view.findViewById(android.R.id.list);
			theList.invalidateViews();
			
			//show member details
			FragmentManager fragmentManager = this.getChildFragmentManager();
	        FragmentTransaction ft = fragmentManager.beginTransaction();
	        //MemberDetailsFragment f = new MemberDetailsFragment();
	        Fragment f = new MemberSlideFragment();
	        Bundle bdl = new Bundle(1);
	        bdl.putString(Services.Statics.MID, id);
	        f.setArguments(bdl);
	        ft.replace(R.id.frame_left, f);
	        ft.commit();
		}
				
	}
}

