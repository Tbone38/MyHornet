package com.treshna.hornet.lists;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.form.FormFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class DoorLister extends ListerClass {
	
	private static final String[] FROM = {};
	private static final int[] TO = {};
	
	public DoorLister(Activity activity, ListView list, Fragment f){
		super(activity, list, f);
		mAdapter = new DoorAdapter(mActivity, R.layout.row_programme_group, null, FROM, TO, this);
		mList.setAdapter(mAdapter);
		((MainActivity)mActivity).updateSelectedNavItem(((MainActivity)mActivity).getFragmentNavPosition(this));
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
		case (R.id.action_add):{
			Fragment fragment = new FormFragment();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.DOOR);
        	fragment.setArguments(bdl);
        	String tag = "formFragment";
        	((MainActivity)mActivity).changeFragment(fragment, tag);
        	mode.finish();
        	return true;
		}
		case (R.id.action_edit):{
			Cursor cur = mResolver.query(ContentDescriptor.Door.CONTENT_URI, null, null, null, ContentDescriptor.Door.Cols.DOORID);
			cur.moveToPosition(selectedItem);
			Fragment f = new FormFragment();
			Bundle bdl = new Bundle(2);
			bdl.putInt(Services.Statics.KEY, FormFragment.DOOR);
			bdl.putInt(Services.Statics.ID_KEY, cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORID)));
			f.setArguments(bdl);
			String tag = "formFragment";
        	((MainActivity)mActivity).changeFragment(f, tag);
        	mode.finish();
        	return true;
		}
		case (R.id.action_delete):{
			Toast.makeText(mActivity, "Feature currently unavaible. Use the desktop app instead.", Toast.LENGTH_LONG).show();
			return true;
		}
		default:
			return false;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] columns = {ContentDescriptor.Door.Cols._ID,ContentDescriptor.Door.Cols.DOORID,
				ContentDescriptor.Door.Cols.DOORNAME, ContentDescriptor.Door.Cols.STATUS};
		return new CursorLoader(mActivity, ContentDescriptor.Door.CONTENT_URI, columns, null, null, ContentDescriptor.Door.Cols.DOORID);
	}

	@Override
	public void setTitle(TextView view) {
		view.setText("Doors");
	}
	
	//TODO: remove this once we have deletions working for doors.
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.setTitle("Options");
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.context_add_edit_delete, menu);
		menu.getItem(menu.size()-1).setVisible(false);
		return true;
	}
		
}
