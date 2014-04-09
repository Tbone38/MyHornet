package com.treshna.hornet.member;


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
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R.color;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.network.JSONHandler;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.services.Services.Statics;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.sqlite.ContentDescriptor.BillingHistory;
import com.treshna.hornet.sqlite.ContentDescriptor.Company;
import com.treshna.hornet.sqlite.ContentDescriptor.MemberFinance;
import com.treshna.hornet.sqlite.ContentDescriptor.Company.Cols;

public class MemberFinanceFragment extends Fragment implements TagFoundListener, OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	private String final_session = null;
	
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
		setupFinance();
		return view;
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupButton() {
		TextView billing_add = (TextView) view.findViewById(R.id.button_billing_add_text);
		billing_add.setOnClickListener(this);
		
		TextView billing_show = (TextView) view.findViewById(R.id.button_billing_view_text);
		billing_show.setOnClickListener(this);
		
		TextView billing_check = (TextView) view.findViewById(R.id.button_billing_check_text);
		billing_check.setOnClickListener(this);
		
		return view;
	}
	
	private void setupBilling() {
		Cursor cur = contentResolver.query(ContentDescriptor.BillingHistory.CONTENT_URI, null, ContentDescriptor.BillingHistory.Cols.MEMBERID+" = ?",
				new String[] {memberID}, ContentDescriptor.BillingHistory.Cols.ID+" DESC");
		if (cur.getCount()<=0) {
			Toast.makeText(getActivity(), "No Billing History Available.", Toast.LENGTH_LONG).show();
			return;
		}
		
		View view = mInflater.inflate(R.layout.alert_billing_details, null);
		LinearLayout list = (LinearLayout) view.findViewById(R.id.billing_history_list);
		list.removeAllViews();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Billing History")
		.setPositiveButton("OK", null);
		
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.member_finance_row, null);
			
			if (cur.getPosition()%2 ==0) {
				row.setBackgroundColor(Color.TRANSPARENT);
			}
			
			TextView date_view = (TextView) row.findViewById(R.id.finance_row_occurred);
			Date occurred = new Date((long) cur.getDouble(cur.getColumnIndex(ContentDescriptor.BillingHistory.Cols.PROCESSDATE)));
			date_view.setText(Services.DateToString(occurred));
			
			TextView note_view = (TextView) row.findViewById(R.id.finance_row_note);
			note_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.BillingHistory.Cols.NOTE)));
			
			TextView amount_view2 = (TextView) row.findViewById(R.id.finance_row_amount2);
			amount_view2.setVisibility(View.GONE);
			
			TextView amount_view = (TextView) row.findViewById(R.id.finance_row_amount1);
			amount_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.BillingHistory.Cols.AMOUNT)));
			View colour_block = (View) row.findViewById(R.id.finance_colour_block);
			
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.BillingHistory.Cols.FAILED)).compareTo("f")==0) {
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_green));
			} else {
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_red));
				note_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.BillingHistory.Cols.FAILREASON)));
			}
			list.addView(row);
		}
		cur.close();
		builder.setView(view);
		builder.show();
		
	}
	
	private View setupFinance() {
		
		Cursor cur = contentResolver.query(ContentDescriptor.MemberFinance.CONTENT_URI, null, ContentDescriptor.MemberFinance.Cols.MEMBERID+" = ?",
				new String[] {memberID}, ContentDescriptor.MemberFinance.Cols.OCCURRED+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.finance_history_list);
		list.removeAllViews();
		
		if (cur.getCount() <= 0) {
			LinearLayout heading_row = (LinearLayout) view.findViewById(R.id.member_financeH_row);
			heading_row.setVisibility(View.GONE);
		}
		
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.member_finance_row, null);
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.DD_EXPORT_MEMBERID))) {
				row.setId(cur.getInt(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.DD_EXPORT_MEMBERID)));
			}
			
			if (cur.getPosition()%2==0) {
				row.setBackgroundColor(Color.WHITE);
			}
			
			TextView date_view = (TextView) row.findViewById(R.id.finance_row_occurred);
			Date occurred = new Date((long)(cur.getDouble(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.OCCURRED))));
			date_view.setText(Services.DateToString(occurred));
			
			TextView note_view = (TextView) row.findViewById(R.id.finance_row_note);
			note_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.NOTE)));
			
			View colour_block = (View) row.findViewById(R.id.finance_colour_block);
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.DEBIT))) {
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_green));
				TextView amount_view = (TextView) row.findViewById(R.id.finance_row_amount1);
				amount_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.CREDIT)));
			} else {
				colour_block.setBackgroundColor(getActivity().getResources().getColor(R.color.visitors_red));
				TextView amount_view = (TextView) row.findViewById(R.id.finance_row_amount2);
				amount_view.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.MemberFinance.Cols.DEBIT)));
			}
			
			list.addView(row);
		}
		cur.close();
		
		mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public boolean onNewTag(String serial) {
		return mActions.onNewTag(serial);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_billing_add_text):{
			//start async task.
			AddBilling sync = new AddBilling(false);
			sync.execute(null, null);
			break;
		}
		case (R.id.button_billing_view_text):{
			setupBilling();
			break;
		}
		case (R.id.button_billing_check_text):{
			AddBilling sync = new AddBilling(true);
			sync.execute(null, null);
			break;
		}
		}
	}
	
	private class AddBilling extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private JSONHandler json;
		private boolean check_status;

		
		public AddBilling (boolean check_status) {
			json = new JSONHandler(getActivity());
			this.check_status = check_status;
		}
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(getActivity(), "Syncing..", 
					 "Retrieving Member Debit Key...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			Cursor cur = contentResolver.query(ContentDescriptor.Company.CONTENT_URI, null, null, null, null);
			if (cur.moveToFirst()) {
				String te_username, te_password;
				te_username = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_USERNAME));
				te_password = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_PASSWORD));

				if (!check_status) {
						result = json.DDLogin(te_username, te_password);
					if (result) {
						result = json.DDAdd(Integer.parseInt(memberID), te_username);
					}
				} else { //TODO: check if we need to log in again..?
					result = json.DDLogin(te_username, te_password);
					if (result) {
						result = json.DDcheckStatus(Integer.parseInt(memberID), te_username, final_session);
					}
				}
			}
			cur.close();
			return result;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success && !check_status) {
				String url = json.getURL();
				if (!url.contains("http")) url = "http://"+url;
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				//getActivity().startActivity(i);
				MemberFinanceFragment.this.final_session = json.get_session();
				getActivity().startActivityForResult(i, 1);
			} else if (success && check_status) {
				//wooh! success.
				Toast.makeText(getActivity(), "Member Billing Status: Good", Toast.LENGTH_LONG).show();
				
			} else { //add this back in once we have an actual framework.
				if (json.getErrorCode() != 20) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Error Occurred")
					.setMessage(json.getError()+"\nCODE:"+json.getErrorCode()) //"ERROR:"+...
					.setPositiveButton("OK", null)
					.show();
				} else {
					Toast.makeText(getActivity(), "No Response From Server, is Your gymmaster site setup?", Toast.LENGTH_LONG).show();
				}
			}
	    }
	 }
}