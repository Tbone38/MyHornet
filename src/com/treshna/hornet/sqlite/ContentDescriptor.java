package com.treshna.hornet.sqlite;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/*	ID RANGES

 *  0   > < 100 = company/database settings etc.
 *  100 > < 200 = Member/membership/images etc
 *  200 > < 300 = Pending member uploads & swipes.
 *  300 > < 400 = Bookings related tables.
 *  400 > < 500 = Other GymMaster tables (id-card).
 *  600 > < 700 = custom Tables (specific development).
 *  In use ID's:
 *    1 = drop table					111:
 *    5 = DeletedRecords				112 = MemberNotes
 *    9 = AppConfig						113:
 *   10:								114 = MemberBalance
 *   11 = TableIndex					
 *   12:								
 *   13 = PendingUploads				121 = CancellationFee
 *   14:								
 *   15 = PendingDownloads				
 *   16:								126:
 *   17 = PendingUpdates				127 = MembershipSuspend
 *   18 = PendingDeletes				128 = MembershipExpiryReason
 *   20 = FreeIds						130:
 *   22 = PendingConflicts
 *   50:								135:
 *   55 = Company (unused)
 *   56 = KPI's							140 = Visitor
 *   60:								141 = Visitors/Programme/Membership query.
 *   61 = Payment Method				
 *   62:								150:
 *   63 = door							155:
 *   80:								160:
 *   90 = ResultStatus					165 = Images
 *   91:								200 = UnusedMemberIDs
 *   92 = OpenTime						201 = MemberFind
 *   93:								220:
 *   94 = Date							230 = Swipe
 *  100:								300:
 *  110 = Member (excluding UNUSED ID's)
 *  115 = Member_MemberBalance			305:
 *  120:								310 = Booking
 *  125 = Membership
 *  170 = Prospect
 *  311:								400:
 *  312:								401 = idcard
 *  313 = BookingTime					405 = dd_export_format
 *  315:
 *  324:								410 = MemberFinance
 *  325 = Time							411 = Billing_history
 *  321:								412 = Payment_against
 *  322 = Class							413 = correspondance_set
 *  320:
 *  330 = Bookingtype 
 *  340:
 *  350 = Resource
 *  360:
 *  370 = Programme
 *  371 = ProgrammeGroup (sorted from programme)
 */
