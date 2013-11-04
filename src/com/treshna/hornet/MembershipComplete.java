package com.treshna.hornet;

import com.treshna.hornet.BookingPage.TagFoundListener;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
	int rowid;
	String memberid;
	private AlertDialog alertDialog = null;
	private String cardid;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 ctx = getActivity();
		 contentResolver = getActivity().getContentResolver();
		 rowid = this.getArguments().getInt(Services.Statics.KEY);
		 page = inflater.inflate(R.layout.membership_complete, container, false);
		 page = setupView();
		 
		 ((EmptyActivity) this.getActivity()).setTitle("Confirm Membership");
		 
		 return page;
	}


	private View setupView() {
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols._ID+" = ?",
				new String[] {String.valueOf(rowid)}, null);
		if (!cur.moveToFirst()) {
			//the row wasn't found.
			Log.e(TAG, "ContentProvider could not find row with _id:"+rowid);
			return page;
		}
		
		EditText programmeName = (EditText) page.findViewById(R.id.membership_programme_name);
		programmeName.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
		
		EditText memberCard = (EditText) page.findViewById(R.id.membership_tag);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.CARDNO)) != null) {
			memberCard.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.CARDNO)));
		} else {
			//no unique card no, or cardno from other membership found.
			memberCard.setText("Warning - No Membership Card");
			memberCard.setTextColor(Color.RED);
		}
		
		EditText membershipStartEnd = (EditText) page.findViewById(R.id.membership_start_end);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)) != null) {
			membershipStartEnd.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART))+" to "
					+cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)));
		} else {
			membershipStartEnd.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART))
					+" Open Ended");
		}
		
		EditText membershipSignup = (EditText) page.findViewById(R.id.membership_signup_fee);
		membershipSignup.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.SIGNUP)));
		
		EditText membershipPrice = (EditText) page.findViewById(R.id.membership_price);
		membershipPrice.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PRICE)));
		
		memberid = cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MID));
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
			
			break;
		}
		case (R.id.button_cancel):{
			contentResolver.delete(ContentDescriptor.Membership.CONTENT_URI, ContentDescriptor.Membership.Cols._ID+" = ?",
					new String[] {String.valueOf(rowid)});
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
				
				ContentValues values = new ContentValues();
				values.put(ContentDescriptor.Membership.Cols.CARDNO, cardid);
				contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, 
						ContentDescriptor.Membership.Cols._ID+" = ?", new String[] {String.valueOf(rowid)});
			}
		}
		cur.close();
		//TOAST!
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
		setupView();
	}
	
}