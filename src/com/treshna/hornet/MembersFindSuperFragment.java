package com.treshna.hornet;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.MembersFindFragment.OnMemberSelectListener;

/*
 * 
 */
public class MembersFindSuperFragment extends Fragment implements OnMemberSelectListener{
   
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
        MembersFindFragment f = new MembersFindFragment();
        f.setMemberSelectListener(this);
        ft.replace(R.id.frame_right, f);
        ft.commit();
        ((MainActivity) getActivity()).setSelectedTab(0);
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
			Intent intent = new Intent(getActivity(), EmptyActivity.class);
			intent.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MemberDetails.getKey());
			intent.putStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID, tag);
			getActivity().startActivity(intent);
		} else { //double panel
			//redraw the list view
			ListView theList = (ListView) view.findViewById(android.R.id.list);
			theList.invalidateViews();
			
			//show member details
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

