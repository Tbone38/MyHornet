package com.treshna.hornet.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;

public class ProgressBar {
	
	private boolean DEBUG;
	private ProgressDialog progress;
	private Handler thehandler;
	private Context theCtx;
	
	public ProgressBar (Handler handler, Context ctx, final String message){
		thehandler = handler;
		theCtx = ctx;
		if (theCtx == null) {
			progress = null;
			return;
		}
		
		DEBUG = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("progress", false);
		System.out.print("\n\nDebug:"+DEBUG);
		
		if (DEBUG == true) {
			//System.out.print("\n\nHandler:"+handler);
			thehandler.post(new Runnable() {  
					@Override  
					public void run() {
						//System.out.print("\n\nContext:"+ctx);
						progress = ProgressDialog.show(theCtx, "Syncing", message, true);		
			}});
		}
	}
	
	public void stopProgress(){
		thehandler.post(new Runnable() {  
			@Override  
			public void run() {
				if (progress != null){
					if (progress.isShowing()){
						progress.dismiss();
					}
				}
			}});
	}
}
