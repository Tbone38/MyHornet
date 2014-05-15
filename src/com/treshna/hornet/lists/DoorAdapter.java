package com.treshna.hornet.lists;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.treshna.hornet.R;
import com.treshna.hornet.sqlite.ContentDescriptor;

public class DoorAdapter extends AdapterSuperClass {
	
	public DoorAdapter(Activity activity, int layout, Cursor c,
			String[] from, int[] to, ListerClass lister) {
		super(activity, layout, c, from, to, lister);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BookingTypeHolder holder;
		
		if (convertView == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			convertView = inflater.inflate(row_id, parent, false);
			
			holder = new BookingTypeHolder();
			holder.id = (TextView) convertView.findViewById(R.id.programme_group_name);
			holder.name = (TextView) convertView.findViewById(R.id.programme_group_cards);
			holder.status = (TextView) convertView.findViewById(R.id.programme_group_historic);
			
			convertView.setTag(holder);
		} else {
			holder = (BookingTypeHolder) convertView.getTag();
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
		holder.id.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORID)));
		holder.name.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORNAME)));
		switch (cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.STATUS))) {
		case (0):
			holder.status.setText("Locked");
			break;
		case (2):
			holder.status.setText("Open/Unlocked");
			break;
		default: //1
			holder.status.setText("Active");
			break;
		}
		
		convertView.setClickable(true);
		convertView.setOnClickListener(this);
		
		return convertView;
	}
	
	private static class BookingTypeHolder extends ViewHolder {
		TextView id;
		TextView name;
		TextView status;
	}
}
