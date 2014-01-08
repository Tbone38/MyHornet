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
import com.treshna.hornet.ContentDescriptor.Door;
import com.treshna.hornet.ContentDescriptor.FreeIds;
import com.treshna.hornet.ContentDescriptor.IdCard;
import com.treshna.hornet.ContentDescriptor.Image;
import com.treshna.hornet.ContentDescriptor.Member;
import com.treshna.hornet.ContentDescriptor.MemberBalance;
import com.treshna.hornet.ContentDescriptor.MemberNotes;
import com.treshna.hornet.ContentDescriptor.Membership;
import com.treshna.hornet.ContentDescriptor.MembershipSuspend;
import com.treshna.hornet.ContentDescriptor.OpenTime;
import com.treshna.hornet.ContentDescriptor.PaymentMethod;
import com.treshna.hornet.ContentDescriptor.PendingDeletes;
import com.treshna.hornet.ContentDescriptor.PendingDownloads;
import com.treshna.hornet.ContentDescriptor.PendingUpdates;
import com.treshna.hornet.ContentDescriptor.PendingUploads;
import com.treshna.hornet.ContentDescriptor.Programme;
import com.treshna.hornet.ContentDescriptor.Resource;
import com.treshna.hornet.ContentDescriptor.ResultStatus;
import com.treshna.hornet.ContentDescriptor.RollCall;
import com.treshna.hornet.ContentDescriptor.RollItem;
import com.treshna.hornet.ContentDescriptor.Swipe;
import com.treshna.hornet.ContentDescriptor.TableIndex;
import com.treshna.hornet.ContentDescriptor.Time;
import com.treshna.hornet.ContentDescriptor.Visitor;

public class HornetDatabase extends SQLiteOpenHelper {
	
	 public static final String DATABASE_NAME="hornet.db";
	 private static final int DATABASE_VERSION = 92;
	 //private Context theContext;
	 //^^may be required for toasts etc at some point.
	 
