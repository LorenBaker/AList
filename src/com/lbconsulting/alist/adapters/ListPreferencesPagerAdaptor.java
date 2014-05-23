package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.ui.fragments.ListPreferencesFragment;
import com.lbconsulting.alist.utilities.MyLog;

//FragmentPagerAdapter
//FragmentStatePagerAdapter

public class ListPreferencesPagerAdaptor extends FragmentStatePagerAdapter {

	private Cursor mAllListsCursor;
	private Context mContext;
	private int mCount;

	public ListPreferencesPagerAdaptor(FragmentManager fm, Context context) {
		super(fm);
		MyLog.i("ListPreferencesPagerAdaptor", "Constructor.");
		this.mContext = context;
		setAllListsCursor();
	}

	@Override
	public Fragment getItem(int position) {
		long listID = getlistID(position);
		MyLog.d("ListPreferencesPagerAdaptor", "getItem - position=" + position + "; listID=" + listID);
		ListPreferencesFragment newListPreferencesFragment = ListPreferencesFragment.newInstance(listID);
		return newListPreferencesFragment;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	private void setAllListsCursor() {
		mAllListsCursor = ListsTable.getAllLists(mContext);
		mCount = mAllListsCursor.getCount();
	}

	private long getlistID(int position) {
		long listID = -1;
		try {
			mAllListsCursor.moveToPosition(position);
			listID = mAllListsCursor.getLong(mAllListsCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));
		} catch (Exception e) {
			MyLog.d("ListPreferencesPagerAdaptor", "Exception in getlistID: " + e);
		}
		return listID;
	}

}
