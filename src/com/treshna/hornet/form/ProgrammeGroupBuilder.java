package com.treshna.hornet.form;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;

import com.treshna.hornet.R;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.ProgrammeGroup;


public class ProgrammeGroupBuilder implements FormGenerator.FormBuilder, OnClickListener {
	private Activity mActivity;
	private FormGenerator formgen;
	private int pgID;
	private View mView;
	private ContentResolver mResolver;
	
	private static final int RES_NAME_ID = 1;
	private static final int RES_ISSUECARDS_ID = 3;
	private static final int RES_HIST_ID = 3;
	private static final int RES_SAVE_ID = 4;
	private static final int RES_CANCEL_ID = 5;
	
	
	public ProgrammeGroupBuilder(Activity activity, int rid) {
		mActivity = activity;
		pgID = rid;
		formgen = new FormGenerator(mActivity.getLayoutInflater(), mActivity);
		mResolver = activity.getContentResolver();
		
	}
	
	@Override
	public View generateForm() {
		
		if (pgID > 0) { //do a look up, use those values as default in the edit field. handle on the on save clicking.
			
		} else {
			
			formgen.addHeading(mActivity.getString(R.string.programme_group_add));
			
			formgen.addEditText(mActivity.getString(R.string.programme_group_name), RES_NAME_ID, ProgrammeGroup.Cols.NAME, null);
			
			formgen.addCheckBox(mActivity.getString(R.string.programme_group_issue_card), RES_ISSUECARDS_ID, ProgrammeGroup.Cols.ISSUECARD, true);
			
			formgen.addCheckBox(mActivity.getString(R.string.programme_group_historic), RES_HIST_ID, ProgrammeGroup.Cols.HISTORIC, false);
			
			formgen.addButtonRow(mActivity.getString(R.string.buttonSave), mActivity.getString(R.string.buttonCancel), RES_SAVE_ID, RES_CANCEL_ID, this);
		}
		
		mView = formgen.getForm();
		return mView;
	}
	
	private boolean isValid() {
		boolean is_validated = true;
		
		is_validated = (formgen.getEditText(RES_NAME_ID) == null) ? false : true;
		
		
		return is_validated;
	}
	
	private void saveProgrammeGroup() {
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.ProgrammeGroup.Cols.NAME, formgen.getEditText(RES_NAME_ID));
		values.put(ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD, ((formgen.getCheckBox(RES_ISSUECARDS_ID) == false)? "f" : "t"));
		values.put(ContentDescriptor.ProgrammeGroup.Cols.HISTORIC, ((formgen.getCheckBox(RES_HIST_ID) == false)? "f" : "t"));
		values.put(ContentDescriptor.ProgrammeGroup.Cols.DEVICESIGNUP, "t");
		
		if (pgID > 0) {
			mResolver.update(ContentDescriptor.ProgrammeGroup.CONTENT_URI, values, ContentDescriptor.ProgrammeGroup.Cols.ID+" = ?",
					new String[] {String.valueOf(pgID)});
		} else { //INSERT, with a key.
			Cursor cur = mResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey())}, null);
			if (!cur.moveToFirst()) {
				//no ids available. throw a fit!
				throw new RuntimeException("FRIDAYS!"); //FIXME show a toast? 
			}
			pgID = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
			cur.close();
			values.put(ContentDescriptor.ProgrammeGroup.Cols.ID, pgID);
			
			mResolver.insert(ContentDescriptor.ProgrammeGroup.CONTENT_URI, values);
			
			mResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.TABLEID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.ROWID+" = ?",new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey()),
					String.valueOf(pgID)});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (RES_SAVE_ID):{
			//get values, check to make sure they're not empty.
			//save it.
			if (isValid()) {
				saveProgrammeGroup();
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
