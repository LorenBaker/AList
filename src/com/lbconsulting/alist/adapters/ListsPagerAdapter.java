package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.ui.fragments.ListsFragment;
import com.lbconsulting.alist.utilities.MyLog;

//FragmentStatePagerAdapter
//FragmentPagerAdapter
public class ListsPagerAdapter extends FragmentStatePagerAdapter {

	private Cursor mAllListsCursor;
	private Context mContext;
	private int mCount;

	public ListsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.mContext = context;
		setAllListsCursor();
	}

	@Override
	public Fragment getItem(int position) {
		long listID = getlistID(position);
		MyLog.d("ListsPagerAdapter", "getItem - position=" + position + "; listID=" + listID);
		Fragment newListsFragment = ListsFragment.newInstance(listID);
		return newListsFragment;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	private Cursor setAllListsCursor() {
		mAllListsCursor = ListsTable.getAllLists(mContext);
		mCount = mAllListsCursor.getCount();
		return mAllListsCursor;
	}

	private long getlistID(int position) {
		long listID = -1;
		try {
			mAllListsCursor.moveToPosition(position);
			listID = mAllListsCursor.getLong(mAllListsCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));
		} catch (Exception e) {
			MyLog.d("ListsPagerAdapter", "Exception in getlistID: " + e);
		}
		return listID;
	}
}
