package com.treshna.hornet.lists;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.form.FormFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class ResourceLister extends ListerClass {
	
	private static final String[] FROM = {ContentDescriptor.Resource.Cols.NAME, ContentDescriptor.Resource.Cols.RTNAME};
	private static final int[] TO = {R.id.resource_name, R.id.resource_type_name};
	
	public ResourceLister(Activity activity, ListView list){
		super(activity, list);
		mAdapter = new ResourceAdapter(mActivity, R.layout.row_resource, null, FROM, TO, this);
		mList.setAdapter(mAdapter);
		((MainActivity)mActivity).updateSelectedNavItem(((MainActivity)mActivity).getFragmentNavPosition(this));
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
		case (R.id.action_add):{
			Fragment fragment = new FormFragment();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.RESOURCE);
        	fragment.setArguments(bdl);
        	String tag = "formFragment";
        	((MainActivity)mActivity).changeFragment(fragment, tag);
        	mode.finish();
        	return true;
		}
		case (R.id.action_edit):{
			Cursor cur = mResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, ContentDescriptor.Resource.Cols.ID);
			cur.moveToPosition(selectedItem);
			Fragment f = new FormFragment();
			Bundle bdl = new Bundle(2);
			bdl.putInt(Services.Statics.KEY, FormFragment.RESOURCE);
			bdl.putInt(Services.Statics.ID_KEY, cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)));
			f.setArguments(bdl);
			String tag = "formFragment";
        	((MainActivity)mActivity).changeFragment(f, tag);
        	mode.finish();
        	return true;
		}
		default:
			return false;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] columns = {ContentDescriptor.Resource.Cols.ID, 
				ContentDescriptor.Resource.Cols.NAME, ContentDescriptor.Resource.Cols.RTNAME};
		return new CursorLoader(mActivity, ContentDescriptor.Resource.CONTENT_URI, columns, null, null, ContentDescriptor.Resource.Cols.ID);
	}

	@Override
	public void setTitle(TextView view) {
		view.setText("Resources");
	}

	
	
}
