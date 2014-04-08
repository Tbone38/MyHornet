package com.treshna.hornet.services;

import java.io.File;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapLoader {
	public BitmapLoader(File imgFile, ImageView imageView, int reqWidth, int reqHeight) {
		BitmapWorkerTask task = new BitmapWorkerTask(imageView, reqWidth, reqHeight);
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

	    public BitmapWorkerTask(ImageView imageView, int w, int h) {

	        imageViewReference = new WeakReference<ImageView>(imageView);
	        width = w;
	        height = h;
	    }

	    @Override
	    protected Bitmap doInBackground(File... params) {
	        return Services.decodeSampledBitmapFromFile(params[0], width, height);
	    }

	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            if (imageView != null) {
	                imageView.setImageBitmap(bitmap);
	            }
	        }
	    }
	}
}
