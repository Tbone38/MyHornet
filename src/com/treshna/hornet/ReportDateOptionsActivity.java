package com.treshna.hornet;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class ReportDateOptionsActivity extends Activity {
	private HashMap<String,String> reportData = new HashMap<String,String>() ;
	private DatePicker startDatePicker = null;
	private DatePicker endDatePicker = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.report_user_date_options);
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		TextView reportNameTxt = (TextView) findViewById(R.id.report_date_options_report_name);
		startDatePicker = (DatePicker) findViewById(R.id.start_date);
		endDatePicker = (DatePicker) findViewById(R.id.end_date);
		Button nextButton =  (Button) findViewById(R.id.btnNext);	
		reportNameTxt.setText(intent.getStringExtra("report_name"));
		reportData.put("report_id", intent.getStringExtra("report_id"));
		reportData.put("report_name", intent.getStringExtra("report_name"));
		reportData.put("report_function_name", intent.getStringExtra("report_function_name"));
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				renderReport();
				
			}
		}); 
		//renderReport();
	}
	
	private String dateStringFromPicker (DatePicker datePicker) {
		int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth() + 1;
		int year = datePicker.getYear();
		
		StringBuilder dateString = new StringBuilder();
		dateString.append(day);
		dateString.append("-");
		dateString.append(month);
		dateString.append("-");
		dateString.append(year);
		
		return dateString.toString();
	}
	
	
	
	private void renderReport(){
		int duration = Toast.LENGTH_LONG;
		String dates  = "Start Date: ";
		dates += this.dateStringFromPicker(this.startDatePicker);
		dates += "\nEnd Date: ";
		dates += this.dateStringFromPicker(this.endDatePicker);
		Toast dateToast = Toast.makeText(ReportDateOptionsActivity.this, dates, duration);
		dateToast.show();
		reportData.put("start_date", this.dateStringFromPicker(startDatePicker));
		reportData.put("end_date", this.dateStringFromPicker(endDatePicker));	
		
		for (Map.Entry param: reportData.entrySet()){
			System.out.println(param.getKey().toString()+ ": " + param.getValue().toString());
		}
	}
	
	
	
	

}
