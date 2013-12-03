package com.treshna.hornet;

import com.treshna.hornet.ContentDescriptor.Image;
import com.treshna.hornet.ContentDescriptor.Member;
import com.treshna.hornet.ContentDescriptor.PendingUpdates;
import com.treshna.hornet.ContentDescriptor.TableIndex;
import com.treshna.hornet.ContentDescriptor.Visitor;

/**
 * Ideally these want to be seperate sql files in the assets folder/sub-dir.
 * However, I need to reference the column/table statics that are in the ContentDescriptor Class.
 * 
 * How do I solve?
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
				
				+ ""; //ADD TRIGGERS FOR ON UPDATES (set pending UPLOADS/UPDATES).
	}
}
