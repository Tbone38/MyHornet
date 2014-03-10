package com.treshna.hornet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

public class JSONHandler {
	private static final String TESTING = ":3223"; //append testing to end of host to enable testing. Python script must be running.
	private static final String HOST = "http://api.gymmaster.co.nz";
	
	private static final String TAG = "JSONHandler";
	
	private static byte[] iv;
	private Context ctx;
	private int errorcode = -1;
	private String errormsg = null;
	private String signup_url = null;
	private String session_token = null;
	private static final String GYMLOGIN = "/gymlogin";
	private static final String CREATEDDACCT = "/createddaccount";
	private static final String EDITEZIDEBITDETAILS = "/editezdebitdetails";
	private static final String CHECKDDSTATUS = "/checkddstatus";
	
	public JSONHandler(Context context) {
		
		this.ctx = context;
	}
	
	public static enum JSONError { //large-ish values (i.e 255/254/253) probably mean a broken bash script.
		NOERROR(0), SQL(1),MAIL(2), INTERNALCON(3), MISSINGINPUT(4),
		INVALIDINPUT(5), POST(6), USEREXISTS(7), USERNOTSETUP(8), GENERICERROR(9),
		UNIQUEVIOLATION(23505), DUPLICATEOBJECT(42710), UNDEFINEDOBJECT(42704), 
		NORESPONSE(20), BADRETRIEVE(21);
		        
		private final int key;
			
		JSONError(int thekey) {
			this.key = thekey;
		}

		public int getKey() {
 			return this.key;
 		}
	}
	
	public boolean AddUser(String email, String organisation) {
		String url = HOST+"/signup?email="+email+"&organisation="+organisation;
		JSONObject result;
		String status;
		errorcode = -1;
		errormsg = null;
		//2a780e78620070e1c37f321065bb6a845fd1371cd5ed3d457cf2c83bb4b9a0c0 	//ZLuImr
		//16b1392fa1c881843097531a35388b68e30058b76e17c5920798da3ce8da348f 	//ZfqmRe
		//7C6D5B646F8F6D5D8E89076210880CEF 									//VsM2GO
		//String encrypt_string = "c3ab8ab87d7907aae91f6bcc9dbc4321";
		/*byte[] encrypt, decrypt;
		encrypt = Services.hexStringToByteArray(encrypt_string);
		Log.e(TAG, Arrays.toString(encrypt));
		iv = Arrays.copyOfRange(encrypt, 0, 16);
		Log.e(TAG, "IV:"+Services.bytesToHex(iv));
		byte [] encrypt_password = Arrays.copyOfRange(encrypt, 16, encrypt.length);
		decrypt = decrypt(encrypt_password, iv);
		try {
		Log.e(TAG, "DECRYPT_PASSWORD: "+new String(decrypt, "UTF-8")+"\n UNCRYPT_PASSWORD: 'ZfqmRe'");
		} catch (Exception e) {e.printStackTrace();};
		
		if (encrypt != null) {
			return false;
		}
		
		byte[] encrypt, decrypt;
		encrypt = this.encrypt("VsM2GO");
		Log.w(TAG, "ENCRYPT:"+Services.bytesToHex(encrypt));
		decrypt = this.decrypt(encrypt);
		try {
		Log.w(TAG, new String(decrypt, "UTF-8")); } catch (Exception e) {};
		
		encrypt = Services.hexStringToByteArray(encrypt_string);
		Log.e(TAG, Arrays.toString(encrypt));
		decrypt = decrypt(encrypt);
		try {
		Log.e(TAG, "DECRYPT_PASSWORD: "+new String(decrypt, "UTF-8")+"\n UNCRYPT_PASSWORD: 'VsM2G0'");
		} catch (Exception e) {};
		
		if (encrypt != null) {
			return false;
		}*/
		
		result = getJSON(url);
		if (result == null){
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return false;
		}
		try {
			status = result.getString("status");
		} catch (JSONException e) {
			status = "error";
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
		}
		
		if (status.compareTo("error")==0){
			if (errormsg == null) {
				try {
					errormsg = result.getString("error");
					errorcode = result.getInt("errorcode");
				} catch (JSONException e) {
					errormsg = "Server not responding";
					errorcode = JSONError.NORESPONSE.getKey();
				}
			}
		}
		
		Log.d(TAG, result.toString());
		//errorcodes 8 = email exists but isn't setup, < 0 is success.
		return (errorcode <= 0 || errorcode == JSONError.USERNOTSETUP.getKey());
	}
	
	public String getError(){
		return this.errormsg;
	}
	
	public int getErrorCode(){
		return this.errorcode;
	}
	//used for returning urls.
	public String getURL() {
		return this.signup_url;
	}
	
