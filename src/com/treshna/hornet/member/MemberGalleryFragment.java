package com.treshna.hornet.member;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.treshna.hornet.R;
import com.treshna.hornet.services.CameraWrapper;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;
import com.treshna.hornet.visitor.VisitorsViewAdapter;


public class MemberGalleryFragment extends Fragment implements OnClickListener, 
		LoaderManager.LoaderCallbacks<Cursor> {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private GalleryViewAdapter mAdapter;
	private LoaderManager mLoader;
	
	//private static final String TAG = "MemberDetailsGallery";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		memberID = this.getArguments().getString(Services.Statics.MID);
		mLoader = this.getLoaderManager();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.member_details_gallery, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		//mInflater = inflater;
		view = setupView();
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mLoader.restartLoader(0, null, this);
	}
		
	private View setupView() {
		
		GridView gallery = (GridView) view.findViewById(R.id.member_gallery);
		mAdapter = new GalleryViewAdapter(getActivity(), R.layout.member_gallery_row, null,
				new String[] {}, new int[] {}, 130);
		gallery.setAdapter(mAdapter);
		
		Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                9, r.getDisplayMetrics());
 
        int columnWidth = (int) ((Services.getScreenWidth(getActivity()) - ((3 + 1) * padding)) / 3);
 
        gallery.setNumColumns(3);
        gallery.setColumnWidth(columnWidth);
        gallery.setStretchMode(GridView.NO_STRETCH);
        gallery.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gallery.setHorizontalSpacing((int) padding);
        gallery.setVerticalSpacing((int) padding);
		mLoader.initLoader(0, null, this);
		
		LinearLayout addPhoto = (LinearLayout) view.findViewById(R.id.button_add_photo);
		addPhoto.setOnClickListener(this);
		
		return view;
	}
	
	 private void InitilizeGridLayout() {
	           }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader( getActivity(), ContentDescriptor.Image.CONTENT_URI,
				null, ContentDescriptor.Image.Cols.MID+" = ?", new String[] {memberID}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> lodder, Cursor cursor) {
		mAdapter.changeCursor(cursor);		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.button_add_photo):{
			Intent camera = new Intent(getActivity(), CameraWrapper.class);
			camera.putExtra(VisitorsViewAdapter.EXTRA_ID,memberID);
			getActivity().startActivity(camera);
			break;
		}
		}
	}
	
}