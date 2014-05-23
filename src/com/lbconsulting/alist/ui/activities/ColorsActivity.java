package com.lbconsulting.alist.ui.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.ScrollView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.ColorsPreviewPagerAdapter;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.ui.fragments.ColorsPreviewFragment;
import com.lbconsulting.alist.utilities.MyLog;

public class ColorsActivity extends FragmentActivity implements View.OnClickListener, OnColorChangedListener {

	private ColorsPreviewPagerAdapter mColorsPreviewPagerAdapter;
	private ViewPager mPager;
	private Cursor mAllListsCursor;

	private long mActiveListID = -1;
	private int mActiveListPosition = -1;
	private int mColorsActivitySelectedNavigationIndex = 0;
	private boolean mInhibitColorChangeBroadcast = false;
	private ListSettings mListSettings;

	private ScrollView mPresetsScrollView;
	private ScrollView mPickerScrollView;

	private Button btnPreset0;
	private Button btnPreset1;
	private Button btnPreset2;
	private Button btnPreset3;
	private Button btnPreset4;
	private Button btnPreset5;
	private Button btnApply;

	private Button btnSetTitleBackground;
	private Button btnSetTitleText;
	private Button btnSetListBackground;
	private Button btnSetListNormalText;
	private Button btnSetListStrikeOutText;
	private Button btnSetSeparatorBackground;
	private Button btnSetSeparatorText;

	private int mLastButtonPressedID = 0;

