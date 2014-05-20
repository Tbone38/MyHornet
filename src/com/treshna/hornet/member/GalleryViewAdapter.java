package com.treshna.hornet.member;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
	
	private static class ViewHolder {
		
		public ViewHolder() {};
		ImageView image;
		TextView takenView;
		CheckBox checkbox;
	}
	
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
	public void changeCursor(Cursor c) {
		super.changeCursor(c);
		this.cur = c;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		super.getView(position, convertView, parent);
		ViewHolder vh;
		cur.moveToPosition(position);
		if (convertView == null) {
			//inflate a convertView
			LayoutInflater inflater = parentfrag.getActivity().getLayoutInflater();
			convertView = inflater.inflate(R.layout.row_member_gallery, null);
			vh = new ViewHolder();
			vh.image = (ImageView) convertView.findViewById(R.id.member_gallery_image);
			vh.takenView = (TextView) convertView.findViewById(R.id.date);
			vh.checkbox = (CheckBox) convertView.findViewById(R.id.is_profile);
			
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		if (vh.checkbox != null) { //i guess this is because sometime the view is outside the visible area, which means its null?
			//because of the way grids/lists implement view recycling?
			vh.checkbox.setId(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.ID)));
			vh.checkbox.setTag(cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID)));
			vh.checkbox.setOnCheckedChangeListener(this);
			
			if (cur.getInt(cur.getColumnIndex(ContentDescriptor.Image.Cols.IS_PROFILE)) == 1) {
				vh.checkbox.setChecked(true);
			} else {
				vh.checkbox.setChecked(false);
			}
		}
	
		if (vh.takenView != null) {
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE))) {
				Date date = new Date(cur.getLong(cur.getColumnIndex(ContentDescriptor.Image.Cols.DATE)));
				vh.takenView.setText("Taken: "+Services.DateToString(date));
			} else {
				vh.takenView.setVisibility(View.GONE);
			}
		}

		ArrayList<String> tag = new ArrayList<String>();
	    tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.IID)));
	    tag.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Image.Cols.MID)));
	    
		String imgDir = context.getExternalFilesDir(null)+"/"+tag.get(0)+"_"+tag.get(1)+".jpg";
		File imgFile = new File(imgDir);
				
		if (imgFile.exists() == true) {
			new BitmapLoader(imgFile,vh.image,imageWidth,imageWidth, Integer.parseInt(tag.get(1)));		   
		    vh.image.setTag(tag);
		    vh.image.setOnClickListener(this);
		} else {
			vh.image.setImageBitmap(null);
		}
		
		return convertView;
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
