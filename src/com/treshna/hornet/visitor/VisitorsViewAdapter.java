package com.treshna.hornet.visitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.treshna.hornet.R;
import com.treshna.hornet.R.color;
import com.treshna.hornet.R.id;
import com.treshna.hornet.services.BitmapLoader;
import com.treshna.hornet.services.CameraWrapper;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class VisitorsViewAdapter extends SimpleCursorAdapter {
	
	public final static String EXTRA_ID = "com.treshna.hornet.ID";
	Context context;
	String[] FROM;
	Cursor cursor;
	private static int REQ_WIDTH = 100;
	private static int REQ_HEIGHT = 100;
	private boolean showDoorname = false;
	
	private OnClickListener theClicker;
	
	@SuppressWarnings("deprecation")
	public VisitorsViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, OnClickListener clicker) {
		super(context, layout, c, from, to);
		this.context = context;
		this.FROM = from;
		this.theClicker = clicker;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
	    showDoorname = preferences.getBoolean("show_doorname", false);
	}
	
	@Override
	public void bindView(View rowView, Context context, Cursor cursor) {
		
		//TextView imageText = (TextView) rowView.findViewById(R.id.imageText);
		TextView nameView = (TextView) rowView.findViewById(R.id.name);	
		TextView timeView = (TextView) rowView.findViewById(R.id.time);
		TextView denyView = (TextView) rowView.findViewById(R.id.deny);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
		ImageView smileView = (ImageView) rowView.findViewById(R.id.smiley);
		//ImageView taskView = (ImageView) rowView.findViewById(R.id.task);
		View colorBlock = (View) rowView.findViewById(R.id.visitor_colour_block);
		colorBlock.setBackgroundColor(context.getResources().getColor(R.color.visitors_green));
				
		ArrayList<String> tagInfo = new ArrayList<String>();
		tagInfo.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.MID))); 
		tagInfo.add(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DATETIME)));
		rowView.setTag(tagInfo);
		rowView.setClickable(true);
		
		if (theClicker != null) {
			rowView.setOnClickListener(theClicker);
		} else {
			//rowView.setOnClickListener(this);
		}
		if (cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Member.Cols.MID))) {
			rowView.setClickable(false);
			nameView.setTextColor(Color.parseColor("#C4C4C4"));
			timeView.setTextColor(Color.parseColor("#C4C4C4"));
			//imageText.setTextColor(Color.parseColor("#C4C4C4"));
		} else {
			/*try {
				nameView.setTextColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR))));
			} catch(Exception e){
				nameView.setTextColor(Color.BLACK);
			}*/
			nameView.setTextColor(Color.BLACK);
		}
		AssetManager am = context.getResources().getAssets();
		String face = null;
		String visits = (cursor.isNull(cursor.getColumnIndex(ContentDescriptor.Member.Cols.HAPPINESS))) ? "||" 
				: cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.HAPPINESS));
		if (visits.length() >= 2){	
			if (visits.substring(visits.length()-2, visits.length()).equals(":)")){
				face = "face-smile.png";
			} else if (visits.substring(visits.length()-2, visits.length()).equals(":|")){
				face = "face-plain.png";
			} else if (visits.substring(visits.length()-2, visits.length()).equals(":(")){
				face = "face-sad.png";
		}	}
		if (face != null) {
			InputStream is = null;
			try {
				is = am.open(face);
			} catch (IOException e) {
				//not critical/doesn't matter.
			}
			Bitmap sm = BitmapFactory.decodeStream(is);
			smileView.setImageBitmap(sm);
			smileView.setVisibility(View.VISIBLE);
		} else {
			smileView.setVisibility(View.GONE);
		}
		String imgDir = context.getExternalFilesDir(null)+"/"+cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Image.Cols.IID))
				+"_"+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.MID))+".jpg";
		File imgFile = new File(imgDir);
		
		if (imgFile.exists() == true){
			imageView.bringToFront();
			imageView.setVisibility(View.VISIBLE);
			imageView.setTag(cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.MID)));
			new BitmapLoader(imgFile,imageView,110,110, cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.MID)));
		}
		else {
			imageView.setVisibility(View.INVISIBLE);
		}
		    
	    String displayText = "";
	    displayText += cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.TIME))+" "
	    		+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DATE));
	    String denyText = "";
		
		if (showDoorname == true) {
			if (cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY)).compareTo("Granted") == 0){
				denyText += "Access Granted at "+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DOORNAME));
			} else {
			   	denyText += "Denied: "+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY))
			   			+" at "+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DOORNAME));
			   	colorBlock.setBackgroundColor(context.getResources().getColor(R.color.visitors_red));
			}
		} else {
			if (cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY)).compareTo("Granted") == 0){
				denyText +="Access Granted";
			} else {
				denyText +="Denied: "+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.DENY));
				colorBlock.setBackgroundColor(context.getResources().getColor(R.color.visitors_red));
		}	}
		timeView.setText(displayText);
		denyView.setText(denyText);
		
		if (cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.MID))<= 0) {
			nameView.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Visitor.Cols.CARDNO)));
		} else {
			nameView.setText(cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))+" "
					+cursor.getString(cursor.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		}
		
	}
}
