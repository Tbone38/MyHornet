package com.treshna.hornet;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class MemberFind extends FragmentActivity {
	
	FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_find);
		Services.setContext(this);
		// Show the Up button in the action bar.
		setupActionBar();
		Services.setContext(this);
		fragmentManager = getSupportFragmentManager();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {
		    // Handle item selection
		    switch (item.getItemId()) {
		    case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		    case (R.id.action_settings):
		    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
		    	startActivity(settingsIntent);
		    	return true;
		    case (R.id.action_scan):
		    	Intent scanIntent = new Intent(this, HornetRFIDReader.class);
		    	startActivity(scanIntent);
		    	return true;
		    case (R.id.action_update): {
		    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
			 		Services.setPreference(this, "sync_frequency", "5");
			 	}
			 	PollingHandler polling = Services.getPollingHandler();
		    	polling.startService();
		    	return true;
		    }
		    case (R.id.action_halt): {
		    	PollingHandler polling = Services.getPollingHandler();
		    	polling.stopPolling(false);
		    	Services.setPreference(this, "sync_frequency", "-1");
		    	return true;
		    }
		    case (R.id.action_bookings):{
		    	Intent bookings = new Intent(this, HornetDBService.class);
				bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
			 	this.startService(bookings);
		    	
			 	Intent intent = new Intent(this, BookingsSlidePager.class);
		       	startActivity(intent);
		       	return true;
		    }
		    case (R.id.action_addMember):{
		    	Intent intent = new Intent(this, AddMember.class);
		    	startActivity(intent);
		    	return true;
		    }
		    case (R.id.action_findMember):{
		    	Intent i = new Intent(this, MemberFind.class);
		    	startActivity(i);
		    	return true;
		    }
		    case (R.id.action_visitors):{
		    	Intent intent = new Intent(this, DisplayResultsActivity.class);
				intent.putExtra(Services.Statics.KEY,DisplayResultsActivity.LASTVISITORS); 
				startActivity(intent);
		    	return true;
		    }
		    default:
		    	return super.onOptionsItemSelected(item);
		}       
	 }
}
