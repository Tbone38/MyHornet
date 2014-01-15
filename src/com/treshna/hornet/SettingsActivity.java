package com.treshna.hornet;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true; //TODO: fix the fragments here;
	private static Context ctx;
	private View other_settings_view;
	private AlertDialog other_settings_alert;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		ctx = this;
		Services.setContext(this);
		setupSimplePreferencesScreen();
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override //when the server sync finishes, it sends out a broadcast.
        public void onReceive(Context context, Intent intent) {
        	System.out.println("*INTENT RECIEVED*");
           SettingsActivity.this.receivedBroadcast(intent);
        }
    };

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}
		
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);
		
		Preference collect = createCollectData();
		getPreferenceScreen().addPreference(collect);
		createView();
		//createView();
		
		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_display);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_display);
		Preference doorlist = getDoorList();
		getPreferenceScreen().addPreference(doorlist);

		// Add 'data and sync' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_data_sync);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_data_sync);
		setupGymSpecificWindow();
		
		createDebugOpt();
		Preference clear = createClearData();
		getPreferenceScreen().addPreference(clear);
		Preference sync = createClearSync();
		getPreferenceScreen().addPreference(sync);
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("address"));
		bindPreferenceSummaryToValue(findPreference("port"));
		bindPreferenceSummaryToValue(findPreference("database"));
		bindPreferenceSummaryToValue(findPreference("username"));
		bindPreferenceSummaryToValue(findPreference("password"));
		bindPreferenceSummaryToValue(findPreference("door"));
		//bindPreferenceSummaryToValue(findPreference("resourcelist"));
		bindPreferenceSummaryToValue(findPreference("sync_frequency"));
	}
	
	protected void receivedBroadcast(Intent intent) {
		//restart the application.
		if (intent.getBooleanExtra(Services.Statics.IS_RESTART, false) == true) {
			Intent i = getBaseContext().getPackageManager()
		             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Services.setContext(this);
		if (Services.getProgress() != null) {
    		Services.getProgress().show();
		}
		IntentFilter iff = new IntentFilter();
	    iff.addAction("com.treshna.hornet.serviceBroadcast");
	    this.registerReceiver(this.mBroadcastReceiver,iff);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(this.mBroadcastReceiver);
		if (Services.getProgress() != null && Services.getProgress().isShowing()) {
    		Services.getProgress().dismiss();
    		//Services.setProgress(null);
    	}
	}
	
	//Currently used for YMCA specific features -- Roll call & parent-name ?
	private void setupGymSpecificWindow() {
		@SuppressWarnings("deprecation")
		Preference button = getPreferenceScreen().findPreference("specific_gym");
		button.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
				other_settings_view = inflater.inflate(R.layout.alert_settings_other, null);
				
				ToggleButton roll = (ToggleButton) other_settings_view.findViewById(R.id.enable_roll);
				int use_roll = Integer.parseInt(Services.getAppSettings(ctx, "use_roll"));
				if (use_roll > 0) {
					roll.setChecked(true);
				}
				
				TextView accept, cancel;
				accept = (TextView) other_settings_view.findViewById(R.id.button_apply_text);
				accept.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//get the value from the view.
						ToggleButton roll = (ToggleButton) other_settings_view.findViewById(R.id.enable_roll);
						SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.ctx);
						Editor e = preferences.edit();
						e.putString("use_roll", String.valueOf(Services.booltoInt(roll.isChecked())));
						e.commit();
						other_settings_alert.dismiss();
				}});
				
				cancel = (TextView) other_settings_view.findViewById(R.id.button_cancel_text);
				cancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						other_settings_alert.dismiss();
				}});
				
				AlertDialog.Builder build = new AlertDialog.Builder(SettingsActivity.ctx);
				build.setTitle("Gym Specific Settings");
				build.setView(other_settings_view);
				other_settings_alert = build.create();
				other_settings_alert.show();
				return true;
			}
			
		});
	}
	
	private Preference getDoorList() {
		ListPreference doorlist = new ListPreference(this);
		ContentResolver contentResolver = this.getContentResolver();
		Cursor cur = null;
		
		doorlist.setTitle(getString(R.string.pref_title_door));
		doorlist.setKey("door");
		
		List<String> entries = new ArrayList<String>();
		List<String> entryValues = new ArrayList<String>();
		
		cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, null, null, null);
		while (cur.moveToNext()) {
			entries.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORNAME)));
			entryValues.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORID)));
		}
		cur.close();
		String[] entriesA = new String[entries.size()];
		String[] entryValuesA = new String[entryValues.size()];
		for (int i=0;i<entries.size();i+=1) {
			entriesA[i] = entries.get(i);
			entryValuesA[i] = entryValues.get(i);
		}
		doorlist.setEntries(entriesA);
		doorlist.setEntryValues(entryValuesA);
		
		return doorlist;
	}

	private Preference createClearData() {
		Preference clearData = new Preference(this);
		clearData.setKey("clear");
		clearData.setTitle("Clear Application Data");
		clearData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
            	clearData();       	
            		Intent i = getPackageManager().getLaunchIntentForPackage( getApplicationContext().getPackageName() );
                	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                	startActivity(i);	
            	return true;
        }});
		return clearData;
	}
	
	private Preference createClearSync() {
		Preference clearSync = new Preference(this);
		clearSync.setKey("clearsync");
		clearSync.setTitle("Clear Last Sync Time");
		clearSync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
				Editor e = preferences.edit();
				e.putString("b_lastsync", String.valueOf(3)); //3 ms after epoch.
				e.putString("lastsync", String.valueOf(3)); //3 ms after epoch.
				e.putString("last_freq_sync", String.valueOf(3)); //3 ms after epoch.
				e.putString("last_infreq_sync", String.valueOf(3)); //3 ms after epoch.
				e.commit();
				//this should make the app redownload all data.
				return true;
			}
		});
		
		return clearSync;
	}
	
	public static Context getContext(){
		return ctx;
	}
	
	private Preference createCollectData(){
		Preference collectData = new Preference(this);
		collectData.setKey("collect");
		collectData.setTitle("Download Database");
		collectData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				collectData();				
				return true;
		}});
		
		return collectData;
	}
	private void collectData(){
		Intent updateInt = new Intent(this, HornetDBService.class);
		updateInt.putExtra(Services.Statics.KEY, Services.Statics.FIRSTRUN);
	 	this.startService(updateInt);
	}
	
	@SuppressWarnings("deprecation")
	private void createView() {
		
		//getPreferenceManager().findPreference(key)
		/*******Adds Bookings settings*******/
		ContentResolver contentResolver = this.getContentResolver();
		Cursor cur = null;
		PreferenceCategory category = new PreferenceCategory(this);
		category.setTitle("Bookings");
		getPreferenceScreen().addPreference(category);
		
		
		/***Resources***/
		ListPreference resource = new ListPreference(this);
		resource.setTitle(getString(R.string.pref_title_resource));
		resource.setKey("resourcelist");
		
		List<String> entries = new ArrayList<String>();
		List<String> entryValues = new ArrayList<String>();
		//String selectedid = Services.getAppSettings(this, "companylist");
		//cur = getResource(Integer.parseInt(selectedid), contentResolver);
		cur = contentResolver.query(ContentDescriptor.Resource.CONTENT_URI, null, null, null, null);
		while (cur.moveToNext()) {
			entries.add(cur.getString(2));
			entryValues.add(cur.getString(0));
		}
		cur.close();
		String[] entriesA = new String[entries.size()];
		String[] entryValuesA = new String[entryValues.size()];
		for (int i=0;i<entries.size();i+=1) {
			entriesA[i] = entries.get(i);
			entryValuesA[i] = entryValues.get(i);
		}
		resource.setEntries(entriesA);
		resource.setEntryValues(entryValuesA);
		category.addPreference(resource);
		
       
		/*******End Bookings settings*******/
	}
	
	@SuppressWarnings("deprecation")
	private void createDebugOpt(){
		PreferenceCategory debug = new PreferenceCategory(this);
		debug.setTitle("Debug Options");
		getPreferenceScreen().addPreference(debug);
		// progress on
		CheckBoxPreference progress = new CheckBoxPreference(this);
		progress.setKey("progress");
		progress.setTitle("Progress Box Visible");
		progress.setSummary("Checking this ensures sync progress is displayed on screen");
		progress.setChecked(false);
		debug.addPreference(progress);
		// debug toasts on
		CheckBoxPreference toast = new CheckBoxPreference(this);
		toast.setKey("toast");
		toast.setTitle("Show Toasts");
		toast.setSummary("Checking displays sync results in a toast on screen");
		toast.setChecked(true);
		debug.addPreference(toast);

		Preference copydb = new Preference(this);
		copydb.setKey("copydb");
		copydb.setTitle("Backup Database");
		copydb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FileHandler fh = new FileHandler(Services.getContext());
				fh.copyDatabase(false);
				return true;
		}});
		debug.addPreference(copydb);
		
		Preference restoredb = new Preference(this);
		restoredb.setKey("restoredb");
		restoredb.setTitle("Restore Backup");
		restoredb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FileHandler fh = new FileHandler(Services.getContext());
				fh.copyDatabase(true);
				return true;
		}});
		debug.addPreference(restoredb);
		
		Preference deletebackup = new Preference(this);
		deletebackup.setKey("deletebackup");
		deletebackup.setTitle("Delete Backup");
		deletebackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FileHandler fh = new FileHandler(Services.getContext());
				fh.deleteFile("../"+HornetDatabase.DATABASE_NAME);
				return true;
		}});
		debug.addPreference(deletebackup);
	}
	
	private static void clearData(){
		Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).edit();
		editor.clear();
		editor.commit();
		//does this break things?
		ContentResolver contentResolver =  MainActivity.getContext().getContentResolver();
		ContentResolver.cancelSync(null, ContentDescriptor.AUTHORITY);
		contentResolver.delete(ContentDescriptor.DROPTABLE_URI, null, null);
		Toast.makeText(MainActivity.getContext(), "Data Cleared, restarting Application.", Toast.LENGTH_LONG).show();
		//return result;
	}
	

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS;
				/*|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);*/
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
			/*
			 * This is the code that builds headers/fragments.
			 */
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			}  else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		if (preference.getKey().compareTo("password") == 0) {
			String pass = PreferenceManager.getDefaultSharedPreferences(
					preference.getContext()).getString(preference.getKey(),
					"");
			String stars = "";
			for (int i=0; i<pass.length(); i+=1){
				stars += "*";
			}
			pass = "";
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference, stars);
		} else {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
		}
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
		
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("address"));
			bindPreferenceSummaryToValue(findPreference("port"));
			bindPreferenceSummaryToValue(findPreference("database"));
			bindPreferenceSummaryToValue(findPreference("username"));
			bindPreferenceSummaryToValue(findPreference("password"));
			bindPreferenceSummaryToValue(findPreference("door"));
			//bindPreferenceSummaryToValue(findPreference("resourcelist"));
			//bindPreferenceSummaryToValue(findPreference("date"));
		}
	}
	
	/**
	 * This fragment shows booking preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class BookingPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_bookings);
			
			
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
		}
	}

	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_display);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
		}
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		}
	}
}
