package com.treshna.hornet;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MembershipHold extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.membership_hold);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.membership_hold, menu);
		return true;
	}

}
