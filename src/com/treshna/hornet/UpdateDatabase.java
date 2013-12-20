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
		public static final String SQL = 
				"BEGIN TRANSACTION;"
				+ "ALTER TABLE "+Visitor.NAME+" RENAME TO tmp_"+Visitor.NAME+";"
				
				+" CREATE TABLE "+Visitor.NAME+" ("+Visitor.Cols.ID+" INTEGER PRIMARY KEY, "
						+Visitor.Cols.MID+" INTEGER, "+Visitor.Cols.DATETIME+" TIMESTAMP, "
						+Visitor.Cols.DATE+" TEXT, "+Visitor.Cols.TIME+" TEXT, "
						+Visitor.Cols.DENY+" TEXT, "+Visitor.Cols.CARDNO+" TEXT, "
						+Visitor.Cols.DOORNAME+" TEXT, "+Visitor.Cols.MSID+" INTEGER, "
						+Visitor.Cols.LASTUPDATE+" NUMERIC "
						+");"
						//Visitors indexes.
				+" CREATE INDEX "+Visitor.Indexs.MEMBER_ID+" ON "+Visitor.NAME+" ( "
						+Visitor.Cols.MID+" );"
				+" CREATE INDEX "+Visitor.Indexs.MS_ID+" ON "+Visitor.NAME+" ( "
						+Visitor.Cols.MSID+" );"
				+" CREATE INDEX "+Visitor.Indexs.DATE_TIME+" ON "+Visitor.NAME+" ( "
						+Visitor.Cols.DATETIME+" );"
				
				+" INSERT INTO "+Visitor.NAME+"("+Visitor.Cols.ID+", "+Visitor.Cols.MID+", "
					+Visitor.Cols.DATETIME+", "+Visitor.Cols.DATE+", "+Visitor.Cols.TIME+", "+Visitor.Cols.DENY+", "
					+Visitor.Cols.CARDNO+", "+Visitor.Cols.DOORNAME+", "+Visitor.Cols.MSID+", "+Visitor.Cols.LASTUPDATE+")"
				+" SELECT "+Visitor.Cols.ID+", "+Visitor.Cols.MID+", "+Visitor.Cols.DATETIME+", "
					+Visitor.Cols.DATE+", "+Visitor.Cols.TIME+", "+Visitor.Cols.DENY+", "+Visitor.Cols.CARDNO+", "
					+Visitor.Cols.DOORNAME+", "+Visitor.Cols.MSID+", "+Visitor.Cols.LASTUPDATE
				+" FROM tmp_"+Visitor.NAME+";"
				+"DROP TABLE tmp_"+Visitor.NAME+";"
				+"COMMIT;"
				
				//create FreeIds table.
				//move any free ids into the table then delete them.
				//all future/current code needs to use this table.
				+"CREATE TABLE "+FreeIds.NAME+" ("+FreeIds.Cols._ID+" INTEGER PRIMARY KEY, "
						+FreeIds.Cols.ROWID+" INTEGER, "+FreeIds.Cols.TABLEID+" INTEGER );"
				
				+"INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+Member.Cols.MID+", "+TableIndex.Values.Member.getKey()
						+" FROM "+Member.NAME+" WHERE "+Member.Cols.STATUS+" =  -1 ;"
				+"DELETE FROM "+Member.NAME+" WHERE "+Member.Cols.STATUS+" =  -1;"
				
				+"INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+Booking.Cols.BID+", "+TableIndex.Values.Booking.getKey()
						+" FROM "+Booking.NAME+" WHERE "+Booking.Cols.LASTUPDATE+" = 0;"
				+"DELETE FROM "+Booking.NAME+" WHERE "+Booking.Cols.LASTUPDATE+" = 0;"
				
				+"INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+MembershipSuspend.Cols.SID+", "+TableIndex.Values.MembershipSuspend.getKey()
						+" FROM "+MembershipSuspend.NAME+" WHERE "+MembershipSuspend.Cols.MID+" = 0;"
				+"DELETE FROM "+MembershipSuspend.NAME+" WHERE "+MembershipSuspend.Cols.MID+" = 0;"
				
				+"INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+Membership.Cols.MSID+", "+TableIndex.Values.Membership.getKey()
						+" FROM "+Membership.NAME+" WHERE "+Membership.Cols.MID+" = 0;"
				+"DELETE FROM "+Membership.NAME+" WHERE "+Membership.Cols.MID+" = 0;"
				
				+"INSERT INTO "+FreeIds.NAME+" ("+FreeIds.Cols.ROWID+", "+FreeIds.Cols.TABLEID+") "
						+ "SELECT "+MemberNotes.Cols.MNID+", "+TableIndex.Values.MemberNotes.getKey()
						+" FROM "+MemberNotes.NAME+" WHERE "+MemberNotes.Cols.MID+" = 0;"
				+"DELETE FROM "+MemberNotes.NAME+" WHERE "+MemberNotes.Cols.MID+" = 0;"
				
				
				
				//move more triggers here.
				//TODO:ADD TRIGGERS FOR ON UPDATES/INSERTS that set pending UPLOADS/UPDATES table.
				+ "CREATE TRIGGER "+Member.Triggers.ON_INSERT+" AFTER INSERT ON "+Member.NAME
				+" FOR EACH ROW WHEN new."+Member.Cols.MID+" > 0"
				+" BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUploads.NAME
					+ " ("+PendingUploads.Cols.ROWID+", "+PendingUploads.Cols.TABLEID+")"
					+ " VALUES (new."+Member.Cols._ID+", "+TableIndex.Values.Member.getKey()+");"
				+" END; "
				
				+"CREATE TRIGGER "+Member.Triggers.ON_UPDATE+" AFTER UPDATE ON "+Member.NAME
				+" FOR EACH ROW"
				+" BEGIN "
					+"INSERT OR REPLACE INTO "+PendingUpdates.NAME
					+" ("+PendingUpdates.Cols.ROWID+", "+PendingUpdates.Cols.TABLEID+")"
					+" VALUES (new."+Member.Cols._ID+", "+TableIndex.Values.Member.getKey()+");"
				+"END;"
					
				+ "CREATE TABLE "+PendingDeletes.NAME+" ("+PendingDeletes.Cols._ID+" INTEGER PRIMARY KEY, "
						+PendingDeletes.Cols.ROWID+" INTEGER, "+PendingDeletes.Cols.TABLEID+" INTEGER );"; 
	}
}
