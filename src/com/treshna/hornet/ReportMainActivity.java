package com.treshna.hornet;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.R.attr;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class ReportMainActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	private ArrayList<HashMap<String,String>> columnsMapList =  null;
	private ArrayList<HashMap<String,String>> joiningTablesMapList =  null;
	private String reportName = null;
	private String reportFunctionName = null;
	private String queryFunctionParamsCut = null; 
	private String callingActivity = null;
	private String finalQuery = null;
	private Date startDate = null;
	private Date endDate = null;
	private boolean stripe = true;
	private int[] selectedColumnIds = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_main_list);
		//Fetching data passed through from the preceding Activities..
		Intent intent = this.getIntent();
		startDate  =  new Date(intent.getLongExtra("start_date", 0));
		endDate  =  new Date(intent.getLongExtra("end_date", 0));
		int reportId = intent.getIntExtra("report_id",0);
		callingActivity = intent.getStringExtra("calling_activity");
		if (callingActivity.compareTo("column_options")==  0){
			selectedColumnIds = intent.getIntArrayExtra("selected_column_ids");
		}
		reportName = intent.getStringExtra("report_name");
		reportFunctionName = intent.getStringExtra("report_function_name");
		queryFunctionParamsCut = reportFunctionName.substring(0,reportFunctionName.indexOf('('));
		this.getColumnData(reportId);
	    
	}
	
	private void buildQuery(){
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ");
		boolean isSelected = true;
	
		for (HashMap<String,String> columnsMap : columnsMapList ){
			
			if (callingActivity.compareTo("column_options")== 0) {
				isSelected = false;
				for (Map.Entry<String,String> field: columnsMap.entrySet()){
					
					if (field.getKey().toString().compareTo("column_id")== 0){
						if (this.isColumnSelected(Integer.parseInt(field.getValue()))){
							isSelected = true;
						}
						
					}
				}
			}	
		for (Map.Entry<String,String> field: columnsMap.entrySet()){
				//System.out.println(field.getKey() + " : " + field.getValue());
				if (isSelected){
					if(field.getKey().toString().compareTo("field")== 0){
							queryBuilder.append(field.getValue());
							queryBuilder.append(" AS ");
							
					}
					if(field.getKey().toString().compareTo("column_name")== 0){
								queryBuilder.append("\"" + field.getValue() + "\"");
								queryBuilder.append(", ");
					}				

			   }
			
           }							
		 }

			//remove the last comma...
			queryBuilder.replace(queryBuilder.length() - 2, queryBuilder.length(), " ");
			queryBuilder.append(" FROM ");
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-M-dd");
			String paramsWithBrackets = this.reportFunctionName.substring(reportFunctionName.indexOf('('),reportFunctionName.length());
			String reportFunctionWithDateParams = "";
			if (paramsWithBrackets.length() > 2) {
				reportFunctionWithDateParams =  this.queryFunctionParamsCut + "(" + "'" + dateFormatter.format(this.startDate) + "'" +"::Date"  + "," + "'" + dateFormatter.format(this.endDate) + "'" + "::Date" +  ')';
			} else {
				reportFunctionWithDateParams =  this.queryFunctionParamsCut + "()";
			}
			queryBuilder.append(reportFunctionWithDateParams);
			queryBuilder.append("As fun ");
			
			
			for (HashMap<String,String> joinsMap : joiningTablesMapList ){
				for (Map.Entry<String,String> field: joinsMap.entrySet()){
					queryBuilder.append(field.getValue());
					queryBuilder.append(' ');	
				}
			}
			
			queryBuilder.append(";");
			System.out.println(queryBuilder.toString());
			finalQuery = queryBuilder.toString();
			this.getReportData(finalQuery);
		
		/*
		//remove the last comma...
		queryBuilder.replace(queryBuilder.length() - 2, queryBuilder.length(), " ");
		queryBuilder.append(mainQuery);
		queryBuilder.append("ORDER BY ");
		//Adding the select columns to order by..
		for (Map.Entry<String,String> field: fieldsMap.entrySet()){
			  
			if (this.isColumnSelected(field.getKey().toString())){
				queryBuilder.append(field.getValue());
				queryBuilder.append(',');
				queryBuilder.append(' ');
			}
		}*/

}	

		
	private boolean isColumnSelected(int columnId){
		
		for (int i = 0; i < this.selectedColumnIds.length; i++){
			if (selectedColumnIds[i] != 0){
				 if (selectedColumnIds[i]== columnId){
					return true;
				 }
			}
		}
		return false;
	}

	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void buildListAdapter() {
		
		    buildColumnHeaders();

			ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(ReportMainActivity.this,R.layout.report_main_row,
					this.resultMapList){

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {
						//Dynamically binding column names to textView text
						TextView textView  = null;
						
						LinearLayout linLayout = new LinearLayout(ReportMainActivity.this);
						//Adding zebra striping on alternate rows
						if (position % 2 == 0){
							linLayout.setBackgroundColor(getResources().getColor(R.color.booking_resource_background));
						}
						ReportMainActivity.this.stripe = false;
						linLayout.setOrientation(LinearLayout.HORIZONTAL);
						AbsListView.LayoutParams listLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
						linLayout.setLayoutParams(listLayoutParams);
						HashMap<String,String> dataRow = this.getItem(position);
						for (Entry<String,String> col : dataRow.entrySet()){
							if (!isColumnAllNull(col.getKey().toString())) {
								  	layoutParams = new LinearLayout.LayoutParams( 0,LayoutParams.WRAP_CONTENT,3);
								  	layoutParams.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
									//Dynamically generate text views for each column name..
									textView =  new TextView(ReportMainActivity.this);
									if (col.getValue() != null &&  col.getValue().toString().matches("[0-9]+")){
										Log.i("Numeric Column", col.getKey());
										textView.setMinWidth(col.getKey().toString().length());
										textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
									}
									layoutParams.setMargins(5, 0, 0, 0);
									textView.setLayoutParams(layoutParams);
									textView.setText(col.getValue());						

									linLayout.addView(textView);
							}
						}	
							
							return linLayout;
						}
													
			        };
			this.setListAdapter(listAdapter);
	  }
	

	
	private void buildColumnHeaders() {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
			LinearLayout reportColumnHeadingLayout = (LinearLayout) this.findViewById(R.id.report_list_headings);
			TextView reportNameTextView = (TextView) findViewById(R.id.report_main_title);
			reportNameTextView.setText(reportName);
			
		if (resultMapList.size() > 0){
			//Building title header row...
			HashMap<String,String> dataRow = resultMapList.get(0);

			//Building column header row...

			for (Entry<String,String> row : dataRow.entrySet()){
						
				 if (!isColumnAllNull(row.getKey().toString())) {
					 
					 reportColumnHeadingLayout.addView(buildColumnHeaderTextView(row.getKey(), Gravity.NO_GRAVITY, 16));
				}
			}
			
		} else {
			
			reportColumnHeadingLayout.addView(buildColumnHeaderTextView("No Data Available", Gravity.CENTER, 22));
			
		}
	}
	
	
	private TextView buildColumnHeaderTextView(String contentString, int layoutGravity, int textSize) {
		TextView textView  = null;
		LinearLayout.LayoutParams layoutParams = null;
		textView =  new TextView(ReportMainActivity.this);		
		layoutParams = new LinearLayout.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		textView.setGravity(layoutGravity);
		layoutParams.setMargins(5, 0, 5, 0);
		textView.setLayoutParams(layoutParams);
		textView.setText(contentString);
		return textView;
	}
	
	
	private boolean isColumnAllNull(String colName) {
		HashMap<String,Integer> colNullCount = new HashMap<String,Integer>();
		
		for (HashMap<String,String> dataRow: resultMapList){
			
			
			
			for (Entry<String,String> col : dataRow.entrySet()){


				if (!colNullCount.containsKey(col.getKey())){
					colNullCount.put(col.getKey(),0); 
				}
				else
				{
					if (col.getValue()== null || col.getValue().isEmpty() ){
						colNullCount.put(col.getKey(),colNullCount.get(col.getKey() )+ 1 );
					}																										
				}														
			}																
		}
		 return colNullCount.get(colName) == resultMapList.size();
	}
	
	private void getReportData (String finalQuery) {
		
		GetReportDataByDateRange syncNames = new GetReportDataByDateRange(finalQuery);	
		syncNames.execute(null,null);
		
	}
	
	private void getColumnData (int reportId) {
		
		GetReportColumnsFieldsByReportId syncColumns  = new GetReportColumnsFieldsByReportId(reportId);	
		syncColumns.execute(null,null);
		
	}
	
	private void getJoiningTablesData (String functionName) {
		
		GetJoiningTablesByFunctionName syncColumns  = new GetJoiningTablesByFunctionName(functionName);	
		syncColumns.execute(null,null);
		
	}
	
	

	protected class GetReportDataByDateRange extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private String finalQuery = null;
		
	
		public GetReportDataByDateRange (String finalQuery) {
			sync = new HornetDBService();
			this.finalQuery = finalQuery;

		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Date By Date Range...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportDataByDateRange(ReportMainActivity.this,finalQuery);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {

				ReportMainActivity.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportMainActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	protected class GetReportColumnsFieldsByReportId extends AsyncTask<String, Integer, Boolean> {
		protected ProgressDialog progress;
		protected HornetDBService sync;
		private int reportId = 0;
		
		public GetReportColumnsFieldsByReportId () {
			sync = new HornetDBService();

		}
		
		
		public GetReportColumnsFieldsByReportId (int reportId) {
			sync = new HornetDBService();
			this.reportId = reportId;

		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Column Data...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			columnsMapList = sync.getReportColumnsFieldsByReportId(ReportMainActivity.this,reportId);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				//Calls back to the owning activity to call the thread to retrieve the joining tables
				ReportMainActivity.this.getJoiningTablesData(reportFunctionName);
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportMainActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	private class GetJoiningTablesByFunctionName extends GetReportColumnsFieldsByReportId {
		private String functionName = null;

		public GetJoiningTablesByFunctionName(String functionName) {
			super();
			this.functionName = functionName;
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Joining Table Data...");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			joiningTablesMapList = sync.getJoiningTablesByFunctionName(ReportMainActivity.this, functionName);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				//Calls back to the owning activity to build the adapter
				ReportMainActivity.this.buildQuery();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportMainActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	      }
		}				
	}
		
