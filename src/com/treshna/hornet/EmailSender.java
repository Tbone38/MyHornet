package com.treshna.hornet;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class EmailSender {
	private Intent mailIntent = null;
	private Context context = null;
	
	
public EmailSender(Context context, String[] toRecipients, String[] bccRecipients,  String subject) {
		this.context = context;
		mailIntent = new Intent(Intent.ACTION_SEND);
		if (bccRecipients != null){
			mailIntent.putExtra(Intent.EXTRA_BCC,  bccRecipients);
		}
		if (subject != null) {
			mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		mailIntent.putExtra(Intent.EXTRA_EMAIL, toRecipients);
		mailIntent.setType("message/rfc822");
		mailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	
	}



	public void  attachFile (String fileName) {
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root, fileName);
		if (!file.exists() || !file.canRead()) {
		    Toast.makeText(context, "Attachment Error", Toast.LENGTH_SHORT).show();
		    return;
		}
		Uri uri = Uri.parse("file://" + file);
		mailIntent.putExtra(Intent.EXTRA_STREAM, uri);
	}
	
	public void sendToClientEmail () {
		context.startActivity(Intent.createChooser(mailIntent, "Choose an email client: "));
	}
	
	public Intent getMailIntent() {
		return mailIntent;
	}

}
