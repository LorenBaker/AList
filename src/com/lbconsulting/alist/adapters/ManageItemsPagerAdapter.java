package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.ui.fragments.ManageItemsFragment;
import com.lbconsulting.alist.utilities.MyLog;

//FragmentStatePagerAdapter
//FragmentPagerAdapter
public class ManageItemsPagerAdapter extends FragmentStatePagerAdapter {

	private Cursor mAllListsCursor;
	private Context mContext;
	private int mCount;

	public ManageItemsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.mContext = context;
		setAllListsCursor();
	}

	@Override
	public Fragment getItem(int position) {
		long listID = getlistID(position);
		MyLog.d("CheckItemsPagerAdapter", "getItem - position=" + position + "; listID=" + listID);

		SharedPreferences storedStates = mContext.getSharedPreferences("AList", Context.MODE_PRIVATE);
		int tabPosition = storedStates.getInt("ActiveTabPosition", 0);

		Fragment newCheckItemsFragment = ManageItemsFragment.newInstance(listID, tabPosition);
		return newCheckItemsFragment;
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
			MyLog.d("CheckItemsPagerAdapter", "Exception in getlistID: " + e);
		}
		return listID;
	}
}
