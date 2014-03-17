package com.treshna.hornet;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


public class ReportColumnOptionsActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_column_options);
		Intent intent = this.getIntent();

		Date startDate  =  new Date(intent.getLongExtra("start_date", 0));
		Date endDate  =  new Date(intent.getLongExtra("end_date", 0));
		String reportId = intent.getStringExtra("report_id");
		String reportName = intent.getStringExtra("report_name");
		String functionName = intent.getStringExtra("report_function_name");
		functionName = functionName.substring(0, functionName.indexOf('('));
	    System.out.println("Function: " + functionName);
		GetReportDataByDateRange syncNames = new GetReportDataByDateRange(functionName, startDate, endDate);
		syncNames.execute(null,null);
		
	}
	
	private class GetReportDataByDateRange extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private ResultSet result = null;
		private String functionName = null;
		private Date startDate = null;
		private Date endDate = null;
		
	
		public GetReportDataByDateRange (String functionName, Date startDate, Date endDate) {
			sync = new HornetDBService();
			this.functionName = functionName;
			this.startDate = startDate;
			this.endDate = endDate;
		}
		
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportColumnOptionsActivity.this, "Retrieving..", 
					 "Retrieving Report Date By Date Range...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportDataByDateRange(ReportColumnOptionsActivity.this, functionName, startDate, endDate);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				
				System.out.println("\nReport-Type_Data");
				
				System.out.println("Result List Size: " + resultMapList.size());
				
				for (HashMap<String,String> resultMap: resultMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}
				//Calls back to the owning activity to build the adapter
				//ReportColumnOptionsActivity.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportColumnOptionsActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
}
