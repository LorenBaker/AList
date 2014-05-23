package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.ui.fragments.StoresFragment;
import com.lbconsulting.alist.utilities.MyLog;

//FragmentPagerAdapter
//FragmentStatePagerAdapter

public class StoresPagerAdaptor extends FragmentStatePagerAdapter {

	private Cursor mAllStoresInActiveListCursor;
	private Context mContext;
	private int mCount;
	private long mActiveListID;

	public StoresPagerAdaptor(FragmentManager fm, Context context, long activeListID) {
		super(fm);
		MyLog.i("StoresPagerAdaptor", "Constructor.");
		this.mContext = context;
		this.mActiveListID = activeListID;
		setAllStoresCursor();
	}

	@Override
	public Fragment getItem(int position) {
		long storeID = getStoreID(position);
		MyLog.d("StoresPagerAdaptor", "getItem - position:" + position + "; storeID:" + storeID);
		StoresFragment newStoresFragment = StoresFragment.newInstance(mActiveListID, storeID);
		return newStoresFragment;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	private void setAllStoresCursor() {
		mAllStoresInActiveListCursor = StoresTable.getAllStoresInListCursor(mContext, mActiveListID,
				StoresTable.SORT_ORDER_STORE_NAME);
		mCount = mAllStoresInActiveListCursor.getCount();
	}

	private long getStoreID(int position) {
		long storeID = -1;
		try {
			mAllStoresInActiveListCursor.moveToPosition(position);
			storeID = mAllStoresInActiveListCursor.getLong(mAllStoresInActiveListCursor
					.getColumnIndexOrThrow(StoresTable.COL_STORE_ID));
		} catch (Exception e) {
			MyLog.d("StoresPagerAdaptor", "Exception in getStoreID: " + e);
		}
		return storeID;
	}

}
