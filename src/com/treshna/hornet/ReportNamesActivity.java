package com.treshna.hornet;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ReportNamesActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_names_list);
		//Call the thread for dowloading data from central db
		GetReportNameData syncNames = new GetReportNameData();
		syncNames.execute(null,null);
		
	}
	
	private void buildListAdapter() {
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();
			
			/*listView.setOnItemClickListener( new setOnItemClickListener(
					
					));;*/
			ListAdapter listAdapter = new SimpleAdapter(ReportNamesActivity.this,this.resultMapList,
					R.layout.report_names_row,
					new String[] {"id","name"},
					new int[] {R.id.report_name_Id,R.id.report_name_name});
			this.setListAdapter(listAdapter);
	  }
	}
	private class GetReportNameData extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private ResultSet result = null;
		
		

		
		public GetReportNameData () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportNamesActivity.this, "Retrieving..", 
					 "Retrieving Report Names List...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportNamesByReportTypeId(ReportNamesActivity.this, 1);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				
				/*System.out.println("\nReport-Type_Data");
				
				System.out.println("Result List Size: " + resultMapList.size());
				
				for (HashMap<String,String> resultMap: resultMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}*/
				//Calls back to the owning activity to build the adapter
				ReportNamesActivity.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportNamesActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	

}
