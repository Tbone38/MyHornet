package com.treshna.hornet;


import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MemberMembershipFragment extends MemberActionsFragment{
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	
	private static final String TAG = "MemberDetails";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_membership, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
		
	private View setupView() {
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, 
				ContentDescriptor.Membership.Cols.MID+" = ?", new String[] {memberID}, null);
		
		LinearLayout mslist = (LinearLayout) view.findViewById(R.id.member_ms_list);
		
		while (cur.moveToNext()) {
			//Log.e(TAG, "Inflater is:"+mInflater.toString());
			RelativeLayout membershipRow = (RelativeLayout) mInflater.inflate(R.layout.member_membership_row, null);
			
			TextView name = (TextView) membershipRow.findViewById(R.id.member_membership_title);
			name.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
			
			TextView start = (TextView) membershipRow.findViewById(R.id.member_ms_started);
			start.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART))
					, "yyyy-MM-dd", "dd MMM yyyy"));
			
			TextView end = (TextView) membershipRow.findViewById(R.id.member_ms_expired);
			end.setText(Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY))
					, "yyyy-MM-dd", "dd MMM yyyy"));
			
			TextView visits = (TextView) membershipRow.findViewById(R.id.member_ms_visits);
			visits.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.VISITS)));
			
			mslist.addView(membershipRow);
		}
		cur.close();
		super.setupActions(view, memberID);
		return view;
	}
	
}