package com.treshna.hornet.services;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapLoader {
	public BitmapLoader(File imgFile, ImageView imageView, int reqWidth, int reqHeight, int memberid) {
		BitmapWorkerTask task = new BitmapWorkerTask(imageView, reqWidth, reqHeight, memberid);
		//BitmapWorkerTask task = new Services.BitmapWorkerTask(imageView, reqWidth, reqHeight);
		task.execute(imgFile);
		}
	
	/**
	 * I've added a BitmapWorkerTask as I was getting class not found errors on compilation.
	 * Is this correct?
	 */
	public class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    
	    private int width = 0;
	    private int height = 0;
	    int memid = 0;

	    public BitmapWorkerTask(ImageView imageView, int w, int h, int memid) {

	        imageViewReference = new WeakReference<ImageView>(imageView);
	        width = w;
	        height = h;
	        this.memid = memid;
	    }

	    @Override
	    protected Bitmap doInBackground(File... params) {
	        return Services.decodeSampledBitmapFromFile(params[0], width, height);
	    }

	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            if (imageView != null && imageView.getTag() != null) {
	            	//covulted checking to make sure we're not trying to load the wrong image into an imageView.
	            	int curmid = 0;
	            	if (imageView.getTag() instanceof Integer) {
	            		curmid = Integer.parseInt(imageView.getTag().toString());
	            	} else
	            	if (imageView.getTag() instanceof ArrayList<?>) {
	            		curmid = Integer.parseInt(((ArrayList<String>)imageView.getTag()).get(1)); 
	            	}
	            	if (curmid == this.memid) {
	            		imageView.setImageBitmap(bitmap);
	            	}
	            }
	        }
	    }
	}
}
