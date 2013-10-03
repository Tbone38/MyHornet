package com.treshna.hornet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.treshna.hornet.ContentDescriptor.Booking;
import com.treshna.hornet.ContentDescriptor.BookingTime;
import com.treshna.hornet.ContentDescriptor.Bookingtype;
import com.treshna.hornet.ContentDescriptor.Class;
import com.treshna.hornet.ContentDescriptor.Company;
import com.treshna.hornet.ContentDescriptor.Date;
import com.treshna.hornet.ContentDescriptor.Image;
import com.treshna.hornet.ContentDescriptor.Member;
import com.treshna.hornet.ContentDescriptor.Membership;
import com.treshna.hornet.ContentDescriptor.OpenTime;
import com.treshna.hornet.ContentDescriptor.Pending;
import com.treshna.hornet.ContentDescriptor.PendingUploads;
import com.treshna.hornet.ContentDescriptor.Programme;
import com.treshna.hornet.ContentDescriptor.Resource;
import com.treshna.hornet.ContentDescriptor.ResultStatus;
import com.treshna.hornet.ContentDescriptor.Swipe;
import com.treshna.hornet.ContentDescriptor.TableIndex;
import com.treshna.hornet.ContentDescriptor.Time;
import com.treshna.hornet.ContentDescriptor.Visitor;

public class HornetDatabase extends SQLiteOpenHelper {
	
	 public static final String DATABASE_NAME="hornet.db";
	 private static final int DATABASE_VERSION = 81;
	 
