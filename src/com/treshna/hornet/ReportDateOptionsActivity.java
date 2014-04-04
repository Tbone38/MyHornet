package com.treshna.hornet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
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
		final Spinner datePresetsSpinner = (Spinner) findViewById(R.id.datePresetsSpinner);
		String[] spinnerOptions = {"Last Month", "Last Two Months", "Last Six Months", "Last Year"};
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(this, R.layout.report_date_options_spinner , spinnerOptions);
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
	
	private void setSelectedDate(String selectedOption) {
		
		if (selectedOption.compareTo("Last Two Months")== 0){
			selectedStartDate = resetDateByNumOfMonths ( -2, startDateText);
			
		} else if (selectedOption.compareTo("Last Six Months")== 0){
			
			selectedStartDate = resetDateByNumOfMonths ( -6, startDateText);
			
		} else if (selectedOption.compareTo("Last Year")== 0){
			
			selectedStartDate = resetDateByNumOfMonths ( -12, startDateText);
			
		} else if (selectedOption.compareTo("Last Month")== 0){
			
			selectedStartDate = resetDateByNumOfMonths ( -1, startDateText);
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
		
}

