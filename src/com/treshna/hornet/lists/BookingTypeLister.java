package com.treshna.hornet.lists;

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
import com.treshna.hornet.form.FormFragment;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class BookingTypeLister extends ListerClass {
	
	private static final String[] FROM = {};
	private static final int[] TO = {};
	
	public BookingTypeLister(Activity activity, ListView list){
		super(activity, list);
		mAdapter = new BookingTypeAdapter(mActivity, R.layout.row_resource, null, FROM, TO, this);
		mList.setAdapter(mAdapter);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
		case (R.id.action_add):{
			Fragment fragment = new FormFragment();
        	Bundle bdl = new Bundle(1);
        	bdl.putInt(Services.Statics.KEY, FormFragment.BOOKINGTYPE);
        	fragment.setArguments(bdl);
        	String tag = "formFragment";
        	((MainActivity)mActivity).changeFragment(fragment, tag);
        	mode.finish();
        	return true;
		}
		case (R.id.action_edit):{
			Cursor cur = mResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, null, null, null);
			cur.moveToPosition(selectedItem);
			Fragment f = new FormFragment();
			Bundle bdl = new Bundle(2);
			bdl.putInt(Services.Statics.KEY, FormFragment.BOOKINGTYPE);
			bdl.putInt(Services.Statics.ID_KEY, cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)));
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
		String[] columns = {ContentDescriptor.Bookingtype.Cols.ID, ContentDescriptor.Bookingtype.Cols.NAME, ContentDescriptor.Bookingtype.Cols.PRICE};
		return new CursorLoader(mActivity, ContentDescriptor.Bookingtype.CONTENT_URI, columns, null, null, null);
	}

	@Override
	public void setTitle(TextView view) {
		view.setText("Booking Types");
	}
		
}
