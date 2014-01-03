package com.treshna.hornet;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.treshna.hornet.ContentDescriptor.Booking;
import com.treshna.hornet.ContentDescriptor.Class;
import com.treshna.hornet.ContentDescriptor.FreeIds;
import com.treshna.hornet.ContentDescriptor.Image;
import com.treshna.hornet.ContentDescriptor.Member;
import com.treshna.hornet.ContentDescriptor.MemberNotes;
import com.treshna.hornet.ContentDescriptor.Membership;
import com.treshna.hornet.ContentDescriptor.MembershipSuspend;
import com.treshna.hornet.ContentDescriptor.PendingDeletes;
import com.treshna.hornet.ContentDescriptor.PendingUpdates;
import com.treshna.hornet.ContentDescriptor.PendingUploads;
import com.treshna.hornet.ContentDescriptor.RollCall;
import com.treshna.hornet.ContentDescriptor.RollItem;
import com.treshna.hornet.ContentDescriptor.TableIndex;
import com.treshna.hornet.ContentDescriptor.Visitor;

/**
 * Ideally these want to be seperate sql files in the assets folder/sub-dir.
 * However, I need to reference the column/table statics that are in the ContentDescriptor Class.
 * 
 * 
 * could use the string substitution with %s etc, though that involves a reasonable amount of manipulation,
 * which sort of defeats the purpose.
 * @author callum
 *
 */
public class UpdateDatabase {
	
	public static class Ninety {
		public static final String SQL = "ALTER TABLE "+Image.NAME+" ADD COLUMN "
				+Image.Cols.IID+" INTEGER ;";
	}
	public static class NinetyOne {
		public static final String SQL = "ALTER TABLE "+Member.NAME+" ADD COLUMN "
				+Member.Cols.CARDNO+" INTEGER ;";
	}
	public static class NinetyTwo {
		public static final String SQL = "CREATE TABLE "+PendingUpdates.NAME+" ("
				+PendingUpdates.Cols._ID+" INTEGER PRIMARY KEY, "
				+PendingUpdates.Cols.ROWID+" INTEGER, "
				+PendingUpdates.Cols.TABLEID+" INTEGER, "
				+"FOREIGN KEY ("+PendingUpdates.Cols.TABLEID+") "
						+ "REFERENCES "+TableIndex.NAME+" ("+TableIndex.Cols._ID+") "
				+ ");";
	}
	public static class NinetyThree {
		public static final String SQL1 = 
				"ALTER TABLE "+Visitor.NAME+" RENAME TO tmp_"+Visitor.NAME+";";
		public static final String SQL2 = 		
				" CREATE TABLE "+Visitor.NAME+" ("+Visitor.Cols.ID+" INTEGER PRIMARY KEY,"
						+Visitor.Cols.MID+" INTEGER, "+Visitor.Cols.DATETIME+" TIMESTAMP, "
						+Visitor.Cols.DATE+" TEXT, "+Visitor.Cols.TIME+" TEXT, "
						+Visitor.Cols.DENY+" TEXT, "+Visitor.Cols.CARDNO+" TEXT, "
						+Visitor.Cols.DOORNAME+" TEXT, "+Visitor.Cols.MSID+" INTEGER, "
						+Visitor.Cols.LASTUPDATE+" NUMERIC "
						+");";
						//Visitors indexes.
				
		public static final String SQL3 =" INSERT INTO "+Visitor.NAME+"("+Visitor.Cols.ID+", "+Visitor.Cols.MID+", "
					+Visitor.Cols.DATETIME+", "+Visitor.Cols.DATE+", "+Visitor.Cols.TIME+", "+Visitor.Cols.DENY+", "
					+Visitor.Cols.CARDNO+", "+Visitor.Cols.DOORNAME+", "+Visitor.Cols.MSID+", "+Visitor.Cols.LASTUPDATE+")"
				+" SELECT "+Visitor.Cols.ID+", "+Visitor.Cols.MID+", "+Visitor.Cols.DATETIME+", "
					+Visitor.Cols.DATE+", "+Visitor.Cols.TIME+", "+Visitor.Cols.DENY+", "+Visitor.Cols.CARDNO+", "
					+Visitor.Cols.DOORNAME+", "+Visitor.Cols.MSID+", "+Visitor.Cols.LASTUPDATE
				+" FROM tmp_"+Visitor.NAME+";";
		public static final String SQL4 =
				"DROP TABLE tmp_"+Visitor.NAME+";\n";
				
