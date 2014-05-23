package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.ui.fragments.ManageLocationsFragment;
import com.lbconsulting.alist.utilities.MyLog;

public class ManageLocationsPagerAdaptor extends FragmentStatePagerAdapter {

	private Cursor mAllStoresInActiveListCursor;
	private Context mContext;
	private int mCount;
	private long mActiveListID;

	public ManageLocationsPagerAdaptor(FragmentManager fm, Context context, long activeListID) {
		super(fm);
		MyLog.i("ManageLocationsPagerAdaptor", "Constructor.");
		this.mContext = context;
		this.mActiveListID = activeListID;
		setAllStoresCursor();
	}

	@Override
	public Fragment getItem(int position) {
		long storeID = getStoreID(position);
		MyLog.d("ManageLocationsPagerAdaptor", "getItem - position:" + position + "; storeID:" + storeID);
		ManageLocationsFragment newManageLocationsFragment = ManageLocationsFragment
				.newInstance(mActiveListID, storeID);
		return newManageLocationsFragment;
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
			MyLog.d("ManageLocationsPagerAdaptor", "Exception in getStoreID: " + e);
		}
		return storeID;
	}

}
