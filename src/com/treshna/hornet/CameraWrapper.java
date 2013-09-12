package com.treshna.hornet;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
/*
 * This class handles the camera input/output
 * as well as passing the correct data onto the
 * filehandler, to ensure the image is saved where expected
 */
public class CameraWrapper extends Activity {
	
	private ContentResolver contentResolver = null;
	private static final int PICTURE_RESULT = 1;
	public String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_wrapper);
		// Show the Up button in the action bar.
		setupActionBar();
		Intent intent = getIntent();
		contentResolver = this.getContentResolver();
        id = intent.getStringExtra(VisitorsViewAdapter.EXTRA_ID);
        System.out.println("Add Photo for ID: "+id+" Pushed");
		launchCamera();
	}
	
	public void launchCamera(){
		Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		this.startActivityForResult(camera, PICTURE_RESULT);
	}
	
	@SuppressLint("SimpleDateFormat")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICTURE_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
            	System.out.println("Photo for ID: "+id+" Taken");
            	Bundle b = data.getExtras();
            	setDescription(b);//<---------
               // Display image received on the view
               // Kept as a Bundle to check for other things in my actual code
            } else {
            	this.finish();
            }
		} else {
			this.finish();
		}
	}	
	
	private void setDescription(final Bundle b){
		final EditText input = new EditText(this);
		input.setId(1);
		 AlertDialog.Builder alert = new AlertDialog.Builder(this);
	      alert.setMessage("Description");
	      alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String description = input.getText().toString();
				uploadImage(b, description);
			}});
	      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				uploadImage(b, "");				
			}});
	      alert.setView(input);
	      alert.show();
		
	}
	
	private void uploadImage(Bundle b, String description){
		 Bitmap pic = (Bitmap) b.get("data");
         
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         pic.compress(Bitmap.CompressFormat.PNG, 100, stream);
         byte[] byteArray = stream.toByteArray();
         
         Cursor cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, ContentDescriptor.Image.Cols.MID
         		+" = "+id, null, null);
         int imgCount = 0;
             cur.moveToLast();                
             if (cur.getPosition() == -1) {
             	imgCount = 0;
             } else {
             	imgCount = cur.getInt(0);
             	imgCount +=1;
             }
         cur.close();
         FileHandler fileHandler = new FileHandler(this);
         System.out.print("\n\n**Writing "+imgCount+"_"+id);
         fileHandler.writeFile(byteArray, imgCount+"_"+id);
        
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy hh:mm:ss aa", Locale.US);
 		 java.util.Date date = new Date();
 		 ContentValues val = new ContentValues();
 		 val.put(ContentDescriptor.Image.Cols.ID, imgCount);
         val.put(ContentDescriptor.Image.Cols.MID, id);
         val.put(ContentDescriptor.Image.Cols.DATE, dateFormat.format(date));
         val.put(ContentDescriptor.Image.Cols.IS_PROFILE, (imgCount==0));
         val.put(ContentDescriptor.Image.Cols.DESCRIPTION, description);
         contentResolver.insert(ContentDescriptor.Image.CONTENT_URI, val);
         System.out.println("Updating Image Table");
  
         Intent intent = new Intent(this, HornetDBService.class);
         intent.putExtra(Services.Statics.KEY,Services.Statics.UPLOAD); 
         startService(intent);
         finish();
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
