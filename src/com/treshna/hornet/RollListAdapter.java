package com.treshna.hornet;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RollListAdapter extends SimpleCursorAdapter {
	
	Context context;
	String[] FROM;
	Cursor cursor;
	private static final String TAG = "RollListAdapter";
	
	@SuppressWarnings("deprecation")
	public RollListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
	}
	
	@Override
	public void bindView(View rowView, Context context, Cursor cursor) {
		if (cursor.isNull(0)) return;
		TextView name = (TextView) rowView.findViewById(R.id.roll_list_name);
		name.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.RollCall.Cols.NAME)));
		
		TextView datetime = (TextView) rowView.findViewById(R.id.roll_list_datetime);
		datetime.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.RollCall.Cols.DATETIME)));
		
		int attended = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContentDescriptor.RollItem.Cols.ATTENDED)));
		int total = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContentDescriptor.RollItem.Cols.TOTAL)));
		TextView attend_count = (TextView) rowView.findViewById(R.id.roll_list_attend_count);
		attend_count.setText(attended+"/"+total);
		
		double result = 0;
		if (total != 0) {
			result = (attended/total);
		}
		//we show success for having more than 90% ?
		View colour_block = (View) rowView.findViewById(R.id.roll_list_colour_block);
		if (result >= 0.90d) {
			colour_block.setBackgroundColor(context.getResources().getColor(R.color.visitors_green));
		} else {
			colour_block.setBackgroundColor(context.getResources().getColor(R.color.visitors_red));
		}
	}
}
