package com.treshna.hornet;


import com.treshna.hornet.BookingPage.TagFoundListener;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MemberMembershipFragment extends Fragment implements TagFoundListener{
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private MemberActions mActions;
	
	//private static final String TAG = "MemberDetails";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		mActions = new MemberActions(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_membership, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		view = setupView();
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setupView();
	}
	
	public MemberActions getMemberActions(){
		return this.mActions;
	}
	
	private View setupView() {
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, 
				ContentDescriptor.Membership.Cols.MID+" = ?", new String[] {memberID}, null);
		
		LinearLayout mslist = (LinearLayout) view.findViewById(R.id.member_ms_list);
		mslist.removeAllViews();
		
		while (cur.moveToNext()) {
			//Log.e(TAG, "Inflater is:"+mInflater.toString());
			RelativeLayout membershipRow = (RelativeLayout) mInflater.inflate(R.layout.member_membership_row, null);
			
			ImageView icon = (ImageView) membershipRow.findViewById(R.id.member_membership_drawable);
			icon.setColorFilter(Services.ColorFilterGenerator.setColourGrey());
			
			TextView name = (TextView) membershipRow.findViewById(R.id.member_membership_title);
			name.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			
			TextView start = (TextView) membershipRow.findViewById(R.id.member_ms_started);
			start.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART))
					, "yyyy-MM-dd", "dd MMM yyyy"));
			
			TextView end = (TextView) membershipRow.findViewById(R.id.member_ms_expired);
			String enddate = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY))
					, "yyyy-MM-dd", "dd MMM yyyy");
			if (enddate == null) {
				enddate = cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY));
			}
			end.setText(enddate);
			
			TextView visits = (TextView) membershipRow.findViewById(R.id.member_ms_visits);
			visits.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.VISITS)));
			
			mslist.addView(membershipRow);
		}
		cur.close();
		mActions.setupActions(view, memberID);
		return view;
	}

	@Override
	public void onNewTag(String serial) {
		mActions.onNewTag(serial);
	}
	
}