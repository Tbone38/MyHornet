package com.treshna.hornet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberFindFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	Cursor cur;
	ContentResolver contentResolver;
	MemberFindAdapter mAdapter;
	LoaderManager loadermanager;
	Context parent;
	View view;
	private String input;
	private boolean is_booking;
	private OnMemberSelectListener mCallback;
	private static final String TAG = "MemberFindFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		Log.v(TAG, "Creating MemberFindFragment");
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
	 public void onResume(){
		 super.onResume();
		 Log.v(TAG, "Resuming MemberFindFragment");
		 setupView();
	 }
	 

	@SuppressLint("NewApi")
	private void setupView(){
		
		String[] from = new String[] {ContentDescriptor.Member.Cols.FNAME};
		int[] to = new int[] {R.id.name};
		
		EditText inputField = (EditText) view.findViewById(R.id.find);
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
		
		// TODO 	- Sort up the member & membership join 		- DONE
		//			- handle Bookings differently				- DONE
		//			-
		if (is_booking) {
			mAdapter = new MemberFindAdapter(parent,R.layout.member_find_row, null, from, to, true, mCallback);
		} else {
			mAdapter = new MemberFindAdapter(parent,R.layout.member_find_row, null, from, to, false, mCallback);
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
		
		if (input != null) {
			return new CursorLoader( getActivity(), ContentDescriptor.Member.CONTENT_URI,
					null, ContentDescriptor.Member.Cols.FNAME+"||' '||"+ContentDescriptor.Member.Cols.SNAME+" LIKE ?"
					, new String[] {"%"+input+"%"}, null); 
		} else {
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
	        }
	    }
	 
	 public void setMemberSelectListener(OnMemberSelectListener theListener) {
		 this.mCallback = theListener;
	 }
	
}
