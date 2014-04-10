package com.treshna.hornet.services;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.SettingsActivity;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.network.PollingHandler;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Image;
import com.treshna.hornet.sqlite.ContentDescriptor.Image.Cols;
import com.treshna.hornet.visitor.VisitorsViewAdapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
/*
 * This class handles the camera input/output
 * as well as passing the correct data onto the
 * filehandler, to ensure the image is saved where expected
 */
public class CameraWrapper extends Activity {
	
	private ContentResolver contentResolver = null;
	private static final int PICTURE_RESULT = 1;
	public String id;
	private int iid = 0;

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
		
		Cursor cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
       		 new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey())}, null);
        if (!cur.moveToFirst()) {
       	 Toast.makeText(this, "Image could not be created, Please try syncing the app.", Toast.LENGTH_LONG).show();
       	 finish();
       	 return;
        } else {
       	 iid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
        }
		
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
         
         
         
         ContentValues val = new ContentValues();
 		 java.util.Date date = new Date();
 		 
 		 val.put(ContentDescriptor.Image.Cols.IID, iid);
         val.put(ContentDescriptor.Image.Cols.MID, id);
         val.put(ContentDescriptor.Image.Cols.DATE, date.getTime());
         val.put(ContentDescriptor.Image.Cols.IS_PROFILE, 0);
         val.put(ContentDescriptor.Image.Cols.DESCRIPTION, description);
         val.put(ContentDescriptor.Image.Cols.DEVICESIGNUP, "t"); 	// This never gets unset, it's OK though,
         															// because we don't check it when we're updating anyway.
         contentResolver.insert(ContentDescriptor.Image.CONTENT_URI, val);
         contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.TABLEID+" = ? AND "
        		 +ContentDescriptor.FreeIds.Cols.ROWID+" = ?", new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Image.getKey()),
        		 String.valueOf(iid)});
         
         FileHandler fileHandler = new FileHandler(this);
         fileHandler.writeFile(byteArray, iid+"_"+id+".jpg");
         
         contentResolver.notifyChange(ContentDescriptor.Image.CONTENT_URI, null);
         Intent intent = new Intent(this, HornetDBService.class);

         intent.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
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
}
