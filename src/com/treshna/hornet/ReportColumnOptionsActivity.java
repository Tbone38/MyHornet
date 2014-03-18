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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class ReportColumnOptionsActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_column_options);
		//Fetching data passed through from the preceding Activities..
		Intent intent = this.getIntent();
		Date startDate  =  new Date(intent.getLongExtra("start_date", 0));
		Date endDate  =  new Date(intent.getLongExtra("end_date", 0));
		String reportId = intent.getStringExtra("report_id");
		String reportName = intent.getStringExtra("report_name");
		String functionName = intent.getStringExtra("report_function_name");
		Button createBtn = (Button) this.findViewById(R.id.btnCreateReport);
		//removes the parameter refs from the function name
		functionName = functionName.substring(0, functionName.indexOf('('));
	    System.out.println("Function: " + functionName);
	    createBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 getCheckedColumns();
			}
		});
	    this.getReportData(functionName, startDate , endDate);
	}
	
	private void buildListAdapter() {
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();

			ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(ReportColumnOptionsActivity.this,R.layout.report_column_options_row,
					this.getResultColumnNames()){

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {
						//Dynamically binding column names to textView text
						LayoutInflater inflater = LayoutInflater.from(getContext());
						HashMap<String,String> dataRow =  this.getItem(position);
						for (Entry<String,String> row : dataRow.entrySet()){
							convertView  = inflater.inflate(R.layout.report_column_options_row, null);
							TextView columnName	 = (TextView) convertView.findViewById(R.id.report_column_name);
							CheckBox columnBox = (CheckBox) convertView.findViewById(R.id.column_checkBox);
							//Attaching a tag with column name value to the checkBox
							columnBox.setTag(row.getValue());
							columnName.setText(row.getValue());
						}	
							
							return convertView;
						}
						
			        };
			this.setListAdapter(listAdapter);
	  }
	}
	
	protected void getReportData (String functionName, Date startDate, Date endDate) {
		
		GetReportDataByDateRange syncNames = new GetReportDataByDateRange(functionName, startDate , endDate);
		syncNames.execute(null,null);
		
	}
	
	private ArrayList<HashMap<String,String>> getResultColumnNames () {
		HashMap<String,String> rowMap = resultMapList.get(0);
		HashMap<String,String> colName = null;
		ArrayList<HashMap<String,String>> colNamesList = new ArrayList<HashMap<String,String>>();
		
		for (Entry<String, String> row : rowMap.entrySet()){
			System.out.println("Column Name: " + row.getKey());
			colName = new HashMap<String,String>();
			colName.put("column_name", row.getKey());
			colNamesList.add(colName);
		}
		return colNamesList;
	}
	
	private void getCheckedColumns () {
	  ArrayList<String> checkedColumns = null;
	  ListView listView = this.getListView();
	  View view = null;
	  CheckBox checkBox = null;
	  for (int i = 0; i < listView.getCount(); i++){
		  	view = listView.getChildAt(i);
		  	checkBox = (CheckBox) view.findViewById(R.id.column_checkBox);
		  	if (checkBox.isChecked())
		  		System.out.println(checkBox.getTag());
		  	
	  }
	    	  
	}
	
	
	protected class GetReportDataByDateRange extends AsyncTask<String, Integer, Boolean> {
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
