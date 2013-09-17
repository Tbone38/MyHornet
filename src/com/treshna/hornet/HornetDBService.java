package com.treshna.hornet;

import java.sql.ResultSet;
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

import com.treshna.hornet.ContentDescriptor.Membership;
import com.treshna.hornet.ContentDescriptor.Resource;

import android.annotation.SuppressLint;
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
	    * TODO: move the connection.open & connection.close to into the switch cases. 
	    */
	   
	   if (thread == null) {
		   thread = new NetworkThread();
	   }
	   thread.addNetwork(currentCall, resourceid, this);
	   if (!thread.isAlive() && thread.getState() == Thread.State.NEW) {
		   System.out.print("\n\n**STARTING THREAD**\n\n");
		   thread.start();
	   } else if (thread.getState() == Thread.State.TERMINATED) {
		   System.out.print("\n\n**RESTARTING THREAD**\n\n");
		   thread = new NetworkThread();
		   thread.addNetwork(currentCall, resourceid, this);
		   thread.start();
	   } else {
		   System.out.print("\n\nTHREAD STATE:"+thread.getState());
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
     * this function is currently broken.
     */
    public void startNetworking(int currentcall, String theResource){
    	switch (currentCall){
 	   case (Services.Statics.LASTVISITORS): { //this should be run frequently
 		  /* new Thread(new Runnable() { //threading because Android blocks networking on main thread.
 			   public void run() {*/
		   	//Never show progress bar for last-visitors.
		   	thread.is_networking = true;
		  
		   	boolean result = getLastVisitors();
			if (result == true) { //If database query was successful, then look for images; else show toast.
				visitorImages();
			}
			uploadBookings();
			uploadMember();
			getMemberID();
			getBookingID();
			Services.setPreference(ctx, "lastsync", String.valueOf(new Date().getTime()));
			Services.showToast(getApplicationContext(), statusMessage, handler);
			/*Broadcast an intent to let the app know that the sync has finished
			 * communicating with the server/updating the cache.
			 * App can now refresh the list. */
			
			thread.is_networking = false;
			Intent bcIntent = new Intent();
			bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
			sendBroadcast(bcIntent);
			System.out.println("*Sending Intent, Stoping Service*");
 			 //  }}).start();
 		   	break;
 	   } 
 	   case (Services.Statics.UPLOAD):{
 		  statusMessage = "";
 		   /*new Thread(new Runnable() { //threading because Android blocks networking on main thread.
 		   		public void run() {*/
			thread.is_networking = true;
			
			int count = 0;
			uploadBookings();
			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, null, null, null);
			cur.moveToFirst();
			int i = 0;
			while (i < cur.getCount()){
				cur.moveToPosition(i);
				boolean result = uploadImage(cur.getInt(0), cur.getInt(1), cur.getString(2), cur.getString(3), cur.getInt(4));
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
 		   //call bookings functions.
 		   statusMessage = null;
 		  /* new Thread(new Runnable() {
 			   public void run() {*/
 		  thread.is_networking = true;
 		 
		   Services.showProgress(Services.getContext(), "Syncing Bookings From Server", handler, currentCall);
		   //upload bookings, then update bookings, then get bookings.
		   int uploadcount = uploadBookings();
		   updateBookings(); //this should be in last visitors as well
		   int result = getBookings();
		   bookingImages();
		   
			thread.is_networking = false;
		  
		   if (result >= 0) statusMessage = "Retrieved "+result+" Bookings\n Uploaded "+uploadcount+" Bookings";
		  // bookingImages();
		   Services.stopProgress(handler, currentCall);
		   Services.showToast(getApplicationContext(), statusMessage, handler);
		  
		   Intent bcIntent = new Intent();
		   
		   if (result >= 0) {
			   bcIntent.putExtra(Services.Statics.IS_SUCCESSFUL, true);
		   }else {
			   bcIntent.putExtra(Services.Statics.IS_SUCCESSFUL, false);
		   }
		   bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
		  
		   sendBroadcast(bcIntent);
		   System.out.println("*Sending Intent, Stoping Service*");
 			  // }}).start();
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
 				   int rcount = 0;
 				   int btcount = 0;
 				   int rscount = 0;
 				   int mcount = 0;
 				   int days = 0;
 				   int midcount = 0;
 				   int mscount = 0;
 				   rcount = getResource();
 				   
 				   days = getOpenHours();
 				   mscount = getMembership();
 				   midcount = getMemberID();
 				   if (midcount != 0) statusMessage = midcount+" Sign-up's available";
 				   if (statusMessage != null && statusMessage.length() >3 ) {
 					   Services.showToast(getApplicationContext(), statusMessage, handler);
 				   }
 				   //statusMessage = null;
 				   rscount = getResultStatus();
 				   getBookingType();
 				   mcount = getMember();
 				   memberImages();
 				   
 				   getBookingID();
 				  
 				  thread.is_networking = false;
 				   Services.stopProgress(handler, currentCall);
 				   if (statusMessage != null) {
 					   Services.showToast(getApplicationContext(), statusMessage, handler);
 				   }
 				   statusMessage = "Recieved "+mcount+" Members, "+mscount+" memberships, and "+rcount+" Resources";
 				   Services.showToast(getApplicationContext(), statusMessage, handler);
 				   System.out.print("\n\nrcount:"+rcount+"  btcount:"+btcount+" rscount:"+rscount+"  mcount:"+mcount+" days:"+days);
 				   
 				   Services.showToast(getApplicationContext(),"Download Finished GymMaster Mobile will now restart",handler);
 				   
 				  Intent bcIntent = new Intent();
 				  bcIntent.putExtra(Services.Statics.IS_RESTART, true);
 				  bcIntent.setAction("com.treshna.hornet.serviceBroadcast");
 				  sendBroadcast(bcIntent);
 				  System.out.println("*Sending Intent, Stoping Service*");
 			  // }}).start();
 		   break;
 	   }
 	   /* The below service is not networking, but updates the local-database based on
 	    * user selections, It's been placed in this service for ease of use.
 	    * It's threaded so it doesn't block the UI.
 	    */
 	   case (Services.Statics.RESOURCESELECTED):{
 		  thread.is_networking = true;
 		 Services.showProgress(Services.getContext(), "Setting up resource", handler, currentCall);
 		   	   resourceid = theResource;
 		   	/*new Thread(new Runnable() {
 				   public void run() {*/ 
 					   //rebuild times, then update the reference in date.
			   setTime(); 
			   setDate();
			   updateOpenHours();
			   Services.stopProgress(handler, currentCall);
			  thread.is_networking = false;
 				   //}}).start();
 				   break;
 	   }
 	   }
    }
    
    public static Handler getHandler(){
    	return handler;
    }
    
    @SuppressLint("SimpleDateFormat")
	public boolean getLastVisitors(){
    	
    	long this_sync = new Date().getTime();
    		try {
    			connection.openConnection();
    		} catch (Exception e) {
    			statusMessage = e.getLocalizedMessage();
    			return false;
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
					id = cur.getInt(0);
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
    	catch (Exception e){
    		if (e.getCause() != null) {
	    		Throwable cause = e.getCause();
	    		if (cause.getMessage().compareToIgnoreCase("host=-1, port=-1") == 0) {
	    			statusMessage = "Error Occured: Server not set. Please visit the Application Settings before attempting to connect";
	    			connection.closeStatementQuery();
	    			//moved connection.close switch-case;
	    			cur.close();
	    			return false;
	    		} else {
	    			statusMessage = "Exception Occured: "+cause.getMessage()+";";
	    			e.printStackTrace();
	    			//statusMessage = "Error Occured: Server not set. Please visit the Application Settings before attempting to connect";
	    			connection.closeStatementQuery();
	    			//moved connection.close switch-case;
	    			//cur.close();
	    			return false;
	    		}
    		} else {
	    		e.printStackTrace();	
	    	}
    	}
        Services.setPreference(ctx, "lastsync", String.valueOf(this_sync));//String.valueOf(System.currentTimeMillis())
    	//moved connection.close switch-case;   	
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
    	boolean oldQuery = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("image", false);
    	
    	imageWhereQuery = " AND memberid IN (";
    	imageWhereQuery += (cursor.getCount() == 0)? "-1 " : " ";
    	while (cursor.moveToNext()){
    		imageWhereQuery = imageWhereQuery + " "+ cursor.getString(index) + ",";
    	}
    	imageWhereQuery = imageWhereQuery.substring(0, imageWhereQuery.length()-1);
    	imageWhereQuery +=");";
		cursor.close();
		System.out.println("\nQuerying server for image");
		System.out.print("\n\n"+imageWhereQuery);
		try {
			connection.openConnection();
		} catch (Exception e) {
			statusMessage = e.getLocalizedMessage();
			return;
		}
			/*
			 * Hard-coded file size is a pain in the ass, look into fixing this.
			 * If the filesize isn't exact, the query will fail (doesn't like having empty data in 
			 * the middle of the query).
			 */
		FileHandler fileHandler = new FileHandler(this);
		ResultSet rs = null;
		if (oldQuery != true){ 
        	int fileSize = 187; //ImageQuery.sql = 149, multi = 187
        	// file size, gets passed into byte, this number need only be an int though.
        	String query = fileHandler.readFile(fileSize, "multiImageQuery.sql");
        	query = query + imageWhereQuery;
        
        	try {
        		rs = connection.startStatementQuery(query);
        	} catch (Exception e) {
        		statusMessage = e.getLocalizedMessage();
        		e.printStackTrace();
        		return;
        	}
		} else if (oldQuery == true) { //the table doesn't have description or is_profile
			int fileSize = 162; 
        	// file size, gets passed into byte, this number need only be an int though.
        	
        	String query = fileHandler.readFile(fileSize, "noDescImageQuery.sql");
        	query = query + imageWhereQuery;
        	
        	try {
        		rs = connection.startStatementQuery(query);
        	} catch (Exception e) {
        		statusMessage = e.getLocalizedMessage();
        		e.printStackTrace();
        		return;
        	}
		}
        	ContentValues val = new ContentValues();
        	byte[] is;
        	int count = 0;
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
        	while (rs.next()) {
        		cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.MID
                		+" = "+rs.getString(2), null, null);
            	cur.moveToFirst();
            	
            	//do some date checking. rather than rewriting images every time.
            	List<String> dates = new ArrayList<String>();
            	Date cacheDate = null;
        		while(!cur.isAfterLast()){
        			if ( cur.getCount() > 0 && cur.isNull(2) == false) {
        				String cDate = Services.dateFormat(cur.getString(2), "dd MMM yy hh:mm:ss aa", "yyyy-MM-dd");
            			dates.add(cDate);
            		}
        			cur.moveToNext();
            	}
            	boolean imgExists = false;
            	Date sDate = null;
            	sDate = dateFormat.parse(rs.getString(3));
            	for (String date : dates){
            		cacheDate = dateFormat.parse(date);
            		if (cacheDate.compareTo(sDate) == 0) imgExists = true;
            	}
            	//if the image isn't found: add it!
            	if (imgExists == false){
            		count +=1;
	            	int imgCount = 0;
	            	imgCount = cur.getCount();
	            	if (oldQuery != true) {
		            	if (rs.getBoolean(5) == true) { //isProfile
		            		imgCount = 0;
		            		if (cur.getCount() > 0) {
		            			val = new ContentValues();
		            			val.put(ContentDescriptor.Image.Cols.ID, cur.getCount());
		            			contentResolver.update(ContentDescriptor.Image.CONTENT_URI, val, ContentDescriptor.Image.Cols.ID +" = "+imgCount
		            					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+rs.getString(2), null);
		            			fileHandler.renameFile("0_"+rs.getString(2), cur.getCount()+"_"+rs.getString(2));
		            		}
		            	}
	            	}
	            	else {
	            		imgCount = 0;
	            		if (cur.getCount() > 0) {
	            			val = new ContentValues();
	            			val.put(ContentDescriptor.Image.Cols.ID, cur.getCount());
	            			contentResolver.update(ContentDescriptor.Image.CONTENT_URI, val, ContentDescriptor.Image.Cols.ID +" = "+imgCount
	            					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+rs.getString(2), null);
	            			fileHandler.renameFile("0_"+rs.getString(2), cur.getCount()+"_"+rs.getString(2));
	            		}
	            	}
	            	//cur.close();     		
		            	//Add some null handling as well.
	            	is = rs.getBytes(1);
	        		fileHandler.writeFile(is, imgCount+"_"+rs.getString(2));
	        		is = null;
	        		val.put(ContentDescriptor.Image.Cols.ID, imgCount);
	        		val.put(ContentDescriptor.Image.Cols.MID, rs.getString(2));
	        		String ssDate = Services.dateFormat(rs.getString(3), "yyyy-MM-dd", "dd MMM yy hh:mm:ss aa");
	        		val.put(ContentDescriptor.Image.Cols.DATE, ssDate);
	        		String description = null;
	        		if (oldQuery != true) {
	        			description = rs.getString(4);
	        		}
	        		if (description == null || description.length() < 2 || description.compareTo(" ") == 0) {
	        			description = "no description";
	        		}
	        		val.put(ContentDescriptor.Image.Cols.DESCRIPTION, description);
	        		int isProfile = 0;
	        		if (oldQuery != true){
	        			isProfile = Services.booltoInt(rs.getBoolean(5));
	        		} else {
	        			isProfile = Services.booltoInt(rs.getBoolean(1));
	        		}
	        		val.put(ContentDescriptor.Image.Cols.IS_PROFILE, isProfile);
	        		contentResolver.insert(ContentDescriptor.Image.CONTENT_URI, val);
            	}
            	cur.close();
            }
        	rs.close();
        	statusMessage = statusMessage +"\n"+count+" new Images were Recieved";
        	connection.closeStatementQuery();
        	
		} catch (Exception e) {
			e.printStackTrace();
			statusMessage = statusMessage+"\nException with Images: "+e.getMessage();			
		} finally {
			connection.closeStatementQuery();
			cur.close();
		}
		connection.closeConnection();
	}
    
    @SuppressLint("SimpleDateFormat")
	public boolean uploadImage(int id, int mid, String cDate, String description, int isProfile) {
    	int idExists = 0;
    	List<String> dateList = new ArrayList<String>();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	try {
    		connection.openConnection();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		return false;
    	}
    		ResultSet rs  = null;
    	try {	
    		rs = connection.imageCount(mid);
    	}catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    		return false;
    	}
    	try {
    		while (rs.next()){
    			//from imageCount(), getString(1)  = lastUpdated, 2 = created
    			idExists = rs.getRow();
    			dateList.add(rs.getString(2)); 
    			/*System.out.println(idExists);
    			serverImageDate = dateFormat.parse(rs.getString(1));
    			System.out.println(serverImageDate);*/
    		}
    		rs.close();
    		connection.closePreparedStatement();	
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	cDate = Services.dateFormat(cDate, "dd MMM yy hh:mm:ss aa", "yyyy-MM-dd HH:mm:ss");
    	Date cacheImageDate = null;
    	try {
    		cacheImageDate = dateFormat.parse(cDate);
    	} catch (ParseException e) {
    		//no date/image not in SQLite
    		Log.e("HORNET", "No Date for image found in SQLite");
    		connection.closePreparedStatement();
    		//moved connection.close switch-case;
    		return false;
    	}

    	FileHandler fileHandler = new FileHandler(getApplicationContext());
    	int imageSize = 20000;
    	byte[] image = fileHandler.readImage(imageSize, Integer.toString(id)+"_"+Integer.toString(mid));
    	int updateCount = 0;
    	
    	if (idExists >= 1) {
    	/* Handle Multiple images.
    	 * do date comparison for each image found, if not found do insert.
    	 * cannot do image updates.
    	 */	Date sDate = null;
    	 	boolean doesExist = false;
    		for (String date : dateList) {
    			try { sDate = dateFormat.parse(date);  }
    			catch (ParseException e){/* non-proper date format given*/ }
    			//System.out.print("\n * Cache: "+cacheImageDate);
    			//System.out.print("\n * Server: "+sDate);
				if ((cacheImageDate.compareTo(sDate)) == 0) {
					//image with exact same date already exists.
					System.out.println("**SKIPPING IMAGE** \n Server Copy is same as local");
					doesExist = true;
				}
    		}
    		if (doesExist == false) {
    			try {
    				updateCount = connection.insertImage(image, mid, cacheImageDate, description, Services.isProfile(isProfile));
    				System.out.print("\n\n**Insert Result: "+updateCount);
    			} catch (Exception e) {
    				statusMessage = e.getLocalizedMessage();
    				e.printStackTrace();
    			}
    		}
    	}else if(idExists == 0){
    		try {
    			updateCount = connection.insertImage(image, mid, cacheImageDate, description, Services.isProfile(isProfile));
    			System.out.println(updateCount);
    		} catch (Exception e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    		}
    	}
    	connection.closePreparedStatement();
    	connection.closeConnection();
	    return (updateCount != 0);	
    }
    
    private int uploadMember(){
    	//if isUsed = 1, upload member to database, else skip.
    	String email, medical, suburb, hphone, cphone, gender, dob;
    	int result = 0;
    	
    	cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 1", null, null);
    	if (cur.getColumnCount() <= 0) {
    		cur.close();
    		System.out.print("\n\n NO pending members");
    		return 0;
    	}
    	cur.moveToFirst();
    	try {
    		connection.openConnection();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		return 0;
    	}
    	try {
	    	while (cur.getPosition() < cur.getCount()) {
	    		/*****************/ //null handling
	        	if (cur.isNull(10)) email = "";
	        	else email = cur.getString(10);
	        	if (cur.isNull(5)) medical = "";
	        	else medical = cur.getString(5);
	        	if (cur.isNull(7)) suburb = "";
	        	else suburb = cur.getString(7);
	        	if (cur.isNull(11)) hphone = "";
	        	else hphone = cur.getString(11);
	        	if (cur.isNull(12)) cphone = "";
	        	else cphone = cur.getString(12);
	        	
	        	gender = cur.getString(4);
	        	if (gender.compareTo(getString(R.string.radioMale))== 0) gender = "M";
	        	else gender = "F";
	        	dob = cur.getString(3);
	        	dob = Services.dateFormat(dob, "dd/MM/yyyy", "yyyy-MM-dd");
	        	/****************/
	    		if (cur.getString(13).compareTo(getString(R.string.radioMember)) == 0){
	    			result += connection.addMember(cur.getInt(14), cur.getString(2), cur.getString(1), gender, email, dob, cur.getString(6), suburb, cur.getString(8), cur.getString(9), hphone, cphone, medical);
	    			String[] memberid = {cur.getString(14)};
	    			connection.closePreparedStatement();
	        		contentResolver.delete(ContentDescriptor.Pending.CONTENT_URI, ContentDescriptor.Pending.Cols.MID+" = ?", memberid);
	        		//after successful upload, remove from local database.
	    		} else {
	    			result += connection.addProspect(cur.getString(2), cur.getString(1), gender, email, dob, cur.getString(6), suburb, cur.getString(8), cur.getString(9), hphone, cphone, medical);
	    			String[] memberid = {cur.getString(0)};
	    			connection.closePreparedStatement();
	        		contentResolver.delete(ContentDescriptor.Pending.CONTENT_URI, ContentDescriptor.Pending.Cols.ID+" = ?", memberid);
	    		}
	    		
	    		cur.moveToNext();
	    	}
    	} catch (Exception e){
	    	statusMessage = e.getLocalizedMessage();
	    	e.printStackTrace();
	    }
    	cur.close();
    	connection.closeConnection();
    	return result;
    }
    /* Retrieves and stores free memberID's from the database,
     * the memberID's are assigned to members upon signup through the app.
     */
    private int getMemberID() {
    	//when pending.rowCount < 10, get memberID until rowCount = 200
    	//always do upload first.
    	try {
			connection.openConnection();
		} catch (Exception e) {
			// Connection failed to open
			statusMessage = e.getLocalizedMessage();
			return 0;
		}
    	cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 0", null, null);
    	int count = cur.getCount();
    	cur.close();
    	if (count>= 3) {
    		// have 3 ID's already, don't bother getting more.
    		return 0;
    	}
    	for (int l=(5-count); l>=0;l -=1){ //Dru suggested 200?
    		count = 0;
    		ResultSet rs = null;
    		try {
    			rs = connection.startStatementQuery("select nextval('member_id_seq');");
    			rs.next();
    		
	    		ContentValues val = new ContentValues();
	    		System.out.print(("\n\nMID: "+rs.getString(1)));
	    		val.put(ContentDescriptor.Pending.Cols.MID, rs.getString(1));
	    		
	    		cur = contentResolver.query(ContentDescriptor.Pending.CONTENT_URI, null, ContentDescriptor.Pending.Cols.ISUSED+" = 2", null, null);
	    		System.out.print("\n\nPending Uploads (without ID):"+cur.getCount());
	    		if (cur.getCount() != 0) {
	    			cur.moveToFirst();
	    			System.out.print("\n\nUpdating Record where id ="+cur.getInt(0));
	    			System.out.print("\n\nWith "+rs.getString(1));
	    			int id = cur.getInt(0);
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
    		} catch (Exception e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    			return 0;
    		}
    		
    	}
    	connection.closeConnection();
    	return count;
    }
    
    private int getBookingID(){
    	int result = 0;
    	
    	try {
			connection.openConnection();
		} catch (Exception e) {
			// Connection failed to open
			statusMessage = e.getLocalizedMessage();
			return 0;
		}
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" = 0", null, null);
    	int count = cur.getCount();
    	cur.close();
    	if (count>= 16) {
    		// have 16 ID's already, don't bother getting more.
    		return 0;
    	}
    	for (int l=(20-count); l>=0;l -=1){ //Dru suggested 200?
    		ResultSet rs = null;
    		try {
	    		rs = connection.startStatementQuery("select nextval('booking_id_seq');");
				rs.next();
				
				ContentValues val = new ContentValues();
	    		System.out.print(("\n\nBID: "+rs.getString(1)));
	    		val.put(ContentDescriptor.Booking.Cols.BID, rs.getString(1));
	    		val.put(ContentDescriptor.Booking.Cols.LASTUPDATED, 0);
	    		contentResolver.insert(ContentDescriptor.Booking.CONTENT_URI, val);
				result +=1;
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		try {
    			connection.closeStatementQuery();
    			rs.close();
    		}catch (Exception e) {
    			//
    		}
    	}
    	connection.closeConnection();
    	return result;
    }
    
    private int getResource(){
    	ResultSet rs = null;
    	int result = 0;
    	try {
			connection.openConnection();
    		rs = connection.getResource();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		return 0;
    	}
    	try {
    		while (rs.next()) {
    			cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID +" = "+rs.getString(1),
	    				null, null);
	    		if (cur.getCount() == 0) {
	    			ContentValues val = new ContentValues();
		    		val.put(ContentDescriptor.Resource.Cols.ID, rs.getString(1));
		    		val.put(ContentDescriptor.Resource.Cols.NAME, rs.getString(2));
		    		val.put(ContentDescriptor.Resource.Cols.CID, rs.getString(3));
		    		val.put(ContentDescriptor.Resource.Cols.RTNAME, rs.getString(4));
		    		val.put(ContentDescriptor.Resource.Cols.PERIOD, rs.getString(5));
		    		
		    		contentResolver.insert(ContentDescriptor.Resource.CONTENT_URI, val);
		    		result +=1;
	    		}
	    		cur.close();
    		}
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    	}
    	connection.closePreparedStatement();
    	connection.closeConnection();
    	
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
    	int result = 0;
    	ArrayList<String> bookinglist = new ArrayList<String>();
    	String b_lastsync = Services.getAppSettings(ctx, "b_lastsync");
    	//System.out.print("\n\n**Uploading Bookings with update after:"+b_lastsync);
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" > ? " +
    			"AND "+ContentDescriptor.Booking.Cols.IS_UPLOADED+" = 0",
    			new String[] {b_lastsync}, null);
    	
    	try {
    		connection.openConnection();
    	} catch (Exception e){
    		statusMessage = e.getLocalizedMessage();
    	}
    	while (cur.moveToNext()) {
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
			System.out.print("\n\nOFFSET:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			values.put(ContentDescriptor.Booking.Cols.OFFSET, cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.OFFSET)));
			System.out.print("\n\nBooking Modified:"+cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED)));
			Date lastupdate = new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED)));
			System.out.print("\n\nLast-Update:"+lastupdate.getTime()+"\n");
			
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
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
    	}
    	System.out.print("\n\nUploaded "+result+" Bookings \n\n");
    	cur.close();
    	connection.closeConnection();
    	
    	for (int j=0; j<bookinglist.size(); j+=1){
    		ContentValues values = new ContentValues();
    		values.put(ContentDescriptor.Booking.Cols.IS_UPLOADED, 1);
    		contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, values, ContentDescriptor.Booking.Cols.BID+" = ?",
    				new String[] {bookinglist.get(j)});
    	}
    	
    	return result;
    }
    
    private int updateBookings(){
    	int result = 0;
    	String lastSync = Services.getAppSettings(ctx, "b_lastsync");
    	//System.out.print("\n\nLast Sync Was:"+lastSync);
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" > ? " +
    			"AND "+ContentDescriptor.Booking.Cols.RESULT+" != 0", //don't update empty bookings;
    			new String[] {lastSync}, null);//get the bookings that have changed since last sync.
    	//System.out.print("\n\n Updating "+cur.getCount()+" Bookings \n\n");
    	
    	if (cur.getCount()<= 0) { //no booking found for that
    		cur.close();
    		return 0;
    	}
    	try {
    		connection.openConnection();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	
    	while (cur.moveToNext()) {//TODO:crashing here ?
    		
    		int bookingid, resultstatus, bookingtypeid;
    		long lastupdate, checkin;
    		String notes;
    	
    		bookingid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BID));
    		resultstatus = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.RESULT));
    		bookingtypeid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.BOOKINGTYPE));
    		lastupdate = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.LASTUPDATED));
    		checkin = cur.getLong(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CHECKIN));
    		notes = cur.getString(cur.getColumnIndex(ContentDescriptor.Booking.Cols.NOTES));
    		System.out.print("\n\nLast update for booking:"+bookingid+" was "+lastupdate);
    		try {
    			result += connection.updateBookings(bookingid, resultstatus, notes, lastupdate, bookingtypeid, checkin);
    			connection.closePreparedStatement();
    		} catch (Exception e) {
    			statusMessage = e.getLocalizedMessage();
    			e.printStackTrace();
    		}
    	}
    	System.out.print("\n\nUpdated "+result+" Bookings!\n\n");
    	connection.closeConnection();
    	return result;
    }
    
    private int getBookings(){
    	
    	ResultSet rs = null;
    	int result = 0;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.MONTH, -1);
    	java.sql.Date yesterday = new java.sql.Date(cal.getTime().getTime());
 
    	cal.add(Calendar.MONTH, +2);
    	java.sql.Date tomorrow = new java.sql.Date(cal.getTime().getTime());
    	long this_sync = new Date().getTime();
    	long last_sync = Long.parseLong(Services.getAppSettings(ctx, "b_lastsync"));
    	int lastrid = Integer.decode(Services.getAppSettings(ctx, "last_rid"));
    	
    	int resourceid = Integer.decode(Services.getAppSettings(this, "resourcelist"));
    	if (lastrid != resourceid) {
    		last_sync = 3;
    	}
    	if (resourceid < 0 ) {
    		statusMessage = "please set resource in the application settings";
    		return -1;
    	}
    	try {
    		connection.openConnection();
    		/*System.out.print("\nYesterday:"+yesterday.toLocaleString());
    		System.out.print("\nTomorrow:"+tomorrow.toLocaleString());*/
    		//System.out.print("\nResourceID:"+resourceid);
    		//System.out.print("\n\nLast Sync:"+last_sync);
    		rs = connection.getBookings(yesterday, tomorrow, resourceid, last_sync);
    	} catch (Exception e) {
    		e.printStackTrace();
    		statusMessage = e.getLocalizedMessage();
    		return -1;
    	}
    	
    	try {
    		System.out.print("\n\nCount:"+rs.getFetchSize());
	    	while (rs.next()) {
	    		ContentValues val = new ContentValues();
	    		val.put(ContentDescriptor.Booking.Cols.FNAME, rs.getString("firstname"));
	    		val.put(ContentDescriptor.Booking.Cols.SNAME, rs.getString("surname"));
	    		val.put(ContentDescriptor.Booking.Cols.BOOKING, rs.getString("bookingname"));
	    		
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS", Locale.US);
	    		String uscheckin = rs.getString("checkin"); 
	    		long checkin = 0;
	    		if (uscheckin != null && !uscheckin.isEmpty()) {
	    			try {
	    				checkin = format.parse(uscheckin).getTime();
	    			} catch (Exception e) {
	    				checkin = 0;
	    			}
	    		}
	    		val.put(ContentDescriptor.Booking.Cols.CHECKIN, checkin);
	    		
	    		String date = Services.dateFormat(rs.getString("arrival"), "yyyy-MM-dd", "yyyyMMdd");
	    		int starttime = getTime(rs.getString("startid"), contentResolver );
	    		int endtime = getTime(rs.getString("endid"), contentResolver);
	    		
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
	    		
	    		/** 
	    		 * last-update is equal to the last sync time, as using the database's last-update time can
	    		 * cause issues when the times don't match (e.g. if the last-update time on the database is ahead, it'll break thing).. 
	    		 */
	    		val.put(ContentDescriptor.Booking.Cols.LASTUPDATED, this_sync);
	    		
	    		//get the offset for this booking;
	    		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, ContentDescriptor.Resource.Cols.ID+" = ?",
	    				new String[] {rs.getString("resourceid")}, null);
	    		if (cur.getCount() > 0) {
	    			cur.moveToFirst();
	    			//System.out.print("\n\nOFFSET:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.PERIOD)));
	    			val.put(ContentDescriptor.Booking.Cols.OFFSET, cur.getString(cur.getColumnIndex(ContentDescriptor.Resource.Cols.PERIOD)));
	    			cur.close();
	    		} else {
	    			cur.close();
	    		}
	    		
	    		
	    		cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.BID +" = "+rs.getString("bookingid"),
	    				null, null);
	    		if (cur.getCount() == 0) { //insert
	    			//System.out.print("\n\nINSERTING BOOKING\n\n");
	    			cur.close();
	    			contentResolver.insert(ContentDescriptor.Booking.CONTENT_URI, val);
//	    			timeid+=1;
		    		result +=1;
	    		} else { //update
	    			//System.out.print("\n\nUPDATING BOOKING\n\n");
	    			cur.close();
	    			int status = 0;
	    			status= contentResolver.update(ContentDescriptor.Booking.CONTENT_URI, val, 
	    					ContentDescriptor.Booking.Cols.BID+" = ? ",
	    					new String[] {rs.getString("bookingid")});
	    			if (status == 0) {
	    				//update failed
	    				System.out.print("\n\nUPDATE FAILED\n\n");
	    			}
	    			result +=status;
	    			if (rs.getInt("result") ==  5) { //booking-cancelled, delete it from the bookingtime table.
	    				contentResolver.delete(ContentDescriptor.BookingTime.CONTENT_URI, ContentDescriptor.BookingTime.Cols.BID+" = ?",
	    						new String[] {rs.getString("bookingid")});
	    			}
	    		}
	    		
	    		int timeid = starttime;
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
    	} catch (Exception e) {
    		e.printStackTrace();
    		statusMessage = e.getLocalizedMessage();
    	}
    	connection.closePreparedStatement();
    	connection.closeConnection();
    	System.out.print("\n\nBookingCount:"+result);
    	System.out.print("\n\nBookings sync'd at:"+this_sync+"\n\n");
    	Services.setPreference(ctx, "last_rid", String.valueOf(resourceid));
    	Services.setPreference(ctx, "b_lastsync", String.valueOf(this_sync));//String.valueOf(System.currentTimeMillis())
    	return result;
    }
    
    @SuppressWarnings("unused")
	private int getBookingType(){
    	int result = 0;
    	ResultSet rs = null;
    	result = contentResolver.delete(ContentDescriptor.Bookingtype.CONTENT_URI,null, null);
    	//System.out.print("\n\nDeleted "+result+" from bookingtype");
    	final int CACI = 0; //TODO: fix this
    	try {
    		connection.openConnection();
    		//if caci ? bookingtypeValids
    		// else bookingtype
    		rs = (CACI != 0)?connection.getBookingTypesValid() : connection.getBookingTypes();
    	} catch (Exception e){
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    		return -1;
    	}
    	try {
    		while (rs.next()) {
    			
    				ContentValues values = new ContentValues();
    				values.put(ContentDescriptor.Bookingtype.Cols.BTID, rs.getString(1));
    				values.put(ContentDescriptor.Bookingtype.Cols.NAME, rs.getString(2));
    				values.put(ContentDescriptor.Bookingtype.Cols.PRICE, rs.getString(3));
    				if (CACI != 0){ //only applies to clinicMaster ?
	    				values.put(ContentDescriptor.Bookingtype.Cols.VALIDFROM, rs.getString(4));
	    				values.put(ContentDescriptor.Bookingtype.Cols.VALIDTO, rs.getString(5));
	    				values.put(ContentDescriptor.Bookingtype.Cols.EXTERNAL, rs.getString(6));
    				} else {
    					values.put(ContentDescriptor.Bookingtype.Cols.EXTERNAL, rs.getString(4));
    				}
	    			
    				contentResolver.insert(ContentDescriptor.Bookingtype.CONTENT_URI, values);
    				result +=1;
    		}
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	//System.out.print("\n\nGot"+result+" bookingtypes from server");
    	connection.closePreparedStatement();
    	connection.closeConnection();
    	
    	return result;
    }
    
    public static int getTime(String time, ContentResolver contentResolver){
    	int result = 0;
    	if (cur != null){
    		cur.close();
    	}
    	/*if (contentResolver == null){
    		contentResolver = getContentResolver();
    	}*/
    	cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME
				+" = '"+time+"' ", null, null);
		if (cur.getCount() == 0) {
			//statusMessage = "incorrect Time slot set, please visit application settings";
			return -1;
		}
		cur.moveToFirst();
		/*for (int i=0;i<cur.getCount();i+=1){
			//System.out.print("\n\ni:"+cur.getString(i));
		}*/
		result = cur.getInt(cur.getColumnIndex(ContentDescriptor.Time.Cols.ID));
		cur.close();
		return result;
		//return cur.getInt(0);
    }
    
    private int getResultStatus(){
    	int result = 0;
    	ResultSet rs = null;
    	try {
    		connection.openConnection();
    		rs = connection.getResultStatus();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    		return -1;
    	}
    	contentResolver.delete(ContentDescriptor.ResultStatus.CONTENT_URI, null, null);
    	try {
	    	while (rs.next()) {
	    		ContentValues values = new ContentValues();
	    		values.put(ContentDescriptor.ResultStatus.Cols.ID, rs.getString(1));
	    		values.put(ContentDescriptor.ResultStatus.Cols.NAME, rs.getString(2));
	    		values.put(ContentDescriptor.ResultStatus.Cols.COLOUR, rs.getString(3));
	    		
	    		contentResolver.insert(ContentDescriptor.ResultStatus.CONTENT_URI, values);
	    		//System.out.print("\n\n"+rs.getString(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
	    		result +=1;
	    	}
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	connection.closePreparedStatement();
    	connection.closeConnection();
    	return result;
    }
    
    private void setDate(){
    	contentResolver.delete(ContentDescriptor.Date.CONTENT_URI, null, null);
    	System.out.print("\n\nSetting Date!!\n\n");
    	Calendar current = Calendar.getInstance();
    	Calendar maximum = Calendar.getInstance();
    	maximum.set(Calendar.MONTH, (current.get(Calendar.MONTH)+1));
    	current.set(Calendar.MONTH, (current.get(Calendar.MONTH)-1));
		while (current.get(Calendar.DAY_OF_YEAR) <= maximum.get(Calendar.DAY_OF_YEAR)) { //TODO: how many dates to store?
			Date date = current.getTime();
			int currentDay = current.get(Calendar.DAY_OF_WEEK);
			ContentValues values = new ContentValues();
			values.put(ContentDescriptor.Date.Cols.DATE, Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
			values.put(ContentDescriptor.Date.Cols.DAYOFWEEK, currentDay);
			contentResolver.insert(ContentDescriptor.Date.CONTENT_URI, values);
			//System.out.print("\n\nDate:"+Services.dateFormat(date.toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyyMMdd"));
			//System.out.print("\n\ndayofweek:"+currentDay);
			current.add(Calendar.DATE, 1);
		}
		System.out.print("\n\nFinished Setting Date!!\n\n");
    }
    
    private void setTime(){
    	System.out.print("\n\nSetting Time!!\n\n");
		contentResolver.delete(ContentDescriptor.Time.CONTENT_URI, null, null); 
		
		{
			System.out.print("\n\nresource:"+resourceid);
			if (resourceid == null) {
				resourceid = Services.getAppSettings(this, "resourcelist");
			}
			
			cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, new String[] {Resource.Cols.PERIOD}, Resource.Cols.ID+" = ?", 
					new String[] {resourceid}, null);
			
			if (cur.getCount() <= 0) {
				cur.close();
			} else {
				cur.moveToFirst();
				System.out.print("\n\nPeriod:"+cur.getString(0));
				String intv = cur.getString(0).replaceAll(":", "");
				int intvl = (Integer.parseInt(intv)/100);
				Services.setPreference(ctx, "timeslot", String.valueOf(intvl));
				cur.close();
			}
		}
		
		int interval = Integer.decode(Services.getAppSettings(this, "timeslot"));
		System.out.print("\n\nInterval:"+interval);
		if (interval != 15 && interval != 30 && interval != 60) interval = 15; //default every 15 minutes.
		Calendar day = Calendar.getInstance();
		day.add(Calendar.DATE, -1);
		
		Calendar upperlimit = Calendar.getInstance();
		upperlimit.set(Calendar.HOUR_OF_DAY, 23);
		upperlimit.set(Calendar.MINUTE, 59);
		upperlimit.set(Calendar.SECOND, 1);
		
		//System.out.print("\n\nLowerLimit:"+llimit);
		Calendar lowerlimit = Calendar.getInstance();
		lowerlimit.set(Calendar.HOUR_OF_DAY, 0); 
		lowerlimit.set(Calendar.MINUTE, 0);
		lowerlimit.set(Calendar.SECOND, 0);
		
			while (lowerlimit.getTime().before(upperlimit.getTime())) {
				ContentValues values = new ContentValues();
				String time = lowerlimit.getTime().toString();
				time = Services.dateFormat(time, "EEE MMM dd HH:mm:ss", "HH:mm:ss");
				//System.out.print("\n\nAfter Time:"+time);
				values.put(ContentDescriptor.Time.Cols.TIME, time);
				contentResolver.insert(ContentDescriptor.Time.CONTENT_URI, values);
				lowerlimit.add(Calendar.MINUTE, interval);
				//System.out.print("\n\nID:"+id+"  TIME:"+time+" DATE:"+date);
			}
    }
    
    private int getMember(){
    	System.out.print("\n\nGetting MemberID's");
    	int result = 0;
    	ResultSet rs = null;
    	contentResolver.delete(ContentDescriptor.Member.CONTENT_URI, null, null);
    	try {
    		connection.openConnection();
    		rs = connection.getMembers();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    		return -1;
    	}
    	try {
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
    	} catch (Exception e){
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	connection.closePreparedStatement();
    	connection.closeConnection();
    	
    	return result;
    }
    
    private int getMembership() {
    	int result = 0;
    	ResultSet rs = null;
    	contentResolver.delete(ContentDescriptor.Membership.CONTENT_URI, null, null);
    	try {
    		connection.openConnection();
    		rs = connection.getMembership();
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	try {
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
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	connection.closeConnection();
    	return result;
    }
    
    private void bookingImages(){
    	String b_lastsync = Services.getAppSettings(ctx, "b_lastsync");
    	cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI, null, ContentDescriptor.Booking.Cols.LASTUPDATED+" > ?", 
    			new String[] {b_lastsync}, null);
    	queryServerForImage(cur, 12);
    	cur.close();
    }
    
    private int swipe(){
    	int result = 0;    	
    	cur = contentResolver.query(ContentDescriptor.Swipe.CONTENT_URI, null, null, null, null);
    	System.out.print("\n\n## of Swipes:"+cur.getCount());
	
    	while (cur.moveToNext()) {
    		String id = cur.getString(0);
    		System.out.print("\n\nid:"+id);
    		int door = cur.getInt(1);
    		System.out.print("\n\ndoor:"+door);
    		//String datetime = cur.getString(2);
    		try {
    			connection.openConnection();
	    		ResultSet rs = connection.tagInsert(door, id);
	    		rs.close();
	    		connection.closePreparedStatement();
	    		
	    		rs = connection.getTagUpdate(door);
	    		String tempmess = null;
	    		while (rs.next()){
	    			tempmess = rs.getString("message")+" "+rs.getString("message2");
	    			statusMessage = tempmess;
	    			System.out.println(tempmess);
	    			break;
	    		}	
	    		rs.close();
	    		connection.closePreparedStatement();
		    	
	    	} catch (Exception e) {
	    		statusMessage = e.getLocalizedMessage();
	    		e.printStackTrace();
	    	}
    		connection.closeConnection();
    	}
    	cur.close();
    	contentResolver.delete(ContentDescriptor.Swipe.CONTENT_URI, null, null);
		
    	return result;
    }
    
    private int getOpenHours(){
    	int result = 0;
    	contentResolver.delete(ContentDescriptor.OpenTime.CONTENT_URI, null, null);
    	ResultSet rs = null;
    	try {
    		connection.openConnection();
    		rs = connection.getOpenHours();
    	} catch (Exception e){
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    		return -1;
    	}
    	try {
    		while (rs.next()) {
    			ContentValues values = new ContentValues();
    			values.put(ContentDescriptor.OpenTime.Cols.DAYOFWEEK, (rs.getInt("dayofweek")+1));
    			values.put(ContentDescriptor.OpenTime.Cols.OPENTIME, rs.getString("opentime"));
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSETIME, rs.getString("closetime"));
    			values.put(ContentDescriptor.OpenTime.Cols.NAME, rs.getString("name"));
    			
    			contentResolver.insert(ContentDescriptor.OpenTime.CONTENT_URI, values);
    			result +=1;
    		}
    	} catch (Exception e) {
    		statusMessage = e.getLocalizedMessage();
    		e.printStackTrace();
    	}
    	connection.closeConnection();
    	return result;
    }
    
    private int updateOpenHours(){
    	int result = 0;
    	System.out.print("\n\nUpdating Open Hours\n");
    	cur = contentResolver.query(ContentDescriptor.OpenTime.CONTENT_URI, null, null, null, null);
    	ArrayList<String[]> idList = new ArrayList<String[]>();
    	while (cur.moveToNext()) {
    		/*for (int l=0; l<cur.getColumnCount(); l+=1){
    			System.out.print("\n\nColumn:"+cur.getColumnName(l)+" Value:"+cur.getString(l));
    		}*/
    		String[] day = new String[3];
    		day[0] = cur.getString(0);
    		day[1] = (cur.isNull(2))? "-1" : cur.getString(2); // ?
    		day[2] = (cur.isNull(4))? "-1" : cur.getString(4);
    		idList.add(day);
    	}
    	
    	for (int i=0; i<idList.size(); i +=1) {
    		ContentValues values = new ContentValues();

    		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
			new String[] {idList.get(i)[1]}, null);
    		if (idList.get(i)[1].compareTo("-1") == 0) {
    			//no starttime/endtime set for this day, what should I do?
    			System.out.print("\n\nNO STARTTIME SET***\n\n");
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
        		values.put(ContentDescriptor.OpenTime.Cols.OPENID, cur.getString(0)); 
        		cur.close();
        		
        		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
    			new String[] {idList.get(i)[2]}, null);
    			//System.out.print("\n\nCloseId:"+cur.getCount());
    			if (cur.getCount() <= 0) {
    				cur.close();
    				cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, null, null, ContentDescriptor.Time.Cols.ID+" DESC");
    			}
    			cur.moveToFirst();
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, (cur.getInt(0)-1));
    			cur.close();
    		}  else {
    			cur.moveToFirst();
        		//System.out.print("\n\nOpenId:"+cur.getString(0));
        		values.put(ContentDescriptor.OpenTime.Cols.OPENID, cur.getString(0)); 
        		cur.close();
        		
        		cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, ContentDescriptor.Time.Cols.TIME+" = ?",
    			new String[] {idList.get(i)[2]}, null);
    			//System.out.print("\n\nCloseId:"+cur.getCount());
    			if (cur.getCount() <= 0) {
    				cur.close();
    				cur = contentResolver.query(ContentDescriptor.Time.CONTENT_URI, null, null, null, ContentDescriptor.Time.Cols.ID+" DESC");
    			}
    			cur.moveToFirst();
    			values.put(ContentDescriptor.OpenTime.Cols.CLOSEID, (cur.getInt(0)-1));
    			cur.close();    			
    		}
    		contentResolver.update(ContentDescriptor.OpenTime.CONTENT_URI, values, ContentDescriptor.OpenTime.Cols._ID+" = ?", 
					new String[] {idList.get(i)[0]});
    	}
    	
    	return result;
    }
    
    /**TODO
    private int getClasses() {
    	int result = 0;
    	
    	return result;
    }*/
}
