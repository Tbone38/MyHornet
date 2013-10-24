/**
 * 
 */
package com.treshna.hornet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import android.util.Log;

/**
 * @author callum
 * This class uses JDBC to connect to a server.
 * Username, Password, Address, and Database all need to be set
 * prior to calling the open connection method.
 * 
 * This class should also be called in a seperate thread, as android doesn't like
 * networking in the main thread (and will force close it if you try.)
 */
public class JDBCConnection {

    private String Type = "PostgreSQL", Username = "", Password = "", Address = "", Port = "5432";
    private String Database = "";
    //TODO: set default username & pw = gymmaster/7urb0
    private Connection con = null;
    private Statement statement;
    private PreparedStatement pStatement;
    private static final String TAG = "com.treshna.hornet.JDBCConnection";
    
    private String getConnectionUrl() {
            return new String("jdbc:postgresql://" + Address + ":" + Port + "/" + Database);
    }
    
    public String error = null;
    public int errorLevel = 0;
    //private static int OK = 0;
    private static int WARN = 1;
    private static int SERR = 2;
    //private static int USERR = 3;
    
    public JDBCConnection(String address, String port, String database, String username, 
    		String password) {
    	this.Address = address;
    	this.Port = port;
    	this.Database = database;
    	this.Username = username;
    	this.Password = password;
    }
    public JDBCConnection(String address, String database, String username, 
    		String password) {
    	this(address, "5432", database, username, password);
    }

    public synchronized void openConnection() throws ClassNotFoundException, SQLException {
            if (con != null) {
                    try {
                        con.close();
                    } catch (Exception e) {
                    	error = "error occured closing connection, continuing";
                    	errorLevel = WARN;
                    } finally {
                        con = null;
                    }
            }
           
            Properties properties = new Properties();
            properties.put("user", Username);
            properties.put("password", Password);
           
            if (Type.compareTo("PostgreSQL") == 0) {
            	Class.forName("org.postgresql.Driver");
            }
            //System.out.println("Start Connection");
            Log.v(TAG, "Starting Connection");
            con = DriverManager.getConnection(getConnectionUrl(), properties);
    }
    
    public void closeConnection(){
    	 if (con != null) {
    		 try {
            	//System.out.println("Closing Connection");
    			 Log.v(TAG, "Closing Connection");
            	con.close();
             } catch (SQLException e) {
               //shouldn't occur, if it does not a concern.
             } finally {
                con = null;
             }
    	 }
    }
    
    public int insertImage(byte[] image, int rowId, Date date, String description, boolean isProfile) throws SQLException{
    	int result = -1;
 
    		pStatement = con.prepareStatement("insert INTO image (imagedata, memberid, lastupdated, created, description, is_profile ) VALUES ('1|'||encode(?,'base64'),?, ?, ?, ?, ?)");
    		pStatement.setBytes(1, image);
    		pStatement.setInt(2,rowId);
    		pStatement.setDate(3, new java.sql.Date(date.getTime()));
    		pStatement.setTimestamp(4, new Timestamp(date.getTime()));
    		pStatement.setString(5, description);
    		pStatement.setBoolean(6, isProfile);
    		result = pStatement.executeUpdate();
    	
    	return result;
    }
	/*
	 * At some point these will need changed to handle is_profile and description.
	 */
    public int deleteImage(int memberId, Date created) throws SQLException{
    	
    	int result = -1;
    	if (memberId == -1 || created == null) return result;
	    	pStatement = con.prepareStatement("DELETE FROM image WHERE memberid = ? AND created = ?");
	    	Timestamp theTimestamp = new Timestamp(created.getTime());
	    	pStatement.setTimestamp(2, theTimestamp);
	    	pStatement.setInt(1, memberId);
	    	result = pStatement.executeUpdate();
    	
    	return result;
    }
    
    public ResultSet tagInsert(int door, String cardid) throws SQLException{
    	ResultSet result = null;
    	
	    	pStatement = con.prepareStatement("select * from swipe(?, ?, true);");
	    	pStatement.setInt(1, door);
	    	pStatement.setString(2, cardid);
	    	result = pStatement.executeQuery();
    	
    	return result;
    }
    
    public ResultSet getTagUpdate(int door) throws SQLException{
    	ResultSet result = null;
    		pStatement = con.prepareStatement("select * from doormsg where doorid = ?;");
    		pStatement.setInt(1,  door);
    		result = pStatement.executeQuery();
    	
    	return result;
    }
    
