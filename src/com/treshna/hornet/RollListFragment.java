package com.treshna.hornet;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.treshna.hornet.DatePickerFragment.DatePickerSelectListener;
import com.treshna.hornet.TimePickerFragment.TimePickerSelectListener;


public class RollListFragment extends ListFragment implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor>,
		DatePickerSelectListener, TimePickerSelectListener, OnItemClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	private View view;
	LayoutInflater mInflater;
	private RollListAdapter mAdapter;
	private LoaderManager mLoader;
	private DatePickerFragment mDatePicker;
	private TimePickerFragment mTimePicker;
	private View add_roll_view;
	private AlertDialog mAddRoll;
	private String membership;
	
	private static final String TAG = "RollListFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		mLoader = this.getLoaderManager();
		membership = Services.getAppSettings(getActivity(), "filter_membership");
		if (membership.compareTo("-1")==0) membership =null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.roll_list, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		
		view = setupView();
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mLoader.restartLoader(0, null, this);
		this.getListView().setOnItemClickListener(this);
		getActivity().setTitle("Roll Lists");
	}
		
	private View setupView() {
		TextView add_roll = (TextView) view.findViewById(R.id.button_add_rollcall);
		add_roll.setOnClickListener(this);
		
		mDatePicker = new DatePickerFragment();
		mDatePicker.setDatePickerSelectListener(this);
		mTimePicker = new TimePickerFragment();
		mTimePicker.setTimePickerSelectListener(this);
		
		String[] from = {};
		int[] to = {};
		mAdapter = new RollListAdapter(getActivity(), R.layout.roll_list_row, null, from, to);
		
        setListAdapter(mAdapter);
		
		return view;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		/* SELECT r.*, 
		 * 	(SELECT count(CASE WHEN r1.attended = 't' THEN r1.attended ELSE NULL END) 
		 * 		FROM roll_item r1 WHERE r1.rollid = r.rollid) AS attended,
		 * 	(SELECT count(r2._id) FROM roll_item r2 WHERE r2.rollid = r.rollid) AS total
		 * FROM roll r;
		 */
		return new CursorLoader( getActivity(), ContentDescriptor.RollCall.CONTENT_URI,
				new String[] {"r.*", "(SELECT COUNT(CASE WHEN r1."+ContentDescriptor.RollItem.Cols.ATTENDED+" = 't' THEN "
						+ContentDescriptor.RollItem.Cols.ATTENDED+" ELSE NULL END) FROM "+
						ContentDescriptor.RollItem.NAME+" r1 WHERE r1."+ContentDescriptor.RollItem.Cols.ROLLID
						+" = r."+ContentDescriptor.RollCall.Cols.ROLLID+") AS "
						+ContentDescriptor.RollItem.Cols.ATTENDED, "(SELECT COUNT(r2."
						+ContentDescriptor.RollItem.Cols._ID+") FROM "+ContentDescriptor.RollItem.NAME
						+" r2 WHERE r2."+ContentDescriptor.RollItem.Cols.ROLLID+" = r."
						+ContentDescriptor.RollCall.Cols.ROLLID+") AS "
						+ContentDescriptor.RollItem.Cols.TOTAL}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}
	
	private void setupAlert(View view) {
		//handle on click listeners here
		int setselectedpos = -1;
		TextView set_date = (TextView) view.findViewById(R.id.button_set_date);
		set_date.setOnClickListener(this);
		
		TextView set_time = (TextView) view.findViewById(R.id.button_set_time);
		set_time.setOnClickListener(this);
		
		Spinner membership_spinner = (Spinner) view.findViewById(R.id.roll_membership);
		cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.GID+" = 0",
				null, null);//terrible hard coded value. 0 = holiday programme.
		ArrayList<String> memberships = new ArrayList<String>();
		memberships.add(""); //empty first value
		while (cur.moveToNext()) {
			memberships.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)));
			if (membership != null && membership.compareTo(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)))==0){
				setselectedpos = cur.getPosition();
				setselectedpos +=1; //we have an empty at the start of the list. so +1 to match the list
			}
		}
		cur.close();
		
		ArrayAdapter<String> membershipAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, memberships);
		membershipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		membership_spinner.setAdapter(membershipAdapter);
		if (setselectedpos > 0) {
			membership_spinner.setSelection(setselectedpos);
		}
		
		TextView button_accept = (TextView) view.findViewById(R.id.button_add_roll_text);
		button_accept.setOnClickListener(this);
		TextView button_cancel = (TextView) view.findViewById(R.id.button_cancel_text);
		button_cancel.setOnClickListener(this);
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		for(int i=1; i<emptyFields.size(); i+=1){
			//get label, change colour.
			TextView label = (TextView) add_roll_view.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.button_add_rollcall):{
			//show an alert/window with details to fill out for adding a roll.
			//date & time & name of roll.
			//show what the rolls ID will be?
			add_roll_view = mInflater.inflate(R.layout.alert_add_roll_call, null);
			setupAlert(add_roll_view);
			AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
			build.setTitle("Add Roll");
			build.setView(add_roll_view);
			mAddRoll = build.create();
			mAddRoll.show();
			break;
		}
		case (R.id.button_set_date):{
			mDatePicker.show(this.getChildFragmentManager(), "datePicker");
			break;
		}
		case (R.id.button_set_time):{
			mTimePicker.show(this.getChildFragmentManager(), "timePicker");
			break;
		}
		case (R.id.button_add_roll_text):{
			//validate the input!
			ArrayList<String> validation = validateAddRoll();
			if (!Boolean.parseBoolean(validation.get(0))) { //did we fill in all the fields?
				updateView(validation); //nope!
				break;
			}
			addRoll();
			mAddRoll.dismiss();
			mLoader.restartLoader(0, null, this);
			break;
		}
		case (R.id.button_cancel_text):{
			mAddRoll.dismiss();
			break;
		}
		}
	}
	
	private void addRoll() {
		String name, date, time;
		int programmeid, rollid;
		
	//GET THE VALUES	
		/*EditText namefield = (EditText) add_roll_view.findViewById(R.id.roll_name);
		name = namefield.getEditableText().toString();*/
		
		TextView datefield = (TextView) add_roll_view.findViewById(R.id.button_set_date);
		date = datefield.getText().toString();
		
		TextView timefield = (TextView) add_roll_view.findViewById(R.id.button_set_time);
		time = timefield.getText().toString();
		
		Spinner membershipfield = (Spinner) add_roll_view.findViewById(R.id.roll_membership);
		int cursorposition = membershipfield.getSelectedItemPosition();
		name = membershipfield.getItemAtPosition(cursorposition).toString();
		//the first item in the spinner is empty, use spinner pos -1 to get the equv item from the cursor.
		cursorposition = cursorposition -1;
		
		cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.GID+" = 0", null, null);
		cur.moveToPosition(cursorposition);
		programmeid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Programme.Cols.PID));
		Log.d(TAG, "ProgrammeID: "+programmeid+" FOR programme:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)));
		cur.close();

	//GET THE ROLLID
		cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
				+ContentDescriptor.TableIndex.Values.RollCall.getKey(), null, null);
		cur.moveToFirst();
		rollid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
		cur.close();	
	//INSERT THE ROLL	
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.RollCall.Cols.NAME, name);
		values.put(ContentDescriptor.RollCall.Cols.DATETIME, date+" "+time);
		values.put(ContentDescriptor.RollCall.Cols.DEVICESIGNUP, "t");
		values.put(ContentDescriptor.RollCall.Cols.ROLLID, rollid);
		
		contentResolver.insert(ContentDescriptor.RollCall.CONTENT_URI, values);
		contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.TABLEID+" = ? AND "
				+ContentDescriptor.FreeIds.Cols.ROWID+" = ?", new String[] {
				String.valueOf(ContentDescriptor.TableIndex.Values.RollCall.getKey()), String.valueOf(rollid)});
		
		
	//GET THE LIST OF MEMBERS TO ADD TO THE ROLL	
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.PID+" = ?",
				new String[] {String.valueOf(programmeid)}, null);
		Log.d(TAG, "Cursor Count:"+cur.getCount());
		Set<Integer> memberids = new HashSet<Integer>();
		while (cur.moveToNext()) {
			memberids.add(cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MID)));
		}
		cur.close();
	//ADD EACH MEMBER TO THE ROLL
		Log.d(TAG, "Member Count for chosen Programme:"+memberids.size());
		Iterator<Integer> midsIterator = memberids.iterator();
		while (midsIterator.hasNext()) {
			cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
					+ContentDescriptor.TableIndex.Values.RollItem.getKey(),null, null);
			int rollitemid = -1;
			boolean got_id = cur.moveToFirst(); 
			if (got_id) {
				rollitemid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
			}
			cur.close();	
			
			int mid = midsIterator.next();
			values = new ContentValues();
			values.put(ContentDescriptor.RollItem.Cols.ROLLID, rollid); 
			values.put(ContentDescriptor.RollItem.Cols.MEMBERID, mid);
			values.put(ContentDescriptor.RollItem.Cols.DEVICESIGNUP, "t");
			values.put(ContentDescriptor.RollItem.Cols.ROLLITEMID, rollitemid);
			
			contentResolver.insert(ContentDescriptor.RollItem.CONTENT_URI, values);
			Log.d(TAG, "Inserting MemberID:"+mid);
			
			if (got_id) {
				contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
						+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(rollitemid),
						String.valueOf(ContentDescriptor.TableIndex.Values.RollItem.getKey())});
			}
		}
		//we return.
	}
	
	@SuppressLint("CutPasteId")
	private ArrayList<String> validateAddRoll(){
		ArrayList<String> results = new ArrayList<String>();
		boolean is_valid = true;
		
		/*EditText namefield = (EditText) add_roll_view.findViewById(R.id.roll_name);
		if (namefield.getEditableText().toString().isEmpty()) {
			is_valid = false;
			results.add(String.valueOf(R.id.roll_name_text));
		} else {
			TextView label = (TextView)add_roll_view.findViewById(R.id.roll_name_text);
			label.setTextColor(Color.BLACK);
		}*/
		
		TextView selectdate = (TextView) add_roll_view.findViewById(R.id.button_set_date);
		if (selectdate.getText().toString().compareTo(
				getActivity().getResources().getString(R.string.roll_set_date))==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.button_set_date));
		} else {
			TextView label = (TextView)add_roll_view.findViewById(R.id.button_set_date);
			label.setTextColor(Color.BLACK);
		}
		
		TextView selecttime = (TextView) add_roll_view.findViewById(R.id.button_set_time);
		if (selecttime.getText().toString().compareTo(
				getActivity().getResources().getString(R.string.roll_set_time))==0) {
			is_valid = false;
			results.add(String.valueOf(R.id.button_set_time));
		} else {
			TextView label = (TextView) add_roll_view.findViewById(R.id.button_set_time);
			label.setTextColor(Color.BLACK);
		}
		
		Spinner selectmembership = (Spinner) add_roll_view.findViewById(R.id.roll_membership);
		if (selectmembership.getSelectedItemPosition() <= 0) {
			is_valid = false;
			results.add(String.valueOf(R.id.roll_select_membership));
		} else {
			TextView label = (TextView)add_roll_view.findViewById(R.id.roll_select_membership);
			label.setTextColor(Color.BLACK);
		}
		
		results.add(0, String.valueOf(is_valid));
		return results;
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		//change text on button to be that of the dates.
		String newdate = Services.dateFormat(date, "yyyy MM dd", "dd MMM yyyy");
		TextView setDate = (TextView) add_roll_view.findViewById(R.id.button_set_date);
		if (newdate != null) {
			setDate.setText(newdate);
		} else {
			setDate.setText(date);
		}
	}
	@Override
	public void onTimeSelect(String time, TimePickerFragment theTimePicker) {
		//change text on button to be that of the time.
		String newtime = Services.dateFormat(time, "HH:mm:ss", "hh:mm:ss aa");
		TextView setTime = (TextView) add_roll_view.findViewById(R.id.button_set_time);
		if (newtime != null) {
			setTime.setText(newtime);
		} else {
			setTime.setText(time);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int rollid;
		Cursor cur = contentResolver.query(ContentDescriptor.RollCall.CONTENT_URI, null, null, null, null);
		cur.moveToPosition(position);
		rollid = cur.getInt(cur.getColumnIndex(ContentDescriptor.RollCall.Cols.ROLLID));
		//change fragments to one which shows the Roll-Items.
		Bundle bdl = new Bundle(2);
		bdl.putInt(Services.Statics.KEY, Services.Statics.FragmentType.RollItemList.getKey());
		bdl.putInt(Services.Statics.ROLLID, rollid);
		Intent i = new Intent(getActivity(), EmptyActivity.class);
		i.putExtras(bdl);
		this.startActivity(i);
	}

}