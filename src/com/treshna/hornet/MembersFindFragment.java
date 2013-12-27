package com.treshna.hornet;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MembersFindFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {
	
	Cursor cur;
	ContentResolver contentResolver;
	MembersFindAdapter mAdapter;
	LoaderManager loadermanager;
	Context parent;
	View view;
	private static String input;
	private String membership = null;
	private String gender = null;
	private String owes = null;
	private String programmeGroup = null;
	private boolean is_booking;
	private OnMemberSelectListener mCallback;
	private static final String TAG = "MemberFindFragment";
	private AlertDialog mFilter;
	private int listIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		Log.v(TAG, "Creating MemberFindFragment");
		
		if (savedInstanceState != null) {
			input = savedInstanceState.getString("input");
			listIndex = savedInstanceState.getInt("index");
		}
	 	is_booking = false;
	 	Bundle b = null;
	 	try {
	 		b = getArguments();
	 		if (b.getBoolean(Services.Statics.IS_BOOKING)){
				is_booking = true;
			}
	 	} catch (Exception e) {
	 		is_booking = false;
	 	}
	 	
	 	membership = Services.getAppSettings(getActivity(), "filter_membership");
	 	if (membership.compareTo("-1")==0){
	 		membership = null;
	 	}
	 	gender = Services.getAppSettings(getActivity(), "filter_gender");
	 	if (gender.compareTo("-1")==0){
	 		gender = null;
	 	}
	 	owes = Services.getAppSettings(getActivity(), "filter_owes");
	 	if (owes.compareTo("-1")==0){
	 		owes = null;
	 	}
	 	programmeGroup = Services.getAppSettings(getActivity(), "filter_programmegroup");
	 	if (programmeGroup.compareTo("-1")==0){
	 		programmeGroup = null;
	 	}
	 	
		loadermanager = getLoaderManager();
		contentResolver = getActivity().getContentResolver();
		parent = getActivity();
	}
	
	 @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		  super.onCreateView(inflater, container, savedInstanceState);
		  System.out.print("\n\nCreating View\n");
		  //return super.onCreateView(inflater, container, savedInstanceState);
		 
		 
		  view = inflater.inflate(R.layout.member_find_fragment, container, false);
		  return view;

	  }
	 
	 @Override
	 public void onSaveInstanceState(Bundle savedInstanceState) {
		 super.onSaveInstanceState(savedInstanceState);
		 savedInstanceState.putInt("index", this.getSelectedItemPosition());
		 savedInstanceState.putString("input", input);
	}
	 
	 @Override
	 public void onResume(){
		 super.onResume();
		 Log.v(TAG, "Resuming MemberFindFragment");
		 loadermanager.restartLoader(0, null, this);
		 setupView();
	 }
	 
	private void setupFilter() {
		LinearLayout filterbutton = (LinearLayout) view.findViewById(R.id.member_find_color_block);
		filterbutton.setOnClickListener(this);
		
		ImageView filter_icon = (ImageView) filterbutton.findViewById(R.id.member_find_filter_drawable);
		filter_icon.setColorFilter(Services.ColorFilterGenerator.setColourGrey());
	}

	@SuppressLint("NewApi")
	private void setupView(){
		
		String[] from = new String[] {ContentDescriptor.Member.Cols.FNAME};
		int[] to = new int[] {R.id.name};
		
		EditText inputField = (EditText) view.findViewById(R.id.find);
		if (input != null) {
			inputField.setText(input);
		}
		inputField.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				updateCursor(s.toString());
			}
		});
		getActivity().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
		
		//Setup Filter
		setupFilter();
		
		if (is_booking) {
			mAdapter = new MembersFindAdapter(parent,R.layout.member_find_row, null, from, to, true, mCallback);
		} else {
			mAdapter = new MembersFindAdapter(parent,R.layout.member_find_row, null, from, to, false, mCallback);
		}
		setListAdapter(mAdapter);
		System.out.print("\n\nADAPTER SET: ");
		
		loadermanager.initLoader(0, null, this);
	}
	
	private void updateCursor(String s) {
		if (s.length() <= 0) {
			input = null;
		} else {
			input = s;
			//System.out.print("\n\nInput:"+input);
		}
		loadermanager.restartLoader(0, null, this);
	}
	 
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		if (input != null || owes != null || gender != null || membership != null || programmeGroup != null) {
			//setup the filter window:
			LinearLayout filter_message_box = (LinearLayout) view.findViewById(R.id.filter_message);
			filter_message_box.setVisibility(View.VISIBLE);

			TextView reset_filter = (TextView) view.findViewById(R.id.button_reset_filter);
			reset_filter.setOnClickListener(this);
			
			TextView filter_message = (TextView) view.findViewById(R.id.filter_details_text);
			String message = "Filters applied: ";

			
			String where = "";
			String[] whereArgs = null;
			if (input != null) {
				where = where +ContentDescriptor.Member.Cols.FNAME+"||' '||"+ContentDescriptor.Member.Cols.SNAME+" LIKE ? ";
				whereArgs = new String[] {"%"+input+"%"};
				message = message + "Name: <b>"+input+"</b>;  ";
			}
			if (owes != null) {
				if (!where.isEmpty()) where = where + "AND ";
				where = where + ContentDescriptor.MemberBalance.Cols.BALANCE+" NOT LIKE '-%' AND "
						+ ContentDescriptor.MemberBalance.Cols.BALANCE+" != '$0.00' ";
				message = message + "<b>Owes Money</b>;  ";
			}
			if (gender != null) {
				if (!where.isEmpty()) where = where + "AND ";
				where = where + ContentDescriptor.Member.Cols.GENDER+" LIKE '"+gender+"%' ";
				message = message + "Gender: <b>"+gender+"</b>;  ";
			}
			if (membership != null) {
				if (!where.isEmpty()) where = where + "AND ";
				where = where + ContentDescriptor.Member.Cols.MID+" IN (SELECT "+ContentDescriptor.Membership.Cols.MID
						+" FROM "+ContentDescriptor.Membership.NAME+" WHERE "+ContentDescriptor.Membership.Cols.PNAME
						+" = '"+membership+"')";
				message = message + "Membership: <b>"+membership+"</b>;  ";
			}
			if (programmeGroup != null) {
				if (!where.isEmpty()) where = where +" AND ";
				where = where + ContentDescriptor.Member.Cols.MID+" IN (SELECT "+ContentDescriptor.Membership.Cols.MID
						+" FROM "+ContentDescriptor.Membership.NAME+" ms LEFT JOIN "+ContentDescriptor.Programme.NAME+" p"
						+" ON (ms."+ContentDescriptor.Membership.Cols.PNAME+" = p."+ContentDescriptor.Programme.Cols.NAME
						+") WHERE "+ContentDescriptor.Programme.Cols.GNAME+" = '"+programmeGroup+"')";
				message = message +"Programme Group: <b>"+programmeGroup+"</b>;  ";
			}
			filter_message.setTextColor(getResources().getColor(R.color.text_green_shade));
			filter_message.setText(Html.fromHtml(message));
			/*return new CursorLoader( getActivity(), ContentDescriptor.Member.CONTENT_URI,
					null, where, whereArgs, null);*/
			if (mAdapter != null) {
				mAdapter.setSelectedPos(-1);
			}
			return new CursorLoader( getActivity(), ContentDescriptor.Member.URI_FIND,
					null, where, whereArgs, null);
		} else {
			LinearLayout filter_message_box = (LinearLayout) view.findViewById(R.id.filter_message);
			filter_message_box.setVisibility(View.GONE);

			return new CursorLoader( getActivity(), ContentDescriptor.Member.CONTENT_URI,
					null, null, null, null );
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		cur = cursor;
		if(cursor.isClosed()) {
           System.out.print("\n\nCursor Closed");
        }
		mAdapter.changeCursor(cursor);		
		mAdapter.notifyDataSetChanged();
		this.setSelection(listIndex);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//System.out.print("\n\nCursor RESET");
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}
	
	 public interface OnMemberSelectListener {
	        public void onMemberSelect(String id);
	 }
	 
	 @Override
	 public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        
	        // This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            mCallback = (OnMemberSelectListener) activity;
	        } catch (ClassCastException e) {
	            //mCallback not set
	        	//we can set it manually after.
	        }
	    }
	 
	 public void setMemberSelectListener(OnMemberSelectListener theListener) {
		 this.mCallback = theListener;
	 }
	 
	 private void showFilterWindow(){
		 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		 LayoutInflater inflater = getActivity().getLayoutInflater();
		 View v = inflater.inflate(R.layout.alert_find_filter, null);
		 
		 Spinner select_programmegroup = (Spinner) v.findViewById(R.id.programmegroup_spinner);
		 ArrayList<String> programmegroups = new ArrayList<String>();
		 Cursor cur = contentResolver.query(ContentDescriptor.Programme.GROUP_URI, null, null, null, null);
		 programmegroups.add(" ");
		 while (cur.moveToNext()) {
			 programmegroups.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.GNAME)));
		 }
		 cur.close();
		 
		 ArrayAdapter<String> pgAdapter = new ArrayAdapter<String>(getActivity(),
				 android.R.layout.simple_spinner_item, programmegroups);
		 pgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 select_programmegroup.setAdapter(pgAdapter);
		
		 Spinner select_membership = (Spinner) v.findViewById(R.id.membership_spinner);
		 ArrayList<String> memberships = new ArrayList<String>();
		 cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, null, null, null);
		 memberships.add(" ");
		 while (cur.moveToNext()) {
			 memberships.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)));
		 }
		 cur.close();
		 
		 ArrayAdapter<String> membershipsAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_spinner_item, memberships);
				membershipsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				select_membership.setAdapter(membershipsAdapter);
		 
				
		TextView applyFilter = (TextView) v.findViewById(R.id.button_apply_text);
		applyFilter.setOnClickListener(this);
		
		TextView cancel = (TextView) v.findViewById(R.id.button_cancel_text);
		cancel.setOnClickListener(this);
		
		 builder.setTitle("Filter Members")
		 	.setView(v);
		 mFilter = builder.create();
		 mFilter.show();
	 }
	 
	private void getFilters(){
		Spinner selectedMembership = (Spinner) mFilter.findViewById(R.id.membership_spinner);
		int selectedPos = selectedMembership.getSelectedItemPosition();
		if (selectedPos > 0) {
			membership = selectedMembership.getItemAtPosition(selectedPos).toString();
			Services.setPreference(getActivity(), "filter_membership", membership);
			Log.v(TAG, membership);
		} else {
			membership = null;
			Services.setPreference(getActivity(), "filter_membership", "-1");
		}
		
		Spinner selectedProgrammeGroup = (Spinner) mFilter.findViewById(R.id.programmegroup_spinner);
		selectedPos = selectedProgrammeGroup.getSelectedItemPosition();
		if (selectedPos > 0) {
			programmeGroup = selectedProgrammeGroup.getItemAtPosition(selectedPos).toString();
			Services.setPreference(getActivity(), "filter_programmegroup", programmeGroup);
			Log.v(TAG, programmeGroup);
		} else {
			programmeGroup = null;
			Services.setPreference(getActivity(), "filter_programmegroup", "-1");
		}
		
		ToggleButton show_owe = (ToggleButton) mFilter.findViewById(R.id.filter_owing);
		if (show_owe.isChecked()) {
			owes = "1";
			Services.setPreference(getActivity(), "filter_owes", "1");
		} else {
			owes = null;
			Services.setPreference(getActivity(), "filter_owes", "-1");
		};
		//gender ?
		RadioGroup getGender = (RadioGroup) mFilter.findViewById(R.id.gender_group);
		if (getGender.getCheckedRadioButtonId() > 0) {
			if (getGender.getCheckedRadioButtonId() == R.id.gender_male) {
				gender = "M";
				Services.setPreference(getActivity(), "filter_gender", gender);
			} else if (getGender.getCheckedRadioButtonId() == R.id.gender_female) {
				gender = "F";
				Services.setPreference(getActivity(), "filter_gender", gender);
			}
		} else {
			gender = null;
			Services.setPreference(getActivity(), "filter_gender", "-1");
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case (R.id.member_find_color_block):{
			//show pop-up.
			showFilterWindow();
			break;
		}
		case (R.id.button_cancel_text):{
			if (mFilter != null && mFilter.isShowing()) {
				mFilter.dismiss();
			}
			break;
		}
		case (R.id.button_apply_text):{
			//get current filter setup, apply.
			getFilters();
			mFilter.dismiss();
			loadermanager.restartLoader(0, null, this);
			break;
		}
		case (R.id.button_reset_filter):{
			owes = null;
			Services.setPreference(getActivity(), "filter_owes", "-1");
			membership = null;
			Services.setPreference(getActivity(), "filter_membership", "-1");
			gender = null;
			Services.setPreference(getActivity(), "filter_gender", "-1");
			programmeGroup = null;
			Services.setPreference(getActivity(), "filter_programmegroup", "-1");
			EditText inputField = (EditText) view.findViewById(R.id.find);
			inputField.setText("");
			input = null;

			loadermanager.restartLoader(0, null, this);
			break;
		}
		}
	}
	
}
