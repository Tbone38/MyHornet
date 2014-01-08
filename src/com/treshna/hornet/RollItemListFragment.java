package com.treshna.hornet;


import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class RollItemListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	Cursor cur;
	ContentResolver contentResolver;
	String memberID;
	private View view;
	LayoutInflater mInflater;
	private SimpleCursorAdapter mAdapter;
	private LoaderManager mLoader;
	private int rollId;
		
	//private static final String TAG = "MemberDetailsGallery";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		contentResolver = getActivity().getContentResolver();
		mLoader = this.getLoaderManager();
		rollId = this.getArguments().getInt(Services.Statics.ROLLID);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
	
		view = inflater.inflate(R.layout.roll_item_list, container, false);
		
		mInflater = getActivity().getLayoutInflater();
		
		view = setupView();
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mLoader.restartLoader(0, null, this);
	}
		
	@SuppressWarnings("deprecation")
	private View setupView() {
		String[] from = {"name", ContentDescriptor.RollItem.Cols.ATTENDED};
		int[] to = {R.id.roll_item_name, R.id.roll_item_attended};
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.roll_item_row, null, null, null);
		
        setListAdapter(mAdapter);
		return view;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader( getActivity(), ContentDescriptor.RollItem.CONTENT_URI,
				new String[] {ContentDescriptor.Member.Cols.FNAME+"+' '+"+ContentDescriptor.Member.Cols.SNAME+" AS name",
		"r."+ContentDescriptor.RollItem.Cols._ID, ContentDescriptor.RollItem.Cols.ATTENDED}, 
		ContentDescriptor.RollItem.Cols.ROLLID+" = ?", new String[] {String.valueOf(rollId)}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mAdapter.notifyDataSetChanged();
	}
	
}