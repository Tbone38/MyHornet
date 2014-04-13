package com.treshna.hornet;

import android.app.Activity;
import android.app.Application;

public class HornetApplication extends Application {
	private boolean is_syncing = false;
    private boolean sync_result = false;
    private boolean activity_showing = false;
    private Activity currentActivity = null;
    private static HornetApplication singleton;
    
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
    
    public void setSyncStatus(boolean sync) {
    	this.is_syncing = sync;
    }
    
    public void setSyncResult(boolean result ){
    	this.sync_result = result;
    }
    
    public void setActivityStatus(boolean status) {
    	this.activity_showing = status;
    }
    
    public void setCurrentActivity(Activity theActivity) {
    	this.currentActivity = theActivity;
    }
	
    public boolean getSyncStatus() {
    	return this.is_syncing;
    }
    
    public boolean getSyncResult() {
    	return this.sync_result;
    }
    
    public boolean getActivityStatus() {
    	return this.activity_showing;
    }
    
    public Activity getCurrentActivity() {
    	//if (this.getActivityStatus()) {
    		return this.currentActivity;
    	/*} else {
    		return null;
    	}*/
    }
    
    public HornetApplication getInstance(){
		return singleton;
	}
}
