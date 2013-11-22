package com.treshna.hornet;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.treshna.hornet.MembersFindFragment.OnMemberSelectListener;
import com.treshna.hornet.R.color;

public class MembersFindAdapter extends SimpleCursorAdapter implements OnClickListener {
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	private boolean IS_BOOKING; //0 = find member, 1 = select member for booking
	private OnMemberSelectListener mCallback;
	private static final String TAG = "MemberFindAdapter";
	
	private int selectedPos = -1;
	
	@SuppressWarnings("deprecation")
	public MembersFindAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, boolean booking, OnMemberSelectListener act) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
		IS_BOOKING = booking;
		mCallback = act;
		
	}
	
	
	@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);
		//TextView name = (TextView) rowLayout.findViewById(R.id.name);
		/*for (int i=0; i<cursor.getColumnCount(); i++){
			System.out.print("\n\nColumn:"+i+" Name:"+cursor.getColumnName(i)+"  Value:"+cursor.getString(i));
		}*/
		View colour_block = (View) rowLayout.findViewById(R.id.member_row_colour_block);
		if (selectedPos == cursor.getPosition()) {
			colour_block.setBackgroundColor(context.getResources().getColor(R.color.member_blue));
		} else {
			colour_block.setBackgroundColor(context.getResources().getColor(R.color.member_grey));
		}
		if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Member.Cols.STATUS)) == 2) {
			//expired!
			rowLayout.setClickable(true);
			rowLayout.setOnClickListener(this);
			ArrayList<String> tagInfo = new ArrayList<String>();
			tagInfo.add(cursor.getString(1));  //cursor.getColumnIndex(ContentDescriptor.Member.Cols.MID)
			tagInfo.add(null);
			tagInfo.add(String.valueOf(cursor.getPosition()));
			rowLayout.setTag(tagInfo);
			
			TextView details = (TextView) rowLayout.findViewById(R.id.details);
			if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)) &&
					cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)).compareTo("") != 0) {
				details.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME))
						+"\n Expired "+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)));
				details.setVisibility(View.VISIBLE);
			} else {
				details.setVisibility(View.INVISIBLE);
			}
		} else if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Member.Cols.STATUS)) == 1) {
			//suspended!
			ArrayList<String> tagInfo = new ArrayList<String>();
			
			rowLayout.setBackgroundColor(Color.WHITE);
			tagInfo.add(cursor.getString(1)); //cursor.getColumnIndex(ContentDescriptor.Member.Cols.MID)
			tagInfo.add(null);
			tagInfo.add(String.valueOf(cursor.getPosition()));
			rowLayout.setTag(tagInfo);
			rowLayout.setClickable(true);
			rowLayout.setOnClickListener(this);
			TextView details = (TextView) rowLayout.findViewById(R.id.details);
			if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)) &&
					cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)).compareTo("") != 0) {
				details.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME))+"\n\nOn Suspension");
				details.setVisibility(View.VISIBLE);
			} else {
				details.setVisibility(View.INVISIBLE);
			}
		} else {
			ArrayList<String> tagInfo = new ArrayList<String>();
			
			rowLayout.setBackgroundColor(Color.WHITE);
			tagInfo.add(cursor.getString(1)); //cursor.getColumnIndex(ContentDescriptor.Member.Cols.MID) 
			tagInfo.add(null);
			tagInfo.add(String.valueOf(cursor.getPosition()));
			rowLayout.setTag(tagInfo);
			rowLayout.setClickable(true);
			rowLayout.setOnClickListener(this);
			TextView details = (TextView) rowLayout.findViewById(R.id.details);
			if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)) &&
					cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)).compareTo("") != 0) {
				details.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
				details.setVisibility(View.VISIBLE);
			} else {
				details.setVisibility(View.INVISIBLE);
			}
		}
		
		TextView memberName = (TextView) rowLayout.findViewById(R.id.name);
		memberName.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))+" "
				+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		if (cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR)) == true ||
				cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR)).compareTo("") ==0) {
			if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Member.Cols.STATUS)) == 1 
					|| cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Member.Cols.STATUS)) == 2) {
				memberName.setTextColor(color.greyout);
			} else {
				memberName.setTextColor(Color.BLACK);
			}
		} else {
			memberName.setTextColor(Color.BLACK);
		}
		
		String imgDir = context.getExternalFilesDir(null)+"/0_"+cursor.getString(1)+".jpg"; //cursor.getColumnIndex(ContentDescriptor.Member.Cols.MID)
		File imgFile = new File(imgDir);
		ImageView imageView = (ImageView) rowLayout.findViewById(R.id.rowimage);
		if (imgFile.exists() == true){
			imageView.setVisibility(View.VISIBLE);
			final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
		    // Calculate inSampleSize
		    options.inSampleSize = Services.calculateInSampleSize(options,80, 80);
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
		    imageView.setImageBitmap(bm);
		}
		else {
			imageView.setVisibility(View.INVISIBLE);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * Handles Photos
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		//do Photo taking stuff here.
		switch(v.getId()){
		case(R.id.listRow):{
			if (!IS_BOOKING) {
				Log.v(TAG, "Row Selected, Displaying More Info:");
				ArrayList<String> tagInfo;
				if (v.getTag() instanceof ArrayList<?>){
					tagInfo = (ArrayList<String>) v.getTag();
				} else {
					break;
				}
				
				selectedPos = Integer.parseInt(tagInfo.get(2));
				View colour_block = (View) v.findViewById(R.id.member_row_colour_block);
				colour_block.setBackgroundColor(context.getResources().getColor(R.color.member_blue));
				
				mCallback.onMemberSelect(tagInfo.get(0));
				
			} else if (IS_BOOKING) {
				if (mCallback != null) {
					ArrayList<String> tagInfo = (ArrayList<String>) v.getTag();
					mCallback.onMemberSelect(tagInfo.get(0));
					//remove this from stack;
				}
			}
			break; }
		}
		
    }
	
	
	
}
