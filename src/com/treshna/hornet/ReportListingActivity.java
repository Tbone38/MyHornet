package com.treshna.hornet;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.R.attr;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
		
		TextView titleView = (TextView) this.findViewById(R.id.reports_listing_title);
		titleView.setText("Reports");
		
		if (resultMapList.size() > 0){
			ListView listView = this.getListView();
			
			listView.setOnItemClickListener( new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					TextView reportName = (TextView) view.findViewById(5);		
					boolean isName = false;
					HashMap<String,String> selectedRowData = (HashMap<String, String>) reportName.getTag();
					
					//Checking that a report name was clicked on (not a type)
					 if ((selectedRowData.get("istype").compareTo("f")== 0)){
							Intent intent = new Intent(view.getContext(),ReportDateOptionsActivity.class);
							intent.putExtra("report_id", Integer.parseInt(selectedRowData.get("id")));
							intent.putExtra("report_name" , selectedRowData.get("name").toString());
							intent.putExtra("report_function_name",selectedRowData.get("function_name").toString());
							startActivity(intent);
					 }

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
				RelativeLayout linLayout = new RelativeLayout(ReportListingActivity.this);
				//linLayout.setOrientation(LinearLayout.HORIZONTAL);
				AbsListView.LayoutParams listLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
				linLayout.setLayoutParams(listLayoutParams);
				linLayout.setPadding(15, 0, 0, 0);
				HashMap<String,String> dataRow = this.getItem(position);
				for (Entry<String,String> col : dataRow.entrySet()){
						
						if (col.getKey().compareTo("name")== 0){
							
						  	layoutParams = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
						  	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
						  	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
							//Dynamically generate text views for each column name..
							textView =  new TextView(ReportListingActivity.this);
							textView.setId(5);
							//To embolden the types listings...
							if (dataRow.get("istype").compareTo("t")== 0){
								textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
							} else {
								//To highlight names as clickable..
								textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
								textView.setTypeface(null, Typeface.BOLD);
								layoutParams.setMargins(35, 0, 0, 0);
							}
							textView.setLayoutParams(layoutParams);
							textView.setTag(dataRow);
							textView.setText(col.getValue());
							linLayout.addView(textView);
						}
						
						if (col.getKey().compareTo("description")== 0){
						  	layoutParams = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
						  	layoutParams.addRule(RelativeLayout.BELOW, 5);
						  	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
							//Dynamically generate text views for each column name..
							textView =  new TextView(ReportListingActivity.this);
							layoutParams.setMargins(35, 0, 0, 0);
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
