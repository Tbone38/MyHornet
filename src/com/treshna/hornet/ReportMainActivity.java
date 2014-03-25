package com.treshna.hornet;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
	private ArrayList<HashMap<String,String>> columnsMapList = null;
	private String reportName = null;
	private String mainQuery = null;
	private String[] selectedColumnNames = null;
	private HashMap<String,String> fieldsMap  = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_main_list);
		//Fetching data passed through from the preceding Activities..
		Intent intent = this.getIntent();
		Date startDate  =  new Date(intent.getLongExtra("start_date", 0));
		Date endDate  =  new Date(intent.getLongExtra("end_date", 0));
		String reportId = intent.getStringExtra("report_id");
		selectedColumnNames = intent.getStringArrayExtra("selected_column_names");
		reportName = intent.getStringExtra("report_name");
	    this.mainQuery = ReportQueryResources.getMainQuery(this.getApplicationContext(),reportId);	
		this.fieldsMap = ReportQueryResources.getAllQueryFields(this.getApplicationContext(),reportId);
		this.buildQuery();
		System.out.println(mainQuery);
		this.getReportData(mainQuery, startDate , endDate);
	    
	}
	
	private void buildQuery(){
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ");
		
		for (Map.Entry<String,String> field: fieldsMap.entrySet()){
			  
				if (this.isColumnSelected(field.getKey().toString())){
					queryBuilder.append(field.getValue());
					queryBuilder.append(',');
					queryBuilder.append(' ');
				}
		}
		
		//remove the last comma...
		queryBuilder.replace(queryBuilder.length() - 2, queryBuilder.length(), " ");
		queryBuilder.append(mainQuery);
		/*queryBuilder.append("ORDER BY ");
		//Adding the select columns to order by..
		for (Map.Entry<String,String> field: fieldsMap.entrySet()){
			  
			if (this.isColumnSelected(field.getKey().toString())){
				queryBuilder.append(field.getValue());
				queryBuilder.append(',');
				queryBuilder.append(' ');
			}
		}*/
		queryBuilder.append(";");
		mainQuery = queryBuilder.toString();
		
		
		}
		
	private boolean isColumnSelected(String columnName){
		
		for (int i = 0; i < this.selectedColumnNames.length; i++){
			System.out.println("Selected Columns: " +selectedColumnNames[i]);
			if (selectedColumnNames[i] != null){
				 if (columnName.compareTo(selectedColumnNames[i])== 0){
					return true;
				 }
			}
		}
		return false;
	}

	
	private void buildListAdapter() {
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();
			TextView textView  = null;
			TextView reportNameTextView = (TextView) findViewById(R.id.report_main_title);
			reportNameTextView.setText(reportName);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
			BigDecimal valueCharLength = null;
			HashMap<String,String> dataRow = resultMapList.get(0);
			LinearLayout reportHeadingLayout = (LinearLayout) this.findViewById(R.id.report_list_headings);
			for (Entry<String,String> row : dataRow.entrySet()){
				
				//if (row.getValue()!= null && !row.getValue().isEmpty() && !(row.getKey().toString().compareTo("id")==0)) {
				if (!isColumnAllNull(row.getKey().toString())) {
					textView =  new TextView(ReportMainActivity.this);
					valueCharLength = new BigDecimal(row.getValue().toString().length());
					valueCharLength.setScale(BigDecimal.ROUND_DOWN);
					int textBaseMargin = 5;
					int colMargin = textBaseMargin;
					//System.out.println("Value Length: " + valueCharLength.intValue());
					if (row.getKey().length() < valueCharLength.intValue())
					{
						colMargin  += valueCharLength.intValue(); 
					}
					//System.out.println("Key Length: " + row.getKey().length());
					//System.out.println("Col Margin: " + colMargin);		
					layoutParams = new LinearLayout.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1);
					textView.setTextAppearance(ReportMainActivity.this,attr.textAppearanceLarge);
					textView.setTextAppearance(ReportMainActivity.this, attr.textStyle|2);
					layoutParams.setMargins(textBaseMargin, 0, 0, 0);
					textView.setLayoutParams(layoutParams);
					textView.setText(row.getKey());
					reportHeadingLayout.addView(textView);
				}
			}
			
			ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(ReportMainActivity.this,R.layout.report_main_row,
					this.resultMapList){

						@TargetApi(Build.VERSION_CODES.HONEYCOMB)
						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {
						//Dynamically binding column names to textView text
						TextView textView  = null;
						//LayoutInflater inflater = LayoutInflater.from(getContext());
						//Loops to find all which columns are all null 
						//convertView  = parent.findViewById(R.layout.report_main_row);
						//convertView  = inflater.inflate(R.layout.report_main_row, null);
						LinearLayout linLayout = new LinearLayout(ReportMainActivity.this);
						linLayout.setOrientation(LinearLayout.HORIZONTAL);
						AbsListView.LayoutParams listLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
						linLayout.setLayoutParams(listLayoutParams);
						HashMap<String,String> dataRow = this.getItem(position);
						for (Entry<String,String> col : dataRow.entrySet()){
									
							//if (col.getValue()!= null && !col.getValue().isEmpty() && !(col.getKey().toString().compareTo("id")==0)) {
							if (!isColumnAllNull(col.getKey().toString())) {
								  	layoutParams = new LinearLayout.LayoutParams( 0,LayoutParams.WRAP_CONTENT,3);
									//Dynamically generate text views for each column name..
									//System.out.println("Column Name Main Row: " + row.getKey());
									textView =  new TextView(ReportMainActivity.this);
									layoutParams.setMargins(5, 0, 0, 0);
									textView.setLayoutParams(layoutParams);
									textView.setText(col.getValue());
									linLayout.addView(textView);
							}
						}	
							
							return linLayout;
						}
						
						private boolean isColumnAllNull(String colName) {
							HashMap<String,Integer> colNullCount = new HashMap<String,Integer>();
							
							for (HashMap<String,String> dataRow: resultMapList){
								
								for (Entry<String,String> col : dataRow.entrySet()){

									if (!colNullCount.containsKey(col.getKey())){
										colNullCount.put(col.getKey(), 1); 
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
						
						
			        };
			this.setListAdapter(listAdapter);
	  }
	}
	
	private boolean isColumnAllNull(String colName) {
		HashMap<String,Integer> colNullCount = new HashMap<String,Integer>();
		
		for (HashMap<String,String> dataRow: resultMapList){
			
			for (Entry<String,String> col : dataRow.entrySet()){

				if (!colNullCount.containsKey(col.getKey())){
					colNullCount.put(col.getKey(), 1); 
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
	
	protected void getReportData (String functionName, Date startDate, Date endDate) {
		
		GetReportDataByDateRange syncNames = new GetReportDataByDateRange(functionName, startDate , endDate);
		syncNames.execute(null,null);
		
	}

	
	protected class GetReportDataByDateRange extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
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
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Date By Date Range...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportDataByDateRange(ReportMainActivity.this,functionName, startDate, endDate);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
			/*
				System.out.println("\nReport-Type_Data");
				
				System.out.println("Result List Size: " + resultMapList.size());
				
				for (HashMap<String,String> resultMap: resultMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}*/
				//Calls back to the owning activity to build the adapter
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
		
}
