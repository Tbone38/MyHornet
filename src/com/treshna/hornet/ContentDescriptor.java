package com.treshna.hornet;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;
/*
 * This class provides/stores the information required
 * by the application to interface with the content provider
 * for the class.
 */

/*	ID RANGES
 *  0   > < 100 = company/database settings etc.
 *  100 > < 200 = Member/membership/images etc
 *  200 > < 300 = Pending member uploads & swipes.
 *  300 > < 400 = Bookings related tables.
 */

public class ContentDescriptor {
	 public static final String AUTHORITY = "com.treshna.hornet";
	 private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	 public static final UriMatcher URI_MATCHER = buildUriMatcher();
	 
	 public static final String DROPTABLE = "dropTable";
	 public static final int TOKEN_DROPTABLE = 1;
	 public static final Uri DROPTABLE_URI = BASE_URI.buildUpon().appendPath(DROPTABLE).build();
	 private ContentDescriptor(){};
	 
	 private static  UriMatcher buildUriMatcher() {
		 final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	     final String authority = AUTHORITY;
	 
	     matcher.addURI(authority, Member.PATH, Member.PATH_TOKEN);
	     matcher.addURI(authority, Member.PATH_FOR_ID, Member.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Image.PATH, Image.PATH_TOKEN);
	     matcher.addURI(authority, Image.PATH_FOR_ID, Image.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Visitor.PATH, Visitor.PATH_TOKEN);
	     matcher.addURI(authority, Visitor.PATH_FOR_ID, Visitor.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Visitor.PATH_FOR_JOIN, Visitor.PATH_FOR_JOIN_TOKEN);
	     matcher.addURI(authority, Image.PATH_FOR_JOIN, Image.PATH_FOR_JOIN_TOKEN);
	     matcher.addURI(authority, Image.PATH_FOR_JOIN_ID, Image.PATH_FOR_JOIN_ID_TOKEN);
	     matcher.addURI(authority, Membership.PATH, Membership.PATH_TOKEN);
	     matcher.addURI(authority, Membership.PATH_FOR_ID, Membership.PATH_FOR_ID_TOKEN);
	     
	     matcher.addURI(authority, Pending.PATH, Pending.PATH_TOKEN);
	     matcher.addURI(authority, Pending.PATH_FOR_ID, Pending.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Swipe.PATH, Swipe.PATH_TOKEN);
	     matcher.addURI(authority, Swipe.PATH_FOR_ID, Swipe.PATH_FOR_ID_TOKEN);
	     
	     matcher.addURI(authority, Booking.PATH, Booking.PATH_TOKEN);
	     matcher.addURI(authority, Booking.PATH_FOR_ID, Booking.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Booking.PATH_JOIN_TIME, Booking.PATH_JOIN_TOKEN); //gets 1 booking joined with time
	     matcher.addURI(authority, Bookingtype.PATH, Bookingtype.PATH_TOKEN);
	     matcher.addURI(authority, Bookingtype.PATH_FOR_ID, Bookingtype.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Class.PATH, Class.PATH_TOKEN);
	     matcher.addURI(authority, Class.PATH_FOR_ID, Class.PATH_FOR_ID_TOKEN);
	     
	     matcher.addURI(authority, Resource.PATH, Resource.PATH_TOKEN);
	     matcher.addURI(authority, Resource.PATH_FOR_ID, Resource.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Company.PATH, Company.PATH_TOKEN);
	     matcher.addURI(authority, Company.PATH_FOR_ID, Company.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, ResultStatus.PATH, ResultStatus.PATH_TOKEN);
	     matcher.addURI(authority, ResultStatus.PATH_FOR_ID, ResultStatus.PATH_FOR_ID_TOKEN);
	     
	     matcher.addURI(authority, OpenTime.PATH, OpenTime.PATH_TOKEN);
	     matcher.addURI(authority, OpenTime.PATH_FOR_ID, OpenTime.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Date.PATH, Date.PATH_TOKEN);
	     matcher.addURI(authority, Date.PATH_FOR_ID, Date.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Time.PATH, Time.PATH_TOKEN);
	     matcher.addURI(authority, Time.PATH_FOR_ID, Time.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, Time.PATH_FOR_JOIN, Time.PATH_FOR_JOIN_TOKEN); //gets list of bookings joined with time
	     matcher.addURI(authority, BookingTime.PATH, BookingTime.PATH_TOKEN);
	     matcher.addURI(authority, BookingTime.PATH_FOR_ID, BookingTime.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, BookingTime.PATH_FOR_JOIN, BookingTime.PATH_FOR_JOIN_TOKEN);
	     
	     matcher.addURI(authority, TableIndex.PATH, TableIndex.PATH_TOKEN);
	     matcher.addURI(authority, TableIndex.PATH_FOR_ID, TableIndex.PATH_FOR_ID_TOKEN);
	     matcher.addURI(authority, PendingUploads.PATH, PendingUploads.PATH_TOKEN);
	     matcher.addURI(authority, PendingUploads.PATH_FOR_ID, PendingUploads.PATH_FOR_ID_TOKEN);
	     
	     matcher.addURI(authority, DROPTABLE, TOKEN_DROPTABLE);
	     
	     return matcher;
	 }
	 	public static class Member {
	        public static final String NAME = "Member";
	        /*
	         * magic numbers follow, they're used only to reference
	         * what the request is, for use with a switch case.
	         */
	        public static final String PATH = "Member";
	        public static final int PATH_TOKEN = 100; 
	        public static final String PATH_FOR_ID = "Member/*";
	        public static final int PATH_FOR_ID_TOKEN = 110;
	 
	        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 
	        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.member";
	        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.member";
	        
