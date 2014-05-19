/**
 * 
 */
package com.treshna.hornet.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.postgresql.util.PSQLException;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

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
    // Hard-code?
    private Connection con = null;
    private Statement statement;
    private PreparedStatement pStatement;
    //private static final String TAG = "JDBCConnection";
    private static String TAG = "HORNETSERVICE";
    private Context ctx; //applicationContext(), DO NOT USE activityContext()
    
    private String getConnectionUrl() {
    	if (Address.isEmpty()||Database.isEmpty()|| Port.isEmpty() && ctx != null) {
    		Address = Services.getAppSettings(ctx, "address");
    		Database = Services.getAppSettings(ctx, "database");
    		Port = Services.getAppSettings(ctx, "port");
    	}
            return new String("jdbc:postgresql://" + Address + ":" + Port + "/" + Database);
    }
    
    public String error = null;
    public int errorLevel = 0;
    private static int SERR = 2;
    
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
    
    public JDBCConnection(Context context) {
    	this.ctx = context;
    }

    public synchronized void openConnection() throws ClassNotFoundException, SQLException, PSQLException {
            if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                    	Log.w(TAG, "error occured closing connection, continuing");
                    } finally {
                        con = null;
                    }
            }
            if (Username.isEmpty()||Password.isEmpty() && ctx != null) {
            	Username = Services.getAppSettings(ctx, "username");
            	Password = Services.getAppSettings(ctx, "password");
            }
            Properties properties = new Properties();
            properties.put("user", Username);
            properties.put("password", Password);
           
            if (Type.compareTo("PostgreSQL") == 0) {
            	Class.forName("org.postgresql.Driver");
            }
            
            Log.v(TAG, "Starting Connection");
            con = DriverManager.getConnection(getConnectionUrl(), properties);            
    }
    
    public boolean isConnected() {
    	boolean result = false;
    	try {
    		result = !con.isClosed();
    	} catch (SQLException e) {
    		result = false;
    	} catch (NullPointerException e) {
    		result = false;
    	}
    	return result;
    	
    }
    
    public void closeConnection(){
    	 if (con != null) {
    		 try {
    			 Log.v(TAG, "Closing Connection");
            	con.close();
             } catch (SQLException e) {
               //shouldn't occur, if it does not a concern.
             } finally {
                con = null;
             }
    	 }
    }
    
    public int getMaxImageId() throws SQLException {
    	ResultSet rs;
    	int max = -1;

    	pStatement = con.prepareStatement("SELECT max(id) AS max FROM image;");
    	rs = pStatement.executeQuery();
    	rs.next();
    	max = rs.getInt("max");
    	rs.close();

    	return max;
    }
    
    public ResultSet getImages(long lastsync, int lastrow) throws SQLException {
    	String query = "SELECT decode(substring(imagedata from 3),'base64') AS imagedata, memberid, lastupdate, description, is_profile, "
				+ "created, id FROM IMAGE where substring(imagedata,1,2) = '1|' AND memberid NOT IN (SELECT id FROM member WHERE status = 3)"
				+ "AND lastupdate > ? ";
    	
    	if (lastrow > 0) {
    		query = query+"AND id > ? ORDER BY id ASC LIMIT 100;";
    	}
    	
    	pStatement = con.prepareStatement(query); //get 200 at a time?
    	
    	//AND id >= ? AND id < (?+200)"
    	pStatement.setTimestamp(1, new Timestamp(lastsync));
    	if (lastrow > 0 ) {
	    	pStatement.setInt(2, lastrow);
    	}

    	return pStatement.executeQuery();
    }
    
    public int uploadImage( byte[] image, int memberId, long date, String description, boolean isProfile, int rowid) throws SQLException, NullPointerException {
			pStatement = con.prepareStatement("INSERT INTO image (imagedata, memberid, lastupdate, created, description, is_profile, id"
					+ ") VALUES ('1|'||encode(?,'base64'), ?, ?, ?, ?, ?, ?);");
			pStatement.setBytes(1, image);
			pStatement.setInt(2, memberId);
			pStatement.setTimestamp(3, new Timestamp(date));
			pStatement.setTimestamp(4, new Timestamp(date));
			pStatement.setString(5, description);
			pStatement.setBoolean(6, isProfile);
			pStatement.setInt(7, rowid);
			
			return pStatement.executeUpdate();
    }
    
   public int deleteImage(int memberId, Date created) throws SQLException, NullPointerException{
	    	int result = -1;
	    	if (memberId == -1 || created == null) return result;
		    	pStatement = con.prepareStatement("DELETE FROM image WHERE memberid = ? AND created = ?");
		    	Timestamp theTimestamp = new Timestamp(created.getTime());
		    	pStatement.setTimestamp(2, theTimestamp);
		    	pStatement.setInt(1, memberId);
		    	result = pStatement.executeUpdate();
	    	
	    	return result;
    }
   
   public int updateImage( boolean is_profile, String description, int imageid) throws SQLException {
	   //what if con is null?!
	   pStatement = con.prepareStatement("UPDATE IMAGE SET (is_profile, description) = (?, ?) WHERE id = ?;");
	   
	   pStatement.setBoolean(1, is_profile);
	   pStatement.setString(2, description);
	   pStatement.setInt(3, imageid);
	   
	   return pStatement.executeUpdate();
   }
    
    public ResultSet uploadTag(int door, String serial) throws SQLException, NullPointerException{
	    	ResultSet result = null;
    	
	    	pStatement = con.prepareStatement("select * from swipe(?, ?, true);");
	    	pStatement.setInt(1, door);
	    	pStatement.setString(2, serial);
	    	result = pStatement.executeQuery();
    	
	    	return result;
    }
    
    public void getSwipeProcessLog() throws SQLException {
    	pStatement = con.prepareStatement("SELECT swipe_processlog(NULL::INTEGER);");
    	
    	pStatement.execute();
    }
    
    public ResultSet getTagUpdate(int door) throws SQLException, NullPointerException{
			ResultSet result = null;
			pStatement = con.prepareStatement("select * from doormsg where doorid = ? ORDER BY id DESC;");
			pStatement.setInt(1,  door);
			result = pStatement.executeQuery();
		
			return result;
    }
    
    public ResultSet imageCount(int rowId) throws SQLException, NullPointerException{
	    	ResultSet rs = null;
			pStatement = con.prepareStatement("select lastupdated, created from image where memberid = ?");
			pStatement.setInt(1, rowId);
			rs = pStatement.executeQuery();
			return rs;
    }
    
    public int uploadMember(int id, String surname, String firstname, String gender, String email, String dob,
    		String street, String suburb, String city, String postal, String hphone, String cphone, String medical) throws SQLException, NullPointerException{

			pStatement = con.prepareStatement("INSERT INTO member ( surname, firstname, gender, email, dob, "
					+"addressstreet, addresssuburb, addresscity, addressareacode, " 
					+"phonehome, phonecell, medicalconditions, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");			
			
			
			pStatement.setString(1, surname);
			pStatement.setString(2, firstname);
			pStatement.setString(3, gender);
			pStatement.setString(4, email);
			SimpleDateFormat input = new SimpleDateFormat("dd MMM yyyy", Locale.US);
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
    
    public ResultSet getResource(long last_sync) throws SQLException, NullPointerException {
	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("select resource.id as resourceid, resource.name as resourcename, "
	    			+ "resource.companyid as resourcecompanyid, resourcetype.name as resourcetypename, "
	    			+ "resourcetype.period as resourcetypeperiod , history "
	    			+ "FROM resource LEFT JOIN resourcetype"
	    			+" ON (resource.resourcetypeid = resourcetype.id) WHERE resource.lastupdate > ?;");
	    	pStatement.setTimestamp(1, new Timestamp(last_sync));
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    
    public ResultSet getCompanyConfig() throws SQLException, NullPointerException {
    		//TODO: web_url te_password
	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("select name, te_username, schemaversion, te_password, web_url, name_order FROM company_config, config LIMIT 1;");
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public ResultSet getBookings(java.sql.Date yesterday, java.sql.Date tomorrow, long last_sync) throws SQLException, NullPointerException{
	    	ResultSet rs = null;

			Log.v(TAG, "Getting Bookings with update After "+new java.sql.Timestamp(last_sync));
	    	pStatement = con.prepareStatement("SELECT resourceid, booking.firstname, booking.surname, "
	    			+"CASE WHEN bookingtype.externalname IS NOT NULL THEN bookingtype.externalname ELSE bookingtype.name END AS bookingname, "
	    			+"booking.startid, booking.endid, EXTRACT(epoch FROM booking.arrival) AS arrival, "
	    			+ "booking.id AS bookingid, bookingtype.id AS bookingtypeid, booking.endtime, booking.notes, booking.result, "
	    			+"booking.memberid, booking.lastupdate AS bookinglastupdate, booking.membershipid, booking.checkin, "
	    			+ "booking.classname, booking.classid, booking.parentid FROM booking "
	    			+"LEFT JOIN bookingtype ON (booking.bookingtypeid = bookingtype.id) "
	    			+"WHERE booking.arrival BETWEEN ?::date AND ?::date AND booking.lastupdate > ? ORDER BY booking.id DESC;");
	    	// removing resourceid break things ?
		    	pStatement.setDate(1, yesterday);
		    	pStatement.setDate(2, tomorrow);
		    	pStatement.setDate(3, new java.sql.Date(last_sync));    	
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public int updateBookings(int bookingID, int resultstatus, String notes, long lastupdate, int bookingtypeid, long checkin,
    			long arrival, String starttime, String endtime, String offset) throws SQLException, NullPointerException {
	    	int result = 0;
	    	pStatement = con.prepareStatement("UPDATE booking SET (result, notes, bookingtypeid, lastupdate, checkin, arrival, startid,"
	    			+ "starttime, endid, endtime) = (?,?,?,?, ?, ?, ?::TIME WITHOUT TIME ZONE, ?::TIME WITHOUT TIME ZONE,"
	    			+ "(?::TIME WITHOUT TIME ZONE - ?::INTERVAL)::TIME WITHOUT TIME ZONE, ?::TIME WITHOUT TIME ZONE)" +
	    			" WHERE id = ?");
	    	pStatement.setInt(1, resultstatus);
	    	pStatement.setString(2, notes);
	    	pStatement.setInt(3, bookingtypeid);
	    	pStatement.setDate(4, new java.sql.Date(lastupdate));
	    	if (checkin <= 0) { 
	    		pStatement.setNull(5, java.sql.Types.TIMESTAMP);
	    	} else {
	    		//pStatement.setDate(5, new java.sql.Date(checkin));
	    		pStatement.setTimestamp(5, new java.sql.Timestamp(checkin));
	    		Log.v(TAG, "updating Booking "+bookingID+", with checkin time: "+new java.sql.Date(checkin));
	    	}
	    	
	    	pStatement.setDate(6, new java.sql.Date(arrival));
	    	pStatement.setString(7, starttime);
	    	pStatement.setString(8, starttime);
	    	pStatement.setString(9, endtime);
	    	pStatement.setString(10, offset);
	    	pStatement.setString(11, endtime);
	    	
	    	pStatement.setInt(12, bookingID);
	    	
	    	
	    	Log.d(TAG, pStatement.toString());
	    	result = pStatement.executeUpdate();
	    	
	    	return result;
    }
    
    public ResultSet getBookingTypesValid() throws SQLException, NullPointerException{ //this is for CACI ?
	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("select id, name, price, validfrom, validto, externalname from bookingtype;");
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;

    }
    
    public ResultSet getBookingTypes(long last_sync) throws SQLException, NullPointerException{

	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("select id, name, price, externalname, history, lastupdate, length, maxintbetween, onlinebook, "
	    			+ "msh_onlybook, description from bookingtype WHERE "
	    			+ "lastupdate > ?;");
	    	
	    	pStatement.setTimestamp(1, new java.sql.Timestamp(last_sync));
	    	
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;

    }
    
    public ResultSet getResultStatus() throws SQLException, NullPointerException {

	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("SELECT id, name, bgcolour FROM resultstatus;");
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;
    }
    
    public final String membersQuery  = "SELECT id, member.firstname, member.surname, " //get_name
			+"CASE WHEN member.happiness = 1 THEN ':)' WHEN member.happiness = 0 THEN ':|'"
			+" WHEN member.happiness <= -1 THEN ':(' WHEN member.happiness = 2 THEN '||' ELSE '' END AS happiness, "
			+"member.phonehome AS mphhome, member.phonework AS mphwork, member.phonecell AS mphcell, "
			+"member.email AS memail, member.notes AS mnotes, member.status, member.cardno, member.gender, "
			+"emergencyname, emergencyhome, emergencywork, emergencycell, emergencyrelationship, "
			+"medication, medicationdosage, medicationbystaff, medicalconditions, "
			+ "dob, addressstreet, addresssuburb, addresscity, addressareacode, addresscountry, billingactive, dd_export_formatid, "
			+ "gender "
			+ "FROM member"
			;
    
    public ResultSet getMembers(String lastupdate) throws SQLException, NullPointerException {

	    	ResultSet rs = null;
	    	String query = membersQuery+" WHERE status != 3";
	    	if (lastupdate != null) {
	    		query = query + " AND lastupdate > ?::TIMESTAMP WITHOUT TIME ZONE";
	    	}
	    	pStatement = con.prepareStatement(query);
	    	
	    	if (lastupdate != null) {
	    		pStatement.setString(1, Services.dateFormat(new Date(Long.parseLong(lastupdate)).toString(),
	    				"EEE MMM dd HH:mm:ss zzz yyyy", "dd-MMM-yyyy HH:mm:ss"));
	    	}
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public final String YMCAMembersQuery = "SELECT id, member.firstname, member.surname, " //get_name
			+"CASE WHEN member.happiness = 1 THEN ':)' WHEN member.happiness = 0 THEN ':|'"
			+" WHEN member.happiness <= -1 THEN ':(' WHEN member.happiness = 2 THEN '||' ELSE '' END AS happiness, "
			+"member.phonehome AS mphhome, member.phonework AS mphwork, member.phonecell AS mphcell, "
			+"member.email AS memail, member.notes AS mnotes, member.status, member.cardno, member.gender, "
			+"emergencyname, emergencyhome, emergencywork, emergencycell, emergencyrelationship, "
			+"medication, medicationdosage, medicationbystaff, medicalconditions, parentname, "
			+ "dob, addressstreet, addresssuburb, addresscity, addressareacode, addresscountry, billingactive, dd_export_formatid, "
			+ "gender "
			+ "FROM member"
			; 
    
    public ResultSet getYMCAMembers(String lastupdate) throws SQLException, NullPointerException {

	    	ResultSet rs = null;
	    	String query = YMCAMembersQuery;
	    	query = query + " WHERE id IN (SELECT DISTINCT memberid FROM membership "
					+ "WHERE programmeid IN (SELECT id FROM programme WHERE history = false "
					+ "AND programmegroupid = 0) GROUP BY memberid) AND status != 3";
	    	if (lastupdate != null) {
	    		query = query + " AND lastupdate > ?::TIMESTAMP WITHOUT TIME ZONE";
	    	}
	    	pStatement = con.prepareStatement(query);
	    	
	    	if (lastupdate != null) {
	    		pStatement.setString(1, Services.dateFormat(new Date(Long.parseLong(lastupdate)).toString(),
	    				"EEE MMM dd HH:mm:ss zzz yyyy", "dd-MMM-yyyy HH:mm:ss"));
	    	}
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public int updateMember(String cardno, String street, String suburb, String city, String areacode, String gender, String dob,
    		String memberid, String phcell, String phhome, String phwork, String email, String emergency_name, String emergency_relationship,
    		String emergency_cell, String emergency_home, String emergency_work, String medical, String medication, String medicationdosage) 
    		throws SQLException {
    	
    	String query = "UPDATE member SET (cardno, addressstreet, addresssuburb, addresscity, addressareacode, gender, dob, phonecell, "
    			+ "phonehome, phonework, email, emergencyname, emergencyrelationship, emergencyhome, emergencywork, emergencycell, "
    			+ "medicalconditions, medication, medicationdosage) = (?, ?, ?, ?, ?, ?, ?::DATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
    			+ "WHERE id = ?;";
    	pStatement = con.prepareStatement(query);
    	if (cardno != null) {
    		pStatement.setInt(1, Integer.valueOf(cardno));
    	} else {
    		pStatement.setNull(1, java.sql.Types.INTEGER);
    	}
    	if (street != null) {
    		pStatement.setString(2, street);
    	} else {
    		pStatement.setNull(2, java.sql.Types.VARCHAR);
    	}
    	if (suburb != null) {
    		pStatement.setString(3, suburb);
    	} else {
    		pStatement.setNull(3, java.sql.Types.VARCHAR);
    	}
    	if (city != null) {
    		pStatement.setString(4, city);
    	} else {
    		pStatement.setNull(4, java.sql.Types.VARCHAR);
    	}
    	if (areacode != null) {
    		pStatement.setString(5, areacode);
    	} else {
    		pStatement.setNull(5, java.sql.Types.VARCHAR);
    	}
    	if (gender != null) {
    		pStatement.setString(6, gender);
    	} else {
    		pStatement.setNull(6, java.sql.Types.CHAR);
    	}
    	if (dob != null) {
    		pStatement.setString(7, dob);
    	} else {
    		pStatement.setNull(7, java.sql.Types.DATE);
    	}
    	if (phcell != null) {
    		pStatement.setString(8, phcell);
    	} else {
    		pStatement.setNull(8, java.sql.Types.VARCHAR);
    	}
    	if (phhome != null) {
    		pStatement.setString(9, phhome);
    	} else {
    		pStatement.setNull(9, java.sql.Types.VARCHAR);
    	}
    	if (phwork != null) {
    		pStatement.setString(10, phwork);
    	} else {
    		pStatement.setNull(10, java.sql.Types.VARCHAR);
    	}
    	if (email != null) {
    		pStatement.setString(11, email);
    	} else {
    		pStatement.setNull(11, java.sql.Types.VARCHAR);
    	}
    	if (emergency_name != null) {
    		pStatement.setString(12, emergency_name);
    	} else {
    		pStatement.setNull(12, java.sql.Types.VARCHAR);
    	}
    	if (emergency_relationship != null) {
    		pStatement.setString(13, emergency_relationship);
    	} else {
    		pStatement.setNull(13, java.sql.Types.VARCHAR);
    	}
    	if (emergency_home != null) {
    		pStatement.setString(14, emergency_home);
    	} else {
    		pStatement.setNull(14, java.sql.Types.VARCHAR);
    	}
    	if (emergency_work != null) {
    		pStatement.setString(15, emergency_work);
    	} else {
    		pStatement.setNull(15, java.sql.Types.VARCHAR);
    	}
    	if (emergency_cell != null) {
    		pStatement.setString(16, emergency_cell);
    	} else {
    		pStatement.setNull(16, java.sql.Types.VARCHAR);
    	}
    	if (medical != null) {
    		pStatement.setString(17, medical);
    	} else {
    		pStatement.setNull(17, java.sql.Types.VARCHAR);
    	}
    	if (medication != null) {
    		pStatement.setString(18, medication);
    	} else {
    		pStatement.setNull(18, java.sql.Types.VARCHAR);
    	}
    	if (medicationdosage != null) {
    		pStatement.setString(19, medicationdosage);
    	} else {
    		pStatement.setNull(19, java.sql.Types.VARCHAR);
    	}
    	if (memberid != null) {
    		pStatement.setInt(20, Integer.parseInt(memberid));
    	} else {
    		return 0;
    	}
    	
    	return pStatement.executeUpdate();
    }
    
    
    /*public int updateRow(ArrayList<String[]> values, String tablename, String where) throws SQLException, NullPointerException {
	    	if (where == null || where.isEmpty()) {
	    		return 0;
	    	}
	    	//find a way to make this easier to read.
	    	String set = "UPDATE "+tablename+" SET (";
	    	String value = "(";
	    	for (int i = 0; i < values.size(); i++) {
				set = set + values.get(i)[0]; //column name
				value = value + values.get(i)[1]; //value
				if (i == (values.size() -1)) {
					set = set +") = ";
					value = value+" ) WHERE ";
				} else {
					set = set +", ";
					value = value+", ";
				}
			}
	    	
	    	String query = set+value+where;
	    	pStatement = con.prepareStatement(query);
	    	return pStatement.executeUpdate();
    }*/
    
    public ResultSet getOpenHours(long last_sync) throws SQLException, NullPointerException {
	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("SELECT dayofweek, opentime, closetime, name, lastupdate FROM opentime WHERE lastupdate > ?;");
	    	pStatement.setTimestamp(1, new java.sql.Timestamp(last_sync));
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public ResultSet getClasses(String lastsync) throws SQLException, NullPointerException {
	    	ResultSet rs = null;
	    	String query = "SELECT id, name, max_students,  description, price, onlinebook, multiplebookings FROM class "
	    	+" WHERE lastupdate > ?";
	   
	    	Date lastupdate = new Date(Long.valueOf(lastsync));
	    	Log.d(TAG, "Classes Last-Update:"+lastupdate);
	    	pStatement = con.prepareStatement(query);
	    	/*pStatement.setString(1, Services.dateFormat(lastupdate.toString(),
	    				"EEE MMM dd HH:mm:ss zzz yyyy", "dd MMM yyyy HH:mm:ss"));*/
	    	pStatement.setTimestamp(1, new java.sql.Timestamp(Long.parseLong(lastsync)));
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    	
    }
    
    public ResultSet getTimeInterval() throws SQLException, NullPointerException { //shouldn't this have a where-clause?
	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("SELECT period FROM resourcetype;");
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public ResultSet getMembership(String lastsync, int ymca) throws SQLException, NullPointerException {
	    	ResultSet rs = null;
	    	String query ="SELECT membership.id, memberid, membership.startdate, membership.enddate, cardno, membership.notes, " +
	    			"primarymembership, membership.lastupdate, membership_state(membership.*, programme.*) as state," +
	    			" membership.concession, programme.name, programme.id AS programmeid, membership.termination_date,";
	    	
	    	String check_query = "SELECT TRUE from pg_proc WHERE proname = 'select_column' "
	    			+ "UNION SELECT false FROM pg_proc WHERE 'select_column' NOT IN (SELECT proname FROM pg_proc);";
	    	pStatement = con.prepareStatement(check_query);
	    	rs = pStatement.executeQuery();
	    	
	    	if (rs.next() && rs.getBoolean(1)) { //check's if the select_column function even exists.
	    			query = query+ "select_column('membership','cancel_reason', membership.*) AS cancel_reason,";
	    	} else {
	    		query = query+ "NULL AS cancel_reason,";
	    	}
	    	rs.close();
	    	this.closePreparedStatement();
	    	
	    	query = query+ " membership.history, membership.signupfee, "
	    			+ "membership.paymentdue::TEXT||' '::TEXT||price_desc(programme.amount, programme.id) AS paymentdue, membership.nextpayment,"
	    			+ " membership.firstpayment, membership.upfront"
	    			+ " FROM membership LEFT JOIN programme ON (membership.programmeid = programme.id)" +
	    			" WHERE 1=1 ";
	    	if (ymca > 0) { //ADD YMCA HANDLING.
	    		query = query + "AND memberid IN (SELECT DISTINCT memberid FROM membership "
	    				+ "WHERE programmeid IN (SELECT id FROM programme WHERE history = false "
	    				+ "AND programmegroupid = 0) GROUP BY memberid) ";
	    	}
	    	if (lastsync != null) {
	    		//query = query + "AND membership.lastupdate > ?::TIMESTAMP WITHOUT TIME ZONE ;";
	    		query = query + "AND membership.lastupdate > ?;";
	    	} else {
	    		query = query + "AND membership.history = 'f'";
	    	}
	    	
	    	pStatement = con.prepareStatement(query);
	    	if (lastsync != null) {
	    		/*pStatement.setString(1, Services.dateFormat(new Date(Long.parseLong(lastsync)).toString(),
	    				"EEE MMM dd HH:mm:ss zzz yyyy", "dd-MMM-yyyy HH:mm:ss"));*/
	    		pStatement.setTimestamp(1, new java.sql.Timestamp(Long.parseLong(lastsync)));
	    	}
	    	
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
    public int uploadBookings(Map<String, String> booking) throws SQLException, NullPointerException {
    	//ResultSet rs = null;
    	pStatement = con.prepareStatement("INSERT INTO booking (id, memberid, resourceid, arrival, startid, starttime, "
    			+"bookingtypeid, firstname, surname, result, membershipid, notes, endtime, endid, lastupdate, parentid, classid) " +
    			"VALUES (?,?,?,?::DATE,?::TIME WITHOUT TIME ZONE,?::TIME WITHOUT TIME ZONE,?,?,?,?, ?, ?, " +
    			"(?::TIME WITHOUT TIME ZONE), (?::TIME WITHOUT TIME ZONE - ?::INTERVAL)::TIME WITHOUT TIME ZONE, ?, ?, ?);");
    			//"(?::TIME WITHOUT TIME ZONE), (?::TIME WITHOUT TIME ZONE - ?::TIME WITHOUT TIME ZONE), ?, ?, ?);");    	
    	
    	pStatement.setInt(1, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.BID)));
    	pStatement.setInt(2, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.MID)));
    	pStatement.setInt(3, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.RID)));
    	pStatement.setString(4, booking.get(ContentDescriptor.Booking.Cols.ARRIVAL));
    	pStatement.setString(5, booking.get(ContentDescriptor.Booking.Cols.STIME));
    	pStatement.setString(6, booking.get(ContentDescriptor.Booking.Cols.STIME));
    	if (booking.get(ContentDescriptor.Booking.Cols.BOOKINGTYPE) == null || 
    			booking.get(ContentDescriptor.Booking.Cols.BOOKINGTYPE).compareTo("null")==0) {
    		pStatement.setNull(7, java.sql.Types.INTEGER);
    	} else {
    		pStatement.setInt(7, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.BOOKINGTYPE)));
    	}
    	
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
       	pStatement.setString(14, booking.get(ContentDescriptor.Booking.Cols.ETIME));
    	pStatement.setString(15, booking.get(ContentDescriptor.Booking.Cols.OFFSET));

    	pStatement.setTimestamp(16, new java.sql.Timestamp(Long.valueOf(booking.get(ContentDescriptor.Booking.Cols.LASTUPDATE))));
    	
    	if (Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.PARENTID))<= 0 ) {
    		pStatement.setNull(17, java.sql.Types.INTEGER);
    	} else {
    		pStatement.setInt(17, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.PARENTID)));
    	}
    	
    	if (booking.get(ContentDescriptor.Booking.Cols.CLASSID) == null) {
    		pStatement.setNull(18, java.sql.Types.INTEGER);
    	} else {
    		pStatement.setInt(18, Integer.parseInt(booking.get(ContentDescriptor.Booking.Cols.CLASSID)));
    	}
    	
    	Log.v(TAG, "Upload Bookings Query:"+pStatement.toString());
    	return pStatement.executeUpdate();
    }
    
    public ResultSet uploadClass(String name, int max_students) throws SQLException, NullPointerException {
	    	ResultSet rs;
	    	
	    	pStatement = con.prepareStatement("INSERT INTO class (name, max_students) VALUES (?, ?) RETURNING id;");
	    	pStatement.setString(1, name);
	    	pStatement.setInt(2, max_students);
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public int uploadRecurrence(String freq, String startdate, String starttime, String endtime, int cid, int rid)
    		throws SQLException, NullPointerException{
    	
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
	    		freq = "1 day";
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
	    	Log.v(TAG, "SQL:"+pStatement.toString());
	    	pStatement.executeUpdate();
	    	
	    	return 0;
    }
    /**
     * DEPRECATED! use the internal idcard table instead.
     * @param serial
     * @return
     * @throws SQLException
     */
    /*public ResultSet findCardBySerial(String serial) throws SQLException {
    	ResultSet rs;
    	
    	pStatement = con.prepareStatement("SELECT id FROM idcard WHERE serial = ?;");
    	pStatement.setString(1, serial);
    	Log.v(TAG, pStatement.toString());
    	rs = pStatement.executeQuery();
    	
    	return rs;
    }*/
    
    public ResultSet getMemberByCard(int cardno) throws SQLException, NullPointerException {
	    	ResultSet rs;
	    	
	    	pStatement = con.prepareStatement("select id as membershipid, memberid from membership where cardno = ?");
	    	pStatement.setInt(1, cardno);
	    	Log.v(TAG, pStatement.toString());
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;
    }
    
    
    public ResultSet getSuspends(long last_sync) throws SQLException , NullPointerException{
	    	pStatement = con.prepareStatement("SELECT membership_suspend.id, memberid, membership_suspend.startdate,"
	    			+ "howlong, membership_suspend.reason, CASE WHEN enddate IS NULL THEN (startdate+howlong)::date "
	    			+ "ELSE enddate END AS edate, "
	    			+ "suspendcost, oneofffee, allowentry, extend_membership, freeze_fees, "
	    			+ "promotion, fullcost, holdfee, prorata "
	    			+ "FROM membership_suspend LEFT JOIN member ON "
	    			+ "(membership_suspend.memberid = member.id) "
	    			+ "WHERE member.status != 3 AND membership_suspend.created >= ?::TIMESTAMP WITHOUT TIME ZONE;");
	    	
	    	pStatement.setString(1, Services.dateFormat(new Date(last_sync).toString(), 
					"EEE MMM dd HH:mm:ss zzz yyyy", "dd-MMM-yyyy HH:mm:ss"));
	    	
	    	return pStatement.executeQuery();
    }
    
    public int uploadSuspend(String sid, String mid, String msid, String startdate, String enddate, 
    		String reason, String freeze, String suspendcost, String oneofffee, String allowentry, String extend_membership,
    		String promotion, String fullcost, String holdfee, String prorata) throws SQLException, NullPointerException {
	    	
	    	pStatement = con.prepareStatement("INSERT INTO membership_suspend (id, startdate, howlong, reason, "
	    			+ "memberid, freeze_fees, enddate, suspendcost, oneofffee, allowentry, "
	    			+ "extend_membership, promotion, fullcost, holdfee, prorata) "
	    			+ "VALUES (?, ?::DATE, AGE(?::DATE, ?::DATE)::INTERVAL, ?, ?, ?::BOOLEAN, ?::date, ?::money, ?::money,"
	    			+ "?::BOOLEAN, ?::BOOLEAN, ?::BOOLEAN, ?::BOOLEAN, ?, ?::BOOLEAN);");
	    	pStatement.setInt(1, Integer.decode(sid));
	    	//pStatement.setString(2, Services.dateFormat(startdate, "yyyyMMdd", "yyyy-MMM-dd"));
	    	pStatement.setString(2, startdate);
	    	pStatement.setString(3, enddate);
	    	pStatement.setString(4, startdate);
	    	pStatement.setString(5, reason);
	    	pStatement.setInt(6, Integer.decode(mid));
    		pStatement.setString(7, freeze);
	    	//pStatement.setString(7, Services.dateFormat(startdate, "yyyyMMdd", "yyyy-MMM-dd"));
    		pStatement.setString(8, enddate);
	    	pStatement.setString(9, suspendcost);
	    	pStatement.setString(10, oneofffee);
	    	pStatement.setString(11, allowentry);
	    	pStatement.setString(12, extend_membership);
	    	pStatement.setString(13, promotion);
	    	pStatement.setString(14, fullcost);
	    	if (holdfee == null) {
	    		pStatement.setNull(15, java.sql.Types.VARCHAR);
	    	} else {
	    		pStatement.setString(15, holdfee);
	    	}
	    	pStatement.setString(16, prorata);
	    	
	    	return pStatement.executeUpdate();
	    	
	    	//DOES THE BELOW NEED TO HAPPEN?
	    	/*this.closePreparedStatement();
	    	
	    	pStatement = con.prepareStatement("UPDATE membership SET suspendid = ? WHERE id = ? ;");
	    	pStatement.setInt(1, Integer.decode(sid));
	    	pStatement.setInt(2, Integer.decode(msid));
	    	
	    	return pStatement.executeUpdate();*/
    }
    
    public int updateSuspend(int memberid, String startdate, String enddate, String reason, String freeze_fees, String suspendcost,
    		String oneofffee, String allowentry, String extend_membership, String promotion, String fullcost, String holdfee,
    		String prorata, int sid) throws SQLException {
    	
    	pStatement = con.prepareStatement("UPDATE membership_suspend SET (memberid, startdate, enddate, howlong, "
    			+ "reason, freeze_fees, suspendcost, oneofffee, allowentry, extend_membership, promotion, fullcost, "
    			+ "holdfee, prorata) = (?, ?::DATE, ?::DATE, AGE(?::DATE, ?::DATE)::INTERVAL, ?, ?::BOOLEAN,"
    			+ "?::MONEY, ?::MONEY, ?::BOOLEAN, ?::BOOLEAN, ?::BOOLEAN, ?::BOOLEAN, ?, ?::BOOLEAN) WHERE "
    			+ "id = ?;");
    	pStatement.setInt(1, memberid);
    	pStatement.setString(2, startdate);
    	pStatement.setString(3, enddate);
    	pStatement.setString(4, enddate);
    	pStatement.setString(5, startdate);
    	pStatement.setString(6, reason);
    	pStatement.setString(7, freeze_fees);
    	pStatement.setString(8, suspendcost);
    	pStatement.setString(9, oneofffee);
    	pStatement.setString(10, allowentry);
    	pStatement.setString(11, extend_membership);
    	pStatement.setString(12, promotion);
    	pStatement.setString(13, fullcost);
    	if (holdfee != null) {
    		pStatement.setString(14, holdfee);
    	} else {
    		pStatement.setNull(14, java.sql.Types.LONGVARCHAR);
    	}
    	pStatement.setString(15, prorata);
    	
    	pStatement.setInt(16, sid);
    	
    	return pStatement.executeUpdate();
    }
    
    public ResultSet getIdCards(long last_sync) throws SQLException, NullPointerException {
	    	ResultSet rs;
	    	try {
	    		pStatement = con.prepareStatement("SELECT id, serial, created FROM idcard WHERE created > ?;");
	    		pStatement.setTimestamp(1, new java.sql.Timestamp(last_sync));
	    		rs = pStatement.executeQuery();
	    	} catch (SQLException e) {
	    		Log.d(TAG, "", e);
	    		this.closePreparedStatement();
	    		pStatement = con.prepareStatement("SELECT id, serial FROM idcard;");
	    		rs = pStatement.executeQuery(); 
	    	}
	    	
	    	return rs;
    }
    
    public ResultSet getPaymentMethods() throws SQLException, NullPointerException {
	    	ResultSet rs;
	    	
	    	pStatement = con.prepareStatement("SELECT id, name FROM paymentmethod;");
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;
    }
    /**
     * grabs the mlength as seconds, add them to the chosen start date.
     */
    public ResultSet getProgrammes(long lastupdate) throws SQLException, NullPointerException {
	    	ResultSet rs;
	    	String query = "SELECT p.id AS pid, programmegroupid, p.name, pg.name AS groupname, startdate, enddate, "
	    			+ "amount, date_part('epoch', mlength::interval) as mlength, signupfee, notes, lastupdate, price_desc(NULL, p.id) AS price_desc,"
	    			+ " concession "
	    			+ "FROM programme p LEFT JOIN programmegroup pg ON "
	    			+ "(p.programmegroupid = pg.id)"
	    			+ "WHERE history = 'f' AND lastupdate >?";
	    	pStatement = con.prepareStatement(query);
	    	pStatement.setTimestamp(1, new java.sql.Timestamp(lastupdate));
	    	
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;
    }
    
    /**
     * This functions needs to upload memberships,
     * @return
     * @throws SQLException
     */
    public int uploadMembership(int memberId, int membershipId, int programmeId, int programmeGroupId, 
    		String startDate, String endDate, int cardNo, String signupFee, String price) throws SQLException, NullPointerException {
	    	//dru said leave firstpayment blank, nightrun should fix it.
    		//however we get weird behaviour from the default/demo programmes.
    		//firstpayment
	    	pStatement = con.prepareStatement("INSERT INTO membership (id, programmeid, programmegroupid, memberid, startdate, "
	    			+ "enddate, cardno, notes, signupfee, paymentdue) VALUES (?, ?, ?, ?, ?::date, ?::date, ?, 'Membership Added Via Android',"
	    			+ " ?::money, ?::money);");
	    	pStatement.setInt(1, membershipId);
	    	pStatement.setInt(2, programmeId);
	    	pStatement.setInt(3, programmeGroupId);
	    	pStatement.setInt(4, memberId);
	    	pStatement.setString(5, startDate);
	    	if (endDate != null) {
	    		pStatement.setString(6, endDate);
	    	} else {
	    		pStatement.setNull(6, java.sql.Types.DATE);
	    	}
	    	pStatement.setInt(7, cardNo);
	    	pStatement.setString(8, signupFee);
	    	pStatement.setString(9, price);
	    	
	    	pStatement.executeUpdate();
	    	this.closePreparedStatement();
	    	
	    	pStatement = con.prepareStatement("UPDATE membership SET (completed) = ('t') WHERE id = ?");
	    	pStatement.setInt(1, membershipId);
	    	
	    	return pStatement.executeUpdate();

    }
    
    public ResultSet getDoors() throws SQLException, NullPointerException {
	    	ResultSet rs = null;
	    	pStatement = con.prepareStatement("SELECT id, name, status, checkout, womenonly, "
	    			+ "concessionhandling, showlastvisits, companyid FROM door;");
	    	
	    	rs = pStatement.executeQuery();
	    	return rs;
    }
    
    public boolean manualCheckIn(int doorid, int membershipid, int memberid) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("SELECT swipe_manual(?, ?, ?, NULL);");
	    	doorid = (doorid <= 0)? 1 : doorid; //if we got here with a broken doorid, then default it to 1.
	    	pStatement.setInt(1, doorid);
	    	pStatement.setInt(2, membershipid);
	    	pStatement.setInt(3, memberid);
	    	
	    	return pStatement.execute();
    }
    
    public boolean OpenDoor(int doorid) throws SQLException, NullPointerException {
    	pStatement = con.prepareStatement("NOTIFY opendoor"+doorid+";");
    	
    	return pStatement.execute();
    }

    //stuff like this breaks on large databases.
    public ResultSet getMemberNotes(Long lastupdate) throws SQLException, NullPointerException {
	    	ResultSet rs = null;
	    	
	    	pStatement = con.prepareStatement("SELECT membernotes.* FROM membernotes LEFT JOIN member ON ("
	    			+ "membernotes.memberid = member.id) WHERE member.status != 3 AND membernotes.occurred >= ?::date;");
	    	if (lastupdate < 20) {
	    		Calendar cal = Calendar.getInstance();
	    		cal.add(Calendar.YEAR, -1);
	    		pStatement.setString(1, Services.DateToString(cal.getTime()));
	    	} else {
	    		pStatement.setString(1, Services.dateFormat(new Date(lastupdate).toString(),
						"EEE MMM dd HH:mm:ss zzz yyyy", "dd-MM-yyyy"));
	    	}
	    	
	    	rs = pStatement.executeQuery();
	    	
	    	return rs;
    }
    
    
    public int uploadMemberNotes(int membernoteid, int memberid, String notes, String occured) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("INSERT INTO membernotes (id, memberid, notes, occurred) VALUES "
	    			+ "(?, ?, ?, ?::date);");
	    	pStatement.setInt(1, membernoteid);
	    	pStatement.setInt(2, memberid);
	    	pStatement.setString(3, notes);
	    	pStatement.setString(4, occured);
	    	
	    	pStatement.executeUpdate();
	    	closePreparedStatement();
	    	//this shouldn't break things, as the update notes trigger checks if it exists first.
	    	pStatement = con.prepareStatement("UPDATE member SET (notes) = (?) WHERE id = ?");
	    	pStatement.setString(1, notes);
	    	pStatement.setInt(2, memberid);
	    	
	    	return pStatement.executeUpdate();
    }
    
    public ResultSet getBalance(String memberid) throws SQLException, NullPointerException {

	    	pStatement = con.prepareStatement("SELECT member_owe(?) AS owing;");
	    	pStatement.setInt(1, Integer.parseInt(memberid));
	    	
	    	return pStatement.executeQuery();
    }
    
    public ResultSet getDeletedRecords(Long last_sync) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("SELECT * FROM deleted_record WHERE deletedat >= ?::TIMESTAMP WITHOUT TIME ZONE");
	    	
	    	pStatement.setTimestamp(1, new Timestamp(last_sync));
	    	
	    	return pStatement.executeQuery();
    }
    
    public int uploadRoll(int rollid, String rollname, String datetime) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("INSERT INTO roll (id, name, datetime) VALUES (?, ?, ?::TIMESTAMP WITHOUT TIME ZONE);");
	    	pStatement.setInt(1, rollid);
	    	pStatement.setString(2, rollname);
	    	pStatement.setString(3, datetime);
	    	
	    	return pStatement.executeUpdate();
    }
    
    public int uploadRollItem(int rollitemid, int rollid, int memberid, String attended) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("INSERT INTO roll_item (id, rollid, memberid, attended) VALUES (?, ?, ?, ?::BOOLEAN)");
	    	pStatement.setInt(1, rollitemid);
	    	pStatement.setInt(2, rollid);
	    	pStatement.setInt(3, memberid);
	    	pStatement.setString(4, attended); //this is a boolean. hopefully it plays nice with postgres.
	    	
	    	return pStatement.executeUpdate();
    }
    
    public int updateRollItem(int rollitemid, String attended) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("UPDATE roll_item SET (attended) = (?::BOOLEAN) WHERE id = ?;");
	    	pStatement.setString(1, attended);
	    	pStatement.setInt(2, rollitemid);
	    	return pStatement.executeUpdate();
    	
    }
    
    public ResultSet getRoll(long last_sync) throws SQLException, NullPointerException {

	    	pStatement = con.prepareStatement("SELECT id, name, datetime FROM roll WHERE created >= ?;");
	    	pStatement.setTimestamp(1, new Timestamp(last_sync));
	    	
	    	return pStatement.executeQuery();
    }
    
    public ResultSet getRollItem(long last_sync) throws SQLException, NullPointerException {
	    	pStatement = con.prepareStatement("SELECT id, rollid, memberid, attended FROM roll_item WHERE lastupdate >= ?;");
	    	pStatement.setTimestamp(1, new Timestamp(last_sync));
	    	
	    	return pStatement.executeQuery();
    }
    
    public int deleteMembership(int membershipid) throws SQLException, NullPointerException {
    		pStatement = con.prepareStatement("DELETE FROM membership WHERE id = ?;");
    		pStatement.setInt(1, membershipid);
    		
    		return pStatement.executeUpdate();
    }
    
    public int uploadIdCard(int cardid, String serial) throws SQLException {
    	pStatement = con.prepareStatement("INSERT INTO idcard (id, serial) VALUES (?, ?);");
    	pStatement.setInt(1, cardid);
    	pStatement.setString(2, serial);
    	
    	return pStatement.executeUpdate();
    }
    
    public ResultSet getExpiryReason() throws SQLException {
    	pStatement = con.prepareStatement("SELECT * FROM membership_expiry_reason;");

    	return pStatement.executeQuery();
    }
    
        
    /**
     * @param values, membershipid
     * @return 
     * @throws SQLException
     */
    public int updateMembership(ContentValues values, int membershipid) throws SQLException {
    	String update_query = "UPDATE membership SET (";
    	String values_query = ") = (";
    	String cancellation_fee = null;
	
		if (values.containsKey(ContentDescriptor.Membership.Cols.CANCEL_REASON)){
			update_query = update_query+" "+ContentDescriptor.Membership.Cols.CANCEL_REASON+",";
			values_query = values_query+" '"+values.getAsString(ContentDescriptor.Membership.Cols.CANCEL_REASON)+"',";
			values.remove(ContentDescriptor.Membership.Cols.CANCEL_REASON);
		}
		if (values.containsKey(ContentDescriptor.Membership.Cols.TERMINATION_DATE)) {
			update_query = update_query+" "+ContentDescriptor.Membership.Cols.TERMINATION_DATE+",";
			values_query = values_query+" "+values.getAsString(ContentDescriptor.Membership.Cols.TERMINATION_DATE)+",";
			values.remove(ContentDescriptor.Membership.Cols.TERMINATION_DATE); 
		}
		if (values.containsKey(ContentDescriptor.Membership.Cols.CARDNO)) {
			update_query = update_query+" "+ContentDescriptor.Membership.Cols.CARDNO+",";
			values_query = values_query+" "+values.getAsString(ContentDescriptor.Membership.Cols.CARDNO)+",";
			values.remove(ContentDescriptor.Membership.Cols.CARDNO);
		}
		if (values.containsKey(ContentDescriptor.CancellationFee.Cols.FEE)) {
			cancellation_fee = values.getAsString(ContentDescriptor.CancellationFee.Cols.FEE);
			values.remove(ContentDescriptor.CancellationFee.Cols.FEE);
		}
    	
		if (values.size() > 0) {
			//we've still got variables we should probably throw an error. 
		}
		update_query = update_query.substring(0, (update_query.length()-1));
		values_query = values_query.substring(0, (values_query.length()-1));
		
		update_query = update_query+values_query+") WHERE id = ?";
		
		pStatement = con.prepareStatement(update_query);
		pStatement.setInt(1, membershipid);
		//Log.w(TAG, pStatement.toString());
		pStatement.executeUpdate();
		
		if (cancellation_fee != null) {
			pStatement = con.prepareStatement("select add_cancelfee(?::money,?);");
	        pStatement.setString(1, cancellation_fee);
	        pStatement.setInt(2, membershipid);
	        pStatement.executeUpdate();
		}
		
		pStatement = con.prepareStatement("select nightrun_membership(?);");
		pStatement.setInt(1, membershipid);
		
    	ResultSet rs = pStatement.executeQuery();
    	rs.close();
    	return 1; // ?
    }
    
    public ResultSet getKPIs() throws SQLException {
    	boolean kpi_available = false;
    	try {									//currently set to 319 for demo-ing purposes.
    		pStatement = con.prepareStatement("SELECT * FROM checkversion_atleast(320);"); 
    		ResultSet rs = pStatement.executeQuery();
    		rs.next();
    		if (rs.getInt(1)==0) {
    			kpi_available = true;
    		} 
    	} catch (SQLException e) {
    		Log.i(TAG, "ERROR",e);
    	}
    	
    	if (kpi_available) {
    		pStatement = con.prepareStatement("SELECT * FROM hornet_kpi();");
    		return pStatement.executeQuery();
    	} else {
    		return null;
    	}
    }
    
    
    
    public ResultSet getFinance(long lastupdate) throws SQLException {
    	//what our members have payed.
    	String query = "SELECT payment.id AS id, payment.memberid AS memberid, payment.membershipid AS membershipid,"
    			+ " EXTRACT(epoch FROM payment.paymentdate) AS occurred,"
    			+ " extract(epoch FROM payment.created) AS created,"
    			+ " EXTRACT(epoch FROM payment.lastupdate) AS lastupdate, payment.amount AS credit, NULL as debit, (case"
    			+ " when dd_export_memberid is not null then 'DD Pay'"
    			+ " when webpayment then 'Web Payment'"
    			+ " when autopayment then 'Auto Added'"
    			+ " when deposit then 'Deposit'"
    			+ " else 'Payment' end) as origin,"
    			+ " paymentmethod.name as note,"
    			+ " dd_export_memberid"
    			+ " FROM payment LEFT JOIN debitjournal ON (debitjournal.id = journalid)"
    			+ " LEFT JOIN paymentmethod ON (paymentmethod.id = paymentmethodid)"
    			+ " LEFT JOIN member ON (payment.memberid = member.id)"
    			+ " WHERE (amount != '0'::money or dd_export_memberid is not null)"
    			+ " AND payment.memberid IS NOT NULL"
    			+ " AND member.status != 3"
    			+ " AND paymentdate BETWEEN ?::DATE AND current_date"

    			+ " UNION"
    			//what our members owed
    			+ " SELECT debitjournal.id AS id, debitjournal.memberid, debitjournal.membershipid,"
    			+ " extract(epoch FROM debitjournal.occurred) AS occurred, "
    			+ " extract(epoch FROM debitjournal.created) as created, EXTRACT(epoch FROM debitjournal.lastupdate) AS lastupdate,"
    			+ " NULL AS credit, debitjournal.debit AS debit,"
    			+ " debitjournal.origin AS origin, debitjournal.note AS note,"
    			+ " NULL as dd_export_memberid"
    			+ " FROM debitjournal LEFT JOIN programme_addon on"
    			+ " (addonid = programme_addon.id)"
    			+ " LEFT JOIN member ON (member.id = debitjournal.memberid)"
    			+ " WHERE occurred BETWEEN ?::DATE AND current_date"
    			+ " AND debitjournal.memberid IS NOT NULL"
    			+ " AND member.status != 3"
    			+ " ORDER BY memberid, occurred DESC;";

    	Date startdate = new Date(lastupdate);
    	
    	if (lastupdate <= 20) {
    		//set our Date to a year a go.
    		Calendar cal = Calendar.getInstance();
    		cal.add(Calendar.YEAR, -1);
    		startdate = cal.getTime();
    	}
    	
    	pStatement = con.prepareStatement(query);
    	pStatement.setString(1, Services.DateToString(startdate));
    	pStatement.setString(2, Services.DateToString(startdate));
    	
    	return pStatement.executeQuery();
    }
    
    public ResultSet getBillingHistory(long lastupdate) throws SQLException {
    	
    	pStatement = con.prepareStatement("SELECT dd_export_member.id, memberid, dd_exportid, amount, failed, dd_export_member.status, note,"
    			+ " EXTRACT(epoch FROM dd_export_member.lastupdate) AS lastupdate, EXTRACT(epoch FROM at) AS processdate, "
    			+ " failreason, dishonoured, paidbyother, runningtotal"
    			+ " FROM dd_export_member LEFT JOIN member ON (dd_export_member.memberid = member.id)"
    			+ " LEFT JOIN dd_export ON (dd_export.id = dd_export_member.dd_exportid)"
    			+ " WHERE dd_export_member.created >= ?"
    			+ " AND member.status != 3;");
    	pStatement.setTimestamp(1, new Timestamp(lastupdate));
    	
    	return pStatement.executeQuery();
    }
    

    public ResultSet getEmailAddressesByIds(Integer []ids, String tableName) throws SQLException {
    	String query = null;
    	String idString = "";
     	   	
    	if (tableName.compareTo("enquiry")== 0) {
    		
    		query = "Select enquiry_id, email from " + tableName + " where enquiry_id in (";
    		
    	} else {
    		
    		query = "Select email from " + tableName + " where id in (";
    		
    	}
    	
    	for (int i=0; i< ids.length; i++) {
    		
    			idString += ids[i];
    		
    		if (i != (ids.length-1)) {
    			
    			idString += ",";
    			
    		} else {
    			
    			idString = idString+");";
    		}
    	
    	}
    	
    	query += idString;
    	
    	System.out.println("Ids Length: " +ids.length);
    	System.out.println("Email Query: " +query);
    	//String query = "Select email from ? where id in ?";
    	//Array sqlArray = con.createArrayOf("Int", ids);
    	//this.pStatement.setString(1, tableName);
    	//this.pStatement.setArray(2, sqlArray);
    	this.pStatement = con.prepareStatement(query);	
    	return this.pStatement.executeQuery();
    }
    
    public ResultSet getReportFilterFieldsByReportId(int reportId) throws SQLException {
    	String query = "Select filter_name, field from report_filter where user_report_id = ?";
    	this.pStatement = con.prepareStatement(query);
    	this.pStatement.setInt(1,reportId);
    	return this.pStatement.executeQuery();
    }
    
   public ResultSet getFirstReportFilterData(String query) throws SQLException {
    	this.pStatement = con.prepareStatement(query);
    	return this.pStatement.executeQuery();
    }
    
   public ResultSet getDevice(int deviceid) throws SQLException {
    	if (deviceid <= 0) {
    		return null;
    	}
    	pStatement = con.prepareStatement("SELECT id, device, servertime, clienttime, completed, allowed_access FROM sync WHERE id = ?");
    	pStatement.setInt(1, deviceid);
    	
    	return pStatement.executeQuery();
    }
    
    public int uploadEnquiry( String surname, String firstname, String gender, String email, String dob,
    		String street, String suburb, String city, String postal, String hphone, String cphone, String notes) throws SQLException {
    	
    	pStatement = con.prepareStatement("INSERT INTO enquiry (surname, firstname, gender, email, dob, addressstreet, addresssuburb, "
    			+ "addresscity, addressareacode, notes, phonehome, phonecell) VALUES (?, ?, ?, ?, ?::DATE, ?, ?, ?, ?, ?, ?, ?);");
    	
    	pStatement.setString(1, surname);
    	pStatement.setString(2, firstname);
    	pStatement.setString(3, gender);
    	
    	if (email != null) {
    		pStatement.setString(4, email);
    	} else {
    		pStatement.setNull(4, java.sql.Types.VARCHAR);
    	}
    	pStatement.setString(5, dob);
    	
    	if (street != null) {
    		pStatement.setString(6, street);
    	} else {
    		pStatement.setNull(6, java.sql.Types.VARCHAR);
    	}
    	if (suburb != null) {
    		pStatement.setString(7, suburb);
    	} else {
    		pStatement.setNull(7, java.sql.Types.VARCHAR);
    	}
    	if (city != null) {
    		pStatement.setString(8, city);
    	} else {
    		pStatement.setNull(8, java.sql.Types.VARCHAR);
    	}
    	if (postal != null) {
    		pStatement.setString(9, postal);
    	} else {
    		pStatement.setNull(9, java.sql.Types.VARCHAR);
    	}
    	if (notes != null) {
    		pStatement.setString(10, notes);
    	} else {
    		pStatement.setNull(10, java.sql.Types.VARCHAR);
    	}
    	if (hphone != null) {
    		pStatement.setString(11, hphone);
    	} else {
    		pStatement.setNull(11, java.sql.Types.VARCHAR);
    	}
    	if (cphone != null) {
    		pStatement.setString(12, cphone);
    	} else {
    		pStatement.setNull(12, java.sql.Types.VARCHAR);
    	}
    	
    	return pStatement.executeUpdate();
    }
    

    public ResultSet getReportTypes() throws SQLException {
    	    this.pStatement = con.prepareStatement("Select id, name, view_name, reportgroup from Report_Type");
    	    return this.pStatement.executeQuery();
    }
    public ResultSet getReportNamesByReportTypeId(int report_type_id) throws SQLException {
    	this.pStatement = con.prepareStatement("Select id, name from user_report where report_type_id = ?");
    	this.pStatement.setInt(1,report_type_id);
    	return this.pStatement.executeQuery();
    }
    public ResultSet getJoiningTablesByFunctionName(String functionName) throws SQLException {
    	String query = "Select table_name, joining_query from report_function_table where function_name = ?";
    	this.pStatement = con.prepareStatement(query);
    	this.pStatement.setString(1,functionName);
    	return this.pStatement.executeQuery();
    }
    public ResultSet getReportTypesAndNames() throws SQLException {
    	
    	String query = "SELECT id, name, function_name, description, report_type_id AS order, false as istype FROM user_report" 
    	+" UNION SELECT id, name, view_name, NULL::text, id AS order, true as istype FROM report_type ORDER BY \"order\", \"istype\" DESC, name,id";
	    this.pStatement = con.prepareStatement(query);
	    return this.pStatement.executeQuery();
    }
    
    public ResultSet getReportColumnsByReportId(int reportId) throws SQLException {
	    this.pStatement = con.prepareStatement("Select id AS \"report_field_id\", column_name from report_field where user_report_id = ? Order By id");
	    this.pStatement.setInt(1, reportId);
	    return this.pStatement.executeQuery();
    }
    
    public ResultSet getReportColumnsFieldsByReportId(int reportId) throws SQLException {
    	String query = "SELECT report_field.source_field_id, report_field.id AS column_id, report_field.column_name"
         +", (SELECT field FROM report_source_field WHERE id = report_field.source_field_id) AS field"
         + " FROM report_field WHERE user_report_id = ? ORDER BY sort_order, column_name";
	    this.pStatement = con.prepareStatement(query);
	    this.pStatement.setInt(1, reportId);
	    return this.pStatement.executeQuery();
    }
    
    public ResultSet getReportDataByDateRange( String finalQuery) throws SQLException {
    	this.pStatement = con.prepareStatement(finalQuery);    	
    	return this.pStatement.executeQuery();
    }
    
    public ResultSet getReportDataByDateRangeTwo(String mainQuery, Date startDate, Date endDate) throws SQLException {
    	try {
    		con.clearWarnings(); 	
    	this.pStatement = con.prepareStatement(mainQuery);
    	this.pStatement.setDate(1, new java.sql.Date(startDate.getTime()));
    	this.pStatement.setDate(2, new java.sql.Date(endDate.getTime()));    	
    	return this.pStatement.executeQuery();
    	} catch (SQLException e) {
    		Log.e(TAG, "SQL ERROR:"+con.getWarnings(), e);
    		throw new SQLException(e);
    	}
    }
     
