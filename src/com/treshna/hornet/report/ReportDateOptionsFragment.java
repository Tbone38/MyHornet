package com.treshna.hornet.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;
import com.treshna.hornet.network.HornetDBService;
import com.treshna.hornet.services.DatePickerFragment;


public class ReportDateOptionsFragment extends Fragment implements DatePickerFragment.DatePickerSelectListener {
	
	private HashMap<String,Object> reportData = new HashMap<String,Object>() ;
	private ArrayList<HashMap<String,String>> reportFiltersMapList = new ArrayList<HashMap<String,String>>();
	private ArrayList<HashMap<String,String>> firstReportFilterMapList = new ArrayList<HashMap<String,String>>();
	private ArrayList<HashMap<String,String>> secondReportFilterMapList = new ArrayList<HashMap<String,String>>();
	//private String reportFilterTableNamesQuery = null;
	private LayoutInflater mInflater = null;
	private View view = null;
	private int filterCount = 0;
	private boolean firstFilterSelectionMade = false;
	private TextView firstFilterTitle = null;
	private TextView secondFilterTitle = null;
	private String[] filterQueries = null;
	private	Button btnStartButton = null;
	private	Button btnEndButton = null;
	private String secondFilterQuery = null;
	private Date selectedStartDate = new Date();
	private Date selectedEndDate = new Date();
	private DatePickerFragment startDatePicker = null;
	private DatePickerFragment endDatePicker = null;
	private TextView startDateText = null;
	private TextView endDateText = null;
	private int reportId = 0;
	protected ProgressDialog progress;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		view = mInflater.inflate(R.layout.report_user_date_options,null);
		TextView reportNameTxt = (TextView) view.findViewById(R.id.report_date_options_report_name);
		startDatePicker =  new  DatePickerFragment();
		startDatePicker.setDatePickerSelectListener(this);
		endDatePicker = new  DatePickerFragment();
		endDatePicker.setDatePickerSelectListener(this);
		startDateText = (TextView) view.findViewById(R.id.startDateTxt);
		endDateText = (TextView) view.findViewById(R.id.endDateTxt);
		setDateTextView(endDateText, selectedEndDate);
		Button createButton =  (Button) view.findViewById(R.id.btnCreateReport);
		Button columnOptionsButton =  (Button) view.findViewById(R.id.btnColumnOptions);
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.dateOptions);
		if (layout.getTag().toString().compareTo("Large") == 0) {
			btnStartButton = (Button) view.findViewById(R.id.btnSelectStartDate);
			btnEndButton = (Button) view.findViewById(R.id.btnSelectEndDate);
		}
		Bundle fragmentData = getArguments();
		reportNameTxt.setText(fragmentData.getString("report_name").trim());
		firstFilterTitle = (TextView) view.findViewById(R.id.firstFilterTitle);
		secondFilterTitle = (TextView) view.findViewById(R.id.secondFilterTitle);
		reportId = fragmentData.getInt("report_id", 0);
		reportData.put("report_name", fragmentData.getString("report_name"));
		reportData.put("report_function_name", fragmentData.getString("report_function_name"));
		setUpQuickSelectSpinner();
		getReportFilterFieldsByReportId();

		
		if (layout.getTag().toString().compareTo("Large") == 0) {
			
			btnStartButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					startDatePicker.show(getActivity().getSupportFragmentManager(), "Start Date Picker");			
				}
				
			});
			
			btnEndButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					endDatePicker.show(getActivity().getSupportFragmentManager(), "End Date Picker");	
				}
				
			});		
		}
	
		
		
		
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
		
		// TODO Auto-generated method stub
		return view;
	}

	
	private void setUpQuickSelectSpinner () {
		
		final Spinner datePresetsSpinner = (Spinner) view.findViewById(R.id.datePresetsSpinner);
		String[] spinnerOptions = {"Today", "This Month", "Last Month", "Last Two Months", "Last Six Months", "Last Year"};
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.report_date_options_spinner , spinnerOptions);
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
	
	private void setUpFirstFilterSpinner() {
		
		final Spinner firstFilterSpinner = (Spinner) view.findViewById(R.id.firstFilterSpinner);
		
		ArrayList<String> filterOptionsList = new ArrayList<String>();
		
		for (HashMap<String,String> filterName : firstReportFilterMapList){
			if (filterName.containsKey("name")) {
					//Bug fix for ALL value being zero (in task-trigger table) in the Task Event filter
				if (!((filterName.get("name").compareTo("ALL") == 0) && (reportFiltersMapList.get(0).get("filter_name").compareTo("Task Event") == 0))) {
					
					if (filterName.get("name").contains("All")) {
						
						filterOptionsList.add(0,filterName.get("name"));
						
					} else {
						
						filterOptionsList.add(filterName.get("name"));
					}
				}
						
			}
			
			else if (filterName.containsKey("text_value")) {
				
				//Inserting the select-all option at the top of the list
				
				if (filterName.get("text_value").contains("All")) {
					
					filterOptionsList.add(0,filterName.get("text_value"));
					
				} else {
					
					filterOptionsList.add(filterName.get("text_value"));
				}	
		    }
		
		}
	
		ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(getActivity(), R.layout.report_date_options_spinner , filterOptionsList);
		firstFilterSpinner.setAdapter(filterAdapter);
		firstFilterSpinner.setPrompt("Select Filter");
		firstFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					String filterField = null;
					String selectedParam =  (String) firstFilterSpinner.getSelectedItem();
											
						String filterType = ReportQueryResources.getFilterTypeByName(getActivity(),reportFiltersMapList.get(0).get("filter_name"));
						
						
						//Checking for filters implemented by id...
						if (filterType.compareTo("type-ID") == 0) {
							
							
							String selectedId = getIdFromReportFirstFilterDataName(selectedParam);
							
								
								if (selectedId == null ) {
								
										if (reportFiltersMapList.get(0).get("filter_name").compareTo("Programme Group") == 0) {
											
											if (filterQueries.length > 1) {
												
												filterQueries[1] = ReportQueryResources.getFilterQueryByName(getActivity(), "Programme");
												
											}
										
										}
										
										reportData.remove("first_filter_field");
										
								} else {
										
										Log.i("Filter ID:", selectedId);
										
										
										
										if (reportFiltersMapList.get(0).get("filter_name").compareTo("Programme Group") == 0) {
											
											if (filterQueries.length > 1) {
											
												filterQueries[1] = ReportQueryResources.getFilterQueryByName(getActivity(), "Group_Programme");
												
												addSelectedParamToQuery(selectedId);
											}
											
										}
										
										
										filterField = buildFilterField(selectedId, 0);
										Log.i(" First Selected Filter Field ", filterField );
										
										reportData.put("first_filter_field", filterField);
									}
								
								if (filterQueries.length > 1) {
										getSecondReportFilterData();
								}
							
						
						}  else if (filterType.compareTo("type-Name")== 0) {
							
								//Block for filters implemented by name...
								addSelectedParamToQuery(selectedParam);
								if (filterQueries.length > 1) {
									getSecondReportFilterData();
								}
								filterField = buildFilterField(selectedParam, 0);
								Log.i(" First Selected Filter Field ", filterField );
								reportData.put("first_filter_field", filterField);
					
						}						
									
			}
			

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub			
			}
			
		});
	}
		
	private void setUpSecondFilterSpinner() {
		
		final Spinner secondFilterSpinner = (Spinner) view.findViewById(R.id.secondFilterSpinner);
		
		ArrayList<String> filterOptionsList = new ArrayList<String>();

		
		for (HashMap<String,String> filterName : secondReportFilterMapList) {
			
			if (filterName.containsKey("name")) {
				
				//Inserting the select-all option at the top of the list
				if (filterName.get("name").contains("All")) {
					
					filterOptionsList.add(0,filterName.get("name"));
					
				} else {
					
					filterOptionsList.add(filterName.get("name"));
				}
			}
			
			else if (filterName.containsKey("text_value")) {
				
				//Inserting the select-all option at the top of the list
				if (filterName.get("text_value").contains("All")) {
					
					filterOptionsList.add(0,filterName.get("text_value"));
					
				} else {
					
					filterOptionsList.add(filterName.get("text_value"));
				}
		    }

		}
		
		ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(getActivity(), R.layout.report_date_options_spinner , filterOptionsList);
		secondFilterSpinner.setAdapter(filterAdapter);
		secondFilterSpinner.setPrompt("Select Filter");
		secondFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					String filterField = null;
					String selectedName =  (String) secondFilterSpinner.getSelectedItem();
					String filterType = ReportQueryResources.getFilterTypeByName(getActivity(),reportFiltersMapList.get(1).get("filter_name"));
					
					Log.i("Second Filter Type:", filterType);
					//Fetching id field to apply filters for reports with the Programme-Group filter...
					if (filterType.compareTo("type-ID") == 0) {
						
						String selectedId = getIdFromSecondReportFilterDataName(selectedName);
						
						if (selectedId != null) {
							
							filterField = buildFilterField(selectedId, 1);
							
						} else {
							
							reportData.remove("second_filter_field");
						}
			
						
					} else if (filterType.compareTo("type-Name") == 0)  {
						
						filterField = buildFilterField(selectedName, 1);

					}
					
					if (filterField != null) {
						
						Log.i(" Second Selected Filter Field ", filterField);
						reportData.put("second_filter_field", filterField);
					}							
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}

		});
	}
	
	private String buildFilterField (String filterValue, int filterNumber) {
		
		String filterField = "";
		
		filterField = reportFiltersMapList.get(filterNumber).get("field");
		//Removing placeholder text from field..
		filterField = filterField.substring(0, filterField.indexOf("=") + 1);
		//Add selected name or id value to filter field..
		filterField +=  "\'" + filterValue + "\'";
		return filterField;
		
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
		
		loadFragment("date_options");
	
	}
	
	private void loadColumnOptions() {
		
		loadFragment("column_options");
	}
	
	private void loadFragment(String callingActivity) {
		Fragment newFragment = null;
		reportData.put("start_date",selectedStartDate);
		reportData.put("end_date",  selectedEndDate);
		
		if (callingActivity.compareTo("column_options")== 0)
		{
			newFragment =  new ReportColumnOptionsFragment();
			
		} else if (callingActivity.compareTo("date_options")== 0) {
			
			newFragment =  new ReportMainFragment();
		}

		Bundle bdl = new Bundle(1);
		newFragment.setArguments(bdl);
		bdl.putInt("report_id", reportId);
		//Pushing UI and upstream data through to the report column options intent..
		for (Map.Entry<String,Object> param: reportData.entrySet()){
			//Casting the date values to time-stamp to pass through the intent..
				if ((param.getKey().toString().compareTo("start_date") == 0) || (param.getKey().toString().compareTo("end_date") == 0) ) {
					
					bdl.putLong(param.getKey().toString(), ((Date) param.getValue()).getTime());
					
				} else {
					
					bdl.putString(param.getKey().toString(),  param.getValue().toString());
				}			
		}
		
		bdl.putString("calling_activity", callingActivity); 
		
		((MainActivity)getActivity()).changeFragment(newFragment, "reportColumnOptions");				
	 }

	@Override
	public void onDateSelect(String date, DatePickerFragment theDatePicker) {
		
		if (theDatePicker == startDatePicker) {
			 selectedStartDate = dateFromPicker(date);
			 this.setDateTextView(startDateText, selectedStartDate);
		}
		else if (theDatePicker == endDatePicker) {
			selectedEndDate = dateFromPicker(date);
			this.setDateTextView(endDateText, selectedEndDate);
		}
		
	}
	
	private String getIdFromReportFirstFilterDataName (String filterDataName) {
		
		
		for (HashMap<String,String> filterData : firstReportFilterMapList) {
			
			if (filterData.containsKey("name")) {
			
				if (filterData.get("name").compareTo(filterDataName) == 0) {
					 return filterData.get("id");
				}
				
			}
			
		}
		return null;
	}
	
	private String getIdFromSecondReportFilterDataName (String filterDataName) {
		
		
		for (HashMap<String,String> filterData : secondReportFilterMapList) {
			
			if (filterData.containsKey("name")) {
				
				if (filterData.get("name").compareTo(filterDataName) == 0) {
					 return filterData.get("id");
				}
				
			}
			
		}
		return null;
	}
	
	private void getReportFilterFieldsByReportId () {
		
		GetReportFilterFieldsByReportId reportFilterThread = new GetReportFilterFieldsByReportId();
		reportFilterThread.execute();
	
	}
	
	private void getFilterQueriesFromXML () {
		
		filterQueries = new String [reportFiltersMapList.size()];
		int index = 0;
		for (HashMap<String,String> reportFiltersMap: reportFiltersMapList) {
			filterQueries[index] = ReportQueryResources.getFilterQueryByName(getActivity(),reportFiltersMap.get("filter_name"));
			index ++;
		}
	}
	
	private void printFilterQueries() {
		for(int i = 0; i < filterQueries.length; i++) {
			Log.i("Filter Query Number: " + i+1 , filterQueries[i]);
		}
	}
	

	
  private void addSelectedParamToQuery(String selectedParam) {
	  
		if (filterQueries.length > 1) {
 		
			if (firstFilterSelectionMade) {
				
				if (selectedParam.compareTo("All") == 0) {
					
					filterQueries[1] = secondFilterQuery.replace("=","").replace("?", " IS NOT NULL ");
					
				}
				
				else {
					
					filterQueries[1] = secondFilterQuery.replace("?", "\'" + selectedParam + "\'");
				}
				
			} else {
				
				//Saving a reference to the original query to the activity object..
				secondFilterQuery = filterQueries[1];
				firstFilterSelectionMade = true;
				
				if (selectedParam.compareTo("All") == 0) {
					
					filterQueries[1] = filterQueries[1].replace("=","").replace("?", " IS NOT NULL ");
					
				}
				
				else {
					
					filterQueries[1] = filterQueries[1].replace("?", "\'" + selectedParam + "\'");
				}

			}
			
			Log.i(" Filter String: ", filterQueries[1]);
		}
   }
 

  private void getFirstReportFilterData() {
		
		GetFirstReportFilterData  firstReportFilterThread = new GetFirstReportFilterData(0);
		firstReportFilterThread.execute();
	}
  
  private void getSecondReportFilterData() {
		
		GetFirstReportFilterData  firstReportFilterThread = new GetFirstReportFilterData(1);
		firstReportFilterThread.execute();
	}
  
  @Override
  public void onResume() {
	  super.onResume();
	  
	  if (progress != null) {
		  progress.show();
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
	

  protected class GetReportFilterFieldsByReportId extends AsyncTask<String, Integer, Boolean> {
		
		protected HornetDBService sync;
		
	
		public GetReportFilterFieldsByReportId () {
			sync = new HornetDBService();
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(getActivity(), "Retrieving..", 
					 "Retrieving Report Filter Data...");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			reportFiltersMapList = sync.getReportFilterFieldsByReportId(getActivity(), reportId);
	        return true;
		}
		

		protected void onPostExecute(Boolean success) {
			
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			
			if (success) {
			
				if (reportFiltersMapList != null) {
					 		
					if (reportFiltersMapList.size() > 0) {
						LinearLayout firstFilterLayout = (LinearLayout) view.findViewById(R.id.firstFilterLayout);
						firstFilterLayout.setVisibility(View.VISIBLE);
						firstFilterTitle.setText(reportFiltersMapList.get(0).get("filter_name"));				
						if (reportFiltersMapList.size() > 1) {
							LinearLayout secondFilterLayout = (LinearLayout) view.findViewById(R.id.secondFilterLayout);
							secondFilterLayout.setVisibility(View.VISIBLE);
							secondFilterTitle.setText(reportFiltersMapList.get(1).get("filter_name"));
						}
						
						getFilterQueriesFromXML();
						printFilterQueries();
						getFirstReportFilterData();
						
					} else {
						
						Log.i("","No Filters");
					}
					
				} else {
					
					Toast.makeText(getActivity(), "No Connection to Database", Toast.LENGTH_LONG).show();
					getActivity().onBackPressed();				
				}

				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
	    }
	 }
	
	private class GetFirstReportFilterData extends GetReportFilterFieldsByReportId {
		
		
		
		public GetFirstReportFilterData (int filterCount) {
			ReportDateOptionsFragment.this.filterCount = filterCount;
		}
		
		@Override
		protected void onPreExecute() {
			if (getActivity().isFinishing()) {
				return;
			}
			progress = ProgressDialog.show(getActivity(), "Retrieving..", 
					 "Retrieving First Report Filter Data...");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (filterCount == 0) {
				
				firstReportFilterMapList = sync.getFirstReportFilterData(getActivity(),filterQueries[filterCount]);
				
			} else if (filterCount == 1) {
				
				secondReportFilterMapList = sync.getFirstReportFilterData(getActivity(),filterQueries[filterCount]);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (progress != null && progress.isShowing()) {
				
				progress.dismiss();
			}
			progress = null;
			
			if (success) {
			
				Log.i("Filter Count", filterCount+"");
				
				if (filterCount == 0) {
					
					setUpFirstFilterSpinner();
					Log.i("Orientation", "Set To Portrait");
					getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					
				} else if (filterCount == 1) {
					
				    setUpSecondFilterSpinner();

			    }
				
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Error Occurred")
				.setMessage(sync.getStatus())
				.setPositiveButton("OK", null)
				.show();
			}
			
		}
		
	}

		
}

