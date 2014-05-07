package com.treshna.hornet.form;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
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
	private static final int BT_PRICE_ID = 2;
	private static final int BT_REBOOK_ID = 3;
	private static final int BT_PERIOD_ID = 4;
	private static final int BT_DESC_ID = 5;
	private static final int BT_ONLINE_ID = 6;
	private static final int BT_HIST_ID = 7;
	private static final int BT_SAVE_ID = 8;
	private static final int BT_CANCEL_ID = 9;
	
	
	public BookingtypeBuilder(Activity activity, int bid) {
		mActivity = activity;
		bookingTypeID = bid;
		formgen = new FormGenerator(mActivity.getLayoutInflater(), mActivity);
		contentResolver = mActivity.getContentResolver();
	}
	
	@Override
	public View generateForm() {
		if (bookingTypeID > 0) { //do a look up, use those values as default in the edit field. handle on the on save clicking.
			Cursor cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.BTID+" = ?",
					new String[] {String.valueOf(bookingTypeID)}, null);
			if (!cur.moveToFirst()) {
				//throw a fit.
				return null;
			}
			
			formgen.addHeading(mActivity.getString(R.string.booking_type_edit));
			
			formgen.addEditText(mActivity.getString(R.string.booking_type_name), BT_NAME_ID, ContentDescriptor.Bookingtype.Cols.NAME, 
					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.NAME)));
			
			formgen.addEditText(mActivity.getString(R.string.booking_type_price), BT_PRICE_ID, ContentDescriptor.Bookingtype.Cols.PRICE,
					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)));
			formgen.setEditTextType(BT_PRICE_ID, EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
			
			//gross spinner stuff here
			{ 
				int rebook_pos = 0;
				if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN)) != null) {
					String[] rebook_values = mActivity.getResources().getStringArray(R.array.booking_type_time_between_values);
					for (int i=0; i< rebook_values.length; i++) {
						if (rebook_values[i].compareTo(cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN))) == 0) {
							rebook_pos = i;
						}
					}
				}
				ArrayList<String> rebook_items = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.booking_type_time_between_keys)));
				formgen.addSpinner(mActivity.getString(R.string.booking_type_rebooking_time), BT_REBOOK_ID, ContentDescriptor.Bookingtype.Cols.MAXBETWEEN,
						rebook_items, rebook_pos);

				int period_pos = 0;
				if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH)) != null) {
					String[] period_values = mActivity.getResources().getStringArray(R.array.booking_type_time_values);
					for (int i=0; i<period_values.length;i++) {
						if (period_values[i].compareTo(cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH))) == 0) {
							period_pos = i;
						}
					}
				}
				ArrayList<String> period_items = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.booking_type_time_keys)));
				formgen.addSpinner(mActivity.getString(R.string.booking_type_default_time), BT_PERIOD_ID, ContentDescriptor.Bookingtype.Cols.LENGTH,
						period_items, -1);
			}

			formgen.addEditText(mActivity.getString(R.string.booking_type_description), BT_DESC_ID, ContentDescriptor.Bookingtype.Cols.DESCRIPTION,
					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION)));
			
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK)) != null && 
					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK)).compareTo("t") == 0) {
				formgen.addCheckBox(mActivity.getString(R.string.booking_type_online), BT_ONLINE_ID, ContentDescriptor.Bookingtype.Cols.ONLINEBOOK, 
						true);
			} else {
				formgen.addCheckBox(mActivity.getString(R.string.booking_type_online), BT_ONLINE_ID, ContentDescriptor.Bookingtype.Cols.ONLINEBOOK, 
						false);
			}
			
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.HISTORY)) != null &&
					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.HISTORY)).compareTo("t") == 0) {
				formgen.addCheckBox(mActivity.getString(R.string.booking_type_historic), BT_HIST_ID, ContentDescriptor.Bookingtype.Cols.HISTORY,
						true);
			} else {
				formgen.addCheckBox(mActivity.getString(R.string.booking_type_historic), BT_HIST_ID, ContentDescriptor.Bookingtype.Cols.HISTORY,
						false);
			}
			
			formgen.addButtonRow(mActivity.getString(R.string.buttonOK), mActivity.getString(R.string.buttonCancel), BT_SAVE_ID, BT_CANCEL_ID, this);
			
		} else {
			formgen.addHeading(mActivity.getString(R.string.booking_type_add));
			
			formgen.addEditText(mActivity.getString(R.string.booking_type_name), BT_NAME_ID, ContentDescriptor.Bookingtype.Cols.NAME, null);
			
			formgen.addEditText(mActivity.getString(R.string.booking_type_price), BT_PRICE_ID, ContentDescriptor.Bookingtype.Cols.PRICE, null);
			formgen.setEditTextType(BT_PRICE_ID, EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
			
			ArrayList<String> rebook_items = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.booking_type_time_between_keys)));
			formgen.addSpinner(mActivity.getString(R.string.booking_type_rebooking_time), BT_REBOOK_ID, ContentDescriptor.Bookingtype.Cols.MAXBETWEEN,
					rebook_items, -1);
			
			ArrayList<String> period_items = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.booking_type_time_keys)));
			formgen.addSpinner(mActivity.getString(R.string.booking_type_default_time), BT_PERIOD_ID, ContentDescriptor.Bookingtype.Cols.LENGTH,
					period_items, -1);
			
			formgen.addEditText(mActivity.getString(R.string.booking_type_description), BT_DESC_ID, ContentDescriptor.Bookingtype.Cols.DESCRIPTION,
					null);
			
			formgen.addCheckBox(mActivity.getString(R.string.booking_type_online), BT_ONLINE_ID, ContentDescriptor.Bookingtype.Cols.ONLINEBOOK, true);
			formgen.addCheckBox(mActivity.getString(R.string.booking_type_historic), BT_HIST_ID, ContentDescriptor.Bookingtype.Cols.HISTORY, false);
			
			formgen.addButtonRow(mActivity.getString(R.string.buttonOK), mActivity.getString(R.string.buttonCancel), BT_SAVE_ID, BT_CANCEL_ID, this);
		}
		
		mView = formgen.getForm();
		return mView;
	}
	
	private boolean isValid() {
		boolean is_validated = true;
		
		is_validated = (formgen.getEditText(BT_NAME_ID, null) == null) ? false : true;
		
		if (formgen.getEditText(BT_PRICE_ID, null) != null) {
			String price_text = formgen.getEditText(BT_PRICE_ID, null);
			price_text = price_text.replaceAll("[^\\d.]", "");
			try {
				double price = Double.valueOf(price_text);
				formgen.setEditLabelColour(BT_PRICE_ID, false);
			} catch (NumberFormatException e) 
			{
				formgen.setEditLabelColour(BT_PRICE_ID, true);
				is_validated = false;
			}
		} else {
			formgen.setEditLabelColour(BT_PRICE_ID, false);
		}
		
		return is_validated;
	}
	
	private void saveBookingType() {
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.Bookingtype.Cols.NAME, formgen.getEditText(BT_NAME_ID, null));
		if (formgen.getEditText(BT_PRICE_ID, null) != null) {
			String price_text = formgen.getEditText(BT_PRICE_ID, null);
			price_text = price_text.replaceAll("[^\\d.]", "");
			try {
				double price = Double.valueOf(price_text);
				values.put(ContentDescriptor.Bookingtype.Cols.PRICE, price);
			} catch (NumberFormatException e) 
			{
			/*Prices are optional, if we've gotten hear 
			someones added too many dots, or otherwise typed an invalid double.
			we just won't add a price.
			 */
			}
		}
		
		if (formgen.getEditText(BT_DESC_ID, null) != null) {
			values.put(ContentDescriptor.Bookingtype.Cols.DESCRIPTION, formgen.getEditText(BT_DESC_ID, null));
		}
		
		values.put(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK, (formgen.getCheckBox(BT_ONLINE_ID)) ? "t": "f");
		values.put(ContentDescriptor.Bookingtype.Cols.HISTORY, (formgen.getCheckBox(BT_HIST_ID)) ? "t" : "f");
		
		//spinner handling is going to be really gross.
		{
			int selected_rebook = formgen.getSpinnerPosition(BT_REBOOK_ID);
			String[] rebooks = mActivity.getResources().getStringArray(R.array.booking_type_time_between_values);
			if (selected_rebook > 0) { //we ignore the 0 pos, because its a null value anyway.
				values.put(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN, rebooks[selected_rebook]);
			}
			
			int selected_period = formgen.getSpinnerPosition(BT_PERIOD_ID);
			String[] periods = mActivity.getResources().getStringArray(R.array.booking_type_time_values);
			if (selected_period > 0) { //same again here, pos 0 = null.
				values.put(ContentDescriptor.Bookingtype.Cols.LENGTH, periods[selected_period]);
			}
		} // shockingly, that wasn't gross.
		
		values.put(ContentDescriptor.Bookingtype.Cols.DEVICE_SIGNUP, "t");
		
		if (bookingTypeID > 0) { //update!
			contentResolver.update(ContentDescriptor.Bookingtype.CONTENT_URI, values, ContentDescriptor.Bookingtype.Cols.BTID+" = ?",
					new String[] {String.valueOf(bookingTypeID)});
		} else { //insert!
			Cursor cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Bookingtype.getKey())}, null);
			if (!cur.moveToFirst()){
				Toast.makeText(mActivity, "No ID's are available, consider syncing the device and trying again.", Toast.LENGTH_LONG).show();
				return;
			}
			bookingTypeID = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
			values.put(ContentDescriptor.Bookingtype.Cols.BTID, bookingTypeID);
			
			contentResolver.insert(ContentDescriptor.Bookingtype.CONTENT_URI, values);
			
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(bookingTypeID),
					String.valueOf(ContentDescriptor.TableIndex.Values.Bookingtype.getKey())});
		}
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