	private ColorPicker picker;
	private SaturationBar saturationBar;
	private SVBar sVBar;
	private BroadcastReceiver mPickerInitialColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("Colors_ACTIVITY", "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_colors);

		mPresetsScrollView = (ScrollView) findViewById(R.id.presetsScrollView);
		mPickerScrollView = (ScrollView) findViewById(R.id.pickerScrollView);

		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", -1);

		picker = (ColorPicker) findViewById(R.id.picker);
		saturationBar = (SaturationBar) findViewById(R.id.saturationBar);
		sVBar = (SVBar) findViewById(R.id.SVBar);

		picker.addSaturationBar(saturationBar);
		picker.addSVBar(sVBar);
		picker.setOnColorChangedListener(this);

		mPickerInitialColor = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra("initialColorPickerColor")) {
					int initialColorPickerColor = intent.getExtras().getInt("initialColorPickerColor", 0);

					picker.setNewCenterColor(initialColorPickerColor);
					picker.setOldCenterColor(initialColorPickerColor);
					mInhibitColorChangeBroadcast = true;
					picker.setColor(initialColorPickerColor);
					mInhibitColorChangeBroadcast = false;
				}
			}

		};

		// Register local broadcast receivers.
		LocalBroadcastManager.getInstance(this).registerReceiver(mPickerInitialColor,
				new IntentFilter(ColorsPreviewFragment.INITIAL_COLOR_BROADCAST_KEY));

		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.action_bar_title_select_list_colors);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// add a tabs to the action bar.
		actionBar.addTab(actionBar.newTab()
				.setText(R.string.actionBar_tab_color_presets)
				.setTabListener(new TabListener() {

					@Override
					public void onTabUnselected(Tab tab, FragmentTransaction ft) {
						// Do nothing
					}

					@Override
					public void onTabSelected(Tab tab, FragmentTransaction ft) {
						mPresetsScrollView.setVisibility(View.VISIBLE);
						mPickerScrollView.setVisibility(View.GONE);
					}

					@Override
					public void onTabReselected(Tab tab, FragmentTransaction ft) {
						// Do nothing
					}
				})
				);
		actionBar.addTab(actionBar.newTab()
				.setText(R.string.actionBar_tab_color_picker)
				.setTabListener(new TabListener() {

					@Override
					public void onTabUnselected(Tab tab, FragmentTransaction ft) {
						// Do nothing
					}

					@Override
					public void onTabSelected(Tab tab, FragmentTransaction ft) {
						mPresetsScrollView.setVisibility(View.GONE);
						mPickerScrollView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onTabReselected(Tab tab, FragmentTransaction ft) {
						// Do nothing
					}
				})
				);

		mAllListsCursor = ListsTable.getAllLists(this);
		mListSettings = new ListSettings(this, mActiveListID);

		mColorsPreviewPagerAdapter = new ColorsPreviewPagerAdapter(getSupportFragmentManager(), this);
		mPager = (ViewPager) findViewById(R.id.colorsPreviewFragmentPager);
		mPager.setAdapter(mColorsPreviewPagerAdapter);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				// A list page has been selected

				// send broadcast to revert colors in fragment to those saved in the database
				String setListSettingsColorsKey = String.valueOf(mActiveListID)
						+ ColorsPreviewFragment.SET_LIST_SETTINGS_COLORS_BROADCAST_KEY;
				Intent setListSettingsColorsIntent = new Intent(setListSettingsColorsKey);
				LocalBroadcastManager.getInstance(ColorsActivity.this).sendBroadcast(setListSettingsColorsIntent);

				// set the ActiveID
				SetActiveListID(position);
				MyLog.d("CheckItems_ACTIVITY", "onPageSelected() - position = " + position + " ; listID = "
						+ mActiveListID);
			}
		});

		btnPreset0 = (Button) findViewById(R.id.btnPreset0);
		btnPreset1 = (Button) findViewById(R.id.btnPreset1);
		btnPreset2 = (Button) findViewById(R.id.btnPreset2);
		btnPreset3 = (Button) findViewById(R.id.btnPreset3);
		btnPreset4 = (Button) findViewById(R.id.btnPreset4);
		btnPreset5 = (Button) findViewById(R.id.btnPreset5);
		btnApply = (Button) findViewById(R.id.btnApply);

		btnSetTitleBackground = (Button) findViewById(R.id.btnSetTitleBackground);
		btnSetTitleText = (Button) findViewById(R.id.btnSetTitleText);
		btnSetListBackground = (Button) findViewById(R.id.btnSetListBackground);
		btnSetListNormalText = (Button) findViewById(R.id.btnSetListNormalText);
		btnSetListStrikeOutText = (Button) findViewById(R.id.btnSetListStrikeOutText);
		btnSetSeparatorBackground = (Button) findViewById(R.id.btnSetSeparatorBackground);
		btnSetSeparatorText = (Button) findViewById(R.id.btnSetSeparatorText);

		btnPreset0.setOnClickListener(this);
		btnPreset1.setOnClickListener(this);
		btnPreset2.setOnClickListener(this);
		btnPreset3.setOnClickListener(this);
		btnPreset4.setOnClickListener(this);
		btnPreset5.setOnClickListener(this);
		btnApply.setOnClickListener(this);

		btnSetTitleBackground.setOnClickListener(this);
		btnSetTitleText.setOnClickListener(this);
		btnSetListBackground.setOnClickListener(this);
		btnSetListNormalText.setOnClickListener(this);
		btnSetListStrikeOutText.setOnClickListener(this);
		btnSetSeparatorBackground.setOnClickListener(this);
		btnSetSeparatorText.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		String setPresetColorsKey = String.valueOf(mActiveListID)
				+ ColorsPreviewFragment.SET_PRESET_COLORS_BROADCAST_KEY;
		Intent setPresetColorsIntent = new Intent(setPresetColorsKey);

		String setViewIdKey = String.valueOf(mActiveListID)
				+ ColorsPreviewFragment.SET_VIEW_BROADCAST_KEY;
		Intent setViewIntent = new Intent(setViewIdKey);

		switch (v.getId()) {

			case R.id.btnPreset0:
				setPresetColorsIntent.putExtra("setPresetColors", ColorsPreviewFragment.SET_PRESET_0_COLORS);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setPresetColorsIntent);
				btnPreset0.setBackgroundResource(R.drawable.preset0_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnPreset0;
				break;

			case R.id.btnPreset1:
				setPresetColorsIntent.putExtra("setPresetColors", ColorsPreviewFragment.SET_PRESET_1_COLORS);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setPresetColorsIntent);
				btnPreset1.setBackgroundResource(R.drawable.preset1_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnPreset1;
				break;

			case R.id.btnPreset2:
				setPresetColorsIntent.putExtra("setPresetColors", ColorsPreviewFragment.SET_PRESET_2_COLORS);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setPresetColorsIntent);
				btnPreset2.setBackgroundResource(R.drawable.preset2_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnPreset2;
				break;

			case R.id.btnPreset3:
				setPresetColorsIntent.putExtra("setPresetColors", ColorsPreviewFragment.SET_PRESET_3_COLORS);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setPresetColorsIntent);
				btnPreset3.setBackgroundResource(R.drawable.preset3_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnPreset3;
				break;

			case R.id.btnPreset4:
				setPresetColorsIntent.putExtra("setPresetColors", ColorsPreviewFragment.SET_PRESET_4_COLORS);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setPresetColorsIntent);
				btnPreset4.setBackgroundResource(R.drawable.preset4_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnPreset4;
				break;

			case R.id.btnPreset5:
				setPresetColorsIntent.putExtra("setPresetColors", ColorsPreviewFragment.SET_PRESET_5_COLORS);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setPresetColorsIntent);
				btnPreset5.setBackgroundResource(R.drawable.preset5_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnPreset5;
				break;

			case R.id.btnApply:
				String applyPresetColorsKey = String.valueOf(mActiveListID)
						+ ColorsPreviewFragment.APPLY_PRESET_COLORS_BROADCAST_KEY;
				Intent applyPresetColorsIntent = new Intent(applyPresetColorsKey);
				LocalBroadcastManager.getInstance(this).sendBroadcast(applyPresetColorsIntent);
				break;

			case R.id.btnSetTitleBackground:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.TITLE_BACKGROUND_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetTitleBackground.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetTitleBackground;
				break;

			case R.id.btnSetTitleText:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.TITLE_TEXT_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetTitleText.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetTitleText;
				break;

			case R.id.btnSetListBackground:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.LIST_BACKGROUND_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetListBackground.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetListBackground;
				break;

			case R.id.btnSetListNormalText:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.LIST_NORMAL_TEXT_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetListNormalText.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetListNormalText;
				break;

			case R.id.btnSetListStrikeOutText:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.LIST_STRIKEOUT_TEXT_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetListStrikeOutText.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetListStrikeOutText;
				break;

			case R.id.btnSetSeparatorBackground:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.SEPARATOR_BACKGROUND_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetSeparatorBackground.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetSeparatorBackground;
				break;

			case R.id.btnSetSeparatorText:
				setViewIntent.putExtra("setViewID", ColorsPreviewFragment.SEPARATOR_TEXT_COLOR);
				LocalBroadcastManager.getInstance(this).sendBroadcast(setViewIntent);
				btnSetSeparatorText.setBackgroundResource(R.drawable.color_picker_background_red_stroke);
				ClearRedStroke(mLastButtonPressedID);
				mLastButtonPressedID = R.id.btnSetSeparatorText;
				break;

			default:
				break;
		}
	}

	private void ClearRedStroke(int lastButtonPressedID) {
		switch (lastButtonPressedID) {
			case R.id.btnPreset0:
				btnPreset0.setBackgroundResource(R.drawable.preset0_background);
				break;

			case R.id.btnPreset1:
				btnPreset1.setBackgroundResource(R.drawable.preset1_background);
				break;

			case R.id.btnPreset2:
				btnPreset2.setBackgroundResource(R.drawable.preset2_background);
				break;

			case R.id.btnPreset3:
				btnPreset3.setBackgroundResource(R.drawable.preset3_background);
				break;

			case R.id.btnPreset4:
				btnPreset4.setBackgroundResource(R.drawable.preset4_background);
				break;

			case R.id.btnPreset5:
				btnPreset5.setBackgroundResource(R.drawable.preset5_background);
				break;

			case R.id.btnSetTitleBackground:
			case R.id.btnSetTitleText:
			case R.id.btnSetListBackground:
			case R.id.btnSetListNormalText:
			case R.id.btnSetListStrikeOutText:
			case R.id.btnSetSeparatorBackground:
			case R.id.btnSetSeparatorText:
				Button btnColorPicker = (Button) findViewById(lastButtonPressedID);
				btnColorPicker.setBackgroundResource(R.drawable.color_picker_background);
				break;

			default:
				break;

		}

	}

	private void SetActiveListID(int position) {
		if (mAllListsCursor != null) {
			try {
				mAllListsCursor.moveToPosition(position);
				mActiveListID = mAllListsCursor.getLong(mAllListsCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));
				mListSettings = new ListSettings(this, mActiveListID);
				mActiveListPosition = position;
			} catch (Exception e) {
				MyLog.d("CheckItems_ACTIVITY", "Exception in getlistID: " + e);
			}
		}
	}

	@Override
	protected void onStart() {
		MyLog.i("Colors_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("Colors_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("Colors_ACTIVITY", "onResume");
		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", -1);
		mColorsActivitySelectedNavigationIndex = storedStates.getInt("ColorsActivitySelectedNavigationIndex", 0);

		if (mActiveListPosition > -1) {
			mPager.setCurrentItem(mActiveListPosition);
		}
		getActionBar().setSelectedNavigationItem(mColorsActivitySelectedNavigationIndex);
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("Colors_ACTIVITY", "onPause");
		SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
		SharedPreferences.Editor applicationStates = preferences.edit();
		applicationStates.putLong("ActiveListID", mActiveListID);
		applicationStates.putInt("ActiveListPosition", mActiveListPosition);
		applicationStates.putInt("ColorsActivitySelectedNavigationIndex", getActionBar().getSelectedNavigationIndex());
		applicationStates.commit();
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("Colors_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("Colors_ACTIVITY", "onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		MyLog.i("Colors_ACTIVITY", "onDestroy");
		if (mAllListsCursor != null) {
			mAllListsCursor.close();
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mPickerInitialColor);
		super.onDestroy();
	}

	@Override
	public void onColorChanged(int color) {

		if (!mInhibitColorChangeBroadcast) {
			String setByColorPickerKey = String.valueOf(mActiveListID)
					+ ColorsPreviewFragment.SET_BY_COLOR_PICKER_BROADCAST_KEY;
			Intent setByColorPickerIntent = new Intent(setByColorPickerKey);
			setByColorPickerIntent.putExtra("colorPickerColor", color);
			LocalBroadcastManager.getInstance(this).sendBroadcast(setByColorPickerIntent);
		}

	}

}
