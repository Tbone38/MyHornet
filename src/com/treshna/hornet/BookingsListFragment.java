package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookingsListFragment extends ListFragment implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	OnCalChangeListener mCallback;

    // Container Activity must implement this interface
    public interface OnCalChangeListener {
        public void onDateChange(Date date);
    }

    //TODO: refresh adapter on sync completion.
    // using cursor adapter ?
    
	
	private static ContentResolver contentResolver = null;
    private static Cursor cur = null;
    private SimpleCursorAdapter mAdapter;
    private Date date;
    private Date newdate;
    private View root;
    public LoaderManager loadermanager;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
       ViewGroup rootView = (ViewGroup) inflater.inflate(
               R.layout.booking_fragment_layout, container, false);
        root = rootView;
        //setDate();
        loadermanager = getLoaderManager();
        return rootView;
    }
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setDate(){
		contentResolver = getActivity().getContentResolver();
        int position = getArguments().getInt(Services.Statics.KEY);
        System.out.print("\n\nPosition::"+position);
        Calendar cal = Calendar.getInstance();
        Date time = new Date(getArguments().getLong("date")); 
        cal.setTime(time);
        SimpleDateFormat format = new SimpleDateFormat("EEEE MMMM dd", Locale.US);
        TextView day = (TextView) root.findViewById(R.id.bookingday);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			day.setBackground(getResources().getDrawable(R.drawable.button));
		} else {
			day.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
		}
        switch (position){
        case(0):{
        	cal.add(Calendar.DATE, -2);
     		date = cal.getTime();
     		day.setText(format.format(date));
     		break;
        		}
        case(1):{
        	cal.add(Calendar.DATE, -1);
     		date = cal.getTime();
     		day.setText(format.format(date));
     		break;
        		}
        case(2):{
        	cal.add(Calendar.DATE, 0);
     		date = cal.getTime();
     		day.setText(format.format(date));
     		break;
        		}
        case(3):{
        	cal.add(Calendar.DATE, 1);
     		date = cal.getTime();
     		day.setText(format.format(date));
     		break;
        		}
        case(4):{
        	cal.add(Calendar.DATE, 2);
     		date = cal.getTime();
     		day.setText(format.format(date));
     		break;
        		}
        case(5):{
        	cal.add(Calendar.DATE, 3);
     		date = cal.getTime();
     		day.setText(format.format(date));
     		break;
        		}
        case (6):{
        	cal.add(Calendar.DATE, 4);
        	date = cal.getTime();
        	day.setText(format.format(date));
        	break;
        }
        default:{ //hopefully never fires.
        	date = new Date();
        	day.setText(format.format(date));
        }
        }
        
        day.setClickable(true);
        day.setOnClickListener(this);
	}
	
	@Override
	 public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
  
	}
	
	@Override
	public void onResume(){
		super.onResume();
		setAdapter();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	}
	
	private void setAdapter(){
		
       	String[] from = {};
       	int[] to = {};
       	loadermanager.restartLoader(0, null, this);
       	mAdapter = new BookingsListAdapter(getActivity(), R.layout.booking_list, null, from, to);
		setListAdapter(mAdapter);
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
	}

	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case (R.id.bookingday):{
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
				View view = createCalendarLayout();
				final Dialog dialog = new Dialog(getActivity());
				dialog.setContentView(view);
				dialog.setTitle("Calendar");
				dialog.setCanceledOnTouchOutside(true);
				
				TextView okay = (TextView) dialog.findViewById(1);
				okay.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						date = newdate;
						mCallback.onDateChange(newdate);
						//setAdapter();
						dialog.dismiss();
					}
				});
				
				TextView cancel = (TextView) dialog.findViewById(2);
				cancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick (View v){
						newdate = null;
						dialog.cancel();
					}
				});
				
				dialog.show();
			}
			break;
			}
		}
	}
	@SuppressLint("NewApi")
	private View createCalendarLayout(){
		RelativeLayout rl = new RelativeLayout(getActivity());
		RelativeLayout.LayoutParams rlparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.setLayoutParams(rlparams);
		
		CalendarView calendar = new CalendarView(getActivity());
		rlparams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 450);
		calendar.setLayoutParams(rlparams);
		calendar.setId(365); //
		calendar.setDate(date.getTime());
		newdate = date;
		calendar.setOnDateChangeListener(new OnDateChangeListener(){
			@Override
			public void onSelectedDayChange(CalendarView view, int year,
					int month, int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(year, month, dayOfMonth);
				newdate = cal.getTime();
			}
		});
		
		LinearLayout buttonRow = new LinearLayout(getActivity());
		rlparams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rlparams.addRule(RelativeLayout.BELOW, calendar.getId());
		buttonRow.setLayoutParams(rlparams);
		
		TextView select = new TextView(getActivity());
		select.setId(1);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, (float) .5);
		params.setMargins(3, 3, 3, 5);
		select.setPadding(5, 5, 5, 5);
		select.setLayoutParams(params);
		select.setGravity(Gravity.CENTER);
		select.setText("Select");
		select.setTextSize(20);
		select.setTextColor(getActivity().getResources().getColor(R.color.gym));
		select.setClickable(true);
		
		TextView cancel = new TextView(getActivity());
		cancel.setId(2);
		params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, (float) .5);
		params.setMargins(3, 3, 3, 5);
		cancel.setPadding(5, 5, 5, 5);
		cancel.setLayoutParams(params);
		cancel.setGravity(Gravity.CENTER);
		cancel.setText("Cancel");
		cancel.setTextColor(getActivity().getResources().getColor(R.color.gym));
		cancel.setTextSize(20);
		cancel.setClickable(true);
		
		buttonRow.addView(select);
		buttonRow.addView(cancel);
		rl.addView(calendar);
		rl.addView(buttonRow);
		
		return rl;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnCalChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCalChangeListener");
        }
    }

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		setDate();
		if (getActivity() != null) {
			String rid = Services.getAppSettings(getActivity(), "resourcelist");
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
			String today = format.format(date);
			String selection = "bt."+ContentDescriptor.BookingTime.Cols.RID+" = "+rid+" AND "//+ContentDescriptor.Booking.Cols.RESULT+" > 5 AND "
	       			+"bt."+ContentDescriptor.BookingTime.Cols.ARRIVAL+" = "+today;
	       	String[] where = {today, ContentDescriptor.Booking.Cols.RESULT+" > 5 "};
	 
	       	return new CursorLoader(getActivity(), ContentDescriptor.Time.TIME_BOOKING_URI, null, selection, where, "_id ASC");
		}
		System.out.print("\n\nACTIVITY NULL\n\n");
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		cur = cursor;
		if(cursor.isClosed()) {
           System.out.print("\n\nCursor Closed");
            Activity activity = getActivity();
            if(activity!=null) {
            	//mAdapter.notifyDataSetChanged();
            	loadermanager.restartLoader(0, null, this);
            }
            return;
        }
		mAdapter.changeCursor(cursor);		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}


}
