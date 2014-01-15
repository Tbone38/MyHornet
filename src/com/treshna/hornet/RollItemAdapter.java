package com.treshna.hornet;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RollItemAdapter extends SimpleCursorAdapter implements OnCheckedChangeListener {
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	Cursor cursor;
	private static final String TAG = "RollItemAdapter";
	
	@SuppressWarnings("deprecation")
	public RollItemAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
	}
	
	@Override
	public void bindView(View rowView, Context context, Cursor cursor) {
		//
		TextView name_view = (TextView) rowView.findViewById(R.id.roll_item_name);
		name_view.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))+" "
				+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		
		Log.d(TAG, "NAME:"+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))+" "
				+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		
		CheckBox attended_box = (CheckBox) rowView.findViewById(R.id.roll_item_attended);
		if (cursor.getString(cursor.getColumnIndex(ContentDescriptor.RollItem.Cols.ATTENDED)).compareToIgnoreCase("f")==0) {
			attended_box.setChecked(false);
		} else {
			attended_box.setChecked(true);
		}
		attended_box.setTag(cursor.getInt(cursor.getColumnIndex(ContentDescriptor.RollItem.Cols._ID)));
		attended_box.setOnCheckedChangeListener(this);
		setColour(rowView);
	}
	
	private void setColour(View rowView){
		View colour_block = rowView.findViewById(R.id.roll_item_colour_block);
		CheckBox check = (CheckBox) rowView.findViewById(R.id.roll_item_attended);
		if (check.isChecked()) {
			colour_block.setBackgroundColor(context.getResources().getColor(R.color.visitors_green));
		} else {
			colour_block.setBackgroundColor(context.getResources().getColor(R.color.visitors_red));
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//get the id,
		//update the row.
		int rowid = 0;
		String attended="f";
		
		if (buttonView.getTag() instanceof Integer) {
			rowid = (Integer) buttonView.getTag();
		}
		
		if (isChecked) {
			attended = "t";
		}
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.RollItem.Cols.ATTENDED, attended);
		values.put(ContentDescriptor.RollItem.Cols.DEVICESIGNUP, "t");
		ContentResolver contentResolver = context.getContentResolver();
		contentResolver.update(ContentDescriptor.RollItem.CONTENT_URI, values, ContentDescriptor.RollItem.Cols._ID+" = ?",
				new String[] {String.valueOf(rowid)});
		setColour((View)buttonView.getParent());
	}
	
}