	        public static class Cols {
	        	//can this table be used for storing/checking if cards are accept/deny ? or should membership table be used?
	            public static final String MID = BaseColumns._ID; // convention
	            //public static final String MID = "memberid";
	            
	           // public static final String NAME="mname"; //TODO: replace this with separate first-name & surname;
	            public static final String FNAME="firstname";
	            public static final String SNAME="surname";
	       	 	public static final String DATE="sdate";
	       	 	public static final String TIME="stime12";
	       	 	public static final String DENY="denyreason";
	            public static final String DATETIME="datetime";
	       	 	
	       	 	public static final String COLOUR="fgcolour";
	       	 	public static final String DOORNAME="doorname";
	       	 	
	       	 	public static final String RESULT="result"; // were they let in ?
	       	 	public static final String HAPPINESS="happiness";

	       	 	public static final String LENGTH="len"; //this should be moved to visitors
	       	 	public static final String TASKP="taskpending";
	       	 	public static final String BOOKP="bookingpending";

	       	 	public static final String PHHOME = "phonehome";
	       	 	public static final String PHCELL = "phonecell";
	       	 	public static final String PHWORK = "phonework";
	       	 	public static final String EMAIL = "email";
	       	 	public static final String NOTES = "notes";
	       	 	public static final String TASK1 = "task1";
	       	 	public static final String TASK2 = "task2";
	       	 	public static final String TASK3 = "task3";
	       	 	public static final String BOOK1 = "booking1";
	       	 	public static final String BOOK2 = "booking2";
	       	 	public static final String BOOK3 = "booking3";
	       	 	public static final String LASTVISIT = "lastvisit1";
	       	 	public static final String STATUS = "status";
	       	 	public static final String LASTUPDATE = "lastupdated";
	        }
	    }

	 	public static class Visitor {
	 		public static final String NAME = "Visitor";
	 		public static final String PATH = "Visitor";
	 		public static final int PATH_TOKEN = 130;
	 		public static final String PATH_FOR_ID = "Visitor/*";
	 		public static final int PATH_FOR_ID_TOKEN = 140;
	 		public static final String PATH_FOR_JOIN = "Visitors_Members";
	 		public static final int PATH_FOR_JOIN_TOKEN = 135;
	 		
	 		public static final Uri VISITOR_JOIN_MEMBER_URI = BASE_URI.buildUpon().appendPath(
	 				PATH_FOR_JOIN).build();
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.visitor";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.visitor";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String MID = "memberid";
	 			public static final String MSID = "membershipid";
	 			public static final String DENY="denyreason";
	            public static final String DATETIME="datetime";
	            public static final String DATE="sdate";
	       	 	public static final String TIME="stime12";
	       	 	public static final String CARDNO = "cardno";
	       	 	public static final String DOORNAME = "doorname";
	       	 	public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class Image {
	 		public static final String NAME = "Image";
	 		public static final String PATH = "Image";
	 		public static final int PATH_TOKEN = 150;
	 		public static final String PATH_FOR_ID = "Image/*";
	 		public static final int PATH_FOR_ID_TOKEN = 160;
	 		
	 		public static final String PATH_FOR_JOIN = "Images_Members";
	 		public static final int PATH_FOR_JOIN_TOKEN = 155;
	 		public static final String PATH_FOR_JOIN_ID = "Images_Members/*";
	 		public static final int PATH_FOR_JOIN_ID_TOKEN = 165;
	 		public static final Uri IMAGE_JOIN_MEMBER_URI = BASE_URI.buildUpon().appendPath(
	 				PATH_FOR_JOIN).build();
	 		
	        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.image";
	        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.image";
	        
	        public static class Cols {
	        	public static final String ID = BaseColumns._ID;
	        	public static final String MID = "memberID";
	        	public static final String DATE = "date";
	        	public static final String DESCRIPTION = "description";
	        	public static final String IS_PROFILE = "is_profile";
	        	public static final String LASTUPDATED = "lastupdated";
	        }
	 	}
	 	
