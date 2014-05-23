package com.lbconsulting.alist.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.utilities.MyLog;

public class AboutActivity extends Activity {

	private boolean mTwoFragmentLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("About_ACTIVITY", "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_temp);
		TextView tvFragmentA = (TextView) this.findViewById(R.id.tvFragmentA);
		TextView tvFragmentB = (TextView) this.findViewById(R.id.tvFragmentB);
		mTwoFragmentLayout = tvFragmentB != null && tvFragmentB.getVisibility() == View.VISIBLE;

		if (mTwoFragmentLayout) {
			tvFragmentA.setText("About Fragment");
			// tvFragmentA.setText(xmlString);
			tvFragmentB.setVisibility(View.GONE);
		} else {
			// tvFragmentA.setText(xmlString);
			tvFragmentA.setText("About Fragment");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1222) {
			MyLog.d("About_ACTIVITY", "requestCode:" + requestCode + " resultCode" + resultCode);
		}
	}

	@Override
	protected void onStart() {
		MyLog.i("About_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("About_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("About_ACTIVITY", "onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("About_ACTIVITY", "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("About_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("About_ACTIVITY", "onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		MyLog.i("About_ACTIVITY", "onDestroy");
		super.onDestroy();
	}
}
