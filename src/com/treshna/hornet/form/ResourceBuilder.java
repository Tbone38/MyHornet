package com.treshna.hornet.form;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class ResourceBuilder implements FormGenerator.FormBuilder, OnClickListener {
	private Activity mActivity;
	private FormGenerator formgen;
	private ContentResolver contentResolver;
	private int resourceID;
	private View mView;
	
	private static final int RES_NAME_ID = 1;
	private static final int RES_TYPE_ID = 2;
	private static final int RES_HIST_ID = 3;
	private static final int RES_SAVE_ID = 4;
	private static final int RES_CANCEL_ID = 5;
	
	
	public ResourceBuilder(Activity activity, int rid) {
		mActivity = activity;
		resourceID = rid;
		formgen = new FormGenerator(mActivity.getLayoutInflater(), mActivity);
		contentResolver = mActivity.getContentResolver();
	}
	
	@Override
	public View generateForm() {
		
		if (resourceID > 0) { //do a look up, use those values as default in the edit field. handle on the on save clicking.
			
		} else {
			
			formgen.addHeading(mActivity.getString(R.string.resource_add));
			
			formgen.addEditText(mActivity.getString(R.string.resource_name), RES_NAME_ID, ContentDescriptor.Resource.Cols.NAME, null);
			
			Cursor cur = contentResolver.query(ContentDescriptor.ResourceType.CONTENT_URI, null, null, null, null);
			ArrayList<String> resourcetypes = new ArrayList<String>();
			while (cur.moveToNext()) {
				resourcetypes.add(cur.getString(cur.getColumnIndex(ContentDescriptor.ResourceType.Cols.NAME)));
			}
			cur.close();
			formgen.addSpinner(mActivity.getString(R.string.resource_type), RES_TYPE_ID, ContentDescriptor.Resource.Cols.RTID, resourcetypes);
			
			formgen.addCheckBox(mActivity.getString(R.string.resource_historic), RES_HIST_ID, ContentDescriptor.Resource.Cols.HISTORY, false);
			
			formgen.addButtonRow(mActivity.getString(R.string.buttonOK), mActivity.getString(R.string.buttonCancel), RES_SAVE_ID, RES_CANCEL_ID, this);
		}
		
		mView = formgen.getForm();
		return mView;
	}
	
	private boolean isValid() {
		boolean is_validated = true;
		
		is_validated = (formgen.getEditText(RES_NAME_ID) == null) ? false : true;
		
		
		return is_validated;
	}
	
	private void saveResource() {
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Resource.Cols.NAME, formgen.getEditText(RES_NAME_ID));
		if (formgen.getCheckBox(RES_HIST_ID)) {
			values.put(ContentDescriptor.Resource.Cols.HISTORY, "t");
		} else {
			values.put(ContentDescriptor.Resource.Cols.HISTORY, "f");
		}
		
		int selectedPos = formgen.getSpinnerPosition(RES_TYPE_ID);
		Cursor cur = contentResolver.query(ContentDescriptor.ResourceType.CONTENT_URI, null, null, null, null);
		if (!cur.moveToPosition(selectedPos)) {
			//our move failed, throw a fit?
			throw new RuntimeException("SOMEBODIES DELETED DATA WHILE WE WERE ATTEMPTING TO USE IT. OR SOMEONE'S BAD AT CODING.");
		}
		values.put(ContentDescriptor.Resource.Cols.RTID, cur.getInt(cur.getColumnIndex(ContentDescriptor.ResourceType.Cols.ID)));
		cur.close();
		
		values.put(ContentDescriptor.Resource.Cols.DEVICESIGNUP, "t");		
		
		if (resourceID > 0) { //update!
			contentResolver.update(ContentDescriptor.Resource.CONTENT_URI, values, ContentDescriptor.Resource.Cols.ID+" = ?",
					new String[] {String.valueOf(resourceID)});
		} else { //insert!
			cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())}, null);
			if (cur.moveToFirst()) {
				resourceID = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
			} else { //no free ids. we should inform the user!
				Toast.makeText(mActivity, "No Resource ID's available, please sync the device.", Toast.LENGTH_LONG).show();
				return;
			}
			cur.close();
			values.put(ContentDescriptor.Resource.Cols.ID, resourceID);
			contentResolver.insert(ContentDescriptor.Resource.CONTENT_URI, values);
			
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String [] {String.valueOf(resourceID),
					String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (RES_SAVE_ID):{
			//get values, check to make sure they're not empty.
			//save it.
			if (isValid()) {
				saveResource();
			}
			break;
		}
		case (RES_CANCEL_ID):{
			mActivity.onBackPressed();
			break;
		}
		}
	}

}
