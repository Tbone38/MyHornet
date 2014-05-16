package com.treshna.hornet.lists;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.R;


public class ListerClass implements LoaderManager.LoaderCallbacks<Cursor>, android.view.ActionMode.Callback,
			FormList.Lister {
	protected Activity mActivity;
	protected SimpleCursorAdapter mAdapter;
	protected ActionMode mActionMode;
	protected ListView mList;
	protected int selectedItem = -1;
	protected ContentResolver mResolver;
	protected Fragment mCaller;
	
	public ListerClass(Activity activity, ListView list, Fragment f){
		mActivity = activity;
		mList = list;
		mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);		
		mResolver = mActivity.getContentResolver();
		mCaller = f;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.setTitle("Options");
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.context_add_edit_delete, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return false;
	}
	
	public ActionMode getActionMode(){
		return mActionMode;
	}

	public void startActionMode(View view){
		if (mActionMode == null) {
			mActionMode = view.startActionMode(this);
		}
	}
	
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void setTitle(TextView view) {
		
	}

	@Override
	public void setPosition(int position) {
		selectedItem = position;
		mList.setItemChecked(selectedItem, true);
		mList.setSelection(selectedItem);
		
	}
	
}
