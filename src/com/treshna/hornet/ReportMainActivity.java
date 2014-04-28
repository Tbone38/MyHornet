package com.treshna.hornet;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import android.R.attr;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class ReportMainActivity extends ListActivity {
	private ArrayList<HashMap<String,String>> resultMapList = null;
	private ArrayList<HashMap<String,String>> columnsMapList =  null;
	private ArrayList<HashMap<String,String>> joiningTablesMapList =  null;
	private ArrayList<HashMap<String,String>> emailsMapList =  null;
	private String reportName = null;
	private String reportFunctionName = null;
	private String queryFunctionParamsCut = null;
	private String numValueRegex = "(^\\$?[0-9]+\\.?[0-9]+)|([0-9]+)";
	private String callingActivity = null;
	private String firstFilter = null;
	private String secondFilter = null;
	private Button btnEmailCSV = null;
	private String finalQuery = null;
	private Date startDate = null;
	private Date endDate = null;
	private Button btnEmail = null;
	private boolean stripe = true;
	private int[] selectedColumnIds = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_main_list);
		//Fetching data passed through from the preceding Activities..
		Intent intent = this.getIntent();
		startDate  =  new Date(intent.getLongExtra("start_date", 0));
		endDate  =  new Date(intent.getLongExtra("end_date", 0));
		btnEmailCSV = (Button) findViewById(R.id.btnEmailReportCSV);
		int reportId = intent.getIntExtra("report_id",0);
		callingActivity = intent.getStringExtra("calling_activity");
		if (callingActivity.compareTo("column_options")==  0) {
			selectedColumnIds = intent.getIntArrayExtra("selected_column_ids");
		}
		firstFilter = intent.getStringExtra("first_filter_field");
		secondFilter = intent.getStringExtra("second_filter_field");
		reportName = intent.getStringExtra("report_name");
		reportFunctionName = intent.getStringExtra("report_function_name");
		queryFunctionParamsCut = reportFunctionName.substring(0,reportFunctionName.indexOf('('));
		btnEmail = (Button) findViewById(R.id.btnEmail);
		btnEmail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getEmailAddressesByIds();
			}
			
		});
		btnEmailCSV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				emailCSVAsAttachment();	
			}
			
		});		
		this.getColumnData(reportId);
	    
	}
	
	private Integer[] getIdsFromReportData () {
		Integer[] reportIds = null;
	
		reportIds = new Integer[resultMapList.size()];
		int count = 0;
		for (HashMap<String,String> columnsMap : resultMapList ){
			if (columnsMap.containsKey("Member ID")){
				if (columnsMap.get("Member ID")!= null){
					reportIds[count] = Integer.parseInt(columnsMap.get("Member ID"));
				}
			} 
			else if (columnsMap.containsKey("MemberID")){
				reportIds[count] = Integer.parseInt(columnsMap.get("MemberID")); 	
			}
			else if (columnsMap.containsKey("Enquiry Id")){
				reportIds[count] = Integer.parseInt(columnsMap.get("Enquiry Id")); 	
			}	
			count += 1;
		}
	   return reportIds;
	}
	
	private boolean columnsFieldsContainsTable(String tableName) {
		
		for (HashMap<String,String> columnsMap : columnsMapList ){
			if (columnsMap.get("field").toString().contains(tableName + ".")){
				return true;
			}
		}
		return false;
	}
	
	private boolean columnsContainName(String columnName) {
		
		for (HashMap<String,String> columnsMap : columnsMapList ){
			if (columnsMap.get("column_name").toString().compareTo("Member ID") == 0){
				return true;
			}
		}
		return false;
	}
	
	
	private void  getEmailAddressesByIds() {
		String tableName = null;
		
		if (columnsContainName("Member ID") || columnsFieldsContainsTable("member")) {
			tableName = "member";
		} 
		else if (columnsFieldsContainsTable("enquiry")) {
			tableName = "enquiry";
		}
		
		Integer[] ids = getIdsFromReportData();
    	//System.out.println("Ids Length: " +ids.length);
    	//System.out.println("Table Name: " +tableName);
		GetEmailAddressesByIds emailsSync = new GetEmailAddressesByIds(ids, tableName);
		emailsSync.execute();
		
	}
	
	private String[] getClientPrimaryEmail() {
		String[] clientPrimaryEmail = new String[1];
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(getBaseContext()).getAccounts();
		for (Account account : accounts) {
		    if (emailPattern.matcher(account.name).matches()) {
		    	clientPrimaryEmail[0] = account.name;
		    }
		}
		
		return clientPrimaryEmail;
	}
	
	private String[] getEmailsAddressesAsArray() {
		String[] addresses = null;
		ArrayList<HashMap<String,String>> cleanedEmailsMapList = new  ArrayList<HashMap<String,String>>();
		//Removing nulls and empty strings..
		for (HashMap<String,String > emailMap : emailsMapList) {
			 if (emailMap.get("email") != null && emailMap.get("email").compareTo("")!= 0 && (!emailMap.get("email").isEmpty()) && emailMap.get("email").contains("@")) {
				 cleanedEmailsMapList.add(emailMap);
			 }
		}
		addresses = new String[cleanedEmailsMapList.size()];
		int index = 0;	
		for (HashMap<String,String > emailMap : cleanedEmailsMapList) {
			
				addresses[index] = 	emailMap.get("email");
				index += 1;
			
		}
		return addresses;	
	}
	
	
	private String createCSVFromReportData () {
		
		String SDCardRoot = Environment.getExternalStorageDirectory().getPath();
		PrintWriter writer = null;
		String fileName = reportName.replace(" ", "_") + ".csv";
		String row = "";
		try {
			writer = new PrintWriter(SDCardRoot + "/" + fileName, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String headerRow = "";
		HashMap<String,String>  firstRow =  resultMapList.get(0);
		for (Entry column: firstRow.entrySet()){
			headerRow += column.getKey() + ",";
		}
		//removing the last comma 
		headerRow = headerRow.substring(0, headerRow.length() - 1);
		writer.println(headerRow);
		
		for (HashMap<String,String> resultMap : resultMapList){
			row = "";
			for (Entry column: resultMap.entrySet()){
				//Enclosing names in quotes..
				if (column.getKey().toString().compareTo("Name") == 0){
					row += "\"" + column.getValue()  + "\"" + ",";  
				} else {
					row += column.getValue() + ",";
				}
			}
			//removing the last comma 
			row = row.substring(0, row.length() - 1);
			writer.println(row);
		}
		 writer.close();
		 return fileName;
	}
	
	private void emailCSVAsAttachment () {
		String fileName  = createCSVFromReportData();
		//Create good filenames...
		//fileName = fileName.replace(" ","_");
		EmailSender emailSender = new EmailSender(ReportMainActivity.this, getClientPrimaryEmail(),null,fileName);
		emailSender.attachFile(fileName);
		emailSender.sendToClientEmail();
	}
	
	
	private void buildQuery() {
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ");
		boolean isSelected = true;
		//boolean containsMemberColumn = false;
		boolean isMemberIdAdded = false;
		boolean isEnquiryIdAdded = false;
		for (HashMap<String,String> columnsMap : columnsMapList ) {
			//Check if coming from column options..
			if (callingActivity.compareTo("column_options")== 0) {
				isSelected = false;
				if (this.isColumnSelected(Integer.parseInt(columnsMap.get("column_id")))) {
					isSelected = true;				}
			}
			
			for (Map.Entry<String,String> field: columnsMap.entrySet()) {
					//System.out.println(field.getKey() + " : " + field.getValue());
				if (isSelected) {
					//Adding the member.id column where other columns join the member table
					if (columnsFieldsContainsTable("member") && !isMemberIdAdded) {
						queryBuilder.append("member.id AS \"MemberID\", ");
						isMemberIdAdded = true;
					}
					//Adding the enquiry.id column where other columns join the enquiry table
					if (columnsFieldsContainsTable("enquiry") && !isEnquiryIdAdded) {
						queryBuilder.append("enquiry.enquiry_id AS \"Enquiry Id\", ");
						isEnquiryIdAdded = true;
					}
					
					if (field.getKey().toString().compareTo("field")== 0) {
							queryBuilder.append(field.getValue());
							queryBuilder.append(" AS ");
							
					}
					if (field.getKey().toString().compareTo("column_name")== 0) {
								queryBuilder.append("\"" + field.getValue() + "\"");
								queryBuilder.append(", ");
					}				

			   }
				
	        }							
		 }

			//remove the last comma...
			queryBuilder.replace(queryBuilder.length() - 2, queryBuilder.length(), " ");
			queryBuilder.append(" FROM ");
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-M-dd");
			String paramsWithBrackets = this.reportFunctionName.substring(reportFunctionName.indexOf('('),reportFunctionName.length());
			String reportFunctionWithDateParams = "";
			if (paramsWithBrackets.length() > 2) {
				reportFunctionWithDateParams =  this.queryFunctionParamsCut + "(" + "'" + dateFormatter.format(this.startDate) + "'" +"::Date"  + "," + "'" + dateFormatter.format(this.endDate) + "'" + "::Date" +  ')';
			} else {
				reportFunctionWithDateParams =  this.queryFunctionParamsCut + "()";
			}
			queryBuilder.append(reportFunctionWithDateParams);
			queryBuilder.append("As fun ");
			
			
			for (HashMap<String,String> joinsMap : joiningTablesMapList ){
				for (Map.Entry<String,String> field: joinsMap.entrySet()){
					queryBuilder.append(field.getValue());
					queryBuilder.append(' ');	
				}
			}
			
			if (firstFilter != null && !firstFilter.contains("'All'")) {
				queryBuilder.append(" WHERE ");
				queryBuilder.append(' ');
				queryBuilder.append(firstFilter);
				queryBuilder.append(' ');
				if (secondFilter != null && !secondFilter.contains("'All'")) {
					queryBuilder.append(" AND ");
					queryBuilder.append(' ');
					queryBuilder.append(secondFilter);
					queryBuilder.append(' ');
				}
			}
				
			queryBuilder.append(";");
			System.out.println(queryBuilder.toString());
			finalQuery = queryBuilder.toString();
			this.getReportData(finalQuery);
		
		/*
		//remove the last comma...
		queryBuilder.replace(queryBuilder.length() - 2, queryBuilder.length(), " ");
		queryBuilder.append(mainQuery);
		queryBuilder.append("ORDER BY ");
		//Adding the select columns to order by..
		for (Map.Entry<String,String> field: fieldsMap.entrySet()){
			  
			if (this.isColumnSelected(field.getKey().toString())){
				queryBuilder.append(field.getValue());
				queryBuilder.append(',');
				queryBuilder.append(' ');
			}
		}*/

}	

		
	private boolean isColumnSelected(int columnId){
		
		for (int i = 0; i < this.selectedColumnIds.length; i++){
			if (selectedColumnIds[i] != 0){
				 if (selectedColumnIds[i]== columnId){
					return true;
				 }
			}
		}
		return false;
	}

	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void buildListAdapter() {
	
			ListView listView = getListView();
			listView.setOnItemClickListener(new OnItemClickListener () {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					TextView idView = (TextView) view.findViewById(2);
					if (idView != null){
						ArrayList<String> tag = new ArrayList<String>();
						tag.add(idView.getText().toString());
						tag.add(null);
						Intent intent = new Intent(ReportMainActivity.this, EmptyActivity.class);
						intent.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MemberDetails.getKey());
						intent.putStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID, tag);
						ReportMainActivity.this.startActivity(intent);
					}
				}
				
			});
		if ((columnsContainName("Member ID") || columnsFieldsContainsTable("member") || columnsFieldsContainsTable("enquiry")) && resultMapList.size() > 0) {
			btnEmail.setVisibility(View.VISIBLE);
		}
		if (resultMapList.size() > 0) {
			btnEmailCSV.setVisibility(View.VISIBLE);
		}
		
	    buildColumnHeaders();
		ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(ReportMainActivity.this,R.layout.report_main_row,
				this.resultMapList){


					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
					//Dynamically binding column names to textView text
					TextView textView  = null;
					
					LinearLayout linLayout = new LinearLayout(ReportMainActivity.this);
					//Adding zebra striping on alternate rows
					if (position % 2 == 0){
						linLayout.setBackgroundColor(getResources().getColor(R.color.booking_resource_background));
					}
					ReportMainActivity.this.stripe = false;
					linLayout.setOrientation(LinearLayout.HORIZONTAL);
					AbsListView.LayoutParams listLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
					linLayout.setLayoutParams(listLayoutParams);
					HashMap<String,String> dataRow = this.getItem(position);
					removeSpacesFromPhoneNumbers(dataRow);
					int columnIndex = 0;
					for (Entry<String,String> col : dataRow.entrySet()){
						if (!isColumnAllNull(col.getKey().toString())) {
							
						  	layoutParams = new LinearLayout.LayoutParams( 0,LayoutParams.WRAP_CONTENT,3);
							//Dynamically generate text views for each column name..
							textView =  new TextView(ReportMainActivity.this);
							String field = col.getKey().toString();
							if (field.compareTo("Member ID")== 0 || field.compareTo("MemberID")== 0) {
								textView.setId(2);
							}
							if (field.compareTo("MemberID")== 0){
								textView.setVisibility(android.view.View.GONE);
							}
							if (col.getValue() != null && col.getValue().toString().matches(numValueRegex)) {
								textView.setGravity(Gravity.RIGHT);										
								if ((getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
									textView.setPadding(0, 0, 60, 0);
								} else {
									textView.setPadding(0, 0, 10, 0);
								}										
							}
							layoutParams.setMargins(10, 0, 0, 0);									
							textView.setLayoutParams(layoutParams);
							textView.setText(col.getValue());
							if (columnIndex % 2 == 0){
								textView.setBackgroundColor(getResources().getColor(R.color.report_column_pale_red));
							}
							if (textView.getVisibility() == View.VISIBLE){
								columnIndex++;
							}
							linLayout.addView(textView);									
						}
					}	
						
						return linLayout;
					}
												
		        };
		this.setListAdapter(listAdapter);
	  }
	
 private boolean isAnyRowAllNums(String colName){
		for (HashMap<String,String> dataRow: resultMapList){
			if (dataRow.get(colName) != null)
				if (dataRow.get(colName).matches(numValueRegex)){
					return true;
				}
		}
	  return false;
 }
 
 private void removeSpacesFromPhoneNumbers(HashMap<String,String> dataRow){
		//Remove spaces from phone numbers..
		if (dataRow.containsKey("Mobile") && dataRow.get("Mobile")!= null) {
			if (dataRow.get("Mobile").toString().contains(" ")) {
				dataRow.put("Mobile", dataRow.get("Mobile").toString().replace(" ", ""));
			}
		}
		if (dataRow.containsKey("Home Phone") && dataRow.get("Home Phone")!= null) {
			if (dataRow.get("Home Phone").toString().contains(" ")) {
				dataRow.put("Home Phone", dataRow.get("Home Phone").toString().replace(" ", ""));
			}
		} 
 }
	
	
 private void setOrientation(HashMap<String,String> dataRow) {
	 
		//Forcing landscape view for reports with greater than 5 columns..
		int maxProtraitColumns = 5;
		int maxSelectedPortraitColumns = maxProtraitColumns;
		//Compensating for a hidden column in the main data set..
		if (dataRow.containsKey("MemberID")){
			maxProtraitColumns += 1;
		}
		
		//Launched from column options UI..
		if (selectedColumnIds != null){
			
			if (dataRow.size() > maxProtraitColumns && selectedColumnIds.length > maxSelectedPortraitColumns){
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
		} else {
			
			//Launched from date options UI..
			if (dataRow.size() > maxProtraitColumns){
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
		}
		
	}
	
private void buildColumnHeaders() {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
			LinearLayout reportColumnHeadingLayout = (LinearLayout) this.findViewById(R.id.report_list_headings);
			TextView reportNameTextView = (TextView) findViewById(R.id.report_main_title);
			reportNameTextView.setText(reportName);
			
		if (resultMapList.size() > 0){
			//Building title header row...
			HashMap<String,String> dataRow = resultMapList.get(0);
			
			//Forcing landscape view for reports with greater than 5 columns..
			setOrientation(dataRow);
			
			//Building column header row...

			for (Entry<String,String> row : dataRow.entrySet()){
						
				 if (!isColumnAllNull(row.getKey().toString())) {
					 
					 reportColumnHeadingLayout.addView(buildColumnHeaderTextView(row.getKey(), Gravity.NO_GRAVITY, 16));
				}
			}
			
		} else {
			
			reportColumnHeadingLayout.addView(buildColumnHeaderTextView("No Data Available",  Gravity.CENTER, 22));
			
		}
	}
	
	
	private TextView buildColumnHeaderTextView(String contentString, int layoutGravity, int textSize) {
		TextView textView  = null;
		LinearLayout.LayoutParams layoutParams = null;
		textView =  new TextView(ReportMainActivity.this);		
		layoutParams = new LinearLayout.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1);
		//Right aligning columns with all numeric data..
		if (isAnyRowAllNums(contentString)){
			textView.setGravity(Gravity.RIGHT);
			if ((getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
				textView.setPadding(0, 0, 50, 0);
			} else {
				textView.setPadding(0, 0, 10, 0);									
			}
		}
		//Centres the no data message
		if (layoutGravity == Gravity.CENTER){
			textView.setGravity(layoutGravity);
		}
		if (contentString.compareTo("MemberID")==0){
			textView.setVisibility(android.view.View.GONE);
		}
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		layoutParams.setMargins(10, 0, 5, 0);
		textView.setLayoutParams(layoutParams);
		textView.setText(contentString);
		return textView;
	}
	
	private boolean columnsContainsTableAndField(String tableAndFieldName){
		
		for (HashMap<String,String> columnsMap : columnsMapList ){
			if (columnsMap.get("field").toString().contains(tableAndFieldName)){
				return true;
			}
		}
		return false;
		
	}
	
	private boolean isColumnAllNull(String colName) {
		HashMap<String,Integer> colNullCount = new HashMap<String,Integer>();
		 //Bug fix blocking of all collumns with a single row of data.
		 if (resultMapList.size() != 1) {
			for (HashMap<String,String> dataRow: resultMapList){
						
				for (Entry<String,String> col : dataRow.entrySet()){
		
					if (!colNullCount.containsKey(col.getKey())){
						colNullCount.put(col.getKey(),0); 
					}
					
					else
						
					{
						if (col.getValue()== null || col.getValue().isEmpty() ){
							colNullCount.put(col.getKey(),colNullCount.get(col.getKey() )+ 1 );
						}																										
					}														
				}																
			 }
			return colNullCount.get(colName) == resultMapList.size()-1;
		  }
		 return false; 
	}
	
	private void getReportData (String finalQuery) {
		
		GetReportDataByDateRange syncNames = new GetReportDataByDateRange(finalQuery);	
		syncNames.execute(null,null);
		
	}
	
	private void getColumnData (int reportId) {
		
		GetReportColumnsFieldsByReportId syncColumns  = new GetReportColumnsFieldsByReportId(reportId);	
		syncColumns.execute(null,null);
		
	}
	
	private void getJoiningTablesData (String functionName) {
		
		GetJoiningTablesByFunctionName syncColumns  = new GetJoiningTablesByFunctionName(functionName);	
		syncColumns.execute(null,null);
		
	}
	
	

	protected class GetReportDataByDateRange extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog progress;
		private HornetDBService sync;
		private String finalQuery = null;
		
	
		public GetReportDataByDateRange (String finalQuery) {
			sync = new HornetDBService();
			this.finalQuery = finalQuery;

		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Date By Date Range...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			resultMapList = sync.getReportDataByDateRange(ReportMainActivity.this,finalQuery);
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
	
	protected class GetReportColumnsFieldsByReportId extends AsyncTask<String, Integer, Boolean> {
		protected ProgressDialog progress;
		protected HornetDBService sync;
		private int reportId = 0;
		
		public GetReportColumnsFieldsByReportId () {
			sync = new HornetDBService();

		}
		
		
		public GetReportColumnsFieldsByReportId (int reportId) {
			sync = new HornetDBService();
			this.reportId = reportId;

		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Column Data...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			columnsMapList = sync.getReportColumnsFieldsByReportId(ReportMainActivity.this,reportId);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				//Calls back to the owning activity to call the thread to retrieve the joining tables
				ReportMainActivity.this.getJoiningTablesData(reportFunctionName);
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportMainActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	protected class GetEmailAddressesByIds extends AsyncTask<String, Integer, Boolean> {
		protected ProgressDialog progress;
		protected HornetDBService sync;
		private Integer[] reportIds = null;
		private String tableName = null;
		
		public GetEmailAddressesByIds() {
			sync = new HornetDBService();
		}
			
		public GetEmailAddressesByIds (Integer[] reportIds, String tableName) {
			sync = new HornetDBService();
			this.reportIds = reportIds;
			this.tableName = tableName;

		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Email Addresses...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			emailsMapList = sync.getEmailAddressesByIds(ReportMainActivity.this, reportIds, tableName);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				String errorMessage = "No valid Emails for Members in this Report";
				//Calls back to the owning activity to call the thread to retrieve the joining tables
				System.out.println("Email List Size: " + emailsMapList.size());
				
				for (HashMap<String,String> resultMap: emailsMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}
				if (getEmailsAddressesAsArray().length > 0){
					EmailSender email = new EmailSender(ReportMainActivity.this, getClientPrimaryEmail(),getEmailsAddressesAsArray(),null);
					email.sendToClientEmail();
				} else {
					Toast message = Toast.makeText(ReportMainActivity.this, errorMessage, Toast.LENGTH_SHORT);
					message.show();
				}
				
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportMainActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	private class GetJoiningTablesByFunctionName extends GetReportColumnsFieldsByReportId {
		private String functionName = null;

		public GetJoiningTablesByFunctionName(String functionName) {
			super();
			this.functionName = functionName;
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportMainActivity.this, "Retrieving..", 
					 "Retrieving Report Joining Table Data...");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			joiningTablesMapList = sync.getJoiningTablesByFunctionName(ReportMainActivity.this, functionName);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				buildQuery();
				
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
		
