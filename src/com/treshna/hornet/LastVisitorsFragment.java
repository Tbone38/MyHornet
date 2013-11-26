package com.treshna.hornet;

import java.util.ArrayList;

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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/*
 * 
 */
public class LastVisitorsFragment extends ListFragment implements OnClickListener,  LoaderManager.LoaderCallbacks<Cursor>{
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
            LastVisitorsFragment.this.receivedBroadcast(intent);
        }
    };
	
    private Cursor cur = null;
    private SimpleCursorAdapter mAdapter;
    private LoaderManager mLoader;
	private static int currentDisplay = -1; //make this private.}
    
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
        mLoader = this.getLoaderManager();
        if (cur != null) cur.close();
        
        /**the below code needs to run on app start.
         */
        /*Intent updateInt = new Intent(getActivity(), HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
		PendingIntent pintent = PendingIntent.getService(getActivity(), 0, updateInt, PendingIntent.FLAG_UPDATE_CURRENT);
		//polling = new PollingHandler(this, pintent);
		Services.setFreqPollingHandler(getActivity(), pintent);
		startReciever();*/
		/************************************/
        //different views look different, build the views differently?
        getList();
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.visitor_list, container, false);
		
        getActivity().setTitle("Last Visitors");
        String[] from = {};
        int[] to = {};
        mLoader.initLoader(0, null, this);
        mAdapter = new VisitorsViewAdapter(getActivity(), R.layout.visitor_row, cur, from, to, this);
        setListAdapter(mAdapter);
        
        return view;
    }
	
	public int getCurrentDisplay(){
		return currentDisplay;
	}
	
	public static Context getContext(){
		return context;
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
		 mLoader.restartLoader(0, null, this);
	 }
		
	private void receivedBroadcast(Intent i) {
		System.out.println("*Refreshing List*");
		currentDisplay = LASTVISITORS;
		getList();
	}
	
	
	
	public void startReciever(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		getActivity().registerReceiver(Services.getFreqPollingHandler(), intentFilter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case(R.id.listRow):{
			//System.out.println("**Row Selected, Displaying More Info:");
			ArrayList<String> tagInfo = null;
			if (v.getTag() instanceof ArrayList<?>) {
				tagInfo = (ArrayList<String>) v.getTag();
			} else {
				break;
			}
			LinearLayout theView = (LinearLayout) this.getParentFragment().getView().findViewById(R.id.panel_frame);
			Log.d(TAG, "The Tag IS:"+theView.getTag());
			if (theView.getTag().toString().compareTo("single_panel") == 0) {
				//Single Panel
				Intent intent = new Intent(getActivity(), EmptyActivity.class);
				intent.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MemberDetails.getKey());
				intent.putStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID, tagInfo);
				getActivity().startActivity(intent);
			} else {
				//Dual Panel
				((ViewGroup)getView().getParent()).getId();
				LastVisitorsSuperFragment fr = (LastVisitorsSuperFragment) this.getParentFragment();
				fr.setMemberDisplay(tagInfo);
			}
		}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), 
				ContentDescriptor.Visitor.VISITOR_JOIN_MEMBER_URI, 
				null, null, null, 
				ContentDescriptor.Visitor.Cols.DATETIME+" DESC limit 100");
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
}

