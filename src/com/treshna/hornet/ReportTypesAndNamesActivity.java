package com.treshna.hornet;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ReportTypesAndNamesActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	private GetReportTypesAndNamesNameData syncTypesAndNames = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_types_and_names_list);
		getNamesData();
	}
	
	public GetReportTypesAndNamesNameData getSyncTypesAndNames() {
		return syncTypesAndNames;
	}

	public ArrayList<HashMap<String, String>> getResultMapList() {
		return resultMapList;
	}
	
	public void getNamesData () {
		syncTypesAndNames = new GetReportTypesAndNamesNameData();
		//syncTypesAndNames.execute(null,null);
	}

	private void buildListAdapter() {
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();
			
			listView.setOnItemClickListener( new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					TextView reportNameId = (TextView) view.findViewById (R.id.report_types_and_names_id);
					TextView reportName = (TextView) view.findViewById (R.id.report_types_and_names_name);
					TextView reportFunctionName = (TextView) view.findViewById (R.id.report_types_and_names_function_name);
					TextView istype = (TextView) view.findViewById (R.id.report_types_and_names_istype);
					
					//Checking that a report name was clicked on (not a type)
					if (istype.getText().equals("f")){
						System.out.println("Report ID: " + reportNameId + " Report Name: " + reportName.getText() + "\nFunction Name: " 
								+ reportFunctionName.getText() );
						Intent intent = new Intent(view.getContext(),ReportDateOptionsActivity.class);
						intent.putExtra("report_id", reportNameId.getText().toString());
						intent.putExtra("report_name", reportName.getText().toString());
						intent.putExtra("report_function_name", reportFunctionName.getText().toString());
						startActivity(intent);				
					}
					
				}  
				
				
			} );
			ListAdapter listAdapter = new SimpleAdapter(ReportTypesAndNamesActivity.this,this.resultMapList,
					R.layout.report_types_and_names_row,					
					new String[] {"id","name", "function_name", "istype"},
					new int[] {R.id.report_types_and_names_id, R.id.report_types_and_names_name, R.id.report_types_and_names_function_name, R.id.report_types_and_names_istype})
			{
					
					
			};
					
			this.setListAdapter(listAdapter);
	  }
	}
	public class GetReportTypesAndNamesNameData extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private ResultSet result = null;
		
		

		
		public GetReportTypesAndNamesNameData () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportTypesAndNamesActivity.this, "Retrieving..", 
					 "Retrieving Report Types And Names List...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportNamesAndTypes(ReportTypesAndNamesActivity.this);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				
				/*System.out.println("\nReport_Type_And_Name_Data");
				
				System.out.println("Result List Size: " + resultMapList.size());
				
				for (HashMap<String,String> resultMap: resultMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}*/
				//Calls back to the owning activity to build the adapter
				ReportTypesAndNamesActivity.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportTypesAndNamesActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	

}
