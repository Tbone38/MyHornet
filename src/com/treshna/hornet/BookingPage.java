package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Locale;

import com.treshna.hornet.MembersFindFragment.OnMemberSelectListener;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class BookingPage extends ActionBarActivity implements OnMemberSelectListener{
	
	private String bookingID;
	private String starttime;
	private String selectedID;
	private String selectedMS;
	private String selectedMSID;
	
	FragmentManager frm;
	RadioGroup rg;
	ContentResolver contentResolver;
	
	private String[][] mTechLists;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	private static final String TAG = "com.treshna.hornet.BookingPage";
	private TagFoundListener tagFoundListener;
	int classid = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		Services.setContext(this);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		ArrayList<String> tagInfo = intent.getStringArrayListExtra(Services.Statics.KEY);
		bookingID = tagInfo.get(0);
		
		contentResolver = getContentResolver();
		frm = getSupportFragmentManager();
		FragmentTransaction ft = frm.beginTransaction();
	
		setContentView(R.layout.empty_activity);
		if (savedInstanceState == null) {
			if (Integer.parseInt(bookingID) > 0) {
				Cursor cur;
				
				bookingID = tagInfo.get(1);
				
				cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI,null, 
						ContentDescriptor.Booking.Cols.BID+" = "+bookingID, null, null);
				if (cur.moveToFirst()) {
					classid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CLASSID));
					System.out.print("\n\n** CLASS ID:"+classid+" **\n\n");
				}
				
				if (classid > 0) {
					//it's a class, show the class-booking page instead.
					ClassDetailsFragment f;
					Bundle bdl;
					
					if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
						
						pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
						mTechLists = new String[][] { new String[] {NfcA.class.getName()}};
						IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
					    try {
					        tag.addDataType("*/*");
					    } catch (MalformedMimeTypeException e) {
					        throw new RuntimeException("Tag mime type fail", e);
					    }
					    intentFiltersArray = new IntentFilter[] {tag};
					}
					
					f = new ClassDetailsFragment();
					tagFoundListener = (TagFoundListener) f;
					bdl = new Bundle(1);
					bdl.putString(Services.Statics.KEY, bookingID);
					f.setArguments(bdl);
					ft.add(R.id.empty_layout, f);
				} else {
					bookingID = tagInfo.get(1);
					BookingDetailsFragment f = new BookingDetailsFragment();
					Bundle bdl = new Bundle(1);
		            bdl.putString(Services.Statics.KEY, bookingID);
		            f.setArguments(bdl);
					ft.add(R.id.empty_layout, f);
				}
			} else {
				//add Booking
				BookingAddFragment f = new BookingAddFragment();
				starttime = tagInfo.get(1);
				Bundle bdl = new Bundle(2);
	            bdl.putString(Services.Statics.KEY, starttime);
	            bdl.putString(Services.Statics.DATE, tagInfo.get(2));
	            f.setArguments(bdl);
				ft.add(R.id.empty_layout,f, "AddBooking");
				ft.addToBackStack(null);
			}
			ft.commit();
		}
	}
	
	@Override
	public void onBackPressed() {
		Fragment f = frm.findFragmentByTag("AddBooking");
		if (f != null && f.isVisible()) { //hack to remove empty fragment on back press (from add Booking).
			super.onBackPressed();
		}
		super.onBackPressed();
	}

	@Override
	public void onPause(){
		super.onPause();
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			if (classid > 0) {
				NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
				if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
			}
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (classid > 0) {
			if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction())){
				this.onNewIntent(this.getIntent());
			}
			NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, mTechLists);
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return Services.createOptionsMenu(getMenuInflater(), menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
	    case (R.id.action_home):{
	    	Intent i = new Intent (this, MainActivity.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_createclass):{
	    	Intent i = new Intent(this, ClassCreate.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_settings):
	    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	return true;
	    case (R.id.action_update): {
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
		 		Services.setPreference(this, "sync_frequency", "5");
		 	}
		 	PollingHandler polling = Services.getFreqPollingHandler();
	    	polling.startService();
	    	return true;
	    }
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getFreqPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    }
	    /*case (R.id.action_bookings):{
	    	Intent bookings = new Intent(this, HornetDBService.class);
			bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
		 	this.startService(bookings);
	    	
		 	Intent intent = new Intent(this, BookingsSlidePager.class);
	       	startActivity(intent);
	       	return true;
	    }*/
	    case (R.id.action_addMember):{
	    	Intent intent = new Intent(this, MemberAdd.class);
	    	startActivity(intent);
	    	return true;
	    }
	    case (R.id.action_kpi):{
	    	Intent i = new Intent(this, EmptyActivity.class);
	    	i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.KPIs.getKey());
	    	startActivity(i);
	    	return true;
	    }
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}


	@Override
	public void onMemberSelect(String id) {
		//TODO: create an alert dialog with member's memberships in it.
		selectedID = id;
		Cursor cur;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ?",
				new String[] {id}, null);
		
		while (cur.moveToNext()) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)) != null 
					&& cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)).compareTo("") !=0) {
				RadioButton rb = new RadioButton(this);
				rb.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
				rb.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
				rg.addView(rb);
			}
		}	
		cur.close();
        builder.setView(layout);
        builder.setTitle("Select Membership for Booking");
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	
            	if (rg.getChildCount()<=0) {
            		//no memberships found for this member, ignore and continue?
            		selectedMSID = null;
            	} else if (rg.getChildCount() > 0) {
	            	
	            	int cid = rg.getCheckedRadioButtonId();  
	            	if (cid == -1) {
	            		selectedMSID = null;
	            	} else {
		            	RadioButton rb = (RadioButton) rg.findViewById(cid);
		            	selectedMS = (String) rb.getText();
		            	selectedMSID = (String) rb.getTag();
		            	System.out.print("\n\nSelected Membership:"+selectedMS+" with ID:"+selectedMSID);
	            	}
            	}
	            /** Rabbit Hole: fix this by changing the member name handling as well.
	             * 
	             */
            	Cursor cur;
            	cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.MID+" = ?", 
        				new String[] {selectedID}, null);
        		if (cur.getCount() <= 0) {
        			// what should I do?
        		}
        		String fname = null;
        		String sname = null;
        		cur.moveToFirst();
        		fname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME));
        		sname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME));
        		
        		frm = getSupportFragmentManager();
        		BookingAddFragment f = (BookingAddFragment)frm.findFragmentByTag("AddBooking");
        		f.setName(fname, sname);
        		f.setMembership(selectedMSID);
        		/*FragmentTransaction ft = frm.beginTransaction();
        		Bundle bdl = new Bundle(4);
        		//System.out.print("\n\nSTART ID:"+starttime);
                bdl.putString(Services.Statics.KEY, starttime);
                bdl.putString(Services.Statics.IS_BOOKING_F, fname);
                bdl.putString(Services.Statics.IS_BOOKING_S, sname);
                bdl.putString(Services.Statics.MSID, selectedMSID);
                f.setArguments(bdl);
                ft.replace(R.id.booking_frame, f);
        		ft.commit();*/
        		frm.popBackStackImmediate();
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
	public final String getID(Tag tag){
    	StringBuilder sb = new StringBuilder();
       	for (byte b : tag.getId()) {
       		sb.append(String.format("%02X", b));
       	}
       	System.out.println("**TAG ID: "+sb.toString());
       	String cardID = null;
       	if(tag.getId().length == 4) {
       		String temp = sb.toString().substring(0, sb.toString().length() - 2).toLowerCase(Locale.US);
    	   	cardID = "Mx"+temp;
       	} else if(tag.getId().length == 7){
       		String temp = sb.toString().toLowerCase(Locale.US);
       		cardID = "Mv"+temp;
       	}
       	System.out.println(cardID);
    	return cardID;
    }
	
	@Override
	protected void onNewIntent(Intent i) {
		String id;
		Tag card;
		
		card = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		id = getID(card);
		
		Log.v(TAG, "\n\n\ncard.serial:"+id);
		tagFoundListener.onNewTag(id);
	}
	
	public interface TagFoundListener {
	    public void onNewTag(String serial);

	}
}
