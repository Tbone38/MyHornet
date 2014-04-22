package com.treshna.hornet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class ReportDateOptionsActivity extends FragmentActivity implements DatePickerFragment.DatePickerSelectListener{ 
	private HashMap<String,Object> reportData = new HashMap<String,Object>() ;
	private ArrayList<HashMap<String,String>> reportFiltersMapList = new ArrayList<HashMap<String,String>>();
	private ArrayList<HashMap<String,String>> reportFiltersTableNameList = new ArrayList<HashMap<String,String>>();
	private String reportFilterTableNamesQuery = null;
	private DatePickerFragment startDatePicker = null;
	private Date selectedStartDate = new Date();
	private Date selectedEndDate = new Date();
	private DatePickerFragment endDatePicker = null;
	private TextView startDateText = null;
	private TextView endDateText = null;
	private int reportId = 0;
	private DatePickerFragment datePickerFragment = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report_user_date_options);
		Intent intent = this.getIntent();
		//datePickerFragment = new DatePickerFragment();
		TextView reportNameTxt = (TextView) findViewById(R.id.report_date_options_report_name);
		startDatePicker =  new  DatePickerFragment();
		endDatePicker = new  DatePickerFragment();
		startDateText = (TextView) findViewById(R.id.startDateTxt);
		endDateText = (TextView) findViewById(R.id.endDateTxt);
		setDateTextView(endDateText, selectedEndDate);
		Button createButton =  (Button) findViewById(R.id.btnCreateReport);
		Button columnOptionsButton =  (Button) findViewById(R.id.btnColumnOptions);
		Button btnStartButton = (Button) findViewById(R.id.btnSelectStartDate);
		Button btnEndButton = (Button) findViewById(R.id.btnSelectEndDate);
		reportNameTxt.setText(intent.getStringExtra("report_name").trim());
		reportId = intent.getIntExtra("report_id",0);
		reportData.put("report_name", intent.getStringExtra("report_name"));
		reportData.put("report_function_name", intent.getStringExtra("report_function_name"));
		setUpQuickSelectSpinner();
		getReportFilterFieldsByReportId();
		btnStartButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startDatePicker.show( ReportDateOptionsActivity.this.getSupportFragmentManager(), "Start Date Picker");
				
			}
			
		});
		
		btnEndButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				endDatePicker.show(ReportDateOptionsActivity.this.getSupportFragmentManager(), "End Date Picker");
				
			}
			
		});
		
		
		
		createButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadMainReport();
				
			}
		});
		
		columnOptionsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadColumnOptions();
				
			}
		});
		
		
	}
	
	private void setUpQuickSelectSpinner () {
		
		final Spinner datePresetsSpinner = (Spinner) findViewById(R.id.datePresetsSpinner);
		String[] spinnerOptions = {"Today", "This Month", "Last Month", "Last Two Months", "Last Six Months", "Last Year"};
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.report_date_options_spinner , spinnerOptions);
		datePresetsSpinner.setAdapter(spinnerAdapter);
		datePresetsSpinner.setPrompt("Select Date Presets");
		datePresetsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
					String selectedOption =  (String) datePresetsSpinner.getSelectedItem();
					setSelectedDate(selectedOption);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}

		});
	}
	
	private void setSelectedDate(String selectedOption) {
		
		if (selectedOption.compareTo("Last Two Months")== 0){
			selectedStartDate = resetDateByNumOfMonths ( -2, startDateText);
			
		} else if (selectedOption.compareTo("Last Six Months")== 0){
			
			selectedStartDate = resetDateByNumOfMonths ( -6, startDateText);
			
		} else if (selectedOption.compareTo("Last Year")== 0){
			
			selectedStartDate = resetDateByNumOfMonths ( -12, startDateText);
			
		} else if (selectedOption.compareTo("Last Month")== 0){
			
			selectedStartDate = resetDateByNumOfMonths ( -1, startDateText);
			
		} else if (selectedOption.compareTo("This Month")== 0){
			
			selectedStartDate = resetDateToStartOfCurrentMonth(startDateText);
			
		} else if (selectedOption.compareTo("Today")== 0){
			
			selectedStartDate = setStartDateToToday(startDateText);
		}
	}
	
	
	private Date  resetDateByNumOfMonths ( int numOfMonths, TextView dateDisplayView) {
		
		 Date selectedDate = new Date(); 
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(selectedDate);
		 cal.add(Calendar.MONTH, numOfMonths);
		 selectedDate = cal.getTime();
		 setDateTextView(dateDisplayView, selectedDate);
		 return selectedDate;
		
	}
	
	private Date resetDateToStartOfCurrentMonth(TextView dateDisplayView) {
		
		 Date selectedDate = new Date(); 
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(selectedDate); 
		 cal.add(Calendar.DAY_OF_MONTH, - cal.get(Calendar.DATE) + 1);
		 
		 selectedDate = cal.getTime();
		 setDateTextView(dateDisplayView, selectedDate);
		 return selectedDate;
		
	}
	
	private Date setStartDateToToday(TextView dateDisplayView) {
		
		 Date selectedDate = new Date(); 
		 setDateTextView(dateDisplayView, selectedDate);
		 return selectedDate;
		
	}
	
	
	
	private  TextView setDateTextView (TextView dateTextView, Date selectedDate){
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
		dateTextView.setText(formatter.format(selectedDate));
		return dateTextView;
	}
	
	private Date  dateFromPicker (String selectedDate) {
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		Date date = null;
		try {
			date = format.parse(selectedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Calendar cal = Calendar.getInstance();
	
		return 	date;
	}
	

	private void loadMainReport(){
		
		loadIntent("date_options", ReportMainActivity.class);
	
	}
	
	private void loadColumnOptions() {
		
		loadIntent("column_options", ReportColumnOptionsActivity.class);
	}
	
	private void loadIntent(String callingActivity, Class<?> activity) {
		Log.i("Selected Start Date: ", selectedStartDate+"");
		Log.i("Selected End Date: ", selectedEndDate+"");
		
		reportData.put("start_date",selectedStartDate);
		reportData.put("end_date",  selectedEndDate);
		
		Intent reportMainIntent = new Intent(this.getApplicationContext(), activity);
		reportMainIntent.putExtra("report_id", reportId);
		//Pushing UI and upstream data through to the report column options intent..
		for (Map.Entry<String,Object> param: reportData.entrySet()){
			//Casting the date values to time-stamp to pass through the intent..
				if ((param.getKey().toString().compareTo("start_date") == 0) || (param.getKey().toString().compareTo("end_date") == 0) ){
					reportMainIntent.putExtra(param.getKey().toString(), ((Date) param.getValue()).getTime());
				} else {
					reportMainIntent.putExtra(param.getKey().toString(),  param.getValue().toString());
				}
			
		}
		reportMainIntent.putExtra("calling_activity", callingActivity);     
						
		this.startActivity(reportMainIntent);
	 }

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		if (theDatePicker == startDatePicker){
			 selectedStartDate = this.dateFromPicker(date);
			 this.setDateTextView(startDateText, selectedStartDate);
		}
		else if (theDatePicker == endDatePicker){
			selectedEndDate = this.dateFromPicker(date);
			this.setDateTextView(endDateText, selectedEndDate);
		}
		
	}
	
	private void getReportFilterFieldsByReportId () {
		GetReportFilterFieldsByReportId reportFilterThread = new GetReportFilterFieldsByReportId();
		reportFilterThread.execute();
	
	}
	
	private void getReportFilterTableNames() {
		GetReportFilterTableNames reportFilterTablesThread = new GetReportFilterTableNames();
		reportFilterTablesThread.execute();
	}
	
	
	private void buildFiltersTableNameQuery() {
		
		//stripTableIdsFromFields();
		StringBuilder filtersQuery = new StringBuilder();
		filtersQuery.append("Select distinct table_name from report_function_table where (joining_query ");
		int index = 0;
		for (HashMap<String,String> reportFiltersMap: reportFiltersMapList){
			index ++;
			if (index == 1) {
				filtersQuery.append("like \'%" + reportFiltersMap.get("field") + "%\'");
			} else {
				filtersQuery.append("joining_query like \'%" + reportFiltersMap.get("field") + "%\'");
			}
			
			//Looking other than the last value
			if (index != reportFiltersMapList.size()){
				filtersQuery.append(" or ");
			}		
		}
		
		filtersQuery.append(")");
		filtersQuery.append(" and ");
		filtersQuery.append("function_name = ");
		filtersQuery.append("\'" + reportData.get("report_function_name") +"\'");
		filtersQuery.append(";");
		Log.i("Filters Table Name Query: ", filtersQuery.toString());
		reportFilterTableNamesQuery =  filtersQuery.toString();
		
	}
	
	private void stripTableIdsFromFields() {
		for (HashMap<String,String> reportFiltersMap: reportFiltersMapList){
			String filterFieldValue = reportFiltersMap.get("field");
			reportFiltersMap.put("field", filterFieldValue.substring(0, filterFieldValue.indexOf('=')));
			
		}
		for (HashMap<String,String> reportFiltersMap: reportFiltersMapList){
			Log.i("Stripped Value: ", reportFiltersMap.get("field"));
		}
		
		
	}
	protected class GetReportFilterFieldsByReportId extends AsyncTask<String, Integer, Boolean> {
		protected ProgressDialog progress;
		protected HornetDBService sync;
		//private int reportId = 0;
		
	
		public GetReportFilterFieldsByReportId () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportDateOptionsActivity.this, "Retrieving..", 
					 "Retrieving Report Filter Data...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			reportFiltersMapList = sync.getReportFilterFieldsByReportId(ReportDateOptionsActivity.this, reportId);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				//Calls back to the owning activity to call the thread to retrieve the joining tables
				//ReportDateOptionsActivity.this.getJoiningTablesData(reportFunctionName);
				/*System.out.println("\nReport-Type_Data");
				
				System.out.println("Result List Size: " + reportFiltersMapList.size());
				
				for (HashMap<String,String> resultMap: reportFiltersMapList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}*/
				if (reportFiltersMapList.size() > 0) {
					buildFiltersTableNameQuery();
					getReportFilterTableNames();
				} else {
					Log.i("","No Filters");
				}
				
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportDateOptionsActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	private class GetReportFilterTableNames extends GetReportFilterFieldsByReportId {

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(ReportDateOptionsActivity.this, "Retrieving..", 
					 "Retrieving Report Filter Table_Name Data...");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			reportFiltersTableNameList = sync.getReportFilterTableNames(getApplicationContext(),reportFilterTableNamesQuery);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			progress.dismiss();
			if (success) {
				//Calls back to the owning activity to call the thread to retrieve the joining tables
				//ReportDateOptionsActivity.this.getJoiningTablesData(reportFunctionName);
				System.out.println("\nReport-Type_Data");
				
				System.out.println("Result List Size: " + reportFiltersTableNameList.size());
				
				for (HashMap<String,String> resultMap: reportFiltersTableNameList){
				
					for (HashMap.Entry entry: resultMap.entrySet()){
						 System.out.println("Field: " + entry.getKey() + " Value: " + entry.getValue());					 
					}
				
				}
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReportDateOptionsActivity.this);
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
			
		}
		
	}
	
		
}

