package com.treshna.hornet.services;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.treshna.hornet.network.PollingHandler;

public class Services {
	
	private static PollingHandler pollingFreqHandler;
	//private static PollingHandler pollingInfreqHandler;
	private static boolean DEBUG;
	private static ProgressDialog progress;
	private static Context theCtx;
	private static final String TAG = "Services";
	
	private static Activity theActivity;
	private static boolean activityVisible = false;
	
	
	/** Always returns date format as "dd MMM yyyy"
	 * 
	 */
	public static String DateToString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		return format.format(date);
	}
	
	public static String DateToString(Date date, String outputformat) {
		SimpleDateFormat format = new SimpleDateFormat(outputformat, Locale.US);
		return format.format(date);
	}
	
	/**
	 * Returns Date based on string format or null
	 */
	public static Date StringToDate(String datestring, String dateformat) {
		SimpleDateFormat format = new SimpleDateFormat(dateformat, Locale.US);
		Date date;
		try {
			date = format.parse(datestring);
		} catch (ParseException e) {
			//Log.e(TAG, "Error Parsing Date:", e);
			date = null;
		} catch (NullPointerException e) {
			//Log.e(TAG, "Error Parsing Date:", e);
			date = null;
		}
		return date;
	}
	
	/**
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
			//Log.e(TAG, "Error Parsing Date:", e);
			//this should return the original date.
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
		} else if (height < reqHeight && width < reqWidth) {
			 while (options.outWidth / inSampleSize / 2 >= reqWidth
	                    && options.outHeight / inSampleSize / 2 >= reqHeight)
	                inSampleSize *= 2;
		}
		return inSampleSize;
	}
	public static Bitmap decodeSampledBitmapFromFile(File imgFile, 
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
	    // Calculate inSampleSize
	    options.inSampleSize = Services.calculateInSampleSize(options,reqWidth, reqHeight);
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    try {
	    	return BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
	    } catch (OutOfMemoryError e) {
	    	return null;
	    }
	   
	}
	
	/*
	 * This function allows the application to manually set the value of an 
	 * application property. (setting)
	 */
	@SuppressLint("InlinedApi")
	public static void setPreference(Context context, String key, String value) {
		SharedPreferences preferences;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			preferences = context.getSharedPreferences(context.getPackageName()+"_preferences", Context.MODE_MULTI_PROCESS);
		} else {
			preferences = PreferenceManager.getDefaultSharedPreferences(context);
		}
		Editor e = preferences.edit();
		e.putString(key, value);
		e.commit();
	}
	
	public static String getAppSettings(Context context, String key){
		 //Exception e = new Exception();
		 Log.d(TAG, "Getting App Setting: "+key);
		 SharedPreferences preferences;
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			 preferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		 } else {
			 preferences = PreferenceManager.getDefaultSharedPreferences(context);
		 }
		 return (preferences.getString(key, "-1"));
	}
	
	//converts a DP input into pixels
	 //not used for anything?
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
	
	@SuppressWarnings("deprecation")
	public static int getScreenWidth(Context _context) {
        int columnWidth;
        WindowManager wm = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
 
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        
        if (_context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        	columnWidth = point.x;
        } else {
        	columnWidth = point.y/2; //we could be in the fragment view, so 1/2 the screen ?
        }
        
        return columnWidth;
    }
	
	//used for the is_profile column in the Image Table.
	public static int booltoInt(boolean input){
		return (input == true)? 1 : 0;
	}
	
	public static boolean isProfile(int boolValue){
		return (boolValue == 1)? true : false;
	}
	
	public static void setFreqPollingHandler(Context ctx, PendingIntent pintent){
		pollingFreqHandler = new PollingHandler(ctx, pintent);
	}
	
	public static PollingHandler getFreqPollingHandler(){
		return pollingFreqHandler;
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/*public static void setInfreqPollingHandler(Context ctx, PendingIntent pintent){
		pollingInfreqHandler = new PollingHandler(ctx, pintent);
	}
	
	public static PollingHandler getInfreqPollingHandler(){
		return pollingInfreqHandler;
	}*/
	
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
						if (message != null && !message.isEmpty() && message.length() >= 5) {
							Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
						}
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
		            0f, 0f, 0f, 1f, 0f }; 
		            //0f, 0f, 0f, 0f, 1f };
		    
		    cm.postConcat(new ColorMatrix(mat));
		}
		
		/**
		 * Returns a colour filter for the given colour. 
		 * @param color int,
		 * @return colorFilter ColorFilter,
		 */
		public static ColorFilter setColour(int color) {
			ColorMatrix cm = new ColorMatrix();
			
			int  red, green, blue;
			//ignore alpha, else we end up with a solid color block.
			//or write special colours just for this?
			red = Color.red(color);
			green = Color.green(color);
			blue = Color.blue(color);
			
			float[] gMatrix = {
					0, 0, 0, 0, red,
					0, 0, 0, 0, green,
					0, 0, 0, 0, blue,
					1, 1, 1, 1, 0
			};
			
			cm.postConcat(new ColorMatrix(gMatrix));
			return new ColorMatrixColorFilter(cm);
		}
		
		protected static float cleanValue(float p_val, float p_limit)
		{
		    return Math.min(p_limit, Math.max(-p_limit, p_val));
		}
	}
	
	//IT WORKS!
	public static class Typefaces {
		private static final String TAG = "Typefaces";

		private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

		public static Typeface get(Context c, String assetPath) {
			synchronized (cache) {
				if (!cache.containsKey(assetPath)) {
					try {
						Typeface t = Typeface.createFromAsset(c.getAssets(),
								assetPath);
						cache.put(assetPath, t);
					} catch (Exception e) {
						Log.e(TAG, "Could not get typeface '" + assetPath
								+ "' because " + e.getMessage());
						return null;
					}
				}
				return cache.get(assetPath);
			}
		}
	}
	
	
	public static class Statics {
		public static final int FREQUENT_SYNC = -1;
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
		public static final String ROLLID = "roll_id";
		public static String PREF_NAME = "addMember";
		public static String PREF_KEY = "memberType";
		
		public static final String IS_SUCCESSFUL = "com.treshna.hornet.is_successful";
		public static final String IS_RESTART = "com.treshna.hornet.is_restart";
		public static final String IS_CLASSSWIPE = "com.treshna.hornet.class_swipe";
		
		public static final String ERROR_MSHOLD1 = "ERROR: Membership hold/free time dates are invalid. "
				+ "Dates of free time and holds must overlap an existing membership dates.";
		public static final String ERROR_MSHOLD2 = "ERROR: This member is already suspended from";
		public static final String ERROR_MSHOLD3 = "ERROR: Membership hold/free time date is invalid. "
				+ "It must start after an existing membership, not before it. ";
		
		public static enum FragmentType {
			MembershipAdd(1), MembershipComplete(2), MemberDetails(3), MemberGallery(4),
			RollList(5), RollItemList(6), MemberAddTag(7), KPIs(8), Resource(9);
			
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
