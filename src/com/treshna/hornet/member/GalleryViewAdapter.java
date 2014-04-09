package com.treshna.hornet.member;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.treshna.hornet.R;
import com.treshna.hornet.services.BitmapLoader;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class GalleryViewAdapter extends SimpleCursorAdapter implements OnClickListener{
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	Cursor cursor;
	private int imageWidth; //was 130.
	
	@SuppressWarnings("deprecation")
	public GalleryViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int imageWidth ) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
		this.imageWidth = imageWidth;
	}
	
	@Override
	public void bindView(View rowView, Context context, Cursor cursor) {
		ImageView image = (ImageView) rowView.findViewById(R.id.member_gallery_image);
		

	    ArrayList<String> tag = new ArrayList<String>();
	    tag.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Image.Cols.DISPLAYVALUE)));
	    tag.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Image.Cols.MID)));
	    
		String imgDir = context.getExternalFilesDir(null)+"/"+tag.get(0)+"_"+tag.get(1)+".jpg";
		File imgFile = new File(imgDir);
				
		if (imgFile.exists() == true) {
			new BitmapLoader(imgFile,image,imageWidth,imageWidth);		   
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
			String selection = ContentDescriptor.Image.Cols.DISPLAYVALUE+" = "+tag.get(0)
					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+tag.get(1);
			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, selection, null, null);
			if (cur.getCount() <= 0) break;
			cur.moveToFirst();
			String date = Services.dateFormat(cur.getString(2), "dd MMM yy hh:mm:ss aa", "dd MMM yyyy");
			String message = "Image Taken: "+date+ "\nImage Description: "+cur.getString(3);
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			break;	
		}
		}
	}
}
