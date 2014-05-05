package com.treshna.hornet.form;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.treshna.hornet.R;
import com.treshna.hornet.services.Services;


public class FormList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	Cursor cur;
	ContentResolver contentResolver;
	private View view;
	LayoutInflater mInflater;
	private LoaderManager mLoader;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		mLoader = this.getLoaderManager();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.fragment_list, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mLoader.restartLoader(0, null, this);
	}
		
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
	
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
}