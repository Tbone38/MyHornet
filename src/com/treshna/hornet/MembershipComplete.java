package com.treshna.hornet;

import java.util.ArrayList;
import java.util.Date;

import com.treshna.hornet.BookingPage.TagFoundListener;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MembershipComplete extends Fragment implements OnClickListener, TagFoundListener {
	private static final String TAG ="MembershipComplete";
	Context ctx;
	ContentResolver contentResolver;
	Cursor cur;
	View page;
	//int rowid;
	ArrayList<String> results;
	String memberid;
	private AlertDialog alertDialog = null;
	private String cardid = null;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 contentResolver = getActivity().getContentResolver();
		 results = this.getArguments().getStringArrayList(Services.Statics.KEY);
		 cardid = results.get(8);
		 page = inflater.inflate(R.layout.membership_complete, container, false);
		 page = setupView();
		 
		 ((EmptyActivity) this.getActivity()).setTitle("Confirm Membership");
		 
		 return page;
	}


	private View setupView() {
		//TODO
		EditText programmeName = (EditText) page.findViewById(R.id.membership_programme_name);
		cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.PID+" = ?",
				new String[] {results.get(2)}, null);
		if (cur.moveToFirst()) {
			programmeName.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)));
		}
		cur.close();
		
		EditText memberCard = (EditText) page.findViewById(R.id.membership_tag);
		if (cardid != null && Integer.parseInt(cardid) > 0) {
			memberCard.setText(cardid);
			memberCard.setTextColor(Color.BLACK);
			
		} else {
			//no unique card no, or cardno from other membership found.
			memberCard.setText("Warning - No Membership Card");
			memberCard.setTextColor(Color.RED);
		}
		
		EditText membershipStartEnd = (EditText) page.findViewById(R.id.membership_start_end);
		if (results.get(4) != null) {
			membershipStartEnd.setText(results.get(3)+" to "
					+results.get(4));
		} else {
			membershipStartEnd.setText(results.get(3)
					+" Open Ended");
		}
		
		EditText membershipSignup = (EditText) page.findViewById(R.id.membership_signup_fee);
		membershipSignup.setText(results.get(7));
		
		EditText membershipPrice = (EditText) page.findViewById(R.id.membership_price);
		membershipPrice.setText(results.get(5));
		
		memberid = results.get(0);
		cur.close();
		
		//EditText memberAmountOutstanding = (EditText) page.findViewById(R.id.membership_amount_outstanding);
		
		/** Actions **/
		LinearLayout addTag = (LinearLayout) page.findViewById(R.id.button_addtag_row);
		addTag.setOnClickListener(this);
		
		LinearLayout addPhoto = (LinearLayout) page.findViewById(R.id.button_addphoto_row);
		addPhoto.setOnClickListener(this);
		
		LinearLayout accept = (LinearLayout) page.findViewById(R.id.button_accept);
		accept.setOnClickListener(this);
		
		LinearLayout cancel = (LinearLayout) page.findViewById(R.id.button_cancel);
		cancel.setOnClickListener(this);
		
		return page;
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_accept):{
			//should do actual insertion here.
			insertMembership(results);
			getActivity().finish();
			break;
		}
		case (R.id.button_cancel):{
			getActivity().finish();
			break;
		}
		case (R.id.button_addphoto_row):{
			Intent camera = new Intent(ctx, CameraWrapper.class);
			camera.putExtra(VisitorsViewAdapter.EXTRA_ID,memberid);
			ctx.startActivity(camera);
			break;
		}
		case (R.id.button_addtag_row):{
			swipeBox();
			break;
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
	public void onNewTag(String serial) {
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cur;
		String message = "";
		
		cur = contentResolver.query(ContentDescriptor.IdCard.CONTENT_URI, null, ContentDescriptor.IdCard.Cols.SERIAL+" = ?",
				new String[] {serial}, null);
		if (!cur.moveToFirst()) {
			//tag not found in db. Tell them to swipe card at reception, then re-sync
			//the phone.		
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
				Log.v(TAG, "Adding "+rowid+" to PendingUploads");
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
				Log.v(TAG, message);
				cardid = null;
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
				setupView();
			}
		}
		cur.close();
		//TOAST!
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
	}
	
	private int insertMembership(ArrayList<String> input) {
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.Membership.Cols.MID, input.get(0));
		values.put(ContentDescriptor.Membership.Cols.PGID, input.get(1));
		values.put(ContentDescriptor.Membership.Cols.PID, input.get(2));
		values.put(ContentDescriptor.Membership.Cols.MSSTART, Services.dateFormat(input.get(3), "dd MMM yyyy", "yyyy-MM-dd"));
		
		if (input.get(4) != null) {
			values.put(ContentDescriptor.Membership.Cols.EXPIRERY, Services.dateFormat(input.get(4), "dd MMM yyyy", "yyyy-MM-dd"));
			
		}
		values.put(ContentDescriptor.Membership.Cols.PRICE, input.get(5));
		//payment-date?
		values.put(ContentDescriptor.Membership.Cols.SIGNUP, input.get(7));
		if (input.get(8) != null) {
			values.put(ContentDescriptor.Membership.Cols.CARDNO, cardid);
		}
		values.put(ContentDescriptor.Membership.Cols.CREATION, Services.dateFormat(new Date().toString(),
				"EEE MMM dd HH:mm:ss zzz yyyy", "dd MMM yyyy HH:mm:ss"));
		cur = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, null, ContentDescriptor.Programme.Cols.PID+" = ?",
				new String[] {input.get(2)}, null);
		if (cur.moveToFirst()) {
			values.put(ContentDescriptor.Membership.Cols.PNAME, 
					cur.getString(cur.getColumnIndex(ContentDescriptor.Programme.Cols.NAME)));
		}
		cur.close();
		
		int msid;
		String rowid = "-1";
		
		cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = "
				+ContentDescriptor.TableIndex.Values.Membership.getKey(), null, null);
		if (!cur.moveToFirst()) {
			msid = -1;
		} else {
			msid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
		}
		cur.close();
	
		values.put(ContentDescriptor.Membership.Cols.MSID, msid);
		values.put(ContentDescriptor.Membership.Cols.DEVICESIGNUP, "t");
		Uri row =contentResolver.insert(ContentDescriptor.Membership.CONTENT_URI, values);
		rowid = row.getLastPathSegment();
		
		contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
		+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(msid),
		String.valueOf(ContentDescriptor.TableIndex.Values.Membership.getKey())});
		
		Log.v(TAG, "insert rowid: "+rowid);
		
		return Integer.parseInt(rowid);
	}
	
}