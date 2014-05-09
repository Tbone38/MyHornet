package com.treshna.hornet.network;

import java.sql.ResultSet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.treshna.hornet.HornetApplication;
import com.treshna.hornet.R;
import com.treshna.hornet.services.ApplicationID;
import com.treshna.hornet.services.FileHandler;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Membership;

/* TODO: refactor this service out into:
 * 		- initial Getting/querying 
 * 		- Uploading
 * 		- Updating
 * 		- Deleting
 * 		
 * Because we're at 4k lines.
 */

public class HornetDBService extends Service {
	
	public static final String RESULT = "sync_result";
	private static String TAG = "HORNETSERVICE";
	private static ContentResolver contentResolver = null;
    private static  Cursor cur = null; 
    private JDBCConnection connection = null;
    private String statusMessage = "";
    private static Handler handler;
    private static int currentCall;
    private static FileHandler logger;

    //Context ctx;
    private long this_sync;
    private long last_sync;
    
    public static final String STARTBROADCAST = "com.treshna.hornet.startBroadcast";
    public static final String FINISHBROADCAST = "com.treshna.hornet.finishBroadcast";
    
    /****/ //does this need to be final as well?
    private static NetworkThread thread;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	//context is probably leaky?
	private void setup(Context context) {
		//SharedPreferences preferences;
		if (context == null) {
			context = getApplicationContext();
		} else {
		}
		
		connection = new JDBCConnection(context);
		contentResolver = context.getContentResolver();
	   
	}
	@Override  
	public int onStartCommand(final Intent intent, int flags, int startId) {  //final ?
	   handler = new Handler();

	   setup(null);
	   
	   logger = new FileHandler(getApplicationContext());
	   logger.clearLog();
	   currentCall = intent.getIntExtra(Services.Statics.KEY, -1);
	   Bundle bundle = intent.getExtras();
	   
	   //TODO: 	check if this is more or/less memory efficient.
	   //		also check if the threading is functional.
	   thread = NetworkThread.getInstance();
	   thread.addNetwork(currentCall, bundle, this);
	 //  startNetworking(currentCall, bundle);
	 
	   if (!thread.isAlive() && thread.getState() == Thread.State.NEW) {
		   Log.v(TAG, "STARTING THREAD");
		   thread.start();
	   } else if (thread.getState() == Thread.State.TERMINATED || !thread.isAlive()) {
		   Log.v(TAG, "RESTARTING THREAD");
		   thread = new NetworkThread();
		   thread.addNetwork(currentCall, bundle, this);
		   thread.start();
		   
	   } else {
		   Log.v(TAG, "Thread State:"+thread.getState());
	   }
	   
	   
	   // do these want moved into the threads ?
	   statusMessage = "";
       this.stopSelf();
	   return super.onStartCommand(intent, flags, startId);  
	}  
		
    @Override
    public void onDestroy() {
        super.onDestroy();
        connection.closeConnection();
        Log.d(TAG, "Database Service destroyed");
    }
    /**
     * this functions starts a series of network operations determined by the call.
     * for a list of calls, see Services.Statics
     * 
     */
    public synchronized void startNetworking(int currentcall, Bundle bundle){
    	String first_sync = Services.getAppSettings(getApplicationContext(), "first_sync");
    	HornetApplication mApplication = ((HornetApplication) getApplicationContext()).getInstance();
    	
    	if (first_sync.compareTo("-1")==0 && currentCall == Services.Statics.FREQUENT_SYNC) {
    		currentCall = Services.Statics.FIRSTRUN;
    	}
    	
    	if (!getDeviceDetails()) {
    		
    		Intent bcIntent = new Intent();
    		bcIntent.putExtra(RESULT, false);
			bcIntent.setAction(FINISHBROADCAST);
			sendBroadcast(bcIntent);
	   		return;
	   	} else {
	   		//Starting networking broadcast.
	   		Intent bcIntent = new Intent();
    		bcIntent.putExtra(RESULT, true);
			bcIntent.setAction(STARTBROADCAST);
			sendBroadcast(bcIntent);
	   	
	   	}
    	
    	
    	switch (currentCall){
 	   	case (Services.Statics.FREQUENT_SYNC): { //this should be run frequently
 	   		thread.is_networking = true;
 	   		
 	   		mApplication.setSyncStatus(true);
    	
			if (first_sync.compareTo("-1")==0) {
				SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
				Services.setPreference(getApplicationContext(), "first_sync", format.format(new Date()));
			}
 	   		
 	   		this_sync = System.currentTimeMillis();
 	   		last_sync = Long.parseLong(Services.getAppSettings(getApplicationContext(), "last_freq_sync")); //use this for checking lastupdate
	 	   	
 	   		setDate();
 	   		getPendingDownloads();
			
			uploadIdCard();
			getIdCards(last_sync, null);
			
			getMemberNoteID();
			getMemberID();
			getBookingID();
			getMembershipID();
			getIdCardID();
			getImageID();
			getResourceID();
			getProgrammeGroupID();
			getBookingTypeID();
			int sid_count = getSuspendID();
			if (sid_count < 0) {
				Services.showToast(getApplicationContext(), statusMessage, handler);
			}
			
			//do uploads //ORDER MATTERS!
			uploadProgrammeGroup();
			uploadMember();
			uploadProspects();
			uploadMembership();
			getPendingUpdates();
			updateMembership();
			uploadMemberNotes();
			uploadImage();
			updateImage();
			uploadBookingTypes();
			uploadBookings();
			insertResource();
			updateResource();
			updateProgrammeGroup();
			updateBookingTypes();
			int classcount = uploadClass();
			Log.d(TAG, "Uploaded "+classcount+" Classes");
			int upload_sid_count = uploadSuspends();
			if (upload_sid_count < 0) {
				Services.showToast(getApplicationContext(), statusMessage, handler);
			}
			
			//do bookings!
			//getOpenHours(last_sync);
			getOpenHours(0);
			updateOpenHours();
			updateBookings();
			long b_lastsync = Long.parseLong(Services.getAppSettings(getApplicationContext(), "b_lastsync"));
			getBookingType(last_sync);
			getBookings(b_lastsync);
			getClasses(last_sync);
			
			//downloads!
			getResourceType(last_sync);
			getResource(last_sync);
			getMember(last_sync);
			getProgrammes(last_sync);
			getProgrammeGroups(last_sync);
			getMembership(last_sync);
			getMembershipSuspends(last_sync);
			getClasses(last_sync);
			
			//Roll Stuff.
			int use_roll = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "use_roll"));
			if (use_roll > 0) {
	 	   		getRollID();
	 	   		getRollItemID();
	 	   		uploadRoll();
	 	   		uploadRollItem();
	 	   		updateRollItem();
	 	   		getRoll(last_sync);
	 	   		getRollItem(last_sync);
		 	   		
		 	   	logger.writeLog();
		   	  	uploadLog();
		   	  	
		   	  	mApplication.setSyncStatus(false);
		   	  	mApplication.setSyncResult(true);
		   	  	
	 	   		Services.setPreference(getApplicationContext(), "last_freq_sync", String.valueOf(this_sync));
		 	   	Intent bcIntent = new Intent();
				bcIntent.putExtra(RESULT, true);
				bcIntent.setAction(FINISHBROADCAST);
				sendBroadcast(bcIntent);
	 	   		return;
			}
			
			getMemberNotes(last_sync);
			getMemberBalance(last_sync);
			getFinancialDetails(last_sync);
			getBillingHistory(last_sync);
			
			//get Visitors
 	   		boolean result = getLastVisitors();
			if (result == true) { //If database query was successful, then look for images; else show toast.
				visitorImages();
			}
			
			uploadPendingDeletes();
			getDeletedRecords(last_sync);
			logger.writeLog();
	   	  	uploadLog();
	   	  	
	   	  	mApplication.setSyncStatus(false);
	   	  	mApplication.setSyncResult(true); //where is this actually getting set?
	   	  	
	   	  	//Finish.
			Services.setPreference(getApplicationContext(), "last_freq_sync", String.valueOf(this_sync));
			//Services.showToast(getApplicationContext(), statusMessage, handler);
			/*Broadcast an intent to let the app know that the sync has finished
			 * communicating with the server/updating the cache.
			 * App can now refresh the list. */
			
			Intent bcIntent = new Intent();
			bcIntent.putExtra(RESULT, true);
			bcIntent.setAction(FINISHBROADCAST);
			sendBroadcast(bcIntent);
			Log.v(TAG, "Sending Intent, Stopping Service");
			
 		   	break;
 	   } 
 	   case (Services.Statics.SWIPE):{
 		   statusMessage = null;
 		   thread.is_networking = true;
			  
		   int result;
		   result = swipe();
		   
		   if (result >= 0) {/*We didn't swipe anybody in.*/};//TODO:
		   Services.showToast(getApplicationContext(), statusMessage, handler);
 		   new Thread (new Runnable() { 
 				public void run() { 
 					try {
 						wait(1500);
 					} catch (Exception e ) {};
 					Intent updateInt = new Intent(getApplicationContext(), HornetDBService.class);
 					updateInt.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
 					getApplicationContext().startService(updateInt);
 				}}).start();
 		  
 		   break;
 	   }
 	   
