package com.treshna.hornet.classes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R;
import com.treshna.hornet.member.MembersFindFragment;
import com.treshna.hornet.member.MembersFindFragment.OnMemberSelectListener;
import com.treshna.hornet.services.Services;

/*
 * 
 */
public class ClassDetailsSuperFragment extends Fragment implements OnMemberSelectListener, TagFoundListener{
	private String bookingID;
	private View view;
	private ClassDetailsFragment cf;
	private MembersFindFragment mff;
	private TagFoundListener tagListener;
	
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
	
	@Override
	public void onPause() {
		super.onPause();
		
		//we need to detach the fragment here, because otherwise it hangs around.
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.remove(cf);
        if (mff != null) {
        	ft.remove(mff);
        }
        ft.commit();
	}
	
	public void refresh(){
		FragmentManager fragmentManager = this.getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        
        cf = new ClassDetailsFragment();
        tagListener = (TagFoundListener) cf;
        Bundle b = new Bundle(1);
        b.putString(Services.Statics.KEY, bookingID);
        cf.setArguments(b);
        
        if (view.getTag().toString().compareTo("single_panel") == 0) {
        	ft.replace(R.id.frame_right, cf);
        } else {
        	ft.replace(R.id.frame_left, cf);
        	mff = new MembersFindFragment();
            mff.setMemberSelectListener(this);
            ft.replace(R.id.frame_right, mff);
        }
        ft.commit();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_visitor_frame, container, false);
		bookingID = getArguments().getString(Services.Statics.KEY);
		
		TextView hideme = (TextView) view.findViewById(R.id.empty_text);
		if (hideme != null) {
			hideme.setVisibility(View.GONE);
		}
		
        return view;
    }
	

	@Override
	public void onMemberSelect(String id) {
		if (view.getTag().toString().compareTo("single_panel") != 0) {
			ListView theList = (ListView) view.findViewById(android.R.id.list);
			theList.invalidateViews();
			cf.showAlert(id, null, null);
		} 
	}

	@Override
	public boolean onNewTag(String serial) {
		Log.d("CLASS SUPER FRAGMENT", "DETAILS SUPER FRAGMENT RECIEVED NEW TAG");
		return tagListener.onNewTag(serial);
	}
}
