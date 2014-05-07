package com.treshna.hornet.lists;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.R;
import com.treshna.hornet.form.FormFragment;
import com.treshna.hornet.services.Services;


public class FormList extends ListFragment {
	Cursor cur;
	ContentResolver contentResolver;
	private View view;
	LayoutInflater mInflater;
	private LoaderManager mLoader;
	private LoaderManager.LoaderCallbacks<Cursor> mCallback;
	private Lister mLister;
	private ListView mList;
	
	private int buildertype;
	
	public interface Lister {
		public ActionMode getActionMode();
		public void startActionMode(View view);
		public void setTitle(TextView view);
		/*public void onCreateContextMenu(ContextMenu menu,
				View v,
                ContextMenuInfo menuInfo);*/
		public void setPosition(int position);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		
		buildertype = this.getArguments().getInt(Services.Statics.KEY);
		contentResolver = getActivity().getContentResolver();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.fragment_list, container, false);
		mList = (ListView) view.findViewById(android.R.id.list);
		//this.registerForContextMenu(mList);
		
		switch (buildertype){
		case (FormFragment.RESOURCE):{
			mLister = new ResourceLister(getActivity(), mList);
			break;
		}
		case (FormFragment.PROGRAMMEGROUP):{
			mLister = new ProgrammeGroupLister(getActivity(), mList);
			break;
		}
		case (FormFragment.BOOKINGTYPE):{
			mLister = new BookingTypeLister(getActivity(), mList);
		}
		}
		
		mCallback = (LoaderManager.LoaderCallbacks<Cursor>) mLister;
		mLoader = this.getLoaderManager();
		mLister.setTitle((TextView) view.findViewById(R.id.heading));
		return view;
	}
	
	/*@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    
	    mLister.onCreateContextMenu(menu, v, menuInfo);
	}*/
	
	@Override
	public void onResume() {
		super.onResume();
		mList.invalidateViews();
		mLoader.restartLoader(0, null, mCallback);
	}
}