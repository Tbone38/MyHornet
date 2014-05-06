package com.treshna.hornet.form;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.treshna.hornet.R;
import com.treshna.hornet.sqlite.ContentDescriptor;

public class ProgrammeGroupAdapter extends SimpleCursorAdapter implements OnClickListener {
	
	private Cursor cur;
	private Activity mActivity;
	private int row_id;
	private ListerClass mLister;
	
	public ProgrammeGroupAdapter(Activity activity, int layout, Cursor c,
			String[] from, int[] to, ListerClass lister) {
		super(activity, layout, c, from, to);
		cur = c;
		mActivity = activity;
		row_id = layout;
		mLister = lister;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			convertView = inflater.inflate(row_id, parent, false);
			
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.programme_group_name);
			holder.issuecards = (TextView) convertView.findViewById(R.id.programme_group_cards);
			holder.historic = (TextView) convertView.findViewById(R.id.programme_group_historic);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (position%2==0) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				convertView.setBackground(mActivity.getResources().getDrawable(R.drawable.selector_alt));
			}else {
				convertView.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.selector_alt));
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				convertView.setBackground(mActivity.getResources().getDrawable(R.drawable.selector));
			} else {
				convertView.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.selector));
			}
		}
		
		cur.moveToPosition(position);
		
		holder.position = position;
		holder.name.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.NAME)));
		holder.issuecards.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.ISSUECARD)));
		holder.historic.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.ProgrammeGroup.Cols.HISTORIC)));
		
		convertView.setClickable(true);
		convertView.setOnClickListener(this);
		
		return convertView;
	}
	
	@Override
	public void changeCursor(Cursor c) {
		super.changeCursor(c);
		cur = c;
	}
	
	private static class ViewHolder {
		TextView name;
		TextView issuecards;
		TextView historic;
		int position = -1;
	}

	@Override
	public void onClick(View v) {
		v.setSelected(true);
		mLister.setPosition(((ViewHolder)v.getTag()).position);
		mLister.startActionMode(v);
		
	}
}
