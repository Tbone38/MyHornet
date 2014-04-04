/**
 * 
 */
package com.treshna.hornet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

/**
 * @author callum
 * This class should handle the different queries.
 */
public class FileHandler {

	
	private Context context;
	private static final String TAG = "filehandler";

    //Constructor
	public FileHandler(Context ctx) {
        this.context = ctx;}
	
	public String readFile(int fileSize, String fileDir){
		
  	  	AssetManager am = context.getResources().getAssets();
  	  	InputStream is = null;
		try {
			is = am.open(fileDir);//query file to read goes here
		} catch (IOException e) {
			e.printStackTrace();
		} 
  	  	StringBuffer fileContents = new StringBuffer("");
  	  	byte[] buffer = new byte[fileSize]; //size of the file in bytes
  	  	try {
  	  		while (is.read(buffer) != -1) {
  	  			fileContents.append(new String(buffer));
  	  		}
  	  	} catch (IOException e) {
  	  		e.printStackTrace();
  	  	}
  	  	String query = fileContents.toString();
  	  	//System.out.print(query);
  	  
  	  	return query;
	}
	
	public byte[] readImage(int fileSize, String fileName){
  	  	InputStream is = null;
		try {
			File file = new File(context.getExternalFilesDir(null), fileName+".jpg");
			//System.out.println(file.getAbsolutePath());
			is = new FileInputStream(file);//image file to read goes here
		} catch (IOException e) {
			e.printStackTrace();
		}   	  	
  	  	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  	  	int nRead;
  	  	byte[] data = new byte[fileSize];
  	  	try{
  	  		while ((nRead = is.read(data, 0, data.length)) != -1) {
  	  			buffer.write(data, 0, nRead);
  	  		}
  	  		buffer.flush();
  	  		is.close();
  	  	}catch(Exception e){
  	  		e.printStackTrace();
  	  	}
  	  	
  	  	return buffer.toByteArray();
		//return null;
	}
	
	public void writeFile(byte[] fileInput, String fileName) {
		try{
			File imageFile = new File(context.getExternalFilesDir(null), fileName);
			if (imageFile.exists()) {
	            imageFile.delete();
	      }
			FileOutputStream writer = new FileOutputStream(imageFile);
	        writer.write(fileInput);
	        writer.flush();
	        writer.close();
		}catch (Exception e){
				e.printStackTrace();
		}
	}
	
	public boolean renameFile(String fromName, String toName){
		boolean result = false;
		File sdcard = context.getExternalFilesDir(null);
		File from = new File(sdcard, fromName+".jpg");
		File to = new File(sdcard, toName+".jpg");
		result = from.renameTo(to);
		return result;
	}
	
	public boolean deleteFile(String fileName) {
		File file = new File(context.getExternalFilesDir(null), fileName);
		return file.delete();
	}
	
	public boolean deleteDirectory(File path) {
        boolean result = false;
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    result = files[i].delete();
                }
            }
        }
        return result;
    }
	
	public boolean clearDirectory() {
		File path = new File(context.getExternalFilesDir(null), "");
        boolean result = false;
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    result = files[i].delete();
                }
            }
        }
        return result;
    }
	
	public boolean copyDatabase(boolean restore) {
		try {
            File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();
            File appdata = context.getExternalFilesDir(null);

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.treshna.hornet//databases//"+HornetDatabase.DATABASE_NAME;
                String backupDBPath = "../"+HornetDatabase.DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(appdata, backupDBPath);
                
                FileInputStream fileInputStream = null;
                FileOutputStream fileOutputStream = null;
                if (restore) {
                	fileInputStream = new FileInputStream(backupDB);
                    fileOutputStream = new FileOutputStream(currentDB);
                } else {
                	fileInputStream = new FileInputStream(currentDB);
                	fileOutputStream = new FileOutputStream(backupDB);
                }
    			FileChannel src = fileInputStream.getChannel();
				FileChannel dst = fileOutputStream.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                fileInputStream.close();
                fileOutputStream.close();
                
                if (restore) {
                	//delete the backup, otherwise we're wasting space.
                	backupDB.delete();
                }
            }
		} catch (FileNotFoundException e) {
        	e.printStackTrace();
        	return false;
        } catch (IOException e){
        	e.printStackTrace();
        	return false;
        }
		return true;	
	}
	
	public void clearLog() {
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLog() {
		Process process = null;
		try {
		      process = Runtime.getRuntime().exec("logcat -d -s HORNETSERVICE:W *:S");
		      BufferedReader bufferedReader = new BufferedReader(
		      new InputStreamReader(process.getInputStream()));

		      StringBuilder log=new StringBuilder();
		      String line;
		      while ((line = bufferedReader.readLine()) != null) {
		        log.append(line);
		      }
		      this.writeFile(log.toString().getBytes(), "db_sync.log");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			process.destroy();
		}
	}
	
	//what the hell is this returning? 
	public String getLog() {
		File logfile = new File(context.getExternalFilesDir(null), "db_sync.log");
		long fileSize = 0;
		InputStream is = null;
		StringBuffer fileContents = new StringBuffer("");
		
		fileSize = logfile.length();
		//Log.d(TAG, "LOG FILE SIZE:"+fileSize);
		if (fileSize <= 0|| fileSize==36) {
			return null;
		}
		
		try {
			is = new FileInputStream(logfile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
  	  	
  	  	byte[] buffer = new byte[(int)fileSize]; //size of the file in bytes
  	  	try {
  	  		while (is.read(buffer) != -1) {
  	  			fileContents.append(new String(buffer));
  	  		}
  	  		is.close();
  	  	} catch (IOException e) {
  	  		e.printStackTrace();
  	  		return null;
  	  	}
  	  	
  	  	return fileContents.toString();
	}
	
}