	 public HornetDatabase (Context context) {
		 super(context, DATABASE_NAME, null, DATABASE_VERSION);
		 //this.theContext = context;
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//Member Table.
		db.execSQL("CREATE TABLE "+Member.NAME+" ("+Member.Cols._ID+" INTEGER PRIMARY KEY, "
				+Member.Cols.MID+" INTEGER NOT NULL, "
				+Member.Cols.COLOUR+" TEXT, "+Member.Cols.HAPPINESS+" TEXT, "
				+Member.Cols.LENGTH+" TEXT, "+Member.Cols.BOOKP+" INTEGER, "
				+Member.Cols.TASKP+" INTEGER, "+Member.Cols.RESULT+" TEXT, "
				+Member.Cols.PHHOME+" TEXT, "+Member.Cols.PHCELL+" TEXT, "
				+Member.Cols.PHWORK+" TEXT, "+Member.Cols.EMAIL+" TEXT, "
				+Member.OldCols.NOTES+" TEXT, "+Member.Cols.TASK1+" TEXT, "
				+Member.Cols.TASK2+" TEXT, "+Member.Cols.TASK3+" TEXT, "
				+Member.Cols.BOOK1+" TEXT, "+Member.Cols.BOOK2+" TEXT, "
				+Member.Cols.BOOK3+" TEXT, "+Member.Cols.LASTVISIT+" TEXT, "
				+Member.Cols.STATUS+" INTEGER, "+Member.Cols.FNAME+" TEXT, "
				+Member.Cols.SNAME+" TEXT, "+Member.Cols.GENDER+" TEXT, "
				+Member.Cols.DOB+" TEXT, "+Member.OldCols.MEDICAL+" TEXT, "
				+Member.Cols.STREET+" TEXT, "+Member.Cols.SUBURB+" TEXT, "
				+Member.Cols.CITY+" TEXT, "+Member.Cols.POSTAL+" TEXT "
				+");");
		//Member indexs.
		db.execSQL("CREATE INDEX "+Member.Indexs.MEMBER_NAME+" ON "+Member.NAME+" ( "
				+Member.Cols.FNAME+","+Member.Cols.SNAME+");");
		
		
		//LastVisitor Table. (entry exit?)
		db.execSQL("CREATE TABLE "+Visitor.NAME+" ("+Visitor.Cols.ID+" INTEGER PRIMARY KEY, "
				+Visitor.Cols.MID+" INTEGER, "+Visitor.Cols.DATETIME+" DATETIME, "
				+Visitor.Cols.DATE+" TEXT, "+Visitor.Cols.TIME+" TEXT, "
				+Visitor.Cols.DENY+" TEXT, "+Visitor.Cols.CARDNO+" TEXT, "
				+Visitor.Cols.DOORNAME+" TEXT, "+Visitor.Cols.MSID+" INTEGER, "
				+Visitor.Cols.LASTUPDATE+" NUMERIC "
				+");");
		//Visitors indexes.
		db.execSQL("CREATE INDEX "+Visitor.Indexs.MEMBER_ID+" ON "+Visitor.NAME+" ( "
				+Visitor.Cols.MID+" );");
		db.execSQL("CREATE INDEX "+Visitor.Indexs.MS_ID+" ON "+Visitor.NAME+" ( "
				+Visitor.Cols.MSID+" );");
		db.execSQL("CREATE INDEX "+Visitor.Indexs.DATE_TIME+" ON "+Visitor.NAME+" ( "
				+Visitor.Cols.DATETIME+" );");
		
		
		//Membership Table
		db.execSQL("CREATE TABLE "+Membership.NAME+" ("+Membership.Cols._ID+" INTEGER PRIMARY KEY, "
				+Membership.Cols.MID+" INTEGER NOT NULL, "+Membership.Cols.MSID+" INTEGER, "
				+Membership.Cols.CARDNO+" TEXT, "+Membership.Cols.DENY+" INTEGER, "
				+Membership.Cols.PNAME+" TEXT, "+Membership.Cols.MSSTART+" TEXT, "
				+Membership.Cols.EXPIRERY+" TEXT, "+Membership.Cols.VISITS+" TEXT, "
				+Membership.Cols.LASTUPDATE+" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+Membership.Cols.PRIMARYMS+" INTEGER, "+Membership.Cols.PID+" INTEGER, "
				+Membership.Cols.PGID+" INTEGER, "+Membership.Cols.PRICE+" TEXT, "
				+Membership.Cols.SIGNUP+" TEXT, "+Membership.Cols.CREATION+" TEXT"
				+");"); 
		//Membership Indexes
		db.execSQL("CREATE INDEX "+Membership.Indexs.MEMBER_ID+" ON "+Membership.NAME+" ( "
				+Membership.Cols.MID+" );");
		db.execSQL("CREATE INDEX "+Membership.Indexs.MEMBERSHIP_ID+" ON "+Membership.NAME+" ( "
				+Membership.Cols.MSID+" );");
		
		
		//Image Table
		db.execSQL("CREATE TABLE "+ContentDescriptor.Image.NAME+" ("+ContentDescriptor.Image.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Image.Cols.MID+ " INTEGER, "+ContentDescriptor.Image.Cols.DATE+" DATETIME, "
				+ContentDescriptor.Image.Cols.DESCRIPTION+" TEXT , "+ContentDescriptor.Image.Cols.IS_PROFILE+" INTEGER, "//IS_PROFILE row contains text. change the datattype.
				+ContentDescriptor.Image.Cols.DISPLAYVALUE+" INTEGER, "
				+"FOREIGN KEY("+ContentDescriptor.Image.Cols.MID
				+") REFERENCES "+ContentDescriptor.Member.NAME+"("+ContentDescriptor.Member.Cols.MID+") "
				+");");
		//image Indexs
		db.execSQL("CREATE INDEX "+Image.Indexs.MEMBER_ID+" ON "+Image.NAME+" ( "
				+Image.Cols.MID+" );");
		
		//time slots table.
		db.execSQL("CREATE TABLE "+ContentDescriptor.Time.NAME+" ("+ContentDescriptor.Time.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Time.Cols.TIME+" TEXT ); ");
		
		//OpenHours
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
				+Booking.Cols.LASTUPDATE+" NUMERIC, "+Booking.Cols.STIME+" TEXT, "
				+Booking.Cols.MSID+" INTEGER, "+Booking.Cols.CHECKIN+" NUMERIC, " //timestamp ?
				+Booking.Cols.RID+" INTEGER, "+Booking.Cols.ARRIVAL+" INTEGER, "
				+Booking.Cols.OFFSET+" TEXT, "/*+Booking.Cols.IS_UPLOADED+" INTEGER DEFAULT 1, "*/
				+Booking.Cols.CLASSID+" INTEGER DEFAULT 0, "+Booking.Cols.PARENTID+" INTEGER DEFAULT 0, "
				+"FOREIGN KEY ("+Booking.Cols.STIMEID
				+") REFERENCES "+ContentDescriptor.Time.NAME+" ("+ContentDescriptor.Time.Cols.ID+") "
				+");");
		//booking indexs
		db.execSQL("CREATE INDEX "+Booking.Indexs.BOOKING_ID+" ON "+Booking.NAME+" ( "
				+Booking.Cols.BID+");");
		db.execSQL("CREATE INDEX "+Booking.Indexs.BOOKING_NAME+" ON "+Booking.NAME+" ( "
				+Booking.Cols.FNAME+","+Booking.Cols.SNAME+");");
		db.execSQL("CREATE INDEX "+Booking.Indexs.CLASS_ID+" ON "+Booking.NAME+" ( "
				+Booking.Cols.CLASSID+");");
		db.execSQL("CREATE INDEX "+Booking.Indexs.MEMBER_ID+" ON "+Booking.NAME+" ( "
				+Booking.Cols.MID+");");
		db.execSQL("CREATE INDEX "+Booking.Indexs.MEMBERSHIP_ID+" ON "+Booking.NAME+" ( "
				+Booking.Cols.MSID+");");
		db.execSQL("CREATE INDEX "+Booking.Indexs.RESOURCE_ID+" ON "+Booking.NAME+" ( "
				+Booking.Cols.RID+");");
		
		
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
		
