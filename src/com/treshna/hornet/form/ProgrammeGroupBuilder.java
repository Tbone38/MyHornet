package com.treshna.hornet.form;

import android.app.Activity;
import android.content.ContentResolver;
import android.view.View;
import android.view.View.OnClickListener;

import com.treshna.hornet.R;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class ProgrammeGroupBuilder implements FormGenerator.FormBuilder, OnClickListener {
	private Activity mActivity;
	private FormGenerator formgen;
	private ContentResolver contentResolver;
	private int pgID;
	private View mView;
	
	private static final int RES_NAME_ID = 1;
	private static final int RES_HIST_ID = 3;
	private static final int RES_SAVE_ID = 4;
	private static final int RES_CANCEL_ID = 5;
	
	
	public ProgrammeGroupBuilder(Activity activity, int rid) {
		mActivity = activity;
		pgID = rid;
		formgen = new FormGenerator(mActivity.getLayoutInflater(), mActivity);
		contentResolver = mActivity.getContentResolver();
	}
	
	@Override
	public View generateForm() {
		
		if (pgID > 0) { //do a look up, use those values as default in the edit field. handle on the on save clicking.
			
		} else {
			
			formgen.addHeading(mActivity.getString(R.string.resource_add));
			
			formgen.addEditText(mActivity.getString(R.string.resource_name), RES_NAME_ID, ContentDescriptor.Resource.Cols.NAME, null);
			
			formgen.addCheckBox(mActivity.getString(R.string.resource_historic), RES_HIST_ID, ContentDescriptor.Resource.Cols.HISTORY, false);
			
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
