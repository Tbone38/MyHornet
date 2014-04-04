package com.treshna.hornet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Report_Types_ListActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_types_list);
    	GetReportTypeData aSyncReport = new GetReportTypeData();
    	aSyncReport.execute(null,null);		
	}
	private void buildListAdapter() {
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();
			
			listView.setOnItemClickListener( new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					TextView reportNameId = (TextView) findViewById (R.id.report_types_and_names_id);
					
				}  
				
				
			} );
			ListAdapter listAdapter = new SimpleAdapter(Report_Types_ListActivity.this,this.resultMapList,
					R.layout.reporttype_row,
					new String[] {"id","name","view_name", "reportgroup"},
					new int[] {R.id.report_type_Id, R.id.report_type_name, R.id.report_type_viewname, R.id.report_type_group});
			this.setListAdapter(listAdapter);
	}
		
		
	}
	
	
	
	
	//Nested class for retrieving the report_types data from the central database
	private class GetReportTypeData extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private ResultSet result = null;
		

		
		public GetReportTypeData () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(Report_Types_ListActivity.this, "Retrieving..", 
					 "Retrieving Key Performance Indicators...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportTypes(Report_Types_ListActivity.this);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			String id, name, viewName, reportGroup;
			if (success) {
				
				/*Code check the data is present
				System.out.println("\nReport-Type_Data");
				
				System.out.println("Result List Size: " + resultMapList.size());
				
				for (HashMap<String,String> resultMap: resultMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				}*/
				
				//Calling the method from the owning activty to render the list view
				Report_Types_ListActivity.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(Report_Types_ListActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
}
