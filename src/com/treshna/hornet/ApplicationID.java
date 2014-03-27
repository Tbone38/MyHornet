package com.treshna.hornet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.os.Environment;

public class ApplicationID {
    private static String sID = null;
    private static final String INSTALLATION = "GYMMASTER_MOBILE_INSTALLATION";

    /**
     * returns the UUID for the application install on success, else returns null.
     * @return
     */
    public synchronized static String id() { //Context context
        if (sID == null) {  
        	
        	File sdcard = Environment.getExternalStorageDirectory();
        	if (!sdcard.canWrite()) {
        		return null;
        	}
        	
            File installation = new File(sdcard, "/Android/data/"+INSTALLATION);
            try {
                if (!installation.exists())
                {
                	writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (FileNotFoundException e) {
            	//we've not got an sd-card, or something else went wrong.
                //throw new RuntimeException(e);
            	e.printStackTrace();
            	return null;
            } catch (IllegalArgumentException e) {
            	e.printStackTrace();
            	return null;
            } catch (IOException e) {
            	e.printStackTrace();
            	return null;
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}
