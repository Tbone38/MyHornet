package com.treshna.hornet;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.treshna.hornet.BookingPage.TagFoundListener;
import com.treshna.hornet.ContentDescriptor.Member;


public class MemberActionsFragment extends Fragment implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	LayoutInflater mInflater;
	RadioGroup rg;
	View checkinWindow;
	private String mid;
	private AlertDialog alertDialog = null;
	private String cardid = null;
	
	private static final String TAG = "MemberActions";
	
			
	public void setupActions(View view, String memberID) {
		this.mid = memberID;
		Uri uri = Uri.withAppendedPath(ContentDescriptor.Image.IMAGE_JOIN_MEMBER_URI,
				memberID);
		contentResolver = this.getActivity().getContentResolver();
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
				email.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
				email.setClickable(false);
				TextView text = (TextView) email.findViewById(R.id.button_email_text);
				text.setTextColor(getActivity().getResources().getColor(R.color.grey));
			}
		} else {
			email.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			email.setClickable(false);
			TextView text = (TextView) email.findViewById(R.id.button_email_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
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
			sms.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			sms.setClickable(false);
			TextView text = (TextView) sms.findViewById(R.id.button_sms_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
		}
		
		if (has_number) {
			call.setTag(callTag);
			call.setOnClickListener(this);
		} else {
			call.setVisibility(View.GONE);
			call.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			call.setClickable(false);
			TextView text = (TextView) call.findViewById(R.id.button_call_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
		}
		
		LinearLayout hold = (LinearLayout) view.findViewById(R.id.button_hold);
		hold.setOnClickListener(this);
		
		LinearLayout manualcheckin = (LinearLayout) view.findViewById(R.id.button_manual_checkin);
		manualcheckin.setOnClickListener(this);
		
		LinearLayout gallery = (LinearLayout) view.findViewById(R.id.button_gallery);
		gallery.setOnClickListener(this);
		gallery.setTag(memberID);

		LinearLayout addtag = (LinearLayout) view.findViewById(R.id.button_tag);
		addtag.setOnClickListener(this);
		
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
			Intent i = new Intent(getActivity(), EmptyActivity.class);
			i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MembershipAdd.getKey());
			i.putExtra(Services.Statics.MID, memberid);
			startActivity(i);
			break;
		}
		case (R.id.button_email):{
			String email="mailto:"+Uri.encode(v.getTag().toString())+"?subject="+Uri.encode("Gym Details");
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse(email));
			startActivity(intent);
			break;
		}
		case (R.id.button_sms):{
			String smsNo = (String) v.getTag();
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			smsIntent.putExtra("address",smsNo);
			try {
				startActivity(smsIntent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(getActivity(), "Cannot send SMS from this device.", Toast.LENGTH_LONG).show();
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
				startActivity(intent);
			} 
			else {
				//show popup window, let user select the number to call.
				showPhoneWindow(tag);
			}
			break;
		}
		case (R.id.button_hold):{
			Intent i = new Intent(getActivity(), MembershipHold.class);
			i.putExtra(Services.Statics.KEY, mid);
			startActivity(i);
			break;
		}
		case (R.id.button_manual_checkin):{
			//show a window which: 	lets the user select a membership to checkin,
			//						lets the user select a door to check in at.
			//						(shows the user the member name?)
			showCheckinWindow();
			break;
		}
		case (R.id.button_gallery):{
			Intent i = new Intent(getActivity(), EmptyActivity.class);
			i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MemberGallery.getKey());
			i.putExtra(Services.Statics.MID, mid);
			startActivity(i);
			break;
		}
		case (R.id.button_tag):{
			swipeBox();
		}
		}
	}
	
	private void swipeBox(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
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
	public void onNewTag(String serial) {
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cur;
		String message;
		
		cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols.SERIAL+" = ?",
				new String[] {serial}, null);
		if (!cur.moveToFirst()) {
			//tag not found in db. Tell them to swipe card at reception, then re-sync
			//the phone.
			//TODO:
			message = "Tag not found in db, please swipe tag at reception, then re-sync device";
			Log.v(TAG, message);
		} else {
			cardid = cur.getString(cur.getColumnIndex(ContentDescriptor.IdCard.Cols.CARDID));
			cur.close();
			
			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.CARDNO+" = ?",
					new String[] {cardid}, null);
			if (cur.getCount() > 0) {
				//id is in use, what should I do?
				message = "Tag already in use";
				Log.v(TAG, message);
				cardid = null;
			} else {
				message = "Assigning card No. "+cardid+" to member.";
				Log.v(TAG, message);
				if (alertDialog != null){
					alertDialog.dismiss();
					alertDialog = null;
				}
				updateMember();
			}
		}
		cur.close();
		//TOAST!
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		
	}
	
	private void updateMember(){
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.Member.Cols.CARDNO, cardid);
		contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {mid});
		Log.v(TAG, "Updated Member id:"+mid+" with cardno: "+cardid);
		values = new ContentValues();
		values.put(ContentDescriptor.PendingUpdates.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Member.getKey());
		values.put(ContentDescriptor.PendingUpdates.Cols.ROWID, mid);
		
		contentResolver.insert(ContentDescriptor.PendingUpdates.CONTENT_URI, values);
	}
	
	private void showPhoneWindow(ArrayList<String> phones) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		
		for (int i=0; i< phones.size(); i +=1) {
			RadioButton rb = new RadioButton(getActivity());
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
	            		Toast.makeText(getActivity(), "Select a Phone Number", Toast.LENGTH_LONG).show();
	            		
	            	} else {
		            	RadioButton rb = (RadioButton) rg.findViewById(cid);
		            	selectedNo = (String) rb.getTag();
		    
		            	String ph ="tel:"+selectedNo;
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse(ph));
						startActivity(intent);
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
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
		ArrayAdapter<String> doorAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, doorlist);
		doorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		doorspinner.setAdapter(doorAdapter);
		
		ArrayList<String> membershiplist = new ArrayList<String>();
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ?",
				new String[] {mid}, null);
		while (cur.moveToNext()) {
			//Log.v(TAG, "membership:"+cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			membershiplist.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
		}
		cur.close();
		Spinner membershipSpinner = (Spinner) checkinWindow.findViewById(R.id.manual_checkin_membership);
		ArrayAdapter<String> membershipAdapter = new ArrayAdapter<String>(getActivity(), 
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
						Toast.makeText(getActivity(), "No Membership Available for Member.", Toast.LENGTH_LONG).show();
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
					Intent updateInt = new Intent(getActivity(), HornetDBService.class);
					Bundle extras = new Bundle(3);
					extras.putInt("doorid", doorid);
					extras.putInt("memberid", Integer.parseInt(mid));
					extras.putInt("membershipid", membershipid);
					
					updateInt.putExtras(extras);
					updateInt.putExtra(Services.Statics.KEY, Services.Statics.MANUALSWIPE);
				 	getActivity().startService(updateInt);
				 	PollingHandler p = Services.getFreqPollingHandler();
				 	if (p != null && !p.getConStatus()) {
				 		Toast.makeText(getActivity(), "Could not check member in, Check that this device is connected"
				 				+ " to the internet.", Toast.LENGTH_LONG).show();
				 	}
				}
	        });
	        builder.show();
	}
}