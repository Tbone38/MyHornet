package com.treshna.hornet.form;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class ProgrammeGroupLister extends ListerClass {
	
	private static final String[] FROM = {ContentDescriptor.ProgrammeGroup.Cols.NAME, ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD,
		ContentDescriptor.ProgrammeGroup.Cols.HISTORIC};
	private static final int[] TO = {R.id.programme_group_name, R.id.programme_group_cards, R.id.programme_group_historic};
	
	public ProgrammeGroupLister(Activity activity, ListView list){
		super(activity, list);
		mAdapter = new ProgrammeGroupAdapter(mActivity, R.layout.row_programme_group, null, FROM, TO, this);
		mList.setAdapter(mAdapter);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
		case (R.id.action_add):{
			Fragment fragment = new FormFragment();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.PROGRAMMEGROUP);
        	fragment.setArguments(bdl);
        	String tag = "formFragment";
        	((MainActivity)mActivity).changeFragment(fragment, tag);
        	mode.finish();
        	return true;
		}
		case (R.id.action_edit):{
			Cursor cur = mResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, null, null, null);
			cur.moveToPosition(selectedItem);
			Fragment f = new FormFragment();
			Bundle bdl = new Bundle(2);
			bdl.putInt(Services.Statics.KEY, FormFragment.PROGRAMMEGROUP);
			bdl.putInt(Services.Statics.ID_KEY, cur.getInt(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ID)));
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
		String[] columns = {ContentDescriptor.ProgrammeGroup.Cols._ID, ContentDescriptor.ProgrammeGroup.Cols.NAME, 
				"CASE WHEN "+ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD+" = 't' THEN 'Issues Card' ELSE 'No Cards' END AS "
				+ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD, "CASE WHEN "+ContentDescriptor.ProgrammeGroup.Cols.HISTORIC
				+" = 't' THEN 'Historic' ELSE 'Shown' END AS "+ContentDescriptor.ProgrammeGroup.Cols.HISTORIC};
		return new CursorLoader(mActivity, ContentDescriptor.ProgrammeGroup.CONTENT_URI, columns, null, null, null);
	}

	@Override
	public void setTitle(TextView view) {
		view.setText("Programme Groups");
	}
		
}