				//create FreeIds table.
				//move any free ids into the table then delete them.
				//all future/current code needs to use this table.
		public static final String SQL5 ="CREATE TABLE "+FreeIds.NAME+" ("+FreeIds.Cols._ID+" INTEGER PRIMARY KEY, "
						+FreeIds.Cols.ROWID+" INTEGER, "+FreeIds.Cols.TABLEID+" INTEGER );";
				
		public static final String SQL6 ="INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") \n"
						+ "SELECT "+Member.Cols.MID+", "+TableIndex.Values.Member.getKey()
						+" FROM "+Member.NAME+" WHERE "+Member.Cols.STATUS+" =  -1 ;";
		public static final String SQL7 ="DELETE FROM "+Member.NAME+" WHERE "+Member.Cols.STATUS+" =  -1;";
				
		public static final String SQL8 ="INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+Booking.Cols.BID+", "+TableIndex.Values.Booking.getKey()
						+" FROM "+Booking.NAME+" WHERE "+Booking.Cols.LASTUPDATE+" = 0;";
		public static final String SQL9 ="DELETE FROM "+Booking.NAME+" WHERE "+Booking.Cols.LASTUPDATE+" = 0;";
				
		public static final String SQL10 ="INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+MembershipSuspend.Cols.SID+", "+TableIndex.Values.MembershipSuspend.getKey()
						+" FROM "+MembershipSuspend.Old.NAME+" WHERE "+MembershipSuspend.Cols.MID+" = 0;";
		public static final String SQL11 ="DELETE FROM "+MembershipSuspend.Old.NAME+" WHERE "+MembershipSuspend.Cols.MID+" <= 0;";
				
		public static final String SQL12 ="INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+Membership.Cols.MSID+", "+TableIndex.Values.Membership.getKey()
						+" FROM "+Membership.NAME+" WHERE "+Membership.Cols.MID+" = 0;";
		public static final String SQL13 ="DELETE FROM "+Membership.NAME+" WHERE "+Membership.Cols.MID+" = 0;";
				