    public ResultSet imageCount(int rowId) throws SQLException{
    	ResultSet rs = null;
    		pStatement = con.prepareStatement("select lastupdated, created from image where memberid = ?");
    		pStatement.setInt(1, rowId);
    		rs = pStatement.executeQuery();
    	return rs;
    }
    
    public int addMember(int id, String surname, String firstname, String gender, String email, String dob,
    		String street, String suburb, String city, String postal, String hphone, String cphone, String medical) throws SQLException{
    	
		pStatement = con.prepareStatement("INSERT INTO member ( surname, firstname, gender, email, dob, "
				+"addressstreet, addresssuburb, addresscity, addressareacode, " 
				+"phonehome, phonecell, medicalconditions, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");			
		
		
		pStatement.setString(1, surname);
		pStatement.setString(2, firstname);
		pStatement.setString(3, gender);
		pStatement.setString(4, email);
		SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date date = null;
		try {
			date = input.parse(dob);
		} catch (ParseException e) {
			//date issue, shouldn't occur.
			e.printStackTrace();
		}
		pStatement.setDate(5, new java.sql.Date(date.getTime()));
		pStatement.setString(6, street);
		pStatement.setString(7, suburb);
		pStatement.setString(8, city);
		pStatement.setString(9, postal);
		pStatement.setString(10, hphone);
		pStatement.setString(11, cphone);
		pStatement.setString(12, medical);
		pStatement.setInt(13, id);
		
		return pStatement.executeUpdate();
		
    }
    
