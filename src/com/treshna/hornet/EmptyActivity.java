package com.treshna.hornet;

import java.util.ArrayList;

import com.treshna.hornet.BookingPage.TagFoundListener;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class EmptyActivity extends NFCActivity{
	FragmentManager frm;
	//NfcAdapter mNfcAdapter;
	private String[][] mTechLists;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	private TagFoundListener tagFoundListener;
	private int view;
	private static final String TAG = "EmptyActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//check bundle for value. create fragment depending on the value called.
		Services.setContext(this);
		this.setContentView(R.layout.empty_activity);
		frm = getSupportFragmentManager();
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		view = intent.getIntExtra(Services.Statics.KEY, -1);
		
		Bundle bdl = null;
		if (view == Services.Statics.FragmentType.MembershipAdd.getKey()) {
			String memberid = intent.getStringExtra(Services.Statics.MID);
			bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberid);

		} else if (view == Services.Statics.FragmentType.MemberDetails.getKey()) {
			ArrayList<String> tag = intent.getStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID);
			
			bdl = new Bundle(2);
			bdl.putString(Services.Statics.MID, tag.get(0));
		    bdl.putString(Services.Statics.KEY, tag.get(1));
		}
		
		setFragment(view, bdl);
	}
	
	public void setFragment(int theView, Bundle bdl) {
		this.view = theView;
		
		tagFoundListener = null;
		FragmentTransaction ft = frm.beginTransaction();
		if (view == Services.Statics.FragmentType.MembershipAdd.getKey()) {
			Fragment f = new MembershipAdd();
			f.setArguments(bdl);
			ft.replace(R.id.empty_layout, f);
			
		} else if (view == Services.Statics.FragmentType.MembershipComplete.getKey()) {
			Fragment f = new MembershipComplete();
			f.setArguments(bdl);
			tagFoundListener = (TagFoundListener) f;
			ft.replace(R.id.empty_layout, f);
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
				
				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				mTechLists = new String[][] { new String[] {NfcA.class.getName()}};
				IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
			    try {
			        tag.addDataType("*/*");
			    } catch (MalformedMimeTypeException e) {
			    	//ignore, it's probably not critical.
			    }
			    intentFiltersArray = new IntentFilter[] {tag};
			}
		
		} else if (view == Services.Statics.FragmentType.MemberDetails.getKey()) {
			Fragment f = new MemberDetailsFragment();
			f.setArguments(bdl);
			ft.replace(R.id.empty_layout, f);
		}
		
		else { //default!

		}
		ft.commit();
		
	}
	
	public void setTitle(String title) {
		super.setTitle(title);
	}
	
	@Override
	public void onNewIntent(Intent i) {
		String id;
		Tag card;
		
		card = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		id = this.getID(card);
		
		Log.v(TAG, "\n\n\ncard.serial:"+id);
		if (tagFoundListener != null) {
			tagFoundListener.onNewTag(id);
		} else {
			super.onNewIntent(i);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			if (view == Services.Statics.FragmentType.MembershipAdd.getKey()) {
				NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
				if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
			}
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (view == Services.Statics.FragmentType.MembershipComplete.getKey()) {
			if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction())){
				this.onNewIntent(this.getIntent());
			}
			NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, mTechLists);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.not_main, menu);
		return true;
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
		 	PollingHandler polling = Services.getPollingHandler();
	    	polling.startService();
	    	return true;
	    }
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    }
	    case (R.id.action_addMember):{
	    	Intent intent = new Intent(this, MemberAdd.class);
	    	startActivity(intent);
	    	return true;
	    }	    
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
}
