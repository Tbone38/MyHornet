package com.treshna.hornet.report;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.Inflater;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.R.id;
import com.treshna.hornet.R.layout;
import com.treshna.hornet.network.HornetDBService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;


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
	private ProgressDialog progress = null;
	private HornetDBService sync = null;
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
			     
			    if (isAnyColumnChecked()) {
			    	
			    	loadMainReportFragment();
			    	
			    } else {
			    	
			    	Toast.makeText(getActivity(), "You must select at least one column", Toast.LENGTH_SHORT).show();
			    }
				 
			}
		});
	    
	    this.getReportColumnOptions();
	    
		return view;
		
	}
	
	private void buildListAdapter() {
		if (this.resultMapList.size() > 0 && getActivity() != null){
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
	
	private void loadMainReportFragment() {
		
		

		//Modified to call report main fragment - 09-05-2014
		Fragment reportMainFragment = new ReportMainFragment();
		Bundle fragmentData = new Bundle();
		fragmentData.putInt("report_id", reportId);
		fragmentData.putString("report_name", reportName);
		fragmentData.putString("calling_activity", "column_options");
		fragmentData.putString("report_function_name", reportFunctionName);
		fragmentData.putIntArray("selected_column_ids", selectedColumns);
		fragmentData.putLong("start_date", startDate);
		fragmentData.putLong("end_date", endDate);
		reportMainFragment.setArguments(fragmentData);
		((MainActivity)getActivity()).changeFragment(reportMainFragment, "reportMainFragment");
		
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
	
	private boolean isAnyColumnChecked() {
		
		for (int i = 0; i < selectedColumns.length; i++) {
			
			if (selectedColumns[i] != 0){
				return true;
			}
		}
		return false;
	}
	
	private void PrintQueryResultData () {
		
		for (HashMap<String,String> resultMap: this.resultMapList){
			for (Entry<String,String> column: resultMap.entrySet()){
				System.out.println(column.getKey()+ " " + column.getValue());
			}
		}
	}
	
	 
	private void displayQueryThreadProgressDialogue(String message) {
		 
			if (getActivity() != null) {
				
				progress = ProgressDialog.show(getActivity(), "Retrieving..", message);
				
			} else {
				
				progress = null;
				
			}
	 }
	 private void dismissThreadDialogue() {
		 
			if (progress != null && progress.isShowing()) {
				
				progress.dismiss();
				
			}
	 }
	 
	 private void showQueryThreadFailedDialogue() {
		 
			if (getActivity() != null) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
		 
	 }
	
	protected class GetReportColumnOptions extends AsyncTask<String, Integer, Boolean> {


		private ResultSet result = null;
		
			
		public GetReportColumnOptions () {
			sync = new HornetDBService();
	
		}
		
		
		protected void onPreExecute() {
			
			displayQueryThreadProgressDialogue("Retrieving Report Column Names...");
			
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			if (progress == null) {
				return false;
			}
			resultMapList = sync.getReportColumnsByReportId(getActivity(), reportId);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			
			dismissThreadDialogue();
			
			if (success) {
	
				buildListAdapter();
				
			} else {
				
				showQueryThreadFailedDialogue();
			}
	    }
	 }
}