	 	public static class Pending {
	 		public static final String NAME = "Pending";
	 		public static final String PATH = "Pending";
	 		public static final int PATH_TOKEN = 200;
	 		public static final String PATH_FOR_ID = "Pending/*";
	 		public static final int PATH_FOR_ID_TOKEN = 210;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.pending";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.pending";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String MID = "memberID";
	 			public static final String FNAME = "fName";
	 			public static final String SNAME = "sName";
	 			public static final String DOB = "dob";
	 			public static final String GENDER = "gender";
	 			public static final String MEDICAL = "medical";
	 			public static final String STREET = "street";
	 			public static final String SUBURB = "suburb";
	 			public static final String CITY = "city";
	 			public static final String POSTAL = "postal";
	 			public static final String EMAIL = "email";
	 			public static final String HPHONE = "homephone";
	 			public static final String CPHONE = "cellphone";
	 			public static final String SIGNUP = "signuptype";
	 			
	 			//is the MID being used? //0 = no, 1 = yes.
	 			public static final String ISUSED = "isused";
	 		}
	 	}
	 	
	 	public static class Time {
	 		public static final String NAME = "Time";
	 		public static final String PATH = "Time";
	 		public static final int PATH_TOKEN = 324;
	 		public static final String PATH_FOR_ID = "Time/*";
	 		public static final int PATH_FOR_ID_TOKEN = 315;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.time";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.time";
	 		
	 		public static final String PATH_FOR_JOIN = "Time_Booking";
	 		public static final int PATH_FOR_JOIN_TOKEN = 325;
	 		public static final Uri TIME_BOOKING_URI = BASE_URI.buildUpon().appendPath(
	 				PATH_FOR_JOIN).build();
	 		
	 		public static class Cols {
	 			public static String ID = BaseColumns._ID;
	 			public static String TIME = "time";	 			
	 		}
	 	}
	 	
	 	public static class Booking {
	 		public static final String NAME = "Booking";
	 		public static final String PATH = "Booking";
	 		public static final int PATH_TOKEN = 300;
	 		public static final String PATH_FOR_ID = "Booking/*";
	 		public static final int PATH_FOR_ID_TOKEN = 310;
	 		
