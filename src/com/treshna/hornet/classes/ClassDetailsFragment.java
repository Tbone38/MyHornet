package com.treshna.hornet.classes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R;
import com.treshna.hornet.member.MemberAddFragment;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class ClassDetailsFragment extends ListFragment implements TagFoundListener, LoaderManager.LoaderCallbacks<Cursor>,
		OnClickListener {
	
	private Cursor cur;
	private ContentResolver contentResolver;
	private String bookingID;
	private String classId;
	private Context ctx;
	ClassMemberListAdapter mAdapter;
	LoaderManager mLoaderManager;
	private View thePage;
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	        @Override //when the server sync finishes, it sends out a broadcast.
	        public void onReceive(Context context, Intent intent) {
	        	//System.out.println("*INTENT RECIEVED*");
	        	
	           ClassDetailsFragment.this.receivedBroadcast(intent);
	        }
	    };
	private static final String TAG = "com.treshna.hornet.ClassDetailsFragment";

	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 bookingID = getArguments().getString(Services.Statics.KEY);
		 mLoaderManager = getLoaderManager();
		 System.out.print("\n\nBOOKINGID:"+bookingID+"\n\n");
		 contentResolver = getActivity().getContentResolver();
		 View page =inflater.inflate(R.layout.fragment_class_details, container, false); 
		 setupView(page);
		 thePage = page;
		 return page;
	 }
	 
	 
	 @Override
	 public void onResume(){
		 super.onResume();
		 
		 IntentFilter iff = new IntentFilter();
		 iff.addAction("com.treshna.hornet.serviceBroadcast");
		 getActivity().registerReceiver(this.mBroadcastReceiver,iff);
	 }
	 
	 
	 @Override
	 public void onPause(){
		 super.onPause();
		 
		 getActivity().unregisterReceiver(this.mBroadcastReceiver);
	 }
	
	 
	protected void receivedBroadcast(Intent intent) {
		String recieved_cid;
		
		recieved_cid = intent.getStringExtra(Services.Statics.IS_CLASSSWIPE);
		
		if (recieved_cid != null && recieved_cid.compareTo(bookingID) == 0) {
			//the broadcast was for a class-swipe, and the class id matches this class.
			//refresh the cursor!
			mLoaderManager.restartLoader(0, null, this);
		}
		
	}


	private void setupView(View page) {
		String date, stime, etime, classname;
		TextView dateView, stimeView, etimeView, addmemberButton;
		AutoCompleteTextView addmember;
		ArrayAdapter<String> mArrayAdapter;
		ArrayList<String> membernames;
		String[] from = {};
		int[] to = {};
		
		
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" = ?", 
				new String[] {bookingID}, null);
		cur.moveToFirst();
		
		classname = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME));
		date = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
		stime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
		etime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME));
		classId = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CLASSID));
		
		cur.close();
		getActivity().setTitle(classname);
		
		dateView = (TextView) page.findViewById(R.id.classDate);
		dateView.setText(Services.DateToString(new Date(Long.parseLong(date)))+", ");
		//dateView.setText(date);
		
		stimeView = (TextView) page.findViewById(R.id.classSTime);
		stimeView.setText(stime+" ");
		
		etimeView = (TextView) page.findViewById(R.id.classETime);
		etimeView.setText("- "+etime);
		
		//find-member
		addmember = (AutoCompleteTextView) page.findViewById(R.id.classAddMember);
		cur = contentResolver.query(ContentDescriptor.Member.URI_INCLUDE, new String[] {ContentDescriptor.Member.Cols.FNAME,
				ContentDescriptor.Member.Cols.SNAME}, null, null, null);
		//above cursor includes expired members.
		membernames = new ArrayList<String>();
		while (cur.moveToNext()) {
			membernames.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))+" "
					+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		}
		mArrayAdapter = new ArrayAdapter<String>(ctx,
                android.R.layout.simple_dropdown_item_1line, membernames);
		addmember.setAdapter(mArrayAdapter);
		
		addmemberButton = (TextView) page.findViewById(R.id.classAddMemberB);
		addmemberButton.setClickable(true);
		addmemberButton.setOnClickListener(this);
		
		//for populating the list
		mAdapter = new ClassMemberListAdapter(ctx, R.layout.row_class_details, null, from, to);
		setListAdapter(mAdapter);
		mLoaderManager.restartLoader(0, null, this);
	}


	@Override
	public boolean onNewTag(String serial) {
		/* we need to look up the serial in the database,
		 * find the member associated with it (if there is one),
		 * and then either: Add said member to the list
		 * 				or Check the box for the member if they're already in the list.
		 */
		int maxStudents = 0, curStudents, online = 0;
		ContentResolver contentResolver;
		contentResolver = getActivity().getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Class.CONTENT_URI, null, ContentDescriptor.Class.Cols.CID+" = ?",
				new String[] {classId}, null);
		if (cur.moveToFirst()) {
			maxStudents = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols.MAX_ST));
			online = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols.ONLINE));
		}
		cur.close();
		//how many students are currently signed up?
		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.PARENTID+" = ?",
				new String[] {bookingID}, null);
		curStudents = cur.getCount();
		
		if (curStudents >= maxStudents || online == 0) {
			Log.e(TAG, "Current Students:"+curStudents);
			Log.e(TAG, "MAX STUDENTS: "+maxStudents);
			Log.e(TAG, "Online:"+online);
			//can't add members, we're already full (or online booking's are set to false for this class).
			Toast.makeText(getActivity(), "The class has already reached it's student limit!", Toast.LENGTH_LONG).show();
			return true;
		}
		
		Log.v(TAG, "TAG DISCOVERY, SERIAL: "+serial);
		
		ContentValues values;
		String door;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);

		door = "-"+bookingID;
		
		
		values = new ContentValues();
		values.put(ContentDescriptor.Swipe.Cols.ID, serial);
		values.put(ContentDescriptor.Swipe.Cols.DOOR, door);
		values.put(ContentDescriptor.Swipe.Cols.DATETIME, format.format(new Date()));
		
		contentResolver.insert(ContentDescriptor.Swipe.CONTENT_URI, values);
		
		Intent updateInt = new Intent(getActivity(), HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.CLASSSWIPE);
	 	getActivity().startService(updateInt);
	 	return true;
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (getActivity() != null) {
			if (thePage != null) {
				AutoCompleteTextView addmember = (AutoCompleteTextView) thePage.findViewById(R.id.classAddMember);
				addmember.setText("");;
			}
			return new CursorLoader(getActivity(), ContentDescriptor.Booking.CONTENT_URI, null, 
					ContentDescriptor.Booking.Cols.PARENTID + " = ? AND "+ContentDescriptor.Booking.Cols.RESULT+" != 5", new String[] {bookingID}, null);
		}
		return null;
	}

	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		
		if(cursor.isClosed()) {
	           System.out.print("\n\nCursor Closed");
	            Activity activity = getActivity();
	            if(activity!=null) {
	            	//mAdapter.notifyDataSetChanged();
	            	mLoaderManager.restartLoader(0, null, this);
	            }
	            return;
	        }
			mAdapter.changeCursor(cursor);		
			mAdapter.notifyDataSetChanged();
	}

	
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
		case (R.id.classAddMemberB):{
			// get the name from the edit-text, look up member,
			// add member(ship?) to the list.
			int maxStudents = 0, curStudents, online = 0;
			String multibook = null;
			ContentResolver contentResolver;
			contentResolver = getActivity().getContentResolver();
			Cursor cur = contentResolver.query(ContentDescriptor.Class.CONTENT_URI, null, ContentDescriptor.Class.Cols.CID+" = ?",
					new String[] {classId}, null);
			if (cur.moveToFirst()) {
				maxStudents = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols.MAX_ST));
				online = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols.ONLINE));
				multibook = cur.getString(cur.getColumnIndex(ContentDescriptor.Class.Cols.MULTIBOOK));
			}
			cur.close();
			//how many students are currently signed up?
			cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.PARENTID+" = ? AND "
					+ContentDescriptor.Booking.Cols.RESULT+" != 5",
					new String[] {bookingID}, null); //&& STATUS >= 10 || status == NULL ?
			curStudents = cur.getCount();
			
			if (curStudents >= maxStudents || online == 0) {
				//can't add members, we're already full (or online booking's are set to false for this class).
				Toast.makeText(getActivity(), "The class has already reached it's student limit!", Toast.LENGTH_LONG).show();
				return;
			}
			
			AutoCompleteTextView findmember;
			findmember = (AutoCompleteTextView) ((View) view.getParent()).findViewById(R.id.classAddMember);
			
			String membername, firstname, lastname;
			membername = findmember.getEditableText().toString();
			
			if (membername.compareTo("")==0 || membername.compareTo(" ")==0) {
				return;
			}
			
			contentResolver = getActivity().getContentResolver();
			
			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.FNAME+"||' '||"+ContentDescriptor.Member.Cols.SNAME+" LIKE ?"
					, new String[] {"%"+membername+"%"}, null);
			if (cur.getCount() <= 0) {
				//not sure what happened.
				Log.e(TAG, "no members found by that name");
				//prompt to add Member.
				alertAddMember(membername);
				return;
				//
			} else if (cur.getCount() >= 2) {
				// 2 or more members returned with that name, what should I do?
				Log.e(TAG, "2 or more members found with that name");
			}
			String memberid;
			if (cur.moveToFirst()) {
				//check the multibook stuff here.
				firstname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME));
				lastname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME));
				memberid = cur.getString(1); //changing this will break things!
				cur.close();

				showAlert(memberid, firstname, lastname);
			}
			
			break;
		}
		}
	}
	
	public void alertAddMember(final String membername) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Add Member?")
		.setMessage("Member not found in database, do you want to add them?")
		.setNegativeButton("Cancel", null)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Fragment f = new MemberAddFragment(); //we're probably going to need to add something to tell the app to come back here after.
				Bundle bdl = new Bundle(2);
				bdl.putString(Services.Statics.MID, membername);
				bdl.putBoolean("class", true); //doesn't matter what we put in there, we're only checking if its there at all.
				f.setArguments(bdl);
				((MainActivity) getActivity()).changeFragment(f, "AddMember");
			}})
		.show();
	}
		
	public void showAlert(final String memberid, final String firstname, final String surname) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		final RadioGroup rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ? AND "
				+ContentDescriptor.Membership.Cols.HISTORY+" = 'f'", new String[] {memberid}, null);
		if (cur.getCount() == 1) {
			cur.moveToFirst();
			String membershipid = cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID));
			cur.close();
			this.startTransaction(membershipid, memberid, firstname, surname);
			return;
		}
		
		while (cur.moveToNext()) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)) != null 
					&& cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)).compareTo("") !=0) {
				RadioButton rb = new RadioButton(getActivity());
				rb.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
				rb.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
				rg.addView(rb);
			}
		}	
		cur.close();
        builder.setView(layout);
        builder.setTitle("Select Membership for Class");
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            	String selectedMSID = null;
            	if (rg.getChildCount()<=0) {
            		//no memberships found for this member, ignore and continue?
            		selectedMSID = null;
            		//show an error that no memberships were found.
            		//Toast.makeText(getActivity(), "Cannot add Booking without membership", Toast.LENGTH_LONG).show();
            		dialog.dismiss();
            	} else if (rg.getChildCount() > 0) {

	            	int cid = rg.getCheckedRadioButtonId();     	
	            	RadioButton rb = (RadioButton) rg.findViewById(cid);
	            	selectedMSID = (String) rb.getTag();
            	}
	            	
	            	startTransaction(selectedMSID, memberid, firstname, surname);
            	}
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}
	
	
	/**
	 * TODO: get the following values:
	 * 	- Resourceid				- DONE
	 * 	- Firstname & lastname		- DONE & DONE
	 *  - startid & endid			- DONE
	 *  - arrival					- DONE
	 *  - parentid					- DONE
	 *  - bookingid					- DONE
	 *  - stime & etime				- DONE
	 *  - Result					- DONE
	 *  - checkin					- NOT NEEDED ?
	 *  - BookingType!!!			- DONE
	 *  - Offset					- DONE
	 *  - MemberID					- DONE
	 *  - MembershipID				- DONE
	 */			
	public void startTransaction(String membership, String memberid, String firstname, String surname) {
		// get all the other variables, and insert them into SQLite
		String resourceid, startid, stime, endid, etime, arrival, offset;
		int bookingid;
		
		if (firstname == null || surname == null) {	
			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, new String[] {ContentDescriptor.Member.Cols.FNAME,
					ContentDescriptor.Member.Cols.SNAME}, ContentDescriptor.Member.Cols.MID+" = ?",
					new String[] {memberid}, null);
			if (cur.moveToFirst()) {
				firstname = cur.getString(0);
				surname = cur.getString(1);
			}
			cur.close();
		}
		
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" = ?",
				new String[] {bookingID}, null);
		
		if (!cur.moveToFirst()) {
			//no rows, complain!
			Log.e(TAG, "Class not found in SQLite, something must be super-broken.");
		}
		
		resourceid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RID));
		startid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIMEID));
		stime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
		endid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIMEID));
		etime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME));
		arrival = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
		offset = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET));
		
		cur.close();
		
		//bookingid as well.
		cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())}, null);
		if (cur.moveToFirst()) {
			bookingid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
		} else {
			//we haven't got any spare booking-id's. what should I do?
			bookingid = -1;
		}
		cur.close();
		
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Booking.Cols.MID, memberid);
		if (membership != null) {
			values.put(ContentDescriptor.Booking.Cols.MSID, membership);
		}
		values.put(ContentDescriptor.Booking.Cols.RID, resourceid);
		values.put(ContentDescriptor.Booking.Cols.STIMEID, startid);
		values.put(ContentDescriptor.Booking.Cols.STIME, stime);
		values.put(ContentDescriptor.Booking.Cols.ETIMEID, endid);
		values.put(ContentDescriptor.Booking.Cols.ETIME, etime);
		values.put(ContentDescriptor.Booking.Cols.ARRIVAL, arrival);
		values.put(ContentDescriptor.Booking.Cols.OFFSET, offset);
		values.put(ContentDescriptor.Booking.Cols.BID, bookingid);
		values.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, 0); //class attendant = 0, probably shouldn't be hardcoded.
		values.put(ContentDescriptor.Booking.Cols.RESULT, 10);
		values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
		values.put(ContentDescriptor.Booking.Cols.PARENTID, bookingID);
		values.put(ContentDescriptor.Booking.Cols.FNAME, firstname);
		values.put(ContentDescriptor.Booking.Cols.SNAME, surname);
		values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "t");
		//DON'T ADD CLASSID FOR CLASS MEMBERS. it'll break the the upload!
		
		contentResolver.insert(ContentDescriptor.Booking.CONTENT_URI, values);
		
		if (bookingid > 0) { //we've used the free id, remove it from our list.
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(bookingid), 
					String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())});
		}
		//refresh the view.
		mLoaderManager.restartLoader(0, null, this);
	}
}
