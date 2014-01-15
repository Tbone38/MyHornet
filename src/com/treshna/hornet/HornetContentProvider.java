package com.treshna.hornet;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/*This class is an android database wrapper used by the Content provider.
 * All queries/inserts/updates/deletes should go through it.
 * (getType is only used when exporting the content Provider?)
 */
public class HornetContentProvider extends ContentProvider {
	private HornetDatabase hornetDb;
	private Context ctx;
	@Override
	public boolean onCreate() {
        ctx = getContext();
        hornetDb = new HornetDatabase(ctx);
        return true;
    }
		//getType isn't actually used?
	 @Override
	 public String getType(Uri uri) {
		 final int match = ContentDescriptor.URI_MATCHER.match(uri);
	     switch(match){ //the switch for handling magic numbers
	     case ContentDescriptor.Member.PATH_TOKEN:
	         return ContentDescriptor.Member.CONTENT_TYPE_DIR;
	     case ContentDescriptor.Member.PATH_FOR_ID_TOKEN:
	    	 return ContentDescriptor.Member.CONTENT_ITEM_TYPE;
	     case ContentDescriptor.Image.PATH_TOKEN:
	    	 return ContentDescriptor.Image.CONTENT_TYPE_DIR;
	     case ContentDescriptor.Image.PATH_FOR_ID_TOKEN:
	    	 return ContentDescriptor.Image.CONTENT_ITEM_TYPE;
	     case ContentDescriptor.Visitor.PATH_TOKEN:
	    	 return ContentDescriptor.Image.CONTENT_TYPE_DIR;
	     case ContentDescriptor.Visitor.PATH_FOR_ID_TOKEN:
	    	 return ContentDescriptor.Image.CONTENT_ITEM_TYPE;
	    	 //bookings
	     case ContentDescriptor.Booking.PATH_TOKEN:
	    	 return ContentDescriptor.Booking.CONTENT_TYPE_DIR;
	     case ContentDescriptor.Booking.PATH_FOR_ID_TOKEN:
	    	 return ContentDescriptor.Booking.CONTENT_ITEM_TYPE;
	     case ContentDescriptor.Resource.PATH_TOKEN:
	    	 return ContentDescriptor.Resource.CONTENT_TYPE_DIR;
	     case ContentDescriptor.Resource.PATH_FOR_ID_TOKEN:
	    	 return ContentDescriptor.Resource.CONTENT_ITEM_TYPE;
	     case ContentDescriptor.Company.PATH_TOKEN:
	    	 return ContentDescriptor.Company.CONTENT_TYPE_DIR;
	     case ContentDescriptor.Company.PATH_FOR_ID_TOKEN:
	    	 return ContentDescriptor.Company.CONTENT_ITEM_TYPE;
	     default:
	         throw new UnsupportedOperationException ("URI " + uri + " is not supported.");
	     }
	 }
	 
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = hornetDb.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        switch(token){
        case ContentDescriptor.Booking.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Booking.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Company.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Company.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Swipe.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Swipe.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Time.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Time.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.ResultStatus.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.ResultStatus.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Bookingtype.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Bookingtype.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Member.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Member.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.OpenTime.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.OpenTime.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Date.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Date.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Class.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Class.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Membership.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Membership.NAME,selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.BookingTime.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.BookingTime.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.BookingTime.PATH_FOR_JOIN_TOKEN:{ //this may want to be two seperate deletes, with the same where clause.
        	int rows = db.delete(ContentDescriptor.BookingTime.NAME+" bt LEFT JOIN "+ContentDescriptor.Booking.NAME
        			+" b ON (bt."+ContentDescriptor.BookingTime.Cols.BID+" = "+ContentDescriptor.Booking.Cols.BID+")",
        			selection, selectionArgs); 
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.TableIndex.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.TableIndex.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.PendingUploads.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.PendingUploads.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.PendingDownloads.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.PendingDownloads.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.MembershipSuspend.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.MembershipSuspend.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.IdCard.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.IdCard.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.PaymentMethod.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.PaymentMethod.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Programme.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Programme.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.Door.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.Door.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.MemberNotes.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.MemberNotes.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.MemberBalance.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.MemberBalance.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.PendingUpdates.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.PendingUpdates.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.FreeIds.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.FreeIds.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.PendingDeletes.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.PendingDeletes.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.RollCall.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.RollCall.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        case ContentDescriptor.RollItem.PATH_TOKEN:{
        	int rows = db.delete(ContentDescriptor.RollItem.NAME, selection, selectionArgs);
        	getContext().getContentResolver().notifyChange(uri, null);
        	return rows;
        }
        
        case ContentDescriptor.TOKEN_DROPTABLE:{ //special case, drops tables/deletes database.
        	FileHandler fh = new FileHandler(ctx);
        	fh.clearDirectory();
        	hornetDb.dropTables(db);
        	hornetDb.close();
        	
        	db = hornetDb.getWritableDatabase();
        	hornetDb.onCreate(db);
        	return 0;
        }
        default:
        	throw new UnsupportedOperationException("URI: " + uri + " not supported.");
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = hornetDb.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        switch(token){
           	case ContentDescriptor.Member.PATH_TOKEN:{
                long id = db.insert(ContentDescriptor.Member.NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentDescriptor.Member.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Image.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Image.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Image.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Visitor.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Visitor.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Image.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }          
            case ContentDescriptor.Booking.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Booking.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Booking.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            
            case ContentDescriptor.Resource.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Resource.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Booking.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            
            case ContentDescriptor.Company.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Company.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Company.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            
            case ContentDescriptor.Swipe.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Swipe.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Swipe.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            
            case ContentDescriptor.Time.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Time.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Time.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            
            case ContentDescriptor.Bookingtype.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Bookingtype.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Bookingtype.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.ResultStatus.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.ResultStatus.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.ResultStatus.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.OpenTime.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.OpenTime.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.OpenTime.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Date.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Date.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Date.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Class.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Class.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Class.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Membership.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Membership.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Membership.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.BookingTime.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.BookingTime.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.BookingTime.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.TableIndex.PATH_TOKEN:{ //shouldn't be used, tableindex isn't added via the contentProvider
            	long id = db.insert(ContentDescriptor.TableIndex.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.TableIndex.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.PendingUploads.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.PendingUploads.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.PendingUploads.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.PendingDownloads.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.PendingDownloads.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.PendingDownloads.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.MembershipSuspend.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.MembershipSuspend.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.MembershipSuspend.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.IdCard.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.IdCard.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.IdCard.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.PaymentMethod.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.PaymentMethod.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.PaymentMethod.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Programme.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Programme.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Programme.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.Door.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.Door.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.Door.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.MemberNotes.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.MemberNotes.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.MemberNotes.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.MemberBalance.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.MemberBalance.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.MemberBalance.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.PendingUpdates.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.PendingUpdates.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.PendingUpdates.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.FreeIds.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.FreeIds.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.FreeIds.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.PendingDeletes.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.PendingDeletes.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.PendingDeletes.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.RollCall.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.RollCall.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.RollCall.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            case ContentDescriptor.RollItem.PATH_TOKEN:{
            	long id = db.insert(ContentDescriptor.RollItem.NAME, null, values);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return ContentDescriptor.RollItem.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
        }
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (selection == null) {
			selection = "";
		}
				
		SQLiteDatabase db = hornetDb.getReadableDatabase();		
        final int match = ContentDescriptor.URI_MATCHER.match(uri);
        switch(match){
            // retrieve list
            case ContentDescriptor.Member.PATH_TOKEN:{
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(ContentDescriptor.Member.NAME+" m "
                		+"LEFT OUTER JOIN "+ContentDescriptor.Membership.NAME+" ms "
                		+"ON (m."+ContentDescriptor.Member.Cols.MID+" = ms."+ContentDescriptor.Membership.Cols.MID
                		+")");
                if (selection.isEmpty()) {
                	selection = "("+ContentDescriptor.Member.Cols.STATUS+" >= 0 OR "+ContentDescriptor.Member.Cols.STATUS
                			+" IS NULL)";
                } else {
                	selection = selection+" AND ("+ContentDescriptor.Member.Cols.STATUS+" >= 0 OR "+ContentDescriptor.Member.Cols.STATUS
                			+" IS NULL)";
                }
                return builder.query(db, projection, selection, selectionArgs, "m."+ContentDescriptor.Member.Cols.MID, null, sortOrder);
            }
 
            case ContentDescriptor.Member.TOKEN_FIND:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Member.NAME+" m "
            			+"LEFT JOIN "+ContentDescriptor.MemberBalance.NAME+" mb"
            			+" ON (m."+ContentDescriptor.Member.Cols.MID+" = mb."+ContentDescriptor.MemberBalance.Cols.MID
            			+") "
            			+ "LEFT JOIN "+ContentDescriptor.Membership.NAME+" ms "
            			+ "ON (ms."+ContentDescriptor.Membership.Cols.MID+" = m."+ContentDescriptor.Member.Cols.MID
            			+ ") "
            			);
            	if (selection.isEmpty()) {
            		selection = "("+ContentDescriptor.Member.Cols.STATUS+" >= 0 OR "+ContentDescriptor.Member.Cols.STATUS
                			+" IS NULL)";
            	} else {
            		selection = selection+" AND ("+ContentDescriptor.Member.Cols.STATUS+" >= 0 OR "+ContentDescriptor.Member.Cols.STATUS
                			+" IS NULL)";
            	}
            	return builder.query(db, projection, selection, selectionArgs, "m."+ContentDescriptor.Member.Cols.MID, null, sortOrder);
            }
            case ContentDescriptor.Member.PATH_FOR_ID_TOKEN:{
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                
                builder.setTables(ContentDescriptor.Member.NAME);
                selection = selection+" _ID = " + uri.getLastPathSegment();
                return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Image.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Image.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Image.PATH_FOR_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Image.NAME);
            	 selection = selection+" _ID = " + uri.getLastPathSegment();
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Visitor.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Visitor.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Visitor.PATH_FOR_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Visitor.NAME);
            	 selection = selection+" _ID = " + uri.getLastPathSegment();
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Visitor.PATH_FOR_JOIN_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Visitor.NAME
            			+" v LEFT JOIN "+ContentDescriptor.Member.NAME
            			+ " m ON (v."+ContentDescriptor.Visitor.Cols.MID
            			+" = m."+ContentDescriptor.Member.Cols.MID+")"
            			+" LEFT JOIN "+ContentDescriptor.Membership.NAME
            			+" ms ON (v."+ContentDescriptor.Visitor.Cols.MSID+" = "
            			+"ms."+ContentDescriptor.Membership.Cols.MSID+")");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Image.PATH_FOR_JOIN_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Member.NAME
            			+" LEFT JOIN "+ContentDescriptor.Image.NAME
            			+" ON ("+ContentDescriptor.Member.NAME+"."+ContentDescriptor.Member.Cols.MID
            			+" = "+ContentDescriptor.Image.NAME+"."+ContentDescriptor.Image.Cols.MID+") "
            			+" LEFT JOIN "+ContentDescriptor.Membership.NAME
            			+" ON ("+ContentDescriptor.Member.NAME+"."+ContentDescriptor.Member.Cols.MID+" = "
            			+ContentDescriptor.Membership.NAME+"."+ContentDescriptor.Membership.Cols.MID+")");
            	selection = selection+" "+ContentDescriptor.Member.NAME+"."+ContentDescriptor.Member.Cols.MID
            			+" = " + uri.getLastPathSegment();
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);            	
            }
            case ContentDescriptor.Booking.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Booking.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Booking.PATH_FOR_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Booking.NAME);
            	selection = selection +" "+ ContentDescriptor.Booking.Cols.ID+" = "+uri.getLastPathSegment();
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Time.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Time.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Time.PATH_FOR_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Time.NAME);
            	selection = selection +" "+ContentDescriptor.Time.Cols.ID+" = "+uri.getLastPathSegment();
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }/********/
            //the below query is probably incredibly inefficient.
            case ContentDescriptor.Time.PATH_FOR_JOIN_TOKEN:{ 
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Time.NAME
            			+" t LEFT OUTER JOIN "+ContentDescriptor.BookingTime.NAME
            			+" bt ON (t."+ContentDescriptor.Time.Cols.ID
            			+" = bt."+ContentDescriptor.BookingTime.Cols.TIMEID+")" //STIMEID
            			+" AND "+selection
            			+" LEFT OUTER JOIN "+ContentDescriptor.Booking.NAME
            			+" b ON (bt."+ContentDescriptor.BookingTime.Cols.BID
            			+" = b."+ContentDescriptor.Booking.Cols.BID+")"
            			+" AND "+selectionArgs[1]//+selectionArgs[2]
            			);
            			selection = "t."+ContentDescriptor.Time.Cols.ID+" >= (" 
            					+"SELECT ot."+ContentDescriptor.OpenTime.Cols.OPENID+" FROM "
            					+ContentDescriptor.OpenTime.NAME+" ot LEFT JOIN "
            					+ContentDescriptor.Date.NAME+" d ON (d."+ContentDescriptor.Date.Cols.DAYOFWEEK
            					+" = ot."+ContentDescriptor.OpenTime.Cols.DAYOFWEEK+" ) WHERE "
            					+" d."+ContentDescriptor.Date.Cols.DATE+" = "+selectionArgs[0]+")"
            					+" AND "
            					+"t."+ContentDescriptor.Time.Cols.ID+" <= (" 
            					+"SELECT ot."+ContentDescriptor.OpenTime.Cols.CLOSEID+" FROM "
            					+ContentDescriptor.OpenTime.NAME+" ot LEFT JOIN "
            					+ContentDescriptor.Date.NAME+" d ON (d."+ContentDescriptor.Date.Cols.DAYOFWEEK
            					+" = ot."+ContentDescriptor.OpenTime.Cols.DAYOFWEEK+" ) WHERE "
            					+" d."+ContentDescriptor.Date.Cols.DATE+" = "+selectionArgs[0]+")"
            					;
            	/* If etime is not null, then for each id between current id and etime, show details equal to
            	 * the current details.
            	 *
            	 */
            	return builder.query(db, projection, selection, null, "_id", null, sortOrder);
            }
            
            //another special case
            case ContentDescriptor.Booking.PATH_JOIN_TOKEN:{ 
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Booking.NAME+" b LEFT JOIN "
            			+ContentDescriptor.Time.NAME+" t1 ON (b."+ContentDescriptor.Booking.Cols.STIMEID
            			+" = t1."+ContentDescriptor.Time.Cols.ID+") LEFT JOIN "
            			+ContentDescriptor.Time.NAME+" t2 ON (b."+ContentDescriptor.Booking.Cols.ETIMEID
            			+" = t2."+ContentDescriptor.Time.Cols.ID+")"
            			+" LEFT JOIN "
            			+ContentDescriptor.BookingTime.NAME+" bt ON (b."
            			+ContentDescriptor.Booking.Cols.BID+" = bt."+ContentDescriptor.BookingTime.Cols.BID+")");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Resource.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Resource.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Resource.PATH_FOR_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Resource.NAME);
            	selection = selection +" "+ContentDescriptor.Resource.Cols.ID+" = "+uri.getLastPathSegment();
            	return builder.query(db,  projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.Company.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Company.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.Swipe.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Swipe.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.Bookingtype.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Bookingtype.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.ResultStatus.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.ResultStatus.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.OpenTime.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.OpenTime.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.Date.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Date.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.Class.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Class.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Class.PATH_FOR_ID_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Class.NAME);
            	selection = selection+" "+ContentDescriptor.Class.Cols._ID+" = "+ uri.getLastPathSegment();
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.Membership.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Membership.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.BookingTime.PATH_TOKEN:{ 
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.BookingTime.NAME+" bt LEFT JOIN "
            			+ContentDescriptor.Booking.NAME+" b ON (bt."
            			+ContentDescriptor.BookingTime.Cols.BID+" = b."
            			+ContentDescriptor.Booking.Cols.BID+")"
            			+ "LEFT JOIN "+ContentDescriptor.Time.NAME+" t ON (bt."
            			+ContentDescriptor.BookingTime.Cols.TIMEID+" = t."
            			+ContentDescriptor.Time.Cols.ID+")");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);	
            }
            
            case ContentDescriptor.TableIndex.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.TableIndex.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.PendingUploads.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.PendingUploads.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.PendingDownloads.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.PendingDownloads.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.MembershipSuspend.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.MembershipSuspend.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.IdCard.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.IdCard.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.PaymentMethod.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.PaymentMethod.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Programme.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Programme.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Programme.PATH_FOR_GROUP_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Programme.NAME);
            	return builder.query(db, new String[] {ContentDescriptor.Programme.Cols.GID,
            			ContentDescriptor.Programme.Cols.GNAME}, selection, selectionArgs, 
            			ContentDescriptor.Programme.Cols.GNAME, null, null);
            }
            case ContentDescriptor.Door.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Door.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.MemberNotes.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.MemberNotes.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.MemberBalance.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.MemberBalance.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Member.TOKEN_JOIN_BALANCE:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Member.NAME+" m LEFT JOIN "
            			+ContentDescriptor.MemberBalance.NAME+" mb ON (m."
            			+ContentDescriptor.Member.Cols.MID+" = mb."
            			+ContentDescriptor.MemberBalance.Cols.MID+")");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            case ContentDescriptor.PendingUpdates.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.PendingUpdates.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.FreeIds.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.FreeIds.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.PendingDeletes.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.PendingDeletes.NAME);
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.Visitor.TOKEN_VISIT_PROGRAMME:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.Visitor.NAME+" v LEFT JOIN "
            		+ContentDescriptor.Membership.NAME+" ms ON "
            		+ "(v."+ContentDescriptor.Visitor.Cols.MSID+" = ms."+ContentDescriptor.Membership.Cols.MSID+")");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.RollCall.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.RollCall.NAME+" r");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case ContentDescriptor.RollItem.PATH_TOKEN:{
            	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            	builder.setTables(ContentDescriptor.RollItem.NAME+" r LEFT JOIN "
            			+ContentDescriptor.Member.NAME+" m ON (r."+ContentDescriptor.RollItem.Cols.MEMBERID
            			+" = m."+ContentDescriptor.Member.Cols.MID+")");
            	return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            }
            
            default: throw new UnsupportedOperationException("URI: " + uri + " not supported.");
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = hornetDb.getWritableDatabase();
		
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        switch(token){
            case ContentDescriptor.Member.PATH_TOKEN:{
                int result = db.update(ContentDescriptor.Member.NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return result;
            }
            case ContentDescriptor.Member.PATH_FOR_ID_TOKEN:{
            	int result = db.update(ContentDescriptor.Member.NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return result;
            }
            case ContentDescriptor.Image.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Image.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Image.PATH_FOR_ID_TOKEN:{
            	int result = db.update(ContentDescriptor.Image.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Visitor.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Visitor.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Visitor.PATH_FOR_ID_TOKEN:{
            	int result = db.update(ContentDescriptor.Visitor.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Booking.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Booking.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Resource.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Resource.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Time.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Time.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.ResultStatus.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.ResultStatus.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Class.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Class.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.OpenTime.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.OpenTime.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Membership.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Membership.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.BookingTime.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.BookingTime.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.TableIndex.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.TableIndex.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.PendingUploads.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.PendingUploads.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.MembershipSuspend.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.MembershipSuspend.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.IdCard.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.IdCard.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.PaymentMethod.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.PaymentMethod.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Programme.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Programme.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.Door.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.Door.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.MemberNotes.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.MemberNotes.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.MemberBalance.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.MemberBalance.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.RollCall.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.RollCall.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            case ContentDescriptor.RollItem.PATH_TOKEN:{
            	int result = db.update(ContentDescriptor.RollItem.NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	return result;
            }
            //TODO: fill this out further.
            case ContentDescriptor.DeletedRecords.PATH_TOKEN:{ //special case, deletes row from table.
            	String tablename = values.getAsString(ContentDescriptor.DeletedRecords.TABLENAME);
            	String rowid = values.getAsString(ContentDescriptor.DeletedRecords.ROWID);
            	if (tablename.compareTo(ContentDescriptor.Member.NAME) == 0) {
            		return db.delete(ContentDescriptor.Member.NAME, ContentDescriptor.Member.Cols.MID+" = ?", 
            				new String[] {rowid});
            	} else if (tablename.compareTo(ContentDescriptor.Membership.NAME) == 0) {
            		return db.delete(ContentDescriptor.Membership.NAME, ContentDescriptor.Membership.Cols.MSID+" = ?", 
            				new String[] {rowid});
            	} else if (tablename.compareTo(ContentDescriptor.Image.NAME) == 0) {
            		return db.delete(ContentDescriptor.Image.NAME, ContentDescriptor.Image.Cols.IID+" = ?",
            				new String[] {rowid});
            	} else if (tablename.compareTo(ContentDescriptor.Booking.NAME) == 0) {
            		return db.delete(ContentDescriptor.Booking.NAME, ContentDescriptor.Booking.Cols.BID+" = ?",
            				new String[] {rowid});
            	}//TODO: roll & roll_item
            	else {
            		Log.e("Unsupported Operation", "DELETE FROM TABLE: "+tablename, 
            				new UnsupportedOperationException("DELETE FROM TABLE: "+tablename+" NOT SUPPORTED.")); 
            		return 0;
            	}
            		
            }
            
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
        }
	}
}
