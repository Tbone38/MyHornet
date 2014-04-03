package com.treshna.hornet.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;

import com.treshna.hornet.MainActivity;
import com.treshna.hornet.R;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final MainActivity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(MainActivity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	        mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                //ft.remove(mFragment);
                ft.detach(mFragment);
                ft.commit();
                mFragment = null;
            }
	        
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	    	 mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
	         if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.replace(R.id.content_view, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	        	ft.attach(mFragment);
	        }
	         //mDrawerList.setItemChecked(tab.getPosition()+1, true);
	         mActivity.getDrawerList().setItemChecked(tab.getPosition()+1, true);
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	    	mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
	    	if (mFragment != null) {
	            // Detach the fragment, because another one is being attached	    		
	    		ft.detach(mFragment);
	        } else {
	        	ft.detach(mActivity.getCurrentFragment());
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}