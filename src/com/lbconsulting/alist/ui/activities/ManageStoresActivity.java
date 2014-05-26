package com.lbconsulting.alist.ui.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.StoresPagerAdaptor;
import com.lbconsulting.alist.classes.AListEvents.RestartStoresActivity;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.AListContentProvider;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.dialogs.StoresDialogFragment;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class ManageStoresActivity extends FragmentActivity {

	private long mActiveListID = -1;
	private long mActiveStoreID = -1;
	private int mActiveStorePosition = 0;
	private ListSettings mListSettings;

	// private boolean mTwoFragmentLayout;
	private StoresPagerAdaptor mStoresPagerAdapter;
	private ViewPager mPager;
	private Cursor mAllStoresCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("Stores_ACTIVITY", "onCreate");
		super.onCreate(savedInstanceState);

		AListContentProvider.setContext(this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.activity_stores_pager);
		mPager = (ViewPager) findViewById(R.id.storesPager);
		if (mPager == null) {
			// not in portrait orientation ... so finish
			finish();
			return;
		}

		Intent intent = getIntent();
		mActiveListID = intent.getLongExtra("ActiveListID", -1);
		mListSettings = new ListSettings(this, mActiveListID);
		mActiveStoreID = mListSettings.getActiveStoreID();
		mAllStoresCursor = StoresTable.getAllStoresInListCursor(this, mActiveListID, StoresTable.SORT_ORDER_STORE_NAME);

		TextView tvListTitle = (TextView) findViewById(R.id.tvListTitle);
		if (tvListTitle != null) {
			tvListTitle.setText(mListSettings.getListTitle());
			tvListTitle.setBackgroundColor(mListSettings.getTitleBackgroundColor());
			tvListTitle.setTextColor(mListSettings.getTitleTextColor());
		}

		SetStoresPagerAdaptor();

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				SetActiveStoreID(position);
				MyLog.d("Stores_ACTIVITY", "onPageSelected() - position = " + position + " ; storeID = "
						+ mActiveStoreID);

			}
		});
	}

	public void onEvent(RestartStoresActivity event) {
		mActiveStoreID = event.getStoreID();
		RestartStoresActivity();
	}

	private void SetStoresPagerAdaptor() {
		mStoresPagerAdapter = new StoresPagerAdaptor(getSupportFragmentManager(), this, mActiveListID);
		mPager.setAdapter(mStoresPagerAdapter);
	}

	protected void SetActiveStoreID(int position) {
		if (mAllStoresCursor != null) {
			try {
				mAllStoresCursor.moveToPosition(position);
				mActiveStoreID = mAllStoresCursor.getLong(mAllStoresCursor
						.getColumnIndexOrThrow(StoresTable.COL_STORE_ID));
				mActiveStorePosition = position;

				// update the lists table with the selected store ID
				ContentValues newFieldValues = new ContentValues();
				newFieldValues.put(ListsTable.COL_ACTIVE_STORE_ID, mActiveStoreID);
				mListSettings.updateListsTableFieldValues(newFieldValues);

			} catch (Exception e) {
				MyLog.d("Stores_ACTIVITY", "Exception in SetActiveStoreID: " + e);
			}
		}
	}

	@Override
	protected void onStart() {
		MyLog.i("Stores_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("Stores_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("Stores_ACTIVITY", "onResume");
		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mListSettings = new ListSettings(this, mActiveListID);
		mActiveStoreID = mListSettings.getActiveStoreID();
		mAllStoresCursor = StoresTable.getAllStoresInListCursor(this, mActiveListID, StoresTable.SORT_ORDER_STORE_NAME);
		if (mAllStoresCursor != null && mAllStoresCursor.getCount() > 0) {
			if (mActiveStoreID > 1) {
				mActiveStorePosition = AListUtilities.getCursorPositon(mAllStoresCursor, mActiveStoreID);
			} else {
				// there are stores in the list, but we don't have an ActiveStoreID
				// so ... go to the first store
				mActiveStorePosition = 0;
				SetActiveStoreID(mActiveStorePosition);
			}
			mPager.setCurrentItem(mActiveStorePosition);
		} else {
			// there are no stores in this list
			// so... add a new store
			AddNewStore();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("Stores_ACTIVITY", "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("Stores_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("Stores_ACTIVITY", "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.stores_1activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {

			case R.id.action_showStoreLocation:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_newStore:
				AddNewStore();
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				return true;

			case R.id.action_deleteStore:
				DeleteStore();
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				return true;

			case R.id.action_editStoreName:
				EditStoreName();
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	private void AddNewStore() {
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_store_create_edit_delete");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}
		StoresDialogFragment addNewStoreNameDialog = StoresDialogFragment.newInstance(mActiveListID, mActiveStoreID,
				StoresDialogFragment.NEW_STORE);
		addNewStoreNameDialog.show(fm, "dialog_store_create_edit_delete");
	}

	private void DeleteStore() {
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_store_create_edit_delete");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		if (mActiveStoreID > 1) {
			// can't edit the default store
			StoresDialogFragment deleteStoreNameDialog = StoresDialogFragment.newInstance(mActiveListID,
					mActiveStoreID, StoresDialogFragment.DELETE_STORE);
			deleteStoreNameDialog.show(fm, "dialog_store_create_edit_delete");
		}
	}

	private void EditStoreName() {
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_store_create_edit_delete");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		if (mActiveStoreID > 1) {
			// can't edit the default store
			StoresDialogFragment editStoreNameDialog = StoresDialogFragment.newInstance(mActiveListID, mActiveStoreID,
					StoresDialogFragment.EDIT_STORE_NAME);
			editStoreNameDialog.show(fm, "dialog_store_create_edit_delete");
		}
	}

	private void RestartStoresActivity() {
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(ListsTable.COL_ACTIVE_STORE_ID, mActiveStoreID);
		mListSettings.updateListsTableFieldValues(newFieldValues);

		Intent intent = new Intent(this, ManageStoresActivity.class);
		// prohibit the back button from displaying previous version of this StoresActivity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("ActiveListID", mActiveListID);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		MyLog.i("Stores_ACTIVITY", "onDestroy");
		// Unregister since the activity is about to be closed.
		AListContentProvider.setContext(null);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

}
