package com.treshna.hornet.lists;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;


public class AdapterSuperClass extends SimpleCursorAdapter implements OnClickListener {
	
	protected Cursor cur;
	protected Activity mActivity;
	protected int row_id;
	protected ListerClass mLister;
	
	public AdapterSuperClass(Activity activity, int layout, Cursor c,
			String[] from, int[] to, ListerClass lister) {
		super(activity, layout, c, from, to);
		cur = c;
		mActivity = activity;
		row_id = layout;
		mLister = lister;
	}

	@Override
	public void changeCursor(Cursor c) {
		super.changeCursor(c);
		cur = c;
	}
	
	@Override
	public void onClick(View v) {
		v.setSelected(true);
		mLister.setPosition(((ViewHolder)v.getTag()).position);
		mLister.startActionMode(v);
		
	}
}
