package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class NFCActivity extends ActionBarActivity {
	private static final String TAG = "NFCActivity";
	//NfcAdapter mNfcAdapter;
	private String[][] mTechLists;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			
			pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			mTechLists = new String[][] { new String[] {NfcA.class.getName()}};
			IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		    try {
		        tag.addDataType("*/*");
		    } catch (MalformedMimeTypeException e) {
		        throw new RuntimeException("fail", e);
		    }
		    intentFiltersArray = new IntentFilter[] {tag};
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Services.setContext(this);
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())){
			onNewIntent(getIntent());
		}
			NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, mTechLists);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	public final String getID(Tag tag){
    	StringBuilder sb = new StringBuilder();
       	for (byte b : tag.getId()) {
       		sb.append(String.format(Locale.US,"%02X", b));
       	}
       	//System.out.println("**TAG ID: "+sb.toString());
       	String cardID = null;
       	if(tag.getId().length == 4) {
       		String temp = sb.toString().substring(0, sb.toString().length() - 2).toLowerCase(Locale.US);
    	   	cardID = "Mx"+temp;
       	} else if(tag.getId().length == 7){
       		String temp = sb.toString().toLowerCase(Locale.US);
       		cardID = "Mv"+temp;
       	}
       	//System.out.println(cardID);
       	Log.v(TAG, "Got tag with Serial: "+cardID);
    	return cardID;
    }
	
	/** A tag has been discovered */
    @Override 
    public void onNewIntent(Intent intent){
    	if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
    		int rowid; 
    		Tag tag;
    		String id;
    		String[] techlist;
    		ContentResolver contentResolver;
    		Date today;
    		SimpleDateFormat format;
    		ContentValues values;
    		
	    	//System.out.print("intent Started");
    		Log.v(TAG, "NFC Intent Started.");
	           	// get the tag object for the discovered tag
	    	tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    	techlist = tag.getTechList();
	    	
	    	Log.v(TAG, "Detected Tag's techlist:");
	    	for (int i=0;i<techlist.length; i+=1){
	    		//System.out.print("\n\n"+techlist[i]);
	    		Log.v(TAG, techlist[i]);
	    	}
	    	
	    	id = getID(tag);
	    	contentResolver = getContentResolver();
	    	today = new Date();
	    	format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);       
	       
	    	values = new ContentValues();
	    	values.put(ContentDescriptor.Swipe.Cols.ID, id);
	    	int door = Integer.parseInt(Services.getAppSettings(this, "door"));
	    	door =(door < 0)? 1: door; 
	    	values.put(ContentDescriptor.Swipe.Cols.DOOR, door);
	    	values.put(ContentDescriptor.Swipe.Cols.DATETIME, format.format(today));
	    	rowid = Integer.parseInt(contentResolver.insert(ContentDescriptor.Swipe.CONTENT_URI, values).getLastPathSegment());

	    	values = new ContentValues();
	    	values.put(ContentDescriptor.PendingUploads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Swipe.getKey());
	    	values.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);
	    	contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
	    	//start sync here as well

			Intent updateInt = new Intent(this, HornetDBService.class);
			updateInt.putExtra(Services.Statics.KEY, Services.Statics.SWIPE);
			this.startService(updateInt);
	   		
    	}
    }
}