/**
 * This class provides/stores the information required
 * by the application to interface with the content provider
 * for the class.
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
	     matcher.addURI(authority, PendingDownloads.PATH, PendingDownloads.PATH_TOKEN);
	     matcher.addURI(authority, PendingUpdates.PATH, PendingUpdates.PATH_TOKEN);
	     matcher.addURI(authority, DeletedRecords.PATH, DeletedRecords.PATH_TOKEN);
	     
	     matcher.addURI(authority, MembershipSuspend.PATH, MembershipSuspend.PATH_TOKEN);
	     matcher.addURI(authority, IdCard.PATH, IdCard.PATH_TOKEN);
	     matcher.addURI(authority, PaymentMethod.PATH, PaymentMethod.PATH_TOKEN);
	     matcher.addURI(authority, Programme.PATH, Programme.PATH_TOKEN);
	     matcher.addURI(authority, Programme.PATH_FOR_GROUP, Programme.PATH_FOR_GROUP_TOKEN);
	     matcher.addURI(authority, Visitor.PATH_VISIT_PROGRAMME, Visitor.TOKEN_VISIT_PROGRAMME);
	     
	     matcher.addURI(authority, Door.PATH, Door.PATH_TOKEN);
	     matcher.addURI(authority, MemberNotes.PATH, MemberNotes.PATH_TOKEN);
	     matcher.addURI(authority, MemberBalance.PATH, MemberBalance.PATH_TOKEN);
	     matcher.addURI(authority, Member.PATH_JOIN_BALANCE, Member.TOKEN_JOIN_BALANCE);
	     matcher.addURI(authority, Member.PATH_FIND, Member.TOKEN_FIND);
	     matcher.addURI(authority, FreeIds.PATH, FreeIds.PATH_TOKEN);
	     matcher.addURI(authority, PendingDeletes.PATH, PendingDeletes.PATH_TOKEN);
	     
	     matcher.addURI(authority, RollCall.PATH, RollCall.PATH_TOKEN);
	     matcher.addURI(authority, RollItem.PATH, RollItem.PATH_TOKEN);
	     matcher.addURI(authority, RollItem.CREATE_ROLL_PATH, RollItem.CREATE_ROLL_TOKEN);
	     
	     matcher.addURI(authority, MembershipExpiryReason.PATH, MembershipExpiryReason.PATH_TOKEN);
	     matcher.addURI(authority, CancellationFee.PATH, CancellationFee.PATH_TOKEN);
	     matcher.addURI(authority, KPI.PATH, KPI.PATH_TOKEN);
	     matcher.addURI(authority, AppConfig.PATH, AppConfig.PATH_TOKEN);
	     matcher.addURI(authority, Enquiry.PATH, Enquiry.PATH_TOKEN);
	     
	     matcher.addURI(authority, MemberFinance.PATH, MemberFinance.PATH_TOKEN);
	     matcher.addURI(authority, BillingHistory.PATH, BillingHistory.PATH_TOKEN);
	     matcher.addURI(authority, DDExportFormat.PATH, DDExportFormat.PATH_TOKEN);
	     matcher.addURI(authority, PaymentAgainst.PATH, PaymentAgainst.PATH_TOKEN);
	     matcher.addURI(authority, PendingConflicts.PATH, PendingConflicts.PATH_TOKEN);
	     
	     matcher.addURI(authority, DROPTABLE, TOKEN_DROPTABLE);
	     
	     return matcher;
	 }
	 	public static class Member {
	        
	 		public static final String NAME = "member";
	        public static final String PATH = "Member";
	        public static final int PATH_TOKEN = 100; 
	        public static final String PATH_FOR_ID = "Member/*";
	        public static final int PATH_FOR_ID_TOKEN = 110;
	        
	        public static final String PATH_FIND = "MemberFind";
	        public static final int TOKEN_FIND = 201;
	        public static final Uri URI_FIND = BASE_URI.buildUpon().appendPath(PATH_FIND).build();
	 
	        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.member";
	        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.member";
	        
	        public static final String PATH_JOIN_BALANCE = "MemberMemberBalance";
	        public static final int TOKEN_JOIN_BALANCE = 115;
	        public static final Uri URI_JOIN_BALANCE = BASE_URI.buildUpon().appendPath(PATH_JOIN_BALANCE).build();
	        
	        public static class Indexs {
	        	public static final String MEMBER_NAME = "member_member_name";
	        }
	        
	        public static class Triggers {
	 			public static final String ON_INSERT = "member_insert";
	 			public static final String ON_UPDATE_MID = "memberid_update";
	 			public static final String ON_UPDATE = "member_update";
	 			public static final String ON_DELETE = "member_delete"; //unused
	 		}
	        
	        public static class Cols implements BaseColumns{
	        	
	            public static final String MID = "mid"; 	            
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
	       	 	
	       	 	public static final String TASK1 = "task1";
	       	 	public static final String TASK2 = "task2";
	       	 	public static final String TASK3 = "task3";
	       	 	public static final String BOOK1 = "booking1";
	       	 	public static final String BOOK2 = "booking2";
	       	 	public static final String BOOK3 = "booking3";
	       	 	public static final String LASTVISIT = "lastvisit1";
	       	 	public static final String CARDNO = "cardno";
	       	 	/**
	       	 	 * Member Status's:
	       	 	 * 		   -1 = FREE MEMBERID
	       	 	 * 			0 = Current;
	       	 	 * 			1 = Suspended;
	       	 	 * 			2 = Expired Recently;
	       	 	 * 			3 = Expired;
	       	 	 * 			4 = Promotion;
	       	 	 * 			5 = Casual;
	       	 	 */
	       	 	public static final String STATUS = "status";
	       	 	public static final String LASTUPDATE = "lastupdate";
	       	 	//added for merge with Pending.
	       	 	public static final String DOB = "dob";
	 			public static final String GENDER = "gender";
	 			public static final String MEDICAL = "medicalconditions"; //rename this medicalconditions
	 			public static final String STREET = "street";
	 			public static final String SUBURB = "suburb";
	 			public static final String CITY = "city";
	 			public static final String POSTAL = "postal";
	 			public static final String DEVICESIGNUP = "devicesignup"; 
	 			//boolean field, to tell the difference between downloaded members and android signup members.
	 			
	 			//YMCA request.
	 			public static final String EMERGENCYNAME = "emergencyname";
	 			public static final String EMERGENCYHOME = "emergencyhome";
	 			public static final String EMERGENCYCELL = "emergencycell";
	 			public static final String EMERGENCYWORK = "emergencywork";
	 			public static final String EMERGENCYRELATIONSHIP = "emergencyrelationship";
	 			public static final String MEDICALDOSAGE = "medicaldosage";
	 			public static final String MEDICATIONBYSTAFF = "medicationbystaff";
	 			public static final String MEDICATION = "medication";
	 			
	 			//YMCA SPECIFIC
	 			public static final String PARENTNAME = "parentname"; //default this to empty.
	 			
	 			//Added in V119
	 			public static final String COUNTRY = "addresscountry";
	 			public static final String BILLINGACTIVE = "billingactive";
	 			public static final String DD_EXPORT_FORMATID = "dd_export_formatid";
	 			public static final String EZIDEBIT = "ezidebitcustomerid";
	        }
	        
	        public static class OldCols {
	        	public static final String MEDICAL = "medical"; //renamed t0 medicalconditions
	        	public static final String NOTES = "notes"; //deleted.
	        }
	        
	    }
	 	
	 	public static class Enquiry {
	 		public static final String NAME = "enquiry";
	 		public static final String PATH = "enquiry";
	 		public static final int PATH_TOKEN = 170;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.enquiry";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.enquiry";
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "enquiry_insert";
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			
	 			public static final String EID = "eid";
	 			public static final String MID = "mid"; 	            
	            public static final String FNAME="firstname";
	            public static final String SNAME="surname";	 			
	 			public static final String PHHOME = "phonehome";
	       	 	public static final String PHCELL = "phonecell";
	       	 	public static final String PHWORK = "phonework";
	       	 	public static final String EMAIL = "email";
	       	 	public static final String DOB = "dob";
	 			public static final String GENDER = "gender";
	 			public static final String NOTES = "notes";
	 			public static final String STREET = "street";
	 			public static final String SUBURB = "suburb";
	 			public static final String CITY = "city";
	 			public static final String POSTAL = "postal";
	 			public static final String DEVICESIGNUP = "devicesignup"; 
	 			//boolean field, to tell the difference between downloaded members and android signup members.
	 		}
	 	}

	 	public static class Visitor {
	 		public static final String NAME = "visitor";
	 		public static final String PATH = "Visitor";
	 		public static final int PATH_TOKEN = 130;
	 		public static final String PATH_FOR_ID = "Visitor/*";
	 		public static final int PATH_FOR_ID_TOKEN = 140;
	 		public static final String PATH_FOR_JOIN = "Visitors_Members";
	 		public static final int PATH_FOR_JOIN_TOKEN = 135;
	 		
	 		public static final String PATH_VISIT_PROGRAMME = "Visitors_Membership_Programme";
	 		public static final int TOKEN_VISIT_PROGRAMME = 141;
	 		public static final Uri VISITOR_PROGRAMME_URI = BASE_URI.buildUpon().appendPath(PATH_VISIT_PROGRAMME).build();
	 		
	 		public static final Uri VISITOR_JOIN_MEMBER_URI = BASE_URI.buildUpon().appendPath(
	 				PATH_FOR_JOIN).build();
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.visitor";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.visitor";
	 		
	 		public static class Indexs {
	 			public static final String MEMBER_ID = "visitor_member_id";
	 			public static final String MS_ID = "visitor_membership_id";
	 			public static final String DATE_TIME = "visitor_date_time";
	 		}
	 		
	 		
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
	       	 	public static final String LASTUPDATE = "lastupdate";
	 		}
	 	}
	 	
	 	public static class Image {
	 		public static final String NAME = "image";
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
	        
	        public static class Indexs {
	        	public static final String MEMBER_ID = "image_member_id";
	        }
	        
	        public static class Triggers {
	        	public static final String ON_INSERT = "image_insert";
	        	public static final String ON_UPDATE = "image_update";
	        }
	        
	        public static class Cols {
	        	public static final String ID = BaseColumns._ID;
	        	
	        	public static final String IID = "imageid";
	        	public static final String MID = "memberID";
	        	public static final String DATE = "date";
	        	public static final String DESCRIPTION = "description";
	        	public static final String IS_PROFILE = "is_profile";
	        	public static final String LASTUPDATE = "lastupdate";
	        	
	        	public static final String DEVICESIGNUP = "devicesignup";
	        }
	        
	        public static class OldCols {
	        	public static final String DISPLAYVALUE = "displayvalue"; //not used
	        }
	 	}
	 	
	 	
	 	public static class Time {
	 		public static final String NAME = "time";
	 		public static final String PATH = "Time";
	 		public static final int PATH_TOKEN = 324;
	 		public static final String PATH_FOR_ID = "Time/*";
	 		public static final int PATH_FOR_ID_TOKEN = 315;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.time";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.time";
	 		
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
	 		public static final String NAME = "booking";
	 		public static final String PATH = "Booking";
	 		public static final int PATH_TOKEN = 300;
	 		public static final String PATH_FOR_ID = "Booking/*";
	 		public static final int PATH_FOR_ID_TOKEN = 310;
	 		
	 		public static final String PATH_JOIN_TIME = "Booking_Time";
	 		public static final int PATH_JOIN_TOKEN = 305;
	 		public static final Uri BOOKING_TIME_URI = BASE_URI.buildUpon().appendPath(PATH_JOIN_TIME).build();
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.bookings";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.bookings";
	 		
	 		public static class Indexs {
	 			public static final String BOOKING_ID = "booking_booking_id";
	 			public static final String BOOKING_NAME = "booking_member_name";
	 			public static final String MEMBER_ID = "booking_member_id";
	 			public static final String MEMBERSHIP_ID = "booking_membership_id";
	 			public static final String RESOURCE_ID = "booking_resource_id";
	 			public static final String CLASS_ID = "booking_class_id";
	 		}
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "booking_insert";
	 			public static final String ON_UPDATE_BID = "bookingid_update";
	 			public static final String ON_UPDATE = "booking_update";
	 			public static final String ON_DELETE = "booking_delete"; //unused
	 		}
	 		
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
	 			public static final String LASTUPDATE = "lastupdate"; //since Unix Epoch
	 			public static final String CHECKIN = "checkin";
	 			public static final String OFFSET = "offset"; // == resource.period
	 			public static final String CLASSID ="classid";
	 			public static final String PARENTID = "parentid";
	 			//was the booking made on this device. (used so we know what to upload).
	 			public static final String DEVICESIGNUP = "devicesignup";
	 		}
	 	}
	 	
	 	
	 	// to implement, make time, booking, arrival, and resource all the primary key ?
	 	public static class BookingTime {
	 		public static final String NAME = "bookingtime";
	 		public static final String PATH = "BookingTime";
	 		public static final int PATH_TOKEN = 311;
	 		public static final String PATH_FOR_ID = "BookingTime/*";
	 		public static final int PATH_FOR_ID_TOKEN = 312;
	 		
	 		public static final String PATH_FOR_JOIN = "Booking_BookingTime";
	 		public static final int PATH_FOR_JOIN_TOKEN = 313;
	 		public static final Uri JOIN_URI = BASE_URI.buildUpon().appendPath(PATH_FOR_JOIN).build();
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.bookingtime";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.bookingtime";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String TIMEID = "timeid";
	 			public static final String BID = "bookingid";
	 			public static final String ARRIVAL = "arrival"; //yyyyMMdd
	 			public static final String RID = "resourceid";
	 		}
	 	}
	 	
	 	public static class Bookingtype {
	 		public static final String NAME = "bookingtype";
	 		public static final String PATH = "Bookingtype";
	 		public static final int PATH_TOKEN = 320;
	 		public static final String PATH_FOR_ID = "Bookingtype/*";
	 		public static final int PATH_FOR_ID_TOKEN = 330;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.bookingtype";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.bookingtype";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String BTID = "bookingtypeid";
	 			public static final String NAME = "name";
	 			public static final String PRICE = "price";
	 			public static final String VALIDFROM = "validfrom";
	 			public static final String VALIDTO = "validto";
	 			public static final String EXTERNAL = "externalname";
	 			public static final String LASTUPDATE = "lastupdate";
	 			
	 			public static final String HISTORY = "history";
	 		}
	 	}
	 	
	 	public static class Resource {
	 		public static final String NAME = "resource";
	 		public static final String PATH = "Resource";
	 		public static final int PATH_TOKEN = 340;
	 		public static final String PATH_FOR_ID= "Resource/*";
	 		public static final int PATH_FOR_ID_TOKEN = 350;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.resource";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.resource";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String NAME = "name";
	 			public static final String CID = "companyid";
	 			public static final String RTID = "resourcetypeid"; //
	 			public static final String RTNAME = "typename";
	 			public static final String PERIOD = "period";
	 			public static final String LASTUPDATE = "lastupdate";
	 			
	 			//added v120
	 			public static final String HISTORY = "historic";
	 			
	 		}
	 	}
	 	
	 	// Only used internally.
	 	public static class Company {
	 		public static final String NAME = "company";
	 		public static final String PATH = "Company";
	 		public static final int PATH_TOKEN = 50;
	 		public static final String PATH_FOR_ID = "Company/*";
	 		public static final int PATH_FOR_ID_TOKEN = 55;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.company";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.company";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID;
	 			public static final String NAME = "name";
	 			public static final String TE_USERNAME = "te_username";
	 			public static final String SCHEMAVERSION = "schemaversion";
	 			public static final String LASTUPDATE = "lastupdate";
	 			public static final String TE_PASSWORD = "te_password";
	 			public static final String WEB_URL = "web_url";
	 			public static final String NAMEORDER = "name_order";
	 		}
	 	}
	 	// for creating member ships ?
	 	public static class Programme {
	 		public static final String NAME = "programme";
	 		public static final String PATH = "Programme";
	 		public static final int PATH_TOKEN = 360;
	 		public static final String PATH_FOR_ID = "Programme/*";
	 		public static final int PATH_FOR_ID_TOKEN = 370;
	 		public static final String PATH_FOR_GROUP = "ProgrammeGroup";
	 		public static final int PATH_FOR_GROUP_TOKEN = 371;
	 		
	 		public static final Uri GROUP_URI = BASE_URI.buildUpon().appendPath(PATH_FOR_GROUP).build();
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.programme";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.programme";
	 		
	 		public static class Indexs {
	 			public static final String PROGRAMME_ID = "programme_programme_id";
	 			public static final String GROUP_ID = "programme_group_id";
	 		}
	 		
	 		public static class Cols implements BaseColumns{
	 			public static final String PID = "programmeid";
	 			public static final String GID = "groupid";
	 			public static final String NAME = "name";
	 			public static final String GNAME = "groupname";
	 			public static final String SDATE = "startdate";
	 			public static final String EDATE = "enddate";
	 			public static final String PRICE = "price";
	 			public static final String MLENGTH = "mlength";
	 			public static final String SIGNUP = "signupfee";
	 			public static final String NOTE = "pricenotes";
	 			public static final String LASTUPDATE = "lastupdate";
	 			public static final String PRICE_DESC ="price_desc";
	 			public static final String CONCESSION = "concession";
	 		}
	 	}
	 	
	 	//for storing actual memberships ?
	 	public static class Membership {
	 		public static final String NAME = "membership";
	 		public static final String PATH = "Membership";
	 		public static final int PATH_TOKEN = 120;
	 		public static final String PATH_FOR_ID = "Membership/*";
	 		public static final int PATH_FOR_ID_TOKEN = 125;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.membership";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.membership";
	 		
	 		public static class Indexs {
	 			public static final String MEMBER_ID = "member_id";
	 			public static final String MEMBERSHIP_ID = "membership_id";
	 		}
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "membership_insert";
	 			public static final String ON_UPDATE_MSID = "membershipid_update";
	 			public static final String ON_UPDATE = "membership_update";
	 			public static final String ON_DELETE = "membership_delete"; 
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String MID = "memberID";
	 			public static final String MSID="membershipid";
	       	 	public static final String PNAME="pname";
	       	 	public static final String MSSTART="msstart"; 
	       	 	public static final String EXPIRERY="msexpiry";
	       	 	public static final String VISITS="msvisits"; //concession
	 			//if membership table is used for checking cards then: see pi cache;
	 			public static final String CARDNO = "cardno";
	 			public static final String DENY = "deny"; //MOVE THIS TO A SEPERATE TABLE
	 			public static final String LASTUPDATE = "lastupdate";
	 			public static final String PRIMARYMS = "primarymembership";
	 			public static final String PID = "programmeid";
	 			public static final String PGID ="programmegroupid";
	 			public static final String PRICE = "price";
	 			public static final String SIGNUP = "signupfee";
	 			public static final String CREATION = "creationdate";
	 			public static final String DEVICESIGNUP = "devicesignup";
	 			
	 			//ADDED in V118
	 			public static final String TERMINATION_DATE = "termination_date";
	 			public static final String CANCEL_REASON = "cancel_reason";
	 			public static final String STATE = "state";
	 			public static final String HISTORY = "history";
	 			
	 			//ADDED in V119
	 			public static final String PAYMENTDUE = "paymentdue";
	 			public static final String NEXTPAYMENT = "nextpayment";
	 			public static final String FIRSTPAYMENT = "firstpayment";
	 			public static final String UPFRONT = "upfront";
	 			
	 		}
	 	}
	 	
	 	public static class Swipe {
	 		public static final String NAME = "swipe";
	 		public static final String PATH = "Swipe";
	 		public static final int PATH_TOKEN = 220;
	 		public static final String PATH_FOR_ID = "Swipe/*";
	 		public static final int PATH_FOR_ID_TOKEN = 230;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.swipe";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.swipe";
	 		
	 		public static class Cols {
	 			public static final String ID = BaseColumns._ID; //cardno
	 			public static final String DOOR = "door";
	 			public static final String DATETIME = "datetime";
	 			public static final String LASTUPDATE = "lastupdate";
	 		}
	 	}
	 	
	 	public static class ResultStatus {
	 		public static final String NAME = "resultstatus";
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
	 			public static final String LASTUPDATE = "lastupdate";
	 		}
	 	}
	 	
	 	public static class OpenTime {
	 		public static final String NAME = "opentime";
	 		public static final String PATH = "OpenTime";
	 		public static final int PATH_TOKEN = 91;
	 		public static final String PATH_FOR_ID = "OpenTime/*";
	 		public static final int PATH_FOR_ID_TOKEN = 92;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.opentime";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.opentime";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DAYOFWEEK = "dayofweek";
	 			public static final String OPENTIME = "opentime";
	 			public static final String OPENID = "openid";
	 			public static final String CLOSETIME = "closetime";
	 			public static final String CLOSEID = "closeid";
	 			public static final String NAME = "name"; //monday tuesday etc
	 			public static final String LASTUPDATE = "lastupdate";
	 		}
	 	}
	 	
	 	public static class Date {
	 		public static final String NAME = "dates";
	 		public static final String PATH = "Dates";
	 		public static final int PATH_TOKEN = 93;
	 		public static final String PATH_FOR_ID = "Dates/*";
	 		public static final int PATH_FOR_ID_TOKEN = 94;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.date";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.date";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DATE = "dates"; //'s' probably because other table has date ?
	 			public static final String DAYOFWEEK = "dayofweek";
	 		}
	 	}
	 	
	 	public static class Class {
	 		public static final String NAME = "class";
	 		public static final String PATH = "Class";
	 		public static final int PATH_TOKEN = 321;
	 		public static final String PATH_FOR_ID = "Class/*";
	 		public static final int PATH_FOR_ID_TOKEN = 322;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.class";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.class";
	 		
	 		public static class Indexs {
	 			public static final String CLASS_ID = "class_class_id";
	 			public static final String RESOURCE_ID = "class_resource_id";
	 		}
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "class_insert";
	 			public static final String ON_UPDATE_CID = "classid_update";
	 			public static final String ON_DELETE = "class_delete"; //unused
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String CID = "cid";
	 			public static final String NAME = "name";
	 			public static final String DESC = "description";
	 			public static final String MAX_ST = "max_students";
	 			public static final String ONLINE = "online_booking";
	 			//public static final String PRICE = "price";
	 			/***********Creation/booking stuff***********/
	 			public static final String FREQ = "frequency";
	 			public static final String SDATE = "startdate";
	 			public static final String STIME = "starttime";
	 			public static final String ETIME = "endtime";
	 			public static final String RID = "resourceid";
	 			public static final String LASTUPDATE = "lastupdate";
	 			public static final String DEVICESIGNUP = "devicesignup";
	 		}
	 	}
	 	
	 	public static class MembershipSuspend {
	 		public static final String NAME = "membershipSuspend";
	 		public static final String PATH = "MembershipSuspend";
	 		public static final int PATH_TOKEN = 126;
	 		public static final String PATH_FOR_ID = "MembershipSuspend/*";
	 		public static final int PATH_FOR_ID_TOKEN = 127;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.membershipsuspend";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.membershipsuspend";
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "suspend_insert";
	 			public static final String ON_UPDATE_SID = "suspendid_update";
	 			public static final String ON_UPDATE = "suspend_update";
	 			public static final String ON_DELETE = "suspend_delete"; //unused
	 		}
	 		//referenced here for historical reasons.
	 		public static class Old {
	 			public static final String NAME = "membership_suspend";
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String SID = "suspendid";
	 			public static final String STARTDATE = "start";
	 			public static final String LENGTH = "length";
	 			public static final String REASON = "reason";
	 			public static final String MID = "memberid";
	 			public static final String FREEZE = "freeze_fees";
	 			public static final String ENDDATE = "enddate";
	 			public static final String DEVICESIGNUP = "devicesignup";
	 			
	 			//Added v119.
	 			public static final String SUSPENDCOST = "suspendcost";
	 			public static final String ONEOFFFEE = "oneofffee";
	 			public static final String ALLOWENTRY = "allowentry";
	 			public static final String EXTEND_MEMBERSHIP = "extend_membership";
	 			public static final String PROMOTION = "promotion";
	 			public static final String FULLCOST = "fullcost";
	 			public static final String HOLDFEE = "holdfee";	
	 			public static final String PRORATA = "prorata";
	 			
	 			public static final String ORDER = "orderdate";
	 		}
	 	}
	 	
	 	public static class IdCard {
	 		public static final String NAME = "idcard";
	 		public static final String PATH = "idcard";
	 		public static final int PATH_TOKEN = 400;
	 		public static final String PATH_FOR_ID = "idcard/*";
	 		public static final int PATH_FOR_ID_TOKEN = 401;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.idcard";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.idcard";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String CARDID = "cardid";
	 			public static final String SERIAL = "serial";
	 			public static final String CREATED = "created";
	 		}
	 	}
	 	
	 	public static class PaymentMethod {
	 		public static final String NAME = "paymentmethod";
	 		public static final String PATH = "paymentmethod";
	 		public static final int PATH_TOKEN = 60;
	 		public static final String PATH_FOR_ID = "paymentmethod/*";
	 		public static final int PATH_FOR_ID_TOKEN = 61;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.paymentmethod";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.paymentmethod";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String PAYMENTID = "paymentid";
	 			public static final String NAME = "paymentname";
	 		}
	 	}
	 	
	 	public static class Door {
	 		public static final String NAME = "door";
	 		public static final String PATH = "door";
	 		public static final int PATH_TOKEN = 62;
	 		public static final String PATH_FOR_ID = "door/*";
	 		public static final int PATH_FOR_ID_TOKEN = 63;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.door";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.door";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DOORID = "doorid";
	 			public static final String DOORNAME = "doorname";
	 		}
	 	}
	 	
	 	public static class MemberNotes {
	 		public static final String NAME = "membernotes";
	 		public static final String PATH  = "membernotes";
	 		public static final int PATH_TOKEN = 111;
	 		public static final String PATH_FOR_ID = "membernotes/*";
	 		public static final int PATH_FOR_ID_TOKEN = 112;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.membernotes";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.membernotes";
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "note_insert";
	 			public static final String ON_UPDATE_MNID = "noteid_update";
	 			public static final String ON_DELETE = "note_delete"; //unused
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String MNID = "membernoteid";
	 			public static final String MID = "memberid";
	 			public static final String OCCURRED = "occurred";
	 			public static final String NOTES = "notes";
	 			public static final String DEVICESIGNUP = "devicesignup";
	 			public static final String UPDATEUSER = "update_user_name";
	 		}
	 	}
	 	
	 	public static class MemberBalance {
	 		public static final String NAME = "memberbalance";
	 		public static final String PATH = "memberbalance";
	 		public static final int PATH_TOKEN = 113;
	 		public static final String PATH_FOR_ID = "memberbalance/*";
	 		public static final int PATH_FOR_ID_TOKEN = 114;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.memberbalance";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.memberbalance";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String MID = "memberid";
	 			public static final String BALANCE = "balance";
	 			public static final String LASTUPDATE = "lastupdate";
	 		}
	 	}
	 	
	 	//TODO: is this used?
	 	public static class DeletedRecords {
	 		
	 		public static final String PATH = "DeletedRecords";
	 		public static final int PATH_TOKEN = 5;
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		
	 		public static final String TABLENAME = "tablename";
	 		public static final String ROWID = "rowid";
	 	}

	 	
	 	//TODO: move more uploads here:
	 	//		-Swipe
	 	public static class PendingUploads {
	 		public static final String NAME = "PendingUploads";
	 		public static final String PATH = "PendingUploads";
	 		public static final int PATH_TOKEN = 12;
	 		public static final String PATH_FOR_ID = "PendingUploads/*";
	 		public static final int PATH_FOR_ID_TOKEN = 13;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.pendinguploads";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.pendinguploads";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 			//timestamp, probably not neccissary.
	 		}
	 	}
	 	
	 	//used to redownload a members info after it's been uploaded.
	 	public static class PendingDownloads {
	 		public static final String NAME = "PendingDownloads";
	 		public static final String PATH = "PendingDownloads";
	 		public static final int PATH_TOKEN = 14;
	 		public static final String PATH_FOR_ID = "PendingDownloads/*";
	 		public static final int PATH_FOR_ID_TOKEN = 15;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.pendingdownloads";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.pendingdownloads";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 		}
	 	}
	 	
	 	public static class PendingUpdates {
	 		public static final String NAME = "PendingUpdates";
	 		public static final String PATH = "PendingUpdates";
	 		public static final int PATH_TOKEN = 16;
	 		public static final String PATH_FOR_ID = "PendingUpdates/*";
	 		public static final int PATH_FOR_ID_TOKEN = 17;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.pendingupdates";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.pendingupdates";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 		}
	 	}
	 	
	 	//PendingDeletes this is currently unused.
	 	public static class PendingDeletes {
	 		public static final String NAME = "PendingDeletes";
	 		public static final String PATH = "PendingDeletes";
	 		public static final int PATH_TOKEN = 18;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.pendingdeletes";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.pendingdeletes";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 		}
	 	}
	 	
	 	
	 	//**************************************************
	 	/* Currently we get and store ids from the Postgres database for use when we upload new items (members, memberships)
	 	 * etc. These Id's are stored in there respective tables. However retrieving them is convulted. 
	 	 * Solution is to move them here, using the tableIndex # as the tableid, and changing all the relevent
	 	 * code (ALOT!).
	 	 */
	 	public static class FreeIds {
	 		public static final String NAME = "FreeIds";
	 		public static final String PATH = "FreeIds";
	 		public static final int PATH_TOKEN = 20;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.freeids";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.freeids";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 		}
	 	}
	 	
	 	public static class TableIndex {
	 		public static final String NAME = "tableindex";
	 		public static final String PATH = "TableIndex";
	 		public static final int PATH_TOKEN = 10;
	 		public static final String PATH_FOR_ID = "TableIndex/*";
	 		public static final int PATH_FOR_ID_TOKEN = 11;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.tableindex";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.tableindex";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String NAME = "name";
	 		}
	 		/**
	 		 * key (id's) used for referencing each table-name.
	 		 * @author callum
	 		 */
	 		public static enum Values {
	 			Booking(1), Class(2), Swipe(3),Membership(4) /*when adding memberships*/,
	 					Member(5) /*when adding members/prospects*/,Image(6),
	 					MembershipSuspend(7), MemberNotes(8), RollCall(9), RollItem(10),
	 					Idcard(11), Prospect(12);
	 			
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
	 	
	 	public static class RollCall {
	 		public static final String NAME = "roll";
	 		public static final String PATH = "roll";
	 		public static final int PATH_TOKEN = 601;
	 		
	 		public static final String PATH_FOR_ID = "roll/*";
	 		public static final int PATH_FOR_ID_TOKEN = 602;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.roll";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.roll";
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "roll_insert";
	 			public static final String ON_UPDATE_RID = "rollid_update";
	 			public static final String ON_DELETE = "roll_delete"; //unused
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DATETIME = "datetime"; //timestamp?
	 			public static final String NAME = "name";
	 			public static final String ROLLID = "rollid";
	 			public static final String DEVICESIGNUP = "devicesignup";
	 		}
	 	}
	 	
	 	public static class RollItem {
	 		public static final String NAME = "roll_item";
	 		public static final String PATH = "roll_item";
	 		public static final int PATH_TOKEN = 603;
	 		
	 		public static final String CREATE_ROLL_PATH = "createroll";
	 		public static final int CREATE_ROLL_TOKEN = 604;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.rollitem";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.rollitem";
	 		
	 		public static class Triggers {
	 			public static final String ON_INSERT = "roll_item_insert";
	 			public static final String ON_UPDATE = "roll_item_update";
	 			public static final String ON_UPDATE_RIID = "roll_item_id_update";
	 			public static final String ON_DELETE = "roll_item_delete"; //unused
	 		}
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROLLID = "rollid"; //reference the roll.
	 			public static final String MEMBERID = "memberid"; //reference member
	 			public static final String ATTENDED = "attended"; //boolean
	 			public static final String ROLLITEMID = "rollitemid";
	 			public static final String DEVICESIGNUP = "devicesignup";
	 			
	 			//aggregate column name, not an actual column!:
	 			public static final String TOTAL = "total";
	 		}
	 	}
	 	
	 	public static class MembershipExpiryReason {
	 		public static final String NAME = "membership_expiry_reason";
	 		public static final String PATH = "membership_expiry_reason";
	 		public static final int PATH_TOKEN = 128;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.membership_expiry_reason";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.membership_expiry_reason";
	 		
	 		//don't need any triggers. we can't actually change any of this info from the device.
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ID = "expiry_reason_id";
	 			public static final String NAME = "name";
	 		}
	 	}
	 	
	 	//we temporarily store selected fees here while we wait on the upload.
	 	public static class CancellationFee {
	 		public static final String NAME = "cancellation_fee";
	 		public static final String PATH = "cancellation_fee";
	 		public static final int PATH_TOKEN = 121;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.cancellation_fee";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.cancellation_fee";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String MEMBERSHIPID = "membershipid";
	 			public static final String FEE = "fee";
	 		}
	 	}
	 	
	 	public static class KPI {
	 		public static final String NAME = "key_performance_index";
	 		public static final String PATH = "key_performance_index";
	 		public static final int PATH_TOKEN =  56;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.key_performance_index";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.key_performance_index";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String METRIC = "metric";
	 			public static final String VALUE = "value";
	 			public static final String LASTUPDATE = "lastupdate";
	 			//what other columns do we need ?
	 		}
	 		
	 	}
	 	
	 	public static class MemberFinance {
	 		public static final String NAME = "member_finance";
	 		public static final String PATH = "member_finance";
	 		public static final int PATH_TOKEN = 410;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.member_finance";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.member_finance";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid"; //this is either payment.id or debitjournal.id
	 			public static final String MEMBERID = "memberid";
	 			public static final String MEMBERSHIPID = "membershipid";
	 			public static final String OCCURRED = "occurred";
	 			public static final String CREATED = "created";
	 			public static final String LASTUPDATE = "lastupdate";
	 			public static final String CREDIT = "credit";
	 			public static final String DEBIT = "debit";
	 			public static final String ORIGIN = "origin";
	 			public static final String NOTE  = "note";
	 			public static final String DD_EXPORT_MEMBERID = "dd_export_memberid";
	 		}
	 	}
	 	
	 	public static class BillingHistory {
	 		public static final String NAME = "billing_history";
	 		public static final String PATH = "billing_history";
	 		public static final int PATH_TOKEN = 411;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.billing_history";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.billing_history";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ID = "id";
	 			public static final String MEMBERID = "memberid";
	 			public static final String FAILED = "failed";
	 			public static final String AMOUNT = "amount";
	 			public static final String STATUS = "status";
	 			public static final String LASTUPDATE = "lastupdate";
	 			public static final String NOTE = "note";
	 			public static final String DDEXPORTID = "dd_export_id";
	 			public static final String PROCESSDATE = "processdate";
	 			public static final String FAILREASON = "failreason";
	 			public static final String PAIDBYOTHER = "paidbyother";
	 			public static final String DISHONOURED = "dishonoured";
	 			public static final String RUNNINGTOTAL = "runningtotal";
	 		}
	 	}
	 	
	 	public static class DDExportFormat {
	 		public static final String NAME = "dd_export_format";
	 		public static final String PATH = "dd_export_format";
	 		public static final int PATH_TOKEN = 405;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.dd_export_format";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.dd_export_format";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ID = "id";
	 			public static final String NAME = "name";
	 			public static final String FILENAMESQL = "filenamesql";
	 			public static final String SQLFUNC = "sqlfunc";
	 			public static final String EXPORTTYPE = "exporttype";
	 		}
	 	}
	 	
	 	public static class AppConfig {
	 		public static final String NAME = "app_config";
	 		public static final String PATH = "app_config";
	 		public static final int PATH_TOKEN = 9;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.app_config";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.app_config";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String DB_DEVICEID = "deviceid";
	 			public static final String DB_TIMEOFFSET = "timeoffset";
	 		}
	 	}
	 	
	 	/**
	 	 * TODO: this needs finished.
	 	 * @author callum
	 	 *
	 	 */
	 	public static class PaymentAgainst { //this isn't active yet I don't think..?
	 		public static final String NAME = "payment_against";
	 		public static final String PATH = "payment_against";
	 		public static final int PATH_TOKEN = 412;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.payment_against";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.payment_against";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ID = "id";
	 			public static final String PAYMENTID = "paymentid";
	 			public static final String DEBITJOURNALID = "debitjournalid";
	 			public static final String AMOUNT = "amount";
	 			public static final String VOIDAMOUNT = "voidamount";
	 			public static final String CREATED = "created";
	 		}
	 	}
	 	
	 	/**
	 	 * Going to put this table in the "too hard basket" for the time being.
	 	 * There doesn't seem to be an easy way to get the details of sms/email without
	 	 * writing a custom frame/window for sending them via.
	 	 * 
	 	 * may do it at some point in the future.
	 	 * 
	 	 * @author callum
	 	 *
	 	 */
	 	public static class CorrespondanceSent {
	 		public static final String NAME = "correspondance_sent";
	 		public static final String PATH  = "correspondance_sent";
	 		public static final int  PATH_TOKEN = 413;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.correspondance_sent";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.correspondance_sent";
	 		
	 		public static class Cols implements BaseColumns {
	 			
	 		}
	 	}
	 	
	 	/**
	 	 * TODO: 	add handling to insert into this table when we get SQL errors/etc.
	 	 * 			we can re use the magical form builder (that's still to be created) for it.
	 	 * @author callum
	 	 *
	 	 */
	 	public static class PendingConflicts {
	 		public static final String NAME = "PendingConflicts";
	 		public static final String PATH = "PendingConflicts";
	 		public static final int PATH_TOKEN = 22;
	 		
	 		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
	 		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.treshna.pending_conflicts";
	 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.treshna.pending_conflicts";
	 		
	 		public static class Cols implements BaseColumns {
	 			public static final String ROWID = "rowid";
	 			public static final String TABLEID = "tableid";
	 			public static final String ERROR = "error"; //an error message to show to the user?
	 		}
	 	}
	 	
	 	
}
