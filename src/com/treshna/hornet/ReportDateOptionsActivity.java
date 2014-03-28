package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;





import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;


public class ReportDateOptionsActivity extends Activity {
	private HashMap<String,Object> reportData = new HashMap<String,Object>() ;
	private DatePicker startDatePicker = null;
	private DatePicker endDatePicker = null;
	private int reportId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.report_user_date_options);
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		TextView reportNameTxt = (TextView) findViewById(R.id.report_date_options_report_name);
		startDatePicker = (DatePicker) findViewById(R.id.start_date);
		endDatePicker = (DatePicker) findViewById(R.id.end_date);
		Button createButton =  (Button) findViewById(R.id.btnCreateReport);
		Button columnOptionsButton =  (Button) findViewById(R.id.btnColumnOptions);
		reportNameTxt.setText(intent.getStringExtra("report_name").trim());
		reportId = Integer.parseInt(intent.getStringExtra("report_id"));
		reportData.put("report_name", intent.getStringExtra("report_name"));
		reportData.put("report_function_name", intent.getStringExtra("report_function_name"));
	
		
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
	
	private Date  dateStringFromPicker (DatePicker datePicker) {
		int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth();
		int year = datePicker.getYear();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal.getTime();	
	}
	

	private void loadMainReport(){
		
		loadIntent("date_options", ReportMainActivity.class);
	
	}
	
	private void loadColumnOptions() {
		
		loadIntent("column_options", ReportColumnOptionsActivity.class);
	}
	
	private void loadIntent(String callingActivity, Class<?> activity) {
		reportData.put("start_date",this.dateStringFromPicker(startDatePicker));
		reportData.put("end_date", this.dateStringFromPicker(endDatePicker));
		
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
		
}

