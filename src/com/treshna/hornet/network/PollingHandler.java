package com.treshna.hornet.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PollingHandler extends BroadcastReceiver {
	public static final String POLLING_START = "polling_start";
	private Context context = null;
	private PendingIntent pintent = null;
	private AlarmManager alarm = null;
	private Calendar cal = Calendar.getInstance();
	private String message;
	private Handler handler;
	
	private boolean conStatus = false;
	private boolean serverExists = false;
	
	private boolean isPolling = true;
	
	private long start_time;
	private static final String TAG = "com.treshna.hornet.pollingHandler";
	
	public PollingHandler(Context context, PendingIntent pIntent) {
		this.context = context;
		pintent = pIntent;
		handler = new Handler();
		alarm = (AlarmManager)this.context.getSystemService(Context.ALARM_SERVICE);
		start_time = 0;
		if ((conStatus = isConnected()) == true && (serverExists = serverExists()) == true) setPolling();
	}
	
	public boolean getIsPolling(){
		return isPolling;
	}
	public boolean getConStatus(){
		return conStatus;
	}
	public boolean getServerExists(){
		return serverExists;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//if (isPolling == false) return;
		final String action = intent.getAction();
		if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
			if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
				this.serverExists = serverExists();
				this.conStatus = true;
			} else {
				this.serverExists = false;
				this.conStatus = false;
			}
		}
		if ( conStatus == true) {
			setPolling();
		} else {
			makeToast();
			stopPolling(true);
		}
		//System.out.print("\nConnection Status: "+conStatus+"\n");
		Log.v(TAG, "Connection Status: "+conStatus);
	}
	
	public boolean startService() {
		conStatus = isConnected();
		serverExists = serverExists();
		if (conStatus == true) {
			this.isPolling = true;
			setPolling();
			return true;
		} else {
			makeToast();
			stopPolling(true);
			return false;
		}
	}
	
	private boolean isConnected() {
		//get the current status of the network
		 ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		 //Do I want to checn here that were using WIFI and not data?
		 boolean result = activeNetworkInfo != null && activeNetworkInfo.isConnected();
		 //System.out.print("\nConnection Status: "+result+"\n");
		 Log.v(TAG, "Connection Status: "+result);
		 return result;
	}
	
	private boolean serverExists() {
		getDataThread gdt = new getDataThread();
		gdt.start();
		try {
			gdt.join(5000); //stop after 5 secs
		} catch (InterruptedException e) {
			// something interrupted the main threads wait.
			return false;
		}
		
		this.serverExists = gdt.serverExists;
		//System.out.print("\nServer Status: "+serverExists+"\n");
		Log.v(TAG, "Server Status: "+serverExists);
		return this.serverExists;
	}
	/**
	 * sets the alarm manager to start sync's based on settings configuration.
	 */
	private void setPolling() {
		
		
		//does this needs to ramp up? It shouldn't ever be called in a situation where it'll fail.
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int pollingInterval = Integer.parseInt(preferences.getString("sync_frequency", "-1"));
		if (pollingInterval == -1) {
			message = "sync set to never, please check application sync settings.";
			makeToast();
			return;
		}
		stopPolling(true);
		cal = Calendar.getInstance();
		start_time = cal.getTimeInMillis();
		
		preferences.edit().putLong(POLLING_START, start_time).commit();
		//set starttime in app preferences.
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, start_time, (long) (pollingInterval*60*1000), pintent);
		isPolling = true;
	}
	
	public void stopPolling(boolean tryAgain) {
		alarm.cancel(pintent);
		isPolling = tryAgain;
	}
	
	private void makeToast() {
		if (conStatus == false) message = "no wifi connection found.";
		else if (serverExists == false) message = "server not found on wifi, please check server ip settings.";
		if (message != null) {
			handler.post(new Runnable() {  
					@Override  
					public void run() {  
						Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
						message = null;
					}});
		}
	}
	
	/*************************************/
	private class getDataThread extends Thread {
		//Threading because networking.
		private boolean serverExists = false;
	    @Override
	    public void run() {
	        super.run();
	        // This needs redesigned to be threaded off of the UI. currently it's holding up the app.
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			InetAddress server = null;
			try {
				 server = InetAddress.getByName(preferences.getString("address", "-1"));	  
			} catch (UnknownHostException e) {
				this.serverExists = false;
				return;
			}
			if (server == null) { 
				//System.out.print("\n\n **inetAddress threw an exception");
				Log.e(TAG, "**inetAddress threw an exception");
				this.serverExists = false;
				return;
			}
			/* wait up to 10 seconds for a response,
			 * as the function is called before the wireless has correctly reconnected,
			 * the wait time allows for the reconnection to occur. 
			 */
			try { 
				int i;
				for (i=2; i!=0; i--) {
					if (server.isReachable(2000)) {
						this.serverExists = true;
						break;
					}
					getDataThread.sleep((long) 3000);
					//this.sleep(((long) 3000)); <- doesn't work because static.
				}
				if (serverExists) return;
				
			} catch (IOException e) {
				//System.out.print("\n\n **isReachable() threw an exception");
				Log.e(TAG, "**isReachable() threw an exception");
				this.serverExists = false;
			} catch (InterruptedException e) {
				//the sleep was interupted.
			}
			this.serverExists = false;
	    }
	}
	/*************************************/
}