	 public HornetDatabase (Context context) {
		 super(context, DATABASE_NAME, null, DATABASE_VERSION);
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		//TODO add foreign Keys.
		db.execSQL("CREATE TABLE "+Member.NAME+" ("+Member.Cols.MID+" INTEGER PRIMARY KEY, "
				+Member.Cols.COLOUR+" TEXT, "+Member.Cols.HAPPINESS+" TEXT, "
				+Member.Cols.LENGTH+" TEXT, "+Member.Cols.BOOKP+" INTEGER, "
				+Member.Cols.TASKP+" INTEGER, "+Member.Cols.RESULT+" TEXT, "
				+Member.Cols.PHHOME+" TEXT, "+Member.Cols.PHCELL+" TEXT, "
				+Member.Cols.PHWORK+" TEXT, "+Member.Cols.EMAIL+" TEXT, "
				+Member.Cols.NOTES+" TEXT, "+Member.Cols.TASK1+" TEXT, "
				+Member.Cols.TASK2+" TEXT, "+Member.Cols.TASK3+" TEXT, "
				+Member.Cols.BOOK1+" TEXT, "+Member.Cols.BOOK2+" TEXT, "
				+Member.Cols.BOOK3+" TEXT, "+Member.Cols.LASTVISIT+" TEXT, "
				+Member.Cols.STATUS+" INTEGER, "+Member.Cols.FNAME+" TEXT, "
				+Member.Cols.SNAME+" TEXT "
				+");");
		
		db.execSQL("CREATE TABLE "+Visitor.NAME+" ("+Visitor.Cols.ID+" INTEGER PRIMARY KEY, "
				+Visitor.Cols.MID+" INTEGER, "+Visitor.Cols.DATETIME+" DATETIME, "
				+Visitor.Cols.DATE+" TEXT, "+Visitor.Cols.TIME+" TEXT, "
				+Visitor.Cols.DENY+" TEXT, "+Visitor.Cols.CARDNO+" TEXT, "
				+Visitor.Cols.DOORNAME+" TEXT, "+Visitor.Cols.MSID+" INTEGER, "
				+Visitor.Cols.LASTUPDATED+" NUMERIC "
				+");");
		
		db.execSQL("CREATE TABLE "+Membership.NAME+" ("+Membership.Cols._ID+" INTEGER PRIMARY KEY, "
				+Membership.Cols.MID+" INTEGER, "+Membership.Cols.MSID+" INTEGER, "
				+Membership.Cols.CARDNO+" TEXT, "+Membership.Cols.DENY+" INTEGER, "
				+Membership.Cols.PNAME+" TEXT, "+Membership.Cols.MSSTART+" TEXT, "
				+Membership.Cols.EXPIRERY+" TEXT, "+Membership.Cols.VISITS+" TEXT, "
				+Membership.Cols.LASTUPDATED+" TIMESTAMP NOT NULL DEFAULT current_timstamp );"); //ms since epoch;
		
		//for quick look up of Images:, Uses composite PK
		db.execSQL("CREATE TABLE "+ContentDescriptor.Image.NAME+" ("+ContentDescriptor.Image.Cols.ID+" INTEGER, "
				+ContentDescriptor.Image.Cols.MID+ " INTEGER, "+ContentDescriptor.Image.Cols.DATE+" DATETIME, "
				+ContentDescriptor.Image.Cols.DESCRIPTION+" TEXT , "+ContentDescriptor.Image.Cols.IS_PROFILE+" INTEGER, "
				+"FOREIGN KEY("+ContentDescriptor.Image.Cols.MID
				+") REFERENCES "+ContentDescriptor.Member.NAME+"("+ContentDescriptor.Member.Cols.MID+"), "
				+"PRIMARY KEY ("+ContentDescriptor.Image.Cols.ID+", "+ContentDescriptor.Image.Cols.MID+"));");
	
		// add member/prospect --> pending uploads table?
		// use this table for holding unused MID's?
		db.execSQL(" CREATE TABLE "+Pending.NAME+" ("+Pending.Cols.ID+" INTEGER PRIMARY KEY, "
				+Pending.Cols.FNAME+" TEXT, "+Pending.Cols.SNAME+" TEXT, "
				+Pending.Cols.DOB+" TEXT, "+Pending.Cols.GENDER+" TEXT, "
				+Pending.Cols.MEDICAL+" TEXT, "+Pending.Cols.STREET+" TEXT, "
				+Pending.Cols.SUBURB+" TEXT, "+Pending.Cols.CITY+" TEXT, "
				+Pending.Cols.POSTAL+" TEXT, "+Pending.Cols.EMAIL+" TEXT, "
				+Pending.Cols.HPHONE+" TEXT, "+Pending.Cols.CPHONE+" TEXT, "
				+Pending.Cols.SIGNUP+" TEXT, "+Pending.Cols.MID+" INTEGER, "
				+Pending.Cols.ISUSED+" INTEGER );");
		
		db.execSQL("CREATE TABLE "+ContentDescriptor.Time.NAME+" ("+ContentDescriptor.Time.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Time.Cols.TIME+" TEXT ); ");
		
		db.execSQL("CREATE TABLE "+ContentDescriptor.OpenTime.NAME+" ("+ContentDescriptor.OpenTime.Cols._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ContentDescriptor.OpenTime.Cols.DAYOFWEEK+" INTEGER, "+ContentDescriptor.OpenTime.Cols.OPENTIME+" TEXT, "
				+ContentDescriptor.OpenTime.Cols.OPENID+" INTEGER, "+ContentDescriptor.OpenTime.Cols.CLOSETIME+" TEXT, "
				+ContentDescriptor.OpenTime.Cols.CLOSEID+" INTEGER, "+ContentDescriptor.OpenTime.Cols.NAME+" TEXT );");
		
		db.execSQL("CREATE TABLE "+ContentDescriptor.Date.NAME+" ("+ContentDescriptor.Date.Cols._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ContentDescriptor.Date.Cols.DATE+" INTEGER, "+ContentDescriptor.Date.Cols.DAYOFWEEK+" INTEGER, "
				+" FOREIGN KEY ("+ContentDescriptor.Date.Cols.DAYOFWEEK+" ) REFERENCES "+ContentDescriptor.OpenTime.NAME
				+" ("+ContentDescriptor.OpenTime.Cols.DAYOFWEEK+"));");
		
		//bookings, 
		db.execSQL("CREATE TABLE "+Booking.NAME+" ("+Booking.Cols.ID+" INTEGER PRIMARY KEY, "
				+Booking.Cols.FNAME+" TEXT, "+Booking.Cols.SNAME+" TEXT, "
				+Booking.Cols.BOOKING+" TEXT, "
				+Booking.Cols.STIMEID+" INTEGER, "+Booking.Cols.ETIMEID+" INTEGER, "
				+Booking.Cols.BID+" TEXT, "+Booking.Cols.BOOKINGTYPE+" INTEGER, "
				+Booking.Cols.ETIME+" TEXT, "+Booking.Cols.NOTES+" TEXT, "
				+Booking.Cols.RESULT+" INTEGER, "+Booking.Cols.MID+" INTEGER, "
				+Booking.Cols.LASTUPDATED+" NUMERIC, "+Booking.Cols.STIME+" TEXT, "
				+Booking.Cols.MSID+" INTEGER, "+Booking.Cols.CHECKIN+" NUMERIC, " //timestamp ?
				+Booking.Cols.RID+" INTEGER, "+Booking.Cols.ARRIVAL+" INTEGER, "
				+Booking.Cols.OFFSET+" TEXT, "+Booking.Cols.IS_UPLOADED+" INTEGER DEFAULT 1, "
				+Booking.Cols.CLASSID+" INTEGER DEFAULT 0, "+Booking.Cols.PARENTID+" INTEGER DEFAULT 0, "
				+"FOREIGN KEY ("+Booking.Cols.STIMEID
				+") REFERENCES "+ContentDescriptor.Time.NAME+" ("+ContentDescriptor.Time.Cols.ID+") "
				//+" FOREIGN KEY ("+Booking.Cols.ARRIVAL+") REFERENCES "+ContentDescriptor.Date.NAME+" ("
				//+ContentDescriptor.Date.Cols.DATE+"));"); //lastupdated = seconds since epoch.
				+");");
		
		db.execSQL("CREATE TABLE "+BookingTime.NAME+" ("+BookingTime.Cols._ID+" INTEGER PRIMARY KEY, "
				+BookingTime.Cols.TIMEID+" INTEGER, "+BookingTime.Cols.BID+" INTEGER, "
				+BookingTime.Cols.RID+" INTEGER, "+BookingTime.Cols.ARRIVAL+" INTEGER, "
				+"FOREIGN KEY ("+BookingTime.Cols.BID+") REFERENCES "+Booking.NAME+" ("+Booking.Cols.BID+"), "
				+"FOREIGN KEY ("+BookingTime.Cols.TIMEID+") REFERENCES "+Time.NAME+" ("+Time.Cols.ID+"), "
				+"FOREIGN KEY ("+BookingTime.Cols.RID+") REFERENCES "+Resource.NAME+" ("+Resource.Cols.ID+"), "
				+"FOREIGN KEY ("+BookingTime.Cols.ARRIVAL+") REFERENCES "+Date.NAME+" ("+Date.Cols.DATE+"));");
		
		//bookingtypes
		db.execSQL("CREATE TABLE "+ContentDescriptor.Bookingtype.NAME+" ("+ContentDescriptor.Bookingtype.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Bookingtype.Cols.NAME+" TEXT, "+ContentDescriptor.Bookingtype.Cols.PRICE+" TEXT, "
				+ContentDescriptor.Bookingtype.Cols.VALIDFROM+" TEXT, "+ContentDescriptor.Bookingtype.Cols.VALIDTO+" TEXT, "
				+ContentDescriptor.Bookingtype.Cols.EXTERNAL+" TEXT, "+ContentDescriptor.Bookingtype.Cols.BTID+" TEXT );");
		
		//resources (for bookings).
		db.execSQL("CREATE TABLE "+ContentDescriptor.Resource.NAME+" ("+ContentDescriptor.Resource.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Resource.Cols.CID+" TEXT, "+ContentDescriptor.Resource.Cols.NAME+" TEXT, "
				+ContentDescriptor.Resource.Cols.RTNAME+" TEXT, "+Resource.Cols.PERIOD+" TEXT );");
		
		//classes?
		db.execSQL("CREATE TABLE "+Programme.NAME+" ("+Programme.Cols.ID+" INTEGER PRIMARY KEY, "
				+Programme.Cols.GID+" INTEGER, "+Programme.Cols.NAME+" TEXT, "
				+Programme.Cols.GNAME+" TEXT, "+Programme.Cols.MLENGTH+" TEXT, "
				+Programme.Cols.SDATE+" TEXT, "+Programme.Cols.EDATE+" TEXT, "
				+Programme.Cols.PRICE+" TEXT, "+Programme.Cols.SIGNUP+" TEXT, "
				+Programme.Cols.ONLINE+" TEXT"+Programme.Cols.NOTE+" TEXT );"); //add memberships, lookup priority
		
		db.execSQL("CREATE TABLE "+ContentDescriptor.Swipe.NAME+" ("+ContentDescriptor.Swipe.Cols.ID+" TEXT, "
				+ContentDescriptor.Swipe.Cols.DOOR+" INTEGER, "+ContentDescriptor.Swipe.Cols.DATETIME+" DATETIME, "
				+"PRIMARY KEY ("+ContentDescriptor.Swipe.Cols.ID+", "+ContentDescriptor.Swipe.Cols.DATETIME+"));");
		
		db.execSQL("CREATE TABLE "+ContentDescriptor.ResultStatus.NAME+" ("+ContentDescriptor.ResultStatus.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.ResultStatus.Cols.NAME+" TEXT, "+ContentDescriptor.ResultStatus.Cols.COLOUR+" TEXT );");
		
		db.execSQL("CREATE TABLE "+ContentDescriptor.Class.NAME+" ("+ContentDescriptor.Class.Cols._ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Class.Cols.CID+" INTEGER, "+ContentDescriptor.Class.Cols.NAME+" TEXT, "
				+ContentDescriptor.Class.Cols.SDATE+" INTEGER, "+ContentDescriptor.Class.Cols.FREQ+" TEXT, "
				+ContentDescriptor.Class.Cols.STIME+" TEXT, "+ContentDescriptor.Class.Cols.ETIME+" TEXT, "
				+ContentDescriptor.Class.Cols.MAX_ST+" INTEGER, "+ContentDescriptor.Class.Cols.RID+" INTEGER, "
				+ContentDescriptor.Class.Cols.LASTUPDATED+" NUMERIC "
				+");");
		
		/* TODO:
		 * 	Consider adding a pending-uploads table that takes:
		 * 		- an id (representing the table the pending upload is in),
		 * 		- a row id (for the row in the table)
		 * 		- a timestamp (?)
		 * 
		 * something like 	tableid 1 = booking (upload booking)
		 * 					tableid 2 = class
		 * 					tableid 3 = swipe
		 * 					tableid 4 = member
		 * 					tableid 5 = image
		 */
		db.execSQL("CREATE TABLE "+ContentDescriptor.TableIndex.NAME+" ("+ContentDescriptor.TableIndex.Cols._ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.TableIndex.Cols.NAME+" TEXT "
				+");");
		
		db.execSQL("CREATE TABLE "+PendingUploads.NAME+" ("+PendingUploads.Cols._ID+" INTEGER PRIMARY KEY, "
				+PendingUploads.Cols.TABLEID+" INTEGER, "+PendingUploads.Cols.ROWID+" INTEGER, "
				+"FOREIGN KEY ("+PendingUploads.Cols.TABLEID+") REFERENCES "+TableIndex.NAME+" ("+TableIndex.Cols._ID+") "
				+");");
		
		repopulateTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(HornetDatabase.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		dropTables(db);
		onCreate(db);
		
	}
	
	public void dropTables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "+Member.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Image.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Visitor.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Membership.NAME);
		
		db.execSQL("ALTER TABLE "+Pending.NAME+" RENAME TO old_"+Pending.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+BookingTime.NAME);
		
		//below code is to move unused bookingID's into the rebuilt database. (save getting them again).
		Cursor cur = db.query(Booking.NAME, null, Booking.Cols.LASTUPDATED+" = 0", 
				null, null, null, null);
		db.execSQL("CREATE TABLE old_"+Booking.NAME+" ("+Booking.Cols.ID+" INTEGER PRIMARY KEY, "
				+Booking.Cols.BID+" INTEGER, "+Booking.Cols.LASTUPDATED+" NUMERIC );");
		while (cur.moveToNext()) {
			ContentValues values = new ContentValues();
			values.put(Booking.Cols.BID, cur.getString(cur.getColumnIndex(Booking.Cols.BID)));
			values.put(Booking.Cols.LASTUPDATED, cur.getString(cur.getColumnIndex(Booking.Cols.LASTUPDATED)));
			db.insert("old_"+Booking.NAME, null, values);
		}
		cur.close();
		
		db.execSQL("DROP TABLE IF EXISTS "+Booking.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Resource.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Company.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Swipe.NAME);   //likewise, this should be saved rather than drop.
		db.execSQL("DROP TABLE IF EXISTS "+Programme.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Time.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Bookingtype.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ResultStatus.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+OpenTime.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Date.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Class.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+TableIndex.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+PendingUploads.NAME);
	}
	
