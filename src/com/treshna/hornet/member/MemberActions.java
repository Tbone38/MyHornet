package com.treshna.hornet.member;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R.color;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.membership.MembershipAdd;
import com.treshna.hornet.membership.MembershipHoldFragment;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.network.PollingHandler;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.services.Services.Statics;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.Door;
import com.treshna.hornet.sqlite.ContentDescriptor.FreeIds;
import com.treshna.hornet.sqlite.ContentDescriptor.IdCard;
import com.treshna.hornet.sqlite.ContentDescriptor.Image;
import com.treshna.hornet.sqlite.ContentDescriptor.Member;
import com.treshna.hornet.sqlite.ContentDescriptor.Membership;
import com.treshna.hornet.sqlite.ContentDescriptor.PendingUpdates;
import com.treshna.hornet.sqlite.ContentDescriptor.PendingUploads;
import com.treshna.hornet.sqlite.ContentDescriptor.TableIndex;
import com.treshna.hornet.sqlite.ContentDescriptor.PendingUpdates.Cols;
import com.treshna.hornet.sqlite.ContentDescriptor.TableIndex.Values;


public class MemberActions implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	LayoutInflater mInflater;
	RadioGroup rg;
	View checkinWindow;
	private String mid;
	private AlertDialog alertDialog = null;
	private String cardid = null;
	private Context ctx;
	private FragmentActivity caller;
	
	public MemberActions(FragmentActivity a) {
		this.caller = a;
		this.ctx = a;
	}

	//This needs significant changes!.
	
	public void setupActions(View view, String memberID) {
		this.mid = memberID;
		Uri uri = Uri.withAppendedPath(ContentDescriptor.Image.IMAGE_JOIN_MEMBER_URI,
				memberID);
		contentResolver = ctx.getContentResolver();
		
		cur = contentResolver.query(uri, null, null, null, null);
		if (!cur.moveToFirst()){
			return;
		}
		
		LinearLayout addMembership = (LinearLayout) view.findViewById(R.id.button_add_membership);
		addMembership.setTag(memberID);
		addMembership.setOnClickListener(this);
		
		LinearLayout email = (LinearLayout) view.findViewById(R.id.button_email);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)) != null) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)).compareTo("null") != 0) {
				email.setOnClickListener(this);
				email.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)));
			} else {
				email.setBackgroundColor(ctx.getResources().getColor(R.color.white));
				email.setClickable(false);
				TextView text = (TextView) email.findViewById(R.id.button_email_text);
				text.setTextColor(ctx.getResources().getColor(R.color.grey));
			}
		} else {
			email.setBackgroundColor(ctx.getResources().getColor(R.color.white));
			email.setClickable(false);
			TextView text = (TextView) email.findViewById(R.id.button_email_text);
			text.setTextColor(ctx.getResources().getColor(R.color.grey));
		}
		
		LinearLayout call = (LinearLayout) view.findViewById(R.id.button_call);
		LinearLayout sms = (LinearLayout) view.findViewById(R.id.button_sms);
		ArrayList<String> callTag = new ArrayList<String>();
		boolean has_number = false;
		
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)) != null &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)).compareTo("null") != 0) {
			
			callTag.add("Home: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)));
			has_number = true;
		}
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)) != null &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)).compareTo("null") != 0) {
			
			callTag.add("Work: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)));
			has_number = true;
		}
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)) != null &&
				cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)).compareTo("null") !=0) {
			
			sms.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
			sms.setOnClickListener(this);
			
			callTag.add("Cell: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
			has_number = true;
		} else {
			sms.setBackgroundColor(ctx.getResources().getColor(R.color.white));
			sms.setClickable(false);
			TextView text = (TextView) sms.findViewById(R.id.button_sms_text);
			text.setTextColor(ctx.getResources().getColor(R.color.grey));
		}
		
		if (has_number) {
			call.setTag(callTag);
			call.setOnClickListener(this);
		} else {
			call.setBackgroundColor(ctx.getResources().getColor(R.color.white));
			call.setClickable(false);
			TextView text = (TextView) call.findViewById(R.id.button_call_text);
			text.setTextColor(ctx.getResources().getColor(R.color.grey));
		}
		
		LinearLayout hold = (LinearLayout) view.findViewById(R.id.button_hold);
		hold.setOnClickListener(this);
		
		LinearLayout manualcheckin = (LinearLayout) view.findViewById(R.id.button_manual_checkin);
		manualcheckin.setOnClickListener(this);
		
				
		LinearLayout addtag = (LinearLayout) view.findViewById(R.id.button_tag);
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1 &&
				NfcAdapter.getDefaultAdapter(ctx) != null) {
			addtag.setOnClickListener(this);
		} else {
			addtag.setBackgroundColor(ctx.getResources().getColor(R.color.white));
			addtag.setClickable(false);
			TextView text = (TextView) addtag.findViewById(R.id.button_tag_text);
			text.setTextColor(ctx.getResources().getColor(R.color.grey));
		}
		
		cur.close();
		return ;
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case (R.id.button_add_membership):{
			String memberid = null;
			if (v.getTag() instanceof String) {
				memberid = (String) v.getTag();
			}
			
			Fragment f = new MembershipAdd();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberid);
			f.setArguments(bdl);
			((MainActivity)caller).changeFragment(f, "MembershipAdd");
			break;
		}
		case (R.id.button_email):{
			String email="mailto:"+Uri.encode(v.getTag().toString())+"?subject="+Uri.encode("Gym Details");
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse(email));
			try {
				ctx.startActivity(intent);
			} catch (ActivityNotFoundException e) { 
				Toast.makeText(ctx, "Cannot send emails from this device.", Toast.LENGTH_LONG).show();
			}
			break;
		}
		case (R.id.button_sms):{
			String smsNo = (String) v.getTag();
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			smsIntent.putExtra("address",smsNo);
			try {
				ctx.startActivity(smsIntent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(ctx, "Cannot send SMS from this device.", Toast.LENGTH_LONG).show();
			}
			break;
		}
		case (R.id.button_call):{
			ArrayList<String> tag = null;
			if (v.getTag() instanceof ArrayList<?>) {
				tag = (ArrayList<String>) v.getTag();
			}
			if (tag.size() == 1) {
				String ph ="tel:"+tag.get(0).substring(tag.get(0).indexOf(":")+1);
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(ph));
				try {
					ctx.startActivity(intent);
				} catch(ActivityNotFoundException e) {
					Toast.makeText(ctx, "Cannot make call's from this device.", Toast.LENGTH_LONG).show();
				}
			} 
			else {
				//show popup window, let user select the number to call.
				showPhoneWindow(tag);
			}
			break;
		}
		case (R.id.button_hold):{
			Fragment f = new MembershipHoldFragment();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, mid);
			f.setArguments(bdl);
			((MainActivity)caller).changeFragment(f, "MembershipHold");
			break;
		}
		case (R.id.button_manual_checkin):{
			//show a window which: 	lets the user select a membership to checkin,
			//						lets the user select a door to check in at.
			//						(shows the user the member name?)
			showCheckinWindow();
			break;
		}
		case (R.id.button_tag):{
			Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
					new String[] {mid}, null);
			if (!cur.moveToFirst()) break;
			
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CARDNO))) {
				//confirm we want to overwrite.
				AlertDialog.Builder builder= new AlertDialog.Builder(ctx);
				builder.setTitle("Overwrite current Tag?")
				.setMessage("Are you sure you want to overwrite the currently assigned Tag?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						swipeBox();
					}})
				.setNegativeButton("Cancel", null)
				.show();
				cur.close();
			} else {
				cur.close();
				swipeBox();
			}
		}
		}
	}
	
	private void swipeBox(){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Swipe Tag")
		.setMessage("Please Swipe a tag against the device");
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	/** look up the serial in the idcard table,
	 * check that the cardno is not in use by anyone else.
	 * assign the cardno to the membership.
	 */
	@Override
	public boolean onNewTag(String serial) {
		if (alertDialog == null || !alertDialog.isShowing()) {
			/*((NFCActivity)caller).onNewTag(serial);
			return true;*/
			return false;
		}
		ContentResolver contentResolver = ctx.getContentResolver();
		Cursor cur;
		String message = null;

		cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols.SERIAL+" = ?",
				new String[] {serial}, null);
		if (!cur.moveToFirst()) { //card not in db, add it.
			cur.close();
			cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
					new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())},null);
			if (cur.moveToFirst()) {
				cardid = cur.getString(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
				cur.close();
				
				ContentValues values = new ContentValues();
				values.put(ContentDescriptor.IdCard.Cols.CARDID, cardid);
				values.put(ContentDescriptor.IdCard.Cols.SERIAL, serial);
				Uri row = contentResolver.insert(ContentDescriptor.IdCard.CONTENT_URI, values);
				String rowid = row.getLastPathSegment(); 
				contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "+
						ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",new String[] {cardid, 
						String.valueOf(ContentDescriptor.TableIndex.Values.Idcard.getKey())});
				
				//normally we'd use a trigger. but we haven't got a timestamp column, and I'm feeling lazy..
				values = new ContentValues();
				values.put(ContentDescriptor.PendingUploads.Cols.ROWID, rowid);
				values.put(ContentDescriptor.PendingUploads.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Idcard.getKey());
				contentResolver.insert(ContentDescriptor.PendingUploads.CONTENT_URI, values);
				
			} else {
				cur.close();
				message = "No Tag ID's available. Please resync the device.";
			}
			
		} else { //card in db, get the id.
			cardid = cur.getString(cur.getColumnIndex(ContentDescriptor.IdCard.Cols.CARDID));
			cur.close();
		}
		
		if (cardid != null) {
			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.CARDNO+" = ?",
					new String[] {cardid}, null);
			if (cur.getCount() > 0) {
				//id is in use, what should I do?
				if (message == null) message = "Tag already in use";
				cardid = null;
			} else {
				message = "Assigning card No. "+cardid+" to member.";
				if (alertDialog != null){
					alertDialog.dismiss();
					alertDialog = null;
				}
				updateMember();
			}
			cur.close();
		}
		//TOAST!
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
		return true;
	}
	
	private void updateMember(){
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.Member.Cols.CARDNO, cardid);
		contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {mid});
		values = new ContentValues();
		values.put(ContentDescriptor.PendingUpdates.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Member.getKey());
		values.put(ContentDescriptor.PendingUpdates.Cols.ROWID, mid);
		
		contentResolver.insert(ContentDescriptor.PendingUpdates.CONTENT_URI, values);
		
		Intent i = new Intent(ctx, HornetDBService.class);
		i.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
		ctx.startService(i);
	}
	
	private void showPhoneWindow(ArrayList<String> phones) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		LayoutInflater inflater = caller.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		
		for (int i=0; i< phones.size(); i +=1) {
			RadioButton rb = new RadioButton(ctx);
			rb.setText(phones.get(i));
			rb.setTag(phones.get(i).substring(phones.get(i).indexOf(":")+1));
			rg.addView(rb);
		}	
        builder.setView(layout);
        builder.setTitle("Select Number to Call");
        builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            		
            		String selectedNo = null;
	            	int cid = rg.getCheckedRadioButtonId();  
	            	if (cid == -1) {
	            		Toast.makeText(ctx, "Select a Phone Number", Toast.LENGTH_LONG).show();
	            		
	            	} else {
		            	RadioButton rb = (RadioButton) rg.findViewById(cid);
		            	selectedNo = (String) rb.getTag();
		    
		            	String ph ="tel:"+selectedNo;
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse(ph));
						ctx.startActivity(intent);
	            	}
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}
	
	
	private void showCheckinWindow(){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		LayoutInflater inflater = caller.getLayoutInflater();
		checkinWindow = inflater.inflate(R.layout.alert_manual_checkin, null);
		String name = null;
		cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, new String[] {Member.Cols.FNAME, Member.Cols.SNAME},
				"m."+ContentDescriptor.Member.Cols.MID+" = ?", new String[] {String.valueOf(mid)}, null);
		if (!cur.moveToFirst()) {
			return;
		}
		name = cur.getString(0)+" "+cur.getString(1);
		cur.close();
		
		EditText nameView = (EditText) checkinWindow.findViewById(R.id.manual_checkin_name);
		nameView.setText(name);

		ArrayList<String> doorlist = new ArrayList<String>();
		cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, null, null, null);
		while (cur.moveToNext()) {
			doorlist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORNAME)));
		}
		cur.close();
		Spinner doorspinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_door);
		ArrayAdapter<String> doorAdapter = new ArrayAdapter<String>(ctx,
				android.R.layout.simple_spinner_item, doorlist);
		doorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		doorspinner.setAdapter(doorAdapter);
		
		ArrayList<String> membershiplist = new ArrayList<String>();
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ? AND "
				+ContentDescriptor.Membership.Cols.HISTORY+" = 'f'", new String[] {mid}, null);
		while (cur.moveToNext()) {
			membershiplist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
		}
		cur.close();
		Spinner membershipSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_membership);
		ArrayAdapter<String> membershipAdapter = new ArrayAdapter<String>(ctx, 
				android.R.layout.simple_spinner_item, membershiplist);
		//membershipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		membershipSpinner.setAdapter(membershipAdapter);
		
		 	builder.setView(checkinWindow);
	        builder.setTitle("Manual Check-In");
	        builder.setPositiveButton("Check In", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//get spinner details.
					Spinner membershipSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_membership);
					cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, new String[] 
							{ContentDescriptor.Membership.Cols.MSID}, ContentDescriptor.Membership.Cols.MID+" = ?",
							new String[] {mid}, null);
					int membershipid = -1;
					try {
						cur.moveToPosition(membershipSpinner.getSelectedItemPosition());
						membershipid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID));
					} catch (CursorIndexOutOfBoundsException e) {
						Toast.makeText(ctx, "No Membership Available for Member.", Toast.LENGTH_LONG).show();
						dialog.cancel();
						return;
					}
					cur.close();
					
					Spinner doorSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_door);
					cur = contentResolver.query(ContentDescriptor.Door.CONTENT_URI, null, null, null, null);
					cur.moveToPosition(doorSpinner.getSelectedItemPosition());
					
					int doorid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Door.Cols.DOORID));
					cur.close();
					
					//parse the details to the Service.
					/*Intent updateInt = new Intent(ctx, HornetDBService.class);
					Bundle extras = new Bundle(3);
					extras.putInt("doorid", doorid);
					extras.putInt("memberid", Integer.parseInt(mid));
					extras.putInt("membershipid", membershipid);
					
					updateInt.putExtras(extras);
					updateInt.putExtra(Services.Statics.KEY, Services.Statics.MANUALSWIPE);
				 	ctx.startService(updateInt);*/
					ManualCheckin async = new ManualCheckin();
					async.execute(doorid, Integer.parseInt(mid), membershipid);
					
				 	PollingHandler p = Services.getFreqPollingHandler();
				 	if (p != null && !p.getConStatus()) {
				 		Toast.makeText(ctx, "Could not check member in, Check that this device is connected"
				 				+ " to the internet.", Toast.LENGTH_LONG).show();
				 	}
				}
	        });
	        builder.show();
	}
	
	private class ManualCheckin extends AsyncTask<Integer, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;

		
		public ManualCheckin() {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(ctx, "Checking In..", 
					 "Manually checking Member In...");
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			int doorid, memberid, membershipid;
			doorid = params[0];
			memberid = params[1];
			membershipid = params[2];
			return sync.manualCheckin(doorid, memberid, membershipid, ctx);
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
}