	 		public static final String PATH_JOIN_TIME = "Booking_Time";
	 		public static final int PATH_JOIN_TOKEN = 305;
	 		public static final Uri BOOKING_TIME_URI = BASE_URI.buildUpon().appendPath(PATH_JOIN_TIME).build();
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.bookings";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.bookings";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String BID = "bookingid";
	 			//public static final String NAME = "name";
	 			public static final String FNAME = "firstname";
	 			public static final String SNAME = "surname";
	 			public static final String BOOKING = "bookingdescription";
	 			public static final String STIMEID = "starttimeid"; //reference time.id
	 			public static final String ETIMEID = "endtimeid";   // reference time.id
	 			public static final String STIME = "starttime";
	 			public static final String ETIME = "endtime";
	 			public static final String RID = "resourceid";
	 			public static final String ARRIVAL = "arrival";
	 			public static final String BOOKINGTYPE = "bookingtypeid";
	 			public static final String NOTES = "notes";
	 			public static final String RESULT = "result";
	 			/* Result Status;
	 			 * 20 = showed,
	 			 * 21 = showed late,
	 			 *  5 = cancelled,
	 			 * 15 = no show
	 			 * 10 = booking,
	 			 * 11 = first-booking
	 			 */
	 			public static final String MID = "memberid";
	 			public static final String MSID = "membershipid";
	 			public static final String LASTUPDATED = "lastupdate"; //since Unix Epoch
	 			public static final String CHECKIN = "checkin";
	 			public static final String OFFSET = "offset"; // == resource.period
	 			public static final String IS_UPLOADED = "is_uploaded"; //has the booking been uploaded?
	 			public static final String CLASSID ="classid";
	 		}
	 	}
	 	
	 	
	 	// to implement, make time, booking, arrival, and resource all the primary key ?
	 	public static class BookingTime {
	 		public static final String NAME = "BookingTime";
	 		public static final String PATH = "BookingTime";
	 		public static final int PATH_TOKEN = 311;
	 		public static final String PATH_FOR_ID = "BookingTime/*";
	 		public static final int PATH_FOR_ID_TOKEN = 312;
	 		
	 		public static final String PATH_FOR_JOIN = "Booking_BookingTime";
	 		public static final int PATH_FOR_JOIN_TOKEN = 313;
	 		public static final Uri JOIN_URI = BASE_URI.buildUpon().appendPath(PATH_FOR_JOIN).build();
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.bookingtime";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.bookingtime";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String TIMEID = "timeid";
	 			public static final String BID = "bookingid";
	 			public static final String ARRIVAL = "arrival"; //yyyyMMdd
	 			public static final String RID = "resourceid";
	 		}
	 	}
	 	
	 	public static class Bookingtype {
	 		public static final String NAME = "Bookingtype";
	 		public static final String PATH = "Bookingtype";
	 		public static final int PATH_TOKEN = 320;
	 		public static final String PATH_FOR_ID = "Bookingtype/*";
	 		public static final int PATH_FOR_ID_TOKEN = 330;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.bookingtype";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.bookingtype";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String BTID = "bookingtypeid";
	 			public static final String NAME = "name";
	 			public static final String PRICE = "price";
	 			public static final String VALIDFROM = "validfrom";
	 			public static final String VALIDTO = "validto";
	 			public static final String EXTERNAL = "externalname";
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class Resource {
	 		public static final String NAME = "Resource";
	 		public static final String PATH = "Resource";
	 		public static final int PATH_TOKEN = 340;
	 		public static final String PATH_FOR_ID= "Resource/*";
	 		public static final int PATH_FOR_ID_TOKEN = 350;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.resource";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.resource";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String NAME = "name";
	 			public static final String CID = "companyid";
	 			public static final String RTNAME = "typename";
	 			public static final String PERIOD = "period";
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	// NOT USED;
	 	public static class Company {
	 		public static final String NAME = "Company";
	 		public static final String PATH = "Company";
	 		public static final int PATH_TOKEN = 50;
	 		public static final String PATH_FOR_ID = "Company/*";
	 		public static final int PATH_FOR_ID_TOKEN = 55;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.company";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.company";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String NAME = "name";
	 		}
	 	}
	 	// for creating member ships ?
	 	public static class Programme {
	 		public static final String NAME = "Programme";
	 		public static final String PATH = "Programme";
	 		public static final int PATH_TOKEN = 360;
	 		public static final String PATH_FOR_ID = "Programme/*";
	 		public static final int PATH_FOR_ID_TOKEN = 370;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.programme";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.programme";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String GID = "groupid";
	 			public static final String NAME = "name";
	 			public static final String GNAME = "groupname";
	 			public static final String SDATE = "startdate";
	 			public static final String EDATE = "enddate";
	 			public static final String PRICE = "price";
	 			public static final String MLENGTH = "mlength";
	 			public static final String SIGNUP = "signupfee";
	 			public static final String ONLINE = "online";
	 			public static final String NOTE = "pricenotes";
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	//for storing actual memberships ?
	 	public static class Membership {
	 		public static final String NAME = "Membership";
	 		public static final String PATH = "Membership";
	 		public static final int PATH_TOKEN = 120;
	 		public static final String PATH_FOR_ID = "Membership/*";
	 		public static final int PATH_FOR_ID_TOKEN = 125;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.membership";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.membership";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String MID = "memberID";
	 			public static final String MSID="membershipid";
	       	 	public static final String PNAME="pname";
	       	 	public static final String MSSTART="msstart"; 
	       	 	public static final String EXPIRERY="msexpiry";
	       	 	public static final String VISITS="msvisits"; //concession
	 			//if membership table is used for checking cards then: see pi cache;
	 			public static final String CARDNO = "cardno";
	 			public static final String DENY = "deny"; //0 = allow, 1 = deny ? where do I get this?
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class Swipe {
	 		public static final String NAME = "Swipe";
	 		public static final String PATH = "Swipe";
	 		public static final int PATH_TOKEN = 220;
	 		public static final String PATH_FOR_ID = "Swipe/*";
	 		public static final int PATH_FOR_ID_TOKEN = 230;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.swipe";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.swipe";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID; //cardno
	 			public static final String DOOR = "door";
	 			public static final String DATETIME = "datetime";
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class ResultStatus {
	 		public static final String NAME = "ResultStatus";
	 		public static final String PATH = "ResultStatus";
	 		public static final int PATH_TOKEN = 80;
	 		public static final String PATH_FOR_ID = "ResultStatus/*";
	 		public static final int PATH_FOR_ID_TOKEN = 90;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.resultstatus";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.resultstatus";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String NAME = "name";
	 			public static final String COLOUR = "bgcolour";
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class OpenTime {
	 		public static final String NAME = "OpenTime";
	 		public static final String PATH = "OpenTime";
	 		public static final int PATH_TOKEN = 91;
	 		public static final String PATH_FOR_ID = "OpenTime/*";
	 		public static final int PATH_FOR_ID_TOKEN = 92;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.opentime";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.opentime";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DAYOFWEEK = "dayofweek";
	 			public static final String OPENTIME = "opentime";
	 			public static final String OPENID = "openid";
	 			public static final String CLOSETIME = "closetime";
	 			public static final String CLOSEID = "closeid";
	 			public static final String NAME = "name"; //monday tuesday etc
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class Date {
	 		public static final String NAME = "Dates";
	 		public static final String PATH = "Dates";
	 		public static final int PATH_TOKEN = 93;
	 		public static final String PATH_FOR_ID = "Dates/*";
	 		public static final int PATH_FOR_ID_TOKEN = 94;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.date";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.date";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DATE = "dates"; //'s' probably because other table has date ?
	 			public static final String DAYOFWEEK = "dayofweek";
	 		}
	 	}
	 	
	 	public static class Class {
	 		public static final String NAME = "Class";
	 		public static final String PATH = "Class";
	 		public static final int PATH_TOKEN = 321;
	 		public static final String PATH_FOR_ID = "Class/*";
	 		public static final int PATH_FOR_ID_TOKEN = 322;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.class";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.class";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String CID = "cid";
	 			public static final String NAME = "name";
	 			public static final String MAX_ST = "max_students";
	 			public static final String FREQ = "frequency";
	 			public static final String SDATE = "startdate";
	 			public static final String STIME = "starttime";
	 			public static final String ETIME = "endtime";
	 			public static final String RID = "resourceid";
	 			public static final String LASTUPDATED = "lastupdated";
	 		}
	 	}
	 	
	 	public static class TableIndex {
	 		public static final String NAME = "TableIndex";
	 		public static final String PATH = "TableIndex";
	 		public static final int PATH_TOKEN = 10;
	 		public static final String PATH_FOR_ID = "TableIndex/*";
	 		public static final int PATH_FOR_ID_TOKEN = 11;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.tableindex";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.tableindex";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String NAME = "name";
	 		}
	 		/**
	 		 * key (id's) used for referencing each table-name.
	 		 * @author callum
	 		 */
	 		public static enum Values {
	 			Booking(1), Class(2), Swipe(3),Membership(4) /*when adding memberships*/,
	 					Pending(5) /*when adding members/prospects*/,Image(6);
	 			
	 			private final int key;
	 			
	 			Values(int key) {
	 				this.key = key;
	 			}
	 			
	 			public static final int getLength(){
	 				return Values.values().length;
	 			}
	 			
	 			/*public static final int getKey(Values v) {
	 				return v.key;
	 			}*/
	 			
	 			public static final Values getValue(int id){
	 				for (Values v : Values.values()){
	 					if (v.key == id) {
	 						return v;
	 					}
	 				}
	 				return null;
	 			}
	 			
	 			public int getKey() {
	 				return this.key;
	 			}
	 		}
	 	}
	 	
	 	//TODO: move more uploads here:
	 	//		-Member
	 	//		-Image
	 	//		-Swipe
	 	//		-Booking
	 	public static class PendingUploads {
	 		public static final String NAME = "PendingUploads";
	 		public static final String PATH = "PendingUploads";
	 		public static final int PATH_TOKEN = 12;
	 		public static final String PATH_FOR_ID = "PendingUploads/*";
	 		public static final int PATH_FOR_ID_TOKEN = 13;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.cursor.dir/vnd.treshna.pendinguploads";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.cursor.item/vnd.treshna.pendinguploads";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 			//timestamp, probably not neccissary.
	 		}
	 	}
}
