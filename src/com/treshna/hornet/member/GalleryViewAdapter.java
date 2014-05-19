package com.treshna.hornet.member;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.services.BitmapLoader;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class GalleryViewAdapter extends SimpleCursorAdapter implements OnClickListener, OnCheckedChangeListener{
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	Cursor cur;
	private int imageWidth; //was 130.
	private GridView list;
	private MemberGalleryFragment parentfrag;
	
	private static final String TAG = "GALLERY";
	
	@SuppressWarnings("deprecation")
	public GalleryViewAdapter(MemberGalleryFragment frag, int layout, Cursor c,
			String[] from, int[] to, int imageWidth, GridView parent ) {
		super(frag.getActivity(), layout, c, from, to);
		this.context = frag.getActivity();
		this.FROM = from;
		this.imageWidth = imageWidth;
		this.cur = c; 
		this.list = parent;
		this.parentfrag = frag;
	}
	
	@Override
	public Cursor swapCursor(Cursor c) {
		this.cur = c;
		
		return super.swapCursor(c);
	}
	
	@Override
	public void changeCursorAndColumns(Cursor c, String[] from, int[] to) {
		this.cur = c;
		super.changeCursorAndColumns(c, from, to);
	}
	
	@Override
	public void bindView(View rowLayout, Context context, Cursor cursor){
		super.bindView(rowLayout, context, cursor);
		
		ImageView image = (ImageView) rowLayout.findViewById(R.id.member_gallery_image);
		TextView takenView = (TextView) rowLayout.findViewById(R.id.date);
		CheckBox checkbox = (CheckBox) rowLayout.findViewById(R.id.is_profile);
		
		if (checkbox == null) {

		} else {
			checkbox.setId(cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Image.Cols.ID)));
			checkbox.setTag(cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Image.Cols.MID)));
			checkbox.setOnCheckedChangeListener(this);
			
			if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Image.Cols.IS_PROFILE)) == 1) {
				checkbox.setChecked(true);
			}
		}
		if (takenView == null) {
			//Log.e("GALLERY", "TEXT VIEW NULL TOO ... ?");
		} else {
			if (!cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Image.Cols.DATE))) {
				Date date = new Date(cursor.getLong(cursor.getColumnIndex(ContentDescriptor.Image.Cols.DATE)));
				takenView.setText("Taken: "+Services.DateToString(date));
			} else {
				takenView.setVisibility(View.GONE);
			}
		}
		
	    ArrayList<String> tag = new ArrayList<String>();
	    tag.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Image.Cols.IID)));
	    tag.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Image.Cols.MID)));
	    
		String imgDir = context.getExternalFilesDir(null)+"/"+tag.get(0)+"_"+tag.get(1)+".jpg";
		File imgFile = new File(imgDir);
				
		if (imgFile.exists() == true) {
			new BitmapLoader(imgFile,image,imageWidth,imageWidth, Integer.parseInt(tag.get(1)));		   
		    image.setTag(tag);
		    image.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.member_gallery_image):{
			Cursor cur;
			@SuppressWarnings("unchecked")
			ArrayList<String> tag = (ArrayList<String>) v.getTag();
			ContentResolver contentResolver = context.getContentResolver();
			String selection = ContentDescriptor.Image.Cols.IID+" = "+tag.get(0)
					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+tag.get(1);
			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, selection, null, null);
			if (cur.getCount() <= 0) break;
			cur.moveToFirst();
			String date = Services.DateToString(new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE))));
			String message = "Image Taken: "+date+ "\nImage Description: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.DESCRIPTION));
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			break;	
		}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int mid = (Integer) buttonView.getTag();
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.MID+" = ?",
				new String[] {String.valueOf(mid)}, null);
		
		if (isChecked) {
			while (cur.moveToNext()) {
				if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID))== buttonView.getId()) {
					//its our image.
					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.Image.Cols.IS_PROFILE, 1);

					contentResolver.update(ContentDescriptor.Image.CONTENT_URI, values, ContentDescriptor.Image.Cols.ID+" = ?",
							new String[] {String.valueOf(buttonView.getId())});
				} else if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IS_PROFILE)) == 1) {
					//this image is already the current profile, update it's value and check box.
					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.Image.Cols.IS_PROFILE, 0);
					
					contentResolver.update(ContentDescriptor.Image.CONTENT_URI, values, ContentDescriptor.Image.Cols.ID+" = ?",
							new String[] {String.valueOf(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID)))});
					CheckBox checkbox = (CheckBox) list.findViewById(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID)));
					if (checkbox != null) {
						checkbox.setChecked(false);
					} else {
						//this may cause recursive issues.
						parentfrag.getLoader().restartLoader(0, null, parentfrag);
					}
				}
			}
		} else {
			boolean hasChecked = false;
			while (cur.moveToNext()) {
				CheckBox checkbox = (CheckBox) list.findViewById(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID)));
				if (checkbox != null && checkbox.isChecked())
					hasChecked = true;
			}
			if (!hasChecked) {
				//we've unchecked out box, without checking another.
				buttonView.setChecked(true);
			}
		}
		cur.close();
	}
}
