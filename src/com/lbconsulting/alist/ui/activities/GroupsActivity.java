package com.lbconsulting.alist.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.utilities.MyLog;

public class GroupsActivity extends Activity {

	private long mActiveListID = -1;
	private boolean mTwoFragmentLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("Groups_ACTIVITY", "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_temp);
		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		TextView tvFragmentA = (TextView) this.findViewById(R.id.tvFragmentA);
		TextView tvFragmentB = (TextView) this.findViewById(R.id.tvFragmentB);
		mTwoFragmentLayout = tvFragmentB != null && tvFragmentB.getVisibility() == View.VISIBLE;

		if (mTwoFragmentLayout) {
			StartStoresActivity();
		} else {
			tvFragmentA.setText("Groups Fragment;\nListID = " + mActiveListID);
		}
	}

	@Override
	protected void onStart() {
		MyLog.i("Groups_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("Groups_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("Groups_ACTIVITY", "onResume");
		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("Groups_ACTIVITY", "onPause");
		SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
		SharedPreferences.Editor applicationStates = preferences.edit();
		applicationStates.putLong("ActiveListID", mActiveListID);
		applicationStates.commit();
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("Groups_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("Groups_ACTIVITY", "onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		MyLog.i("Groups_ACTIVITY", "onDestroy");
		super.onDestroy();
	}

	private void StartStoresActivity() {
		Intent intent = new Intent(this, StoresActivity.class);
		startActivity(intent);
	}
}
