package com.treshna.hornet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.treshna.hornet.ContentDescriptor.Membership;
import com.treshna.hornet.ContentDescriptor.Resource;

/* TODO: consider refactoring this service out into:
 * 	Bookings, LastVisitors, Swipes, and Signups.
 */

public class HornetDBService extends Service {
	
	private static String TAG = "HORNET SERVICE";
	private static ContentResolver contentResolver = null;
    private static  Cursor cur = null; 
    private JDBCConnection connection = null;
    private String imageWhereQuery = "";
    private String statusMessage = "";
    private static Handler handler;
    private static int currentCall;
    private String resourceid;
    Context ctx;
    
    /****/
    private static NetworkThread thread;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override  
	public int onStartCommand(final Intent intent, int flags, int startId) {  //final ?
	   handler = new Handler();
	   
	   
	   SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	   connection = new JDBCConnection(preferences.getString("address", "-1"),preferences.getString("port", "-1"),
				 preferences.getString("database", "-1"), preferences.getString("username", "-1"),
				 preferences.getString("password", "-1"));
	   contentResolver = this.getContentResolver();
	   ctx = getApplicationContext();
	   
	   currentCall = intent.getIntExtra(Services.Statics.KEY, -1);
	   resourceid = intent.getStringExtra("newtime"); 
	   /**
	    * magical queue-ing, used to enforce one network operation at a time.
	    * 
	    */
	   
	   if (thread == null) {
		   thread = new NetworkThread();
	   }
	   thread.addNetwork(currentCall, resourceid, this);
	   if (!thread.isAlive() && thread.getState() == Thread.State.NEW) {
		   Log.v(TAG, "STARTING THREAD");
		   thread.start();
	   } else if (thread.getState() == Thread.State.TERMINATED || !thread.isAlive()) {
		   Log.v(TAG, "RESTARTING THREAD");
		   thread = new NetworkThread();
		   thread.addNetwork(currentCall, resourceid, this);
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
        Log.d(TAG, "Database Service destroyed");
    }
    /**
     * this functions starts a series of network operations determined by the call.
     * for a list of calls, see Services.Statics
     * 
     * It should only be run from a seperate thread, as otherwise the networking blocks the 
     * UI..
     */
    public void startNetworking(int currentcall, String theResource){
    	switch (currentCall){
 	   case (Services.Statics.LASTVISITORS): { //this should be run frequently
 		  /* new Thread(new Runnable() { //threading because Android blocks networking on main thread.
 			   public void run() {*/
		   	//Never show progress bar for last-visitors.
		   	thread.is_networking = true;
		   	long polling_start = PreferenceManager.getDefaultSharedPreferences(ctx).getLong(PollingHandler.POLLING_START, -1);
		   	long threehours = (180 * 60 * 1000);
		   	long currenttime = new Date().getTime();
		 
		   	if (currenttime > (polling_start+threehours)) {
		   		//polling has been running for over three hours, turn it off.
		   		//I should probably let myself know that the Polling has been turned off.
		   		Log.w(TAG, "Polling has been going for 3 hours, turning it off.");
		   		Services.getPollingHandler().stopPolling(false);
		   		Services.setPreference(ctx, "sync_frequency", "-1");
		   		return;
		   	}
		   	boolean result = getLastVisitors();
			if (result == true) { //If database query was successful, then look for images; else show toast.
				visitorImages();
			}
			uploadClass();
			uploadBookings();
			uploadMember();
			getMemberID();
			getBookingID();
			int sid_count = getSuspendID();
			if (sid_count < 0) {
				Services.showToast(getApplicationContext(), statusMessage, handler);
			}
			int upload_sid_count = uploadSuspends();
			if (upload_sid_count < 0) {
				Services.showToast(getApplicationContext(), statusMessage, handler);
			}
			Services.setPreference(ctx, "lastsync", String.valueOf(new Date().getTime()));
			Services.showToast(getApplicationContext(), statusMessage, handler);
			/*Broadcast an intent to let the app know that the sync has finished
			 * communicating with the server/updating the cache.
			 * App can now refresh the list. */
			
			thread.is_networking = false;
			Intent bcIntent = new Intent();
			bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
			sendBroadcast(bcIntent);
			//System.out.println("*Sending Intent, Stoping Service*");
			Log.v(TAG, "Sending Intent, Stopping Service");
 			 //  }}).start();
 		   	break;
 	   } 
 	   case (Services.Statics.UPLOAD):{
 		  statusMessage = "";
 		   /*new Thread(new Runnable() { //threading because Android blocks networking on main thread.
 		   		public void run() {*/
			thread.is_networking = true;
			
			int count = 0;
			uploadClass();
			uploadBookings();
			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, null, null, null);
			cur.moveToFirst();
			int i = 0;
			while (i < cur.getCount()){
				cur.moveToPosition(i);
				boolean result = uploadImage(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID)),
						cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID)),
						cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE)), 
						cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.DESCRIPTION)),
						cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IS_PROFILE)));
				count = (result == true) ? count +1 : count + 0;
				i +=1;
			}
			cur.close();
			if (count != 0) statusMessage = "Uploaded "+count+" Images";
			Services.showToast(getApplicationContext(), statusMessage, handler);
			//upload member
			uploadMember();
			
			
			thread.is_networking = false;
 			//}}).start();
 		   break;
 	   }
 	   /****/
 	   case (Services.Statics.BOOKING):{ //this should be rolled into last-visitors, if rid not set, then skip.
 		   statusMessage = null;
 		   thread.is_networking = true;
 		   int uploadcount, bookingcount, classcount;
 		 
 		  //check resource state.
 		  long last_sync = Long.parseLong(Services.getAppSettings(ctx, "b_lastsync"));
 		  int sresource = Integer.parseInt(Services.getAppSettings(ctx, "resourcelist"));
 		  if (last_sync <=10 && sresource < 0) {
 			  cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
 			  if (cur.getCount() > 0) {
 				  Services.setPreference(ctx, "resourcelist", "1");
 			  }
 			  cur.close();
 		  }
 		   
		   Services.showProgress(Services.getContext(), "Syncing Bookings From Server", handler, currentCall);
		   //upload bookings, then update bookings, then get bookings.
		   uploadcount = uploadBookings();
		   updateBookings(); //this should be in last visitors as well
		   bookingcount = getBookings();
		   bookingImages();
		   
		   classcount = getClasses();
		   
		   thread.is_networking = false;
		  
		   if (bookingcount >= 0) {
			   statusMessage = "Retrieved "+bookingcount+" Bookings\n Uploaded "+uploadcount+" Bookings";
		   }
		   Services.stopProgress(handler, currentCall);
		   Services.showToast(getApplicationContext(), statusMessage, handler);
		  
		   Intent bcIntent = new Intent();
		   if (bookingcount >= 0) {
			   bcIntent.putExtra(Services.Statics.IS_SUCCESSFUL, true);
		   } else {
			   bcIntent.putExtra(Services.Statics.IS_SUCCESSFUL, false);
		   }
		   bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
		   sendBroadcast(bcIntent);

		   Log.v(TAG, "Sending Intent, Stopping Service");
 		   break;
 	   }
 	   
 	   case (Services.Statics.SWIPE):{
 		   statusMessage = null;
 		   /*new Thread(new Runnable() {
 			public void run() {*/
 		   thread.is_networking = true;
			  
		   int result;
		   result = swipe();
		   if (result > 0) {};//TODO:
		   Services.showToast(getApplicationContext(), statusMessage, handler);
 		   /*}}).start();*/
 		   new Thread (new Runnable() { 
 				public void run() { 
 					try {
 						wait(1500);
 					} catch (Exception e ) {};
 					Intent updateInt = new Intent(ctx, HornetDBService.class);
 					updateInt.putExtra(Services.Statics.KEY, Services.Statics.LASTVISITORS);
 					ctx.startService(updateInt);
 				}}).start();
 		  
 		  thread.is_networking = false;
 		   break;
 	   }
 	 
 	   case (Services.Statics.FIRSTRUN):{ //this should be run nightly/weekly
 		   /*new Thread(new Runnable() {
 			   public void run() {*/
 		   thread.is_networking = true;
 		   	
		   Services.showProgress(Services.getContext(), "Syncing Local Database setting from Server", handler, currentCall);
		   //the above box should probably always show.
		   
		   int rcount = getResource();
		   int days = getOpenHours();
		   int mscount = getMembership();
		   int midcount = getMemberID();
		   if (midcount != 0) statusMessage = midcount+" Sign-up's available";
			   if (statusMessage != null && statusMessage.length() >3 ) {
				   Services.showToast(getApplicationContext(), statusMessage, handler);
			   }
			   //statusMessage = null;
			   int rscount = getResultStatus();
			   int btcount = getBookingType();
			   int mcount = getMember();
			   memberImages();
			   
			   getBookingID();
			  
			  thread.is_networking = false;
			   Services.stopProgress(handler, currentCall);
			   if (statusMessage != null) {
				   Services.showToast(getApplicationContext(), statusMessage, handler);
			   }
			   statusMessage = "Recieved "+mcount+" Members, "+mscount+" memberships, and "+rcount+" Resources";
			   Services.showToast(getApplicationContext(), statusMessage, handler);
			   //System.out.print("\n\nrcount:"+rcount+"  btcount:"+btcount+" rscount:"+rscount+"  mcount:"+mcount+" days:"+days);
			   Log.v(TAG, "rcount:"+rcount+"  btcount:"+btcount+"  rscount:"+rscount+"  mcount:"+mcount
					   +"  days:"+days);
			   
			   Services.showToast(getApplicationContext(),"Download Finished GymMaster Mobile will now restart",handler);
			   
			  Intent bcIntent = new Intent();
			  bcIntent.putExtra(Services.Statics.IS_RESTART, true);
			  bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
			  sendBroadcast(bcIntent);
			  //System.out.println("*Sending Intent, Stoping Service*");
			  Log.v(TAG, "Sending Intent, Stopping Service");
		  // }}).start();
 		   break;
 	   }
 	   /* The below service is not networking, but updates the local-database based on
 	    * user selections, It's been placed in this service for ease of use.
 	    * It's threaded so it doesn't block the UI.
 	    */
 	   case (Services.Statics.RESOURCESELECTED):{
 		   Log.v(TAG, "STARTING RESOURCE SETUP");
 		   	thread.is_networking = true;
 		   	Services.showProgress(Services.getContext(), "Setting up resource", handler, currentCall);
	   	  	resourceid = theResource;
	   	  	Log.v(TAG, "Selected Resource:"+resourceid);
	   	  	//rebuild times, then update the reference in date.
	   	  	setTime(); 
	   	  	setDate();
	   	  	updateOpenHours();
	   	  	Services.stopProgress(handler, currentCall);
	   	  	thread.is_networking = false;
			
	   	  	break;
 	   }
 	   case (Services.Statics.CLASSSWIPE):{
 		   thread.is_networking = true;
 		   int result;
 		   result = classSwipe();
 		   if (result <= 0) {
 			   Log.e(TAG, statusMessage);
 			   Log.e(TAG, "Class Swipe returned Error-Code:"+result);
 		   }
 		   thread.is_networking = false;
 	   }
 	   }
    }
    
    public static Handler getHandler(){
    	return handler;
    }
    
	public boolean getLastVisitors(){
    	    	
    	long this_sync = new Date().getTime();
    	if (!openConnection()) {
    		return false; //connection failed;
    	}
	
        	/*
        	 * The Below information handles queries. 
        	 */ //consider making FileSize hard capped. 10,000?
    		int fileSize = 10000; //
    		// file size, gets passed into byte, this number need only be an int though.
    		FileHandler fileHandler = new FileHandler(this);
    		String query = fileHandler.readFile(fileSize, "callumLastVisitors130416.sql");
    		ResultSet rs = null;
    		try {
    			rs = connection.startStatementQuery(query);
    		}catch (Exception e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    			return false;
    		}
    		String imageIDs = ""; //TODO make this only contain 1 instance of an ID (not 118, 118, 118).
        	int insertCount = 0;
        	int updateCount = 0;
        	ContentValues val = new ContentValues();
        	try {
        	while (rs.next()) {
        		/*Get Last ID, set this ID = ID+1*/
        		
    			cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, null, null,
    					ContentDescriptor.Visitor.Cols.ID+" DESC Limit 1");
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
						new String[] {rs.getString("datetime"), rs.getString("denyreason")},null);
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
	    			//System.out.print("\n\nNull Member");
        		}
        		/*Addded 2013-05-21*/
        		String membershipid = rs.getString("membershipid");
        		boolean msNull = rs.wasNull();
        		if (msNull == true) {
        			Random r = new Random();
	    			membershipid = "-"+Integer.toString(r.nextInt(100)); //force this int(?) to be negative
	    			//System.out.print("\n\nNull Membership");
        		}
        		val = new ContentValues();
        		val.put(ContentDescriptor.Visitor.Cols.ID, id);
        		val.put(ContentDescriptor.Visitor.Cols.MID, memberid);
        		val.put(ContentDescriptor.Visitor.Cols.MSID, membershipid);
        		val.put(ContentDescriptor.Visitor.Cols.DATETIME, rs.getString("datetime"));
        		val.put(ContentDescriptor.Visitor.Cols.DATE, rs.getString("sdate"));
        		val.put(ContentDescriptor.Visitor.Cols.TIME, rs.getString("stime12"));
        		val.put(ContentDescriptor.Visitor.Cols.DENY, rs.getString("denyreason"));
        		val.put(ContentDescriptor.Visitor.Cols.CARDNO, rs.getString("cardno"));
        		val.put(ContentDescriptor.Visitor.Cols.DOORNAME, rs.getString("doorname"));
        		val.put(ContentDescriptor.Visitor.Cols.LASTUPDATED, this_sync);
        		//todo last-update.
        		contentResolver.insert(ContentDescriptor.Visitor.CONTENT_URI, val);
        		if (isNull == false) {
        			//Check if the ID already exists in the database.
        			val = new ContentValues();
        			val.put(ContentDescriptor.Member.Cols.MID, memberid);
		        	
		        	//val.put(ContentDescriptor.Member.Cols.NAME, rs.getString("mname"));
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
	        		val.put(ContentDescriptor.Member.Cols.NOTES, rs.getString("mnotes"));
	        		val.put(ContentDescriptor.Member.Cols.TASK1, rs.getString("task1"));
	        		val.put(ContentDescriptor.Member.Cols.TASK2, rs.getString("task2"));
	        		val.put(ContentDescriptor.Member.Cols.TASK3, rs.getString("task3"));
	        		val.put(ContentDescriptor.Member.Cols.BOOK1, rs.getString("booking1"));
	        		val.put(ContentDescriptor.Member.Cols.BOOK2, rs.getString("booking2"));
	        		val.put(ContentDescriptor.Member.Cols.BOOK3, rs.getString("booking3"));
	        		val.put(ContentDescriptor.Member.Cols.LASTVISIT, rs.getString("lastvisit1"));
	        		
	        		
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
        			//System.out.print("\n\nMembershipID:"+membershipid);
        			/*Add the membership info to the membership table;*/
        			//System.out.print("\n\nMEMBERID: "+memberid);
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
	        			//	System.out.print("\n\nInserting ("+membershipid+","+rs.getString("msexpiry")+","+rs.getString("msvisits")
		        		//			+","+rs.getString("pname")+","+rs.getString("msstart")+","+memberid+","+rs.getString("cardno")
		        		//			+","+rs.getString("result"));
	        				contentResolver.insert(Membership.CONTENT_URI, val);
	        			} else if (cur.getCount() >0) {
	        				//System.out.print("\n\nUpdating "+membershipid+" WITH ("+rs.getString("msexpiry")+","+rs.getString("msvisits")
		        			//		+","+rs.getString("pname")+","+rs.getString("msstart")+","+memberid+","+rs.getString("cardno")
		        			//		+","+rs.getString("result"));
	        				contentResolver.update(Membership.CONTENT_URI, val, Membership.Cols.MSID+" = ?",
	        						new String[] {rs.getString("membershipid")});
	        			}
	        			cur.close();
        			}
        			
        			insertCount = insertCount + 1;
	        		imageIDs = imageIDs + " " + memberid + ",";
        		}
        	}
        	rs.close();
        	connection.closeStatementQuery();	
        	if ((insertCount == 0) && (updateCount == 0)){
        		//imageWhereQuery = imageWhereQuery + "-1";
        		statusMessage = "No new Results were received.";
        	} else {
        		statusMessage = (updateCount+insertCount)+" Rows Recieved from Server";
        		//imageWhereQuery = imageWhereQuery + imageIDs.substring(0, imageIDs.length()-1);
        	}
        	}
    	catch (SQLException e){
    		if (e.getCause() != null) {
	    		Throwable cause = e.getCause();
	    		if (cause.getMessage().compareToIgnoreCase("host=-1, port=-1") == 0) {
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
	    		e.printStackTrace();	
	    	}
    	}
        closeConnection();
        Services.setPreference(ctx, "lastsync", String.valueOf(this_sync));//String.valueOf(System.currentTimeMillis())   	
		return true;
    }
    
    public void visitorImages() {
    	    	
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.HOUR_OF_DAY, -6); //TODO:Use last_updated instead.
    	String lastsync = Services.getAppSettings(ctx, "lastsync");
    	
    	cur = contentResolver.query(ContentDescriptor.Visitor.CONTENT_URI, null, ContentDescriptor.Visitor.Cols.LASTUPDATED+" >= ?",
    			new String[] {lastsync}, null);
    	
    	queryServerForImage(cur, 1);
    }
    
    public void memberImages() {
    	cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, null, null, null);
    	queryServerForImage(cur, 0);
    }
    
    /*
     * Image ID's are set such that id 0 for membership is always the profile picture.
     * this means that getting the profile picture from the sdcard should be 0_<memberid>.jpg
     */
    private void queryServerForImage(Cursor cursor, int index){
    	// Fix this
    	boolean oldQuery;
    	ResultSet rs;
    	FileHandler fileHandler;
    	String query;
    	int fileSize, count;
    	ContentValues val;
    	byte[] is;
    	SimpleDateFormat dateFormat;
    	
    	oldQuery = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("image", false);
    	
    	imageWhereQuery = " AND memberid IN (";
    	imageWhereQuery += (cursor.getCount() == 0)? "-1 " : " ";
    	while (cursor.moveToNext()){
    		imageWhereQuery = imageWhereQuery + " "+ cursor.getString(index) + ",";
    	}
    	imageWhereQuery = imageWhereQuery.substring(0, imageWhereQuery.length()-1);
    	imageWhereQuery +=");";
		cursor.close();
		//System.out.println("\nQuerying server for image");
		Log.v(TAG, "Querying Server for images");
		//System.out.print("\n\n"+imageWhereQuery);
		Log.v(TAG, imageWhereQuery);
		if (!openConnection()) {
    		return ; //connection failed;
    	}
			/*
			 * Hard-coded file size is a pain in the ass, look into fixing this.
			 * If the filesize isn't exact, the query will fail (doesn't like having empty data in 
			 * the middle of the query).
			 */
		fileHandler = new FileHandler(this);
		rs = null;
		if (oldQuery != true){ 
        	fileSize = 187; //ImageQuery.sql = 149, multi = 187
        	// file size, gets passed into byte, this number need only be an int though.
        	query = fileHandler.readFile(fileSize, "multiImageQuery.sql");
        	query = query + imageWhereQuery;
        
        	try {
        		rs = connection.startStatementQuery(query);
        	} catch (SQLException e) {
        		statusMessage = e.getLocalizedMessage();
        		e.printStackTrace();
        		return;
        	}
		} else if (oldQuery == true) { //the table doesn't have description or is_profile
			fileSize = 162; 
        	// file size, gets passed into byte, this number need only be an int though.
        	
        	query = fileHandler.readFile(fileSize, "noDescImageQuery.sql");
        	query = query + imageWhereQuery;
        	
        	try {
        		rs = connection.startStatementQuery(query);
        	} catch (SQLException e) {
        		statusMessage = e.getLocalizedMessage();
        		e.printStackTrace();
        		return;
        	}
		}
        	val = new ContentValues();
        	count = 0;
        	dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
        	while (rs.next()) {
        		List<String> dates;
        		Date cacheDate, sDate;
        		boolean imgExists;
        		
        		
        		cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.MID
                		+" = "+rs.getString("memberid"), null, null);
            	cur.moveToFirst();
            	
            	//do some date checking. rather than rewriting images every time.
            	dates = new ArrayList<String>();
            	cacheDate = null;
        		while(!cur.isAfterLast()){
        			if ( cur.getCount() > 0 && cur.isNull(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE)) == false) {
        				String cDate = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE)),
        						"dd MMM yy hh:mm:ss aa", "yyyy-MM-dd");
            			dates.add(cDate);
            		}
        			cur.moveToNext();
            	}
            	imgExists = false;
            	sDate = dateFormat.parse(rs.getString("lastupdated"));
            	for (String date : dates){
            		cacheDate = dateFormat.parse(date);
            		if (cacheDate.compareTo(sDate) == 0) imgExists = true;
            	}
            	//if the image isn't found: add it!
            	if (imgExists == false){
            		int imgCount = 0, isProfile;
            		count +=1;
	            	imgCount = cur.getCount();
	            	String ssDate, description;
	            	
	            	if (oldQuery != true) {
		            	if (rs.getBoolean("is_profile") == true) { //isProfile
		            		imgCount = 0;
		            		
		            		if (cur.getCount() > 0) {
		            			val = new ContentValues();
		            			val.put(ContentDescriptor.Image.Cols.ID, cur.getCount());
		            			contentResolver.update(ContentDescriptor.Image.CONTENT_URI, val, ContentDescriptor.Image.Cols.ID +" = "+imgCount
		            					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+rs.getString("memberid"), null);
		            			fileHandler.renameFile("0_"+rs.getString("memberid"), cur.getCount()+"_"+rs.getString("memberid"));
		            		}
		            	}
	            	}
	            	else {
	            		imgCount = 0;
	            		if (cur.getCount() > 0) {
	            			val = new ContentValues();
	            			val.put(ContentDescriptor.Image.Cols.ID, cur.getCount());
	            			contentResolver.update(ContentDescriptor.Image.CONTENT_URI, val, ContentDescriptor.Image.Cols.ID +" = "+imgCount
	            					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+rs.getString("memberid"), null);
	            			fileHandler.renameFile("0_"+rs.getString("memberid"), cur.getCount()+"_"+rs.getString("memberid"));
	            		}
	            	}
	            	//cur.close();     		
		            	//Add some null handling as well.
	            	is = rs.getBytes(1); //imagedata
	            	
	        		fileHandler.writeFile(is, imgCount+"_"+rs.getString("memberid"));
	        		is = null;
	        		val.put(ContentDescriptor.Image.Cols.ID, imgCount);
	        		val.put(ContentDescriptor.Image.Cols.MID, rs.getString("memberid"));
	        		ssDate = Services.dateFormat(rs.getString("lastupdated"), "yyyy-MM-dd", "dd MMM yy hh:mm:ss aa");
	        		val.put(ContentDescriptor.Image.Cols.DATE, ssDate);
	        		description = null;
	        		if (oldQuery != true) {
	        			description = rs.getString("description");
	        		}
	        		if (description == null || description.length() < 2 || description.compareTo(" ") == 0) {
	        			description = "no description";
	        		}
	        		val.put(ContentDescriptor.Image.Cols.DESCRIPTION, description);
	        		isProfile = 0;
	        		if (oldQuery != true){
	        			isProfile = Services.booltoInt(rs.getBoolean("is_profile"));
	        		} else {
	        			isProfile = Services.booltoInt(rs.getBoolean(1)); //TODO: pretty sure this is broken, remove the rs.getBoolean
	        		}
	        		val.put(ContentDescriptor.Image.Cols.IS_PROFILE, isProfile);
	        		contentResolver.insert(ContentDescriptor.Image.CONTENT_URI, val);
            	}
            	cur.close();
            }
        	rs.close();
        	statusMessage = statusMessage +"\n"+count+" new Images were Recieved";
        	connection.closeStatementQuery();
        	
		} catch (SQLException e) {
			e.printStackTrace();
			statusMessage = statusMessage+"\nException with Images: "+e.getMessage();
		} catch (ParseException e) {
			// date formatting error occured.
			// this probably means the dates on the server are in a different format to what was expected.
			// TODO: fix this if it occurs.
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			closeConnection();
		}
		
	}
    
	public boolean uploadImage(int id, int mid, String cDate, String description, int isProfile) {
    	int idExists, imageSize, updateCount;
    	List<String> dateList;
    	SimpleDateFormat dateFormat;
    	ResultSet rs;
    	Date cacheImageDate;
    	FileHandler fileHandler;
    	byte[] image;
    	
    	idExists = 0;
    	dateList = new ArrayList<String>();
    	dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    	if (!openConnection()) {
    		return false; //connection failed;
    	}
  
    	try {	
    		rs = connection.imageCount(mid);
    	}catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		//e.printStackTrace();
    		return false;
    	}
    	try {
    		while (rs.next()){
    			//from imageCount(), getString(1)  = lastUpdated, 2 = created
    			idExists = rs.getRow();
    			dateList.add(rs.getString("created")); //does this want to be lastupdated? 
    		}
    		rs.close();
    		connection.closePreparedStatement();	
    	}catch(SQLException e) {
    		//e.printStackTrace();
    		statusMessage = e.getLocalizedMessage();
    		Log.e(TAG, e.getMessage());
    		//return false;
    	}
    	cDate = Services.dateFormat(cDate, "dd MMM yy hh:mm:ss aa", "yyyy-MM-dd HH:mm:ss");
    	cacheImageDate = null;
    	try {
    		cacheImageDate = dateFormat.parse(cDate);
    	} catch (ParseException e) {
    		//no date/image not in SQLite
    		Log.e("HORNET", "No Date for image found in SQLite");
    		connection.closePreparedStatement();
    		connection.closeConnection();
    		return false;
    	}

    	fileHandler = new FileHandler(getApplicationContext());
    	imageSize = 20000;
    	image = fileHandler.readImage(imageSize, Integer.toString(id)+"_"+Integer.toString(mid));
    	updateCount = 0;
    	
    	if (idExists >= 1) {
    	/* Handle Multiple images.
    	 * do date comparison for each image found, if not found do insert.
    	 * cannot do image updates.
    	 */	
    		Date sDate;
    		boolean doesExist;
    		
    		sDate = null;
    	 	doesExist = false;
    		for (String date : dateList) {
    			try { sDate = dateFormat.parse(date);  }
    			catch (ParseException e){/* non-proper date format given*/ }
    			//System.out.print("\n * Cache: "+cacheImageDate);
    			//System.out.print("\n * Server: "+sDate);
				if ((cacheImageDate.compareTo(sDate)) == 0) {
					//image with exact same date already exists.
					//System.out.println("**SKIPPING IMAGE** \n Server Copy is same as local");
					Log.v(TAG, "SKIPPING IMAGE, \nServer copy is same as local");
					doesExist = true;
				}
    		}
    		if (doesExist == false) {
    			try {
    				updateCount = connection.insertImage(image, mid, cacheImageDate, description, Services.isProfile(isProfile));
    				//System.out.print("\n\n**Insert Result: "+updateCount);
    				Log.v(TAG, "Insert Result: "+updateCount);
    			} catch (SQLException e) {
    				statusMessage = e.getLocalizedMessage();
    				e.printStackTrace();
    			}
    		}
    	}else if(idExists == 0){
    		try {
    			updateCount = connection.insertImage(image, mid, cacheImageDate, description, Services.isProfile(isProfile));
    			//System.out.println(updateCount);
    			Log.v(TAG, String.valueOf(updateCount));
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    		}
    	}
    	closeConnection();
	    return (updateCount != 0);	
    }
    
    private int uploadMember(){
    	//if isUsed = 1, upload member to database, else skip.
    	String email, medical, suburb, hphone, cphone, gender, dob;
    	String[] memberid;
    	int result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 1", null, null);
    	if (cur.getColumnCount() <= 0) {
    		cur.close();
    		Log.v(TAG, "No Pending members");
    		return 0;
    	}
    	cur.moveToFirst();
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	try {
	    	while (cur.getPosition() < cur.getCount()) { //TODO: ensure this matches the addMember required fields (2013-09-27)
	    		/*****************/ //null handling
	        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Pending.Cols.EMAIL))) email = "";
	        	else email = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.EMAIL));
	        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Pending.Cols.MEDICAL))) medical = "";
	        	else medical = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.MEDICAL));
	        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Pending.Cols.SUBURB))) suburb = "";
	        	else suburb = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.SUBURB));
	        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Pending.Cols.HPHONE))) hphone = "";
	        	else hphone = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.HPHONE));
	        	if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Pending.Cols.CPHONE))) cphone = "";
	        	else cphone = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.CPHONE));
	        	
	        	gender = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.GENDER));
	        	if (gender.compareTo(getString(R.string.radioMale))== 0) gender = "M";
	        	else gender = "F";
	        	dob = cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.DOB));
	        	dob = Services.dateFormat(dob, "dd/MM/yyyy", "yyyy-MM-dd");
	        	/****************/
	    		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.SIGNUP)).compareTo(
	    				getString(R.string.radioMember)) == 0){
	    			
	    			
	    			result += connection.addMember(cur.getInt(cur.getColumnIndex(ContentDescriptor.Pending.Cols.MID)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.SNAME)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.FNAME)),
	    					gender,
	    					email,
	    					dob, 
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.STREET)),
	    					suburb,
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.CITY)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.POSTAL)),
	    					hphone,
	    					cphone,
	    					medical);
	    			memberid = new String[] {cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.MID))};
	    			connection.closePreparedStatement();
	        		contentResolver.delete(ContentDescriptor.Pending.CONTENT_URI, ContentDescriptor.Pending.Cols.MID+" = ?", memberid);
	        		//after successful upload, remove from local database.
	    		} else {
	    			result += connection.addProspect(cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.SNAME)), 
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.FNAME)),
	    					gender,
	    					email,
	    					dob,
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.STREET)),
	    					suburb,
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.CITY)),
	    					cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.POSTAL)),
	    					hphone,
	    					cphone,
	    					medical);
	    			memberid = new String[] {cur.getString(cur.getColumnIndex(ContentDescriptor.Pending.Cols.ID))};
	    			connection.closePreparedStatement();
	        		contentResolver.delete(ContentDescriptor.Pending.CONTENT_URI, ContentDescriptor.Pending.Cols.ID+" = ?", memberid);
	    		}
	    		
	    		cur.moveToNext();
	    	}
    	} catch (SQLException e){
	    	statusMessage = e.getLocalizedMessage();
	    	e.printStackTrace();
	    }
    	closeConnection();
    	return result;
    }
    /* Retrieves and stores free memberID's from the database,
     * the memberID's are assigned to members upon signup through the app.
     */
    private int getMemberID() {
    	//when pending.rowCount < 10, get memberID until rowCount = 200
    	//always do upload first.
    	int count;
    	if (!openConnection()) {
    		return -1; //connection failed; see statusMessage for why
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 0", null, null);
    	count = cur.getCount();
    	cur.close();
    	if (count>= 3) {
    		// have 3 ID's already, don't bother getting more.
    		return 0;
    	}
    	for (int l=(5-count); l>=0;l -=1){ //Dru suggested 200?
    		count = 0;
    		ResultSet rs = null;
    		try {
    			int id;
    			rs = connection.startStatementQuery("select nextval('member_id_seq');");
    			rs.next();
    		
	    		ContentValues val = new ContentValues();
	    		//System.out.print(("\n\nMID: "+rs.getString("nextval"))); //nextval
	    		Log.v(TAG, "MID: "+rs.getString("nextval"));
	    		val.put(ContentDescriptor.Pending.Cols.MID, rs.getString("nextval"));
	    		
	    		cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 2", null, null);
	    		//System.out.print("\n\nPending Uploads (without ID):"+cur.getCount());
	    		Log.v(TAG, "Pending Uploads (without ID):"+cur.getCount());
	    		if (cur.getCount() != 0) {
	    			cur.moveToFirst();
	    			//System.out.print("\n\nUpdating Record where id ="+cur.getInt(cur.getColumnIndex(ContentDescriptor.Pending.Cols.ID)));
	    			//System.out.print("\n\nWith "+rs.getString("nextval"));
	    			Log.v(TAG, "Upading Record where id="+cur.getInt(cur.getColumnIndex(ContentDescriptor.Pending.Cols.ID)));
	    			Log.v(TAG, " with "+rs.getString("nextval"));
	    			
	    			id = cur.getInt(cur.getColumnIndex(ContentDescriptor.Pending.Cols.ID));
		    		cur.close();
	    			val.put(ContentDescriptor.Pending.Cols.ISUSED, 1);
	    			contentResolver.update(ContentDescriptor.Pending.CONTENT_URI, val, ContentDescriptor.Pending.Cols.ID+" = "+id, null);
	    			l +=1; //get another id, because this one was used?
	    		}
	    		else { 
	    			val.put(ContentDescriptor.Pending.Cols.ISUSED, 0);
	    			contentResolver.insert(ContentDescriptor.Pending.CONTENT_URI, val);
		    		
		    		cur.close();
	    		}
	    		count +=1;
	    		rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    			return 0;
    		}
    		
    	}
    	closeConnection();
    	return count;
    }
    
    private int getBookingID(){
    	int result = 0, count;
    	ResultSet rs;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" = 0", null, null);
    	count = cur.getCount();
    	cur.close();
    	if (count>= 16) {
    		// have 16 ID's already, don't bother getting more.
    		return 0;
    	}
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID+" < 0",
				null, null);
		if (cur.getCount() > 0) {
			//some booking's need ids.
			count = count - cur.getCount();
		}
		cur.close();
		
    	
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
	    		//System.out.print(("\n\nBID: "+rs.getString("nextval")));
				Log.v(TAG, "BID:"+rs.getString("nextval"));
	    		val.put(ContentDescriptor.Booking.Cols.BID, rs.getString("nextval"));
	    		
	    		if (is_update) {
	    			val.put(ContentDescriptor.Booking.Cols.LASTUPDATED, new Date().getTime());
	    			contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, val, ContentDescriptor.Booking.Cols.ID+" = ?",
	    					new String[] {String.valueOf(rowid)});
	    		} else {
	    			val.put(ContentDescriptor.Booking.Cols.LASTUPDATED, 0);
	    			contentResolver.insert(ContentDescriptor.Booking.CONTENT_URI, val);
	    		}
	    		
				result +=1;
    		} catch (SQLException e) {
    			e.printStackTrace();
    			statusMessage = e.getLocalizedMessage();
    		}
    		try {
    			connection.closeStatementQuery();
    			rs.close();
    		}catch (SQLException e) {
    			//doesn't matter, we're only closing the statement anyway.
    		}
    	}
    	closeConnection();
    	return result;
    }
    
    private int getResource(){
    	ResultSet rs = null;
    	int result = 0;
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	try {
    		rs = connection.getResource();
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
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    	}
    	closeConnection();
    	
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
    	int result;
    	ArrayList<String> bookinglist;
    	String b_lastsync;
    	
    	result = 0;
    	bookinglist = new ArrayList<String>();
    	b_lastsync = Services.getAppSettings(ctx, "b_lastsync");
    	//System.out.print("\n\n**Uploading Bookings with update after:"+b_lastsync);
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" > ? " +
    			"AND "+ContentDescriptor.Booking.Cols.IS_UPLOADED+" = 0",
    			new String[] {b_lastsync}, null);
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	while (cur.moveToNext()) { //why isn't this using ContentValues() ?
			Map<String, String> values = new HashMap<String, String>();
			values.put(ContentDescriptor.Booking.Cols.BID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
			values.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE)));
			values.put(ContentDescriptor.Booking.Cols.ETIME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ETIME)));
			values.put(ContentDescriptor.Booking.Cols.FNAME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.FNAME)));
			values.put(ContentDescriptor.Booking.Cols.SNAME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.SNAME)));
			values.put(ContentDescriptor.Booking.Cols.RESULT, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT)));
			values.put(ContentDescriptor.Booking.Cols.MID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.MID)));
			values.put(ContentDescriptor.Booking.Cols.STIME, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.STIME)));
			values.put(ContentDescriptor.Booking.Cols.ARRIVAL, Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.ARRIVAL)),
					"yyyyMMdd","yyyy-MM-dd"));
			values.put(ContentDescriptor.Booking.Cols.RID, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RID)));
			//System.out.print("\n\nOFFSET:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			Log.v(TAG, "OFFSET:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			values.put(ContentDescriptor.Booking.Cols.OFFSET, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			//System.out.print("\n\nBooking Modified:"+cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED)));
			Log.v(TAG, "Booking Modified:"+cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED)));
			Date lastupdate = new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED)));
			//System.out.print("\n\nLast-Update:"+lastupdate.getTime()+"\n");
			Log.v(TAG, "Last-Update:"+lastupdate.getTime());
			
			values.put(ContentDescriptor.Booking.Cols.LASTUPDATED, String.valueOf(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED))));
			
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
	    		if (state ==1) {
	    			//upload success
	    			//change the is_uploaded to 
	    			bookinglist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID)));
	    		}
	    		result += state;
	    		connection.closePreparedStatement();
	    	} catch (SQLException e) {
	    		e.printStackTrace();
	    		statusMessage = e.getLocalizedMessage();
	    	}
    	}
    	//System.out.print("\n\nUploaded "+result+" Bookings \n\n");
    	Log.v(TAG, "Uploaded "+result+" Bookings");
    	closeConnection();
    	
    	ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Booking.Cols.IS_UPLOADED, 1);
    	for (int j=0; j<bookinglist.size(); j+=1) {
    		contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
    				new String[] {bookinglist.get(j)});
    	}
    	
    	return result;
    }
    
    private int updateBookings(){
    	int result;
    	String lastSync;
    	
    	result = 0;
    	lastSync = Services.getAppSettings(ctx, "b_lastsync");
    	//System.out.print("\n\nLast Sync Was:"+lastSync);
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" > ? " +
    			"AND "+ContentDescriptor.Booking.Cols.RESULT+" != 0", //don't update empty bookings;
    			new String[] {lastSync}, null);//get the bookings that have changed since last sync.
    	//System.out.print("\n\n Updating "+cur.getCount()+" Bookings \n\n");
    	
    	if (cur.getCount()<= 0) { //no booking found for that
    		cur.close();
    		return 0;
    	}
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	while (cur.moveToNext()) {
    		int bookingid, resultstatus, bookingtypeid;
    		long lastupdate, checkin;
    		String notes;
    	
    		bookingid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
    		resultstatus = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT));
    		bookingtypeid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE));
    		lastupdate = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED));
    		checkin = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN));
    		notes = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.NOTES));
    		//System.out.print("\n\nLast update for booking:"+bookingid+" was "+lastupdate);
    		Log.v(TAG, "Last update for booking:"+bookingid+" was "+lastupdate);
    		try {
    			result += connection.updateBookings(bookingid, resultstatus, notes, lastupdate, bookingtypeid, checkin);
    			connection.closePreparedStatement();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    		}
    	}
    	Log.v(TAG, "Updated "+result+" Booking!");
    	closeConnection();
    	return result;
    }
    
    private int getBookings(){
    	ResultSet rs;
    	int result, lastrid, theResource;
    	Calendar cal;
    	java.sql.Date yesterday, tomorrow;
    	long this_sync, last_sync;
    	
    	rs = null;
    	result = 0;
    	cal = Calendar.getInstance();
    	cal.add(Calendar.MONTH, -1);
    	yesterday = new java.sql.Date(cal.getTime().getTime());
 
    	cal.add(Calendar.MONTH, +2);
    	tomorrow = new java.sql.Date(cal.getTime().getTime());
    	
    	this_sync = new Date().getTime();
    	last_sync = Long.parseLong(Services.getAppSettings(ctx, "b_lastsync"));
    	String rid = Services.getAppSettings(ctx, "last_rid");
    	if (rid == null || rid.compareTo("null") == 0) {
    		lastrid = -1;
    	} else {
    		lastrid = Integer.decode(rid);
    	}
    	
    	
    	theResource = Integer.decode(Services.getAppSettings(this, "resourcelist"));
    	if (lastrid != theResource) {
    		last_sync = 0;
    	}
    	if (theResource < 0 ) {
    		statusMessage = "please set resource in the application settings";
    		return -1;
    	}
    	cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, null, null, null);
    	if (cur.getCount() <= 0) {
    		Log.e(TAG, "**No Rows returned by BookingTime");
    		resourceid = String.valueOf(theResource);
	   	  	Log.v(TAG, "Selected Resource:"+resourceid);
	   	  	//rebuild times, then update the reference in date.
	   	  	setTime(); 
	   	  	setDate();
	   	  	updateOpenHours();
    	}
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	
    	try {
    		rs = connection.getBookings(yesterday, tomorrow, theResource, last_sync);
    		Log.v(TAG, "getBookings() Row Count"+rs.getFetchSize()); 
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
	    		
	    		date = Services.dateFormat(rs.getString("arrival"), "yyyy-MM-dd", "yyyyMMdd");
	    		starttime = getTime(rs.getString("startid"), contentResolver );
	    		endtime = getTime(rs.getString("endid"), contentResolver);
	    		
	    		val.put(ContentDescriptor.Booking.Cols.STIMEID, starttime);
	    		val.put(ContentDescriptor.Booking.Cols.ETIMEID, endtime);
	    		val.put(ContentDescriptor.Booking.Cols.STIME, rs.getString("startid"));
	    		val.put(ContentDescriptor.Booking.Cols.ETIME, rs.getString("endtime"));
	    		val.put(ContentDescriptor.Booking.Cols.BID, rs.getString("bookingid"));
	    		val.put(ContentDescriptor.Booking.Cols.BOOKINGTYPE, rs.getString("bookingtypeid"));
	    		val.put(ContentDescriptor.Booking.Cols.NOTES, rs.getString("notes"));
	    		val.put(ContentDescriptor.Booking.Cols.RESULT, rs.getString("result"));
	    		val.put(ContentDescriptor.Booking.Cols.MID, rs.getString("memberid"));
	    		val.put(ContentDescriptor.Booking.Cols.MSID, rs.getString("membershipid"));
	    		val.put(ContentDescriptor.Booking.Cols.RID, rs.getString("resourceid"));
	    		val.put(ContentDescriptor.Booking.Cols.ARRIVAL, Integer.decode(date));
	    		
	    		val.put(ContentDescriptor.Booking.Cols.CLASSID, rs.getInt("classid"));
	    		if (rs.wasNull()) {
	    			val.remove(ContentDescriptor.Booking.Cols.CLASSID);
	    		}
	    		val.put(ContentDescriptor.Booking.Cols.PARENTID, rs.getInt("parentid"));
	    		if (rs.wasNull()) {
	    			has_parent = false;
	    			val.remove(ContentDescriptor.Booking.Cols.PARENTID);
	    		}
	    		
	    		/** 
	    		 * last-update is equal to the last sync time, as using the database's last-update time can
	    		 * cause issues when the times don't match (e.g. if the last-update time on the database is ahead, it'll break thing).. 
	    		 */
	    		val.put(ContentDescriptor.Booking.Cols.LASTUPDATED, this_sync);
	    		
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
	    		
	    			status= contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, val, 
	    					ContentDescriptor.Booking.Cols.BID+" = ? ",
	    					new String[] {rs.getString("bookingid")});
	    			if (status == 0) {
	    				//update failed
	    				Log.e(TAG, "Booking Update Failed for id:"+rs.getString("bookingid"));
	    			}
	    			result +=status;
	    			if (rs.getInt("result") ==  5) { //booking-cancelled, delete it from the bookingtime table.
	    				contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI, ContentDescriptor.BookingTime.Cols.BID+" = ?",
	    						new String[] {rs.getString("bookingid")});
	    			}
	    		}
	    		if (!has_parent) {
		    		timeid = starttime;
		    		while (timeid<=endtime) {
		    			val = new ContentValues();
		    			val.put(ContentDescriptor.BookingTime.Cols.BID, rs.getString("bookingid"));
		    			val.put(ContentDescriptor.BookingTime.Cols.RID, rs.getString("resourceid"));
		    			val.put(ContentDescriptor.BookingTime.Cols.TIMEID, timeid);
		    			val.put(ContentDescriptor.BookingTime.Cols.ARRIVAL, Integer.decode(date));
		    			
		    			cur = contentResolver.query(ContentDescriptor.BookingTime.CONTENT_URI, null, "bt."+ContentDescriptor.BookingTime.Cols.BID+" = ? AND bt."
		    					+ContentDescriptor.BookingTime.Cols.RID+" = ? AND bt."+ContentDescriptor.BookingTime.Cols.TIMEID+" = ? AND bt."
		    					+ContentDescriptor.BookingTime.Cols.ARRIVAL+" = ?", new String[] {rs.getString("bookingid"), rs.getString("resourceid"),
		    					String.valueOf(timeid), date}, null);
		    			
		    			if (cur.getCount() == 0 ) { //insert
		    				cur.close();
		    				contentResolver.insert(ContentDescriptor.BookingTime.CONTENT_URI, val);
		    			} else { //update 
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
    	} catch (SQLException e) {
    		e.printStackTrace();
    		statusMessage = e.getLocalizedMessage();
    	}
    	closeConnection();

    	Log.v(TAG, "BookingCount:"+result);
    	Log.v(TAG,"Bookings Sync'd at:"+this_sync);
    	Services.setPreference(ctx, "last_rid", String.valueOf(resourceid));
    	Services.setPreference(ctx, "b_lastsync", String.valueOf(this_sync));//String.valueOf(System.currentTimeMillis())
    	return result;
    }
    
    @SuppressWarnings("unused")
	private int getBookingType(){
    	
    	int result = 0;
    	ResultSet rs = null;
    	
    	result = contentResolver.delete(ContentDescriptor.Bookingtype.CONTENT_URI,null, null);

    	final int CACI = 0; //TODO: fix this
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	try {
    		rs = (CACI != 0)?connection.getBookingTypesValid() : connection.getBookingTypes();
    		while (rs.next()) {
    			
    				ContentValues values = new ContentValues();
    				values.put(ContentDescriptor.Bookingtype.Cols.BTID, rs.getString("id"));
    				values.put(ContentDescriptor.Bookingtype.Cols.NAME, rs.getString("name"));
    				values.put(ContentDescriptor.Bookingtype.Cols.PRICE, rs.getString("price"));
    				values.put(ContentDescriptor.Bookingtype.Cols.EXTERNAL, rs.getString("externalname"));
    				if (CACI != 0){ //only applies to clinicMaster ?
	    				values.put(ContentDescriptor.Bookingtype.Cols.VALIDFROM, rs.getString("validfrom"));
	    				values.put(ContentDescriptor.Bookingtype.Cols.VALIDTO, rs.getString("validto"));
    				}
	    			
    				contentResolver.insert(ContentDescriptor.Bookingtype.CONTENT_URI, values);
    				result +=1;
    		}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	closeConnection();
    	
    	return result;
    }
    
    public static int getTime(String time, ContentResolver contentResolver){
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
		return result;
		
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
	    		//System.out.print("\n\n"+rs.getString(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
	    		result +=1;
	    	}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	closeConnection();
    	return result;
    }
    
    private void setDate(){
    	Calendar current, maximum;
    	Date date;
    	int currentDay;
    	ContentValues values;
    	
    	contentResolver.delete(ContentDescriptor.Date.CONTENT_URI, null, null);
    	//System.out.print("\n\nSetting Date!!\n\n");
    	Log.v(TAG, "Setting Date!!");
    	
    	current = Calendar.getInstance();
    	maximum = Calendar.getInstance();
    	maximum.set(Calendar.MONTH, (current.get(Calendar.MONTH)+1));
    	current.set(Calendar.MONTH, (current.get(Calendar.MONTH)-1));
		while (current.get(Calendar.DAY_OF_YEAR) <= maximum.get(Calendar.DAY_OF_YEAR)) { //TODO: how many dates to store?
			date = current.getTime();
			currentDay = current.get(Calendar.DAY_OF_WEEK);
			values = new ContentValues();
			values.put(ContentDescriptor.Date.Cols.DATE, Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
			values.put(ContentDescriptor.Date.Cols.DAYOFWEEK, currentDay);
			contentResolver.insert(ContentDescriptor.Date.CONTENT_URI, values);
			//System.out.print("\n\nDate:"+Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
			//System.out.print("\n\ndayofweek:"+currentDay);
			current.add(Calendar.DATE, 1);
		}
		//System.out.print("\n\nFinished Setting Date!!\n\n");
		Log.v(TAG, "Finished Setting Date");
    }
    
    private void setTime(){
    	//System.out.print("\n\nSetting Time!!\n\n");
    	Log.v(TAG, "Setting Time!!");
		contentResolver.delete(ContentDescriptor.Time.CONTENT_URI, null, null); 
		int interval;
		Calendar day, upperlimit, lowerlimit;
		
		{
			//System.out.print("\n\nresource:"+resourceid);
			Log.v(TAG, "resource:"+resourceid);
			if (resourceid == null) {
				resourceid = Services.getAppSettings(this, "resourcelist");
			}
			
			cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, new String[] {Resource.Cols.PERIOD}, Resource.Cols.ID+" = ?", 
					new String[] {resourceid}, null);
			
			if (cur.getCount() <= 0) {
				cur.close();
			} else {
				cur.moveToFirst();
				String intv;
				int intvl;
				
				//System.out.print("\n\nPeriod:"+cur.getString(0));
				Log.v(TAG, "Period:"+cur.getString(0));
				intv = cur.getString(0).replaceAll(":", "");
				intvl = (Integer.parseInt(intv)/100);
				Services.setPreference(ctx, "timeslot", String.valueOf(intvl));
				cur.close();
			}
		}
		
		interval = Integer.decode(Services.getAppSettings(this, "timeslot"));
		//System.out.print("\n\nInterval:"+interval);
		Log.v(TAG, "Interval:"+interval);
		if (interval != 15 && interval != 30 && interval != 60) interval = 15; //default every 15 minutes.
		day = Calendar.getInstance();
		day.add(Calendar.DATE, -1);
		
		upperlimit = Calendar.getInstance();
		upperlimit.set(Calendar.HOUR_OF_DAY, 23);
		upperlimit.set(Calendar.MINUTE, 59);
		upperlimit.set(Calendar.SECOND, 1);
		
		//System.out.print("\n\nLowerLimit:"+llimit);
		lowerlimit = Calendar.getInstance();
		lowerlimit.set(Calendar.HOUR_OF_DAY, 0); 
		lowerlimit.set(Calendar.MINUTE, 0);
		lowerlimit.set(Calendar.SECOND, 0);
		
			while (lowerlimit.getTime().before(upperlimit.getTime())) {
				ContentValues values;
				String time;
				
				values = new ContentValues();
				time = lowerlimit.getTime().toString();
				time = Services.dateFormat(time, "EEE MMM dd HH:mm:ss", "HH:mm:ss");
				//System.out.print("\n\nAfter Time:"+time);
				values.put(ContentDescriptor.Time.Cols.TIME, time);
				contentResolver.insert(ContentDescriptor.Time.CONTENT_URI, values);
				lowerlimit.add(Calendar.MINUTE, interval);
				//System.out.print("\n\nID:"+id+"  TIME:"+time+" DATE:"+date);
			}
    }
    
    private int getMember(){
    	//System.out.print("\n\nGetting MemberID's");
    	Log.v(TAG, "Getting MemberID's");
    	int result;
    	ResultSet rs;
    	
    	result = 0;
    	rs = null;
    	contentResolver.delete(ContentDescriptor.Member.CONTENT_URI, null, null);
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	try {
    		rs = connection.getMembers();
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.Member.Cols.MID, rs.getString("id"));
    			values.put(ContentDescriptor.Member.Cols.FNAME, rs.getString("firstname"));
    			values.put(ContentDescriptor.Member.Cols.SNAME, rs.getString("surname"));
    			values.put(ContentDescriptor.Member.Cols.HAPPINESS, rs.getString("happiness"));
    			values.put(ContentDescriptor.Member.Cols.PHHOME, rs.getString("mphhome"));
    			values.put(ContentDescriptor.Member.Cols.PHWORK, rs.getString("mphwork"));
    			values.put(ContentDescriptor.Member.Cols.PHCELL, rs.getString("mphcell"));
    			values.put(ContentDescriptor.Member.Cols.EMAIL, rs.getString("memail"));
    			values.put(ContentDescriptor.Member.Cols.NOTES, rs.getString("mnotes"));
    			values.put(ContentDescriptor.Member.Cols.STATUS, rs.getString("status"));
    			contentResolver.insert(ContentDescriptor.Member.CONTENT_URI, values);
    			result +=1;
    		}
    	} catch (SQLException e){
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	closeConnection();
    	
    	return result;
    }
    
    private int getMembership() {
    	int result = 0;
    	ResultSet rs = null;
    	
    	contentResolver.delete(ContentDescriptor.Membership.CONTENT_URI, null, null);
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	try {
    		rs = connection.getMembership();
    		while (rs.next()){
    			ContentValues values = new ContentValues();
    			
    			values.put(ContentDescriptor.Membership.Cols.MID, rs.getString("memberid"));
    			values.put(ContentDescriptor.Membership.Cols.MSID, rs.getString("id"));
    			values.put(ContentDescriptor.Membership.Cols.CARDNO, rs.getString("cardno")); //this can be null
    			values.put(ContentDescriptor.Membership.Cols.MSSTART, rs.getString("startdate"));
    			values.put(ContentDescriptor.Membership.Cols.EXPIRERY, rs.getString("enddate"));
    			values.put(ContentDescriptor.Membership.Cols.PNAME, rs.getString("name"));
    			values.put(ContentDescriptor.Membership.Cols.VISITS, rs.getString("concession"));
    			values.put(ContentDescriptor.Membership.Cols.LASTUPDATED, rs.getString("lastupdate"));
    			
    			/*System.out.print("\n\n Inserting ("+rs.getString("memberid")+", "+rs.getString("id")+","
    					+rs.getString("cardno")+", "+rs.getString("startdate")+", "+rs.getString("enddate")
    					+", "+rs.getString("name")+", "+rs.getString("concession")+", "+rs.getString("lastupdate")+")\n");*/
    			contentResolver.insert(ContentDescriptor.Membership.CONTENT_URI, values);
    			result +=1;
    		}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	closeConnection();
    	return result;
    }
    
    private void bookingImages(){
    	String b_lastsync;
    	
    	b_lastsync = Services.getAppSettings(ctx, "b_lastsync");
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" > ?", 
    			new String[] {b_lastsync}, null);
    	queryServerForImage(cur, 12);
    	cur.close();
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
    			continue;
    		}
    		//System.out.print("\n\nid:"+id);
    		//System.out.print("\n\ndoor:"+door);
    		Log.v(TAG, "id:"+id);
    		Log.v(TAG, "door:"+door);
    		if (!openConnection()) {
        		return -1; //connection failed;
        	}
    		try {
    			ResultSet rs;
    			String tempmess;
    			
	    		rs = connection.tagInsert(door, id);
	    		rs.close();
	    		connection.closePreparedStatement();
	    		
	    		rs = connection.getTagUpdate(door);
	    		tempmess = null;
	    		while (rs.next()){
	    			tempmess = rs.getString("message")+" "+rs.getString("message2");
	    			statusMessage = tempmess;
	    			//System.out.println(tempmess);
	    			Log.v(TAG, tempmess);
	    			break;
	    		}	
	    		rs.close();
	    		connection.closePreparedStatement();
		    	
	    	} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		e.printStackTrace();
	    	}
    		closeConnection();
    	}
    	cur.close();
    	contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, null, null);
		
    	return result;
    }
    
    private int getOpenHours(){
    	int result = 0;
    	ResultSet rs = null;
    	
    	contentResolver.delete(ContentDescriptor.OpenTime.CONTENT_URI, null, null);
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	try {
    		rs = connection.getOpenHours();
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.OpenTime.Cols.DAYOFWEEK, (rs.getInt("dayofweek")+1));
    			values.put(ContentDescriptor.OpenTime.Cols.OPENTIME, rs.getString("opentime"));
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSETIME, rs.getString("closetime"));
    			values.put(ContentDescriptor.OpenTime.Cols.NAME, rs.getString("name"));
    			
    			contentResolver.insert(ContentDescriptor.OpenTime.CONTENT_URI, values);
    			result +=1;
    		}
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	closeConnection();
    	return result;
    }
    
    private int updateOpenHours(){
    	//System.out.print("\n\nUpdating Open Hours\n");
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
			new String[] {idList.get(i)[1]}, null);
    		if (idList.get(i)[1].compareTo("-1") == 0) {
    			//no starttime/endtime set for this day, what should I do?
    			//System.out.print("\n\nNO STARTTIME SET***\n\n");
    			Log.v(TAG, "NO STARTTIME SET***");
    			cur.close();
    			values.put(ContentDescriptor.OpenTime.Cols.OPENID, 0);
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, 0);
    		}
    		else if (cur.getCount() == 0) {
    			cur.close();
    			cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" >= ?",
    					new String[] {idList.get(i)[1]}, ContentDescriptor.Time.Cols.TIME+" ASC LIMIT 1");
    			cur.moveToFirst();
        		//System.out.print("\n\nOpenId:"+cur.getString(0));
        		values.put(ContentDescriptor.OpenTime.Cols.OPENID, cur.getString(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))); 
        		cur.close();
        		
        		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
    			new String[] {idList.get(i)[2]}, null);
    			//System.out.print("\n\nCloseId:"+cur.getCount());
    			if (cur.getCount() <= 0) {
    				cur.close();
    				cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, null, null, ContentDescriptor.Time.Cols.ID+" DESC");
    			}
    			cur.moveToFirst();
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, (cur.getInt(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))-1));
    			cur.close();
    		}  else {
    			cur.moveToFirst();
        		//System.out.print("\n\nOpenId:"+cur.getString(0));
        		values.put(ContentDescriptor.OpenTime.Cols.OPENID, cur.getString(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID))); 
        		cur.close();
        		
        		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
    			new String[] {idList.get(i)[2]}, null);
    			//System.out.print("\n\nCloseId:"+cur.getCount());
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
    
    
    private int uploadClass(){
    	int result = 0;
    	ArrayList<String> idlist;
    	
    	idlist = new ArrayList<String>();
    	
    	cur = contentResolver.query(ContentDescriptor.PendingUploads.CONTENT_URI, null, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ?",
    			new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Class.getKey())}, null);
    	
    	while (cur.moveToNext())
    	{
    		idlist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.PendingUploads.Cols.ROWID)));
    	}
    	cur.close();
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	for (int i = 0; i < idlist.size(); i +=1) {
    		String name, freq, sdate, stime, etime;
    		int cid, rid, max_st, rowid;
    		ResultSet rs;
    		
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
    		
    		try {
    			rs = connection.uploadClass(name, max_st);
    			rs.next(); //move to first
        		cid = rs.getInt(1);
    		} catch (SQLException e) {
    			//error occured with sql on upload class.
    			e.printStackTrace();
    			statusMessage = e.getLocalizedMessage();
    			return -2;
    		}
    		connection.closePreparedStatement();
    		
    		try {
    			connection.uploadRecurrence(freq, sdate, stime, etime, cid, rid);
    		} catch (SQLException e) {
    			//error occured with sql on upload recurring;
    			e.printStackTrace();
    			statusMessage = e.getLocalizedMessage();
    			return -3;
    		}
    		
    		//if we got here the inserts must've been successful. so remove the pending upload.
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Class.getKey()),
    					String.valueOf(rowid)});
    		
    		result += 1;
    	}
    	closeConnection();
    	
    	return result;
    }
    /**
     * after getting bookings, we need to get classes.
     * when swiping in tags for classes, we need to check that
     * the class member limit has not been reached.
     * @return
     */
    
    private int getClasses() {
    	int result = 0;
    	
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	ResultSet rs = null;
    	try {
    		rs = connection.getClasses();
    		//do handling here.
    		while (rs.next()){
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.Class.Cols.CID, rs.getString("id"));
    			values.put(ContentDescriptor.Class.Cols.NAME, rs.getString("name"));
    			values.put(ContentDescriptor.Class.Cols.DESC, rs.getString("description"));
    			values.put(ContentDescriptor.Class.Cols.MAX_ST, rs.getString("max_students"));
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
    		return -2;
    	}
    	closeConnection();
    	
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
    	while (bookingswipecount > 0) {
    		
    		String serial, memberid, classid, membershipid;
    		int cardno;
    		ResultSet rs;
    		
    		if (cur == null || cur.isClosed()) {
    			cur = contentResolver.query(ContentDescriptor.Swipe.CONTENT_URI, null, ContentDescriptor.Swipe.Cols.DOOR+" < 0",
    	    			null, null);
    		}
    		
    		cur.moveToFirst();
    		if (cur.getCount() <= 0) return 0;
    		
    		serial = cur.getString(cur.getColumnIndex(ContentDescriptor.Swipe.Cols.ID)); 
    		
    		//rs = connection.findMemberBySerial(serial);
    		//select id FROM idcard where serial = 'Mx1bc34e';
    		//select memberid from membership where cardno = 168;
    		try {
    			rs = connection.findCardBySerial(serial);
    			Log.v(TAG, "findCard By Serial Size:"+rs.getFetchSize());
    			if (!rs.next()) {
    				//something went wrong, (sometimes the serial is just an int?)
    				//I should probably delete the serial from the swipe table.
    				cur.close();
    				contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ? ",
    						new String[] {serial});
    				return -4; //only 1 row.
    			}
    		
    			cardno = rs.getInt("id");
    			if (rs.wasNull()) {
    				//tag not found
    				statusMessage = "tag not found in database";
    				contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ?",
    						new String[] {serial});
    				continue;
    			}
    			rs.close();
    			connection.closePreparedStatement();
    			
    			rs = connection.findMemberByCard(cardno);
    			rs.next();
    			
    			memberid = rs.getString("memberid");
    			membershipid = rs.getString("membershipid");
    			Log.v(TAG,"Class-Swipe Member-ID:"+memberid);
    			if (rs.wasNull()) {
    				//tag empty.
    				statusMessage = "tag not assigned to a member";
    				contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, ContentDescriptor.Swipe.Cols.ID+" = ?",
    						new String[] {serial});
    				continue;
    			}
    			rs.close();
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    			return -1;
    		}
    		connection.closePreparedStatement();
    		
    		classid = cur.getString(cur.getColumnIndex(ContentDescriptor.Swipe.Cols.DOOR)).substring(1);
    		cur.close();
			
			cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.PARENTID+" = ? AND "
					+ContentDescriptor.Booking.Cols.MID+" = ?",
					new String[] {classid, memberid}, null);
    		if (cur.getCount() <= 0) {
    			// the member is NOT already booked in for the class,
    			// the function should add them.
    			Log.v(TAG, "Adding New Member to the booking.");
    			/**
    			 * TODO: get the following values:
    			 * 	- Resourceid				- DONE
    			 * 	- Firstname & lastname		- DONE
    			 *  - startid & endid			- DONE
    			 *  - arrival					- DONE
    			 *  - parentid					- DONE
    			 *  - bookingid					- DONE
    			 *  - stime & etime				- DONE
    			 *  - Result					- DONE
    			 *  - checkin					- DONE
    			 *  - BookingType!!!			- DONE
    			 *  - Offset					- DONE
    			 */
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
    			cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" = 0",
    					null, null);
    			if (cur.getCount() > 0) {
    				cur.moveToFirst();
    				bookingid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
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
    			values.put(ContentDescriptor.Booking.Cols.IS_UPLOADED, 0);
    			values.put(ContentDescriptor.Booking.Cols.LASTUPDATED, new Date().getTime());
    			if (bookingid > 0) {
    				contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
    						new String[] {String.valueOf(bookingid)});
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
    	closeConnection();
    	
    	return result;
    }
    
    private boolean openConnection(){
    	try {
    		connection.openConnection();
    	} catch (SQLException e) {
    		statusMessage = e.getLocalizedMessage();
    		return false;
    	} catch (ClassNotFoundException e) {
    		//Postgresql JDBC driver missing!!
    		throw new RuntimeException(e);
    	}
    	return true;
    }
    
    private void closeConnection() {
    	if (cur != null && !cur.isClosed()) {
    		cur.close();
    		cur = null;
    	}
    	connection.closeStatementQuery();
    	connection.closePreparedStatement();
    	connection.closeConnection();
    }
    
    private int getSuspendID(){
    	int result = 0; 
    	String query = "select nextval('membership_suspend_id_seq');";
    	if (!openConnection()) {
    		return -1; //connection failed;
    	}
    	
    	cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, 
    			ContentDescriptor.MembershipSuspend.Cols.MID+" = 0", null, null);
    	int free_count = cur.getCount(); // = cur.count() where mid = 0;;
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
	    		
	    		values.put(ContentDescriptor.MembershipSuspend.Cols.SID, rs.getString("nextval"));
	    		if (need_count > 0) { //we've got pending suspends that need an id before upload.
	    			cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null,
	    	    			ContentDescriptor.MembershipSuspend.Cols.SID+" < 0", null, null);
	    			cur.moveToFirst();
	    			int memberid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.MID));
	    			cur.close();
	    			
	    			contentResolver.update(ContentDescriptor.MembershipSuspend.CONTENT_URI, values,
	    					ContentDescriptor.MembershipSuspend.Cols.MID+" = ?", new String[] {String.valueOf(memberid)});
	    			
	    			//should probably update the pending uploads table too?
	    			cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, new String[] 
	    					{ContentDescriptor.MembershipSuspend.Cols._ID}, ContentDescriptor.MembershipSuspend.Cols.MID+" = ?",
	    					new String[] {String.valueOf(memberid)}, null);
	    			
	    			if (cur.moveToFirst()) {
	    				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols._ID));
	    				
	    				values = new ContentValues();
	    				values.put(ContentDescriptor.PendingUploads.Cols.TABLEID,
	    						ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey());
	    				values.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);
	    				
	    				contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
	    			}
	    			cur.close();
	    			
	    			need_count -=1;
	    		} else {
	    			contentResolver.insert(ContentDescriptor.MembershipSuspend.CONTENT_URI, values);
	    		}
	    	} catch (SQLException e) {
	    		statusMessage = e.getLocalizedMessage();
	    		e.printStackTrace();
	    		return -2;
	    	}
	    	result +=1;
	    	connection.closeStatementQuery();
    	}
    	
    	closeConnection();
    	return result;
    }
    
    private int uploadSuspends(){
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
    			//an error occured, the cursor was empty?
    			statusMessage = "an Error Occured: MembershipSuspend could not find row";
    			//rather than returning we should delete the issue, and then continue.
    			//TODO:
    			return -2;
    		}
    		String mid, msid, sid, reason, start, duration, freeze;
  
    		mid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.MID));
    		msid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.MSID));
    		sid = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.SID));
    		reason = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.REASON));
    		start = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.STARTDATE));
    		duration = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.LENGTH));
    		freeze = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.FREEZE));
    		
    		try {
    			connection.uploadSuspend(sid, mid, msid, start, duration, reason, freeze);
    		} catch (SQLException e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    			return -3;
    		}
    		//remove it from the pendingUploads table.
    		contentResolver.delete(ContentDescriptor.PendingUploads.CONTENT_URI, 
    				ContentDescriptor.PendingUploads.Cols.TABLEID+" = ? AND "
    				+ContentDescriptor.PendingUploads.Cols.ROWID+" = ?", 
    				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MembershipSuspend.getKey()),
    				String.valueOf(rows.get(i))});
    	}
    	
    	closeConnection();
    	return result;
    }
}