		//Programmes (and ProgrammeGroups).
		db.execSQL("CREATE TABLE "+Programme.NAME+" ("+Programme.Cols._ID+" INTEGER PRIMARY KEY, "
				+Programme.Cols.GID+" INTEGER, "+Programme.Cols.NAME+" TEXT, "
				+Programme.Cols.GNAME+" TEXT, "+Programme.Cols.MLENGTH+" TEXT, "
				+Programme.Cols.SDATE+" TEXT, "+Programme.Cols.EDATE+" TEXT, "
				+Programme.Cols.PRICE+" TEXT, "+Programme.Cols.SIGNUP+" TEXT, "
				+Programme.Cols.NOTE+" TEXT, "+Programme.Cols.PID+" INTEGER, "
				+Programme.Cols.LASTUPDATE+" TEXT, "+Programme.Cols.PRICE_DESC+" TEXT "
				+");");
		//Programme Indexs;
		db.execSQL("CREATE INDEX "+Programme.Indexs.GROUP_ID+" ON "+Programme.NAME+" ( "
				+Programme.Cols.GID+");");
		db.execSQL("CREATE INDEX "+Programme.Indexs.PROGRAMME_ID+" ON "+Programme.NAME+" ( "
				+Programme.Cols.PID+");");
		
		
		//door swipes, to do: complete this.
		db.execSQL("CREATE TABLE "+ContentDescriptor.Swipe.NAME+" ("+ContentDescriptor.Swipe.Cols.ID+" TEXT, "
				+ContentDescriptor.Swipe.Cols.DOOR+" INTEGER, "+ContentDescriptor.Swipe.Cols.DATETIME+" DATETIME, "
				+"PRIMARY KEY ("+ContentDescriptor.Swipe.Cols.ID+", "+ContentDescriptor.Swipe.Cols.DATETIME+"));");
		
