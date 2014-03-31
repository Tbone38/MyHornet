package com.treshna.hornet;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ReportListingActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_types_and_names_list);
		//Call the thread for dowloading data from central db
		GetReportTypesAndNamesNameData syncTypesAndNames = new GetReportTypesAndNamesNameData();
		syncTypesAndNames.execute(null,null);
		
	}
	
	private void buildListAdapter() {
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();
			
			listView.setOnItemClickListener( new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					TextView reportName = (TextView) view.findViewById(5);
					
					//Checking that a report name was clicked on (not a type)
					/*if (istype.getText().equals("f")){
						System.out.println("Report ID: " + reportNameId + " Report Name: " + reportName.getText() + "\nFunction Name: " 
								+ reportFunctionName.getText() );
						Intent intent = new Intent(view.getContext(),ReportDateOptionsActivity.class);
						intent.putExtra("report_id", reportNameId.getText().toString());
						intent.putExtra("report_name", reportName.getText().toString());
						intent.putExtra("report_function_name", reportFunctionName.getText().toString());
						startActivity(intent);				
					}*/
					
				}  
				
				
			} );
			
			ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(ReportListingActivity.this,
					R.layout.report_types_and_names_row, this.resultMapList)					
			
			{
				@TargetApi(Build.VERSION_CODES.HONEYCOMB)
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
				//Dynamically binding column names to textView text
				TextView textView  = null;
				LinearLayout linLayout = new LinearLayout(ReportListingActivity.this);
				linLayout.setOrientation(LinearLayout.HORIZONTAL);
				AbsListView.LayoutParams listLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
				linLayout.setLayoutParams(listLayoutParams);
				HashMap<String,String> dataRow = this.getItem(position);
				int idIndex = 1;
				for (Entry<String,String> col : dataRow.entrySet()){
						if (col.getKey().compareTo("name")==0){
							
						  	layoutParams = new LinearLayout.LayoutParams( 0,LayoutParams.WRAP_CONTENT,1);
							//Dynamically generate text views for each column name..
							textView =  new TextView(ReportListingActivity.this);
							layoutParams.setMargins(5, 0, 0, 0);
							textView.setLayoutParams(layoutParams);
							textView.setId(5);
							textView.setTag(dataRow);
							textView.setText(col.getValue());
							linLayout.addView(textView);
							idIndex += 1;
						}
				}	
					
					return linLayout;
				}
					
					
			};
					
			this.setListAdapter(listAdapter);
	  }
	}
	private class GetReportTypesAndNamesNameData extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private ResultSet result = null;
		
		

		
		public GetReportTypesAndNamesNameData () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportListingActivity.this, "Retrieving..", 
					 "Retrieving Report Types And Names List...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportNamesAndTypes(ReportListingActivity.this);
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
				ReportListingActivity.this.buildListAdapter();
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportListingActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	

}