public void fixDuplicatePopUp() throws SQLException {
    	String query = "DO $$ "
    			+"DECLARE "
	    	        +"r RECORD; "
	    	        +"prevmid INT; "
	    	        +"curmid INT; "
    	        +"BEGIN "
    	        	+"prevmid = 0; "
	    	        +"FOR r IN "
	    	                +"SELECT id, memberid, is_profile FROM image WHERE id IN ("
	    	                	+ "SELECT id FROM image i2 WHERE is_profile = true AND i2.memberid = memberid) "
	    	                + "ORDER BY memberid "
	    	        +"LOOP "
	    	                +"curmid = r.memberid; "
	    	                +"IF prevmid = curmid THEN "
	    	                        +"UPDATE image SET (is_profile) = (false) WHERE id = r.id; "
	    	                +"END IF; "
	    	                +"prevmid = curmid; "
	    	        +"END LOOP; "
	    	    +"END$$; ";
    	
    	pStatement = con.prepareStatement(query);
    	pStatement.executeUpdate();
    }
    
    //TODO: this won't work untill payment_against has a last_update column.
    public ResultSet getPaymentAgainst(long last_sync) throws SQLException {
    	pStatement = con.prepareStatement("SELECT id, paymentid, debitjournal, amount, voidamount, lastupdate FROM payment_against WHERE "
    			+ "lastupdate >= ? ;");
    	pStatement.setTimestamp(1, new java.sql.Timestamp(last_sync));
    	
    	return pStatement.executeQuery();
    }
    
    public ResultSet getResourceType(long last_sync) throws SQLException {
    	pStatement = con.prepareStatement("SELECT id, name, period FROM resourcetype WHERE lastupdate >= ? ;");
    	pStatement.setTimestamp(1, new java.sql.Timestamp(last_sync));
    	
    	return pStatement.executeQuery();
    }
    //do I need to add start/end time handling?
    public int uploadResource(int rid, String name, int rtid, String history) throws SQLException {
    	pStatement = con.prepareStatement("INSERT INTO resource (id, name, resourcetypeid, history) VALUES (?, ?, ?, ?::BOOLEAN);");
    	
    	pStatement.setInt(1, rid);
    	pStatement.setString(2, name);
    	pStatement.setInt(3, rtid);
    	pStatement.setString(4, history);
    	
    	return pStatement.executeUpdate();
    }
    
    public int updateResource(int rid, String name, int rtid, String history) throws SQLException {
    	pStatement = con.prepareStatement("UPDATE resource SET (name, resourcetypeid, history) = (?, ?, ?::BOOLEAN) WHERE id = ?");

    	pStatement.setString(1, name);
    	pStatement.setInt(2, rtid);
    	pStatement.setString(3, history);
    	pStatement.setInt(4, rid);

    	return pStatement.executeUpdate();
    }
    
    public ResultSet getProgrammeGroups(long last_sync) throws SQLException {
    	pStatement = con.prepareStatement("SELECT id, name, issuecard, historic FROM programmegroup WHERE lastupdate > ?;");
    	pStatement.setTimestamp(1, new java.sql.Timestamp(last_sync));
    	
    	return pStatement.executeQuery();
    }
    public ResultSet getProgrammeGroups() throws SQLException {
    	pStatement = con.prepareStatement("SELECT id, name, issuecard, historic FROM programmegroup ;");
    	
    	return pStatement.executeQuery();
    }
    
    //uploadProgrammeGroup, updateProgrammeGroup.
    public int uploadProgrammeGroup(int id, String name, String historic, String issuecards) throws SQLException {
    	pStatement = con.prepareStatement("INSERT INTO programmegroup (id, name, historic, issuecard) VALUES (?, ?, ?::BOOLEAN, ?::BOOLEAN);");
    	pStatement.setInt(1, id);
    	pStatement.setString(2, name);
    	pStatement.setString(3, historic);
    	pStatement.setString(4, issuecards);
    	
    	Log.w(TAG, pStatement.toString());
    	return pStatement.executeUpdate();
    }
    
    public int updateProgrammeGroup(int id, String name, String historic, String issuescards) throws SQLException {
    	pStatement = con.prepareStatement("UPDATE programmegroup SET (name, historic, issuecard) = (?, ?::BOOLEAN, ?::BOOLEAN) WHERE id = ?");
    	
    	pStatement.setString(1, name);
    	pStatement.setString(2, historic);
    	pStatement.setString(3, issuescards);
    	pStatement.setInt(4, id);
    	
    	return pStatement.executeUpdate();
    }
    
    public int uploadBookingType(int btid, String name, String price, String length, String desc, String maxbetween, boolean online, 
    		boolean msh_only, boolean history) throws SQLException {
    	pStatement = con.prepareStatement("INSERT INTO bookingtype (id, name, price, length, history, description, maxintbetween, "
    			+ "onlinebook, msh_onlybook) VALUES (?, ?, ?::MONEY, ?::INTERVAL, ?, ?, ?::INTERVAL, ?, ?);");
    	
    	pStatement.setInt(1, btid);
    	pStatement.setString(2, name);
    	if (price != null) {
    		pStatement.setString(3, "$"+price);
    	} else {
    		pStatement.setNull(3, java.sql.Types.VARCHAR);
    	}
    	
    	if (length != null) {
    		pStatement.setString(4, length);
    	} else {
    		pStatement.setNull(4, java.sql.Types.VARCHAR);
    	}
    	
    	pStatement.setBoolean(5, history);
    	if (desc != null) {
    		pStatement.setString(6, desc);
    	} else {
    		pStatement.setNull(6, java.sql.Types.VARCHAR);
    	}
    	if (maxbetween != null) {
    		pStatement.setString(7, maxbetween);
    	} else {
    		pStatement.setNull(7, java.sql.Types.VARCHAR);
    	}
    	pStatement.setBoolean(8, online);
    	pStatement.setBoolean(9, msh_only);
    	
    	return pStatement.executeUpdate();
    }

    public int updateBookingType(int btid, String name, String price, String length, String desc, String maxbetween, boolean online, 
    		boolean msh_only, boolean history) throws SQLException {
    	pStatement = con.prepareStatement("UPDATE bookingtype SET (name, price, length, description, maxintbetween, onlinebook, msh_onlybook, history) "
    			+ "= (?, ?::MONEY, ?::INTERVAL, ?, ?::INTERVAL, ?, ?, ?) WHERE id = ?");
    	
    	pStatement.setString(1, name);
    	if (price != null) {
    		pStatement.setString(2, "$"+price);
    	}else {
    		pStatement.setNull(2, java.sql.Types.VARCHAR);
    	}
    	
    	if (length != null) {
    		pStatement.setString(3, length);
    	} else {
    		pStatement.setNull(3, java.sql.Types.VARCHAR);
    	}
    	
    	
    	if (desc != null) {
    		pStatement.setString(4, desc);
    	} else {
    		pStatement.setNull(4, java.sql.Types.VARCHAR);
    	}
    	
    	if (maxbetween != null) {
    		pStatement.setString(5, maxbetween);
    	} else {
    		pStatement.setNull(5, java.sql.Types.VARCHAR);
    	}
    	
    	pStatement.setBoolean(6, online);
    	pStatement.setBoolean(7, msh_only);
    	pStatement.setBoolean(8, history);
    	pStatement.setInt(9, btid);
    	
    	return pStatement.executeUpdate();
    }
    
    public int uploadDoor(int id, String name, int status, String checkout, String womenonly, int concessionhandling, 
    		String showvisits, int companyid) throws SQLException {
    	pStatement = con.prepareStatement("INSERT INTO DOOR (id, name, status, checkout, womenonly, concessionhandling,"
    			+ " showlastvisits, companyid) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
    	
    	pStatement.setInt(1, id);
    	pStatement.setString(2, name);
    	if (status >= 0) {
    		pStatement.setInt(3, status);
    	} else {
    		pStatement.setNull(3, java.sql.Types.INTEGER);
    	}
    	if (checkout == null || checkout.compareTo("f") == 0) {
    		pStatement.setBoolean(4, false);
    	} else {
    		pStatement.setBoolean(4, true);
    	}
    	
    	if (womenonly == null || womenonly.compareTo("f") == 0) {
    		pStatement.setBoolean(5, false); //default false
    	} else {
    		pStatement.setBoolean(5, true);
    	}
    	
    	if (concessionhandling >= 0) {
    		pStatement.setInt(6, concessionhandling);
    	} else {
    		pStatement.setNull(6, java.sql.Types.INTEGER);
    	}
    	
    	if (showvisits == null || showvisits.compareTo("t") == 0) {
    		pStatement.setBoolean(7, true); //default true
    	} else {
    		pStatement.setBoolean(7, false);
    	}
    	if (companyid > 0) {
    		pStatement.setInt(8, companyid);
    	} else {
    		pStatement.setNull(8, java.sql.Types.INTEGER);
    	}
    	
    	return pStatement.executeUpdate();
    }
    
    public int updateDoor(int id, String name, int status, String checkout, String womenonly, int concessionhandling,
    		String showvisits, int companyid) throws SQLException {
    	pStatement = con.prepareStatement("UPDATE DOOR SET (name, status, booking_checkin, womenonly, concessionhandling,"
    			+ "showlastvisits, companyid) = (?, ?, ?, ?, ?, ?, ?) WHERE id = ?;");
    	
    	pStatement.setString(1, name);
    	if (status >= 0) {
    		pStatement.setInt(2, status);
    	} else {
    		pStatement.setNull(2, java.sql.Types.INTEGER);
    	}
    	
    	if (checkout == null || checkout.compareTo("f") == 0) {
    		pStatement.setBoolean(3, false);
    	} else {
    		pStatement.setBoolean(3, true);
    	}
    	
    	if (womenonly == null || womenonly.compareTo("f") == 0) {
    		pStatement.setBoolean(4, false);
    	} else {
    		pStatement.setBoolean(4, true);
    	}
    	
    	if (concessionhandling >= 0) { 
    		pStatement.setInt(5, concessionhandling);
    	} else {
    		pStatement.setNull(5, java.sql.Types.INTEGER);
    	}
    	
    	if (showvisits == null || showvisits.compareTo("t") == 0) {
    		pStatement.setBoolean(6, true);
    	} else {
    		pStatement.setBoolean(6, false);
    	}
    	//should probably do checking
    	if (companyid > 0) {
    		pStatement.setInt(7, companyid);
    	} else {
    		pStatement.setNull(7, java.sql.Types.INTEGER);
    	}
    	pStatement.setInt(8, id);
    	
    	return pStatement.executeUpdate();
    }
    
    public SQLWarning getWarnings() throws SQLException, NullPointerException {
    	return con.getWarnings();
    }
    
    public void clearAllWarnings() throws SQLException, NullPointerException {
    	con.clearWarnings();
    }
    
    public ResultSet startStatementQuery(String query) throws SQLException, NullPointerException {
    	ResultSet rs = null;
	    	
	    	try {
	    		statement = con.createStatement();
	    		rs = statement.executeQuery(query);
	    	} catch (PSQLException e) { 
	    		//sometimes our socket throws an I/O exception (apparently meaning an issue with the connection..?)
	    		this.closeConnection();
	    		try {
	    			this.openConnection();
	    		} catch (ClassNotFoundException e2) {};
	    		rs = startStatementQuery(query);
	    	} catch (NullPointerException e) {
	    		this.closeConnection();
	    		try {
	    			this.openConnection();
	    		} catch (ClassNotFoundException e2) {/*we've already successfully opened a connection, ignore*/};
	    		rs = startStatementQuery(query);
	    	}
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