		//ResultStatus, used for bookings.
		db.execSQL("CREATE TABLE "+ContentDescriptor.ResultStatus.NAME+" ("+ContentDescriptor.ResultStatus.Cols.ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.ResultStatus.Cols.NAME+" TEXT, "+ContentDescriptor.ResultStatus.Cols.COLOUR+" TEXT );");
		
		//Class Table
		db.execSQL("CREATE TABLE "+ContentDescriptor.Class.NAME+" ("+ContentDescriptor.Class.Cols._ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.Class.Cols.CID+" INTEGER, "+ContentDescriptor.Class.Cols.NAME+" TEXT, "
				+ContentDescriptor.Class.Cols.SDATE+" INTEGER, "+ContentDescriptor.Class.Cols.FREQ+" TEXT, "
				+ContentDescriptor.Class.Cols.STIME+" TEXT, "+ContentDescriptor.Class.Cols.ETIME+" TEXT, "
				+ContentDescriptor.Class.Cols.MAX_ST+" INTEGER, "+ContentDescriptor.Class.Cols.RID+" INTEGER, "
				+ContentDescriptor.Class.Cols.LASTUPDATE+" NUMERIC, "+ContentDescriptor.Class.Cols.ONLINE+" INTEGER DEFAULT 1,"
				+ContentDescriptor.Class.Cols.DESC+" TEXT "
				+");");
		//class Indexs;
		db.execSQL("CREATE INDEX "+Class.Indexs.CLASS_ID+" ON "+Class.NAME+" ( "
				+Class.Cols.CID+");");
		db.execSQL("CREATE INDEX "+Class.Indexs.RESOURCE_ID+" ON "+Class.NAME+" ( "
				+Class.Cols.RID+");");
		
		
		/*
		 * used for referencing other tables in the database.
		 */
		db.execSQL("CREATE TABLE "+ContentDescriptor.TableIndex.NAME+" ("+ContentDescriptor.TableIndex.Cols._ID+" INTEGER PRIMARY KEY, "
				+ContentDescriptor.TableIndex.Cols.NAME+" TEXT "
				+");");
		
		//pending uploads table.
		db.execSQL("CREATE TABLE "+PendingUploads.NAME+" ("+PendingUploads.Cols._ID+" INTEGER PRIMARY KEY, "
				+PendingUploads.Cols.TABLEID+" INTEGER, "+PendingUploads.Cols.ROWID+" INTEGER, "
				+"FOREIGN KEY ("+PendingUploads.Cols.TABLEID+") REFERENCES "+TableIndex.NAME+" ("+TableIndex.Cols._ID+") "
				+");");
		
		//add a table that forces a row to redownload all it's data?
		//it will use the Postgresql ID's in the rowid column (MemberID, membershipID, etc), 
		//not the local id's.
		// (over-write all previous data for row).
		db.execSQL("CREATE TABLE "+PendingDownloads.NAME+" ("+PendingDownloads.Cols._ID+" INTEGER PRIMARY KEY, "
				+PendingDownloads.Cols.TABLEID+" INTEGER, "+PendingDownloads.Cols.ROWID+" INTEGER "
				+");");
		