	public boolean updateUser(String email, String username, String countrycode, boolean contactable, String contact_name, String contact_number) {
		byte[] encrypt, decrypt;		
		/*encrypt = encrypt(" ");
		try {
		Log.e(TAG, "TEST STRING ENCRYPTED: "+new String(encrypt, "UTF-8"));
		} catch (Exception e) {e.printStackTrace();};*/

		//if can contact we add the contact details, other wise ignore.
		username = username.replace(" ", "%20");
		String url = HOST+"/updateuser?email="+email+"&username="+username+"&country="+countrycode+"&contactable="+String.valueOf(contactable);
		
		if (contactable) {
			if (contact_name != null) {
				url = url +"&contactname="+contact_name;	
			}
			if (contact_number != null) {
				url = url +"&contactnumber="+contact_number;
			}
		}
		JSONObject result;
		String status;
		errorcode = -1;
		errormsg = null;
		
		
		result = getJSON(url);
		try {
			status = result.getString("status");
		} catch (JSONException e) {
			status = "error";
			errormsg = "Server not responding";
			errorcode = JSONError.NORESPONSE.getKey();
		}
		
		if (status.compareTo("error")==0) {
			if (errormsg == null) {
				try {
					errormsg = result.getString("error");
					errorcode = result.getInt("errorcode");
				} catch (JSONException e) {
					errormsg = "Server not responding";
					errorcode = JSONError.NORESPONSE.getKey();
				}
			}
		} else {
			Log.d(TAG, result.toString());
			String server = "-1", dbname= "-1", user = "-1", password= "-1";
			try {
				server = result.getString("server");
				dbname = result.getString("db_name");
				user = result.getString("username");
				password = result.getString("uncrypt_password");
				//String encrypt_string = result.getString("encrypt_password");
				//encrypt = Services.hexStringToByteArray(encrypt_string);
				//iv = Arrays.copyOfRange(encrypt, 0, 16);
				//Log.e(TAG, "IV:"+Services.bytesToHex(iv));
				//byte [] encrypt_password = Arrays.copyOfRange(encrypt, 16, encrypt.length);
				 
				/*Log.e(TAG, "ENCRYPT_PASSWORD:"+Services.bytesToHex(encrypt));
				decrypt = decrypt(encrypt); //, iv
				try {
				Log.e(TAG, "DECRYPT_PASSWORD: "+new String(decrypt, "UTF-8")+"\n UNCRYPT_PASSWORD: "+password);
				} catch (Exception e) {e.printStackTrace();};*/
			} catch (JSONException e) {
				//we should have emailed config details to the users email address.
				//if they run into issues (i.e. end up here) then refer them to the email?
				errormsg = "please refer to the details in the email that was sent to you, in order to finish the setup.";
				errorcode = JSONError.BADRETRIEVE.getKey();
			}
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("address", server);
			editor.putString("port", "5432");
			editor.putString("username", user);
			editor.putString("database", dbname);
			editor.putString("password", password);
			
			editor.commit();
		}
		
		return (errorcode <= 0);
	}
	
	public boolean resetPassword(String email){
		errorcode = -1;
		errormsg = null;
		String status = "";
		
		JSONObject response;
		String url = HOST+"/resetpassword?email="+email;
		response = getJSON(url);
		
		if (response == null) {
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return false;
		}
		try {
			status = response.getString("status");
		} catch (JSONException e) {
			status = "error";
			errormsg = "Server not responding";
			errorcode = JSONError.NORESPONSE.getKey();
		}
		
		if (status.compareTo("error")==0) {
			if (errormsg == null) {
				try {
					errormsg = response.getString("error");
					errorcode = response.getInt("errorcode");
				} catch (JSONException e) {
					errormsg = "Server not responding";
					errorcode = JSONError.NORESPONSE.getKey();
				}
			}
		} else {
			String password = null;
			
			try {
				password = response.getString("result");
			} catch (JSONException e) {
				errormsg = "please refer to the email that was sent, in order to retrieve your new password.";
				errorcode = JSONError.BADRETRIEVE.getKey();
			}
			
			if (password != null && errorcode < 0) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("password", password);
				
				editor.commit();
			}
			return (errorcode < 0);
		}
		