 	   case (Services.Statics.FIRSTRUN):{ //this should be run nightly/weekly
 		  thread.is_networking = true;
 		  mApplication.setSyncStatus(true);
 		  if (first_sync.compareTo("-1")==0) {
 			  SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
 			  Services.setPreference(getApplicationContext(), "first_sync", format.format(new Date()));
 		  }
 		  
 		  this_sync = System.currentTimeMillis();
 		  
		   //config
		   int rcount = getResource(0);
		   int days = getOpenHours(0);
		   setTime(); 
	   	   setDate();
	   	   updateOpenHours();
		   getDoors();
		   getConfig();
		   getPaymentMethods();
		   
		   getResourceType(last_sync);
		   getProgrammes(0);
		   getProgrammeGroups(0);
		   getBookings(0);
		   getClasses(-1);
		   getImageID();
		   
		   int use_roll = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "use_roll"));
		   if (use_roll > 0) {
			   getMember(-1);
			   getMembership(-1);
			   getProgrammes(0);
			   getLastVisitors();
			   
			   //ROLL STUFF
			   getRollID();
			   getRollItemID();
			   uploadRoll();
			   uploadRollItem();
			   updateRollItem();
			   getRoll(0);
	 	   	   getRollItem(0);
			   
			   Services.setPreference(getApplicationContext(), "last_freq_sync", String.valueOf(this_sync));
		   	   //rebuild times, then update the reference in date.
			   logger.writeLog();
		   	   uploadLog();
			   mApplication.setSyncStatus(false);
		   	   mApplication.setSyncResult(true);
		   	   
		   	   thread.is_networking = false;
			   Intent bcIntent = new Intent();
			   bcIntent.putExtra(RESULT, true);
			   bcIntent.setAction(FINISHBROADCAST);
			   sendBroadcast(bcIntent);
		   	   return;
		   }
		   
		   //id's
		   getIdCardID();
		   getMemberNoteID();
		   getBookingID();
		   getResourceID();
		   getProgrammeGroupID();
		   getBookingTypeID();
		   int midcount = getMemberID();
		   if (midcount != 0) statusMessage = " Sign-up's available";
		   if (statusMessage != null && statusMessage.length() > 3 ) {
				   Services.showToast(getApplicationContext(), statusMessage, handler);
		   }
		   		   
		   //stuff
		   
		   int rscount = getResultStatus();
		   int btcount = getBookingType(-1);
		   int mcount = getMember(-1);
		   int mscount = getMembership(-1);
		   memberImages();
		   fixImages();
		   getMembershipSuspends(-1);
		   getMemberBalance(-1);
		   getMemberNotes(-1);
		   getMembershipExpiryReasons();
		   getFinancialDetails(0);
		   getBillingHistory(0);
		   getLastVisitors();
		   
		   //do Memberships!
		   getIdCards(0, null);
		   
		   uploadPendingDeletes();
		   
		   Services.setPreference(getApplicationContext(), "last_freq_sync", String.valueOf(this_sync));

	   	  	//rebuild times, then update the reference in date.
	   	  	logger.writeLog();
	   	  	uploadLog();
	   	  	mApplication.setSyncStatus(false);
	   	  	mApplication.setSyncResult(true);

		   if (statusMessage != null) {
			   Services.showToast(getApplicationContext(), statusMessage, handler);
		   }
		   statusMessage = "Recieved "+mcount+" Members, "+mscount+" memberships, and "+rcount+" Resources";
		   Services.showToast(getApplicationContext(), statusMessage, handler);
		   Log.v(TAG, "rcount:"+rcount+"  btcount:"+btcount+"  rscount:"+rscount+"  mcount:"+mcount
				   +"  days:"+days);
		   
		  /*Services.showToast(getApplicationContext(),"Download Finished GymMaster Mobile will now restart",handler);*/
		   
		  Intent bcIntent = new Intent();
		  bcIntent.putExtra(Services.Statics.IS_RESTART, true);
		  bcIntent.putExtra(RESULT, true);
		  bcIntent.setAction(FINISHBROADCAST);
		  sendBroadcast(bcIntent);
		  Log.v(TAG, "Sending Intent, Stopping Service");
		  
 		  break;
 	   }
 	   /* The below service is not networking, but updates the local-database based on
 	    * user selections, It's been placed in this service for ease of use.
 	    * It's threaded so it doesn't block the UI.
 	    */
 	   
 	   case (Services.Statics.CLASSSWIPE):{
 		   thread.is_networking = true;
 		   int result;
 		   result = classSwipe();
 		   if (result <= 0) {
 			   Log.e(TAG, statusMessage);
 			   Log.e(TAG, "Class Swipe returned Error-Code:"+result);
 		   }
 		   break;
 	   }
 	   case (Services.Statics.MANUALSWIPE):{
 		   thread.is_networking = true;
 		   
 		   int doorid, memberid, membershipid;
 		   doorid = bundle.getInt("doorid");
 		   doorid =(doorid < 0)? 1: doorid;
 		   memberid = bundle.getInt("memberid");
 		   membershipid = bundle.getInt("membershipid");
 		   
 		   this.manualCheckin(doorid, memberid, membershipid, null);
 		   
 		   break;
 	   }
 	   }
    	updateDevice();
		connection.closeConnection();
    	thread.is_networking = false;
    }
    
    public static Handler getHandler(){
    	return handler;
    }
    
	private boolean getLastVisitors(){
		Log.v(TAG, "Getting Last Visitors");
    	long this_sync = new Date().getTime();
    	if (!openConnection()) {
    		return false; //connection failed;
    	}
	
        	/*
        	 * The Below information handles queries. 
        	 */
    		int fileSize = 10000; //
    		// file size, gets passed into byte, this number need only be an int though.
    		FileHandler fileHandler = new FileHandler(this);
    		String query = fileHandler.readFile(fileSize, "callumLastVisitors130416.sql");
    		ResultSet rs = null;
    		try { //is this working?
    			//Log.d(TAG, query);
    			//Log.d(TAG, "Connected: "+connection.isConnected());
    			rs = connection.startStatementQuery(query);
    		}catch (Exception e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "",e);
    			return false;
    		}
        	int insertCount = 0;
        	int updateCount = 0;
        	ContentValues val = new ContentValues();
        	try {
        	while (rs.next()) {
        		/*Get Last ID, set this ID = ID+1*/
        		Double doubledate = rs.getDouble("datetime")*1000d;
	    		String date = String.valueOf(doubledate); //we have to convert from secs since epoch to ms since epoch.
	    		
    			cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, null, null,
    					ContentDescriptor.Visitor.Cols.ID+" DESC Limit 1");
    			//this should auto increment anyway?
    			int id = 0;
    			if (cur.getCount() > 0) {
        			cur.moveToPosition(0);
					id = cur.getInt(cur.getColumnIndex(ContentDescriptor.Visitor.Cols.ID));
					id +=1;
				} else {
					id = 1;
				}
    			cur.close();
        		
    			cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, 
						ContentDescriptor.Visitor.Cols.DATETIME+" = ? AND "+ContentDescriptor.Visitor.Cols.DENY+" = ?", 
						new String[] {date, rs.getString("denyreason")},null);
				if (cur.getCount() != 0) {
					// do nothing, an item exists with this datetime already.
					Log.i(TAG,"results reached last query");
					cur.close();
    				break;
				}
				cur.close();
        		String memberid = rs.getString("memberid");
        		boolean isNull = rs.wasNull();
        		if (isNull == true) { //if the id was null, handle null Data.
        			Random r = new Random();
	    			memberid = "-"+Integer.toString(r.nextInt(100000)); //force this int(?) to be negative
	    			//Log.v(TAG, "null Member");
        		}
        		/*Addded 2013-05-21*/
        		String membershipid = rs.getString("membershipid");
        		boolean msNull = rs.wasNull();
        		if (msNull == true) {
        			Random r = new Random();
	    			membershipid = "-"+Integer.toString(r.nextInt(100)); //force this int(?) to be negative
	    			//Log.v(TAG, "null Membership");
        		}
        		val = new ContentValues();
        		val.put(ContentDescriptor.Visitor.Cols.ID, id);
        		val.put(ContentDescriptor.Visitor.Cols.MID, memberid);
        		val.put(ContentDescriptor.Visitor.Cols.MSID, membershipid);
        		val.put(ContentDescriptor.Visitor.Cols.DATETIME, date); 
        		val.put(ContentDescriptor.Visitor.Cols.DATE, rs.getString("sdate"));
        		val.put(ContentDescriptor.Visitor.Cols.TIME, rs.getString("stime12"));
        		val.put(ContentDescriptor.Visitor.Cols.DENY, rs.getString("denyreason"));
        		val.put(ContentDescriptor.Visitor.Cols.CARDNO, rs.getString("cardno"));
        		val.put(ContentDescriptor.Visitor.Cols.DOORNAME, rs.getString("doorname"));
        		val.put(ContentDescriptor.Visitor.Cols.LASTUPDATE, this_sync);
        		
        		contentResolver.insert(ContentDescriptor.Visitor.CONTENT_URI, val);
        		if (isNull == false) {
        			//Check if the ID already exists in the database.
        			val = new ContentValues();
        			val.put(ContentDescriptor.Member.Cols.MID, memberid);
        			val.put(ContentDescriptor.Member.Cols.FNAME, rs.getString("fmname"));
        			val.put(ContentDescriptor.Member.Cols.SNAME, rs.getString("lmname"));
		        	val.put(ContentDescriptor.Member.Cols.COLOUR, rs.getString("fgcolour"));
		        	val.put(ContentDescriptor.Member.Cols.LENGTH, rs.getString("len"));
		        	val.put(ContentDescriptor.Member.Cols.BOOKP, rs.getInt("bookingpending"));
		        	val.put(ContentDescriptor.Member.Cols.HAPPINESS, rs.getString("happiness"));
		        	val.put(ContentDescriptor.Member.Cols.RESULT, rs.getString("result"));
		        	val.put(ContentDescriptor.Member.Cols.TASKP, rs.getInt("taskpending"));
	        		val.put(ContentDescriptor.Member.Cols.PHHOME, rs.getString("mphhome"));
	        		val.put(ContentDescriptor.Member.Cols.PHCELL, rs.getString("mphcell"));
	        		val.put(ContentDescriptor.Member.Cols.PHWORK, rs.getString("mphwork"));
	        		val.put(ContentDescriptor.Member.Cols.EMAIL, rs.getString("memail"));
	        		
	        		val.put(ContentDescriptor.Member.Cols.TASK1, rs.getString("task1"));
	        		val.put(ContentDescriptor.Member.Cols.TASK2, rs.getString("task2"));
	        		val.put(ContentDescriptor.Member.Cols.TASK3, rs.getString("task3"));
	        		val.put(ContentDescriptor.Member.Cols.BOOK1, rs.getString("booking1"));
	        		val.put(ContentDescriptor.Member.Cols.BOOK2, rs.getString("booking2"));
	        		val.put(ContentDescriptor.Member.Cols.BOOK3, rs.getString("booking3"));
	        		val.put(ContentDescriptor.Member.Cols.LASTVISIT, rs.getString("lastvisit1"));
	        		val.put(ContentDescriptor.Member.Cols.CARDNO, rs.getString("membercardno"));
	        		
        			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, 
        					"m."+ContentDescriptor.Member.Cols.MID+" = "+memberid, null, null);
        			if (cur.getCount() > 0) {
        				//member exists, update info		
        				contentResolver.update(ContentDescriptor.Member.CONTENT_URI, val,
        						ContentDescriptor.Member.Cols.MID+" = '"+memberid+"'", null);
        			} else {
		        		//Data does not exist in the Database already/
		        		contentResolver.insert(ContentDescriptor.Member.CONTENT_URI, val);
	        		}
        			cur.close();
        			//Log.v(TAG, "MEMBERID: "+memberid);
        			//Log.v(TAG, "MembershipID:"+membershipid);
        			if (isNull == false && msNull == false) {
	        			val = new ContentValues();
	        			val.put(Membership.Cols.MSID, membershipid);
	        			val.put(Membership.Cols.EXPIRERY, rs.getString("msexpiry"));
	        			val.put(Membership.Cols.VISITS, rs.getString("msvisits"));
			        	val.put(Membership.Cols.PNAME, rs.getString("pname"));
	        			val.put(Membership.Cols.MSSTART, rs.getString("msstart"));
	        			val.put(Membership.Cols.MID, memberid);
	        			val.put(Membership.Cols.CARDNO, rs.getString("cardno"));
	        			val.put(Membership.Cols.DENY, rs.getString("result"));
	        			cur = contentResolver.query(Membership.CONTENT_URI, null, Membership.Cols.MSID+" = ?", 
	        					new String[] {rs.getString("membershipid")} , null);
	        			
	        			if (cur.getCount() == 0) {
	        				contentResolver.insert(Membership.CONTENT_URI, val);
	        			} else if (cur.getCount() >0) {
	        				contentResolver.update(Membership.CONTENT_URI, val, Membership.Cols.MSID+" = ?",
	        						new String[] {rs.getString("membershipid")});
	        			}
	        			cur.close();
        			}
        			
        			insertCount = insertCount + 1;
        		}
        	}
        	rs.close();
        	connection.closeStatementQuery();	
        	if ((insertCount == 0) && (updateCount == 0)){
        		statusMessage = "No new Results were received.";
        	} else {
        		statusMessage = (updateCount+insertCount)+" Rows Recieved from Server";
        	}
        	}
    	catch (SQLException e){
    		if (e.getCause() != null) {
	    		Throwable cause = e.getCause();
	    		if (cause.getMessage().compareToIgnoreCase("host=-1, port=-1") == 0) { //shouldn't get here?
	    			statusMessage = "Error Occured: Server not set. Please visit the Application Settings before attempting to connect";
	    			connection.closeStatementQuery();
	    			cur.close();
	    			return false;
	    		} else {
	    			statusMessage = "Exception Occured: "+cause.getMessage()+";";
	    			e.printStackTrace();
	    			connection.closeStatementQuery();
	    			cur.close();
	    			return false;
	    		}
    		} else {
	    		Log.e(TAG,"",e);	
	    	}
    	}
        cleanUp();
        Services.setPreference(getApplicationContext(), "lastsync", String.valueOf(this_sync));//String.valueOf(System.currentTimeMillis())   	
		return true;
    }
    
    private void visitorImages() {
    	getImagesController(last_sync);
    }
    
    private void memberImages() {
    	getImagesController(last_sync);
    }
    
    
    private int getImagesController(long lastsync) {
    	int result = 0, lastrow = 0, maxid = 0;
    	
    	if (!openConnection()) {
    		return 0;
    	}
    	try {
    		maxid = connection.getMaxImageId();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -1;
    	}
    	
    	if (lastsync > 1000) {
    		int[] status = getImages(lastsync, 0);
    		if (status != null) {
    			return status[0];
    		}
    		return -1;
    	}
    	
    	while (lastrow < maxid) {
    		int[] status = getImages(lastsync, lastrow);
    		if (status == null) {
    			return -1;
    		}
    		Log.d(TAG, "LASTROW = "+status[1]);
    		Log.d(TAG, "MAX ROW = "+maxid);
    		lastrow = status[1];
    		result += status[0];
    	}
	
    	return result;
    }
    
    /** 
     * images are written to file as <image id from psql>_<member id>.jpg
     * @param lastsync
     * @return
     */
    //TODO: test this is still functional...
    private int[] getImages(long lastsync, int lastrow) {
    	int result = 0;
    	int highestid = 0;
    	
    	if (!openConnection()) {
    		return null; //connection failed;
    	}
    	
    	try {
    		ResultSet rs = null;
    		if (lastsync > 10000) { //not our first sync, just check for updated images.
    			rs = connection.getImages(lastsync, -1);
    		} else { //it's our first sync, be careful about memory issues! 
    			rs = connection.getImages(lastsync, lastrow);
    		}
    		
    		while (rs.next()) {
    			int rowid = -1;
    			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.IID+" = ?",
    					new String[] {String.valueOf(rs.getInt("id"))}, null);
    			if (cur.moveToFirst()) {
    				rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID));
    			}
    			cur.close();
    			
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.Image.Cols.DESCRIPTION, rs.getString("description"));
    			values.put(ContentDescriptor.Image.Cols.IID, rs.getInt("id"));
    			values.put(ContentDescriptor.Image.Cols.IS_PROFILE, Services.booltoInt(rs.getBoolean("is_profile")));
    			values.put(ContentDescriptor.Image.Cols.MID, rs.getInt("memberid"));
    			values.put(ContentDescriptor.Image.Cols.DATE, rs.getTimestamp("created").getTime());
    			values.put(ContentDescriptor.Image.Cols.LASTUPDATE, rs.getTimestamp("lastupdate").getTime());
    			values.put(ContentDescriptor.Image.Cols.DEVICESIGNUP, "f");
    			highestid = rs.getInt("id");
    			
    			byte[] is = rs.getBytes("imagedata");
            	logger.writeFile(is, rs.getInt("id")+"_"+rs.getString("memberid")+".jpg");
            	
            	if (rowid > 0) {
            		contentResolver.update(ContentDescriptor.Image.CONTENT_URI, values, ContentDescriptor.Image.Cols.ID+" = ?",
            				new String[] {String.valueOf(rowid)});
            		result +=1;
            	} else {
            		contentResolver.insert(ContentDescriptor.Image.CONTENT_URI, values);
            		result += 1;
            	}
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return null;
    	}

    	return new int[] {result, highestid};
    }
    
    private int getImageID() {
    	int result = 0;
    	int free_count = 0; //we should probably get like, 50. and grab more whenever we can.
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey())}, null);
    	
    	free_count = cur.getCount();
    	cur.close();
    	
    	for (int i=(50-free_count); i> 0; i--) {
    		try {
    			ResultSet rs = connection.startStatementQuery("select nextval('image_id_seq');");
    			
    			rs.next(); 	
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getInt("nextval"));
    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID, 
    					ContentDescriptor.TableIndex.Values.Image.getKey());
    			
    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			result +=1;
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			continue;
    		}
    	}
    	
    	return result;
    }
    
    private int uploadImage() {
    	int result = 0;
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey())}, null);
    	
    	ArrayList<Integer> rowList = new ArrayList<Integer>();
    	while (cur.moveToNext()) {
    		rowList.add(cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	 cur.close();
    	
    	if (!openConnection()){
    		return -1;
    	}
    	
    	for (int i=0; i< rowList.size(); i+=1) {
    		cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.ID+" = ?",
    				new String[] {String.valueOf(rowList.get(i))}, null);
    		if (!cur.moveToFirst()) {
    			//couldn't find row?
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? "
    					+ "AND "+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] {
    					String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey()), String.valueOf(rowList.get(i))});
    			cur.close();
    			continue;
    		}
    		
    		long cDate = cur.getLong(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE));
        	
        	FileHandler fileHandler = new FileHandler(getApplicationContext());
        	int imageSize = 20000;
        	byte[] image = fileHandler.readImage(imageSize, cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.IID))+
        			"_"+cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID)));
        	
        	try {
        		connection.uploadImage(image, 
        				cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID)), 
        				cDate,
        				cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.DESCRIPTION)), 
        				Services.isProfile(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IS_PROFILE))),
        				cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IID)));
        		
        		cur.close();
        		result +=1;
        	} catch (SQLException e) {
        		statusMessage = e.getLocalizedMessage();
        		Log.e(TAG, "", e);
        		return -2;
        	}
        	
        	contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? "
					+ "AND "+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] {
					String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey()), String.valueOf(rowList.get(i))});
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    private int uploadMember(){
    	
    	String email, medical, suburb, hphone, cphone, gender, dob, add, city, post;
    	int result = 0;
    	
    	assignMemberIds();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, 
    			ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", 
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey())}, null);
    	ArrayList<String> rows = new ArrayList<String>();
    	while (cur.moveToNext()) {
    		rows.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	for (int i = 0; i <rows.size(); i ++) {
    		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, 
    				"m."+ContentDescriptor.Member.Cols._ID+" = ?", new String[] {rows.get(i)}, null);
    		if (!cur.moveToFirst()) {
    			Log.e(TAG, "COUD NOT FIND ROW ID :"+rows.get(i));
    			cur.close();
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    					ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    					+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] {
    					String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey()),
    					rows.get(i)});
    			continue;
    		}
    		
    		if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL))) email = "";
        	else email = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL))) medical = "";
        	else medical = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB))) suburb = "";
        	else suburb = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME))) hphone = "";
        	else hphone = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL))) cphone = "";
        	else cphone = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET))) add = "";
        	else add = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY))) city = "";
        	else city = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY));
        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL))) post = "";
        	else post = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL));
        	
        	gender = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.GENDER));
        	if (gender.compareTo(getString(R.string.radioMale))== 0) gender = "M";
        	else gender = "F";
        	dob = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.DOB));
        	dob = Services.dateFormat(dob, "dd/MM/yyyy", "dd MMM yyyy");
        	
        	try {
	    		result += connection.uploadMember(cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols.MID)),
						cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)),
						cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME)),
						gender,
						email,
						dob, 
						add,
						suburb,
						city,
						post,
						hphone,
						cphone,
						medical);
	    		connection.closePreparedStatement();
        	} catch (SQLException e) {
        		statusMessage = e.getLocalizedMessage();
        		Log.e(TAG, "", e);
        		return -2;
        	}
        	
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
					ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
					+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] {
					String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey()),
					rows.get(i)});
    		
    		ContentValues values = new ContentValues();
    		values.put(ContentDescriptor.PendingDownloads.Cols.TABLEID, 
    				ContentDescriptor.TableIndex.Values.Member.getKey());
    		values.put(ContentDescriptor.PendingDownloads.Cols.ROWID,
    				cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols.MID)));
    		contentResolver.insert(ContentDescriptor.PendingDownloads.CONTENT_URI, values);
    		
    		values = new ContentValues();
    		values.put(ContentDescriptor.Member.Cols.DEVICESIGNUP, "f");
    		contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, 
    				ContentDescriptor.Member.Cols.DEVICESIGNUP+" = ?", 
    				new String[] {cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MID))});
    		
    		cur.close();
    	}
    	
    	cleanUp();
    	return result;
    }
    
    /**
     * TODO: this will break if user input contains "'"
     * @param rowid
     * @return
     */
    private int updateMember(String rowid) {
    	String memberid = null;
    	String[] columns = {"m."+ContentDescriptor.Member.Cols.CARDNO, ContentDescriptor.Member.Cols.STREET,
    			ContentDescriptor.Member.Cols.SUBURB, ContentDescriptor.Member.Cols.CITY, ContentDescriptor.Member.Cols.POSTAL,
    			ContentDescriptor.Member.Cols.GENDER, ContentDescriptor.Member.Cols.DOB, "m."+ContentDescriptor.Member.Cols.MID,
    			ContentDescriptor.Member.Cols.PHCELL, ContentDescriptor.Member.Cols.PHHOME, ContentDescriptor.Member.Cols.PHWORK,
    			ContentDescriptor.Member.Cols.EMAIL, ContentDescriptor.Member.Cols.EMERGENCYNAME, ContentDescriptor.Member.Cols.EMERGENCYCELL, 
    			ContentDescriptor.Member.Cols.EMERGENCYRELATIONSHIP, ContentDescriptor.Member.Cols.EMERGENCYHOME,
    			ContentDescriptor.Member.Cols.EMERGENCYWORK, ContentDescriptor.Member.Cols.MEDICAL,
    			ContentDescriptor.Member.Cols.MEDICALDOSAGE, ContentDescriptor.Member.Cols.MEDICATION
    			};
    	
    	cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, columns, "m."+ContentDescriptor.Member.Cols._ID+" = ?",
    			new String[] {rowid}, null);
    	
    	if (!cur.moveToNext()) {
    		cur.close();
    		return 0;
    	}
    	memberid = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MID));
    	
    	Log.v(TAG, "Updating Member for ID:"+memberid);
    	int result = 0;
    	String  cardno, street, suburb, 
    			city, areacode, gender, dob, phcell, phhome, phwork,
    			email, emergency_name, emergency_relationship, emergency_cell,
    			emergency_home, emergency_work, medical, medication, medicationdosage;
    	
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CARDNO))) {
			cardno = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.CARDNO));
		} else {
			cardno = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET))) {
			street = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.STREET));
		} else {
			street = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB))) {
			suburb = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SUBURB));
		} else {
			suburb = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY))) {
			city = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.CITY));
		} else {
			city = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL))) {
			areacode = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.POSTAL));
		} else {
			areacode = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.GENDER))) {
			gender = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.GENDER));
		} else {
			gender =null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.DOB))) {
			dob = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.DOB));
		} else {
			dob = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL))) {
			phcell = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL));
		} else {
			phcell = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME))) {
			phhome = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME));
		} else {
			phhome = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK))) {
			phwork = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK));
		} else {
			phwork = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL))) {
			email = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL));
		} else {
			email = null;
		}
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYNAME))) {
			emergency_name = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYNAME));
		} else {
			emergency_name = null;
		}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYRELATIONSHIP))) {
    		emergency_relationship = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYRELATIONSHIP));
    	} else {
    		emergency_relationship = null;
    	}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYCELL))) {
    		emergency_cell = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYCELL));
    	} else {
    		emergency_cell = null;
    	}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYHOME))) {
    		emergency_home = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYHOME));
    	} else {
    		emergency_home = null;
    	}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYWORK))) {
    		emergency_work = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMERGENCYWORK));
    	} else {
    		emergency_work = null;
    	}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL))) {
    		medical = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICAL));
    	} else {
    		medical = null;
    	}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION))) {
    		medication = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICATION));
    	} else {
    		medication = null;
    	}
    	if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICALDOSAGE))) {
    		medicationdosage = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MEDICALDOSAGE));
    	} else {
    		medicationdosage = null;
    	}

    	try {
    		//connection.updateRow(values, ContentDescriptor.Member.NAME, where);
    		connection.updateMember(cardno, street, suburb, city, areacode, gender, dob, memberid,
    				phcell, phhome, phwork, email, emergency_name, emergency_relationship, emergency_cell,
    				emergency_home, emergency_work, medical, medication, medicationdosage);
    		result+=1;
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	} finally {
    		ContentValues value = new ContentValues();
    		value.put(ContentDescriptor.Member.Cols.DEVICESIGNUP, "f");
    		contentResolver.update(ContentDescriptor.Member.CONTENT_URI, value, ContentDescriptor.Member.Cols.MID+" = ?",
    				new String[] {memberid});
    	}
    	cur.close();
    	return result;
    }
    	
    	
    /** Retrieves and stores free memberID's from the database,
     * the memberID's are assigned to members upon signup through the app.
     */
    private int getMemberID() {
    	//when pending.rowCount < 10, get memberID until rowCount = 200
    	//always do upload first.
    	Log.v(TAG, "Getting MemberID's");
    	int count;
    	if (!openConnection()) {
    		return -1; //connection failed; see statusMessage for why
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
    					+ContentDescriptor.TableIndex.Values.Member.getKey(),null, null);
    	count = cur.getCount();
    	cur.close();
    	if (count>= 7) {
    		// have 7 ID's already, don't bother getting more.
    		return 0;
    	}
    	for (int l=(10-count); l>=0;l -=1){ //Dru suggested 200?
    		count = 0;
    		ResultSet rs = null;
    		try {
    			int id;
    			rs = connection.startStatementQuery("select nextval('member_id_seq');");
    			rs.next();
    		
	    		ContentValues val = new ContentValues();
	    		Log.v(TAG, "MID: "+rs.getString("nextval"));
	    		
	    		
	    		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null,
	    				ContentDescriptor.Member.Cols.MID+" = -1", null, null);
	    		
	    		Log.v(TAG, "Pending Uploads (without ID):"+cur.getCount());
	    		if (cur.getCount() != 0) {
	    			val.put(ContentDescriptor.Member.Cols.MID, rs.getString("nextval"));
	    			cur.moveToFirst();
	    			id = cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols._ID));
		    		cur.close();
	    			contentResolver.update(ContentDescriptor.Member.CONTENT_URI, val, 
	    					ContentDescriptor.Member.Cols._ID+" = "+id, null);
	    			val = new ContentValues();
	    			val.put(ContentDescriptor.PendingUploads.Cols.TABLEID, 
	    					ContentDescriptor.TableIndex.Values.Member.getKey());
	    			val.put(ContentDescriptor.PendingUploads.Cols.ROWID, id);
	    			
	    			contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, val);
	    			
	    			l +=1; 
	    		}
	    		else {
	    			val.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
	    			val.put(ContentDescriptor.FreeIds.Cols.TABLEID, 
	    					ContentDescriptor.TableIndex.Values.Member.getKey());
	    			//val.put(ContentDescriptor.Member.Cols.STATUS, -1);
	    			//contentResolver.insert(ContentDescriptor.Member.CONTENT_URI, val);
		    		contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, val);
		    		cur.close();
	    		}
	    		count +=1;
	    		rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return 0;
    		}

    	}
    	cleanUp();
    	return count;
    }
    
    private int getBookingID(){
    	Log.v(TAG, "Getting Booking ID's");
    	int result = 0, count;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}

    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.Booking.getKey(), null, null);
    	count = cur.getCount();
    	cur.close();
    	
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" < 0",
				null, null);
		if (cur.getCount() > 0) {
			//some booking's need ids.
			count = count - cur.getCount();
		}
		cur.close();
		
		if (count>= 16) {
    		// have 16 ID's already, don't bother getting more.
    		return 0;
    	}
    	
    	for (int l=(20-count); l>=0;l -=1){ //Dru suggested 200?
    		rs = null;
    		try {
    			int rowid = 0;
    			boolean is_update = false;
	    		rs = connection.startStatementQuery("select nextval('booking_id_seq');");
				rs.next();
				
				/*Check to see if there are any booking's that need ids (id < 0)*/
				cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" < 0",
						null, null);
				if (cur.getCount() > 0) {
					//some booking's need ids.
					is_update = true;
					cur.moveToFirst();
					rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ID));
				}
				
				ContentValues val = new ContentValues();
				Log.v(TAG, "BID:"+rs.getString("nextval"));
	    		
	    		
	    		if (is_update) {
	    			val.put(ContentDescriptor.Booking.Cols.BID, rs.getString("nextval"));
	    			val.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
	    			contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, val, ContentDescriptor.Booking.Cols.ID+" = ?",
	    					new String[] {String.valueOf(rowid)});
	    			
	    		} else {
	    			val.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
	    			val.put(ContentDescriptor.FreeIds.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Booking.getKey());
	    			//val.put(ContentDescriptor.Booking.Cols.LASTUPDATE, 0);
	    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, val);
	    		}
	    		
				result +=1;
    		} catch (SQLException e) {
    			Log.e(TAG, "", e);
    			statusMessage = e.getLocalizedMessage();
    		}
    		try {
    			connection.closeStatementQuery();
    			rs.close();
    		}catch (SQLException e) {
    			//doesn't matter, we're only closing the statement anyway.
    		} catch (NullPointerException e) {
    			//either the connection or the resultSet is null. likely because of bad settings.
    		}
    	}
    	cleanUp();
    	return result;
    }
    
    private int getResource(long last_sync){
    	ResultSet rs = null;
    	int result = 0;
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	try {
    		rs = connection.getResource(last_sync);
    		while (rs.next()) {
    			cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID +" = "+rs.getString(1),
	    				null, null);
	    		if (cur.getCount() == 0) {
	    			ContentValues val = new ContentValues();
		    		val.put(ContentDescriptor.Resource.Cols.ID, rs.getString("resourceid"));
		    		val.put(ContentDescriptor.Resource.Cols.NAME, rs.getString("resourcename"));
		    		val.put(ContentDescriptor.Resource.Cols.CID, rs.getString("resourcecompanyid"));
		    		val.put(ContentDescriptor.Resource.Cols.RTNAME, rs.getString("resourcetypename"));
		    		val.put(ContentDescriptor.Resource.Cols.PERIOD, rs.getString("resourcetypeperiod"));
		    		
		    		contentResolver.insert(ContentDescriptor.Resource.CONTENT_URI, val);
		    		result +=1;
	    		}
	    		cur.close();
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    	}
    	cleanUp();
    	
    	return result;
    }
    /**
     * This function needs to:
     * 		- upload bookings that have been added on the phone to the server.
     * 		- resync the bookings to the phone,
     * 		- check for conflicts?
     * @return
     */
    private int uploadBookings(){
    	Log.v(TAG, "Upload Bookings");
    	int result;
    	result = 0;

    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())}, null);
    	ArrayList<String> idlist = new ArrayList<String>();
    	while (cur.moveToNext()) {
    		idlist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	for (int i = 0; i< idlist.size();i +=1) {
    		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.ID+" = ?",
    				new String[] {idlist.get(i)}, null);
    		cur.moveToFirst();
		
    		Map<String, String> values = new HashMap<String, String>();//seriously, why isn't this a contentValue?
			values.put(ContentDescriptor.Booking.Cols.BID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
			values.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE)));
			values.put(ContentDescriptor.Booking.Cols.ETIME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)));
			values.put(ContentDescriptor.Booking.Cols.FNAME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME)));
			values.put(ContentDescriptor.Booking.Cols.SNAME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME)));
			values.put(ContentDescriptor.Booking.Cols.RESULT, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT)));
			values.put(ContentDescriptor.Booking.Cols.MID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.MID)));
			values.put(ContentDescriptor.Booking.Cols.STIME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)));
			values.put(ContentDescriptor.Booking.Cols.ARRIVAL, Services.DateToString(new Date(
					cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL)))));
			values.put(ContentDescriptor.Booking.Cols.RID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RID)));
			Log.v(TAG, "OFFSET:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			values.put(ContentDescriptor.Booking.Cols.OFFSET, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			Log.v(TAG, "Booking Modified:"+cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATE)));
			Date lastupdate = new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATE)));
			Log.v(TAG, "Last-Update:"+lastupdate.getTime());
			
			values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, String.valueOf(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATE))));
			values.put(ContentDescriptor.Booking.Cols.PARENTID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.PARENTID)));
			String classid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CLASSID));
			if (classid.compareTo("")==0||classid.compareTo("0")==0|| classid.compareTo(" ")==0) {
				values.put(ContentDescriptor.Booking.Cols.CLASSID, null);				
			} else {
				values.put(ContentDescriptor.Booking.Cols.CLASSID, classid);
			}
			
			//below values can be null?
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.MSID)))
				values.put(ContentDescriptor.Booking.Cols.MSID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.MSID)));
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.NOTES)))
				values.put(ContentDescriptor.Booking.Cols.NOTES, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.NOTES)));
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN))){
				values.put(ContentDescriptor.Booking.Cols.CHECKIN, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN)));
			}
			try {
				int state = 0;
	    		state = connection.uploadBookings(values);
	    		if (state >=1) {
	    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
	    					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {idlist.get(i), 
	    					String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())});
	    			
	    			ContentValues val = new ContentValues();
	    			val.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "f");
	    			contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, val, ContentDescriptor.Booking.Cols.ID+" = ?",
	    					new String[] {idlist.get(i)});
	    		}
	    		result += state;
	    		connection.closePreparedStatement();
	    	} catch (SQLException e) {
	    		Log.e(TAG, "", e);
	    		statusMessage = e.getLocalizedMessage();
	    	}
			cur.close();
			i +=1;
    	}
    	
    	Log.v(TAG, "Uploaded "+result+" Bookings");
    	cleanUp();
    	
    	return result;
    }
    
    private int updateBookings(){
    	Log.v(TAG, "Updating Bookings");
    	int result;
    	ArrayList<String> pending_bookings = new ArrayList<String>();
    	
    	result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())}, null);
    	
    	while (cur.moveToNext()){
    		pending_bookings.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	for (int i=0; i<pending_bookings.size(); i++)
    	{
    		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.ID+" = ?",
    				new String[] {pending_bookings.get(i)},null);
    		if (!cur.moveToFirst()) {
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {pending_bookings.get(i),
    					String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())});
    		}
    	
    		int bookingid, resultstatus, bookingtypeid;
    		long lastupdate, checkin, arrival;
    		String notes, starttime, endtime, offset;
    	
    		bookingid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
    		resultstatus = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT));
    		bookingtypeid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE));
    		lastupdate = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATE)); //TODO: change this to now();
    		checkin = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN));
    		notes = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.NOTES));
    		arrival = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
    		starttime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
    		endtime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME));
    		offset = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET));
    		
    		//Log.v(TAG, "Last update for booking:"+bookingid+" was "+lastupdate);
    		try {
    			result += connection.updateBookings(bookingid, resultstatus, notes, lastupdate, bookingtypeid, checkin,
    					arrival, starttime, endtime, offset);
    			connection.closePreparedStatement();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    		}
    		cur.close();
    		contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
					+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {pending_bookings.get(i),
					String.valueOf(ContentDescriptor.TableIndex.Values.Booking.getKey())});
    		
    		ContentValues values = new ContentValues();
    		values.put(ContentDescriptor.Booking.Cols.DEVICESIGNUP, "f");
    		contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?", new String[] 
    				{String.valueOf(bookingid)});
    		
    	}
    	Log.v(TAG, "Updated "+result+" Booking!");
    	cleanUp();
    	return result;
    }
    
    private int getBookings(long last_sync){
    	Log.v(TAG, "Getting Bookings");
    	ResultSet rs;
    	int result;
    	Calendar cal;
    	java.sql.Date yesterday, tomorrow;
    	long this_sync;
    	
    	rs = null;
    	result = 0;
    	cal = Calendar.getInstance();
    	cal.add(Calendar.MONTH, -1);
    	yesterday = new java.sql.Date(cal.getTime().getTime());
 
    	cal.add(Calendar.MONTH, +2);
    	tomorrow = new java.sql.Date(cal.getTime().getTime());
    	
    	this_sync = new Date().getTime(); 
    	//last_sync = Long.parseLong(Services.getAppSettings(getApplicationContext(), "b_lastsync"));
    	
    	    	
    	cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, null, null, null);
    	if (cur.getCount() <= 0) {
    		Log.d(TAG, "No Rows returned by BookingTime table, will now setup date/time tables before continuing.");
	   	  	//rebuild times, then update the reference in date.
	   	  	setTime(); 
	   	  	setDate();
	   	  	updateOpenHours();
	   	  	
    	}
    	cur.close();
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	
    	try {
    		rs = connection.getBookings(yesterday, tomorrow, last_sync);
	    	while (rs.next()) {
	    		ContentValues val;
	    		SimpleDateFormat format;
	    		String uscheckin, date;
	    		long checkin;
	    		int starttime, endtime, timeid;
	    		boolean has_parent = true;
	    		
	    		val = new ContentValues();
	    		
	    		val.put(ContentDescriptor.Booking.Cols.FNAME, rs.getString("firstname"));
	    		if (rs.wasNull()) {
	    			val.put(ContentDescriptor.Booking.Cols.FNAME, rs.getString("classname"));
	    		}
	    		val.put(ContentDescriptor.Booking.Cols.SNAME, rs.getString("surname"));
	    		val.put(ContentDescriptor.Booking.Cols.BOOKING, rs.getString("bookingname"));
	    		
	    		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	    		uscheckin = rs.getString("checkin"); 
	    		checkin = 0;
	    		if (uscheckin != null && !uscheckin.isEmpty()) {
	    			try {
	    				checkin = format.parse(uscheckin).getTime();
	    				val.put(ContentDescriptor.Booking.Cols.CHECKIN, checkin);
	    			} catch (ParseException e) {
	    				Log.e(TAG, "error parsing check-in time for:"+rs.getString("firstname")+" "+rs.getString("surname"));
	    				Log.e(TAG, e.getMessage());
	    				checkin = 0;
	    			}
	    		}
	    		
	    		//date = Services.dateFormat(rs.getString("arrival"), "yyyy-MM-dd", "yyyyMMdd");
	    		Double doubledate = rs.getDouble("arrival")*1000d;
	    		date = String.valueOf(doubledate);
	    		starttime = getTime(rs.getString("startid"), contentResolver, false );
	    		endtime = getTime(rs.getString("endtime"), contentResolver, true);
	    		
	    		val.put(ContentDescriptor.Booking.Cols.STIMEID, starttime);
	    		val.put(ContentDescriptor.Booking.Cols.ETIMEID, endtime);
	    		val.put(ContentDescriptor.Booking.Cols.STIME, rs.getString("startid"));
	    		val.put(ContentDescriptor.Booking.Cols.ETIME, rs.getString("endtime"));
	    		val.put(ContentDescriptor.Booking.Cols.BID, rs.getString("bookingid"));
	    		val.put(ContentDescriptor.Booking.Cols.NOTES, rs.getString("notes"));
	    		val.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, rs.getString("bookingtypeid"));
	    		val.put(ContentDescriptor.Booking.Cols.RESULT, rs.getString("result"));
	    		val.put(ContentDescriptor.Booking.Cols.MID, rs.getString("memberid"));
	    		val.put(ContentDescriptor.Booking.Cols.MSID, rs.getString("membershipid"));
	    		val.put(ContentDescriptor.Booking.Cols.RID, rs.getString("resourceid"));
	    		val.put(ContentDescriptor.Booking.Cols.ARRIVAL, date);
	    		val.put(ContentDescriptor.Booking.Cols.CLASSID, rs.getInt("classid"));
	    		val.put(ContentDescriptor.Booking.Cols.PARENTID, rs.getInt("parentid"));
	    		if (rs.getInt("parentid")> 0) {
	    			has_parent = true;
	    		} else {
	    			has_parent = false;
	    		}
	    		/** 
	    		 * last-update is equal to the last sync time, as using the database's last-update time can
	    		 * cause issues when the times don't match (e.g. if the last-update time on the database is ahead, it'll break thing).. 
	    		 */
	    		//val.put(ContentDescriptor.Booking.Cols.LASTUPDATE, this_sync); //HOW DO I FIX THIS?
	    		
	    		//get the offset for this booking;
	    		if (!cur.isClosed()) {
	    			cur.close();
	    		}
	    		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID+" = ?",
	    				new String[] {rs.getString("resourceid")}, null);
	    		if (cur.getCount() > 0) {
	    			cur.moveToFirst();
	    			val.put(ContentDescriptor.Booking.Cols.OFFSET, cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.PERIOD)));
	    		}
	    		cur.close();
	    		
	    		
	    		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID +" = "+rs.getString("bookingid"),
	    				null, null);
	    		if (cur.getCount() == 0) { //insert
	    			cur.close();
	    			contentResolver.insert(ContentDescriptor.Booking.CONTENT_URI, val);
//	    			timeid+=1;
		    		result +=1;
	    		} else { //update
	    			cur.close();
	    			int status = 0;
	    		
	    			contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI, ContentDescriptor.BookingTime.Cols.BID+" = ?",
	    					new String[] {rs.getString("bookingid")});
	    			//delete all it's registered times, we're rebuilding them anyway.
	    			
	    			status= contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, val, 
	    					ContentDescriptor.Booking.Cols.BID+" = ? ",
	    					new String[] {rs.getString("bookingid")});
	    			if (status == 0) {
	    				//update failed
	    				Log.e(TAG, "Booking Update Failed for id:"+rs.getString("bookingid"));
	    			}
	    			result +=status;
	    			/*if (rs.getInt("result") ==  5) { //redundant, we're deleting the bookingTimes above anyway.
	    				contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI, ContentDescriptor.BookingTime.Cols.BID+" = ?",
	    						new String[] {rs.getString("bookingid")});
	    			}*/
	    		}

	    		if (!has_parent) {
		    		timeid = starttime;
		    		while (timeid<=endtime) {
		    			val = new ContentValues();
		    			val.put(ContentDescriptor.BookingTime.Cols.BID, rs.getString("bookingid"));
		    			val.put(ContentDescriptor.BookingTime.Cols.RID, rs.getString("resourceid"));
		    			val.put(ContentDescriptor.BookingTime.Cols.TIMEID, timeid);
		    			val.put(ContentDescriptor.BookingTime.Cols.ARRIVAL, date);

		    			cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, "bt."+ContentDescriptor.BookingTime.Cols.BID+" = ? AND bt."
		    					+ContentDescriptor.BookingTime.Cols.RID+" = ? AND bt."+ContentDescriptor.BookingTime.Cols.TIMEID+" = ? AND bt."
		    					+ContentDescriptor.BookingTime.Cols.ARRIVAL+" = ?", new String[] {rs.getString("bookingid"), rs.getString("resourceid"),
		    					String.valueOf(timeid), date}, null);

		    			if (cur.getCount() == 0 ) { //insert
		    				cur.close();
		    				contentResolver.insert(ContentDescriptor.BookingTime.CONTENT_URI, val);
		    			} else { //update //Probably unneccisary, we're basically always inserting.
		    				cur.moveToFirst();
		    				int id = cur.getInt(cur.getColumnIndex(ContentDescriptor.BookingTime.Cols._ID));
		    				cur.close();
		    				contentResolver.update(ContentDescriptor.BookingTime.CONTENT_URI, val, ContentDescriptor.BookingTime.Cols._ID+" = ?",
		    						new String[] {String.valueOf(id)});
		    			}
		    			timeid +=1;
		    		}
		    	}
	    		if (!cur.isClosed()) {
	    			cur.close();
	    		}
	    	}
	    	rs.close();
    	} catch (SQLException e) {
    		Log.e(TAG, "", e);
    		statusMessage = e.getLocalizedMessage();
    	}
    	cleanUp();

    	Log.v(TAG, "BookingCount:"+result);
    	Services.setPreference(getApplicationContext(), "b_lastsync", String.valueOf(this_sync));
    	return result;
    }
    
    
	private int getBookingType(long last_sync){
    	Log.d(TAG, "GETTING BOOKING TYPES");
    	int result = 0;
    	ResultSet rs = null;

    	//final int CACI = 0; //TODO: create a settings variable that checks if we're using caci stuff or not.
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	try {
    		rs = connection.getBookingTypes(last_sync);
    		while (rs.next()) {
    				ContentValues values = new ContentValues();
    				values.put(ContentDescriptor.Bookingtype.Cols.BTID, rs.getString("id"));
    				values.put(ContentDescriptor.Bookingtype.Cols.NAME, rs.getString("name"));
    				values.put(ContentDescriptor.Bookingtype.Cols.PRICE, rs.getString("price"));
    				values.put(ContentDescriptor.Bookingtype.Cols.EXTERNAL, rs.getString("externalname"));
    				values.put(ContentDescriptor.Bookingtype.Cols.HISTORY, rs.getString("history"));
    				values.put(ContentDescriptor.Bookingtype.Cols.LASTUPDATE, rs.getTimestamp("lastupdate").getTime()*1000);
    				values.put(ContentDescriptor.Bookingtype.Cols.LENGTH, rs.getString("length"));
    				values.put(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN, rs.getString("maxintbetween"));
    				values.put(ContentDescriptor.Bookingtype.Cols.DESCRIPTION, rs.getString("description"));
    				values.put(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK, rs.getString("onlinebook"));
    				values.put(ContentDescriptor.Bookingtype.Cols.MS_ONLY, rs.getString("msh_onlybook"));
    				values.put(ContentDescriptor.Bookingtype.Cols.DEVICE_SIGNUP, "f");
    				
    				cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.BTID+" = ?",
    						new String[] {rs.getString("id")}, null);
    				if (cur.moveToFirst()) {
    					contentResolver.update(ContentDescriptor.Bookingtype.CONTENT_URI, values, 
    							ContentDescriptor.Bookingtype.Cols.BTID+" = ?", new String[] {rs.getString("id")});
    				} else {
    					contentResolver.insert(ContentDescriptor.Bookingtype.CONTENT_URI, values);
    				}
    				cur.close();
    				result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    	}
    	cleanUp();
    	Log.d(TAG, "GOT "+result+" BOOKING TYPES");
    	return result;
    }
    
    public static int getTime(String time, ContentResolver contentResolver, boolean end){
    	int result = 0;
    	
    	if (cur != null){
    		cur.close();
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME
				+" = '"+time+"' ", null, null);
		if (cur.getCount() == 0) {
			//statusMessage = "incorrect Time slot set, please visit application settings";
			return -1;
		}
		cur.moveToFirst();
		
		result = cur.getInt(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID));
		cur.close();
		if (!end) {
			return result;
		} else {
			return (result -1);
		}
		
    }
    
    private int getResultStatus(){
    	int result = 0;
    	ResultSet rs = null;
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	contentResolver.delete(ContentDescriptor.ResultStatus.CONTENT_URI, null, null);
    	try {
    		rs = connection.getResultStatus();
	    	while (rs.next()) {
	    		ContentValues values = new ContentValues();
	    		values.put(ContentDescriptor.ResultStatus.Cols.ID, rs.getString("id"));
	    		values.put(ContentDescriptor.ResultStatus.Cols.NAME, rs.getString("name"));
	    		values.put(ContentDescriptor.ResultStatus.Cols.COLOUR, rs.getString("bgcolour"));
	    		
	    		contentResolver.insert(ContentDescriptor.ResultStatus.CONTENT_URI, values);
	    		
	    		result +=1;
	    	}
	    	rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    	}
    	cleanUp();
    	return result;
    }
    
    
    private void setDate(){
    	Log.v(TAG, "Setting Date");
    	Calendar current, maximum;
    	Date date;
    	int currentDay;
    	ContentValues values;
    	
    	current = Calendar.getInstance();
    	current.set(Calendar.HOUR_OF_DAY, 0); 
		current.set(Calendar.MINUTE, 0);
		current.set(Calendar.SECOND, 0);
    	maximum = Calendar.getInstance();
    	maximum.set(Calendar.HOUR_OF_DAY, 0); 
		maximum.set(Calendar.MINUTE, 0);
		maximum.set(Calendar.SECOND, 0);
    	maximum.add(Calendar.MONTH, 1);
    	current.add(Calendar.MONTH, -1);
    	
    	//this query should hopefully make the date table dynamic. 
    	//though I may need to delete rows with dates < now() - '1 month' at some point.
    	cur = contentResolver.query(ContentDescriptor.Date.CONTENT_URI, null, null, null, null);
    	if (cur.moveToLast()) {
    		String maxdate = cur.getString(cur.getColumnIndex(ContentDescriptor.Date.Cols.DATE));
    		SimpleDateFormat format2 = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    		try {
    			date = format2.parse(maxdate);
    		} catch (ParseException e) {
    			date = null;
    		}
    		if (date != null) {
    			current.setTime(date);
    		}
    	}
    	cur.close();
    	
		while (current.getTimeInMillis() < maximum.getTimeInMillis()) {
			date = current.getTime();
			currentDay = current.get(Calendar.DAY_OF_WEEK);
			values = new ContentValues();
			values.put(ContentDescriptor.Date.Cols.DATE, Services.DateToString(date));
			values.put(ContentDescriptor.Date.Cols.DAYOFWEEK, currentDay);
			contentResolver.insert(ContentDescriptor.Date.CONTENT_URI, values);
			current.add(Calendar.DATE, 1);
		}
		Log.v(TAG, "Finished Setting Date");
    }
    
    private void setTime(){//TODO: fix the delete time issue.
    	Log.v(TAG, "Setting Time!!");
		contentResolver.delete(ContentDescriptor.Time.CONTENT_URI, null, null); 
		int interval;
		Calendar day, upperlimit, lowerlimit;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
		interval = 15; //DEFAULT INTERVER = 15.
		day = Calendar.getInstance();
		day.add(Calendar.DATE, -1);
		
		upperlimit = Calendar.getInstance();
		upperlimit.set(Calendar.HOUR_OF_DAY, 23);
		upperlimit.set(Calendar.MINUTE, 59);
		upperlimit.set(Calendar.SECOND, 1);
		
		lowerlimit = Calendar.getInstance();
		lowerlimit.set(Calendar.HOUR_OF_DAY, 0); 
		lowerlimit.set(Calendar.MINUTE, 0);
		lowerlimit.set(Calendar.SECOND, 0);
		
			while (lowerlimit.getTime().before(upperlimit.getTime())) {
				ContentValues values;
				String time;
				
				values = new ContentValues();
				time = format.format(lowerlimit.getTime());		
				values.put(ContentDescriptor.Time.Cols.TIME, time);
				contentResolver.insert(ContentDescriptor.Time.CONTENT_URI, values);
				lowerlimit.add(Calendar.MINUTE, interval);
			}
    }
    
    private int getMember(long last_sync){
    	Log.v(TAG, "Getting Member's");
    	int result;
    	ResultSet rs;
    	
    	result = 0;
    	rs = null;

    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	int use_roll = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "use_roll"));
    	try {
    		
    		if (last_sync == -1) {
    			if (use_roll > 0) {
    				rs = connection.getYMCAMembers(null);
    			}else {
    				rs = connection.getMembers(null);
    			}
    		} else {
    			if (use_roll > 0) {
    				rs = connection.getYMCAMembers(String.valueOf(last_sync));
    			}else {
    				rs = connection.getMembers(String.valueOf(last_sync));
    			}
    		}
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values = insertMember(rs, use_roll);
    			
    			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.MID+" = ?",
    					new String[] {rs.getString("id")}, null);
    			if (cur.getCount() > 0) {
    				cur.close();
    				contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols.MID+" = ?",
    						new String[] {rs.getString("id")});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.Member.CONTENT_URI, values);
    			}
    			
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e){
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    	}
    	cleanUp();
    	
    	return result;
    }
    
    private int getMembership(long last_sync) {
    	Log.v(TAG, "Getting Memberships");
    	int result = 0;
    	ResultSet rs = null;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	try {
    		int use_roll = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "use_roll"));
    		rs = connection.getMembership(String.valueOf(last_sync), use_roll);
    			
    		while (rs.next()){
    			ContentValues values = insertMembership(rs);
    			values.put(ContentDescriptor.Membership.Cols.CANCEL_REASON, rs.getString("cancel_reason"));
    			
    			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MSID+" = ?",
    					new String[] {rs.getString("id")},null);
    			if (cur.getCount()> 0) { //update
    				cur.close();
    				contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, ContentDescriptor.Membership.Cols.MSID+" = ?",
    						new String[] {rs.getString("id")});
    			} else { //insert
    				contentResolver.insert(ContentDescriptor.Membership.CONTENT_URI, values);
    				cur.close();
    			}
    			//cur.close();
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "Membership Error:", e);
    	}

    	cleanUp();
    	return result;
    }
    
    private int swipe(){
    	//TODO: swap to use PendingUploads table.
    	//test changes.
    	int result = 0, door;
    	String id;
    	
    	cur = contentResolver.query(ContentDescriptor.Swipe.CONTENT_URI, null, null, null, null);
    	System.out.print("\n\n## of Swipes:"+cur.getCount());
	
    	while (cur.moveToNext()) {    		
    		id = cur.getString(cur.getColumnIndex(ContentDescriptor.Swipe.Cols.ID));
    		door = cur.getInt(cur.getColumnIndex(ContentDescriptor.Swipe.Cols.DOOR));
    		
    		if (door < 0 ) {
    			//it's a booking swipe, ignore it.
    			Log.v("NFCActivity", "DOOR < 0");
    			continue;
    		}
    		Log.v("NFCActivity", "id:"+id);
    		Log.v("NFCActivity", "door:"+door);
    		if (!openConnection()) {
    			contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.DOOR+" = ? AND "
    					+ContentDescriptor.Swipe.Cols.ID+" = ?", new String[] {String.valueOf(door), id});
    			
    			statusMessage = "Connection to database failed, Member not swiped in ";
    			Log.e(TAG, statusMessage+" id:"+id);
        		//return -1; //connection failed;
    			continue;
        	}
    		try {
    			ResultSet rs;
    			String tempmess;
    			
	    		rs = connection.uploadTag(door, id);
	    		rs.close();
	    		connection.closePreparedStatement();
	    		connection.getSwipeProcessLog();
	    		connection.closePreparedStatement();
	    		rs = connection.getTagUpdate(door);
	    		tempmess = null;
	    		if (rs.next()) {
	    			tempmess = rs.getString("message")+" "+rs.getString("message2");
	    			statusMessage = tempmess;
	    			Log.v(TAG, tempmess);
	    			break;
	    		}	
	    		rs.close();
	    		connection.closePreparedStatement();
		    	
	    	} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		Log.e(TAG, "", e);
	    	}
    		cleanUp();
    	}
    	cur.close();
    	contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, null, null);
		
    	return result;
    }
    
    private int getOpenHours(long last_sync){
    	Log.v(TAG, "Getting Open Hours");
    	int result = 0;
    	ResultSet rs = null;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	try {
    		rs = connection.getOpenHours(last_sync);
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.OpenTime.Cols.DAYOFWEEK, (rs.getInt("dayofweek")+1));
    			values.put(ContentDescriptor.OpenTime.Cols.OPENTIME, rs.getString("opentime"));
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSETIME, rs.getString("closetime"));
    			values.put(ContentDescriptor.OpenTime.Cols.NAME, rs.getString("name"));
    			
    			cur = contentResolver.query(ContentDescriptor.OpenTime.CONTENT_URI, null, ContentDescriptor.OpenTime.Cols.DAYOFWEEK+" = ?",
    					new String[] {String.valueOf(rs.getInt("dayofweek")+1)}, null);
    			if (cur.moveToFirst()) {
    				cur.close();
    				contentResolver.update(ContentDescriptor.OpenTime.CONTENT_URI, values, ContentDescriptor.OpenTime.Cols.DAYOFWEEK+" = ?",
    						new String[] {String.valueOf(rs.getInt("dayofweek")+1)});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.OpenTime.CONTENT_URI, values);
    			}
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "" , e);
    	}
    	cleanUp();
    	Log.d(TAG, "UPDATED "+result+" OPEN HOURS");
    	return result;
    }
    
    /**
     * This function sets the start id and end id that closest match our opening and closing times.
     * It should be called immediately after every getOpenHours(), but it only works if the date and time tables are set;
     * @return
     */
    private int updateOpenHours(){
    	Log.v(TAG, "Updating Open Hours");
    	int result;
    	ArrayList<String[]> idList;
    	ContentValues values;
    	
    	result = 0;
    	idList = new ArrayList<String[]>();    	
    	cur = contentResolver.query(ContentDescriptor.OpenTime.CONTENT_URI, null, null, null, null);
    	
    	while (cur.moveToNext()) {
    		/*for (int l=0; l<cur.getColumnCount(); l+=1){
    			System.out.print("\n\nColumn:"+cur.getColumnName(l)+" Value:"+cur.getString(l));
    		}*/
    		String[] day = new String[3];
    		day[0] = cur.getString(cur.getColumnIndex(ContentDescriptor.OpenTime.Cols._ID));
    		day[1] = (cur.isNull(cur.getColumnIndex(ContentDescriptor.OpenTime.Cols.OPENTIME)))? "-1" 
    				: cur.getString(cur.getColumnIndex(ContentDescriptor.OpenTime.Cols.OPENTIME)); // ?
    		day[2] = (cur.isNull(cur.getColumnIndex(ContentDescriptor.OpenTime.Cols.CLOSETIME)))? "-1" 
    				: cur.getString(cur.getColumnIndex(ContentDescriptor.OpenTime.Cols.CLOSETIME));
    		idList.add(day);
    	}
    	
    	for (int i=0; i<idList.size(); i +=1) {
    		values = new ContentValues();

    		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
			new String[] {idList.get(i)[1]}, null); //find the time row that matches our start time.
    		
    		if (idList.get(i)[1].compareTo("-1") == 0) {
    			//no starttime/endtime set for this day, what should I do?
    			Log.v(TAG, "NO STARTTIME SET***");
    			cur.close();
    			values.put(ContentDescriptor.OpenTime.Cols.OPENID, 0);
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, 0);
    		}
    		else if (cur.getCount() == 0) {
    			cur.close(); //find the nearest time to our start time.
    			cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" >= ?",
    					new String[] {idList.get(i)[1]}, ContentDescriptor.Time.Cols.TIME+" ASC LIMIT 1");
    			cur.moveToFirst();

        		values.put(ContentDescriptor.OpenTime.Cols.OPENID, cur.getString(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))); 
        		cur.close();
        		
        		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
    			new String[] {idList.get(i)[2]}, null);
    			if (cur.getCount() <= 0) {
    				cur.close();
    				cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, null, null, ContentDescriptor.Time.Cols.ID+" DESC");
    			}
    			cur.moveToFirst();
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, (cur.getInt(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))-1));
    			cur.close();
    		}  else {
    			cur.moveToFirst();
        		values.put(ContentDescriptor.OpenTime.Cols.OPENID, cur.getString(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))); 
        		cur.close();
        		
        		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
    			new String[] {idList.get(i)[2]}, null);
    			if (cur.getCount() <= 0) {
    				cur.close();
    				cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, null, null, ContentDescriptor.Time.Cols.ID+" DESC");
    			}
    			cur.moveToFirst();
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, (cur.getInt(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))-1));
    			cur.close();    			
    		}
    		contentResolver.update(ContentDescriptor.OpenTime.CONTENT_URI, values, ContentDescriptor.OpenTime.Cols._ID+" = ?", 
					new String[] {idList.get(i)[0]});
    	}
    	
    	return result;
    }
    
    //I should really be getting the class ID's before I attempt to upload.
    //That way I can signup members prior to uploading the class.
    private int uploadClass(){
    	Log.d(TAG, "Uploading Classes");
    	int result = 0;
    	ArrayList<String> idlist;
    	
    	idlist = new ArrayList<String>();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Class.getKey())}, null);
    	int count = 0;
    	while (cur.moveToNext())
    	{
    		idlist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    		count +=1;
    	}
    	cur.close();
    	Log.d(TAG, "ATTEMPTING TO UPLOAD "+count+" CLASSES");
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	for (int i = 0; i < idlist.size(); i +=1) {
    		String name, freq, sdate, stime, etime;
    		int cid, rid, max_st, rowid;
    		ResultSet rs;
    		Log.v(TAG, "getting first Upload");
    		cur = contentResolver.query(ContentDescriptor.Class.CONTENT_URI, null, ContentDescriptor.Class.Cols._ID+" = ?",
    				new String[] {idlist.get(i)}, null);
    		
    		cur.moveToFirst();
    		rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols._ID));
    		name = cur.getString(cur.getColumnIndex(ContentDescriptor.Class.Cols.NAME));
    		max_st = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols.MAX_ST));
    		
    		sdate = cur.getString(cur.getColumnIndex(ContentDescriptor.Class.Cols.SDATE));
    		stime = cur.getString(cur.getColumnIndex(ContentDescriptor.Class.Cols.STIME));
    		etime = cur.getString(cur.getColumnIndex(ContentDescriptor.Class.Cols.ETIME));
    		
    		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Class.Cols.FREQ))) {
    			freq = cur.getString(cur.getColumnIndex(ContentDescriptor.Class.Cols.FREQ));
    		} else {
    			freq = null;
    		}
    		
    		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Class.Cols.RID))) {
    			rid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Class.Cols.RID));
    		} else {
    			rid = -1;
    		}
    		
    		cur.close();
    		
    		Log.v(TAG, "Uploading Class Now");
    		try {
    			rs = connection.uploadClass(name, max_st);
    			rs.next(); //move to first
        		cid = rs.getInt(1);
        		rs.close();
    		} catch (SQLException e) {
    			//error occured with sql on upload class.
    			Log.e(TAG, "", e);
    			statusMessage = e.getLocalizedMessage();
    			return -2;
    		}
    		
    		connection.closePreparedStatement();
    		
    		Log.v(TAG, "Uploading Recurrence Now");
    		try {
    			int a = connection.uploadRecurrence(freq, sdate, stime, etime, cid, rid);
    			Log.v(TAG, "Recurrence Result:"+a);
    		} catch (SQLException e) {
    			//error occured with sql on upload recurring;
    			Log.v(TAG, "ERROR OCCURED");
    			Log.e(TAG, "", e);
    			statusMessage = e.getLocalizedMessage();
    			return -3;
    		}
    		Log.v(TAG, "Doing Non-Networky Stuff");
    		//if we got here the inserts must've been successful. so remove the pending upload.
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Class.getKey()),
    					String.valueOf(rowid)});
    		//and update the class with it's id.
    		ContentValues values = new ContentValues();
    		values.put(ContentDescriptor.Class.Cols.CID, cid);
    		values.put(ContentDescriptor.Class.Cols.DEVICESIGNUP, "f");
    		contentResolver.update(ContentDescriptor.Class.CONTENT_URI, values, ContentDescriptor.Class.Cols._ID+" = ?",
    				new String [] {String.valueOf(rowid)});
    		
    		result += 1;
    	}
    	cleanUp();
    	
    	return result;
    }
    /**
     * after getting bookings, we need to get classes.
     * when swiping in tags for classes, we need to check that
     * the class member limit has not been reached.
     * @return
     */
    
    private int getClasses(long last_sync) {
    	Log.v(TAG, "Getting Classes");
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	ResultSet rs = null;
    	try {
    		rs = connection.getClasses(String.valueOf(last_sync));
    		//do handling here.
    		while (rs.next()){
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.Class.Cols.CID, rs.getString("id"));
    			values.put(ContentDescriptor.Class.Cols.NAME, rs.getString("name"));
    			values.put(ContentDescriptor.Class.Cols.DESC, rs.getString("description"));
    			values.put(ContentDescriptor.Class.Cols.MAX_ST, rs.getString("max_students"));
    			values.put(ContentDescriptor.Class.Cols.PRICE, rs.getString("price"));
    			values.put(ContentDescriptor.Class.Cols.MULTIBOOK, rs.getString("multiplebookings"));
    			if (rs.getString("onlinebook").compareTo("t") ==0) {
    				values.put(ContentDescriptor.Class.Cols.ONLINE, 1);
    			} else {
    				values.put(ContentDescriptor.Class.Cols.ONLINE, 0);
    			}
    			
    			cur = contentResolver.query(ContentDescriptor.Class.CONTENT_URI, null, ContentDescriptor.Class.Cols.CID+" = ?",
    					new String[] {rs.getString("id")}, null);
    			if (cur.getCount() > 0) {
    				//update!
    				contentResolver.update(ContentDescriptor.Class.CONTENT_URI, values, ContentDescriptor.Class.Cols.CID+" = ?", 
    						new String[] {rs.getString("id")});
    			} else {
    				//insert!
    				contentResolver.insert(ContentDescriptor.Class.CONTENT_URI, values);
    			}
    			cur.close();
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	cleanUp();
    	
    	return result;
    }
    
    /** we need to look up the serial in the database,
	 * find the member associated with it (if there is one),
	 * and then either: Add said member to the list
	 * 				or Check the box for the member if they're already in the list.
	*/
    private int classSwipe(){
    	int result = 0;
    	int bookingswipecount = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.Swipe.CONTENT_URI, null, ContentDescriptor.Swipe.Cols.DOOR+" < 0",
    			null, null);
    	bookingswipecount = cur.getCount();
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	Log.e(TAG, "Class Swipe Count:"+bookingswipecount);
    	while (bookingswipecount > 0) {
    		
    		String serial, memberid, classid, membershipid;
    		int cardno;
    		ResultSet rs;
    		cur.close();
    		if (cur == null || cur.isClosed()) {
    			cur = contentResolver.query(ContentDescriptor.Swipe.CONTENT_URI, null, ContentDescriptor.Swipe.Cols.DOOR+" < 0",
    	    			null, null);
    		}
    		
    		cur.moveToFirst();
    		if (cur.getCount() <= 0) {
    			Log.e(TAG, "Lost the Cursor.");
    			return 0;
    		}
    		classid = cur.getString(cur.getColumnIndex(ContentDescriptor.Swipe.Cols.DOOR)).substring(1);
    		serial = cur.getString(cur.getColumnIndex(ContentDescriptor.Swipe.Cols.ID)); 
    		cur.close();
    		//rs = connection.findMemberBySerial(serial);
    		//select id FROM idcard where serial = 'Mx1bc34e';
    		//select memberid from membership where cardno = 168;
    		try {
    			cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols.SERIAL+" = ?",
    					new String[] {serial}, null);
    			Log.v(TAG, "FindCard By Serial Size:"+cur.getCount());
    			
    			if (!cur.moveToFirst()) {
    				//something went wrong, (sometimes the serial is just an int?)
    				//I should probably delete the serial from the swipe table.
    				statusMessage = "tag not found in local database, try long syncing to fix.";
    				cur.close();
    				contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ? ",
    						new String[] {serial});
    				//return -4; //only 1 row. ??
    				continue;
    			}
    		
    			//cardno = rs.getInt("id");
    			cardno = cur.getInt(cur.getColumnIndex(ContentDescriptor.IdCard.Cols.CARDID));
    			cur.close();
    			
    			rs = connection.getMemberByCard(cardno);
    			if (! rs.next()) {
    				Log.e(TAG, "Count not find member for card no:"+cardno);
    				contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ? ",
    						new String[] {serial});
    				//return -4; //only 1 row. ??
    				continue;
    			}
    			
    			memberid = rs.getString("memberid");
    			membershipid = rs.getString("membershipid");
    			Log.v(TAG,"Class-Swipe Member-ID:"+memberid);
    			if (rs.wasNull()) {
    				Log.e(TAG, "Tag not assigned to a member");
    				statusMessage = "tag not assigned to a member";
    				contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ?",
    						new String[] {serial});
    				continue;
    			}
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -1;
    		}
    		connection.closePreparedStatement();
			
			cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.PARENTID+" = ? AND "
					+ContentDescriptor.Booking.Cols.MID+" = ?",
					new String[] {classid, memberid}, null);
    		if (cur.getCount() <= 0) {
    			// the member is NOT already booked in for the class,
    			// the function should add them.
    			Log.v(TAG, "Adding New Member to the booking.");
    			
    			String resourceid, startid, endid, stime, etime, arrival, firstname, lastname, offset;
    			int bookingid;
    			ContentValues values;
    			long tenminutes = 600000;
    			Date start = null;
    			SimpleDateFormat format;
    			cur.close(); //nothing in the cursor anyway.
    			
    			cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" = ?",
    					new String[] {classid}, null);
    			cur.moveToFirst(); //should never fail;
    			
    			resourceid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RID));
    			startid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIMEID));
    			stime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
    			endid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIMEID));
    			etime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME));
    			arrival = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
    			offset = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET));
    			cur.close();
    			
    			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.MID+" = ?",
    					new String[] {memberid}, null);
    			cur.moveToFirst();
    			
    			firstname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME));
    			lastname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME));
    			
    			cur.close();
    			
    			//get a bookingid from somewhere ?
    			int rowid = -1;
    			cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATE+" = 0",
    					null, null);
    			if (cur.getCount() > 0) {
    				cur.moveToFirst();
    				bookingid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
    				rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ID));
    			} else {
    				//we haven't got any spare booking-id's. what should I do?
    				bookingid = -1;
    			}
    			
    			values = new ContentValues();
    			values.put(ContentDescriptor.Booking.Cols.RID, resourceid);
    			values.put(ContentDescriptor.Booking.Cols.PARENTID, classid);
    			values.put(ContentDescriptor.Booking.Cols.STIME, stime);
    			values.put(ContentDescriptor.Booking.Cols.STIMEID, startid);
    			values.put(ContentDescriptor.Booking.Cols.ETIME, etime);
    			values.put(ContentDescriptor.Booking.Cols.ETIMEID, endid);
    			values.put(ContentDescriptor.Booking.Cols.ARRIVAL, arrival);
    			values.put(ContentDescriptor.Booking.Cols.FNAME, firstname);
    			values.put(ContentDescriptor.Booking.Cols.SNAME, lastname);
    			values.put(ContentDescriptor.Booking.Cols.BID, bookingid);
    			values.put(ContentDescriptor.Booking.Cols.CHECKIN, new Date().getTime());
    			values.put(ContentDescriptor.Booking.Cols.MID, memberid);
    			values.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, 0); //class attendant has id 0, this probably shouldn't be hard-coded.
    			values.put(ContentDescriptor.Booking.Cols.MSID, membershipid);
    			values.put(ContentDescriptor.Booking.Cols.OFFSET, offset);
    			
    			format = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.US);
    			try {
    				start = format.parse(arrival+" "+stime);
    			} catch (ParseException e) {
    				//shouldn't occur, all dates & times are formated before insertion into SQLite;
    				Log.e(TAG, "class-swipe error arrival & stime not formatted correctly");
    				//throw new RuntimeException(e);
    			}
    			if (new Date().getTime() > (start.getTime()+tenminutes)){
    				//we're late!
    				values.put(ContentDescriptor.Booking.Cols.RESULT, 21);
    			} else {
    				//we're on time!
    				values.put(ContentDescriptor.Booking.Cols.RESULT, 20);
    			}
    			values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
    			
    			
    			if (bookingid > 0) {
    				contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
    						new String[] {String.valueOf(bookingid)});
    				values = new ContentValues();
        			values.put(ContentDescriptor.PendingUploads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Booking.getKey());
        			values.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);
    			} else { //insert;
    				contentResolver.insert(ContentDescriptor.Booking.CONTENT_URI, values);
    			}
    			
    			
    		} else {
    			Log.v(TAG, "Checking in Existing Member");
    			cur.moveToFirst();
    			
    			//the member exists, update there stuff.
    			String bookingid, sdate, stime;
    			ContentValues values;
    			long tenminutes = 600000;
    			Date now, start = null;
    			SimpleDateFormat format;
    			
    			format = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.US);
    			stime = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME));
    			sdate = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL));
    			try {
    				start = format.parse(sdate+" "+stime);
    			} catch (ParseException e) {
    				//e.printStackTrace();
    			}
    			now = new Date();
    			
    			bookingid = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
    			cur.close();
    			Log.v(TAG, "Class Swipe, updating for BookingID:"+bookingid);
    			
    			values = new ContentValues();
    			if (now.getTime() > (start.getTime()+ tenminutes)) { //more than 10 minutes late.
    				values.put(ContentDescriptor.Booking.Cols.RESULT, 21);
    			} else {
    				values.put(ContentDescriptor.Booking.Cols.RESULT, 20);
    			}
    			values.put(ContentDescriptor.Booking.Cols.CHECKIN, new Date().getTime());
    			values.put(ContentDescriptor.Booking.Cols.LASTUPDATE, new Date().getTime());
    		
    			contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?", 
    					new String[] {bookingid});
    		}
    		
    		//send broadcast here.
    		Log.v(TAG, "Sending Class-Swipe Broadcast");
    		Intent bcIntent = new Intent();
			bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
			bcIntent.putExtra(Services.Statics.IS_CLASSSWIPE, classid);
			sendBroadcast(bcIntent);
			
			contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ? AND "
					+ContentDescriptor.Swipe.Cols.DOOR+" = ?", new String[] {serial, "-"+classid});
			
    		result +=1;
    		cur.close();
    		if (cur == null || cur.isClosed()) {
    			cur = contentResolver.query(ContentDescriptor.Swipe.CONTENT_URI, null, ContentDescriptor.Swipe.Cols.DOOR+" < 0",
    	    			null, null);
    	    	bookingswipecount = cur.getCount();
    	    	cur.close();
    		}
    	}
    	if (cur != null && !cur.isClosed()) {
    		cur.close();
    	}
    	cleanUp();
    	
    	return result;
    }
    
    private boolean openConnection(){
    	boolean connected = false;
    	if (connection != null) {
			connected = connection.isConnected();
    	}
    	if (!connected) {
	    	try {
	    		if (connection == null) {
	    			//connection = new JDBCConnection(getApplicationContext());
	    		}
	    		connection.openConnection();
	    		connected = connection.isConnected();
	    	} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		e.printStackTrace();
	    		Services.showToast(getApplicationContext(), statusMessage, handler);
	    		connected = false;
	    	} catch (ClassNotFoundException e) {
	    		//Postgresql JDBC driver missing!!
	    		//if we got here there was an issue with the installation.
	    		throw new RuntimeException(e);
	    	}
    	}
    	return connected;
    }
    
    private void cleanUp() {
    	/*if (cur != null && !cur.isClosed()) {
    		cur.close();
    		cur = null;
    	}*/
    	connection.closeStatementQuery();
    	connection.closePreparedStatement();
    	//connection.closeConnection();
    }
    
    public String getStatus() {
    	return this.statusMessage;
    }
    
    private int getSuspendID(){
    	Log.v(TAG, "Getting Suspend IDs");
    	int result = 0; 
    	String query = "select nextval('membership_suspend_id_seq');";
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	/*cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, 
    			ContentDescriptor.MembershipSuspend.Cols.MID+" = 0", null, null);*/
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey(), null, null);
    	int free_count = cur.getCount(); // = cur.count() where mid = 0;
    	cur.close();
    	
    	cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null,
    			ContentDescriptor.MembershipSuspend.Cols.SID+" < 0", null, null);
    	int need_count = cur.getCount();
    	cur.close();
    	
    	ResultSet rs;
    	
    	for (int i = ((20+need_count)-free_count); i > 0; i -=1) {   	
	    	try {
	    		rs = connection.startStatementQuery(query);
	    		rs.next();
	    		//Handle the insertion.
	    		ContentValues values = new ContentValues();
	    		
	    		
	    		if (need_count > 0) { //we've got pending suspends that need an id before upload.
	    			values.put(ContentDescriptor.MembershipSuspend.Cols.SID, rs.getString("nextval"));
	    			cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null,
	    	    			ContentDescriptor.MembershipSuspend.Cols.SID+" < 0", null, null);
	    			cur.moveToFirst();
	    			int memberid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.MID));
	    			cur.close();
	    			
	    			contentResolver.update(ContentDescriptor.MembershipSuspend.CONTENT_URI, values,
	    					ContentDescriptor.MembershipSuspend.Cols.MID+" = ?", new String[] {String.valueOf(memberid)});
	    			need_count -=1;
	    		} else {
	    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
	    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID,
	    					ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey());
	    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
	    		}
	    		rs.close();
	    	} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		Log.e(TAG, "", e);
	    		return -2;
	    	}
	    	result +=1;
	    	connection.closeStatementQuery();
    	}
    	
    	cleanUp();
    	return result;
    }
    
    //should this get historic suspends?
    //
    private int getMembershipSuspends(long last_sync) {
    	Log.v(TAG, "Getting Membership Suspends");
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1;
    		//see statusMessage for details;
    	}
    	ResultSet rs;
    	try {
    		rs = connection.getSuspends(last_sync);
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG,"", e);
    		return -2;
    	}
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.MembershipSuspend.Cols.SID, rs.getString("id"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.MID, rs.getString("memberid"));

    			values.put(ContentDescriptor.MembershipSuspend.Cols.STARTDATE, 
    					Services.DateToString(new Date(rs.getDate("startdate").getTime())));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.LENGTH, rs.getString("howlong"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.REASON, rs.getString("reason"));

    			values.put(ContentDescriptor.MembershipSuspend.Cols.ENDDATE, 
    					Services.DateToString(new Date(rs.getDate("edate").getTime())));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.SUSPENDCOST, rs.getString("suspendcost"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.ONEOFFFEE, rs.getString("oneofffee"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.ALLOWENTRY, rs.getString("allowentry"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.EXTEND_MEMBERSHIP, rs.getString("extend_membership"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.PROMOTION, rs.getString("promotion"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.FULLCOST, rs.getString("fullcost"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.PRORATA, rs.getString("prorata"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.FREEZE, rs.getString("freeze_fees"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE, rs.getString("holdfee"));
    			values.put(ContentDescriptor.MembershipSuspend.Cols.ORDER, rs.getDate("startdate").getTime());
    			
    			cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, 
    					ContentDescriptor.MembershipSuspend.Cols.SID+" = ?", new String[] {rs.getString("id")}, null);
    			if (cur.moveToFirst()) {
    				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols._ID));
    				cur.close();
    				contentResolver.update(ContentDescriptor.MembershipSuspend.CONTENT_URI, values, 
    						ContentDescriptor.MembershipSuspend.Cols._ID+" = ?", new String[] {String.valueOf(rowid)});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.MembershipSuspend.CONTENT_URI, values);
    			}
    			
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -3;
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    private int uploadSuspends(){
    	Log.v(TAG, "Uploading Suspends");
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	ArrayList<Integer> rows = new ArrayList<Integer>();
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey())}, null);
    	while (cur.moveToNext()) {
    		rows.add(cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	for (int i = 0; i < rows.size(); i +=1) {
    		cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, 
    				ContentDescriptor.MembershipSuspend.Cols._ID+" = ?", new String[] {String.valueOf(rows.get(i))}, null);
    		if (!cur.moveToFirst()) {
    			statusMessage = "an Error Occured: MembershipSuspend could not find row";
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
	    				ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
	    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
	    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
	    				String.valueOf(rows.get(i))});
    			continue;
    		}
    		String mid, msid, sid, reason, start, freeze, end, 
    		suspendcost, oneofffee, allowentry, extend_membership, promotion, fullcost, holdfee = null, prorata;
  
    		mid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.MID));
   			msid = null;
    		sid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.SID));
    		reason = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.REASON));
    		start = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.STARTDATE));
    		freeze = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.FREEZE));
    		end = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ENDDATE));
    		suspendcost = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.SUSPENDCOST));
    		oneofffee = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ONEOFFFEE));
    		allowentry = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ALLOWENTRY));
    		extend_membership = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.EXTEND_MEMBERSHIP));
    		promotion = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.PROMOTION));
    		fullcost = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.FULLCOST));
    		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE))) {
    			holdfee = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE));
    		} 
    		prorata = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.PRORATA));
    		
    		try {
    			connection.uploadSuspend(sid, mid, msid, start, end, reason, freeze,
    					suspendcost, oneofffee, allowentry, extend_membership, promotion, fullcost, holdfee, prorata);
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			
    			/* Realistically if we're hitting any of these, 
    			 * we can probably delete the membership hold as well.
    			 * As it'll never get uploaded or used.
    			 */
    			if (statusMessage.compareTo(Services.Statics.ERROR_MSHOLD1) ==0) {
    				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    	    				ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    	    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    	    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
    	    				String.valueOf(rows.get(i))});
    				Services.showToast(getApplicationContext(), "Hold time set beyond membership start/end.", handler);
    				continue;
    			} else if (statusMessage.contains(Services.Statics.ERROR_MSHOLD2)) {
    				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    	    				ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    	    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    	    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
    	    				String.valueOf(rows.get(i))});
    				Services.showToast(getApplicationContext(), "Member already on Hold.", handler);
    				continue;
    			} else if (statusMessage.contains(Services.Statics.ERROR_MSHOLD3)) {
    				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    	    				ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    	    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    	    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
    	    				String.valueOf(rows.get(i))});
    				continue;
    			} else {
    				return -3;
    			}
    		}
    		//remove it from the pendingUploads table.
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    				ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
    				String.valueOf(rows.get(i))});
    	}
    	cleanUp();
    	return result;
    }
    
    private int updateSuspend(String rowid) {
    	int result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, ContentDescriptor.MembershipSuspend.Cols._ID+" = ?",
    			new String[] {rowid}, null);
    	if (!cur.moveToFirst()) {
    		return 0;
    	}
    	String mid, sid, reason, start, freeze, end, 
		suspendcost, oneofffee, allowentry, extend_membership, promotion, fullcost, holdfee = null, prorata;
    	mid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.MID));
		sid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.SID));
		reason = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.REASON));
		start = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.STARTDATE));
		freeze = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.FREEZE));
		end = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ENDDATE));
		suspendcost = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.SUSPENDCOST));
		oneofffee = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ONEOFFFEE));
		allowentry = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ALLOWENTRY));
		extend_membership = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.EXTEND_MEMBERSHIP));
		promotion = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.PROMOTION));
		fullcost = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.FULLCOST));
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE))) {
			holdfee = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.HOLDFEE));
		} 
		prorata = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.PRORATA));
		
		
		
		try {
			connection.updateSuspend(Integer.parseInt(mid), start, end, reason, freeze, suspendcost, 
					oneofffee, allowentry, extend_membership, promotion, fullcost, holdfee, prorata, Integer.parseInt(sid));
			result +=1;
		} catch (SQLException e) {
			Log.e(TAG, "", e);
			statusMessage = e.getLocalizedMessage();
			return -1;
		}
		
		return result;
    }
    
    private int getIdCardID() {
    	Log.v(TAG, "Getting Idcard ID's");
    	int result = 0;
    	ResultSet rs;
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())}, null);
    	
    	int free_count = cur.getCount();
    	cur.close();
    	
    	cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols.CARDID+" <= 0", null, null);
    	int needed_count = cur.getCount();
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	for (int i=((15+needed_count)-free_count); i> 0; i--) {
    		try {
    			rs = connection.startStatementQuery("select nextval('idcard_id_seq');");
    			rs.next();
    			
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Idcard.getKey());
    			
    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			
    			rs.close();
    			connection.closeStatementQuery();
    			
    			result +=1;
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		} catch (NullPointerException e) {
    			return -3; //we haven't got a connection.
    		}
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    //TODO: make sure we've tried to do this before assigning a tag every time.
    public int getIdCards(long last_sync, Context context) {
    	Log.v(TAG, "Getting ID Cards");
    	int result = 0;
    	ResultSet rs;
    	
    	if (context != null) {
    		setup(context);
    	}
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	try {
    		rs = connection.getIdCards(last_sync);
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	// presumably our connection was a success, in which case clear the idcard cache and re-add all the data.
    	// this would be more efficient if the idcard table had a timestamp column.
    	//contentResolver.delete(ContentDescriptor.IdCard.CONTENT_URI, null, null);
    	
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.IdCard.Cols.CARDID, rs.getString("id"));
    			values.put(ContentDescriptor.IdCard.Cols.SERIAL, rs.getString("serial"));
    			//TODO: created column
    			contentResolver.insert(ContentDescriptor.IdCard.CONTENT_URI, values);
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -3;
    	}
    	cleanUp();
    	
    	return result;
    }
    
    private int uploadIdCard() {
    	Log.v(TAG, "inserting ID cards");
    	int result = 0;
    	ArrayList<String> rowid = new ArrayList<String>();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())}, null);
    	while (cur.moveToNext()) {
    		rowid.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	Log.v(TAG, "Attempting to upload "+rowid.size()+" ID's");
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	for (int i=0; i<rowid.size(); i++) {
    		Log.v(TAG, "Uploading: "+i+" Value: "+rowid.get(i));
    		cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols._ID+" = ?",
    				new String[] {rowid.get(i)}, null);
    		if (!cur.moveToFirst()) {
    			Log.v(TAG, "Card not found in ID table");
    			cur.close();
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {rowid.get(i), 
    					String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())});
    			continue;
    		}
			
    		try {
	    		result += connection.uploadIdCard(cur.getInt(cur.getColumnIndex(ContentDescriptor.IdCard.Cols.CARDID)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.IdCard.Cols.SERIAL)));
    		} catch (SQLException e) {
    			Log.e(TAG, "ERROR OCCURED");;
    			Log.e(TAG, e.getSQLState());
    			if (e.getSQLState().contains("42710")) {
    				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
        					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {rowid.get(i), 
        					String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())});
    			}
    		}
    		cur.close();
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {rowid.get(i), 
					String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())});
    		connection.closePreparedStatement();
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    private int getPaymentMethods() {
    	Log.v(TAG, "Getting Payment Methods");
    	int result = 0;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	try {
    		rs = connection.getPaymentMethods();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.PaymentMethod.Cols.PAYMENTID, rs.getString("id"));
    			values.put(ContentDescriptor.PaymentMethod.Cols.NAME, rs.getString("name"));
    			
    			contentResolver.insert(ContentDescriptor.PaymentMethod.CONTENT_URI, values);
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -3;
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int getProgrammes(long last_sync) {
    	Log.v(TAG, "Getting Programmes");
    	int result = 0;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1; //check statusMessage for reason
    	}
    	try {
    		rs = connection.getProgrammes(last_sync);
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.Programme.Cols.PID, rs.getString("pid"));
    			values.put(ContentDescriptor.Programme.Cols.NAME, rs.getString("name"));
    			values.put(ContentDescriptor.Programme.Cols.GID, rs.getString("programmegroupid"));
    			values.put(ContentDescriptor.Programme.Cols.GNAME, rs.getString("groupname"));
    			values.put(ContentDescriptor.Programme.Cols.SDATE, rs.getString("startdate"));
    			values.put(ContentDescriptor.Programme.Cols.EDATE, rs.getString("enddate"));
    			values.put(ContentDescriptor.Programme.Cols.PRICE, rs.getString("amount"));
    			values.put(ContentDescriptor.Programme.Cols.MLENGTH, rs.getString("mlength"));
    			values.put(ContentDescriptor.Programme.Cols.SIGNUP, rs.getString("signupfee"));
    			values.put(ContentDescriptor.Programme.Cols.NOTE, rs.getString("notes"));
    			values.put(ContentDescriptor.Programme.Cols.LASTUPDATE, rs.getString("lastupdate"));
    			values.put(ContentDescriptor.Programme.Cols.PRICE_DESC, rs.getString("price_desc"));
    			values.put(ContentDescriptor.Programme.Cols.CONCESSION, rs.getString("concession"));
    			
    			cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.PID+" = ?",
    					new String[] {rs.getString("pid")}, null);
    			if (cur.getCount() > 0) {
    				contentResolver.update(ContentDescriptor.Programme.CONTENT_URI, values, 
    						ContentDescriptor.Programme.Cols.PID+" = ?", new String[] {rs.getString("pid")});
    			} else {
    				contentResolver.insert(ContentDescriptor.Programme.CONTENT_URI, values);
    			}
    			cur.close();
    		}
    		rs.close();
    	} catch (SQLException e ) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -3;
    	}
    	cleanUp();
    	return result;	
    }
    
    
    
    public ArrayList<HashMap<String, String>>  getReportTypes(Context context){
    	
    	this.setup(context);
    	ArrayList<HashMap<String, String>> resultMapList  = null;
    	ResultSet result = null;
    	
    	if (!this.openConnection()){
    
    	}
    	
    	try {
			result = this.connection.getReportTypes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	
    	resultMapList = this.resultSetToMapList(result);
		   
    		this.cleanUp();
    	
		return resultMapList;
	
    }
    
  public ArrayList<HashMap<String, String>>  getReportNamesAndTypes(Context context){
    	
    	this.setup(context);
    	ArrayList<HashMap<String, String>> resultMapList  = null;
    	ResultSet result = null;
    	
    	if (!this.openConnection()){
    
    	}
    	
    	try {
			result = this.connection.getReportTypesAndNames();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	
    	resultMapList = this.resultSetToMapList(result);
		   
    		this.cleanUp();
    	
		return resultMapList;
	
    }
  
  public ArrayList<HashMap<String, String>> getEmailAddressesByIds(Context context, Integer []ids, String tableName) {
	  
	  	this.setup(context);
	  	ArrayList<HashMap<String, String>> resultMapList  = null;
	  	ResultSet result = null;
	  	
	  	if (!this.openConnection()){
	  		Log.e(TAG, "no Connection");
	  	}
	  	
	  	try {
				result = this.connection.getEmailAddressesByIds(ids, tableName);

			} catch (SQLException e) {
				e.printStackTrace();
			}
	  	
	  	
	  		resultMapList = this.resultSetToMapList(result);
			   
	  		this.cleanUp();
	  	
			return resultMapList;  
  }
  
  public ArrayList<HashMap<String, String>>  getReportDataByDateRange(Context context, String finalQuery){
  	
  	this.setup(context);
  	ArrayList<HashMap<String, String>> resultMapList  = null;
  	ResultSet result = null;
  	
  	if (!this.openConnection()){
  
  	}
  	
  	try {
			result = this.connection.getReportDataByDateRange(finalQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
  	
  	
  		resultMapList = this.resultSetToMapList(result);
		   
  		this.cleanUp();
  	
		return resultMapList;
	
  }
    
 
    
 public ArrayList<HashMap<String, String>>  getReportNamesByReportTypeId(Context context, int reportTypeId){
    	
    	this.setup(context);
    	ArrayList<HashMap<String, String>> resultMapList  = null;
    	ResultSet result = null;
    	
    	if (!this.openConnection()){
    			
    	}
    	
    	try {
			result = this.connection.getReportNamesByReportTypeId(reportTypeId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	resultMapList = this.resultSetToMapList(result);
		   
    	this.cleanUp();
    	return resultMapList;
	
    }
 
 public ArrayList<HashMap<String, String>>  getReportColumnsByReportId(Context context, int reportId) {
 	
 	this.setup(context);
 	ArrayList<HashMap<String, String>> resultMapList  = null;
 	ResultSet result = null;
 
 	
 	if (!this.openConnection()){
 			
 	}
 	
 	try {
			result = this.connection.getReportColumnsByReportId(reportId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
 	
 	
 	resultMapList = this.resultSetToMapList(result);
		   
 		this.cleanUp();
 	
		return resultMapList;
	
 }
 
 public ArrayList<HashMap<String, String>>  getReportColumnsFieldsByReportId(Context context, int reportId) {
	 	
	 	this.setup(context);
	 	ArrayList<HashMap<String, String>> resultMapList  = null;
	 	ResultSet result = null;
	 
	 	
	 	if (!this.openConnection()){
	 			
	 	}
	 	
	 	try {
				result = this.connection.getReportColumnsFieldsByReportId(reportId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	 	
	 	
	 		resultMapList = this.resultSetToMapList(result);
			   
	 		this.cleanUp();
	 	
			return resultMapList;
		
	 }
 
 public ArrayList<HashMap<String, String>>  getReportFilterFieldsByReportId(Context context, int reportId) {
	 	
	 	this.setup(context);
	 	ArrayList<HashMap<String, String>> resultMapList  = null;
	 	ResultSet result = null;
	 
	 	
	 	if (!this.openConnection()){
	 			
	 	}
	 	
	 	try {
				result = this.connection.getReportFilterFieldsByReportId(reportId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	 	
	 	
	 		resultMapList = this.resultSetToMapList(result);
			   
	 		this.cleanUp();
	 	
			return resultMapList;
		
 }
 
 public ArrayList<HashMap<String, String>>  getFirstReportFilterData(Context context, String query){
	 	
	 	this.setup(context);
	 	ArrayList<HashMap<String, String>> resultMapList  = null;
	 	ResultSet result = null;
	 
	 	
	 	if (!this.openConnection()){
	 			
	 	}

	 	try {
				result = this.connection.getFirstReportFilterData(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	 	
	 	
	 		resultMapList = this.resultSetToMapList(result);
			   
	 		this.cleanUp();
	 	
			return resultMapList;
 } 
 
 public ArrayList<HashMap<String, String>>  getJoiningTablesByFunctionName(Context context, String functionName) {
	 	
	 	this.setup(context);
	 	ArrayList<HashMap<String, String>> resultMapList  = null;
	 	ResultSet result = null;
	 
	 	
	 	if (!this.openConnection()){
	 			
	 	}
	 	
	 	try {
				result = this.connection.getJoiningTablesByFunctionName(functionName);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	 	
	 	
	 		resultMapList = this.resultSetToMapList(result);
			   
	 		this.cleanUp();
	 	
			return resultMapList;
		
 } 
    
    
    
    private ArrayList<HashMap<String, String>> resultSetToMapList(ResultSet result) {
    	ArrayList<HashMap<String, String>> resultMapList =  new ArrayList<HashMap<String, String>>();
    	ResultSetMetaData resultMeta = null;
    	HashMap<String, String> resultMap = null;
    	try {
			resultMeta = result.getMetaData();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
    	   	/*Extracting key-value pairings for each record
    	   	 * into a list of hash-maps, this to detach the data
    	   	 * from the connection object for return to the UI. 
    	   	 */
    	try {
			while (result.next()){
				resultMap = new HashMap<String,String>();
			   for (int i = 1; i <= resultMeta.getColumnCount(); i++)				     
				   
			   {
				   
				   //System.out.println("Column Name:" +resultMeta.getColumnName(i) + " Value: " +result.getString(resultMeta.getColumnName(i)));
				   resultMap.put(resultMeta.getColumnName(i), result.getString(resultMeta.getColumnName(i)));		
			   }
			   
			   resultMapList.add(resultMap);
			   
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return resultMapList;
    }
    
    private int getMembershipID(){
    	Log.v(TAG, "Getting Membership ID");
    	int result = 0;
    	String query = "select nextval('membership_id_seq');";
    	
    	if (!openConnection()) {
    		return -1; //see statusMessage for error;
    	}

    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.Membership.getKey(), null, null);
    	int free_count = cur.getCount();
    	cur.close();
    	
    	cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MSID+" < 0",
    			null, null);
    	int need_count = cur.getCount();
    	cur.close();
    	
    	
    	if (free_count >= 15 && need_count == 0) {
    		//we have 15 already,
    		return 0;
    	}
 
    	ResultSet rs;
    	for (int i = ((20+need_count)-free_count); i > 0; i-=1) {
    		try {
    			rs = connection.startStatementQuery(query);
    			rs.next();
    			
    			ContentValues values = new ContentValues();
    			
    			
    			if (need_count > 0) {
    				cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null,
    						ContentDescriptor.Membership.Cols.MSID+" < 0", null, null);
    				cur.moveToFirst();
    				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols._ID));
    				cur.close();
    				values.put(ContentDescriptor.Membership.Cols.MSID, rs.getString("nextval"));	
    				contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, ContentDescriptor.Membership.Cols._ID+" = ?",
    						new String[] {String.valueOf(rowid)});
    				
    				need_count -= 1;
    			} else {
	    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
	    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Membership.getKey());
	    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			}
    			rs.close();
    			result +=1;
    		} catch (SQLException e){
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	cleanUp();
    	return result;
    }
    
    private int uploadMembership(){
    	Log.v(TAG, "Uploading Membership");
    	int result = 0;
    	ArrayList<Integer> pendingRows = new ArrayList<Integer>();
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, 
    			ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())}, 
    			null);
    	while (cur.moveToNext()) {
    		pendingRows.add(cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	for (int i=0; i <pendingRows.size(); i +=1) {
    		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols._ID+" = ?",
    				new String[] {String.valueOf(pendingRows.get(i))}, null);
    		
    		if (!cur.moveToFirst()) {
    			//can't find the pending row!
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID
    					+"= ? AND "+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey()),
    					String.valueOf(pendingRows.get(i))});
    			continue;
    		}
    		int msid= 0;
    		try {
    			//will this be OK with nulls?
    			 msid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)); 
	    		result = result + connection.uploadMembership(cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MID)),
	    				msid,
	    				cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PID)),
	    				cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PGID)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)),
	    				cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.CARDNO)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.SIGNUP)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PRICE)));
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			//return -2;
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID
    					+"= ? AND "+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey()),
    					String.valueOf(pendingRows.get(i))});
    			contentResolver.delete(ContentDescriptor.Membership.CONTENT_URI, ContentDescriptor.Membership.Cols.MSID+" = ?", 
    					new String[] {String.valueOf(msid)}); //may as well delete our bad membership too.
    			continue;
    		}
    		cur.close();
    		
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID
					+"= ? AND "+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey()),
					String.valueOf(pendingRows.get(i))});
    		
    		ContentValues values = new ContentValues();
    		values.put(ContentDescriptor.Membership.Cols.DEVICESIGNUP, "f");
    		contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, ContentDescriptor.Membership.Cols._ID+" = ?",
    				new String[] {String.valueOf(pendingRows.get(i))});
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    private int getDoors() {
    	Log.v(TAG, "Getting Doors");
    	int result = 0;
    	ResultSet rs = null;
    	if (!openConnection()) {
    		return -1;
    		//see statusMessage for details;
    	}
    	
    	try {
    		rs = connection.getDoors();
    		
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.Door.Cols.DOORID, rs.getInt("id"));
    			values.put(ContentDescriptor.Door.Cols.DOORNAME, rs.getString("name"));
    			values.put(ContentDescriptor.Door.Cols.STATUS, rs.getInt("status"));
    			values.put(ContentDescriptor.Door.Cols.BOOKING, rs.getInt("booking_checkin"));
    			values.put(ContentDescriptor.Door.Cols.WOMENONLY, rs.getString("womenonly"));
    			values.put(ContentDescriptor.Door.Cols.CONCESSION, rs.getInt("concessionhandling"));
    			values.put(ContentDescriptor.Door.Cols.LASTVISITS, rs.getString("showlastvisits"));
    			values.put(ContentDescriptor.Door.Cols.COMPANY, rs.getInt("companyid"));
    			
    			cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, ContentDescriptor.Door.Cols.DOORID+" = ?", 
    					new String[] {String.valueOf(rs.getInt("id"))}, null);
    			if (cur.getCount()> 0 ) { //update
    				contentResolver.update(ContentDescriptor.Door.CONTENT_URI, values, ContentDescriptor.Door.Cols.DOORID+" = ?",
    						new String[] {String.valueOf(rs.getInt("id"))});
    			} else { //insert
    				contentResolver.insert(ContentDescriptor.Door.CONTENT_URI, values);
    			}
    			cur.close();
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	cleanUp();
    	
    	return result;
    }
    
    //call this independantly of our Queuing system.
    public boolean manualCheckin (int doorid, int memberid, int membershipid, Context context) {
    	Log.d(TAG, "Manually Checking In");
    	if (context != null) {
    		setup(context);
    	}
    	if (!openConnection()) {
    		return false;
    		//see statusMessage for details;
    	}
    	
    	if (!getDeviceDetails()) {
   			return false;
    	}
    	
    	try {
    		connection.manualCheckIn(doorid, membershipid, memberid);
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return false;
    	}

    	updateDevice();
    	cleanUp();
    	connection.closeConnection();
    	
    	return true;
    }
    
    private int getMemberNotes(long last_update) {
    	Log.v(TAG, "Getting Member Notes");
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	ResultSet rs;
    	try {
    		rs = connection.getMemberNotes(last_update);
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		return -2;
    	}
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.MemberNotes.Cols.MNID, rs.getString("id"));
    			values.put(ContentDescriptor.MemberNotes.Cols.MID, rs.getString("memberid"));
    			values.put(ContentDescriptor.MemberNotes.Cols.NOTES, rs.getString("notes"));
    			values.put(ContentDescriptor.MemberNotes.Cols.OCCURRED, rs.getString("occurred"));
    			values.put(ContentDescriptor.MemberNotes.Cols.UPDATEUSER, rs.getString("update_user_name"));
    			
    			cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
    					ContentDescriptor.MemberNotes.Cols.MNID+" = ?", new String[] {rs.getString("id")}, null);
    			
    			if (cur.getCount() > 0 ) {
    				cur.moveToFirst();
    				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols._ID));
    				cur.close();
    				contentResolver.update(ContentDescriptor.MemberNotes.CONTENT_URI, values,
    						ContentDescriptor.MemberNotes.Cols._ID+" = ?", new String[] {String.valueOf(rowid)});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.MemberNotes.CONTENT_URI, values);
    			}
    			
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		return -3;
    	}
    	
    	connection.closeConnection();
    	
    	return result;
    }
    
    private int getMemberNoteID() {
    	Log.v(TAG, "Getting MemberNotes IDs");
    	int result = 0;
    	String query = "SELECT nextval('membernotes_id_seq');";
    	
    	if (!openConnection()) {
    		return -1;
    		//see statusMessage for error;
    	}
    	/*cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
    			ContentDescriptor.MemberNotes.Cols.MID+" = 0", null, null);*/
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.MemberNotes.getKey(), null, null);
    	
    	int id_count = cur.getCount();
    	cur.close();
    	
    	cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
    			ContentDescriptor.MemberNotes.Cols.MNID+" <= 0", null, null);
    	int req_count = cur.getCount();
    	cur.close();
    	
    	for (int i = ((25+req_count)-id_count); i > 0; i-=1) {
    		try {
    			ResultSet rs = connection.startStatementQuery(query);
    			rs.next();
    			
    			ContentValues values = new ContentValues();
    			
    			if (req_count > 0) {
    				values.put(ContentDescriptor.MemberNotes.Cols.MNID, rs.getString("nextval"));
    			
    				cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
    						ContentDescriptor.MemberNotes.Cols.MNID+" <= 0", null, null);
    				cur.moveToFirst();
    				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols._ID));
    				cur.close();
    				
    				contentResolver.update(ContentDescriptor.MemberNotes.CONTENT_URI, values, 
    						ContentDescriptor.MemberNotes.Cols._ID+" = ?", new String[] {String.valueOf(rowid)});
    				req_count -= 1;

    			} else {
    				values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
    				values.put(ContentDescriptor.FreeIds.Cols.TABLEID,
    						ContentDescriptor.TableIndex.Values.MemberNotes.getKey());
    				contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			}
    			result +=1;
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			return -2;
    		}
    	}
    	
    	return result;
    }
    
    private int uploadMemberNotes(){
    	Log.v(TAG, "Uploading MemberNotes");
    	int result = 0;
    	
    	ArrayList<String> rowids = new ArrayList<String>();
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, 
    			ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? ", new String[] {String.valueOf(
    					ContentDescriptor.TableIndex.Values.MemberNotes.getKey())}, null);
    	
    	while (cur.moveToNext()) {
    		rowids.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()){
    		return -1;
    		//see StatusMessage for why;
    	}
    	
    	for (int i=0; i < rowids.size(); i++) {
    		cur = contentResolver.query(ContentDescriptor.MemberNotes.CONTENT_URI, null, 
    				ContentDescriptor.MemberNotes.Cols._ID+" = ?", new String[] {rowids.get(i)}, null );
    		
    		if (!cur.moveToFirst()) {
    			statusMessage = "Error Occured retrieving data";
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    					ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    					+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] 
    							{String.valueOf(ContentDescriptor.TableIndex.Values.MemberNotes.getKey()),
    							rowids.get(i)} 
    			);
    			continue;
    		}
    		try {
	    		connection.uploadMemberNotes(cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.MNID)),
	    				cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.MID)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.NOTES)),
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.MemberNotes.Cols.OCCURRED)));
	    		//remove it from pendings
	    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
	    				+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {rowids.get(i),
	    				String.valueOf(ContentDescriptor.TableIndex.Values.MemberNotes.getKey())});
	    		//update it to look like a normal.
	    		ContentValues values = new ContentValues();
	    		values.put(ContentDescriptor.MemberNotes.Cols.DEVICESIGNUP, "f");
	    		contentResolver.update(ContentDescriptor.MemberNotes.CONTENT_URI, values, ContentDescriptor.MemberNotes.Cols._ID+" = ?",
	    				new String[] {rowids.get(i)});
	    		
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	return result;
    }
    
    private int getMemberBalance(long last_update) {
    	Log.d(TAG, "Getting Member Balance");
    	int result = 0;
    	final long TWENTYFOURHOURS = 86400000;
    	if (!openConnection()) {
    		return -1;
    	}
    	cur = contentResolver.query(ContentDescriptor.Member.URI_JOIN_BALANCE, new String[] {"m."
    	+ContentDescriptor.Member.Cols.MID}, "("+ContentDescriptor.MemberBalance.Cols.LASTUPDATE+" <= ? OR "
    	+ContentDescriptor.MemberBalance.Cols.LASTUPDATE+" IS NULL )",
    	new String[] {String.valueOf(new Date(last_sync-TWENTYFOURHOURS).getTime())}, null);
    	
    	ArrayList<String> idlist = new ArrayList<String>();
    	while (cur.moveToNext()) {
    		idlist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.MID)));
    	}
    	cur.close();
    	
    	try {
    		for (int i = 0; i < idlist.size(); i++) {
    			ResultSet rs = connection.getBalance(idlist.get(i));
    			if (!rs.next()) {
    				//bad member id? no balance was returned.
    				continue;
    			}
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.MemberBalance.Cols.BALANCE, rs.getString("owing"));
    			values.put(ContentDescriptor.MemberBalance.Cols.MID, idlist.get(i));
    			values.put(ContentDescriptor.MemberBalance.Cols.LASTUPDATE, this_sync);
    			
    			cur = contentResolver.query(ContentDescriptor.MemberBalance.CONTENT_URI, null,
    					ContentDescriptor.MemberBalance.Cols.MID+" = ?", new String[] {idlist.get(i)}, null);
    			if (cur.moveToFirst()) {//update
    				 int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberBalance.Cols._ID));
    				 cur.close();
    				 contentResolver.update(ContentDescriptor.MemberBalance.CONTENT_URI, values, 
    						 ContentDescriptor.MemberBalance.Cols._ID+" = ?", new String[] {String.valueOf(rowid)});
    			} else { //insert
    				cur.close();
    				contentResolver.insert(ContentDescriptor.MemberBalance.CONTENT_URI, values);
    			}
    			rs.close();
    			
    			result +=1;
    		}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    private ContentValues insertMembership(ResultSet rs) throws SQLException {
    	ContentValues values = new ContentValues();
    	
    	values.put(ContentDescriptor.Membership.Cols.MID, rs.getString("memberid"));
		values.put(ContentDescriptor.Membership.Cols.MSID, rs.getString("id"));
		values.put(ContentDescriptor.Membership.Cols.CARDNO, rs.getString("cardno")); //this can be null
		values.put(ContentDescriptor.Membership.Cols.MSSTART, rs.getString("startdate"));
		values.put(ContentDescriptor.Membership.Cols.EXPIRERY, rs.getString("enddate"));
		values.put(ContentDescriptor.Membership.Cols.PNAME, rs.getString("name"));
		values.put(ContentDescriptor.Membership.Cols.VISITS, rs.getString("concession"));
		values.put(ContentDescriptor.Membership.Cols.LASTUPDATE, rs.getString("lastupdate"));
		values.put(ContentDescriptor.Membership.Cols.PID, rs.getString("programmeid"));
		values.put(ContentDescriptor.Membership.Cols.STATE, rs.getString("state"));
		values.put(ContentDescriptor.Membership.Cols.HISTORY, rs.getString("history"));
		values.put(ContentDescriptor.Membership.Cols.SIGNUP, rs.getString("signupfee"));
		values.put(ContentDescriptor.Membership.Cols.PAYMENTDUE, rs.getString("paymentdue"));
		values.put(ContentDescriptor.Membership.Cols.NEXTPAYMENT, rs.getString("nextpayment"));
		values.put(ContentDescriptor.Membership.Cols.FIRSTPAYMENT, rs.getString("firstpayment"));
		values.put(ContentDescriptor.Membership.Cols.UPFRONT, rs.getString("upfront"));
		
		return values;
    }
    
    private ContentValues insertMember(ResultSet rs, int use_roll) throws SQLException {
    	ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Member.Cols.MID, rs.getString("id"));
		values.put(ContentDescriptor.Member.Cols.FNAME, rs.getString("firstname"));
		values.put(ContentDescriptor.Member.Cols.SNAME, rs.getString("surname"));
		values.put(ContentDescriptor.Member.Cols.HAPPINESS, rs.getString("happiness"));
		values.put(ContentDescriptor.Member.Cols.PHHOME, rs.getString("mphhome"));
		values.put(ContentDescriptor.Member.Cols.PHWORK, rs.getString("mphwork"));
		values.put(ContentDescriptor.Member.Cols.PHCELL, rs.getString("mphcell"));
		values.put(ContentDescriptor.Member.Cols.EMAIL, rs.getString("memail"));
		 
		values.put(ContentDescriptor.Member.Cols.STATUS, rs.getString("status"));
		values.put(ContentDescriptor.Member.Cols.CARDNO, rs.getString("cardno"));
		values.put(ContentDescriptor.Member.Cols.GENDER, rs.getString("gender"));
		values.put(ContentDescriptor.Member.Cols.EMERGENCYCELL, rs.getString("emergencycell"));
		values.put(ContentDescriptor.Member.Cols.EMERGENCYHOME, rs.getString("emergencyhome"));
		values.put(ContentDescriptor.Member.Cols.EMERGENCYWORK, rs.getString("emergencywork"));
		values.put(ContentDescriptor.Member.Cols.EMERGENCYNAME, rs.getString("emergencyname"));
		values.put(ContentDescriptor.Member.Cols.EMERGENCYRELATIONSHIP, rs.getString("emergencyrelationship"));
		values.put(ContentDescriptor.Member.Cols.MEDICAL, rs.getString("medicalconditions"));
		values.put(ContentDescriptor.Member.Cols.MEDICALDOSAGE, rs.getString("medicationdosage"));
		values.put(ContentDescriptor.Member.Cols.MEDICATION, rs.getString("medication"));
		values.put(ContentDescriptor.Member.Cols.MEDICATIONBYSTAFF, rs.getString("medicationbystaff"));
		java.sql.Date dob = rs.getDate("dob");
		if (dob != null) {
			Date _dob = new Date(dob.getTime());
			String __dob = Services.DateToString(_dob);
			values.put(ContentDescriptor.Member.Cols.DOB, __dob);
		}
		values.put(ContentDescriptor.Member.Cols.STREET, rs.getString("addressstreet"));
		values.put(ContentDescriptor.Member.Cols.SUBURB, rs.getString("addresssuburb"));
		values.put(ContentDescriptor.Member.Cols.CITY, rs.getString("addresscity"));
		values.put(ContentDescriptor.Member.Cols.POSTAL, rs.getString("addressareacode"));
		values.put(ContentDescriptor.Member.Cols.COUNTRY, rs.getString("addresscountry"));
		values.put(ContentDescriptor.Member.Cols.BILLINGACTIVE, rs.getString("billingactive"));
		values.put(ContentDescriptor.Member.Cols.DD_EXPORT_FORMATID, rs.getInt("dd_export_formatid"));
		values.put(ContentDescriptor.Member.Cols.GENDER, rs.getString("gender"));
		
		if (use_roll > 0) {
			values.put(ContentDescriptor.Member.Cols.PARENTNAME, rs.getString("parentname"));
		}
		
		return values;
    }
    
    /**
     * Pending downloads are rows that we know are out of sync with the master DB.
     * we should always sync these first.
     * used for adding member, expiring memberships.
     */
    private int getPendingDownloads() {
    	Log.v(TAG, "Getting Pending Downloads");
    	int result = 0;
    	
    	ArrayList<String> member = new ArrayList<String>();
    	ArrayList<String> membership = new ArrayList<String>();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingDownloads.CONTENT_URI, null, null, null, null);
    	while (cur.moveToNext()){
    		if (cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingDownloads.Cols.TABLEID)) == 
    				ContentDescriptor.TableIndex.Values.Member.getKey()) {
    			member.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingDownloads.Cols.ROWID)));
    		} else if (cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingDownloads.Cols.TABLEID)) ==
    				ContentDescriptor.TableIndex.Values.Membership.getKey()) {
    			membership.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingDownloads.Cols.ROWID)));
    		} else {
    			//put it somewhere else;
    		}
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	int use_roll = Integer.parseInt(Services.getAppSettings(getApplicationContext(), "use_roll"));
    	for (int i = 0; i < member.size(); i++) {
    		String query;

    		
    		if (use_roll > 0) {
    			query = connection.YMCAMembersQuery+" WHERE id = "+member.get(i)+";";
    					
    		} else {
    			query = connection.membersQuery+" WHERE id = "+member.get(i)+";";
    		}
    		ResultSet rs;
    		try {
    			rs = connection.startStatementQuery(query);
    			if (rs.next()) {
    				ContentValues values = insertMember(rs, use_roll);
    				
    				contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols.MID+" = ?",
        						new String[] {rs.getString("id")});
    			}
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    		}
    		contentResolver.delete(ContentDescriptor.PendingDownloads.CONTENT_URI, ContentDescriptor.PendingDownloads.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.PendingDownloads.Cols.TABLEID+" = ?",new String[] {member.get(i),
    				String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey())});
    	}
    	//TODO: add cancel_reason to this code.
    	for (int i = 0; i < membership.size(); i++) {
    		ResultSet rs;
    		String query ="SELECT membership.id, memberid, membership.startdate, membership.enddate, cardno, membership.notes, " +
	    			"primarymembership, membership.lastupdate, membership_state(membership.*, programme.*) as state," +
	    			" membership.concession, programme.name, programme.id AS programmeid, membership.termination_date, membership.cancel_reason, "
	    			+ " membership.history, membership.signupfee, membership.paymentdue::TEXT||' '::TEXT||price_desc(programme.amount, programme.id) AS paymentdue,"
	    			+ " membership.nextpayment, membership.firstpayment,"
	    			+ " membership.upfront, price_desc(programme.amount, programme.id) AS price_desc"
	    			+ " FROM membership LEFT JOIN programme ON (membership.programmeid = programme.id)" +
	    			" WHERE membership.id = "+membership.get(i)+" ;";
    		
    		try {
    			rs = connection.startStatementQuery(query);
    			if (rs.next()) {
    				ContentValues values = insertMembership(rs);
    				
    				contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, ContentDescriptor.Membership.Cols.MSID+" = ?",
    						new String[] {membership.get(i)});
    			}
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    		}
    		contentResolver.delete(ContentDescriptor.PendingDownloads.CONTENT_URI, ContentDescriptor.PendingDownloads.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.PendingDownloads.Cols.TABLEID+" = ?", new String[] {membership.get(i),
    				String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())});
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    //Pending Updates are updates/changes we need to push.
    //TODO: make this a centralised function. it should call the individual update functions, rather than running
    //		multiple seperate functions that check for pending-updates.
    private int getPendingUpdates() {
    	Log.v(TAG, "Getting Pending Updates");
    	int result = 0;
    	ArrayList<String> memberids = new ArrayList<String>();
    	ArrayList<String> suspendids = new ArrayList<String>();
    	cur = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, null, null, null);
    	while (cur.moveToNext()) {
    		int tableid = cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.TABLEID)); 
    		
    		if (tableid == ContentDescriptor.TableIndex.Values.Member.getKey()){
    			memberids.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID)));
    		} else if (tableid == ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()) {
    			suspendids.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID)));
    			break;
    		} else {
    			//another type of update.
    			//should probably throw an error so I know to write code here.
    			break;
    		}    		    		
    	}
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	//handle the member updates.
    	Log.d(TAG, "Updating Members");
    	
    	for (int i= 0; i< memberids.size(); i++) {
    		switch ( updateMember(memberids.get(i)) ){
    		case (1): {//success
    			result = result+1;
    			//delete the row;
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.TABLEID
    					+" = ? AND "+ContentDescriptor.PendingUpdates.Cols.ROWID+" = ?",
    					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey()), memberids.get(i)
    			});
    			break;
    		} case (-2): { //SQL error
    			//keep trying, we'll come back to it.
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.TABLEID
    					+" = ? AND "+ContentDescriptor.PendingUpdates.Cols.ROWID+" = ?",
    					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey()), memberids.get(i)
    			});
    			break;
    		} case (0):{ //row not found.
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.TABLEID
    					+" = ? AND "+ContentDescriptor.PendingUpdates.Cols.ROWID+" = ?",
    					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey()), memberids.get(i)
    			});
    			break;
    		}}
    	}
    	Log.d(TAG, "Updated "+result+" Member(s)");
    	
    	for (int i=0; i< suspendids.size(); i++) {
    		switch (updateSuspend(suspendids.get(i))){
    		default:{
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.TABLEID
    					+" = ? AND "+ContentDescriptor.PendingUpdates.Cols.ROWID+" = ?",
    					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()), suspendids.get(i)});
    			break;
    		}}
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int getDeletedRecords(long last_sync) {
    	Log.v(TAG, "Getting Deleted Records");
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	ResultSet rs;
    	try {
    		rs = connection.getDeletedRecords(last_sync);
    		
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.DeletedRecords.TABLENAME, rs.getString("tablename"));
    			values.put(ContentDescriptor.DeletedRecords.ROWID, rs.getString("deletedid"));
    			
    			contentResolver.update(ContentDescriptor.DeletedRecords.CONTENT_URI, values, null, null);
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -3;
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    /**
     * gets 30 roll-ids.
     * @return
     */
    private int getRollID(){
    	int result = 0;
    	ContentValues values;
    	ResultSet rs;
    	String query ="select nextval('roll_id_seq');";
    	 if (!openConnection()) {
    		 return -1;
    		 //connection didn't open!
    	 }
    	 cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "+
    			 ContentDescriptor.TableIndex.Values.RollCall.getKey(), null, null);
    	 int free_count = cur.getCount();
    	 cur.close();
    	 //don't bother checking for required count; we're not going to add Roll's if we haven't got ID's for them.
    	 if (free_count > 10) {
    		 return 0;
    	 }
    	 for (int i=(30-free_count); i>0;i--){
    		 try {
    	    		rs = connection.startStatementQuery(query);
    	    		rs.next();
    	    		values = new ContentValues();
    	    		values.put(ContentDescriptor.FreeIds.Cols.TABLEID, ContentDescriptor.TableIndex.Values.RollCall.getKey());
    	    		values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
    	    		
    	    		contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    	    		result +=1;
    	    		rs.close();
    	    	} catch (SQLException e) {
    	    		statusMessage = e.getLocalizedMessage();
    	    		Log.e(TAG, "", e);
    	    		return -2;
    	    	}
    	}
    	cleanUp();
    	 
    	return result;
    }
    /**
     * get 400 roll-item ids.
     * @return
     */
    private int getRollItemID(){
    	int result = 0;
    	ResultSet rs;
    	ContentValues values;
    	
    	if (!openConnection()){
    		return -1;
    		//we didn't open the connection.
    	}
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.RollItem.getKey(), null, null);
    	int free_count = cur.getCount();
    	cur.close();
    	
    	cur = contentResolver.query(ContentDescriptor.RollItem.CONTENT_URI, null, ContentDescriptor.RollItem.Cols.ROLLID+" <=0",
    			null, null);
    	int req_count = cur.getCount();
    	cur.close();
    	
    	for (int i=((400+req_count)-free_count);i>0;i--) {
    		try {
    			rs = connection.startStatementQuery("select nextval('roll_item_id_seq');");
    			rs.next();
    			
    			if (req_count > 0) {
    				cur = contentResolver.query(ContentDescriptor.RollItem.CONTENT_URI, null, 
    						ContentDescriptor.RollItem.Cols.ROLLITEMID+" <= 0", null, null);
    				cur.moveToFirst();
    				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.RollItem.Cols._ID));
    				cur.close();
    				values = new ContentValues();
    				values.put(ContentDescriptor.RollItem.Cols.ROLLITEMID, rs.getString("nextval"));
    				contentResolver.update(ContentDescriptor.RollItem.CONTENT_URI, values, ContentDescriptor.RollItem.Cols._ID+" = ?",
    						new String[] {String.valueOf(rowid)});
    				//update the devicesignup field when we upload ?
    				req_count -= 1;
    				
    			} else {
	    			values = new ContentValues();
	    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID, ContentDescriptor.TableIndex.Values.RollItem.getKey());
	    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
	    			
	    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			}
    			result +=1;
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	cleanUp();
    	return result;
    }
    
    private int uploadRoll(){
    	int result = 0;
    	ArrayList<String> rolls;
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.RollCall.getKey(), null, null);
    	rolls = new ArrayList<String>();
    	while (cur.moveToNext()){
    		rolls.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    
		for (int i=0;i<rolls.size(); i++) {
			try {
				cur = contentResolver.query(ContentDescriptor.RollCall.CONTENT_URI, null, "r."+ContentDescriptor.RollCall.Cols._ID+" = ?",
						new String[] {rolls.get(i)}, null);
				if (!cur.moveToFirst()) {
					//should probably delete it from pending uploads.
					contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
	    					+ContentDescriptor.PendingUploads.Cols.TABLEID+"= ?", new String[] {rolls.get(i),
	    					String.valueOf(ContentDescriptor.TableIndex.Values.RollCall.getKey())});
					continue;
				}
				connection.uploadRoll(cur.getInt(cur.getColumnIndex(ContentDescriptor.RollCall.Cols.ROLLID)),
						cur.getString(cur.getColumnIndex(ContentDescriptor.RollCall.Cols.NAME)), 
						cur.getString(cur.getColumnIndex(ContentDescriptor.RollCall.Cols.DATETIME)));
				cur.close();
				
				ContentValues values = new ContentValues();
				values.put(ContentDescriptor.RollCall.Cols.DEVICESIGNUP, "f");
				contentResolver.update(ContentDescriptor.RollCall.CONTENT_URI, values, ContentDescriptor.RollCall.Cols._ID+" = ?",
						new String[] {rolls.get(i)});
				
				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
						+ContentDescriptor.PendingUploads.Cols.TABLEID+"= ?", new String[] {rolls.get(i),
						String.valueOf(ContentDescriptor.TableIndex.Values.RollCall.getKey())});
				
				connection.closePreparedStatement();
				result +=1;
			} catch (SQLException e) {
	    		statusMessage =e.getLocalizedMessage();
	    		Log.e(TAG, "", e);
	    		return -2;
	    	}
		}
    	cleanUp();
    	
    	return result;
    }
    
    private int uploadRollItem(){
    	int result = 0;
    	ArrayList<String> rollitems;
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.RollItem.getKey(), null, null);
    	rollitems = new ArrayList<String>();
    	while (cur.moveToNext()) {
    		rollitems.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
	
		for (int i=0;i<rollitems.size();i++) {
			try {
				cur = contentResolver.query(ContentDescriptor.RollItem.CONTENT_URI, null, "r."+ContentDescriptor.RollItem.Cols._ID+" = ?", 
						new String[] {rollitems.get(i)}, null);
				if (!cur.moveToFirst()) {
					//delete from pendinguploads.
					contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
							ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND"
	    					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", 
	    					new String[] {rollitems.get(i), 
							String.valueOf(ContentDescriptor.TableIndex.Values.RollItem.getKey())});
					continue;
				}
				connection.uploadRollItem(cur.getInt(cur.getColumnIndex(ContentDescriptor.RollItem.Cols.ROLLITEMID)),
						cur.getInt(cur.getColumnIndex(ContentDescriptor.RollItem.Cols.ROLLID)), 
						cur.getInt(cur.getColumnIndex(ContentDescriptor.RollItem.Cols.MEMBERID)), 
						cur.getString(cur.getColumnIndex(ContentDescriptor.RollItem.Cols.ATTENDED)));
				cur.close();
				
				ContentValues values = new ContentValues();
				values.put(ContentDescriptor.RollItem.Cols.DEVICESIGNUP, "f");
				contentResolver.update(ContentDescriptor.RollItem.CONTENT_URI, values, ContentDescriptor.RollItem.Cols._ID+" = ?",
						new String[] {rollitems.get(i)});
				
				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
						+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", 
						new String[] {rollitems.get(i), String.valueOf(ContentDescriptor.TableIndex.Values.RollItem.getKey())});
				
				connection.closePreparedStatement();
				
				result +=1;
			}catch (SQLException e) {
        		statusMessage = e.getLocalizedMessage();
        		Log.e(TAG, "", e);
        		return -2;
			}
		}
	
    	cleanUp();
    	
    	return result;
    }
    
    private int updateRollItem() {
    	int result = 0;
    	ArrayList<String> rollitems;
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = "
    			+ContentDescriptor.TableIndex.Values.RollItem.getKey(), null, null);
    	rollitems = new ArrayList<String>();
    	while (cur.moveToNext()) {
    		rollitems.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}

		for (int i=0; i<rollitems.size();i++) {
	    	try {
				cur = contentResolver.query(ContentDescriptor.RollItem.CONTENT_URI, null, "r."+ContentDescriptor.RollItem.Cols._ID+" = ?",
						new String[] {rollitems.get(i)}, null);
				if (!cur.moveToFirst()) {
					//can't find the item.
					contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
	    					+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {rollitems.get(i),
	    					String.valueOf(ContentDescriptor.TableIndex.Values.RollItem.getKey())});
					continue;
				}
				connection.updateRollItem(cur.getInt(cur.getColumnIndex(ContentDescriptor.RollItem.Cols.ROLLITEMID)),
						cur.getString(cur.getColumnIndex(ContentDescriptor.RollItem.Cols.ATTENDED)));
				
				ContentValues values = new ContentValues();
				values.put(ContentDescriptor.RollItem.Cols.DEVICESIGNUP, "f");
				contentResolver.update(ContentDescriptor.RollItem.CONTENT_URI, values,
						ContentDescriptor.RollItem.Cols._ID+" = ?", new String[] {rollitems.get(i)});
				
				contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
						+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {rollitems.get(i),
						String.valueOf(ContentDescriptor.TableIndex.Values.RollItem.getKey())});
				result +=1;
	    	} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		Log.e(TAG, "", e);
	    		return -2;
	    	}
		}
    	
    	return result;
    }
    
    private int getRoll(long last_sync){
    	int result = 0;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	try {
    		rs = connection.getRoll(last_sync);
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.RollCall.Cols.NAME, rs.getString("name"));
    			values.put(ContentDescriptor.RollCall.Cols.DATETIME, rs.getString("datetime"));
    			String rollid = rs.getString("id");
    			cur = contentResolver.query(ContentDescriptor.RollCall.CONTENT_URI, null, "r."+ContentDescriptor.RollCall.Cols.ROLLID
    					+" = ?", new String[] {rollid}, null);
    			if (cur.getCount() > 0) { //update
    				contentResolver.update(ContentDescriptor.RollCall.CONTENT_URI, values, 
    						ContentDescriptor.RollCall.Cols.ROLLID+" = ?", new String[] {rollid});
    			} else { //insert.
    				values.put(ContentDescriptor.RollCall.Cols.ROLLID, rollid);
    				contentResolver.insert(ContentDescriptor.RollCall.CONTENT_URI, values);
    			}
    			cur.close();
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	cleanUp();
    	
    	return result;
    }
    
    private int getRollItem(long last_sync){
    	int result = 0;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	try {
    		rs = connection.getRollItem(last_sync);
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			String rollitemid = rs.getString("id");
    			
    			values.put(ContentDescriptor.RollItem.Cols.ROLLID, rs.getString("rollid"));
    			values.put(ContentDescriptor.RollItem.Cols.MEMBERID, rs.getString("memberid"));
    			values.put(ContentDescriptor.RollItem.Cols.ATTENDED, rs.getString("attended"));
    			
    			cur = contentResolver.query(ContentDescriptor.RollItem.CONTENT_URI, null, ContentDescriptor.RollItem.Cols.ROLLITEMID+" = ?",
    					new String[] {rollitemid}, null);
    			if (cur.getCount() > 0) { //update
    				contentResolver.update(ContentDescriptor.RollItem.CONTENT_URI, values, ContentDescriptor.RollItem.Cols.ROLLITEMID+" = ?",
    						new String[] {rollitemid});
    			} else { //insert
    				values.put(ContentDescriptor.RollItem.Cols.ROLLITEMID, rollitemid);
    				contentResolver.insert(ContentDescriptor.RollItem.CONTENT_URI, values);
    			}
    			cur.close();
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	return result;
    }
    
    private int getConfig() {
    	int result = 0;
    	ResultSet rs;
    	final long ONE_WEEK = 604800000; 
    	long last_update = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.Company.CONTENT_URI, null, null, null, null);
		if (cur.moveToFirst()) {
			last_update = cur.getLong(cur.getColumnIndex(ContentDescriptor.Company.Cols.LASTUPDATE));
		}
		cur.close();
		
		if (last_update+ONE_WEEK > new Date().getTime()) {
			return 0;
		}
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	try {
    		rs = connection.getCompanyConfig();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.Company.Cols.NAME, rs.getString("name"));
    			values.put(ContentDescriptor.Company.Cols.TE_USERNAME, rs.getString("te_username"));
    			values.put(ContentDescriptor.Company.Cols.SCHEMAVERSION, rs.getString("schemaversion"));
    			values.put(ContentDescriptor.Company.Cols.TE_PASSWORD, rs.getString("te_password"));
    			values.put(ContentDescriptor.Company.Cols.WEB_URL, rs.getString("web_url"));
    			values.put(ContentDescriptor.Company.Cols.LASTUPDATE, new Date().getTime());
    			values.put(ContentDescriptor.Company.Cols.NAMEORDER, rs.getString("name_order"));
    			
    			cur = contentResolver.query(ContentDescriptor.Company.CONTENT_URI, null, ContentDescriptor.Company.Cols.NAME+" = ?",
    					new String[] {rs.getString("name")}, null);
    			if (cur.moveToFirst()) {
    				int row_id = cur.getInt(cur.getColumnIndex(ContentDescriptor.Company.Cols.ID));
    				cur.close();
    				contentResolver.update(ContentDescriptor.Company.CONTENT_URI, values, ContentDescriptor.Company.Cols.ID+" = ?", 
    						new String[] {String.valueOf(row_id)});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.Company.CONTENT_URI, values);
    			}
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -3;
    	}
    	cleanUp();
    	
    	return result;
    }
    
    private boolean uploadLog(){ 
    	JSONHandler json = new JSONHandler(getApplicationContext());
    	String te_username = "", schemaversion = "", company_name = "", appid = "";
    	
    	cur = contentResolver.query(ContentDescriptor.Company.CONTENT_URI, null, null, null, null);
    	if (cur.moveToFirst()) {
    		te_username = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_USERNAME));
    		schemaversion = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.SCHEMAVERSION));
    		company_name = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.NAME));
    	}
    	cur.close();
    	
    	appid = ApplicationID.id();
    	
    	return json.uploadLog(this_sync, te_username, schemaversion, company_name, appid);
    }
    
    private int uploadPendingDeletes() {
    	Log.d(TAG, "Uploading Pending Deletes");
    	int result = 0;
    	ArrayList<Integer> memberships = new ArrayList<Integer>();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingDeletes.CONTENT_URI, null, null, null, null);
    	while (cur.moveToNext()) {
    		if (cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingDeletes.Cols.TABLEID)) 
    				== ContentDescriptor.TableIndex.Values.Membership.getKey()) {
    			memberships.add(cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingDeletes.Cols.ROWID)));
    			//the membershipid
    		} else {
    			//another type of pending delete.
    			//should probably throw an error so I know to write code here.
    		}
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1; //Connection Failed!
    	}
    	
    	//handle the pending Membership Deletes;
    	for (int i=0; i<memberships.size(); i++) {
    		try {
    			connection.deleteMembership(memberships.get(i));
    			
    			contentResolver.delete(ContentDescriptor.PendingDeletes.CONTENT_URI, ContentDescriptor.PendingDeletes.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingDeletes.Cols.TABLEID+" = ?", new String[] {String.valueOf(memberships.get(i)),
    					String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())});
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			//may as well remove the pending delete, the SQL Exception may have been caused by it.
    			contentResolver.delete(ContentDescriptor.PendingDeletes.CONTENT_URI, ContentDescriptor.PendingDeletes.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingDeletes.Cols.TABLEID+" = ?", new String[] {String.valueOf(memberships.get(i)),
    					String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())});
    		}
    	}
    	//do other magic handling here.
    	
    	cleanUp();
    	Log.d(TAG, "Finished upload.");
    	return result;
    }
    
    private int getMembershipExpiryReasons() {
    	Log.d(TAG, "Getting Membership Expiry Reasons!");
    	ResultSet rs;
    	int result = 0;
    	
    	contentResolver.delete(ContentDescriptor.MembershipExpiryReason.CONTENT_URI, null, null);
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	try {
    		rs = connection.getExpiryReason();
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.MembershipExpiryReason.Cols.ID, rs.getString("id"));
    			values.put(ContentDescriptor.MembershipExpiryReason.Cols.NAME, rs.getString("name"));
    			
    			contentResolver.insert(ContentDescriptor.MembershipExpiryReason.CONTENT_URI, values);
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "EXPIRY ERROR: ",e);
    		return -2;
    	}
    	
    	cleanUp();
    	return result;
    }
    
    //TODO make this function called from the pending updates function.
    private int updateMembership() {
    	Log.d(TAG, "Updating Memberships");
    	int result = 0;
    	ArrayList<String> pendingMemberships = new ArrayList<String>();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())}, null);
    	while (cur.moveToNext()) {
    		pendingMemberships.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	Log.d(TAG, pendingMemberships.size()+" Pending Membership(s)");
    	for (int i = 0; i <pendingMemberships.size(); i++) {
    		int membershipid = -1;
    		ContentValues values = new ContentValues();
    		
    		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols._ID+" = ?", 
    				new String[] {pendingMemberships.get(i)}, null);
    		if (cur.moveToFirst()) {
	    		membershipid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID));
	    		
	    		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.TERMINATION_DATE)) == null) {
	    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
	        				+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ? ", new String[] {pendingMemberships.get(i),
	        				String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())});
	    			continue;
	    		}
	    		
	    		values.put(ContentDescriptor.Membership.Cols.CANCEL_REASON, 
	    				cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.CANCEL_REASON)));
	    		values.put(ContentDescriptor.Membership.Cols.TERMINATION_DATE, 
	    				"'"+cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.TERMINATION_DATE))+"'::date");
	    		Log.d(TAG, "termination_date:"+values.getAsString(ContentDescriptor.Membership.Cols.TERMINATION_DATE));
				cur.close();
				
				cur = contentResolver.query(ContentDescriptor.CancellationFee.CONTENT_URI, null, ContentDescriptor.CancellationFee.Cols.MEMBERSHIPID+" = ?",
						new String[] {String.valueOf(membershipid)}, null);
				if (cur.moveToFirst()) {
					values.put(ContentDescriptor.CancellationFee.Cols.FEE, cur.getString(cur.getColumnIndex(ContentDescriptor.CancellationFee.Cols.FEE)));
				}
				
				try {
					connection.updateMembership(values, membershipid);
					result+=1;
					
					values = new ContentValues();
					values.put(ContentDescriptor.PendingDownloads.Cols.ROWID, membershipid);
					values.put(ContentDescriptor.PendingDownloads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Membership.getKey());
					contentResolver.insert(ContentDescriptor.PendingDownloads.CONTENT_URI, values);
				} catch (SQLException e) {
					statusMessage = e.getLocalizedMessage();
					Log.e(TAG, "", e);
					return -1;
				}
    		}
    		cur.close();
    		
    		contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ? ", new String[] {pendingMemberships.get(i),
    				String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())});
    		
    		contentResolver.delete(ContentDescriptor.CancellationFee.CONTENT_URI, ContentDescriptor.CancellationFee.Cols.MEMBERSHIPID+" = ?",
    				new String[] {String.valueOf(membershipid)});
    		
    		values = new ContentValues();
    		values.put(ContentDescriptor.Membership.Cols.DEVICESIGNUP, "f");
    		contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, ContentDescriptor.Membership.Cols.MSID+" = ?",
    				new String[] {String.valueOf(membershipid)});
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    
    //ideally we want to run this function on demand.
    //and only on demand. that way we can hide the data from users who shouldn't have access to it.
    public int getKPIs(Context context) {
    	setup(context);
    	ResultSet rs = null;
    	int result = 0;
    	final String IGNORE1 = "Membership Retention (renewed ";
    	final String IGNORE2 = "Members who have not visited in period";
    	
    	if (!openConnection()) {
    		Log.e(TAG, "no Connection");
    		return -1;
    	}
    	if (!getDeviceDetails()) {
   			return -1;
    	}
    	//for the time being, lets delete all metrics when we sync.
    	contentResolver.delete(ContentDescriptor.KPI.CONTENT_URI, null, null);
    	
    	try {
    		ContentValues values;
    		rs = connection.getKPIs();
    		if (rs == null) {
    			statusMessage = "KPI's not available for mobile in this version of GymMaster. Please update to Version 320.";
    			return 0;
    		}
    		while (rs.next()) {
    			
    			values = new ContentValues();
    			if (rs.getString("metric").contains(IGNORE1) || rs.getString("metric").contains(IGNORE2)) {//|| rs.getString("metric").compareTo(IGNORE2) == 0) {
    				continue;
    			}
    			values.put(ContentDescriptor.KPI.Cols.METRIC, rs.getString("metric"));
    			values.put(ContentDescriptor.KPI.Cols.VALUE, rs.getString("value"));
    			values.put(ContentDescriptor.KPI.Cols.LASTUPDATE, new Date().getTime());
    			
    			cur = contentResolver.query(ContentDescriptor.KPI.CONTENT_URI, null, ContentDescriptor.KPI.Cols.METRIC+" = ?",
    					new String[] {rs.getString("metric")}, null);
    			if (cur.moveToFirst()) {
    				cur.close();
    				contentResolver.update(ContentDescriptor.KPI.CONTENT_URI, values, ContentDescriptor.KPI.Cols.METRIC+" = ?",
    						new String[] {rs.getString("metric")});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.KPI.CONTENT_URI, values);
    			}
    			result +=1;
    		}
    		rs.close();
    	} catch (SQLException e) {
    		Log.e(TAG, "", e);
    		statusMessage = e.getLocalizedMessage();
    		return -2;
    	}
    	
    	updateDevice();
    	cleanUp();
    	connection.closeConnection();
    	
    	return result;
    }
    
    private int getFinancialDetails(long lastupdate) {
    	Log.d(TAG, "Getting Financial Details!");
    	int result = 0;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	try {
    		rs = connection.getFinance(lastupdate);
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.MemberFinance.Cols.ROWID, rs.getInt("id"));
    			values.put(ContentDescriptor.MemberFinance.Cols.MEMBERID, rs.getInt("memberid"));
    			values.put(ContentDescriptor.MemberFinance.Cols.MEMBERSHIPID, rs.getInt("membershipid"));
    			values.put(ContentDescriptor.MemberFinance.Cols.CREDIT, rs.getString("credit"));
    			values.put(ContentDescriptor.MemberFinance.Cols.DEBIT, rs.getString("debit"));
    			values.put(ContentDescriptor.MemberFinance.Cols.NOTE, rs.getString("note"));
    			values.put(ContentDescriptor.MemberFinance.Cols.ORIGIN, rs.getString("origin"));
    			Double occurred, row_lastupdate, created;
    			occurred = rs.getDouble("occurred");
    			occurred = (occurred*1000);
    			row_lastupdate = rs.getDouble("lastupdate");
    			row_lastupdate = (row_lastupdate*1000);
    			created = rs.getDouble("created");
    			created = (created*1000);
    			values.put(ContentDescriptor.MemberFinance.Cols.OCCURRED, occurred);
    			values.put(ContentDescriptor.MemberFinance.Cols.LASTUPDATE, row_lastupdate);
    			values.put(ContentDescriptor.MemberFinance.Cols.CREATED, created);
    			values.put(ContentDescriptor.MemberFinance.Cols.DD_EXPORT_MEMBERID, rs.getInt("dd_export_memberid"));
    			
    			cur = contentResolver.query(ContentDescriptor.MemberFinance.CONTENT_URI, null, ContentDescriptor.MemberFinance.Cols.ROWID+" = ?",
    					new String[] {rs.getString("id")}, null);
    			if (cur.moveToFirst()) {
    				int id = cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols._ID));
    				cur.close();
    				contentResolver.update(ContentDescriptor.MemberFinance.CONTENT_URI, values, ContentDescriptor.MemberFinance.Cols._ID+" = ?",
    						new String[] {String.valueOf(id)});
    			} else {
    				cur.close();
    				contentResolver.insert(ContentDescriptor.MemberFinance.CONTENT_URI, values);
    			}
    		}
    		rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	
    	cleanUp();
    	
    	return result;
    }
    
    private int getBillingHistory(long lastupdate) {
    	int result = 0;
    	ResultSet rs;
    	ContentValues values;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	try {
    		rs = connection.getBillingHistory(lastupdate);
    		
			while (rs.next()) {
				values = new ContentValues();
				
				values.put(ContentDescriptor.BillingHistory.Cols.ID, rs.getInt("id"));
				values.put(ContentDescriptor.BillingHistory.Cols.MEMBERID, rs.getInt("memberid"));
				values.put(ContentDescriptor.BillingHistory.Cols.DDEXPORTID, rs.getInt("dd_exportid"));
				values.put(ContentDescriptor.BillingHistory.Cols.FAILED, rs.getString("failed"));
				values.put(ContentDescriptor.BillingHistory.Cols.AMOUNT, rs.getString("amount"));
				values.put(ContentDescriptor.BillingHistory.Cols.STATUS, rs.getString("status"));
				values.put(ContentDescriptor.BillingHistory.Cols.NOTE, rs.getString("note"));
				values.put(ContentDescriptor.BillingHistory.Cols.PROCESSDATE, rs.getString("processdate"));
				values.put(ContentDescriptor.BillingHistory.Cols.FAILREASON, rs.getString("failreason"));
				values.put(ContentDescriptor.BillingHistory.Cols.PAIDBYOTHER, rs.getInt("paidbyother"));
				values.put(ContentDescriptor.BillingHistory.Cols.DISHONOURED, rs.getString("dishonoured"));
				values.put(ContentDescriptor.BillingHistory.Cols.RUNNINGTOTAL, rs.getString("runningtotal"));
				
				Double processdate, row_lastupdate;
				row_lastupdate = rs.getDouble("lastupdate");
				row_lastupdate = (row_lastupdate*1000);
				processdate = rs.getDouble("processdate");
				processdate = (processdate*1000);
				values.put(ContentDescriptor.BillingHistory.Cols.PROCESSDATE, processdate);
				values.put(ContentDescriptor.BillingHistory.Cols.LASTUPDATE, row_lastupdate);
				
				cur = contentResolver.query(ContentDescriptor.BillingHistory.CONTENT_URI, null, ContentDescriptor.BillingHistory.Cols.ID+" = ?",
						new String[] {String.valueOf(rs.getInt("id"))}, null);
				if (cur.moveToNext()) {
					cur.close();
					contentResolver.update(ContentDescriptor.BillingHistory.CONTENT_URI, values, ContentDescriptor.BillingHistory.Cols.ID+" = ?",
							new String[] {String.valueOf(rs.getInt("id"))});
				} else {
					cur.close();
					contentResolver.insert(ContentDescriptor.BillingHistory.CONTENT_URI, values);
				}
				result +=1;
			}
			rs.close();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG,"",e);
    		return -2;
    	}
    	
    	cleanUp();
    	return result;
    }
    
    //sync finished!
    private boolean updateDevice() {
    	ResultSet rs;
    	int deviceid = -1;
    	String uniqueid, query;
    	uniqueid = ApplicationID.id();
    	if (uniqueid == null) {
    		//we've not got an unique string, probably because of an issue
    		//with the sd-card.
    		return true;
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.AppConfig.CONTENT_URI, null, null, null, null);
    	if (cur.moveToFirst()) {
    		deviceid = cur.getInt(cur.getColumnIndex(ContentDescriptor.AppConfig.Cols.DB_DEVICEID));
    	}
    	cur.close();
    	
    	if (!openConnection()) return false;
    	
    	//look for an id.
    	if (deviceid <=0) {
    		query = "SELECT id FROM sync WHERE device = '"+uniqueid+"';";
    		try {
    			rs = connection.startStatementQuery(query);
    			while (rs.next()) {
    				deviceid = rs.getInt("id");
    			}
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG,"", e);
    		}
    		connection.closeStatementQuery();
    	}
    	
    	if (deviceid <= 0) {
    		//we'll need to do an insert.
    		query = "INSERT INTO sync(servertime, device, clienttime, completed) VALUES (now(), '"+uniqueid+"',"+
    		"to_timestamp("+(double)(new Date().getTime()/1000d)+"), true) RETURNING id;";
    		
    	} else { //the behaviour of the servertime doesn't seem correct.
    		query = "UPDATE sync SET (servertime, clienttime, completed) = (now(), to_timestamp("+(double)(new Date().getTime()/1000d)
    				+"), true) WHERE id = "+deviceid+" RETURNING id;";
    	}
    	
    	try {
    		rs = connection.startStatementQuery(query);
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.AppConfig.Cols.DB_DEVICEID, rs.getInt("id"));
    			if (deviceid <=0) {
    				contentResolver.insert(ContentDescriptor.AppConfig.CONTENT_URI, values);
    			} 
    		}
    		rs.close();
    	} catch (SQLException e) {
    		//if we got here either our connection was bad, or our query was bad.
			//if the query was bad, then we're probably using an older version of GymMaster (not > 320)
			//in which case we return false, though we're not actually using these return values for anything.
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return false;
    	}
    	cleanUp();
    	return true;
    }
    
    //sync started!
    private boolean getDeviceDetails() {
    	ResultSet rs;
    	int deviceid = -1;
    	String uniqueid, query;
    	uniqueid = ApplicationID.id();
    	if (uniqueid == null) {
    		//we've not got a unique id. likely because of an issue with the SD-card.
    		//try syncing anyway.
    		return true;
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.AppConfig.CONTENT_URI, null, null, null, null);
    	if (cur.moveToFirst()) {
    		deviceid = cur.getInt(cur.getColumnIndex(ContentDescriptor.AppConfig.Cols.DB_DEVICEID));
    	}
    	cur.close();
    	
    	if (!openConnection()) return false;
    	
    	//look for an id.
    	if (deviceid <=0) {
    		query = "SELECT id FROM sync WHERE device = '"+uniqueid+"';";
    		try {
    			rs = connection.startStatementQuery(query);
    			while (rs.next()) {
    				deviceid = rs.getInt("id");
    			}
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    		}
    		connection.closeStatementQuery();
    	}

    	if (deviceid <= 0) {
    		//we'll need to do an insert.
    		query = "INSERT INTO sync (device, clienttime, completed) VALUES ('"+uniqueid+"',"+
    		"to_timestamp("+(double)(new Date().getTime()/1000d)+"), false) RETURNING *;";
    	} else {
    		query = "UPDATE sync SET (servertime, clienttime, completed) = (now(), to_timestamp("+(double)(new Date().getTime()/1000d)
    				+"),false) WHERE id = "+deviceid+" RETURNING *;";
    	}
    	
    	if (!openConnection()) return false;
    	
    	try {
    		rs = connection.startStatementQuery(query);
    		while (rs.next()) {

    			 if (!rs.getBoolean("allowed_access")) {
    				//delete everything.
    				Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
    				editor.clear();
    				editor.commit();    				
    				ContentResolver.cancelSync(null, ContentDescriptor.AUTHORITY);
    				contentResolver.delete(ContentDescriptor.DROPTABLE_URI, null, null);
    				//return false!
    				return false;
    			}
    			
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.AppConfig.Cols.DB_DEVICEID, rs.getInt("id"));
    			if (deviceid <=0) {
    				contentResolver.insert(ContentDescriptor.AppConfig.CONTENT_URI, values);
    			}
    		}
    		rs.close();
    	} catch (SQLException e) {
			//if we got here either our connection was bad, or our query was bad.
			//if the query was bad, then we're probably using an older version of GymMaster (not > 320)
			//in which case, continue!.
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return true;
    	}
    	
    	return true;
    }
    
    private int uploadProspects() {
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Prospect.getKey())}, null);
    	while (cur.moveToNext()) {
    		int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID));
    		Cursor cur2 = contentResolver.query(ContentDescriptor.Enquiry.CONTENT_URI, null, ContentDescriptor.Enquiry.Cols._ID+" = ?",
    				new String[] {String.valueOf(rowid)}, null);
    		
    		if (!cur2.moveToFirst()) {
    			cur2.close();
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",new String[] {String.valueOf(rowid), 
    					cur.getString(ContentDescriptor.TableIndex.Values.Prospect.getKey())});
    			Log.w(TAG, "Could not find Prospect for Pending Upload, with Enquiry _id:"+rowid);
    			continue;
    		}
    		try {
    			result += connection.uploadEnquiry(cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.SNAME)),
    					cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.FNAME)),
    					cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.GENDER)),
    					cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.EMAIL)),
    					cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.DOB)),
    					cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.STREET)),
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.SUBURB)),
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.CITY)),
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.POSTAL)),
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.PHHOME)),
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.PHCELL)),
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Enquiry.Cols.NOTES)));
    		} catch (SQLException e) {
    			Log.e(TAG, "", e);	
    		} finally { //clean-up.
    			cur2.close();
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",new String[] {String.valueOf(rowid), 
    					String.valueOf(ContentDescriptor.TableIndex.Values.Prospect.getKey())});
    		}
    		
    		cleanUp();		
    	}
    	cur.close();
    	
    	
    	return result;
    }
    
    /**
     * This function assigns free ids to any pending member's that haven't got an id.
     * as it's possible to add a member via the signup app, but not get a member id from the
     * full app if the permissions aren't set.
     * 
     *  
     * @return number of memberids assigned.
     */
    private int assignMemberIds() {
    	int result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.DEVICESIGNUP+" = 't' AND ("
    			+"m."+ContentDescriptor.Member.Cols.MID+" IS NULL OR m."+ContentDescriptor.Member.Cols.MID+" <= 0)",null, null);
    	while (cur.moveToNext()) {
    		Cursor cur2 = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey())}, null);
    		if (!cur2.moveToFirst()) {break;}
    		int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols._ID));
    		int freeid = cur2.getInt(cur2.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
    		
    		ContentValues values = new ContentValues();
    		values.put(ContentDescriptor.Member.Cols.MID, freeid);
    		
    		result += contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols._ID+" = ?", 
    				new String[] {String.valueOf(rowid)});
    		
    		contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(freeid), 
    				String.valueOf(ContentDescriptor.TableIndex.Values.Member.getKey())});
    		
    		cur2.close();
    	}
    	cur.close();
    	
    	return result;
    }
    
    /**
     * Fix for a single member having multiple primary images. Usually caused by bad code earlier in hornets life-cycle.
     * @return
     */
    private int fixImages() {
    	Log.d(TAG, "Fixing Images");
    	int result = 0;
    	int prev_mid = 0;
    	
    	/* SELECT * FROM image 
    	 * WHERE 
    	 * ((SELECT count(*) FROM image i1 WHERE image.memberid = i1.memberid) >1) 
    	 * AND ((SELECT count(is_profile) FROM image i2 WHERE image.memberid = i2.memberid AND is_profile = 1) > 1) 
    	 * ORDER BY memberid;
    	 */
    	
    	cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, "((SELECT count(*) FROM "+ContentDescriptor.Image.NAME+" i1 WHERE "
    			+ContentDescriptor.Image.NAME+"."+ContentDescriptor.Image.Cols.MID+" = i1."+ContentDescriptor.Image.Cols.MID+") > 1) AND (("
    			+"SELECT count("+ContentDescriptor.Image.Cols.IS_PROFILE+") FROM "+ContentDescriptor.Image.NAME+" i2 WHERE "
    			+ContentDescriptor.Image.NAME+"."+ContentDescriptor.Image.Cols.MID+" = i2."+ContentDescriptor.Image.Cols.MID+" AND "
    			+ContentDescriptor.Image.Cols.IS_PROFILE+" = 1) > 1)", null, ContentDescriptor.Image.Cols.MID);
    			
    	while (cur.moveToNext()) {
    		int mid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID));
    		int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IID));
    		
    		if (mid == prev_mid) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.Image.Cols.IS_PROFILE, 0);
    			
    			result += contentResolver.update(ContentDescriptor.Image.CONTENT_URI, values, ContentDescriptor.Image.Cols.IID+" = ?",
    					new String[] {String.valueOf(rowid)});
    		} 
    		prev_mid = mid;
    	}
    	Log.d(TAG, "Fixed "+result+" Images");
    	cur.close();
    	return result;
    }
    
    private int updateImage() {
    	Log.d(TAG, "Updating Images");
    	int result = 0;
    	//fixImages();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey())}, null);
    	
    	while (cur.moveToNext()) {
    		int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID));
    		try {
	    		Cursor cur2 = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.ID+" = ?",
	    				new String[] {String.valueOf(rowid)}, null);
	    		if (!cur2.moveToFirst()) {
	    			throw new SQLException("Image _id not found in database. removing from pendings..");
	    		}
	    		boolean is_profile = (cur2.getInt(cur2.getColumnIndex(ContentDescriptor.Image.Cols.IS_PROFILE)) ==1);
	    		result += connection.updateImage(is_profile, 
	    				cur2.getString(cur2.getColumnIndex(ContentDescriptor.Image.Cols.DESCRIPTION)),
	    				cur2.getInt(cur2.getColumnIndex(ContentDescriptor.Image.Cols.IID)));
	    		cur2.close();
    		}catch (SQLException e){ 
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "UpdateImage:", e);
    		} finally {
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {String.valueOf(rowid),
    					String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey())});
    		}
    	}
    	
    	return result;
    }
    
    //TODO: finish writing a functional UI for this.
    public void duplicatePopupFix(Context context) {
    	setup(context);
    	
    	if (!openConnection()) {
    		Log.e(TAG, "no Connection");
    		return;
    	}
    	if (!getDeviceDetails()) {
   			return;
    	}
    	
    	try {
    		connection.fixDuplicatePopUp();
    	} catch (SQLException e) {
    		Log.e(TAG, "", e);
    		statusMessage = e.getLocalizedMessage();
    	}
    	contentResolver.delete(ContentDescriptor.Image.CONTENT_URI, null, null);
    	getImages(0, 0);
    	
    	updateDevice();
    	cleanUp();
    	connection.closeConnection();
    }
    
    private int getResourceType(long last_sync) {
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	try {
    		ResultSet rs = connection.getResourceType(last_sync);
    		while (rs.next()) {
	    		ContentValues values = new ContentValues();
	    		values.put(ContentDescriptor.ResourceType.Cols.ID, rs.getInt("id"));
	    		values.put(ContentDescriptor.ResourceType.Cols.NAME, rs.getString("name"));
	    		values.put(ContentDescriptor.ResourceType.Cols.PERIOD, rs.getString("period"));
	    		
	    		cur = contentResolver.query(ContentDescriptor.ResourceType.CONTENT_URI, null, ContentDescriptor.ResourceType.Cols.ID+" = ?",
	    				new String[] {rs.getString("id")}, null);
	    		if (cur.getCount()> 0) {
	    			contentResolver.update(ContentDescriptor.ResourceType.CONTENT_URI, values, ContentDescriptor.ResourceType.Cols.ID+" = ?",
	        				new String[] {rs.getString("id")});
	    		} else {
	    			contentResolver.insert(ContentDescriptor.ResourceType.CONTENT_URI, values);
	    		}
	    		cur.close();
	    		result +=1;
    		}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	return result;
    }
    
    private int getResourceID() {
    	int result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())}, null);
    	
    	int count = cur.getCount();
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	for (int i=(10-count);i>0; i--) {
    		try {
    			ResultSet rs = connection.startStatementQuery("select nextval('resource_id_seq');");
    			rs.next();
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Resource.getKey());
    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getInt("nextval"));
    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			result +=1;
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	cleanUp();
    	return result;
    }
    
    private int insertResource() {
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())}, null);
    	if (!openConnection()) {
    		return -1;
    	}
    	while (pendings.moveToNext()) {
    		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID+" = ?", 
    				new String[] {pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID))}, null);
    		if (cur.moveToFirst()) {
    			try {
    				connection.uploadResource(cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)),
    						cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)),
    						cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.RTID)), 
    						cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.HISTORY)));
    				result +=1;
    				
    				
    				//I don't know if this is going to like having contents deleted while a query is open..?
    				contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    						+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",new String[] {cur.getString(cur.getColumnIndex(
    						ContentDescriptor.Resource.Cols.ID)), String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())});
    			} catch (SQLException e) {
    				statusMessage = e.getLocalizedMessage();
    				Log.e(TAG, "", e);
    				return -2;
    			}
    		}
    		cur.close();
    	}
    	pendings.close();
    	cleanUp();
    	
    	return result;
    }
    
    private int updateResource(){
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())}, null);
    	while (pendings.moveToNext()) {
    		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID+" = ?",
    				new String[] {pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID))}, null);
    		if (cur.moveToFirst()) {
    			try {
    				connection.updateResource(cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.ID)),
    						cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.NAME)),
    						cur.getInt(cur.getColumnIndex(ContentDescriptor.Resource.Cols.RTID)), 
    						cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.HISTORY)));
    				result += 1;
    				
    				//I don't know if this is going to like having contents deleted while a query is open..?
    				contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
    						+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",new String[] {cur.getString(cur.getColumnIndex(
    						ContentDescriptor.Resource.Cols.ID)), String.valueOf(ContentDescriptor.TableIndex.Values.Resource.getKey())});
    			} catch (SQLException e) {
    				statusMessage = e.getLocalizedMessage();
    				Log.e(TAG, "", e);
    				return -2;
    			}
    		}
    		cur.close();
    	}
    	
    	return result;
    }
    
    private int getProgrammeGroups(long last_update) { 
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	try {
    		ResultSet rs;
    		try {
    			 rs = connection.getProgrammeGroups(last_update);
    		} catch (SQLException e) {
    			connection.closePreparedStatement();
    			rs = connection.getProgrammeGroups();
    		}
    			
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.ProgrammeGroup.Cols.ID, rs.getString("id"));
    			values.put(ContentDescriptor.ProgrammeGroup.Cols.NAME, rs.getString("name"));
    			values.put(ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD, rs.getString("issuecard"));
    			values.put(ContentDescriptor.ProgrammeGroup.Cols.HISTORIC, rs.getString("historic"));
    			
    			cur = contentResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, ContentDescriptor.ProgrammeGroup.Cols.ID+" = ?",
    					new String[] {rs.getString("id")}, null);
    			if (cur.moveToFirst()) { //update 
    				contentResolver.update(ContentDescriptor.ProgrammeGroup.CONTENT_URI, values, ContentDescriptor.ProgrammeGroup.Cols.ID+" = ?",
    						new String[] {rs.getString("id")});
    			} else {
    				contentResolver.insert(ContentDescriptor.ProgrammeGroup.CONTENT_URI, values);
    			}
    			cur.close();
    			result +=1;
    		}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, "", e);
    		return -2;
    	}
    	cleanUp();
    	
    	return result;
    }
    
    private int getProgrammeGroupID() {
    	int result = 0;
    	int free_count = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey())}, null);
    	free_count = cur.getCount();
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	for (int i=(10-free_count);i > 0; i--){
    		try {
    			ResultSet rs = connection.startStatementQuery("SELECT nextval('programmegroup_id_seq');");
    			
    			rs.next();
    			
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID,
    					ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey());
    			
    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			connection.closeStatementQuery();
    			result +=1;
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int updateProgrammeGroup() {
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey())}, null);
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	while (pendings.moveToNext()) {
    		String pendingid = pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID));
    		try {
	    		cur = contentResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, ContentDescriptor.ProgrammeGroup.Cols.ID+" = ?", 
	    				new String[] {pendingid}, null);
	    		if (!cur.moveToFirst()) {
	    			//throw a fit.
	    			Log.e(TAG, "Could not find row for id = "+pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID)),
	    					new Exception());
	    		} else {
	    			connection.updateProgrammeGroup(cur.getInt(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ID)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.NAME)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.HISTORIC)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD)));
	    			
	    			result +=1;
	    			connection.closePreparedStatement();
	    		}
    		} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		Log.e(TAG, "", e);
	    		/*Return? */
    		} finally {
    			contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {pendingid, 
    					String.valueOf(ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey())});
    		}
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int uploadProgrammeGroup() {
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey())}, null);
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	while (pendings.moveToNext()) {
    		String pendingid = pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID));
    		try {
    			cur = contentResolver.query(ContentDescriptor.ProgrammeGroup.CONTENT_URI, null, ContentDescriptor.ProgrammeGroup.Cols.ID+" = ?",
    					new String[] {pendingid}, null);
    			if (cur.moveToFirst()) {
	    			connection.uploadProgrammeGroup(cur.getInt(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ID)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.NAME)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.HISTORIC)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD)));
	    			result +=1;
	    			connection.closePreparedStatement();
    			} else {
    				Log.e(TAG, "Could not find ProgrammeGroup with id "+pendingid, new Exception());
    			}
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			/*return -2;DO WE RETURN HERE?*/
    		} finally {
    			contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    					+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {pendingid, String.valueOf(
    					ContentDescriptor.TableIndex.Values.ProgrammeGroup.getKey())});
    		}
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int getBookingTypeID(){
    	int result = 0;
    	int free_count = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Bookingtype.getKey())}, null);
    	free_count = cur.getCount();
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	for (int i=(10-free_count);i > 0; i--) {
    		try {
    			ResultSet rs = connection.startStatementQuery("SELECT nextval('bookingtype_id_seq');");
    			rs.next();
    			
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getString("nextval"));
    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID,
    					ContentDescriptor.TableIndex.Values.Bookingtype.getKey());
    			
    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			result +=1;
    			rs.close();
    			connection.closeStatementQuery();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int uploadBookingTypes() {
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Bookingtype.getKey())}, null);
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	while (pendings.moveToNext()) {
    		String id = pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID));
    		
    		cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.ID+" = ?", 
    				new String[] {id}, null);
    		if (cur.moveToFirst()) {
    			//do value checking here to make sure you're not passing empty strings.
    			String price, length, desc, maxbetween;
    			boolean online, msh_only, history;
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)).isEmpty()) {
    				price = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)).replace("$", "");
    				//pass it in without the $, as we'll append it to the front ourself..?
    			} else {
    				price = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH)).isEmpty()) {
    				length = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH));
    			} else {
    				length = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION)).isEmpty()) {
    				desc = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION));
    			} else {
    				desc = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN)).isEmpty()) {
    				maxbetween = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN));
    			} else {
    				maxbetween = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK)) != null &&
    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK)).compareTo("t") == 0) {
    				online = true;
    			} else {
    				online = false;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MS_ONLY)) != null &&
    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MS_ONLY)).compareTo("t") == 0) {
    				msh_only = true;
    			} else {
    				msh_only = false;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.HISTORY)) != null &&
    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.HISTORY)).compareTo("t") == 0) {
    				history = true;
    			} else {
    				history = false;
    			}
    			try {
    				connection.uploadBookingType(cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.NAME)),
	    					price, length, desc, maxbetween, online, msh_only, history);
    				result +=1;
    			} catch (SQLException e) {
    				statusMessage = e.getLocalizedMessage();
    				Log.e(TAG, "", e);
    				return -2;
    			}
    		}
    		cur.close();
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {id, String.valueOf(
					ContentDescriptor.TableIndex.Values.Bookingtype.getKey())});
    	}
    	
    	return result;
    }
    
    private int updateBookingTypes(){
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUpdates.CONTENT_URI, null, ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Bookingtype.getKey())}, null);
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	while (pendings.moveToNext()) {
    		String id = pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUpdates.Cols.ROWID));
    		cur = contentResolver.query(ContentDescriptor.Bookingtype.CONTENT_URI, null, ContentDescriptor.Bookingtype.Cols.ID+" = ?",
    				new String[] {id}, null);
    		if (cur.moveToFirst()) {
    			String price, length, desc, maxbetween;
    			boolean online, msh_only, history;
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)).isEmpty()) {
    				price = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.PRICE)).replace("$", "");
    				//pass it in without the $, as we'll append it to the front ourself..?
    			} else {
    				price = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH)).isEmpty()) {
    				length = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.LENGTH));
    			} else {
    				length = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION)).isEmpty()) {
    				desc = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.DESCRIPTION));
    			} else {
    				desc = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN)) != null &&
    					!cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN)).isEmpty()) {
    				maxbetween = cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MAXBETWEEN));
    			} else {
    				maxbetween = null;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK)) != null &&
    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.ONLINEBOOK)).compareTo("t") == 0) {
    				online = true;
    			} else {
    				online = false;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MS_ONLY)) != null &&
    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.MS_ONLY)).compareTo("t") == 0) {
    				msh_only = true;
    			} else {
    				msh_only = false;
    			}
    			
    			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.HISTORY)) != null &&
    					cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.HISTORY)).compareTo("t") == 0) {
    				history = true;
    			} else {
    				history = false;
    			}
    			try {
    				connection.updateBookingType(cur.getInt(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.BTID)),
    						cur.getString(cur.getColumnIndex(ContentDescriptor.Bookingtype.Cols.NAME)),
    						price, length, desc, maxbetween, online, msh_only, history);
    				result +=1;
    			} catch (SQLException e) {
    				statusMessage = e.getLocalizedMessage();
    				Log.e(TAG, "", e);
    				return -2;
    			}
    		}
    		cur.close();
    		contentResolver.delete(ContentDescriptor.PendingUpdates.CONTENT_URI, ContentDescriptor.PendingUpdates.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.PendingUpdates.Cols.TABLEID+" = ?", new String[] {id, String.valueOf(
    				ContentDescriptor.TableIndex.Values.Bookingtype.getKey())});
    	}
    	
    	return result;
    }
    
    private int getDoorID(){
    	int result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Door.getKey())}, null);
    	int free_count = cur.getCount();
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	for (int i=(5-free_count); i> 0; i--) {
    		try {
    			ResultSet rs = connection.startStatementQuery("SELECT nextval('door_id_seq');");
    			rs.next();
    			
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.FreeIds.Cols.ROWID, rs.getInt("nextval"));
    			values.put(ContentDescriptor.FreeIds.Cols.TABLEID, 
    					ContentDescriptor.TableIndex.Values.Door.getKey());
    			
    			contentResolver.insert(ContentDescriptor.FreeIds.CONTENT_URI, values);
    			result +=1;
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			Log.e(TAG, "", e);
    			return -2;
    		}
    	}
    	
    	cleanUp();
    	return result;
    }
    
    private int uploadDoor() {
    	int result = 0;
    	
    	Cursor pendings = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Door.getKey())}, null);
    	
    	if (!openConnection()) {
    		return -1;
    	}
    	
    	
    	while (pendings.moveToNext()) {
    		String id = pendings.getString(pendings.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID));
    		
    		cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, ContentDescriptor.Door.Cols._ID+" = ?",
    				new String[] {id}, null);
    		
    		if (cur.moveToFirst()) {
    			int status  = -1, booking_checkin = -1, concessionhandling = -1, companyid = -1;
    			String womenonly = null, showvisits = null;
    			
    			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.STATUS))) {
    				status = cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.STATUS));
    			}
    			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.BOOKING))) {
    				booking_checkin = cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.BOOKING));
    			}
    			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.CONCESSION))) {
    				concessionhandling = cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.CONCESSION));
    			}
    			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.COMPANY))) {
    				companyid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.COMPANY));
    			}
    			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.WOMENONLY))) {
    				womenonly = cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.WOMENONLY));
    			}
    			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Door.Cols.LASTVISITS))) {
    				showvisits = cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.LASTVISITS));
    			}
    			
    			try {
    				connection.uploadDoor(Integer.parseInt(id),
    						cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORNAME)),
    						status, booking_checkin, womenonly, concessionhandling, showvisits, companyid);
    			} catch (SQLException e) {
    				statusMessage = e.getLocalizedMessage();
    				Log.e(TAG, "", e);
    				return -2;
    			}
    		}
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.ROWID+" = ? AND "
    				+ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?", new String[] {id, String.valueOf(
    				ContentDescriptor.TableIndex.Values.Door.getKey())});
    	}
    	
    	return result;
    }
}