		//membership suspend table.
		db.execSQL("CREATE TABLE "+MembershipSuspend.Old.NAME+" ("+MembershipSuspend.Cols._ID+" INTEGER PRIMARY KEY, "
				+MembershipSuspend.Cols.SID+" INTEGER, "+MembershipSuspend.Cols.MID+" INTEGER DEFAULT 0, "
				+MembershipSuspend.Cols.STARTDATE+" INTEGER, "+MembershipSuspend.Cols.REASON+" TEXT, "
				+MembershipSuspend.Cols.LENGTH+" INTEGER, "+MembershipSuspend.Cols.ENDDATE+" TEXT, "
				+MembershipSuspend.Cols.FREEZE+" INTEGER "
				+");");
		
		//idCard table
		db.execSQL("CREATE TABLE "+IdCard.NAME+" ("+IdCard.Cols._ID+" INTEGER PRIMARY KEY, "
				+IdCard.Cols.CARDID+" INTEGER, "+IdCard.Cols.SERIAL+" TEXT "
				+");");
		
		//paymentMethod table.
		db.execSQL("CREATE TABLE "+PaymentMethod.NAME+" ("+PaymentMethod.Cols._ID+" INTEGER PRIMARY KEY, "
				+PaymentMethod.Cols.PAYMENTID+" INTEGER, "+PaymentMethod.Cols.NAME+" TEXT "
				+");");
		
		db.execSQL("CREATE TABLE "+Door.NAME+" ("+Door.Cols._ID+" INTEGER PRIMARY KEY, "
				+Door.Cols.DOORID+" INTEGER, "+Door.Cols.DOORNAME+" TEXT "
				+");");
		
		db.execSQL("CREATE TABLE "+MemberNotes.NAME+" ("+MemberNotes.Cols._ID+" INTEGER PRIMARY KEY, "
				+MemberNotes.Cols.MNID+" INTEGER NOT NULL DEFAULT 0, "+MemberNotes.Cols.MID+" INTEGER NOT NULL DEFAULT 0, "
				+MemberNotes.Cols.NOTES+" TEXT, "+MemberNotes.Cols.OCCURRED+" TEXT "
				+" );");
		
		db.execSQL("CREATE TABLE "+MemberBalance.NAME+" ("+MemberBalance.Cols._ID+" INTEGER PRIMARY KEY, "
				+MemberBalance.Cols.MID+" INTEGER NOT NULL, "+MemberBalance.Cols.BALANCE+" TEXT, "
				+MemberBalance.Cols.LASTUPDATE+" NUMERIC "
				+");");
		
		//todo remove this:
		//repopulateTable(db);
		setupTableIndex(db);
		
