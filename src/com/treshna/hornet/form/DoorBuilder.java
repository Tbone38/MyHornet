package com.treshna.hornet.form;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class DoorBuilder implements FormGenerator.FormBuilder, OnClickListener {
	private Activity mActivity;
	private FormGenerator formgen;
	private ContentResolver contentResolver;
	private int doorID;
	private View mView;
	
	private static final int DOOR_NAME_ID = 1;
	private static final int DOOR_STATUS_ID = 2;
	private static final int DOOR_CONCESSION_ID = 3;
	private static final int DOOR_CHECKOUT_ID = 4;
	private static final int DOOR_WOMEN_ID = 5;
	private static final int DOOR_VISITS_ID = 6;
	private static final int DOOR_OPEN_ID = 7;
	private static final int DOOR_SAVE_ID = 8;
	private static final int DOOR_CANCEL_ID = 9;

	
	public DoorBuilder(Activity activity, int did) {
		mActivity = activity;
		doorID = did;
		formgen = new FormGenerator(mActivity.getLayoutInflater(), mActivity);
		contentResolver = mActivity.getContentResolver();
	}
	
	@Override
	public View generateForm() {
		if (doorID > 0) { //do a look up, use those values as default in the edit field. handle on the on save clicking.
			
			Cursor cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, ContentDescriptor.Door.Cols.DOORID+" = ?",
					new String[] {String.valueOf(doorID)}, null);
			if (!cur.moveToFirst()) {
				return null;
			}
			formgen.addHeading(mActivity.getString(R.string.door_edit));
			
			formgen.addEditText(mActivity.getString(R.string.door_name), DOOR_NAME_ID, ContentDescriptor.Door.Cols.DOORNAME,
					cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORNAME)));
			
			ArrayList<String> status_keys = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.door_status_keys)));
			formgen.addSpinner(mActivity.getString(R.string.door_status), DOOR_STATUS_ID, ContentDescriptor.Door.Cols.STATUS, status_keys, 
					cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.STATUS)));
			
			ArrayList<String> concession_keys = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.door_concession_keys)));
			formgen.addSpinner(mActivity.getString(R.string.door_concession), DOOR_CONCESSION_ID, ContentDescriptor.Door.Cols.CONCESSION, 
					concession_keys, cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.CONCESSION)));
			
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.CHECKOUT)) || 
					cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.CHECKOUT)).compareTo("f") == 0) {
				formgen.addCheckBox(mActivity.getString(R.string.door_checkout), DOOR_CHECKOUT_ID, ContentDescriptor.Door.Cols.CHECKOUT, false);
			} else {
				formgen.addCheckBox(mActivity.getString(R.string.door_checkout), DOOR_CHECKOUT_ID, ContentDescriptor.Door.Cols.CHECKOUT, true);
			}
			
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.WOMENONLY)) ||
					cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.WOMENONLY)).compareTo("f") == 0) {
				formgen.addCheckBox(mActivity.getString(R.string.door_women), DOOR_WOMEN_ID, ContentDescriptor.Door.Cols.WOMENONLY, false);
			} else {
				formgen.addCheckBox(mActivity.getString(R.string.door_women), DOOR_WOMEN_ID, ContentDescriptor.Door.Cols.WOMENONLY, true);
			}
			
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.LASTVISITS)) || 
					cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.LASTVISITS)).compareTo("t") == 0) {
				formgen.addCheckBox(mActivity.getString(R.string.door_show_visits), DOOR_VISITS_ID, ContentDescriptor.Door.Cols.LASTVISITS, true);
			} else {
				formgen.addCheckBox(mActivity.getString(R.string.door_show_visits), DOOR_VISITS_ID, ContentDescriptor.Door.Cols.LASTVISITS, false);
			}
			
			//we also need to test/open the door here.
			//use an async task to send the notify command.
			formgen.addClickableText(mActivity.getString(R.string.door_open_label), DOOR_OPEN_ID, "openDoor",
					mActivity.getString(R.string.door_open), DOOR_OPEN_ID, this);
			
			formgen.addButtonRow(mActivity.getString(R.string.buttonOK), mActivity.getString(R.string.buttonCancel), DOOR_SAVE_ID, DOOR_CANCEL_ID, this);
			
		} else {
			formgen.addHeading(mActivity.getString(R.string.door_add));
			
			formgen.addEditText(mActivity.getString(R.string.door_name), DOOR_NAME_ID, ContentDescriptor.Door.Cols.DOORNAME, null);
			
			ArrayList<String> status_keys = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.door_status_keys)));
			formgen.addSpinner(mActivity.getString(R.string.door_status), DOOR_STATUS_ID, ContentDescriptor.Door.Cols.STATUS, status_keys, 1);
			
			ArrayList<String> concession_keys = new ArrayList<String>(Arrays.asList(mActivity.getResources().getStringArray(R.array.door_concession_keys)));
			formgen.addSpinner(mActivity.getString(R.string.door_concession), DOOR_CONCESSION_ID, ContentDescriptor.Door.Cols.CONCESSION, 
					concession_keys, -1);
			
			formgen.addCheckBox(mActivity.getString(R.string.door_checkout), DOOR_CHECKOUT_ID, ContentDescriptor.Door.Cols.CHECKOUT, false);
			formgen.addCheckBox(mActivity.getString(R.string.door_women), DOOR_WOMEN_ID, ContentDescriptor.Door.Cols.WOMENONLY, false);
			formgen.addCheckBox(mActivity.getString(R.string.door_show_visits), DOOR_VISITS_ID, ContentDescriptor.Door.Cols.LASTVISITS, true);
			
			formgen.addButtonRow(mActivity.getString(R.string.buttonOK), mActivity.getString(R.string.buttonCancel), DOOR_SAVE_ID, DOOR_CANCEL_ID, this);
		}
		
		mView = formgen.getForm();
		return mView;
	}
	
	private boolean isValid() {
		boolean is_validated = true;
		
		is_validated = (formgen.getEditText(DOOR_NAME_ID, null) == null) ? false : true;
		
		
		return is_validated;
	}
	
	private void saveDoor() {
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Door.Cols.DOORNAME, formgen.getEditText(DOOR_NAME_ID, null));
		values.put(ContentDescriptor.Door.Cols.CHECKOUT, (formgen.getCheckBox(DOOR_CHECKOUT_ID)) ? "t": "f");
		values.put(ContentDescriptor.Door.Cols.WOMENONLY, (formgen.getCheckBox(DOOR_WOMEN_ID)) ? "t" : "f");
		values.put(ContentDescriptor.Door.Cols.LASTVISITS, (formgen.getCheckBox(DOOR_VISITS_ID)) ? "t" : "f");
		
		{ //spinner handling! ewwww grosss!
			int selected_status = formgen.getSpinnerPosition(DOOR_STATUS_ID);
			String[] statuss = mActivity.getResources().getStringArray(R.array.door_status_values);
			values.put(ContentDescriptor.Door.Cols.STATUS, statuss[selected_status]);
			
			int selected_concession = formgen.getSpinnerPosition(DOOR_CONCESSION_ID);
			String[] concessions = mActivity.getResources().getStringArray(R.array.door_concession_values);
			values.put(ContentDescriptor.Door.Cols.CONCESSION, concessions[selected_concession]);
		}
		
		values.put(ContentDescriptor.Door.Cols.DEVICESIGNUP, "t");
		
		if (doorID > 0) { //update!
			contentResolver.update(ContentDescriptor.Door.CONTENT_URI, values, ContentDescriptor.Door.Cols.DOORID+" = ?",
					new String[] {String.valueOf(doorID)});
		} else {
			Cursor cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Door.getKey())}, null);
			if (!cur.moveToFirst()) {
				Toast.makeText(mActivity, "No ID's available, consider syncing this device, before trying again.", Toast.LENGTH_LONG).show();
				return;
			}
			doorID = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
			cur.close();
			
			values.put(ContentDescriptor.Door.Cols.DOORID, doorID);
			
			contentResolver.insert(ContentDescriptor.Door.CONTENT_URI, values);
			
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(doorID),
					String.valueOf(ContentDescriptor.TableIndex.Values.Door.getKey())});
		}
		mActivity.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (DOOR_SAVE_ID):{
			//get values, check to make sure they're not empty.
			//save it.
			if (isValid()) {
				saveDoor();
			}
			break;
		}
		case (DOOR_CANCEL_ID):{
			mActivity.onBackPressed();
			break;
		}
		case (DOOR_OPEN_ID):{
			//start async, send a notify opendoor1 ...
			OpenDoor doorController = new OpenDoor();
			doorController.execute(null, null);
			break;
		}
		}
	}
	
	private class OpenDoor extends AsyncTask<String, Integer, Boolean> {
		private HornetDBService sync;
		
		public OpenDoor(){
			sync = new HornetDBService();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			return sync.openDoor(doorID, mActivity);
		}
		
	}

}
