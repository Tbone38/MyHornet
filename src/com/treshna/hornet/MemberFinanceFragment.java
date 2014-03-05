package com.treshna.hornet;


import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.BookingPage.TagFoundListener;


public class MemberFinanceFragment extends Fragment implements TagFoundListener, OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	private String final_session = null;
	
	private static final String TAG = "MemberFinanceFragment";
	
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
		setupBilling();
		setupFinance();
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
	
	private View setupBilling() {
		Cursor cur = contentResolver.query(ContentDescriptor.BillingHistory.CONTENT_URI, null, ContentDescriptor.BillingHistory.Cols.MEMBERID+" = ?",
				new String[] {memberID}, ContentDescriptor.BillingHistory.Cols.ID+" DESC");
		
		LinearLayout list = (LinearLayout) view.findViewById(R.id.billing_history_list);
		list.removeAllViews();
		
		if (cur.getCount()<= 0) {
			TextView heading = (TextView) view.findViewById(R.id.member_billing_H);
			heading.setVisibility(View.GONE);
		}
		
		while (cur.moveToNext()) {
			LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.member_finance_row, null);
			row.setTag(cur.getInt(cur.getColumnIndex(ContentDescriptor.BillingHistory.Cols.ID)));
			row.setClickable(true);
			row.setOnClickListener(this);
			
			if (cur.getPosition()%2 ==0) {
				row.setBackgroundColor(Color.WHITE);
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
			}
			list.addView(row);
		}
		
		return view;
	}
	
	private View setupFinance() {
		
		Cursor cur = contentResolver.query(ContentDescriptor.MemberFinance.CONTENT_URI, null, ContentDescriptor.MemberFinance.Cols.MEMBERID+" = ?",
				new String[] {memberID}, ContentDescriptor.MemberFinance.Cols.OCCURRED+" DESC");
		LinearLayout list = (LinearLayout) view.findViewById(R.id.finance_history_list);
		list.removeAllViews();
		
		if (cur.getCount() <= 0) {
			TextView heading = (TextView) view.findViewById(R.id.member_finance_H);
			heading.setVisibility(View.GONE);
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
	public void onNewTag(String serial) {
		mActions.onNewTag(serial);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_billing_add_text):{
			//start async task.
			AddBilling sync = new AddBilling(false);
			sync.execute(null, null);
			break;
		}
		case (R.id.listRow): {
			int id = Integer.parseInt(v.getTag().toString());
			Log.d(TAG, "ID:"+id);
			LinearLayout row = (LinearLayout) view.findViewById(id);
			if (row != null) {
				row.setFocusable(true);
				row.setFocusableInTouchMode(true);
				row.requestFocus();
				row.setClickable(true);
				row.setOnClickListener(null);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					row.setBackground(getActivity().getResources().getDrawable(R.drawable.button));
				} else {
					row.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.button));
				}
				row.performClick(); //this isn't highlighting like it should.
			}
			break;
		}
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (requestCode == 1) {
		     //we need to check the
			  AddBilling sync = new AddBilling(true);
			  sync.doInBackground(null,null);
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
				String api, te_username, te_password;
				api = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.WEB_URL));
				te_username = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_USERNAME));
				te_password = cur.getString(cur.getColumnIndex(ContentDescriptor.Company.Cols.TE_PASSWORD));

				if (!check_status) {
						result = json.DDLogin(api, te_username, te_password);
					if (result) {
						result = json.DDAdd(Integer.parseInt(memberID), api);
					}
				} else { //TODO: check if we need to log in again..?
					result = json.DDcheckStatus(Integer.parseInt(memberID), api, final_session);
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
				Toast.makeText(getActivity(), "Member Billing Added Successfully!", Toast.LENGTH_LONG).show();
				
			} else { //add this back in once we have an actual framework.
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(json.getError()+"\nCODE:"+json.getErrorCode()) //"ERROR:"+...
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
}