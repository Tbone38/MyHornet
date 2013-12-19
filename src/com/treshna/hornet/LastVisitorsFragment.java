package com.treshna.hornet;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/*
 * 
 */
public class LastVisitorsFragment extends ListFragment {
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
            LastVisitorsFragment.this.receivedBroadcast(intent);
        }
    };
	
	private static ContentResolver contentResolver = null;
    private static Cursor cur = null;
    private SimpleCursorAdapter mAdapter;
	private static int currentDisplay = -1; //make this private.
    
	public static final int LASTVISITORS = 1;
	static public String PREF_NAME = "addMember";
	static public String PREF_KEY = "memberType";
	private static final String TAG = "LastVisitorsFragment";
	//public static final int DISPLAYERROR = -1;
	
	//NFCs
	private static Context context;	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Services.setContext(getActivity());
        Log.v(TAG, "Creating Last Visitors");
        context = getActivity().getApplicationContext();
        contentResolver = getActivity().getContentResolver();
        if (cur != null) cur.close();
        
        /**the below code needs to run on app start.
         */
        Intent updateInt = new Intent(getActivity(), HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
		PendingIntent pintent = PendingIntent.getService(getActivity(), 0, updateInt, PendingIntent.FLAG_UPDATE_CURRENT);
		//polling = new PollingHandler(this, pintent);
		Services.setPollingHandler(getActivity(), pintent);
		startReciever();
		/************************************/
        //different views look different, build the views differently?
        //getList();
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.visitor_list, container, false);
        return view;
    }
	
	public int getCurrentDisplay(){
		return currentDisplay;
	}
	
	public static Context getContext(){
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 * This is closing the cursor, it means when the application is returned to:
	 * no results will be displayed until a new cursor is opened.
	 */
	 public void onStop() {
		 super.onStop();
	     if(cur != null) cur.close();
	     try {
		    	getActivity().unregisterReceiver(Services.getPollingHandler()); //?
		     } catch (Exception e) {
		    	 //doesn't matter.
		     }
	 }
	 
	 @Override
	 public void onStart() {
	     super.onStart(); 
	 }
	 
	 public void onResume() {
		 super.onResume();
		 Services.setContext(getActivity());
		 Log.v(TAG, "Resuming Last-Visitors");
		 IntentFilter iff = new IntentFilter();
	     iff.addAction("com.treshna.hornet.serviceBroadcast");
	     getActivity().registerReceiver(this.mBroadcastReceiver,iff);
		 getList();
	 }
	 
	 @Override
	 public void onPause(){
		 super.onPause();
		
		 getActivity().unregisterReceiver(this.mBroadcastReceiver);
	 }
	
	 private void getList(){
		 mAdapter = null;
	
		 System.out.println("Displaying Last Visitors"); //Display Last Visitors
       	
       	 getActivity().setTitle("Last Visitors");
       	 try {
       		 cur.close();
       	 } catch (Exception e) {
       		 cur = null;
       	 }
       	 cur = contentResolver.query(ContentDescriptor.Visitor.VISITOR_JOIN_MEMBER_URI, null, null, null, ContentDescriptor.Visitor.Cols.DATETIME+" DESC limit 100");
		 String[] from = {};
		 int[] to = {};
		 mAdapter = new VisitorsViewAdapter(getActivity(), R.layout.visitor_row, cur, from, to);
		 setListAdapter(mAdapter);
		 ListView listView = getListView();
		 listView.setTextFilterEnabled(true);
		 //TEXTFILTER + FILTER will help search?
	 }
		
	private void receivedBroadcast(Intent i) {
		System.out.println("*Refreshing List*");
		currentDisplay = LASTVISITORS;
		getList();
	}
	
	
	
	public void startReciever(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		getActivity().registerReceiver(Services.getPollingHandler(), intentFilter);
	}    
}

