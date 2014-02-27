package com.treshna.hornet;

import java.io.File;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.treshna.hornet.BookingPage.TagFoundListener;

	//TODO: more null handling
public class MemberDetailsFragment extends Fragment implements OnClickListener, TagFoundListener {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	private String visitDate;
	private int selectedFragment;
	private TagFoundListener tagFoundListener;
	private static final String TAG = "MemberDetails";
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		Log.i(TAG, "Created At:"+new Date().toString());
		contentResolver = getActivity().getContentResolver();
	
		memberID = this.getArguments().getString(Services.Statics.MID);
		visitDate = this.getArguments().getString(Services.Statics.KEY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		  super.onCreateView(inflater, container, savedInstanceState);
	
		  view = inflater.inflate(R.layout.member_details, container, false);
	     
		  view = setupLayout();
		  
		  setupFragment(null);
		  return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setupLayout();
	}
	
	@SuppressLint("NewApi")
	private View setupLayout() {
		Uri uri = Uri.withAppendedPath(ContentDescriptor.Image.IMAGE_JOIN_MEMBER_URI,
				memberID);
		cur = contentResolver.query(uri, null, null, null, null);
		if (!cur.moveToFirst()) {
			return view;
		}
		
		TextView memberName = (TextView) view.findViewById(R.id.member_navigation_name);
		memberName.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))
					+" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		/*if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR)) == true) {
			memberName.setTextColor(Color.BLACK);
		} else {
			memberName.setTextColor(Color.parseColor(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR))));
		}*/
		
		TextView memberNumber = (TextView) view.findViewById(R.id.member_navigation_number);
		memberNumber.setText("#"+memberID);
		
		
		ImageView img = (ImageView) view.findViewById(R.id.member_navigation_image);
		String imgDir = getActivity().getExternalFilesDir(null)+"/0_"+memberID+".jpg";
		File imgFile = null;
		imgFile = new File(imgDir);
		if (imgFile.exists() == true){
			
			Services.loadBitmap(imgFile,img, 500, 450);
		    img.setClickable(true);
		    img.setOnClickListener(this);
		} else {
			Drawable imgDrawable = getActivity().getResources().getDrawable(R.drawable.nophotogrey);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				img.setBackground(imgDrawable);
			} else {
				img.setBackgroundDrawable(imgDrawable);
			}
			
		}
	    
		String happiness = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.HAPPINESS));
		if (happiness != null) {
			if (happiness.compareTo(":(") == 0) {
				ImageView sad;
				sad = (ImageView) view.findViewById(R.id.member_status_sad);
				sad.setVisibility(View.VISIBLE);
			} else if (happiness.compareTo(":|") == 0) {
				ImageView plain;
				plain = (ImageView) view.findViewById(R.id.member_status_plain);
				plain.setVisibility(View.VISIBLE);
			} else if (happiness.compareTo(":)") == 0) {
				ImageView happy;
				happy = (ImageView) view.findViewById(R.id.member_status_happy);
				happy.setVisibility(View.VISIBLE);
			}
		} else {
			ImageView happy;
			happy = (ImageView) view.findViewById(R.id.member_status_happy);
			happy.setVisibility(View.VISIBLE);
		}

		int status = cur.getInt(cur.getColumnIndex(ContentDescriptor.Member.Cols.STATUS));
		if (status >= 0) {
			if (status == 0) { //Current
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_ok);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColourGreen());
			} else if (status == 1) { //on hold
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_hold);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColourBlue());
			} else if (status == 2|| status == 3) { //expired
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_expired);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColourRed());
			} else if (status == 4 ) { //promotion
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_casual);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColourRed());
			} else if (status == 5) { //casual ?
				//what should be shown?
				ImageView statusView;
				statusView = (ImageView) view.findViewById(R.id.member_status_ok);
				statusView.setVisibility(View.VISIBLE);
				statusView.setColorFilter(Services.ColorFilterGenerator.setColourBlue());
			}
		} else {
			ImageView statusView;
			statusView = (ImageView) view.findViewById(R.id.member_status_ok);
			statusView.setVisibility(View.VISIBLE);
			statusView.setColorFilter(Services.ColorFilterGenerator.setColourGreen());
		}
		
		cur.close();
		
		TextView memberBalance = (TextView) view.findViewById(R.id.member_status_balance);
		cur = contentResolver.query(ContentDescriptor.MemberBalance.CONTENT_URI, null, 
				ContentDescriptor.MemberBalance.Cols.MID+" = ?", new String[] {memberID}, null);
		
		if (!cur.moveToNext()) {
			memberBalance.setVisibility(View.GONE);
		} else {
			String amount = cur.getString(cur.getColumnIndex(ContentDescriptor.MemberBalance.Cols.BALANCE));
			if (amount.compareTo("$0.00") == 0) {
				memberBalance.setText("Balance: "+amount);
			} else if (amount.substring(0, 1).compareTo("-") == 0) { //member in Credit
				memberBalance.setText("Credit: "+amount.substring(1));
				memberBalance.setTextColor(getActivity().getResources().getColor(R.color.blue));
			} else {
				memberBalance.setText("Owing: "+amount);
				memberBalance.setTextColor(getActivity().getResources().getColor(R.color.red));
			}
		}
		cur.close();
		
	    //onclick listeners
	    LinearLayout memberships = (LinearLayout) view.findViewById(R.id.button_member_navigation_memberships);
	    memberships.setOnClickListener(this);
	    
	    LinearLayout notes = (LinearLayout) view.findViewById(R.id.button_member_navigation_notes);
	    notes.setOnClickListener(this);
	    
	    LinearLayout visits = (LinearLayout) view.findViewById(R.id.button_member_navigation_visits);
	    visits.setOnClickListener(this);
	    //visits.setVisibility(View.GONE);
			    
	    LinearLayout finance = (LinearLayout) view.findViewById(R.id.button_member_navigation_finance);
	    finance.setOnClickListener(this);
	    
	    LinearLayout bookings = (LinearLayout) view.findViewById(R.id.button_member_navigation_booking);
	    bookings.setOnClickListener(this);
	    bookings.setVisibility(View.GONE);
	    
	    
		return view;
	}
	
	
	private void setupFragment(Fragment f) {
		if (f == null) {
			f = new MemberMembershipFragment();
			Bundle bdl = new Bundle(1);
	        bdl.putString(Services.Statics.MID, memberID);
	        f.setArguments(bdl);
	        selectedFragment = R.id.button_member_navigation_memberships;
		}
		tagFoundListener = (TagFoundListener) f;
		FragmentManager fm = this.getChildFragmentManager();		
		FragmentTransaction ft = fm.beginTransaction();

		ft.replace(R.id.frame_bottom, f);
		ft.commit();
		reDrawButtonMember();
		setSelected();
	}
	
	@SuppressWarnings("deprecation")
	private void reDrawButtonMember(){
		int[] layouts = {R.id.button_member_navigation_booking, R.id.button_member_navigation_finance,
				R.id.button_member_navigation_memberships, R.id.button_member_navigation_notes,
				R.id.button_member_navigation_visits};
		
		for (int i=0; i <layouts.length; i++) {
			LinearLayout button = (LinearLayout) view.findViewById(layouts[i]);
			button.setBackgroundColor(this.getResources().getColor(android.R.color.background_light));
			button.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button));
		}
	}
	
	private void setSelected(){
		LinearLayout selectedView = (LinearLayout) view.findViewById(selectedFragment);
		selectedView.setBackgroundColor(this.getResources().getColor(R.color.android_blue));
	}

	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case (R.id.button_member_navigation_notes):{
			Fragment f = new MemberNotesFragment();
			Bundle bdl = new Bundle(2);
			
	        bdl.putString(Services.Statics.MID, memberID);
	        bdl.putString(Services.Statics.KEY, visitDate);
	        f.setArguments(bdl);
	        
	        selectedFragment = R.id.button_member_navigation_notes;
	        setupFragment(f);
			break;
		}
		case (R.id.button_member_navigation_memberships):{
			Fragment f = new MemberMembershipFragment();
			Bundle bdl = new Bundle(1);
	        bdl.putString(Services.Statics.MID, memberID);
	        f.setArguments(bdl);
	        selectedFragment = R.id.button_member_navigation_memberships;
	        setupFragment(f);
	        break;
		}
		case (R.id.button_member_navigation_visits):{
			Fragment f = new MemberVisitHistoryFragment();
			Bundle bdl = new Bundle(2);
	        bdl.putString(Services.Statics.MID, memberID);
	        bdl.putString(Services.Statics.KEY, visitDate);
	        f.setArguments(bdl);
	        
	        selectedFragment = R.id.button_member_navigation_visits;
	        setupFragment(f);
			break;
		}
		case (R.id.member_navigation_image):{
			String selection = ContentDescriptor.Image.Cols.DISPLAYVALUE+" = 0"
					+" AND "+ContentDescriptor.Image.Cols.MID+" = "+memberID;
			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, null, selection, null, null);
			if (cur.getCount() <= 0) break;
			cur.moveToFirst();
			String date = Services.dateFormat(cur.getString(2), "dd MMM yy hh:mm:ss aa", "yyyy-MM-dd");
			String message = "Image Taken: "+date+ "\nImage Description: "+cur.getString(3);
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			break;
		}
		case (R.id.button_member_navigation_finance):{
			Fragment f = new MemberFinanceFragment();
			Bundle bdl = new Bundle(1);
			bdl.putString(Services.Statics.MID, memberID);
			f.setArguments(bdl);
			selectedFragment = R.id.button_member_navigation_finance;
			break;
		}
		}
	}

	@Override
	public void onNewTag(String serial) {
		tagFoundListener.onNewTag(serial);
	}	
}
