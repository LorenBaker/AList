package com.lbconsulting.alist.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.ListPreferencesPagerAdaptor;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class ListPreferencesActivity extends FragmentActivity {

	private long mActiveListID = -1;
	private int mActiveListPosition = 0;
	private boolean mTwoFragmentLayout;
	private ListPreferencesPagerAdaptor mListPreferencesPagerAdapter;
	private ViewPager mPager;
	private Cursor mAllListsCursor;
	private BroadcastReceiver mListTitleChanged;
	public static final String LIST_TITLE_CHANGE_BROADCAST_KEY = "listTitleChanged";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("ListPreferences_ACTIVITY", "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list_preferences_pager);

		View frag_colors_placeholder = this.findViewById(R.id.frag_colors_placeholder);
		mTwoFragmentLayout = frag_colors_placeholder != null
				&& frag_colors_placeholder.getVisibility() == View.VISIBLE;

		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", 0);

		mPager = (ViewPager) findViewById(R.id.listPreferencesPager);
		SetListPreferencesPagerAdaptor();

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				SetActiveListID(position);
				MyLog.d("ListPreferences_ACTIVITY", "onPageSelected() - position = " + position + " ; listID = "
						+ mActiveListID);

				if (mTwoFragmentLayout) {
					LoadColorsFragment();
				}
			}
		});

		mListTitleChanged = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// the list title has changed ...
				// restart activity to ensure that all lists are shown in alphabetical order
				ReStartListPreferencesActivity();
			}
		};
		// Register to receive messages.
		// We are registering an observer (mPreferencesChangedBroadcastReceiver) to receive Intents
		// with actions named "list_preferences_changed".
		String key = String.valueOf(mActiveListID) + LIST_TITLE_CHANGE_BROADCAST_KEY;
		LocalBroadcastManager.getInstance(this).registerReceiver(mListTitleChanged, new IntentFilter(key));

		if (mTwoFragmentLayout) {
			LoadColorsFragment();
		}
	}

	private void ReStartListPreferencesActivity() {
		mAllListsCursor = ListsTable.getAllLists(this);
		mActiveListPosition = AListUtilities.getCursorPositon(mAllListsCursor, mActiveListID);
		Intent intent = new Intent(this, ListPreferencesActivity.class);
		// prohibit the back button from displaying previous version of this ListPreferencesActivity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void SetListPreferencesPagerAdaptor() {
		mAllListsCursor = ListsTable.getAllLists(this);
		mListPreferencesPagerAdapter = new ListPreferencesPagerAdaptor(getSupportFragmentManager(), this);
		mPager.setAdapter(mListPreferencesPagerAdapter);
	}

	private void LoadColorsFragment() {
		// TODO code LoadColorsFragment

	}

	protected void SetActiveListID(int position) {
		if (mAllListsCursor != null) {
			long listID = -1;
			try {
				mAllListsCursor.moveToPosition(position);
				listID = mAllListsCursor.getLong(mAllListsCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));
			} catch (Exception e) {
				MyLog.d("ListPreferences_ACTIVITY", "Exception in SetActiveListID: " + e);
			}
			mActiveListID = listID;
			mActiveListPosition = position;
		}
	}

	@Override
	protected void onStart() {
		MyLog.i("ListPreferences_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("ListPreferences_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("ListPreferences_ACTIVITY", "onResume");
		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", 0);
		mPager.setCurrentItem(mActiveListPosition);
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("ListPreferences_ACTIVITY", "onPause");
		SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
		SharedPreferences.Editor applicationStates = preferences.edit();
		applicationStates.putLong("ActiveListID", mActiveListID);
		applicationStates.putInt("ActiveListPosition", mActiveListPosition);
		applicationStates.commit();
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("ListPreferences_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("ListPreferences_ACTIVITY", "onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		MyLog.i("ListPreferences_ACTIVITY", "onDestroy");
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mListTitleChanged);
		super.onDestroy();
	}

}