	private void repopulateTable(SQLiteDatabase db) {
		
		Cursor cur = db.query("sqlite_master", new String[] {"name"}, "type='table' AND name='old_Pending'", null, null, null, null);
		if (cur.getCount() != 0) {
			db.execSQL("INSERT INTO "+Pending.NAME+" SELECT * FROM old_"+Pending.NAME+";");
			db.execSQL("DROP TABLE old_"+Pending.NAME);
		}
		cur.close();
		
		//restore saved bookingID's
		cur = db.query("sqlite_master", new String[] {"name"}, "type='table' AND name='old_"+Booking.NAME+"'", null, null, null, null);
		if (cur.getCount() != 0) {
			cur.close();
			cur = db.query("old_"+Booking.NAME, null, null, null, null, null, null);
			while (cur.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(Booking.Cols.BID, cur.getString(cur.getColumnIndex(Booking.Cols.BID)));
				values.put(Booking.Cols.LASTUPDATED, cur.getString(cur.getColumnIndex(Booking.Cols.LASTUPDATED)));
				db.insert(Booking.NAME, null, values);
			}
			cur.close();
			db.execSQL("DROP TABLE old_"+Booking.NAME);
		} else {
			cur.close();
		}
		
		
		{
			ContentValues values = new ContentValues();
			for (int i=1; i <=ContentDescriptor.TableIndex.Values.getLength(); i +=1) {
				values.put(ContentDescriptor.TableIndex.Cols._ID, i);
				values.put(ContentDescriptor.TableIndex.Cols.NAME, ContentDescriptor.TableIndex.Values.getValue(i).toString());
				
				db.insert(ContentDescriptor.TableIndex.NAME, null, values);
			}
			
		}
				
		/*cur = db.query(ContentDescriptor.Pending.NAME, null, null, null, null, null, null);
		cur.moveToFirst();
		for (int i=0;i<cur.getCount();i+=1){
			for (int j=0;j<cur.getColumnCount();j+=1){
				System.out.print("\n Row:"+i+"  column:"+j+" title: "+cur.getColumnName(j)+" Value:"+cur.getString(j));
			}
			cur.moveToNext();
		}
		cur.close();*/				
	}
}