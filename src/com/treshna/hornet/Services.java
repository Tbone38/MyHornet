package com.treshna.hornet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

public class Services {
	
	private static PollingHandler pollingHandler;
	private static boolean DEBUG;
	private static ProgressDialog progress;
	private static Context theCtx;
	private static final String TAG = "Services";
	
	/*
	 *  This function changes the format of a dateString. It requires the dateString, 
	 *  the Layout of the string (e.g "yyyy-MM-dd"), and the requested output layout. 
	 */

	public static String dateFormat(String dateString, String inputLayout, String outputLayout){
		if (dateString == null || inputLayout == null || outputLayout == null ) return null;
		SimpleDateFormat input = new SimpleDateFormat(inputLayout, Locale.US);
		Date date = null;
		try {
			date = input.parse(dateString);
		} catch (ParseException e) {
			Log.e(TAG, "Error Parsing Date:", e);
			return null;
		}
		SimpleDateFormat output = new SimpleDateFormat(outputLayout, Locale.US);
		String dateText = output.format(date);
		
		return dateText;
	}
	
	public static boolean validDate(String inputDate, String regexp, String layout){
		SimpleDateFormat format = new SimpleDateFormat(layout, Locale.US);
		if (!inputDate.matches(regexp)) return false;
		format.setLenient(false);
		try {
			format.parse(inputDate);
			return true;
		}
		catch(ParseException e){
			return false;
		}
}
	/*
	 * This function is for scaling down the images in the list,
	 * in order to avoid memory crashes. It takes a bitmapFactory option
	 * (which it uses for working out the images height), a required height,
	 * and a required width as inputs, it returns a ratio based on which is
	 * smallest.
	 */
	public static int calculateInSampleSize(
		BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	/*
	 * This function allows the application to manually set the value of an 
	 * application property. (setting)
	 */
	public static void setPreference(Context context, String key, String value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = preferences.edit();
		e.putString(key, value);
		e.commit();
	}
	
	 
	 public static String getAppSettings(Context context, String key){
		 //System.out.println("Getting App Settings");
		 Log.v(TAG, "Getting App Setting: "+key);
		 SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		 return (preferences.getString(key, "-1"));
	}

	
	
	//converts a DP input into pixels, useful for programmatically setting View margins/padding/etc.
	public static int convertdpToPxl(Context c, int dp){
		int pxl = 0;
		Resources r = c.getResources();
		pxl = (int) TypedValue.applyDimension(
		        TypedValue.COMPLEX_UNIT_DIP,
		        dp, 
		        r.getDisplayMetrics()
		);
		return pxl;
	}
	//used for the is_profile column in the Image Table.
	public static int booltoInt(boolean input){
		return (input == true)? 1 : 0;
	}
	
	public static boolean isProfile(int boolValue){
		return (boolValue == 1)? true : false;
	}
	
	public static void setPollingHandler(Context ctx, PendingIntent pintent){
		pollingHandler = new PollingHandler(ctx, pintent);;
	}
	
	public static PollingHandler getPollingHandler(){
		return pollingHandler;
	}
	
	public static void setContext(Context ctx){
		theCtx = ctx;
	}
	
	public static Context getContext(){
		return theCtx;
	}
		
	public static void showToast(final Context ctx, final String message, Handler handler) {
		
		DEBUG = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("toast", true);
		
		if (DEBUG == true) {
			handler.post(new Runnable() {  
					@Override  
					public void run() {  
						Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
					}});
		}
	}
	
	public static void showProgress(final Context ctx, final String message, Handler handler, int call, boolean force) {
		
		DEBUG = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("progress", false);
		if (force) {
			DEBUG = true;
		}
		if (progress != null) {
			//Services.stopProgress(handler, call);
			Services.getProgress().dismiss();
			Services.setProgress(null);
		}
		if (DEBUG == true && progress == null) { 
			//System.out.print("\n\nHandler:"+handler);
			handler.post(new Runnable() {  
					@Override  
					public void run() {
						//System.out.print("\n\nContext:"+ctx);
						Log.v(TAG+".showProgress", "Progress Bar Context:"+ctx);
						progress = ProgressDialog.show(ctx, "Syncing", message, true);//breaking here.
			}});
		}
	}
	
	public static void stopProgress(Handler handler, final int call){
		try {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			progress = null;
		} catch (IllegalArgumentException e) {
			//the window wasn't attached.
			progress = null;
			Log.e(TAG, e.getMessage());
		}
	}
	
	public static ProgressDialog getProgress(){
		return progress;
	}
	public static void setProgress(ProgressDialog prog){
		progress = prog;
	}
	
	public static class ColorFilterGenerator {
		/**
		 * Creates a HUE ajustment ColorFilter
		 * @see http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
		 * @see http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
		 * @param value degrees to shift the hue.
		 * @return
		 */
		public static ColorFilter adjustHue( float value )
		{
		    ColorMatrix cm = new ColorMatrix();
	
		    adjustHue(cm, value);
	
		    return new ColorMatrixColorFilter(cm);
		}
	
		/**
		 * @see http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
		 * @see http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
		 * @param cm
		 * @param value
		 */
		public static void adjustHue(ColorMatrix cm, float value)
		{
		    value = cleanValue(value, 180f) / 180f * (float) Math.PI;
		    if (value == 0)
		    {
		        return;
		    }
		    float cosVal = (float) Math.cos(value);
		    float sinVal = (float) Math.sin(value);
		    float lumR = 0.213f;
		    float lumG = 0.715f;
		    float lumB = 0.072f;
		    float[] mat = new float[]
		    { 
		            lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0, 
		            lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
		            lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0, 
		            0f, 0f, 0f, 1f, 0f, 
		            0f, 0f, 0f, 0f, 1f };
		    
		    cm.postConcat(new ColorMatrix(mat));
		}
		
		public static ColorFilter setColourGreen()
		{
		    ColorMatrix cm = new ColorMatrix();
	
		    setColourGreen(cm);
	
		    return new ColorMatrixColorFilter(cm);
		}
		
		public static void setColourGreen(ColorMatrix cm) {
			 float[] gMatrix = { 
			            0, 0, 0, 0, 0, //red
			            1, 1, 1, 1, 1, //green
			            0, 0, 0, 0, 0, //blue
			            1, 1, 1, 1, 1 //alpha
			        };
			    
			    cm.postConcat(new ColorMatrix(gMatrix));
		}
		
		public static ColorFilter setColourRed()
		{
		    ColorMatrix cm = new ColorMatrix();
	
		    setColourRed(cm);
	
		    return new ColorMatrixColorFilter(cm);
		}
		
		public static void setColourRed(ColorMatrix cm) {
			 float[] gMatrix = { 
			            1, 1, 1, 1, 1, //red
			            0, 0, 0, 0, 0, //green
			            0, 0, 0, 0, 0, //blue
			            1, 1, 1, 1, 1 //alpha
			        };
			    
			    cm.postConcat(new ColorMatrix(gMatrix));
		}
		
		public static ColorFilter setColourBlue()
		{
		    ColorMatrix cm = new ColorMatrix();
	
		    setColourBlue(cm);
	
		    return new ColorMatrixColorFilter(cm);
		}
		
		public static void setColourBlue(ColorMatrix cm) {
			 float[] gMatrix = { 
			            0, 0, 0, 0, 0, //red
			            0, 0, 0, 0, 0, //green
			            1, 1, 1, 1, 1, //blue
			            1, 1, 1, 1, 1 //alpha
			        };
			    
			    cm.postConcat(new ColorMatrix(gMatrix));
		}
	
		protected static float cleanValue(float p_val, float p_limit)
		{
		    return Math.min(p_limit, Math.max(-p_limit, p_val));
		}
	}
	
	
	public static class Statics {
		public static final int LASTVISITORS = 1;
		public static final int SWIPE = 3;
		public static final int FIRSTRUN = 10;
		public static final int NEWDATABASE = 12;
		public static final int CLASSSWIPE = 5;
		public static final int MANUALSWIPE = 13;
		//used for referencing various bundles and stuff stored in intents;
		public static final String DATE = "date";
		public static final String KEY = "key";
		public static final String IS_BOOKING = "booking";
		public static final String IS_BOOKING_F = "firstname";
		public static final String IS_BOOKING_S = "surname";
		public static final String MSID = "membershipid";
		public static final String MID = "memberid";
		public static final String VISIT = "visitdate";
		public static String PREF_NAME = "addMember";
		public static String PREF_KEY = "memberType";
		
		public static final String IS_SUCCESSFUL = "com.treshna.hornet.is_successful";
		public static final String IS_RESTART = "com.treshna.hornet.is_restart";
		public static final String IS_CLASSSWIPE = "com.treshna.hornet.class_swipe";
		
		public static enum FragmentType {
			MembershipAdd(1), MembershipComplete(2), MemberDetails(3), MemberGallery(4);
			
			private final int key;
			
			FragmentType(int thekey) {
				this.key = thekey;
			}
			
			public int getKey() {
 				return this.key;
 			}
			
		}
	}
}
