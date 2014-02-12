package com.treshna.hornet;


import java.util.ArrayList;
import java.util.List;

import com.treshna.hornet.BookingPage.TagFoundListener;
import com.treshna.hornet.DatePickerFragment.DatePickerSelectListener;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class MemberMembershipFragment extends Fragment implements TagFoundListener, OnClickListener, DatePickerSelectListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	private int membershipid;
	private View alert_cancel_ms;
	
	private DatePickerFragment datePicker;
	
	//private static final String TAG = "MemberDetails";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		mActions = new MemberActions(getActivity());
		datePicker = new DatePickerFragment();
		datePicker.setDatePickerSelectListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_membership, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupView();
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setupView();
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupView() {
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, 
				ContentDescriptor.Membership.Cols.MID+" = ?", new String[] {memberID}, null);
		
		LinearLayout mslist = (LinearLayout) view.findViewById(R.id.member_ms_list);
		mslist.removeAllViews();
		
		while (cur.moveToNext()) {
			//Log.e(TAG, "Inflater is:"+mInflater.toString());
			RelativeLayout membershipRow = (RelativeLayout) mInflater.inflate(R.layout.member_membership_row, null);
			
			ImageView icon = (ImageView) membershipRow.findViewById(R.id.member_membership_drawable);
			icon.setColorFilter(Services.ColorFilterGenerator.setColourGrey());
			
			ImageView cancel_membership = (ImageView) membershipRow.findViewById(R.id.member_membership_cancel);
			cancel_membership.setTag(cur.getInt(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
			cancel_membership.setColorFilter(Services.ColorFilterGenerator.setColourRed());
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
		mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public void onNewTag(String serial) {
		mActions.onNewTag(serial);
	}
	
	private void cancelMembership(ArrayList<String> inputs, int membershipid) {
		/*contentResolver.delete(ContentDescriptor.Membership.CONTENT_URI, ContentDescriptor.Membership.Cols.MSID+" = ?",
				new String[] {String.valueOf(membershipid)});*/
		//TODO: we need to hide/grey-out expired memberships.
		//			we need a way to check the membership status?
		// 		we need somewhere to insert this input to.
		//probably need more columns on our membership table.
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
			
			builder.setView(alert_cancel_ms);
			
			
			builder.setNegativeButton("Cancel", null);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					//do vailidation.
					ArrayList<String> validate = validateCancel();
					if (!Boolean.valueOf(validate.get(0))) {
						//highlight the issue!
						updateView(validate);
					}
					//get input.
					cancelMembership(getCancelInput(), MemberMembershipFragment.this.membershipid);
				}});
			
			builder.show();
			break;
		}
		case (R.id.text_cancel_date):{
		    datePicker.show(this.getChildFragmentManager(), "datePicker");
			break;
		}
		
		}
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
		TextView date_view = (TextView) this.alert_cancel_ms.findViewById(R.id.text_cancel_date);
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
		input.add(date_view.getText().toString());
		
		Spinner cancel_reason = (Spinner) alert_cancel_ms.findViewById(R.id.spinner_cancel_reason);
		input.add(cancel_reason.getItemAtPosition(cancel_reason.getSelectedItemPosition()).toString());
		
		EditText cancel_fee = (EditText) alert_cancel_ms.findViewById(R.id.input_cancel_fee);
		if (cancel_fee.getText().toString().compareTo("") != 0) {
			input.add(cancel_fee.getText().toString());
		} else {
			input.add(null);
		}
		
		return input;
	}

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		TextView date_view = (TextView) alert_cancel_ms.findViewById(R.id.text_cancel_date);
		date_view.setTextColor(this.getResources().getColor(R.color.android_blue));
		date_view.setText(date);
	}
	
}