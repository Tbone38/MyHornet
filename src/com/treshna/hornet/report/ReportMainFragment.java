package com.treshna.hornet.report;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.EmailSender;
import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.member.MemberSlideFragment;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.visitor.VisitorsViewAdapter;


public class ReportMainFragment extends ListFragment {
	
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
	private int[] selectedColumnIds = null;
	private LayoutInflater mInflater = null;
	private View view = null;
	private ProgressDialog progress;
	private HornetDBService sync;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		view = mInflater.inflate(R.layout.report_main_list, null);
		Bundle fragmentData = getArguments();
		startDate  =  new Date(fragmentData.getLong("start_date", 0));
		endDate  =  new Date(fragmentData.getLong("end_date", 0));
		btnEmailCSV = (Button) view.findViewById(R.id.btnEmailReportCSV);
		int reportId = fragmentData.getInt("report_id",0);
		callingActivity = fragmentData.getString("calling_activity");
		if (callingActivity.compareTo("column_options")==  0) {
			selectedColumnIds = fragmentData.getIntArray("selected_column_ids");
		}
		firstFilter = fragmentData.getString("second_filter_field");
		reportName = fragmentData.getString("report_name");
		reportFunctionName = fragmentData.getString("report_function_name");
		queryFunctionParamsCut = reportFunctionName.substring(0,reportFunctionName.indexOf('('));
		btnEmail = (Button) view.findViewById(R.id.btnEmail);
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
		
		return view;
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
		Account[] accounts = AccountManager.get(getActivity().getBaseContext()).getAccounts();
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
		EmailSender emailSender = new EmailSender(getActivity(), getClientPrimaryEmail(),null,fileName);
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
			
			boolean firstFilterAdded = false;
			if (firstFilter != null && !firstFilter.contains("'All'")) {
				queryBuilder.append(" WHERE ");
				queryBuilder.append(' ');
				queryBuilder.append(firstFilter);
				queryBuilder.append(' ');
				firstFilterAdded = true;
			}
			
			if (secondFilter != null && !secondFilter.contains("'All'")) {
				if (firstFilterAdded) {
					queryBuilder.append(" AND ");
				} else {
					queryBuilder.append(" WHERE ");
				}
				queryBuilder.append(' ');
				queryBuilder.append(secondFilter);
				queryBuilder.append(' ');
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
			            //Displaying the member details view upon clicking on a row - on reports with member related data..
						ArrayList<String> tag = new ArrayList<String>();
						String memberId = idView.getText().toString();
						tag.add(memberId);
						tag.add(null);
						Fragment f = new MemberSlideFragment();
						Bundle bdl = new Bundle(1);
				        bdl.putString(Services.Statics.MID, memberId);
						f.setArguments(bdl);
						((MainActivity)getActivity()).changeFragment(f, "memberDetails");

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
	    
		ListAdapter listAdapter = new ArrayAdapter<HashMap<String,String>>(getActivity(),R.layout.report_main_row,
				this.resultMapList){


					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
					//Dynamically binding column names to textView text
					TextView textView  = null;
					
					LinearLayout linLayout = new LinearLayout(getActivity());
					//Adding zebra striping on alternate rows
					if (position % 2 == 0){
						linLayout.setBackgroundColor(getResources().getColor(R.color.booking_resource_background));
					}
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
							textView =  new TextView(getActivity());
							String field = col.getKey().toString();
							if (field.compareTo("Member ID")== 0 || field.compareTo("MemberID")== 0) {
								textView.setId(2);
							}
							if (field.compareTo("MemberID")== 0){
								textView.setVisibility(android.view.View.GONE);
							}
							if (col.getValue() != null && col.getValue().toString().matches(numValueRegex)) {
								textView.setGravity(Gravity.RIGHT);										
								if ((getActivity().getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
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
	
	
 @Override
public void onPause() {
	super.onPause();
	  if (progress != null && progress.isShowing()) {
		  //progress.hide();
		  progress.dismiss();
		  progress = null;
	  }
	 getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
		} else {
			
			//Launched from date options UI..
			if (dataRow.size() > maxProtraitColumns){
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
		}
		
	}
	
private void buildColumnHeaders() {
	
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
			LinearLayout reportColumnHeadingLayout = null;
			TextView reportNameTextView = null;
			reportColumnHeadingLayout =(LinearLayout) view.findViewById(R.id.report_list_headings);
			reportNameTextView = (TextView) view.findViewById(R.id.report_main_title);
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
		textView =  new TextView(getActivity());		
		layoutParams = new LinearLayout.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1);
		//Right aligning columns with all numeric data..
		if (isAnyRowAllNums(contentString)){
			textView.setGravity(Gravity.RIGHT);
			if ((getActivity().getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
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
	 private void showQueryThreadFailedDialogue() {
		 
			if (getActivity() != null) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
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
	 
	 
	

	protected class GetReportDataByDateRange extends AsyncTask<String, Integer, Boolean> {
		
	
		private String finalQuery = null;
		
	
		public GetReportDataByDateRange (String finalQuery) {
			sync = new HornetDBService();
			this.finalQuery = finalQuery;

		}
		
		protected void onPreExecute() {
			
			displayQueryThreadProgressDialogue("Retrieving Report Data By Date Range...");
		}
		
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			if (progress == null) {
				
				return false;
			}
			
			resultMapList = sync.getReportDataByDateRange(getActivity(),finalQuery);
			
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
	
	protected class GetReportColumnsFieldsByReportId extends AsyncTask<String, Integer, Boolean> {
		private int reportId = 0;
		
		
		public GetReportColumnsFieldsByReportId () {
			sync = new HornetDBService();

		}
		
		
		public GetReportColumnsFieldsByReportId (int reportId) {
			sync = new HornetDBService();
			this.reportId = reportId;

		}
		
		protected void onPreExecute() {
			
			displayQueryThreadProgressDialogue("Retrieving Report Column Data...");
	
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			if (progress == null) {
				return false;
			}
			columnsMapList = sync.getReportColumnsFieldsByReportId(getActivity(),reportId);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			
			dismissThreadDialogue();
			
			if (success) {
				
				//Calls back to the owning activity to call the thread to retrieve the joining tables
				ReportMainFragment.this.getJoiningTablesData(reportFunctionName);
				
			} else {
				showQueryThreadFailedDialogue();
			}
	    }
	 }
	
	protected class GetEmailAddressesByIds extends AsyncTask<String, Integer, Boolean> {
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
			
			displayQueryThreadProgressDialogue("Retrieving Email Addresses...");
		}
		
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			if (progress == null) {
				return false;
			}
			emailsMapList = sync.getEmailAddressesByIds(getActivity(), reportIds, tableName);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			
			dismissThreadDialogue();
			
			if (success) {
				
				String errorMessage = "No valid Emails for Members in this Report";

				if (getEmailsAddressesAsArray().length > 0){
					EmailSender email = new EmailSender(getActivity(), getClientPrimaryEmail(),getEmailsAddressesAsArray(),null);
					email.sendToClientEmail();
				} else {
					Toast message = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
					message.show();
				}
				
				
			} else {
				
				showQueryThreadFailedDialogue();
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
			
			displayQueryThreadProgressDialogue("Retrieving Report Joining Table Data...");

		}

		@Override
		protected Boolean doInBackground(String... params) {
			
			if (progress == null) {
				
				return false;
			}
			
			joiningTablesMapList = sync.getJoiningTablesByFunctionName(getActivity(), functionName);
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			
			dismissThreadDialogue();
			
			if (success) {
				
				buildQuery();
				
			} else {
				
				showQueryThreadFailedDialogue();

			}
	      }
		}				
	}
		