		public static final String SQL14 ="INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+")"
						+ "SELECT "+MemberNotes.Cols.MNID+", "+TableIndex.Values.MemberNotes.getKey()
						+" FROM "+MemberNotes.NAME+" WHERE "+MemberNotes.Cols.MID+" = 0;";
		public static final String SQL15 ="DELETE FROM "+MemberNotes.NAME+" WHERE "+MemberNotes.Cols.MID+" = 0;";
				
				//move more triggers here.
				//TODO:ADD TRIGGERS FOR ON UPDATES/INSERTS that set pending UPLOADS/UPDATES table.
					
		public static final String SQL16 ="CREATE TABLE "+PendingDeletes.NAME+" ("+PendingDeletes.Cols._ID+" INTEGER PRIMARY KEY, "
						+PendingDeletes.Cols.ROWID+" INTEGER, "+PendingDeletes.Cols.TABLEID+" INTEGER );";
						
				
				// We're renaming the Medical column, and adding more medical Details/Emergency Contact Info.
				//also deleting the notes column (use the table instead). 
		public static final String SQL17 ="ALTER TABLE "+Member.NAME+" RENAME TO tmp_"+Member.NAME+";";
				
		public static final String SQL18 ="CREATE TABLE "+Member.NAME+" ("+Member.Cols._ID+" INTEGER PRIMARY KEY, "
				+Member.Cols.MID+" INTEGER NOT NULL, "+Member.Cols.CARDNO+" INTEGER, "
				+Member.Cols.COLOUR+" TEXT, "+Member.Cols.HAPPINESS+" TEXT, "
				+Member.Cols.LENGTH+" TEXT, "+Member.Cols.BOOKP+" INTEGER, "
				+Member.Cols.TASKP+" INTEGER, "+Member.Cols.RESULT+" TEXT, "
				+Member.Cols.PHHOME+" TEXT, "+Member.Cols.PHCELL+" TEXT, "
				+Member.Cols.PHWORK+" TEXT, "+Member.Cols.EMAIL+" TEXT, "
				+Member.Cols.TASK1+" TEXT, "
				+Member.Cols.TASK2+" TEXT, "+Member.Cols.TASK3+" TEXT, "
				+Member.Cols.BOOK1+" TEXT, "+Member.Cols.BOOK2+" TEXT, "
				+Member.Cols.BOOK3+" TEXT, "+Member.Cols.LASTVISIT+" TEXT, "
				+Member.Cols.STATUS+" INTEGER, "+Member.Cols.FNAME+" TEXT, "
				+Member.Cols.SNAME+" TEXT, "+Member.Cols.GENDER+" TEXT, "
				+Member.Cols.DOB+" TEXT, "+Member.Cols.MEDICAL+" TEXT, "
				+Member.Cols.STREET+" TEXT, "+Member.Cols.SUBURB+" TEXT, "
				+Member.Cols.CITY+" TEXT, "+Member.Cols.POSTAL+" TEXT, "
				+Member.Cols.EMERGENCYNAME+" TEXT, "+Member.Cols.EMERGENCYHOME+" TEXT, "
				+Member.Cols.EMERGENCYCELL+" TEXT, "+Member.Cols.EMERGENCYWORK+" TEXT, "
				+Member.Cols.MEDICATION+" TEXT, "+Member.Cols.MEDICALDOSAGE+" TEXT, "
				+Member.Cols.MEDICATIONBYSTAFF+" TEXT, "+Member.Cols.EMERGENCYRELATIONSHIP+" TEXT, "
				+Member.Cols.DEVICESIGNUP+" TEXT DEFAULT 'f' "
				+");";
				
		public static final String SQL19 ="INSERT INTO "+Member.NAME+" ("
					+Member.Cols._ID+", "+Member.Cols.MID+", "
					+Member.Cols.COLOUR+", "+Member.Cols.HAPPINESS+", "
					+Member.Cols.LENGTH+", "+Member.Cols.BOOKP+", "
					+Member.Cols.TASKP+", "+Member.Cols.RESULT+", "
					+Member.Cols.PHHOME+", "+Member.Cols.PHCELL+", "
					+Member.Cols.PHWORK+", "+Member.Cols.EMAIL+", "
					+Member.Cols.TASK1+", "
					+Member.Cols.TASK2+", "+Member.Cols.TASK3+", "
					+Member.Cols.BOOK1+", "+Member.Cols.BOOK2+", "
					+Member.Cols.BOOK3+", "+Member.Cols.LASTVISIT+", "
					+Member.Cols.STATUS+", "+Member.Cols.FNAME+", "
					+Member.Cols.SNAME+", "+Member.Cols.GENDER+", "
					+Member.Cols.DOB+", "+Member.Cols.MEDICAL+", "
					+Member.Cols.STREET+", "+Member.Cols.SUBURB+", "
					+Member.Cols.CITY+", "+Member.Cols.POSTAL+") "
				+"SELECT "+Member.Cols._ID+", "+Member.Cols.MID+", "
					+Member.Cols.COLOUR+", "+Member.Cols.HAPPINESS+", "
					+Member.Cols.LENGTH+", "+Member.Cols.BOOKP+", "
					+Member.Cols.TASKP+", "+Member.Cols.RESULT+", "
					+Member.Cols.PHHOME+", "+Member.Cols.PHCELL+", "
					+Member.Cols.PHWORK+", "+Member.Cols.EMAIL+", "
					+Member.Cols.TASK1+", "
					+Member.Cols.TASK2+", "+Member.Cols.TASK3+", "
					+Member.Cols.BOOK1+", "+Member.Cols.BOOK2+", "
					+Member.Cols.BOOK3+", "+Member.Cols.LASTVISIT+", "
					+Member.Cols.STATUS+", "+Member.Cols.FNAME+", "
					+Member.Cols.SNAME+", "+Member.Cols.GENDER+", "
					+Member.Cols.DOB+", "+Member.OldCols.MEDICAL+", "
					+Member.Cols.STREET+", "+Member.Cols.SUBURB+", "
					+Member.Cols.CITY+", "+Member.Cols.POSTAL
				+" FROM tmp_"+Member.NAME+";";
				
		public static final String SQL20 =" DROP TABLE tmp_"+Member.NAME+";"
		;

		public static final String SQL21 ="CREATE TRIGGER "+Member.Triggers.ON_INSERT+" AFTER INSERT ON "+Member.NAME
				+" FOR EACH ROW WHEN new."+Member.Cols.MID+" > 0 AND new."+Member.Cols.DEVICESIGNUP+" = 't' " 
				//how do I tell the difference between a member added from the device, and one that's just been downloaded?
				+" BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Member.Cols._ID+", "+TableIndex.Values.Member.getKey()+");"
				+" END; ";
				
		public static final String SQL22 ="CREATE TRIGGER "+Member.Triggers.ON_UPDATE+" AFTER UPDATE ON "+Member.NAME
				+" FOR EACH ROW WHEN new."+Member.Cols.MID+" > 0 "
						+ "AND old."+Member.Cols.CARDNO+" != new."+Member.Cols.CARDNO
				//Add OR statements here to allow for other changes.
				+" BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUpdates.NAME
					+" ("+PendingUpdates.Cols.ROWID+", "+PendingUpdates.Cols.TABLEID+")"
					+" VALUES (new."+Member.Cols._ID+", "+TableIndex.Values.Member.getKey()+");"
				+"END;";
		
		public static final String SQL23= "CREATE TRIGGER "+Member.Triggers.ON_UPDATE_MID+" AFTER UPDATE ON "+Member.NAME
				+" FOR EACH ROW WHEN old."+Member.Cols.MID+" <= 0 AND new."+Member.Cols.MID+" > 0 "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Member.Cols._ID+", "+TableIndex.Values.Member.getKey()+");"
				+"END;";
		
	//DO MORE TRIGGERS
		public static final String SQL24 ="ALTER TABLE "+Booking.NAME+" RENAME TO tmp_"+Booking.NAME+";";
		
		public static final String SQL25="CREATE TABLE "+Booking.NAME+" ("+Booking.Cols.ID+" INTEGER PRIMARY KEY, "
				+Booking.Cols.FNAME+" TEXT, "+Booking.Cols.SNAME+" TEXT, "
				+Booking.Cols.BOOKING+" TEXT, "
				+Booking.Cols.STIMEID+" INTEGER, "+Booking.Cols.ETIMEID+" INTEGER, "
				+Booking.Cols.BID+" TEXT, "+Booking.Cols.BOOKINGTYPE+" INTEGER, "
				+Booking.Cols.ETIME+" TEXT, "+Booking.Cols.NOTES+" TEXT, "
				+Booking.Cols.RESULT+" INTEGER, "+Booking.Cols.MID+" INTEGER, "
				+Booking.Cols.LASTUPDATE+" NUMERIC, "+Booking.Cols.STIME+" TEXT, "
				+Booking.Cols.MSID+" INTEGER, "+Booking.Cols.CHECKIN+" NUMERIC, " //timestamp ?
				+Booking.Cols.RID+" INTEGER, "+Booking.Cols.ARRIVAL+" INTEGER, "
				+Booking.Cols.OFFSET+" TEXT, "
				+Booking.Cols.CLASSID+" INTEGER DEFAULT 0, "+Booking.Cols.PARENTID+" INTEGER DEFAULT 0, "
				+Booking.Cols.DEVICESIGNUP+" TEXT DEFAULT 'f', "
				+"FOREIGN KEY ("+Booking.Cols.STIMEID
				+") REFERENCES "+ContentDescriptor.Time.NAME+" ("+ContentDescriptor.Time.Cols.ID+") "
				+");";
		
		public static final String SQL26="INSERT INTO "+Booking.NAME+" SELECT *, 'f' FROM tmp_"+Booking.NAME+";";
		
		public static final String SQL27="DROP TABLE tmp_"+Booking.NAME+";";
		
		public static final String SQL28= "CREATE TRIGGER "+Booking.Triggers.ON_INSERT+" AFTER INSERT ON "+Booking.NAME
				+" FOR EACH ROW WHEN new."+Booking.Cols.BID+" > 0 AND new."+Booking.Cols.DEVICESIGNUP+"= 't' "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Booking.Cols.ID+", "+TableIndex.Values.Booking.getKey()+");"
				+" END; ";
		
		public static final String SQL29 ="CREATE TRIGGER "+Booking.Triggers.ON_UPDATE+" AFTER UPDATE ON "+Booking.NAME
				+" FOR EACH ROW WHEN new."+Booking.Cols.BID+" > 0 "
						//do we need to check for actual changes, 
						//or just assume that the update clause changed relevant info?
						//AND new.<column> != old.<column> ?
				+" BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUpdates.NAME
					+" ("+PendingUpdates.Cols.ROWID+", "+PendingUpdates.Cols.TABLEID+")"
					+" VALUES (new."+Booking.Cols.ID+", "+TableIndex.Values.Booking.getKey()+");"
				+"END;";
		
		public static final String SQL30= "CREATE TRIGGER "+Booking.Triggers.ON_UPDATE_BID+" AFTER UPDATE ON "+Booking.NAME
				+" FOR EACH ROW WHEN old."+Booking.Cols.BID+" <= 0 AND new."+Booking.Cols.BID+" > 0 "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Booking.Cols.ID+", "+TableIndex.Values.Booking.getKey()+");"
				+"END;";
		
		public static final String SQL31= "ALTER TABLE "+Class.NAME+" RENAME TO tmp_"+Class.NAME+";";
		
		public static final String SQL32="CREATE TABLE "+Class.NAME+" ("+Class.Cols._ID+" INTEGER PRIMARY KEY, "
				+Class.Cols.CID+" INTEGER, "+Class.Cols.NAME+" TEXT, "
				+Class.Cols.SDATE+" INTEGER, "+Class.Cols.FREQ+" TEXT, "
				+Class.Cols.STIME+" TEXT, "+Class.Cols.ETIME+" TEXT, "
				+Class.Cols.MAX_ST+" INTEGER, "+Class.Cols.RID+" INTEGER, "
				+Class.Cols.LASTUPDATE+" NUMERIC, "+Class.Cols.ONLINE+" INTEGER DEFAULT 1,"
				+Class.Cols.DESC+" TEXT, "+Class.Cols.DEVICESIGNUP+" TEXT DEFAULT 'f' "
				+");";
		public static final String SQL33="INSERT INTO "+Class.NAME+" SELECT *, 'f' FROM tmp_"+Class.NAME+";";
		public static final String SQL34="DROP TABLE tmp_"+Class.NAME+";";
		
		public static final String SQL35= "CREATE TRIGGER "+Class.Triggers.ON_INSERT+" AFTER INSERT ON "+Class.NAME
				+" FOR EACH ROW WHEN new."+Class.Cols.DEVICESIGNUP+" = 't' "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Class.Cols._ID+", "+TableIndex.Values.Class.getKey()+");"
				+" END; ";
		
		public static final String SQL36= "ALTER TABLE "+Membership.NAME+" RENAME TO tmp_"+Membership.NAME+";";
		public static final String SQL37="CREATE TABLE "+Membership.NAME+
				" ("+Membership.Cols._ID+" INTEGER PRIMARY KEY, "
				+Membership.Cols.MID+" INTEGER NOT NULL, "+Membership.Cols.MSID+" INTEGER, "
				+Membership.Cols.CARDNO+" TEXT, "+Membership.Cols.DENY+" INTEGER, "
				+Membership.Cols.PNAME+" TEXT, "+Membership.Cols.MSSTART+" TEXT, "
				+Membership.Cols.EXPIRERY+" TEXT, "+Membership.Cols.VISITS+" TEXT, "
				+Membership.Cols.LASTUPDATE+" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+Membership.Cols.PRIMARYMS+" INTEGER, "+Membership.Cols.PID+" INTEGER, "
				+Membership.Cols.PGID+" INTEGER, "+Membership.Cols.PRICE+" TEXT, "
				+Membership.Cols.SIGNUP+" TEXT, "+Membership.Cols.CREATION+" TEXT, "
				+Membership.Cols.DEVICESIGNUP+" TEXT DEFAULT 'f' "
				+");";
		public static final String SQL38="INSERT INTO "+Membership.NAME+" SELECT *, 'f' FROM tmp_"+Membership.NAME+";";
		public static final String SQL39="DROP TABLE tmp_"+Membership.NAME+";";
		
		public static final String SQL40= "CREATE TRIGGER "+Membership.Triggers.ON_INSERT+
				" AFTER INSERT ON "+Membership.NAME
				+" FOR EACH ROW WHEN new."+Membership.Cols.MSID+" > 0 AND new."+Membership.Cols.DEVICESIGNUP+"= 't' "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Membership.Cols._ID+", "+TableIndex.Values.Membership.getKey()+");"
				+" END; ";
		
		public static final String SQL41= "CREATE TRIGGER "+Membership.Triggers.ON_UPDATE_MSID+
				" AFTER UPDATE ON "+Membership.NAME
				+" FOR EACH ROW WHEN old."+Membership.Cols.MSID+" <= 0 AND new."+Membership.Cols.MSID+" > 0 "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Membership.Cols._ID+", "+TableIndex.Values.Membership.getKey()+");"
				+"END;";
		
		public static final String SQL42= "CREATE TRIGGER "+Image.Triggers.ON_INSERT+
				" AFTER INSERT ON "+Image.NAME
				+" FOR EACH ROW WHEN new."+Image.Cols.IID +" IS NULL " 
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Image.Cols.ID+", "+TableIndex.Values.Image.getKey()+");"
				+" END; ";
		
		public static final String SQL43="ALTER TABLE "+MembershipSuspend.Old.NAME+" RENAME TO tmp_"+MembershipSuspend.NAME+";";
		
		public static final String SQL44="CREATE TABLE "+MembershipSuspend.NAME+" ("+MembershipSuspend.Cols._ID+" INTEGER PRIMARY KEY, "
				+MembershipSuspend.Cols.SID+" INTEGER, "+MembershipSuspend.Cols.MID+" INTEGER DEFAULT 0, "
				+MembershipSuspend.Cols.STARTDATE+" INTEGER, "+MembershipSuspend.Cols.REASON+" TEXT, "
				+MembershipSuspend.Cols.LENGTH+" INTEGER, "+MembershipSuspend.Cols.ENDDATE+" TEXT, "
				+MembershipSuspend.Cols.FREEZE+" INTEGER, "+MembershipSuspend.Cols.DEVICESIGNUP+" TEXT DEFAULT 'f' "
				+");";
		public static final String SQL45="INSERT INTO "+MembershipSuspend.NAME+" SELECT *, 'f' FROM tmp_"+MembershipSuspend.NAME+";";
		public static final String SQL46="DROP TABLE tmp_"+MembershipSuspend.NAME+";";
		
		public static final String SQL47= "CREATE TRIGGER "+MembershipSuspend.Triggers.ON_INSERT+
				" AFTER INSERT ON "+MembershipSuspend.NAME
				+" FOR EACH ROW WHEN new."+MembershipSuspend.Cols.SID+" > 0 AND new."+MembershipSuspend.Cols.DEVICESIGNUP+"= 't' "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+MembershipSuspend.Cols._ID+", "+TableIndex.Values.MembershipSuspend.getKey()+");"
				+" END; ";
		
		public static final String SQL48= "CREATE TRIGGER "+MembershipSuspend.Triggers.ON_UPDATE_SID+
				" AFTER UPDATE ON "+MembershipSuspend.NAME
				+" FOR EACH ROW WHEN old."+MembershipSuspend.Cols.SID+" <= 0 AND new."+MembershipSuspend.Cols.SID+" > 0 "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+MembershipSuspend.Cols._ID+", "+TableIndex.Values.MembershipSuspend.getKey()+");"
				+"END;";
		//MEMBER NOTES.
		public static final String SQL49="ALTER TABLE "+MemberNotes.NAME+" RENAME TO tmp_"+MemberNotes.NAME+";";
		public static final String SQL50="CREATE TABLE "+MemberNotes.NAME
				+" ("+MemberNotes.Cols._ID+" INTEGER PRIMARY KEY, "
				+MemberNotes.Cols.MNID+" INTEGER NOT NULL DEFAULT 0, "+MemberNotes.Cols.MID+" INTEGER NOT NULL DEFAULT 0, "
				+MemberNotes.Cols.NOTES+" TEXT, "+MemberNotes.Cols.OCCURRED+" TEXT, "
				+MemberNotes.Cols.DEVICESIGNUP+" TEXT DEFAULT 'f' "
				+" );";
		public static final String SQL51="INSERT INTO "+MemberNotes.NAME+" SELECT *, 'f' FROM tmp_"+MemberNotes.NAME+";";
		public static final String SQL52="DROP TABLE tmp_"+MemberNotes.NAME+";";
		
		public static final String SQL53= "CREATE TRIGGER "+MemberNotes.Triggers.ON_INSERT+
				" AFTER INSERT ON "+MemberNotes.NAME
				+" FOR EACH ROW WHEN new."+MemberNotes.Cols.MNID+" > 0 AND new."+MemberNotes.Cols.DEVICESIGNUP+"= 't' "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+MemberNotes.Cols._ID+", "+TableIndex.Values.MemberNotes.getKey()+");"
				+" END; ";
		
		public static final String SQL54= "CREATE TRIGGER "+MemberNotes.Triggers.ON_UPDATE_MNID+
				" AFTER UPDATE ON "+MemberNotes.NAME
				+" FOR EACH ROW WHEN old."+MemberNotes.Cols.MNID+" <= 0 AND new."+MemberNotes.Cols.MNID+" > 0 "
				+"BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+MemberNotes.Cols._ID+", "+TableIndex.Values.MemberNotes.getKey()+");"
				+"END;";

		//do YMCA specific stuff. append ParentName to Member Table,
		//Add roll-call & rollItem tables.
		public static final String SQL55 = "ALTER TABLE "+Member.NAME+" ADD COLUMN "
				+Member.Cols.PARENTNAME+" TEXT;";
		
		//CREATE TABLES
		public static final String SQL56="CREATE TABLE "+RollCall.NAME+" ("+RollCall.Cols._ID+" INTEGER PRIMARY KEY NOT NULL, "
				+RollCall.Cols.DATETIME+" TIMESTAMP, "+RollCall.Cols.NAME+" TEXT "
				+");";
		
		public static final String SQL57="CREATE TABLE "+RollItem.NAME+" ("+RollItem.Cols._ID+" INTEGER PRIMARY KEY NOT NULL, "
				+RollItem.Cols.ROLLID+" INTEGER, "+RollItem.Cols.MEMBERID+" INTEGER, "
				+RollItem.Cols.ATTENDED+" TEXT DEFAULT 'f' "
				+");";
		
		public static void patchNinetyThree(SQLiteDatabase db) {
			db.beginTransaction();
			try {
				Log.w(HornetDatabase.class.getName(), "SQL-Patch:93 \n"+UpdateDatabase.NinetyThree.SQL1);
				db.execSQL(UpdateDatabase.NinetyThree.SQL1);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL2);
				db.execSQL(UpdateDatabase.NinetyThree.SQL2);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL3);
				db.execSQL(UpdateDatabase.NinetyThree.SQL3);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL4);
				db.execSQL(UpdateDatabase.NinetyThree.SQL4);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL5);
				db.execSQL(UpdateDatabase.NinetyThree.SQL5);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL6);
				db.execSQL(UpdateDatabase.NinetyThree.SQL6);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL7);
				db.execSQL(UpdateDatabase.NinetyThree.SQL7);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL8);
				db.execSQL(UpdateDatabase.NinetyThree.SQL8);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL9);
				db.execSQL(UpdateDatabase.NinetyThree.SQL9);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL10);
				db.execSQL(UpdateDatabase.NinetyThree.SQL10);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL11);
				db.execSQL(UpdateDatabase.NinetyThree.SQL11);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL12);
				db.execSQL(UpdateDatabase.NinetyThree.SQL12);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL13);
				db.execSQL(UpdateDatabase.NinetyThree.SQL13);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL14);
				db.execSQL(UpdateDatabase.NinetyThree.SQL14);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL15);
				db.execSQL(UpdateDatabase.NinetyThree.SQL15);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL16);
				db.execSQL(UpdateDatabase.NinetyThree.SQL16);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL17);
				db.execSQL(UpdateDatabase.NinetyThree.SQL17);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL18);
				db.execSQL(UpdateDatabase.NinetyThree.SQL18);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL19);
				db.execSQL(UpdateDatabase.NinetyThree.SQL19);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL20);
				db.execSQL(UpdateDatabase.NinetyThree.SQL20);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL21);
				db.execSQL(UpdateDatabase.NinetyThree.SQL21);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL22);
				db.execSQL(UpdateDatabase.NinetyThree.SQL22);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL23);
				db.execSQL(UpdateDatabase.NinetyThree.SQL23);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL24);
				db.execSQL(UpdateDatabase.NinetyThree.SQL24);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL25);
				db.execSQL(UpdateDatabase.NinetyThree.SQL25);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL26);
				db.execSQL(UpdateDatabase.NinetyThree.SQL26);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL27);
				db.execSQL(UpdateDatabase.NinetyThree.SQL27);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL28);
				db.execSQL(UpdateDatabase.NinetyThree.SQL28);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL29);
				db.execSQL(UpdateDatabase.NinetyThree.SQL29);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL30);
				db.execSQL(UpdateDatabase.NinetyThree.SQL30);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL31);
				db.execSQL(UpdateDatabase.NinetyThree.SQL31);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL32);
				db.execSQL(UpdateDatabase.NinetyThree.SQL32);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL33);
				db.execSQL(UpdateDatabase.NinetyThree.SQL33);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL34);
				db.execSQL(UpdateDatabase.NinetyThree.SQL34);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL35);
				db.execSQL(UpdateDatabase.NinetyThree.SQL35);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL36);
				db.execSQL(UpdateDatabase.NinetyThree.SQL36);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL37);
				db.execSQL(UpdateDatabase.NinetyThree.SQL37);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL38);
				db.execSQL(UpdateDatabase.NinetyThree.SQL38);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL39);
				db.execSQL(UpdateDatabase.NinetyThree.SQL39);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL40);
				db.execSQL(UpdateDatabase.NinetyThree.SQL40);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL41);
				db.execSQL(UpdateDatabase.NinetyThree.SQL41);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL42);
				db.execSQL(UpdateDatabase.NinetyThree.SQL42);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL43);
				db.execSQL(UpdateDatabase.NinetyThree.SQL43);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL44);
				db.execSQL(UpdateDatabase.NinetyThree.SQL44);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL45);
				db.execSQL(UpdateDatabase.NinetyThree.SQL45);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL46);
				db.execSQL(UpdateDatabase.NinetyThree.SQL46);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL47);
				db.execSQL(UpdateDatabase.NinetyThree.SQL47);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL48);
				db.execSQL(UpdateDatabase.NinetyThree.SQL48);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL49);
				db.execSQL(UpdateDatabase.NinetyThree.SQL49);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL50);
				db.execSQL(UpdateDatabase.NinetyThree.SQL50);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL51);
				db.execSQL(UpdateDatabase.NinetyThree.SQL51);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL52);
				db.execSQL(UpdateDatabase.NinetyThree.SQL52);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL53);
				db.execSQL(UpdateDatabase.NinetyThree.SQL53);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL54);
				db.execSQL(UpdateDatabase.NinetyThree.SQL54);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL55);
				db.execSQL(UpdateDatabase.NinetyThree.SQL55);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL56);
				db.execSQL(UpdateDatabase.NinetyThree.SQL56);
				Log.w(HornetDatabase.class.getName(), "\n"+UpdateDatabase.NinetyThree.SQL57);
				db.execSQL(UpdateDatabase.NinetyThree.SQL57);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}	
}
