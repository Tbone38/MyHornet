package com.treshna.hornet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

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
	private static final String TAG = "JSONHandler";
	
	private Context ctx;
	
	public JSONHandler(Context context) {
		this.ctx = context;
	}
	
	public boolean AddUser(String email, String organisation) {
		String url = HOST+"/signup?email="+email+"&organisation="+organisation;
		JSONObject result;
		String status;
		String error = null;
		
		result = getJSON(url);
		try {
			status = result.getString("status");
		} catch (JSONException e) {
			status = "error";
			error = "Server not responding.";
		}
		
		if (status.compareTo("error")==0){
			if (error == null) {
				try {
					error = result.getString("error");
				} catch (JSONException e) {
					error = "Server not responding";
				}
			}
			//Do we show an error here, or return the error to the calling activity?
		}
		
		Log.d(TAG, result.toString());
		return (error == null);
	}
	
	public boolean updateUser(String email, String username, String countrycode) {
		String url = HOST+"/updateuser?email="+email+"&username="+username+"&country="+countrycode;
		JSONObject result;
		String status;
		String error = null;
		
		result = getJSON(url);
		try {
			status = result.getString("status");
		} catch (JSONException e) {
			status = "error";
			error = "Server not responding";
		}
		
		if (status.compareTo("error")==0) {
			if (error == null) {
				try {
					error = result.getString("error");
				} catch (JSONException e) {
					error = "Server not responding";
				}
			}
		} else {
		
			String server = "-1", dbname= "-1", user = "-1", password= "-1";
			try {
				server = result.getString("server");
				dbname = result.getString("db_name");
				user = result.getString("username");
				password = result.getString("password");
			} catch (JSONException e) {
				//we should have emailed config details to the users email address.
				//if they run into issues (i.e. end up here) then refer them to the email?
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
		
		//Log.d(TAG, result.toString());
		return (error == null);
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
		} catch (Exception e) {
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