    public int addProspect(String surname, String firstname, String gender, String email, String dob,
    		String street, String suburb, String city, String postal, String hphone, String cphone, String medical){

    	try {
			
    		pStatement = con.prepareStatement("INSERT INTO enquiry ( surname, firstname, gender, email, dob, "
					+"addressstreet, addresssuburb, addresscity, addressareacode, " 
					+"phonehome, phonecell, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");			
			
    		
			pStatement.setString(1, surname);
			pStatement.setString(2, firstname);
			pStatement.setString(3, gender);
			pStatement.setString(4, email);
			SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			Date date = null;
			try {
				date = input.parse(dob);
			} catch (ParseException e) {
				//date issue, shouldn't occur.
			}
			pStatement.setDate(5, new java.sql.Date(date.getTime()));
			pStatement.setString(6, street);
			pStatement.setString(7, suburb);
			pStatement.setString(8, city);
			pStatement.setString(9, postal);
			pStatement.setString(10, hphone);
			pStatement.setString(11, cphone);
			String notes = "Medical Condition: "+medical.substring(0);
			pStatement.setString(12, notes);
			
			return pStatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = "error occured adding Prospect, prospect was not added.";
    		errorLevel = SERR;
    		return 0;
		}
    }
    public ResultSet getResource() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("select resource.id as resourceid, resource.name as resourcename, "
    			+ "resource.companyid as resourcecompanyid, resourcetype.name as resourcetypename, "
    			+ "resourcetype.period as resourcetypeperiod FROM resource LEFT JOIN resourcetype"
    			+" ON (resource.resourcetypeid = resourcetype.id) WHERE resource.history = 'f';");
    	rs = pStatement.executeQuery();
    	return rs;
    }
    public ResultSet getCompany() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("select id, name FROM company_config;");
    	rs = pStatement.executeQuery();
    	return rs;
    }
    
    public ResultSet getBookings(java.sql.Date yesterday, java.sql.Date tomorrow, int resourceid, long last_sync) throws SQLException{    	
    	
    	ResultSet rs = null;
    	if (last_sync > 0) {
    		//System.out.print("\n\nGetting Bookings with update After "+new java.sql.Date(last_sync));
    		Log.v(TAG, "Getting Bookings with update Afrer "+new java.sql.Timestamp(last_sync));
	    	pStatement = con.prepareStatement("SELECT resourceid, booking.firstname, booking.surname, "
	    			+"CASE WHEN bookingtype.externalname IS NOT NULL THEN bookingtype.externalname ELSE bookingtype.name END AS bookingname, "
	    			+"booking.startid, booking.endid, booking.arrival, booking.id AS bookingid, bookingtype.id AS bookingtypeid, booking.endtime, booking.notes, booking.result, "
	    			+"booking.memberid, booking.lastupdate AS bookinglastupdate, booking.membershipid, booking.checkin, "
	    			+ "booking.classname, booking.classid, booking.parentid FROM booking "
	    			+"LEFT JOIN bookingtype ON (booking.bookingtypeid = bookingtype.id) "
	    			+"WHERE booking.arrival BETWEEN ?::date AND ?::date AND booking.resourceid = ? AND booking.lastupdate > ? ORDER BY booking.id DESC;");
	    	// removing resourceid break things ?
	    	pStatement.setDate(1, yesterday);
	    	pStatement.setDate(2, tomorrow);
	    	pStatement.setInt(3, resourceid);
	    	pStatement.setDate(4, new java.sql.Date(last_sync));
    	} else {
    		pStatement = con.prepareStatement("SELECT resourceid, booking.firstname, booking.surname, "
	    			+"CASE WHEN bookingtype.externalname IS NOT NULL THEN bookingtype.externalname ELSE bookingtype.name END AS bookingname, "
	    			+"booking.startid, booking.endid, booking.arrival, booking.id AS bookingid, bookingtype.id AS bookingtypeid, booking.endtime, booking.notes, booking.result, "
	    			+"booking.memberid, booking.lastupdate AS bookinglastupdate, booking.membershipid, booking.checkin,"
	    			+ " booking.classname, booking.classid, booking.parentid FROM booking "
	    			+"LEFT JOIN bookingtype ON (booking.bookingtypeid = bookingtype.id) "
	    			+"WHERE booking.arrival BETWEEN ?::date AND ?::date AND booking.resourceid = ? ORDER BY booking.id DESC;"); // AND booking.resourceid = ?
    		
    		pStatement.setDate(1, yesterday);
        	pStatement.setDate(2, tomorrow);
        	pStatement.setInt(3, resourceid);
    	}
    	
    	
    	rs = pStatement.executeQuery();
    	return rs;
    	
    }
    
    public int updateBookings(int bookingID, int resultstatus, String notes, long lastupdate, int bookingtypeid, long checkin) throws SQLException {
    	int result = 0;
    	
    	pStatement = con.prepareStatement("UPDATE booking SET (result, notes, bookingtypeid, lastupdate, checkin) = (?,?,?,?, ?)" +
    			" WHERE id = ?");
    	pStatement.setInt(1, resultstatus);
    	pStatement.setString(2, notes);
    	pStatement.setInt(3, bookingtypeid);
    	pStatement.setDate(4, new java.sql.Date(lastupdate));
    	if (checkin <= 0) { //TODO: check this works.
    		pStatement.setNull(5, java.sql.Types.TIMESTAMP);
    	} else {
    		//pStatement.setDate(5, new java.sql.Date(checkin));
    		pStatement.setTimestamp(5, new java.sql.Timestamp(checkin));
    		Log.v(TAG, "updating Booking "+bookingID+", with checkin time: "+new java.sql.Date(checkin));
    	}
    	pStatement.setInt(6, bookingID);
    	
    	result = pStatement.executeUpdate();
    	
    	return result;
    }
    
    public ResultSet getBookingTypesValid() throws SQLException{ //this is for CACI ?
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("select id, name, price, validfrom, validto, externalname from bookingtype;");
    	rs = pStatement.executeQuery();
    	
    	return rs;
    }
    
    public ResultSet getBookingTypes() throws SQLException{
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("select id, name, price, externalname from bookingtype;");
    	rs = pStatement.executeQuery();
    	
    	return rs;
    }
    
    public ResultSet getResultStatus() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("SELECT id, name, bgcolour FROM resultstatus;");
    	rs = pStatement.executeQuery();
    	
    	return rs;
    }
    
    //being used for bookings, TODO: last-visitors as well; ?
    public ResultSet getMembers() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("SELECT id, member.firstname, member.surname, " //get_name
    			+"CASE WHEN member.happiness = 1 THEN ':)' WHEN member.happiness = 0 THEN ':|'"
    			+" WHEN member.happiness <= -1 THEN ':(' WHEN member.happiness = 2 THEN '||' ELSE '' END AS happiness, "
    			+"member.phonehome AS mphhome, member.phonework AS mphwork, member.phonecell AS mphcell, "
    			+"member.email AS memail, member.notes AS mnotes, member.status FROM member"
    			+" WHERE status != 3;");
    	
    	rs = pStatement.executeQuery();
    	return rs;
    }
    
    public ResultSet getOpenHours() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("SELECT dayofweek, opentime, closetime, name FROM opentime;");
    	rs = pStatement.executeQuery();
    	return rs;
    }
    
    public ResultSet getClasses() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("SELECT id, name, max_students,  description, price, onlinebook FROM class;");
    	rs = pStatement.executeQuery();
    	return rs;
    }
    
    public ResultSet getTimeInterval() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("SELECT period FROM resourcetype;");
    	rs = pStatement.executeQuery();
    	return rs;
    }
    
    public ResultSet getMembership() throws SQLException {
    	ResultSet rs = null;
    	pStatement = con.prepareStatement("SELECT membership.id, memberid, membership.startdate, membership.enddate, cardno, membership.notes, " +
    			"primarymembership, membership.lastupdate, " +
    			" membership.concession, programme.name FROM membership LEFT JOIN programme ON (membership.programmeid = programme.id)" +
    			" WHERE membership.enddate >= now()::date ;");
    	rs = pStatement.executeQuery();
    	return rs;
    }
    /**
     * This function does not pass in the last-update date from the device,
     * which should mean the device will re-download the booking data once it's been uploaded.
     * @param booking Map &#60;String Key, String value&#62; = used for getting the booking details to upload.
     * @return insert count
     * @throws SQLException
     */
	//TODO: does this need parentid added?
    public int uploadBookings(Map<String, String> booking) throws SQLException {
    	//ResultSet rs = null;
    	pStatement = con.prepareStatement("INSERT INTO booking (id, memberid, resourceid, arrival, startid, starttime, "
    			+"bookingtypeid, firstname, surname, result, membershipid, notes, endtime, endid, lastupdate) " +
    			"VALUES (?,?,?,?::DATE,?::TIME WITHOUT TIME ZONE,?::TIME WITHOUT TIME ZONE,?,?,?,?, ?, ?, " +
    			"(?::TIME WITHOUT TIME ZONE), (?::TIME WITHOUT TIME ZONE - ?::TIME WITHOUT TIME ZONE), ?);");    	
    	
    	pStatement.setInt(1, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.BID)));
    	pStatement.setInt(2, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.MID)));
    	pStatement.setInt(3, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.RID)));
    	pStatement.setString(4, booking.get(ContentDescriptor.Booking.Cols.ARRIVAL));
    	pStatement.setString(5, booking.get(ContentDescriptor.Booking.Cols.STIME));
    	pStatement.setString(6, booking.get(ContentDescriptor.Booking.Cols.STIME));
    	pStatement.setInt(7, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.BOOKINGTYPE)));
    	pStatement.setString(8, booking.get(ContentDescriptor.Booking.Cols.FNAME));
    	pStatement.setString(9, booking.get(ContentDescriptor.Booking.Cols.SNAME));
    	pStatement.setInt(10, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.RESULT)));
    	if (booking.get(ContentDescriptor.Booking.Cols.MSID) == null || booking.get(ContentDescriptor.Booking.Cols.MSID).compareTo("null")
    			== 0) {
    		//no membershipID
    		pStatement.setNull(11, java.sql.Types.INTEGER);
    	} else {
    		pStatement.setInt(11, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.MSID)));
    	}
    	pStatement.setString(12, booking.get(ContentDescriptor.Booking.Cols.NOTES));
    	
    	pStatement.setString(13, booking.get(ContentDescriptor.Booking.Cols.ETIME));
    	//pStatement.setString(14, booking.get(ContentDescriptor.Booking.Cols.OFFSET)); if uncommenting: +1 the below numbers 
       	pStatement.setString(14, booking.get(ContentDescriptor.Booking.Cols.ETIME));
    	pStatement.setString(15, booking.get(ContentDescriptor.Booking.Cols.OFFSET));
    	//todo last-updated
    	pStatement.setDate(16, new java.sql.Date(Long.valueOf(booking.get(ContentDescriptor.Booking.Cols.LASTUPDATED))));
    	
    	Log.v(TAG, "Upload Bookings Query:"+pStatement.toString());
    	return pStatement.executeUpdate();
    }
    
    public ResultSet uploadClass(String name, int max_students) throws SQLException {
    	ResultSet rs;
    	
    	pStatement = con.prepareStatement("INSERT INTO class (name, max_students) VALUES (?, ?) RETURNING id;");
    	pStatement.setString(1, name);
    	pStatement.setInt(2, max_students);
    	
    	rs = pStatement.executeQuery();
    	return rs;
    }
    
    public int uploadRecurrence(String freq, String startdate, String starttime, String endtime, int cid, int rid)
    	throws SQLException{
    	String enddate;
    	SimpleDateFormat format;
    	
    	if (freq == null) {
    		//not recurring, set enddate to startdate + 1; ?
    		Calendar cal;
    		Date date = null;
    		
    		cal = Calendar.getInstance();
    		format = new SimpleDateFormat("yyyyMMdd", Locale.US);
    		try {
				date = format.parse(startdate);
			} catch (ParseException e) {}
    		cal.setTime(date);
    		cal.add(Calendar.DATE, 1);
    		enddate = Services.dateFormat(cal.getTime().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "yyyy-MM-dd");
    		freq = "0 day";
    	} else {
    		enddate = null;
    	}
    	
    	pStatement = con.prepareStatement("INSERT INTO recurrence (freq, startdate, enddate, starttime, endtime,"
    			+ "resourceid, classid ) VALUES (?::INTERVAL, ?::DATE, ?::DATE, ?::TIME WITHOUT TIME ZONE, "
    			+ "?::TIME WITHOUT TIME ZONE, ?::INTEGER , ? );");
    	
    	pStatement.setString(1, freq);
    	pStatement.setString(2, Services.dateFormat(startdate, "yyyyMMdd", "yyyy-MM-dd"));
    	if (enddate == null) {
    		pStatement.setNull(3, java.sql.Types.DATE);
    	} else {
    		pStatement.setString(3, enddate);
    	}
    	pStatement.setString(4, starttime);
    	pStatement.setString(5, endtime);
    	if (rid < 0) {
    		pStatement.setNull(6, java.sql.Types.INTEGER);
    	} else {
    		pStatement.setInt(6, rid);
    	}
    	pStatement.setInt(7, cid);
    	
    	return pStatement.executeUpdate();
    }
    
    public ResultSet findCardBySerial(String serial) throws SQLException {
    	ResultSet rs;
    	
    	pStatement = con.prepareStatement("SELECT id FROM idcard WHERE serial = ?;");
    	pStatement.setString(1, serial);
    	Log.v(TAG, pStatement.toString());
    	rs = pStatement.executeQuery();
    	
    	return rs;
    }
    
    public ResultSet findMemberByCard(int cardno) throws SQLException {
    	ResultSet rs;
    	
    	pStatement = con.prepareStatement("select id as membershipid, memberid from membership where cardno = ?");
    	pStatement.setInt(1, cardno);
    	Log.v(TAG, pStatement.toString());
    	rs = pStatement.executeQuery();
    	
    	return rs;
    }
    
    
    public int uploadSuspend(String sid, String mid, String msid, String startdate, String duration, 
    		String reason, String freeze) throws SQLException {
    	
    	pStatement = con.prepareStatement("INSERT INTO membership_suspend (id, startdate, howlong, reason, "
    			+ "memberid, freeze_fees) VALUES (?, ?::DATE, ?::INTERVAL, ?, ?, ?);");
    	pStatement.setInt(1, Integer.decode(sid));
    	pStatement.setString(2, Services.dateFormat(startdate, "yyyyMMdd", "yyyy-MM-dd"));
    	pStatement.setString(3, duration);
    	pStatement.setString(4, reason);
    	pStatement.setInt(5, Integer.decode(mid));
    	if (Integer.decode(freeze) == 1) {
    		pStatement.setBoolean(6, true);
    	} else {
    		pStatement.setBoolean(6, false);
    	}
    	
    	pStatement.executeUpdate();
    	this.closePreparedStatement();
    	
    	pStatement = con.prepareStatement("UPDATE membership SET suspendid = ? WHERE id = ? ;");
    	pStatement.setInt(1, Integer.decode(sid));
    	pStatement.setInt(2, Integer.decode(msid));
    	
    	return pStatement.executeUpdate();
    }
    
    
    public ResultSet startStatementQuery(String query) throws SQLException {
    	ResultSet rs = null;
	    	statement = con.createStatement();
	    	rs = statement.executeQuery(query);
		return rs;
    }
    /**
     * Open statements must be closed before server can be queried by that sort of statement again.
     */
    public void closeStatementQuery() {
    	if (statement != null) {
	    	try {
	    		statement.close();
	    	} catch(SQLException e) {
	    		//e.printStackTrace();
	    	} finally {
	    		statement = null;
	    	}
    	}
    }
    /**
     * Open statements must be closed before server can be queried by that sort of statement again.
     */
    public void closePreparedStatement(){
    	if (pStatement != null) {
	    	try{
	    		pStatement.close();
	    	}catch(SQLException e){
	    		//e.printStackTrace();
	    	} finally {
	    		pStatement = null;
	    	}
    	}
    }
}
