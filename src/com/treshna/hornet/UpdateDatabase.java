package com.treshna.hornet;

import com.treshna.hornet.ContentDescriptor.Booking;
import com.treshna.hornet.ContentDescriptor.FreeIds;
import com.treshna.hornet.ContentDescriptor.Image;
import com.treshna.hornet.ContentDescriptor.Member;
import com.treshna.hornet.ContentDescriptor.MemberNotes;
import com.treshna.hornet.ContentDescriptor.Membership;
import com.treshna.hornet.ContentDescriptor.MembershipSuspend;
import com.treshna.hornet.ContentDescriptor.PendingDeletes;
import com.treshna.hornet.ContentDescriptor.PendingUpdates;
import com.treshna.hornet.ContentDescriptor.PendingUploads;
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
						+" FROM "+MembershipSuspend.NAME+" WHERE "+MembershipSuspend.Cols.MID+" = 0;";
		public static final String SQL11 ="DELETE FROM "+MembershipSuspend.NAME+" WHERE "+MembershipSuspend.Cols.MID+" = 0;";
				
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
				+Member.Cols.MEDICATIONBYSTAFF+" INTEGER, "+Member.Cols.EMERGENCYRELATIONSHIP+" TEXT, "
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
				
	}
}
