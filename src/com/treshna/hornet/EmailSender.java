package com.treshna.hornet;

import android.content.Context;
import android.content.Intent;

public class EmailSender {
	private Intent mailIntent = null;
	
	
public EmailSender(Context context, String[] recipientAddresses, String subject, String message) {
		mailIntent = new Intent(Intent.ACTION_SEND);
		mailIntent.putExtra(Intent.EXTRA_BCC,  recipientAddresses);
		mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		mailIntent.putExtra(Intent.EXTRA_TEXT, message);
		mailIntent.setType("message/rfc822");
		mailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(mailIntent, "Choose an email client: "));
	}


public Intent getMailIntent() {
	return mailIntent;
}



}
