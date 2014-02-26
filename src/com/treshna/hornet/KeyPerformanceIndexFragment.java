package com.treshna.hornet;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;



public class KeyPerformanceIndexFragment extends Fragment implements OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	
	private static final String HEADING1 = "Total at";
	private static final String HEADING2 = "Between";
	private static final String HEADING3 = "Booking Summary";
	
	private View view;
	LayoutInflater mInflater;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.key_performace_index_fragment, container, false);
		mInflater = inflater;
		TextView get_kpi = (TextView) view.findViewById(R.id.button_get_kpi);
		get_kpi.setOnClickListener(this);
		
		updateList();
		
		return view;
	}
	/*
	 * I need to do some manual formatting here.
	 */
	private void updateList() {
		Cursor cur = contentResolver.query(ContentDescriptor.KPI.CONTENT_URI, null, null, null, null);
		LinearLayout list = (LinearLayout) view.findViewById(R.id.kpi_list);
		list.removeAllViews();
		
		while (cur.moveToNext()) {
			View row = mInflater.inflate(R.layout.kpi_row, null);
			if (cur.getPosition()%2==0) {
				row.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
			}
			
			String metric = cur.getString(cur.getColumnIndex(ContentDescriptor.KPI.Cols.METRIC));
			if ( metric.contains(HEADING2) || metric.contains(HEADING3)) {
				View row2 = mInflater.inflate(R.layout.kpi_row, null);
				row2.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
				list.addView(row2);
			}
			
			TextView metric_view = (TextView) row.findViewById(R.id.kpi_metric);
			TextView value = (TextView) row.findViewById(R.id.kpi_value);
			if ( metric.contains(HEADING1)||metric.contains(HEADING2) || metric.contains(HEADING3)) {
				value.setVisibility(View.GONE);
				metric_view.setTypeface(null, Typeface.BOLD);
			}
			
			
			metric_view.setText(metric);
			
			
			value.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.KPI.Cols.VALUE)));
			
			list.addView(row);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case (R.id.button_get_kpi):{
			GetKPIs async = new GetKPIs();
			async.execute(null, null);
			break;
		}
		}
	}
	
	private class GetKPIs extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;

		
		public GetKPIs () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			 progress = ProgressDialog.show(getActivity(), "Retrieving..", 
					 "Retrieving Key Performance Indicators...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			int result = sync.getKPIs(getActivity());
			return (result > 0);
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				updateList();
			} else {
				//show an error.
			}
	    }
	 }

}