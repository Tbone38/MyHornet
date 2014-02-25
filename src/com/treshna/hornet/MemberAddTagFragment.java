package com.treshna.hornet;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.BookingPage.TagFoundListener;


public class MemberAddTagFragment extends Fragment implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	int memberID;
	
	private View view;
	LayoutInflater mInflater;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getInt(Services.Statics.MID);
	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_add_tag, container, false);
		
		TextView take_photo = (TextView) view.findViewById(R.id.button_take_phone_text);
		take_photo.setClickable(true);
		take_photo.setOnClickListener(this);
		
		TextView cont_view = (TextView) view.findViewById(R.id.button_continue_text);
		cont_view.setClickable(true);
		cont_view.setOnClickListener(this);
		
		//we need to check for nfc, if no nfc then change the text!.
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			if (NfcAdapter.getDefaultAdapter(getActivity()) == null) {
				TextView hint = (TextView) view.findViewById(R.id.hint_add_tag);
				hint.setText(getActivity().getResources().getString(R.string.member_hint_no_nfc));
			}
		}
		
		return view;
	}
	
	
	private void updateTag(String idcard) {
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Member.Cols.CARDNO, idcard);
		contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols.MID+" = ?", 
				new String[] {String.valueOf(memberID)});
		
		values = new ContentValues();
		values.put(ContentDescriptor.PendingUpdates.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Member.getKey());
		values.put(ContentDescriptor.PendingUpdates.Cols.ROWID, memberID);
		
		contentResolver.insert(ContentDescriptor.PendingUpdates.CONTENT_URI, values);
		
		TextView tag_view = (TextView) view.findViewById(R.id.tag_id);
		tag_view.setTextColor(Color.BLACK);
		tag_view.setText(idcard);
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case (R.id.button_continue_text):{
			Bundle b = new Bundle(2);
			b.putString(Services.Statics.MID, String.valueOf(memberID));
			b.putString(Services.Statics.KEY, null);
			EmptyActivity e = (EmptyActivity) getActivity();
			e.setFragment(Services.Statics.FragmentType.MemberDetails.getKey(), b);
			
			b = new Bundle(1);
			b.putString(Services.Statics.MID, String.valueOf(memberID));
			e.setFragment(Services.Statics.FragmentType.MembershipAdd.getKey(), b);
			break;
		}
		case (R.id.button_take_phone_text):{
			Intent i = new Intent(this.getActivity(), CameraWrapper.class);
			i.putExtra(VisitorsViewAdapter.EXTRA_ID, String.valueOf(memberID));
			getActivity().startActivity(i);
			break;
		}
		}
	}

	@Override
	public void onNewTag(String serial) {
		//we need to look up what the id is, assign that id to the member. and then update our textView.
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor cur;
		String message = null;
		String cardid = null;
		
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
				//show a pop-up.
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
				updateTag(cardid);
				return;
			}
		}
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}
}