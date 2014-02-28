package com.treshna.hornet;


import java.util.Date;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.treshna.hornet.BookingPage.TagFoundListener;


public class MemberFinanceFragment extends Fragment implements TagFoundListener, OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	//private static final String TAG = "MemberVisitHistory";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		//visitDate = this.getArguments().getString(Services.Statics.KEY);
		mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_finance, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		setupButton();
		view = setupList();
		return view;
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupButton() {
		TextView billing_add = (TextView) view.findViewById(R.id.button_billing_add_text);
		billing_add.setOnClickListener(this);
		
		return view;
	}
	
	private View setupList() {
		
		Cursor cur = contentResolver.query(ContentDescriptor.MemberFinance.CONTENT_URI, null, ContentDescriptor.MemberFinance.Cols.MEMBERID+" = ?",
				new String[] {memberID}, ContentDescriptor.MemberFinance.Cols.OCCURRED+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.finance_history_list);
		list.removeAllViews();
		
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.member_finance_row, null);
			
			if (cur.getPosition()%2==0) {
				row.setBackgroundColor(Color.WHITE);
			}
			
			TextView date_view = (TextView) row.findViewById(R.id.finance_row_occurred);
			Date occurred = new Date((long)(cur.getDouble(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.OCCURRED))*1000));
			date_view.setText(Services.DateToString(occurred));
			
			TextView note_view = (TextView) row.findViewById(R.id.finance_row_note);
			note_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.NOTE)));
			
			TextView amount_view = (TextView) row.findViewById(R.id.finance_row_amount);
			View colour_block = (View) row.findViewById(R.id.finance_colour_block);
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.DEBIT))) {
				//colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.android_blue));
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_green));
				amount_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.CREDIT)));
			} else {
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_red));
				amount_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.DEBIT)));
			}
			
			list.addView(row);
		}
		cur.close();
		
		mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public void onNewTag(String serial) {
		mActions.onNewTag(serial);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_billing_add_text):{
			//start async task.
			AddBilling sync = new AddBilling();
			sync.execute(null, null);
			break;
		}
		}
	}
	
	private class AddBilling extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private JSONHandler json;

		
		public AddBilling () {
			json = new JSONHandler(getActivity());
		}
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(getActivity(), "Syncing..", 
					 "Retrieving Member Debit Key...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			int result = 0;
			Cursor cur = contentResolver.query(ContentDescriptor.Company.CONTENT_URI, null, null, null, null);
			if (cur.moveToFirst()) {
				String api, te_username, te_password;
				api = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.WEB_URL));
				te_username = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_USERNAME));
				te_password = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_PASSWORD));
				result = json.AddMemberBillingType(Integer.parseInt(memberID), api, te_username, te_password);
			}
			cur.close();
			return (result > 0);
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			//if (success) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("http://"+json.getURL()));
				getActivity().startActivity(i);
			/*} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage("ERROR:"+json.getError()+"\nCODE:"+json.getErrorCode())
				.setPositiveButton("OK", null)
				.show();
			}*/
	    }
	 }
}