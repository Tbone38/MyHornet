package com.treshna.hornet;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ReportColumnOptionsActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	//private HashMap<String,String> fieldsMap  = null;
	private String[] selectedColumns = null;
	private long startDate = 0;
	private long endDate = 0;
	private int reportId = 0;
	private String reportName = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_column_options);
		//Fetching data passed through from the preceding Activities..
		Intent intent = this.getIntent();
		startDate  =  intent.getLongExtra("start_date", 0);
		endDate  =  intent.getLongExtra("end_date", 0);
		reportId = Integer.parseInt(intent.getStringExtra("report_id"));
		reportName = intent.getStringExtra("report_name");
		Button createBtn = (Button) this.findViewById(R.id.btnCreateReport);
	    /*fetching query fields from xml data file..
	    fieldsMap = ReportQueryResources.getAllQueryFields(this.getApplicationContext(),reportId);
	    */
	    createBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 getCheckedColumns();
				 //loadMainReportActivity();
			}
		});
	    
	    this.getReportColumnOptions();
	}
	
	private void buildListAdapter() {
		if (this.resultMapList.size() > 0){
			ListView listView = this.getListView();
			//PrintQueryResultData();
			ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(ReportColumnOptionsActivity.this,R.layout.report_column_options_row,
					this.resultMapList){

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {
						//Dynamically binding column names to textView text
						LayoutInflater inflater = LayoutInflater.from(getContext());
						HashMap<String,String> dataRow =  this.getItem(position);
						for (Entry<String,String> row : dataRow.entrySet()){							
							//Attaching a tag with column name value to the checkBox
							TextView columnName	 = null;
							CheckBox columnBox  = null;

							if (row.getKey().toString().compareTo("column_name")== 0){
								
								if (columnName.getText()== null){	
								
									columnName.setText(row.getValue());
								} else {
									
									convertView  = inflater.inflate(R.layout.report_column_options_row, null);
									columnName	 = (TextView) convertView.findViewById(R.id.report_column_name);
									columnBox = (CheckBox) convertView.findViewById(R.id.column_checkBox);
									
								}
																							 																
								
							} else if (row.getKey().toString().compareTo("report_field_id")== 0) {
								

								
									columnBox.setTag(row.getValue());
																
							}
							
						}	
							
							return convertView;
						}
						
			        };
			this.setListAdapter(listAdapter);
	  }
	}
	
	protected void getReportColumnOptions () {
		
		GetReportColumnOptions syncData = new GetReportColumnOptions();
		syncData.execute(null,null);
		
	}

	
	private ArrayList<HashMap<String,String>> getResultColumnNames () {
		HashMap<String,String> rowMap = null;
		HashMap<String,String> colName = null;
		ArrayList<HashMap<String,String>> colNamesList = new ArrayList<HashMap<String,String>>();
		
		int index = 0;
		for (Entry<String, String> row : rowMap.entrySet()){
			
				System.out.println("Column Name: " + row.getValue());
				colName = new HashMap<String,String>();
				colName.put("column_name", row.getKey());

			colNamesList.add(colName);
			index ++;
		}
		
		
		
		return colNamesList;
	}
	
	private void loadMainReportActivity() {
		Intent mainReportIntent = new Intent(this.getApplicationContext(), ReportMainActivity.class);
		mainReportIntent.putExtra("report_id", reportId);
		mainReportIntent.putExtra("report_name", reportName);
		mainReportIntent.putExtra("selected_column_names", selectedColumns);
		mainReportIntent.putExtra("start_date", startDate);
		mainReportIntent.putExtra("end_date", endDate);
		startActivity(mainReportIntent);
	}
	
	private void getCheckedColumns () {
	  ListView listView = this.getListView();
	  View view = null;
	  CheckBox checkBox = null;
	  this.selectedColumns = new String[listView.getCount()];
	  for (int i = 0; i < listView.getCount(); i++){
		  	view = listView.getChildAt(i);
		  	checkBox = (CheckBox) view.findViewById(R.id.column_checkBox);
		  	if (checkBox.isChecked())
		  		System.out.println(checkBox.getTag());
			    //selectedColumns[i] = checkBox.getTag().toString();
		  	
	  }    	  
	}
	
	private void PrintQueryResultData () {
		for (HashMap<String,String> resultMap: this.resultMapList){
			for (Entry<String,String> column: resultMap.entrySet()){
				System.out.println(column.getKey()+ " " + column.getValue());
			}
		}
	}
	
	
	
	protected class GetReportColumnOptions extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private ResultSet result = null;
		
			
		public GetReportColumnOptions () {
			sync = new HornetDBService();
	
		}
		
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportColumnOptionsActivity.this, "Retrieving..", 
					 "Retrieving Report Column Names...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportColumnsByReportId(ReportColumnOptionsActivity.this, reportId);
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
				//ReportColumnOptionsActivity.this.buildListAdapter();
				ReportColumnOptionsActivity.this.buildListAdapter();
				
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