		//SQL patches.
		db.execSQL(UpdateDatabase.Ninety.SQL);
		db.execSQL(UpdateDatabase.NinetyOne.SQL);
		db.execSQL(UpdateDatabase.NinetyTwo.SQL);
		UpdateDatabase.NinetyThree.patchNinetyThree(db);
		//db.execSQL("pragma full_column_names=ON;"); //TODO: will this break stuff?*/
	}
	
	private void setupTableIndex(SQLiteDatabase db) {

		db.delete(ContentDescriptor.TableIndex.NAME, null, null);
		
		ContentValues values = new ContentValues();
		for (int i=1; i <=ContentDescriptor.TableIndex.Values.getLength(); i +=1) {
			values.put(ContentDescriptor.TableIndex.Cols._ID, i);
			values.put(ContentDescriptor.TableIndex.Cols.NAME, ContentDescriptor.TableIndex.Values.getValue(i).toString());
			
			db.insert(ContentDescriptor.TableIndex.NAME, null, values);
		}
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(HornetDatabase.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion );
		
		for (int version = (oldVersion+1); version <= newVersion; version++) { //old+1 because we're already at the old version.
			if (version <= 89) {
				/*Drop db and create db include the alters already, if we're baselining we can
				 * skip the rest of this switch case.
				 */
				dropTables(db);
				onCreate(db);
				return;
			}
			//repopulate the index table.
			setupTableIndex(db);
			
			switch (version){
			case (90):{
				Log.w(HornetDatabase.class.getName(),"SQL-Patch:90 \n"+ UpdateDatabase.Ninety.SQL);
				db.execSQL(UpdateDatabase.Ninety.SQL);
				break;
			}
			case (91):{
				Log.w(HornetDatabase.class.getName(), "SQL-Patch:91 \n"+UpdateDatabase.NinetyOne.SQL);
				db.execSQL(UpdateDatabase.NinetyOne.SQL);
				break;
			}
			case (92):{
				Log.w(HornetDatabase.class.getName(), "SQL-Patch:92 \n"+UpdateDatabase.NinetyTwo.SQL);
				db.execSQL(UpdateDatabase.NinetyTwo.SQL);
				break;
			}
			case (93):{
				UpdateDatabase.NinetyThree.patchNinetyThree(db);
				//db.execSQL("pragma full_column_names=ON;"); //TODO: will this break stuff? 
				break;
			}
			}
		}
		
	}
	
	
	public void dropTables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "+Visitor.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Member.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Membership.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Image.NAME);
		
		/* I don't think this code is needed any more.
		 * Cursor cur = db.query(Booking.NAME, null, Booking.Cols.LASTUPDATE+" = 0", 
				null, null, null, null);
		db.execSQL("CREATE TABLE old_"+Booking.NAME+" ("+Booking.Cols.ID+" INTEGER PRIMARY KEY, "
				+Booking.Cols.BID+" INTEGER, "+Booking.Cols.LASTUPDATE+" NUMERIC );");
		while (cur.moveToNext()) {
			ContentValues values = new ContentValues();
			values.put(Booking.Cols.BID, cur.getString(cur.getColumnIndex(Booking.Cols.BID)));
			values.put(Booking.Cols.LASTUPDATE, cur.getString(cur.getColumnIndex(Booking.Cols.LASTUPDATE)));
			db.insert("old_"+Booking.NAME, null, values);
		}
		cur.close();*/
		
		db.execSQL("DROP TABLE IF EXISTS "+Resource.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Company.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Swipe.NAME);   
		
		db.execSQL("DROP TABLE IF EXISTS "+BookingTime.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Programme.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Booking.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Bookingtype.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Class.NAME); 
		
		db.execSQL("DROP TABLE IF EXISTS "+Time.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Date.NAME);
		
		db.execSQL("DROP TABLE IF EXISTS "+OpenTime.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+Door.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+IdCard.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+PaymentMethod.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ResultStatus.NAME);
		
		db.execSQL("DROP TABLE IF EXISTS "+MembershipSuspend.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+MemberNotes.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+MemberBalance.NAME);
		
		db.execSQL("DROP TABLE IF EXISTS "+PendingUploads.NAME); 
		db.execSQL("DROP TABLE IF EXISTS "+PendingDownloads.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+PendingUpdates.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+PendingDeletes.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+FreeIds.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+TableIndex.NAME);
		
		db.execSQL("DROP TABLE IF EXISTS "+RollCall.NAME);
		db.execSQL("DROP TABLE IF EXISTS "+RollItem.NAME);
	}
	
	private void repopulateTable(SQLiteDatabase db) {
		
		
		
		//restore saved bookingID's
		Cursor cur = db.query("sqlite_master", new String[] {"name"}, "type='table' AND name='old_"+Booking.NAME+"'", null, null, null, null);
		if (cur.getCount() != 0) {
			cur.close();
			cur = db.query("old_"+Booking.NAME, null, null, null, null, null, null);
			while (cur.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(Booking.Cols.BID, cur.getString(cur.getColumnIndex(Booking.Cols.BID)));
				values.put(Booking.Cols.LASTUPDATE, cur.getString(cur.getColumnIndex(Booking.Cols.LASTUPDATE)));
				db.insert(Booking.NAME, null, values);
			}
			cur.close();
			db.execSQL("DROP TABLE old_"+Booking.NAME);
		} else {
			cur.close();
		}
	}
	
}