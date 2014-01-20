package com.treshna.hornet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class JSONHandler {
	private static final String HOST = "http://api.gymmaster.co.nz";
	//private static final String HOST = "http://192.168.2.132"; //TESTING
	private static final String TAG = "JSONHandler";
	
	private Context ctx;
	private int errorcode = -1;
	private String errormsg = null;
	
	public JSONHandler(Context context) {
		this.ctx = context;
	}
	
	public static enum JSONError {
		SQL(0),MAIL(1), INTERNALCON(2), MISSINGINPUT(3),
		INVALIDINPUT(4), POST(5), USEREXISTS(6), USERNOTSETUP(7),
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
		//errorcodes 7 = email exists but isn't setup, < 0 is success.
		return (errorcode < 0 || errorcode == 7);
	}
	
	public String getError(){
		return this.errormsg;
	}
	
	public int getErrorCode(){
		return this.errorcode;
	}
	
	public boolean updateUser(String email, String username, String countrycode) {
		String url = HOST+"/updateuser?email="+email+"&username="+username+"&country="+countrycode;
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
				//Log.d(TAG, "uncrypt_password: "+password);
				//Log.d(TAG, "decrypted:"+decrypt(password));
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
		
		return (errorcode < 0);
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
		
		
		return false;
	}
		
	
	private SecretKeySpec generatekey() {
		SecretKeySpec key = null;
		try {
			String passphrase = "correcthorsebatterystaple";
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(passphrase.getBytes());
			byte[] result = digest.digest();
			Log.d(TAG, "passphrase:"+Arrays.toString(result));
			
			key = new SecretKeySpec(result, 0, 16, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} 
		
		return key;
	}
	
	public String encrypt(String input) {
		String output;
		try {	
			SecretKeySpec key = generatekey();
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS7Padding");
			aes.init(Cipher.ENCRYPT_MODE, key);
			byte[] ciphertext = aes.doFinal(input.getBytes());
			output = new String(ciphertext);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
		return output;
	}
	
	public String decrypt(String input) {
		String output = null;
		byte[] inputs = Services.hexStringToByteArray(input);
		//THIS MIGHT BE BROKEN DUE TO SIGNED-NESS ?
		Log.d(TAG, Arrays.toString(inputs));
		
		try {
			SecretKeySpec key = generatekey();
			
			Cipher aes = Cipher.getInstance("AES/ECB/NoPadding"); //PKCS5Padding
			aes.init(Cipher.DECRYPT_MODE, key);
			output = new String(aes.doFinal(inputs), "ASCII");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return output;
	}
	
	public JSONObject getJSON(String url) {
		InputStream io = null;
		JSONObject jObj = null;
		String json = "";
		
			
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient;
			HttpGet httpGet;
			HttpResponse httpResponse;
			HttpEntity httpEntity;
			
			httpClient = new DefaultHttpClient();
			httpGet = new HttpGet(url);
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Content-type", "application/json");
			
			JSONObject json2 = new JSONObject();
			Log.d("JSON Parser", "Getting " + json2.toString());
			
			Log.d("JSON Parser", "Execute");
			httpResponse = httpClient.execute(httpGet);
			if (httpResponse == null)
				return null;
			Log.d("JSON Parser", "Getting Response");
			httpEntity = httpResponse.getEntity();
			io = httpEntity.getContent();
			if (io == null)
				return null;
			Log.d("JSON Parser", "Got Response");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
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
