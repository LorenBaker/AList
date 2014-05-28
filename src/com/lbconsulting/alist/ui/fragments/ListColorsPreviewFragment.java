package com.lbconsulting.alist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.classes.AListEvents.ActiveColorPickerViewChanged;
import com.lbconsulting.alist.classes.AListEvents.ApplyPresetColors;
import com.lbconsulting.alist.classes.AListEvents.ColorPickerColorChange;
import com.lbconsulting.alist.classes.AListEvents.SetInitialColorPickerColor;
import com.lbconsulting.alist.classes.AListEvents.SetListSettingsColors;
import com.lbconsulting.alist.classes.AListEvents.SetPresetColors;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class ListColorsPreviewFragment extends Fragment {

	private Resources res;

	private long mActiveListID = -1;
	private int mActiveViewID = -1;

	private ListSettings mListSettings;

	private LinearLayout colorsPreviewFragmentLinearLayout;
	private TextView tvListTitle;
	private TextView tvListNormalText;
	private TextView tvListStrikeOutText;
	private TextView tvListItemSeparator;

	private int mTitle_background_color;
	private int mTitle_text_color;
	private int mList_background_color;
	private int mList_normal_text_color;
	private int mList_strikeout_text_color;
	private int mSeparator_background_color;
	private int mSeparator_text_color;

	public static final int SET_PRESET_0_COLORS = 10;
	public static final int SET_PRESET_1_COLORS = 20;
	public static final int SET_PRESET_2_COLORS = 30;
	public static final int SET_PRESET_3_COLORS = 40;
	public static final int SET_PRESET_4_COLORS = 50;
	public static final int SET_PRESET_5_COLORS = 60;

	public static final int TITLE_BACKGROUND_COLOR = 100;
	public static final int TITLE_TEXT_COLOR = 110;
	public static final int LIST_BACKGROUND_COLOR = 120;
	public static final int LIST_NORMAL_TEXT_COLOR = 130;
	public static final int LIST_STRIKEOUT_TEXT_COLOR = 140;
	public static final int SEPARATOR_BACKGROUND_COLOR = 150;
	public static final int SEPARATOR_TEXT_COLOR = 160;

	public ListColorsPreviewFragment() {
		// Empty constructor
	}

	/**
	 * Create a new instance of ColorsPreviewFragment
	 * 
	 * @param itemID
	 * @return ColorsPreviewFragment
	 */
	public static ListColorsPreviewFragment newInstance(long newListID) {

		if (newListID < 2) {
			MyLog.e("ColorsPreviewFragment: newInstance; listID = " + newListID, " is less than 2!!!!");
			return null;
		}
		ListColorsPreviewFragment f = new ListColorsPreviewFragment();
		// Supply listID input as an argument.
		Bundle args = new Bundle();
		args.putLong("listID", newListID);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("ColorsPreviewFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("ColorsPreviewFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Store our listID
		outState.putLong("listID", this.mActiveListID);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("ColorsPreviewFragment", "onCreateView");

		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", 0);
			}
		}

		View view = inflater.inflate(R.layout.frag_colors_preview, container, false);

		mListSettings = new ListSettings(getActivity(), mActiveListID);
		if (mListSettings != null) {

			setListSettingsColors();

			colorsPreviewFragmentLinearLayout = (LinearLayout) view
					.findViewById(R.id.colorsPreviewFragmentLinearLayout);

			tvListTitle = (TextView) view.findViewById(R.id.tvListTitle);
			if (tvListTitle != null) {
				tvListTitle.setText(mListSettings.getListTitle());
			}

			tvListNormalText = (TextView) view.findViewById(R.id.tvListNormalText);

			tvListStrikeOutText = (TextView) view.findViewById(R.id.tvListStrikeOutText);
			if (tvListStrikeOutText != null) {
				tvListStrikeOutText.setPaintFlags(tvListStrikeOutText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}

			tvListItemSeparator = (TextView) view.findViewById(R.id.tvListItemSeparator);

			setAllColors();
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("ColorsPreviewFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		EventBus.getDefault().register(this);
		res = getActivity().getResources();
	}

	public void onEvent(ActiveColorPickerViewChanged event) {
		if (event.getListID() == mActiveListID) {
			mActiveViewID = event.getColorPickerViewID();
			switch (mActiveViewID) {

				case TITLE_BACKGROUND_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mTitle_background_color));
					break;

				case TITLE_TEXT_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mTitle_text_color));
					break;

				case LIST_BACKGROUND_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mList_background_color));
					break;

				case LIST_NORMAL_TEXT_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mList_normal_text_color));
					break;

				case LIST_STRIKEOUT_TEXT_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mList_strikeout_text_color));
					break;

				case SEPARATOR_BACKGROUND_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mSeparator_background_color));
					break;

				case SEPARATOR_TEXT_COLOR:
					EventBus.getDefault().post(new SetInitialColorPickerColor(mSeparator_text_color));
					break;

				default:
					break;
			}
		}
	}

	public void onEvent(SetListSettingsColors event) {
		if (event.getListID() == mActiveListID) {
			setListSettingsColors();
			setAllColors();
		}
	}

	public void onEvent(ApplyPresetColors event) {
		if (event.getListID() == mActiveListID) {
			applyColorsToListSettings();

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// set title
			builder.setTitle(R.string.dialog_title_colors_applied);

			String msg = "Colors for " + "\"" + mListSettings.getListTitle() + "\" saved.";
			// set dialog message
			builder
					.setMessage(msg)
					.setCancelable(false)
					.setPositiveButton(R.string.btn_ok_text, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							// do nothing
						}
					});
			// create alert dialog
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	public void onEvent(SetPresetColors event) {
		if (event.getListID() == mActiveListID) {
			int presetcolorValue = event.getPresetcolorValue();
			switch (presetcolorValue) {

				case SET_PRESET_0_COLORS:
					setPreset0colors();
					setAllColors();
					break;

				case SET_PRESET_1_COLORS:
					setPreset1colors();
					setAllColors();
					break;

				case SET_PRESET_2_COLORS:
					setPreset2colors();
					setAllColors();
					break;

				case SET_PRESET_3_COLORS:
					setPreset3colors();
					setAllColors();
					break;

				case SET_PRESET_4_COLORS:
					setPreset4colors();
					setAllColors();
					break;

				case SET_PRESET_5_COLORS:
					setPreset5colors();
					setAllColors();
					break;

				default:
					break;
			}
		}
	}

	public void onEvent(ColorPickerColorChange event) {
		if (event.getListID() == mActiveListID) {
			switch (mActiveViewID) {

				case TITLE_BACKGROUND_COLOR:
					mTitle_background_color = event.getColorPickerColor();
					setTitleBackgroundColor();
					break;

				case TITLE_TEXT_COLOR:
					mTitle_text_color = event.getColorPickerColor();
					setTitleTextColor();
					break;

				case LIST_BACKGROUND_COLOR:
					mList_background_color = event.getColorPickerColor();
					setPreviewFragmentLinearLayoutBackgroundColor();
					setListBackgroundColor();
					break;

				case LIST_NORMAL_TEXT_COLOR:
					mList_normal_text_color = event.getColorPickerColor();
					setItemNormalTextColor();
					break;

				case LIST_STRIKEOUT_TEXT_COLOR:
					mList_strikeout_text_color = event.getColorPickerColor();
					setItemStrikeoutTextColor();
					break;

				case SEPARATOR_BACKGROUND_COLOR:
					mSeparator_background_color = event.getColorPickerColor();
					setSeparatorBackgroundColor();
					break;

				case SEPARATOR_TEXT_COLOR:
					mSeparator_text_color = event.getColorPickerColor();
					setSeparatorTextColor();
					break;

				default:
					break;
			}
		}
	}

	@Override
	public void onStart() {
		MyLog.i("ColorsPreviewFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		MyLog.i("ColorsPreviewFragment", "onResume");
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("listID", 0);
		}

		mListSettings = new ListSettings(getActivity(), mActiveListID);
	}

	@Override
	public void onPause() {
		MyLog.i("ColorsPreviewFragment", "onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		MyLog.i("ColorsPreviewFragment", "onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		MyLog.i("ColorsPreviewFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		MyLog.i("ColorsPreviewFragment", "onDestroy");
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		MyLog.i("ColorsPreviewFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public View getView() {
		MyLog.i("ColorsPreviewFragment", "getView");
		return super.getView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		MyLog.i("ColorsPreviewFragment", "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setAllColors() {
		setPreviewFragmentLinearLayoutBackgroundColor();
		setTitleBackgroundColor();
		setTitleTextColor();
		setListBackgroundColor();
		setItemNormalTextColor();
		setItemStrikeoutTextColor();
		setSeparatorBackgroundColor();
		setSeparatorTextColor();
	}

	private void setPreviewFragmentLinearLayoutBackgroundColor() {
		if (colorsPreviewFragmentLinearLayout != null) {
			colorsPreviewFragmentLinearLayout.setBackgroundColor(mList_background_color);
		}
	}

	private void setTitleBackgroundColor() {
		if (tvListTitle != null) {
			tvListTitle.setBackgroundColor(mTitle_background_color);
		}
	}

	private void setTitleTextColor() {
		if (tvListTitle != null) {
			tvListTitle.setTextColor(mTitle_text_color);
		}
	}

	private void setListBackgroundColor() {
		if (tvListNormalText != null && tvListStrikeOutText != null) {
			tvListNormalText.setBackgroundColor(mList_background_color);
			tvListStrikeOutText.setBackgroundColor(mList_background_color);
		}
	}

	private void setItemNormalTextColor() {
		if (tvListNormalText != null) {
			tvListNormalText.setTextColor(mList_normal_text_color);
		}
	}

	private void setItemStrikeoutTextColor() {
		if (tvListStrikeOutText != null) {
			tvListStrikeOutText.setTextColor(mList_strikeout_text_color);
		}
	}

	private void setSeparatorBackgroundColor() {
		if (tvListItemSeparator != null) {
			tvListItemSeparator.setBackgroundColor(mSeparator_background_color);
		}
	}

	private void setSeparatorTextColor() {
		if (tvListItemSeparator != null) {
			tvListItemSeparator.setTextColor(mSeparator_text_color);
		}
	}

	private void applyColorsToListSettings() {

		ContentValues values = new ContentValues();
		values.put(ListsTable.COL_TITLE_BACKGROUND_COLOR, mTitle_background_color);
		values.put(ListsTable.COL_TITLE_TEXT_COLOR, mTitle_text_color);
		values.put(ListsTable.COL_LIST_BACKGROUND_COLOR, mList_background_color);
		values.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR, mList_normal_text_color);
		values.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR, mList_strikeout_text_color);
		values.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR, mList_background_color);
		values.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR, mList_normal_text_color);
		values.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR, mList_strikeout_text_color);
		values.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR, mSeparator_background_color);
		values.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, mSeparator_text_color);

		ListsTable.UpdateListsTableFieldValues(getActivity(), mActiveListID, values);
		mListSettings = new ListSettings(getActivity(), mActiveListID);
	}

	private void setListSettingsColors() {
		mTitle_background_color = mListSettings.getTitleBackgroundColor();
		mTitle_text_color = mListSettings.getTitleTextColor();
		mList_background_color = mListSettings.getListBackgroundColor();
		mList_normal_text_color = mListSettings.getItemNormalTextColor();
		mList_strikeout_text_color = mListSettings.getItemStrikeoutTextColor();
		mSeparator_background_color = mListSettings.getSeparatorBackgroundColor();
		mSeparator_text_color = mListSettings.getSeparatorTextColor();
	}

	private void setPreset0colors() {
		mTitle_background_color = res.getColor(R.color.preset0_title_background);
		mTitle_text_color = res.getColor(R.color.preset0_title_text);
		mList_background_color = res.getColor(R.color.preset0_list_background);
		mList_normal_text_color = res.getColor(R.color.preset0_list_normal_text);
		mList_strikeout_text_color = res.getColor(R.color.preset0_list_strikeout_text);
		mSeparator_background_color = res.getColor(R.color.preset0_separator_background);
		mSeparator_text_color = res.getColor(R.color.preset0_separator_text);
	}

	private void setPreset1colors() {
		mTitle_background_color = res.getColor(R.color.preset1_title_background);
		mTitle_text_color = res.getColor(R.color.preset1_title_text);
		mList_background_color = res.getColor(R.color.preset1_list_background);
		mList_normal_text_color = res.getColor(R.color.preset1_list_normal_text);
		mList_strikeout_text_color = res.getColor(R.color.preset1_list_strikeout_text);
		mSeparator_background_color = res.getColor(R.color.preset1_separator_background);
		mSeparator_text_color = res.getColor(R.color.preset1_separator_text);
	}

	private void setPreset2colors() {
		mTitle_background_color = res.getColor(R.color.preset2_title_background);
		mTitle_text_color = res.getColor(R.color.preset2_title_text);
		mList_background_color = res.getColor(R.color.preset2_list_background);
		mList_normal_text_color = res.getColor(R.color.preset2_list_normal_text);
		mList_strikeout_text_color = res.getColor(R.color.preset2_list_strikeout_text);
		mSeparator_background_color = res.getColor(R.color.preset2_separator_background);
		mSeparator_text_color = res.getColor(R.color.preset2_separator_text);
	}

	private void setPreset3colors() {
		mTitle_background_color = res.getColor(R.color.preset3_title_background);
		mTitle_text_color = res.getColor(R.color.preset3_title_text);
		mList_background_color = res.getColor(R.color.preset3_list_background);
		mList_normal_text_color = res.getColor(R.color.preset3_list_normal_text);
		mList_strikeout_text_color = res.getColor(R.color.preset3_list_strikeout_text);
		mSeparator_background_color = res.getColor(R.color.preset3_separator_background);
		mSeparator_text_color = res.getColor(R.color.preset3_separator_text);
	}

	private void setPreset4colors() {
		mTitle_background_color = res.getColor(R.color.preset4_title_background);
		mTitle_text_color = res.getColor(R.color.preset4_title_text);
		mList_background_color = res.getColor(R.color.preset4_list_background);
		mList_normal_text_color = res.getColor(R.color.preset4_list_normal_text);
		mList_strikeout_text_color = res.getColor(R.color.preset4_list_strikeout_text);
		mSeparator_background_color = res.getColor(R.color.preset4_separator_background);
		mSeparator_text_color = res.getColor(R.color.preset4_separator_text);
	}

	private void setPreset5colors() {
		mTitle_background_color = res.getColor(R.color.preset5_title_background);
		mTitle_text_color = res.getColor(R.color.preset5_title_text);
		mList_background_color = res.getColor(R.color.preset5_list_background);
		mList_normal_text_color = res.getColor(R.color.preset5_list_normal_text);
		mList_strikeout_text_color = res.getColor(R.color.preset5_list_strikeout_text);
		mSeparator_background_color = res.getColor(R.color.preset5_separator_background);
		mSeparator_text_color = res.getColor(R.color.preset5_separator_text);
	}

}
