package com.treshna.hornet.member;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R;
import com.treshna.hornet.membership.MembershipAdd;
import com.treshna.hornet.membership.MembershipHoldFragment;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.DatePickerFragment;
import com.treshna.hornet.services.DatePickerFragment.DatePickerSelectListener;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;



public class MemberMembershipFragment extends Fragment implements TagFoundListener, OnClickListener, DatePickerSelectListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private int membershipid;
	private View alert_cancel_ms;
	private AlertDialog dialog;
	private AlertDialog alertDialog = null;
	
	private String cardid;
	
	private final String TAG = MemberMembershipFragment.class.getName();
	private DatePickerFragment datePicker;
	
	//private static final String TAG = "MemberDetails";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		//mActions = new MemberActions(getActivity());
		datePicker = new DatePickerFragment();
		datePicker.setDatePickerSelectListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.fragment_member_details_membership, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupView();
		
		setupActions();
		return view;
	}
	
	private void setupActions() {
		LinearLayout addMembership = (LinearLayout) view.findViewById(R.id.button_add_membership);
		addMembership.setTag(memberID);
		addMembership.setOnClickListener(this);

		LinearLayout hold = (LinearLayout) view.findViewById(R.id.button_hold);
		hold.setOnClickListener(this);
		
		LinearLayout addtag = (LinearLayout) view.findViewById(R.id.button_tag);
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1 &&
				NfcAdapter.getDefaultAdapter(getActivity()) != null) {
			addtag.setOnClickListener(this);
		} else {
			addtag.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			addtag.setClickable(false);
			TextView text = (TextView) addtag.findViewById(R.id.button_tag_text);
			text.setTextColor(getActivity().getResources().getColor(R.color.grey));
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setupView();
	}
	
	/*public MemberActions getMemberActions(){
		return this.mActions;
	}*/
	
	
	
	//This needs to filter by active memberships.
	private View setupView() {
		if (memberID == null) {
			return view;
		}
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, 
				ContentDescriptor.Membership.Cols.MID+" = ? AND "+ContentDescriptor.Membership.Cols.HISTORY+" = 'f'", new String[] {memberID}, null);
		
		LinearLayout mslist = (LinearLayout) view.findViewById(R.id.member_ms_list);
		mslist.removeAllViews();
		
		while (cur.moveToNext()) {
			boolean is_cancelled = false;
			is_cancelled = (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Membership.Cols.CANCEL_REASON))
					&& cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.HISTORY)).compareTo("f")==0) ? true : false;
			
			RelativeLayout membershipRow = (RelativeLayout) mInflater.inflate(R.layout.row_member_membership, null);
			membershipRow.setClickable(true);
			membershipRow.setTag(cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
			membershipRow.setOnClickListener(this);
			
			if (is_cancelled) {
				RelativeLayout content = (RelativeLayout) membershipRow.findViewById(R.id.member_membership_content);
				content.setBackgroundColor(getActivity().getResources().getColor(R.color.membership_cancelled));
			}
			
			ImageView icon = (ImageView) membershipRow.findViewById(R.id.member_membership_drawable);
			icon.setColorFilter(Services.ColorFilterGenerator.setColour(getResources().getColor(R.color.grey_cf)));
			
			ImageView cancel_membership = (ImageView) membershipRow.findViewById(R.id.member_membership_cancel);
			cancel_membership.setTag(cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
			cancel_membership.setClickable(true);
			cancel_membership.setOnClickListener(this);
			
			TextView name = (TextView) membershipRow.findViewById(R.id.member_membership_title);
			name.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			
			TextView start = (TextView) membershipRow.findViewById(R.id.member_ms_started);
			start.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART))
					, "yyyy-MM-dd", "dd MMM yyyy"));
			
			TextView end = (TextView) membershipRow.findViewById(R.id.member_ms_expired);
			String enddate = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY))
					, "yyyy-MM-dd", "dd MMM yyyy");
			if (enddate == null) {
				enddate = cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY));
			}
			end.setText(enddate);
			
			TextView visits = (TextView) membershipRow.findViewById(R.id.member_ms_visits);
			visits.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.VISITS)));
			
			mslist.addView(membershipRow);
		}
		cur.close();
		
		LinearLayout holdlist = (LinearLayout) view.findViewById(R.id.member_hold_list);
		holdlist.removeAllViews();
		
		cur = contentResolver.query(ContentDescriptor.MembershipSuspend.CONTENT_URI, null, ContentDescriptor.MembershipSuspend.Cols.MID+" = ?",
				new String[] {memberID}, ContentDescriptor.MembershipSuspend.Cols.ORDER+" DESC"); //FIX THIS
		
		if (cur.getCount() <= 0) {
			TextView heading = (TextView) view.findViewById(R.id.memberHoldH);
			heading.setVisibility(View.GONE);
		}
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.row_member_finance, null);
			
			if (cur.getPosition()%2==0) {
				row.setBackgroundColor(Color.WHITE);
			}
			
			TextView sdate_view = (TextView) row.findViewById(R.id.finance_row_occurred);
			String sdate = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.STARTDATE));
			Date thesdate = Services.StringToDate(sdate, "dd MMM yyyy");
			if (thesdate != null) sdate = Services.DateToString(thesdate);
			sdate_view.setText(sdate);
			
			TextView note_view = (TextView) row.findViewById(R.id.finance_row_note);
			note_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.REASON)));
			
			TextView edate_view = (TextView) row.findViewById(R.id.finance_row_amount1);
			TextView amount_view = (TextView) row.findViewById(R.id.finance_row_amount2);
			//amount_view.setVisibility(View.GONE);
			String edate = cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.ENDDATE));
			Date theedate = Services.StringToDate(edate, "dd MMM yyyy");			
			if (theedate != null) edate =Services.DateToString(theedate);
			edate_view.setText(edate);
			
			amount_view.setText(calculateHoldDuration(thesdate, theedate));
			
			View colour_block = (View) row.findViewById(R.id.finance_colour_block);
			colour_block.setBackgroundColor(getResources().getColor(R.color.android_blue));
			
			if (Services.StringToDate(sdate, "dd MMM yyyy").getTime() <= new Date().getTime()
					&& Services.StringToDate(edate, "dd MMM yyyy").getTime() >= new Date().getTime()) {
				//this is our active hold..
				//we want to edit/cancel it.
				row.setId(R.id.holdfee);
				row.setClickable(true);
				row.setOnClickListener(this);
				int rowid = cur.getInt(cur.getColumnIndex(ContentDescriptor.MembershipSuspend.Cols.SID));
				row.setTag(rowid);
			}
			
			holdlist.addView(row);
		}
		
		return view;
	}
	
	private String calculateHoldDuration(Date start, Date end) {
		if (start == null || end == null) {
			return "";
		}
		long interval = end.getTime() - start.getTime();
		String duration = null;
		
		double days = Double.valueOf(new DecimalFormat("#").format(
				(interval/86400000)));
		
		if ((int) days < 7) { //less than a week?
			duration = (int) days+" days ";
		} else {
			double weeks = Double.valueOf(new DecimalFormat("#").format(
					(interval/604800000)));
			days = 0d;
			days = Double.valueOf(new DecimalFormat("#").format(
					(interval-((double)((int)weeks)*604800000))/86400000));
			if ((int) days == 0) {
				duration = (int) weeks + " weeks ";
			} else {
				duration = (int) weeks+" weeks, "+(int) days + " days ";
			}
		}
		
		return duration;
	}
	

	@Override
	public boolean onNewTag(String serial) {
		Log.w(TAG, "RECIEVED NEW TAG");
		if (alertDialog == null || !alertDialog.isShowing()) {
			return false;
		}
		ContentResolver contentResolver = getActivity().getContentResolver();
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
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		return true;
	}
	
	private void updateMember(){
		ContentValues values = new ContentValues();
		
		values.put(ContentDescriptor.Member.Cols.CARDNO, cardid);
		values.put(ContentDescriptor.Member.Cols.DEVICESIGNUP, "t");
		contentResolver.update(ContentDescriptor.Member.CONTENT_URI, values, ContentDescriptor.Member.Cols.MID+" = ?",
				new String[] {memberID});
		/*values = new ContentValues();
		values.put(ContentDescriptor.PendingUpdates.Cols.TABLEID, ContentDescriptor.TableIndex.Values.Member.getKey());
		values.put(ContentDescriptor.PendingUpdates.Cols.ROWID, memberID);
		
		contentResolver.insert(ContentDescriptor.PendingUpdates.CONTENT_URI, values);*/ // we have a trigger, this is unneccissary?
		
		Intent i = new Intent(getActivity(), HornetDBService.class);
		i.putExtra(Services.Statics.KEY, Services.Statics.FREQUENT_SYNC);
		getActivity().startService(i);
	}
	
	private void cancelMembership(ArrayList<String> inputs, int membershipid) {
		
		ContentValues values = new ContentValues();
		values.put(ContentDescriptor.Membership.Cols.CANCEL_REASON, inputs.get(1));
		values.put(ContentDescriptor.Membership.Cols.TERMINATION_DATE, inputs.get(0));
		values.put(ContentDescriptor.Membership.Cols.DEVICESIGNUP, "t");
		
		contentResolver.update(ContentDescriptor.Membership.CONTENT_URI, values, ContentDescriptor.Membership.Cols.MSID+" = ?", 
				new String[] {String.valueOf(membershipid)});
		
		if (inputs.get(2) != null) {
			values = new ContentValues();
			values.put(ContentDescriptor.CancellationFee.Cols.FEE, inputs.get(2));
			values.put(ContentDescriptor.CancellationFee.Cols.MEMBERSHIPID, membershipid);
			
			cur = contentResolver.query(ContentDescriptor.CancellationFee.CONTENT_URI, null, ContentDescriptor.CancellationFee.Cols.MEMBERSHIPID+" = ?",
					new String[] {String.valueOf(membershipid)}, null);
			if (cur.moveToFirst()) {
				cur.close();
				contentResolver.update(ContentDescriptor.CancellationFee.CONTENT_URI, values, //check triggers.
						ContentDescriptor.CancellationFee.Cols.MEMBERSHIPID+" = ?", new String[] {String.valueOf(membershipid)});
			} else {
				cur.close();
				contentResolver.insert(ContentDescriptor.CancellationFee.CONTENT_URI, values);
			}
		}
		
		cur = contentResolver.query(ContentDescriptor.FreeIds.CONTENT_URI, null, ContentDescriptor.FreeIds.Cols.TABLEID+" = ?",
				new String[] {String.valueOf(ContentDescriptor.TableIndex.Values.MemberNotes.getKey())}, null);
		int mnid = -1;
		if (cur.moveToFirst()) {
			mnid = cur.getInt(cur.getColumnIndex(ContentDescriptor.FreeIds.Cols.ROWID));
		}
		cur.close();
		
		String note ="Membership Expired: "+inputs.get(1); 
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		
		values = new ContentValues();
		values.put(ContentDescriptor.MemberNotes.Cols.DEVICESIGNUP, "t");
		values.put(ContentDescriptor.MemberNotes.Cols.MID, memberID);
		values.put(ContentDescriptor.MemberNotes.Cols.OCCURRED, format.format(new Date()));
		values.put(ContentDescriptor.MemberNotes.Cols.NOTES, note);
		values.put(ContentDescriptor.MemberNotes.Cols.MNID, mnid);
		
		contentResolver.insert(ContentDescriptor.MemberNotes.CONTENT_URI, values);
		 
		if (mnid > 0) {
			contentResolver.delete(ContentDescriptor.FreeIds.CONTENT_URI, ContentDescriptor.FreeIds.Cols.ROWID+" = ? AND "
					+ContentDescriptor.FreeIds.Cols.TABLEID+" = ?", new String[] {String.valueOf(mnid),
					String.valueOf(ContentDescriptor.TableIndex.Values.MemberNotes.getKey())});
		}
		
		//TODO: we need to provide feedback to let the user know it was successful. 
		//i.e. hide/grey-out expired memberships.
		setupView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.member_membership_cancel):{
			membershipid = Integer.parseInt(v.getTag().toString());
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, new String[] {ContentDescriptor.Membership.Cols.PNAME},
					ContentDescriptor.Membership.Cols.MSID+" = ?",new String[] {String.valueOf(membershipid)}, null);
			if (!cur.moveToFirst()) {
				setupView();
			}
			
			builder.setTitle(this.getString(R.string.membership_expire_text, cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME))));
			cur.close();

			alert_cancel_ms = mInflater.inflate(R.layout.alert_cancel_membership, null);

			TextView date_text = (TextView) alert_cancel_ms.findViewById(R.id.text_cancel_date);
			date_text.setClickable(true);
			date_text.setOnClickListener(this);
			
			
			Spinner cancel_reason = (Spinner) alert_cancel_ms.findViewById(R.id.spinner_cancel_reason);
			List<String> cancel_list = new ArrayList<String>();
			
			cur = contentResolver.query(ContentDescriptor.MembershipExpiryReason.CONTENT_URI, null, null, null, 
					ContentDescriptor.MembershipExpiryReason.Cols.ID);
			while (cur.moveToNext()) {
					cancel_list.add(cur.getString(cur.getColumnIndex(ContentDescriptor.MembershipExpiryReason.Cols.NAME)));
			}
			cur.close();
			
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_spinner_item, cancel_list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			cancel_reason.setAdapter(dataAdapter);
			cur.close();
			
			TextView button_cancel = (TextView) alert_cancel_ms.findViewById(R.id.button_cancel_text);
			button_cancel.setOnClickListener(this);
			
			TextView button_ok = (TextView) alert_cancel_ms.findViewById(R.id.button_accept);
			button_ok.setOnClickListener(this);
			
			builder.setView(alert_cancel_ms);
			dialog = builder.create();
			dialog.show();
			break;
		}
		case (R.id.text_cancel_date):{
		    datePicker.show(this.getChildFragmentManager(), "datePicker");
			break;
		}
		case (R.id.button_accept):{
			ArrayList<String> validate = validateCancel();
			if (!Boolean.valueOf(validate.get(0))) {
				//highlight the issue!
				updateView(validate);
				return;
			} else {
				//get input.
				dialog.dismiss();
				cancelMembership(getCancelInput(), MemberMembershipFragment.this.membershipid);
			}
			break;
		}
		case (R.id.button_cancel_text):{
			dialog.dismiss();
			break;
		}
		case (R.id.member_membership_row):{
			//show an alert with billing info for the membership...
			int membershipid = Integer.parseInt(v.getTag().toString());
			String[] columns = {ContentDescriptor.Membership.Cols.STATE+" AS 'Membership State:'", 
					ContentDescriptor.Membership.Cols.FIRSTPAYMENT+" AS 'First Payment Date:'",
					ContentDescriptor.Membership.Cols.NEXTPAYMENT+" AS 'Next Payment Date:'",
					ContentDescriptor.Membership.Cols.SIGNUP+" AS 'Signup Fee:'",
					ContentDescriptor.Membership.Cols.PAYMENTDUE+" AS 'Payment Due:'",					
					ContentDescriptor.Membership.Cols.UPFRONT+" AS 'Upfront Fee:'",
					ContentDescriptor.Membership.Cols.TERMINATION_DATE+" AS 'Termination Date:'",
					ContentDescriptor.Membership.Cols.VISITS+" AS 'Concession Count:'",
					ContentDescriptor.Membership.Cols.PNAME};
			Cursor cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, columns, ContentDescriptor.Membership.Cols.MSID+" = ?",
					new String[] {String.valueOf(membershipid)}, null);
			if (!cur.moveToFirst()) {
				break;
			}
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)))
			.setPositiveButton("OK", null);
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			ScrollView listwrapper = (ScrollView) inflater.inflate(R.layout.alert_membership_details, null);
			LinearLayout list = (LinearLayout) listwrapper.findViewById(R.id.alert_membership_details);
			
			for (int i=0; i <cur.getColumnCount(); i++) {
				LinearLayout row = (LinearLayout) inflater.inflate(R.layout.row_membership_details, null);
				boolean add_row = true;
				if (i%2==0) {
					row.setBackgroundColor(Color.TRANSPARENT);
				}
				TextView key = (TextView) row.findViewById(R.id.membership_key);
				key.setText(cur.getColumnName(i));
				
				TextView value = (TextView) row.findViewById(R.id.membership_value);
				if (!cur.isNull(i)) {
					value.setText(cur.getString(i));
				}
				
				if (cur.getColumnName(i).compareTo("Concession Count:")==0) {
					Cursor cur2 = contentResolver.query(ContentDescriptor.Programme.CONTENT_URI, new String[] {ContentDescriptor.Programme.Cols.CONCESSION},
							ContentDescriptor.Programme.Cols.NAME+" = ?", new String[] 
									{cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME))}, null);
					if (cur2.moveToFirst()) {
						if (!cur2.isNull(0) && cur2.getInt(0)> 0) {
							if (cur.isNull(i)) {
								value.setText("0/"+cur2.getInt(0));
							} else {
								value.setText(cur.getString(i)+"/"+cur2.getInt(0));
							}
						} else {
							add_row = false;
						}
					} else {
						add_row = false;
					}
					cur2.close();
				}

				if (cur.getColumnName(i).compareTo(ContentDescriptor.Membership.Cols.PNAME)==0) {
					add_row = false;
				}
				if (add_row) {
					list.addView(row);
				}
			}
			alert.setView(listwrapper);
			alert.show();
			break;
		}
		case (R.id.holdfee):{
			int suspendid = 0;
			if (v.getTag() instanceof Integer) {
				suspendid = (Integer) v.getTag();
			}
			Fragment f = new MembershipHoldFragment();
			Bundle bdl = new Bundle(2);
			bdl.putString(Services.Statics.KEY, memberID);
			bdl.putInt(ContentDescriptor.MembershipSuspend.Cols.SID, suspendid);
			f.setArguments(bdl);
			((MainActivity)getActivity()).changeFragment(f, "MembershipHold");
			break;
		}
		case (R.id.button_add_membership):{
			String memberid = null;
			if (v.getTag() instanceof String) {
				memberid = (String) v.getTag();
			}
			
			Fragment f = new MembershipAdd();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberID);
			f.setArguments(bdl);
			((MainActivity)getActivity()).changeFragment(f, "MembershipAdd");
			break;
		}
		case (R.id.button_hold):{
			Fragment f = new MembershipHoldFragment();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.KEY, memberID);
			f.setArguments(bdl);
			((MainActivity)getActivity()).changeFragment(f, "MembershipHold");
			break;
		}
		case (R.id.button_tag):{
			Cursor cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, ContentDescriptor.Member.Cols.MID+" = ?",
					new String[] {memberID}, null);
			if (!cur.moveToFirst()) break;
			
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.CARDNO))) {
				//confirm we want to overwrite.
				AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Swipe Tag")
		.setMessage("Please Swipe a tag against the device");
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	private void updateView(ArrayList<String> emptyFields) {
		for(int i=1; i<emptyFields.size(); i+=1){
			//get label, change colour.
			TextView label = (TextView) alert_cancel_ms.findViewById(Integer.parseInt(emptyFields.get(i)));
			label.setTextColor(Color.RED);
		}
	}
	
	private ArrayList<String> validateCancel() {
		boolean is_valid = true;
		ArrayList<String> validate = new ArrayList<String>();
		
		//we only need a date.
		TextView date_view = (TextView) alert_cancel_ms.findViewById(R.id.text_cancel_date);
		if (date_view.getText().toString().compareTo(this.getString(R.string.membership_expire_date)) == 0) {
			is_valid = false;
			validate.add(String.valueOf(R.id.text_cancel_date));
		}
		
		validate.add(0, String.valueOf(is_valid));
		return validate;
	}
	
	private ArrayList<String> getCancelInput() {
		ArrayList<String> input = new ArrayList<String>();
		
		TextView date_view = (TextView) this.alert_cancel_ms.findViewById(R.id.text_cancel_date);
		input.add(date_view.getText().toString());														//0
		
		Spinner cancel_reason = (Spinner) alert_cancel_ms.findViewById(R.id.spinner_cancel_reason);
		input.add(cancel_reason.getItemAtPosition(cancel_reason.getSelectedItemPosition()).toString());	//1
		
		EditText cancel_fee = (EditText) alert_cancel_ms.findViewById(R.id.input_cancel_fee);
		if (cancel_fee.getText().toString().compareTo("") != 0) {
			input.add(cancel_fee.getText().toString());													//2
		} else {
			input.add(null);																			//2
		}
		
		return input;
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		TextView date_view = (TextView) alert_cancel_ms.findViewById(R.id.text_cancel_date);
		date_view.setTextColor(this.getResources().getColor(R.color.android_blue));
		//date_view.setText(Services.dateFormat(date, "yyyy MM dd", "dd MMM yyyy"));
		date_view.setText(date);
	}
	
}