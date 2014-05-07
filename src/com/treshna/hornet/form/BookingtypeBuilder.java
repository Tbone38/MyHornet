package com.treshna.hornet.form;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class BookingtypeBuilder implements FormGenerator.FormBuilder, OnClickListener {
	private Activity mActivity;
	private FormGenerator formgen;
	private ContentResolver contentResolver;
	private int bookingTypeID;
	private View mView;
	
	private static final int BT_NAME_ID = 1;
	private static final int BT_REBOOK_ID = 2;
	private static final int BT_HIST_ID = 3;
	private static final int BT_SAVE_ID = 4;
	private static final int BT_CANCEL_ID = 5;
	
	
	public BookingtypeBuilder(Activity activity, int bid) {
		mActivity = activity;
		bookingTypeID = bid;
		formgen = new FormGenerator(mActivity.getLayoutInflater(), mActivity);
		contentResolver = mActivity.getContentResolver();
	}
	
	@Override
	public View generateForm() {
		if (bookingTypeID > 0) { //do a look up, use those values as default in the edit field. handle on the on save clicking.
			formgen.addHeading(mActivity.getString(R.string.booking_type_edit));
		} else {
			formgen.addHeading(mActivity.getString(R.string.booking_type_add));
			
			formgen.addEditText(mActivity.getString(R.string.booking_type_name), BT_NAME_ID, ContentDescriptor.Bookingtype.Cols.NAME, null);
			
			ArrayList<String> items = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.booking_type_time_between_values)));
			
			formgen.addSpinner(mActivity.getString(R.string.booking_type_rebooking_time), BT_REBOOK_ID, ContentDescriptor.Bookingtype.Cols.MAXBETWEEN,
					items, -1);
			
		}
		
		mView = formgen.getForm();
		return mView;
	}
	
	private boolean isValid() {
		boolean is_validated = true;
		
		
		return is_validated;
	}
	
	private void saveBookingType() {
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (BT_SAVE_ID):{
			//get values, check to make sure they're not empty.
			//save it.
			if (isValid()) {
				saveBookingType();
			}
			break;
		}
		case (BT_CANCEL_ID):{
			mActivity.onBackPressed();
			break;
		}
		}
	}

}