		return (errorcode < 0);
	}
	
	public boolean setPassword(String username, String password) {
		errorcode = -1;
		errormsg = null;
		String status = "";
		
		JSONObject response;
		String url = HOST+"/setpassword?suername="+username;
		response = getJSON(url);
		
		if (response == null) {
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return false;
		}
		
		return false;
	}
	
	public boolean uploadLog(long this_sync, String te_username, String schemaversion, String company_name) {
    	String contents = null;
    	FileHandler filehandler = new FileHandler(ctx);
    	contents = filehandler.getLog();
    	
    	if (contents == null) {
    		return false;
    	}
    	
    	if (te_username == null || te_username.isEmpty()) {
    		//we don't care about them!
    		return false;
    	}
    	
    	String url = HOST+"/uploadlog";
		JSONObject result, post;
		post = new JSONObject();
		try {
			post.put("te_username", te_username);
			post.put("schemaversion", schemaversion);
			post.put("time", this_sync);
			post.put("contents", contents);
			post.put("company_name", company_name);
			
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
    	result = this.postJSON(url, post);
    	if (result != null) {
    		Log.d(TAG, result.toString());
    	}
    	
    	//filehandler.deleteFile("db_sync.log"); 
    	
    	return true;
    }
	
	public boolean DDLogin(String te_username, String te_password) {
		Log.d(TAG, "DD Login");
		boolean result = false;
		String status = "";
		this.signup_url = "api.gymmaster.co.nz/notavailable";
		JSONObject response, post;
		String api = "http://gmbooking-"+te_username+".gymmaster.co.nz";
				
		String url = api+GYMLOGIN;
		post = new JSONObject();
		try {
			post.put("username", te_username);
			post.put("password", te_password);
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}
		
		response = postJSON(url, post);
		
		if (response == null) {
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return result;
		}
		
		try {
			status = response.getString("validlogin");
		} catch (JSONException e) {
			status = "false";
			errormsg = "Server not responding";
			errorcode = JSONError.NORESPONSE.getKey();
		}
		
		if (status.compareTo("false")==0) {		
			errormsg = "Bad Login credentials."; //TODO: we'll need to resync the te_username & te_password
			errorcode = JSONError.BADRETRIEVE.getKey();
		} else {
			try {
				this.session_token = response.getString("session");
				result = true;
			} catch (JSONException e) {
				errormsg = "Could not retrieve Session, please try again later.";
				errorcode = JSONError.BADRETRIEVE.getKey();
			}
			
		}
		
		return result;
	}
	
	public boolean DDAdd(int memberid, String te_username) {
		Log.d(TAG, "DD Add");
		boolean result = false;
		String status = "";
		JSONObject response, post;
		
		String api = "http://gmbooking-"+te_username+".gymmaster.co.nz";
		
		String url = api+CREATEDDACCT;
		
		post = new JSONObject();
		try {
			post.put("userid", memberid);
			post.put("session", this.session_token);
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}
		
		response = this.postJSON(url, post);
		
		if (response == null) {
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return result;
		}
		
		try {
			status = response.getString("result");
		} catch (JSONException e) {
			status = "failure";
			errormsg = "Server not responding";
			errorcode = JSONError.NORESPONSE.getKey();
		}
		
		if (status.compareTo("failure")==0) {		
			try {
				errormsg = response.getString("message");
				if (errormsg.contains("There is already a customer")) result = true;
			} catch (JSONException e) {
				errormsg = "No Response";
			}
			errorcode = JSONError.INVALIDINPUT.getKey();
		} else {
			try {
				this.session_token = response.getString("session");
				result = true;
			} catch (JSONException e) {
				errormsg = "Missing Session";
				errorcode = JSONError.INTERNALCON.getKey();
			}
		}
		if (result) {
			result = DDdetails(memberid, te_username);
		}
		
		return result;
	}
	
	private boolean DDdetails(int memberid, String te_username) {
		Log.d(TAG, "DD Details");
		boolean result = false;
		JSONObject response, post;
		String api = "http://gmbooking-"+te_username+".gymmaster.co.nz";
		String url = api+EDITEZIDEBITDETAILS;
		
		post = new JSONObject();
		try {
			post.put("userid", memberid);
			post.put("session", this.session_token);
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}
		
		response = this.postJSON(url, post);
		
		if (response == null) {
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return result;
		}
		
		try {
			this.signup_url = response.getString("editurl");
			this.session_token = response.getString("session");
			result = true;
		} catch (JSONException e) {
			e.printStackTrace();
			errormsg = "Could not get response";
			errorcode = JSONError.BADRETRIEVE.getKey();
		}
		
		return result;
	}
	
	public String get_session() {
		return this.session_token;
	}
	
	public boolean DDcheckStatus(int memberid, String te_username, String session) {
		boolean result = false;
		String status = "";
		JSONObject response, post;
		
		String api = "http://gmbooking-"+te_username+".gymmaster.co.nz";
		String url = api+CHECKDDSTATUS;
		
		post = new JSONObject();
		try {
			post.put("userid", memberid);
			post.put("session", session);
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}
		
		response = this.postJSON(url, post);
		
		if (response == null) {
			errormsg = "Server not responding.";
			errorcode = JSONError.NORESPONSE.getKey();
			return result;
		}
		
		try {
			status = response.getString("ddstatus");
		} catch (JSONException e) {
			status = "bad";
			errormsg = "Server not responding";
			errorcode = JSONError.NORESPONSE.getKey();
		}
		
		if (status.compareTo("bad")==0) {		
			try {
				errormsg = response.getString("message");
			} catch (JSONException e) {
				errormsg = "No Response";
			}
			errorcode = JSONError.INVALIDINPUT.getKey();
		} else {
			try {
				result = true;
				this.session_token = response.getString("session");
			} catch (JSONException e) {/*probably doesn't matter.*/}
		}
		
		return result;
	}
	
	private SecretKeySpec generatekey() {
		SecretKeySpec key = null;
		//try {
			//String passphrase = "serialbatteryostrichscreen";
			String hex_pass = "19307f3f9253417201e4ef8183136320";
			//MessageDigest digest = MessageDigest.getInstance("MD5");
			//digest.update(passphrase.getBytes());
			//byte[] result = digest.digest();
			byte[] result = Services.hexStringToByteArray(hex_pass);
			Log.e(TAG, Arrays.toString(result));
			key = new SecretKeySpec(result, "AES"); //0, 16,
			
		/*} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} */
		
		return key;
	}
	
	/**
	* This encryption/decryption code doesn't match outputs with the Python code.
	* It needs fixed.
	**/
	public byte[] encrypt(String input) {
		byte[] encryptedBytes;
		try {
			SecretKeySpec key = generatekey();
			//Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			Cipher encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		    iv = encryptCipher.getIV();
		    // Encrypt
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
		    cipherOutputStream.write(input.getBytes());
		    cipherOutputStream.flush();
		    cipherOutputStream.close();
		    encryptedBytes = outputStream.toByteArray();
		    outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}
		
		return encryptedBytes;
	}
	
	/**
	* This encryption/decryption code doesn't match outputs with the Python code.
	* It needs fixed.
	**/
	public byte[] decrypt(byte[] encryptedBytes) { //, byte[] iv
		String output = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
		
			SecretKeySpec key = generatekey();
			
			//Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		    //decryptCipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
			Cipher decryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
			
		    outputStream = new ByteArrayOutputStream();
		    ByteArrayInputStream inStream = new ByteArrayInputStream(encryptedBytes);
		    CipherInputStream cipherInputStream = new CipherInputStream(inStream, decryptCipher);
		    byte[] buf = new byte[1024];
			int bytesRead;
		    while ((bytesRead = cipherInputStream.read(buf)) >= 0) {
		        outputStream.write(buf, 0, bytesRead);
		    }
		    
		    output = new String(outputStream.toByteArray(), "UTF-8");
		    Log.d(TAG, output);
		    cipherInputStream.close();
		    inStream.close();		    
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			//Log.e(TAG,e.getCause().toString());
			Log.e(TAG,e.getMessage());
			return null;
		} /*catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}*/ catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		//return output;
		return outputStream.toByteArray();
	}
	
	private JSONObject postJSON(String url, JSONObject post) {
		StringEntity se;
		HttpPost httpPost;
		httpPost = new HttpPost(url);
		
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		
		Log.d("JSON Parser", "Posting " + post.toString()+" To "+url);
		try {
			se = new StringEntity(post.toString(), "UTF-8");
			se.setContentType("application/json; charset=UTF-8");
			Log.d("JSON Parser", "Setting se");
			httpPost.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return JSONResponse(null, httpPost, true);
	}
	
	private JSONObject getJSON(String url) {
		
		HttpGet httpGet;
		httpGet = new HttpGet(url);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("Content-type", "application/json");
		
		return JSONResponse(httpGet, null, false);
	}

	private JSONObject JSONResponse(HttpGet httpGet, HttpPost httpPost, boolean is_post) {
		InputStream io = null;
		JSONObject jObj = null;
		String json = "";
			
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse;
			HttpEntity httpEntity;
			
			if (!is_post) {
				httpResponse = httpClient.execute(httpGet);
			} else if (is_post) {
				//Log.d(TAG, Arrays.toString(httpPost.getAllHeaders()));
				httpResponse = httpClient.execute(httpPost);
			} else {
				httpResponse = null;
			}
			
			if (httpResponse == null)
				return null;
			Log.d("JSON Parser", "Getting Response");
			httpEntity = httpResponse.getEntity();
			io = httpEntity.getContent();
			if (io == null)
				return null;
			Log.d("JSON Parser", "Got Response");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					io, "UTF8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				//System.out.print("\n|"+line);
				sb.append(line + "\n");
			}
			io.close();
			Log.d("JSON Parser", "Downloaded Results");
			json = sb.toString();
		} catch (IOException e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
		catch (IllegalArgumentException e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
	
		// try parse the string to a JSON object
		try {
			Log.d(TAG, json);
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser",
					"Error parsing data returned of " + e.toString());
			return null;
		}

		// return JSON String
		return jObj;

	}
}
