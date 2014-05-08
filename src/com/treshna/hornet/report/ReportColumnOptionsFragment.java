package com.treshna.hornet.report;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.Inflater;

import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.network.HornetDBService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ReportColumnOptionsFragment extends ListFragment {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	//private HashMap<String,String> fieldsMap  = null;
	private int[] selectedColumns = null;
	private LayoutInflater mInflater = null;
	private View view = null;
	private long startDate = 0;
	private long endDate = 0;
	private int reportId = 0;
	private ArrayList<CheckBox> colCheckBoxes = null;
	private String reportName = null;
	private String reportFunctionName = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mInflater = inflater;
		view = mInflater.inflate(R.layout.report_column_options, null);
		colCheckBoxes = new ArrayList<CheckBox>();
		Bundle fragmentData = getArguments();
		startDate  =  fragmentData.getLong("start_date", 0);
		endDate  =  fragmentData.getLong("end_date", 0);
		reportId =  fragmentData.getInt("report_id", 0);
		reportFunctionName = fragmentData.getString("report_function_name");
		reportName = fragmentData.getString("report_name");
		
		Button createBtn = (Button) view.findViewById(R.id.btnCreateReport);

	    createBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			     getCheckedColumns();
				 loadMainReportActivity();
			}
		});
	    
	    this.getReportColumnOptions();
	    
		return view;
		
	}
	
	private void buildListAdapter() {
		if (this.resultMapList.size() > 0){
			ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(getActivity(),R.layout.report_column_options_row,
					this.resultMapList){

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {
						//Dynamically binding column names to textView text
						LayoutInflater inflater = LayoutInflater.from(getContext());
						HashMap<String,String> dataRow =  this.getItem(position);
						int index = 0;
						TextView columnName	 = null;
						CheckBox columnBox  = null;
						convertView  = inflater.inflate(R.layout.report_column_options_row, null);			
						
						for (Entry<String,String> row : dataRow.entrySet()){							
							//Attaching a tag with column name value to the checkBox

							
							if (row.getKey().toString().compareTo("report_field_id")== 0){
								columnBox = (CheckBox) convertView.findViewById(R.id.column_checkBox);
								columnBox.setTag(row.getValue());	
								colCheckBoxes.add(columnBox);
							
							}			
							
							if (row.getKey().toString().compareTo("column_name")== 0){
								columnName	 = (TextView) convertView.findViewById(R.id.report_column_name);							
								columnName.setText(row.getValue());																			 																
								
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
		Intent mainReportIntent = new Intent(getActivity(), ReportMainActivity.class);
		mainReportIntent.putExtra("report_id", reportId);
		mainReportIntent.putExtra("report_name", reportName);
		mainReportIntent.putExtra("calling_activity", "column_options");
		mainReportIntent.putExtra("report_function_name", reportFunctionName);
		mainReportIntent.putExtra("selected_column_ids", selectedColumns);
		mainReportIntent.putExtra("start_date", startDate);
		mainReportIntent.putExtra("end_date", endDate);
		startActivity(mainReportIntent);
	}
	
	private void getCheckedColumns () {
	  this.selectedColumns = new int[(colCheckBoxes.size())];
	  int columnIndex = 0;
  
	  for (CheckBox checkBox : colCheckBoxes){
		  
		  	if (checkBox.isChecked())
			    selectedColumns[columnIndex] = Integer.parseInt(checkBox.getTag().toString());
		  		columnIndex += 1;	  
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
			progress = ProgressDialog.show(getActivity(), "Retrieving..", 
					 "Retrieving Report Column Names...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportColumnsByReportId(getActivity(), reportId);
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
				ReportColumnOptionsFragment.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
}
