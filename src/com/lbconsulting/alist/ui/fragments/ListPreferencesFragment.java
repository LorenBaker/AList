package com.lbconsulting.alist.ui.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.ui.activities.ListColorsActivity;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class ListPreferencesFragment extends Fragment {

	private long mActiveListID;
	private ListSettings listSettings;

	private LinearLayout llFragListPreferences;
	private TextView tvListTitle;

	// private Button btnEditListTitle;

	private RadioButton rbListsViewAlphabetical;
	private RadioButton rbListsViewGroup;
	private RadioButton rbListsViewManual;
	private RadioButton rbListsViewStoreLocation;

	private Switch swDeleteNoteUponClearingItem;
	private Switch swSyncToDropbox;
	private Switch swSyncListPrefToDropbox;

	private RadioGroup rbGroupListsView;
	private RadioGroup rbGroupMasterListsView;

	private RadioButton rbMasterListViewAlphabetical;
	private RadioButton rbMasterListViewGroup;
	private RadioButton rbMasterListViewLastUsed;
	private RadioButton rbMasterListViewSelectedAtTop;
	private RadioButton rbMasterListViewSelectedAtBottom;

	private Button btnColors;

	public ListPreferencesFragment() {
		// Empty constructor
	}

	public static ListPreferencesFragment newInstance(long newListID) {
		if (newListID < 2) {
			MyLog.e("ListPreferencesFragment: newInstance; listID = " + newListID, " is less than 2!!!!");
			return null;
		}

		ListPreferencesFragment f = new ListPreferencesFragment();
		// Supply listID input as an argument.
		Bundle args = new Bundle();
		args.putLong("listID", newListID);
		f.setArguments(args);
		return f;

	}

	private boolean checkListID(String method) {
		if (mActiveListID < 2) {
			MyLog.e("ListPreferencesFragment", method + "; listID = " + mActiveListID + " is less than 2!!!!");
		} else {
			MyLog.i("ListPreferencesFragment", method + "; listID = " + mActiveListID);
		}
		return (mActiveListID > 1);
	}

	@Override
	public void onAttach(Activity activity) {
		checkListID("onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		checkListID("onCreate");
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
		checkListID("onCreateView");
		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", 0);
			}
		}
		View view = inflater.inflate(R.layout.frag_list_preferences, container, false);
		if (view != null && mActiveListID > 1) {

			listSettings = new ListSettings(getActivity(), mActiveListID);

			llFragListPreferences = (LinearLayout) view.findViewById(R.id.llFragListPreferences);
			tvListTitle = (TextView) view.findViewById(R.id.tvListTitle);

			// btnEditListTitle = (Button) view.findViewById(R.id.btnEditListTitle);

			rbGroupListsView = (RadioGroup) view.findViewById(R.id.rbGroupListsView);
			rbGroupMasterListsView = (RadioGroup) view.findViewById(R.id.rbGroupMasterListsView);

			rbListsViewAlphabetical = (RadioButton) view.findViewById(R.id.rbListsViewAlphabetical);
			rbListsViewGroup = (RadioButton) view.findViewById(R.id.rbListsViewGroup);
			rbListsViewManual = (RadioButton) view.findViewById(R.id.rbListsViewManual);
			rbListsViewStoreLocation = (RadioButton) view.findViewById(R.id.rbListsViewStoreLocation);

			swDeleteNoteUponClearingItem = (Switch) view.findViewById(R.id.swDeleteNoteUponClearingItem);
			swSyncToDropbox = (Switch) view.findViewById(R.id.swSyncToDropbox);
			swSyncListPrefToDropbox = (Switch) view.findViewById(R.id.swSyncListPrefToDropbox);

			rbMasterListViewAlphabetical = (RadioButton) view.findViewById(R.id.rbMasterListViewAlphabetical);
			rbMasterListViewGroup = (RadioButton) view.findViewById(R.id.rbMasterListViewGroup);
			rbMasterListViewLastUsed = (RadioButton) view.findViewById(R.id.rbMasterListViewLastUsed);
			rbMasterListViewSelectedAtTop = (RadioButton) view.findViewById(R.id.rbMasterListViewSelectedAtTop);
			rbMasterListViewSelectedAtBottom = (RadioButton) view.findViewById(R.id.rbMasterListViewSelectedAtBottom);

			btnColors = (Button) view.findViewById(R.id.btnColors);
			fillListPreferencesViews();

		} else {
			MyLog.e("ListPreferencesFragment: onActivityCreated after arguments set; listID = " + mActiveListID,
					" is less than 2!!!!");
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		checkListID("onActivityCreated");

		// btnEditListTitle.setOnClickListener(buttonClick);
		btnColors.setOnClickListener(buttonClick);

		swDeleteNoteUponClearingItem.setOnCheckedChangeListener(switchOnCheckedChanged);
		swSyncToDropbox.setOnCheckedChangeListener(switchOnCheckedChanged);
		swSyncListPrefToDropbox.setOnCheckedChangeListener(switchOnCheckedChanged);

		getActivity().getActionBar().setTitle("List Preferences");

		rbGroupListsView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				ContentValues newFieldValues = new ContentValues();

				switch (checkedId) {
					case R.id.rbListsViewAlphabetical:
						newFieldValues.put(ListsTable.COL_LIST_SORT_ORDER, AListUtilities.LIST_SORT_ALPHABETICAL);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbListsViewGroup:
						newFieldValues.put(ListsTable.COL_LIST_SORT_ORDER, AListUtilities.LIST_SORT_BY_GROUP);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbListsViewManual:
						newFieldValues.put(ListsTable.COL_LIST_SORT_ORDER, AListUtilities.LIST_SORT_MANUAL);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbListsViewStoreLocation:
						newFieldValues.put(ListsTable.COL_LIST_SORT_ORDER, AListUtilities.LIST_SORT_BY_STORE_LOCATION);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					default:
						break;
				}
			}
		});

		rbGroupMasterListsView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				ContentValues newFieldValues = new ContentValues();

				switch (checkedId) {
					case R.id.rbMasterListViewAlphabetical:
						newFieldValues.put(ListsTable.COL_MASTER_LIST_SORT_ORDER,
								AListUtilities.MASTER_LIST_SORT_ALPHABETICAL);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbMasterListViewGroup:
						newFieldValues.put(ListsTable.COL_MASTER_LIST_SORT_ORDER,
								AListUtilities.MASTER_LIST_SORT_BY_GROUP);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbMasterListViewLastUsed:
						newFieldValues.put(ListsTable.COL_MASTER_LIST_SORT_ORDER,
								AListUtilities.MASTER_LIST_SORT_BY_LAST_USED);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbMasterListViewSelectedAtTop:
						newFieldValues.put(ListsTable.COL_MASTER_LIST_SORT_ORDER,
								AListUtilities.MASTER_LIST_SORT_SELECTED_AT_TOP);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					case R.id.rbMasterListViewSelectedAtBottom:
						newFieldValues.put(ListsTable.COL_MASTER_LIST_SORT_ORDER,
								AListUtilities.MASTER_LIST_SORT_SELECTED_AT_BOTTOM);
						listSettings.updateListsTableFieldValues(newFieldValues);
						break;

					default:
						break;
				}
			}
		});

		super.onActivityCreated(savedInstanceState);
	}

	View.OnClickListener buttonClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			FragmentManager fm = getActivity().getSupportFragmentManager();
			// Remove any currently showing dialog
			Fragment prev = fm.findFragmentByTag("dialog_lists_table_update");
			if (prev != null) {
				FragmentTransaction ft = fm.beginTransaction();
				ft.remove(prev);
				ft.commit();
			}
			switch (v.getId()) {
			/*				case R.id.btnEditListTitle:
								// Toast.makeText(getActivity(), "\"" + "btnEditListTitle" + "\"" + " is under construction.",
								// Toast.LENGTH_SHORT).show();
								ListsDialogFragment editListTitleDialog = ListsDialogFragment.newInstance(mActiveListID,
										ListsDialogFragment.EDIT_LIST_TITLE);
								editListTitleDialog.show(fm, "dialog_lists_table_update");
								break;*/

				case R.id.btnColors:
					StartColorsActivity();
					// Toast.makeText(getActivity(), "\"" + "btnColors" + "\"" + " is under construction.",
					// Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
			}
		}
	};

	Switch.OnCheckedChangeListener switchOnCheckedChanged = new Switch.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ContentValues newFieldValues = new ContentValues();
			int checkedValue = AListUtilities.boolToInt(isChecked);
			switch (buttonView.getId()) {

				case R.id.swDeleteNoteUponClearingItem:
					newFieldValues.put(ListsTable.COL_DELETE_NOTE_UPON_DESELECTING_ITEM, checkedValue);
					break;

				case R.id.swSyncToDropbox:
					newFieldValues.put(ListsTable.COL_IS_SYNCED_TO_DROPBOX, checkedValue);

					if (isChecked) {
						swSyncListPrefToDropbox.setEnabled(true);
						newFieldValues.put(ListsTable.COL_IS_FIRST_TIME_SYNC, checkedValue);
					} else {
						newFieldValues.put(ListsTable.COL_IS_LIST_PREF_SYNCED_TO_DROPBOX, 0);
						swSyncListPrefToDropbox.setChecked(false);
						swSyncListPrefToDropbox.setEnabled(false);
					}

					/*if (isChecked) {
						// start the Lists Activity to initialize dropbox
						Intent listsActivtyIntent = new Intent(getActivity(), ListsActivity.class);
						startActivity(listsActivtyIntent);
					}*/
					break;

				case R.id.swSyncListPrefToDropbox:
					newFieldValues.put(ListsTable.COL_IS_LIST_PREF_SYNCED_TO_DROPBOX, checkedValue);

					break;

				default:
					newFieldValues = null;
					break;
			}
			if (newFieldValues != null && newFieldValues.size() > 0) {
				listSettings.updateListsTableFieldValues(newFieldValues);
			}
		}
	};

	private void StartColorsActivity() {
		Intent colorsActivityIntent = new Intent(getActivity(), ListColorsActivity.class);
		getActivity().startActivity(colorsActivityIntent);
	}

	private void fillListPreferencesViews() {
		if (listSettings != null) {
			if (llFragListPreferences != null) {
				llFragListPreferences.setBackgroundColor(listSettings.getListBackgroundColor());
			}

			if (tvListTitle != null) {
				setListTitle(listSettings.getListTitle());
				tvListTitle.setBackgroundColor(listSettings.getTitleBackgroundColor());
				tvListTitle.setTextColor(listSettings.getTitleTextColor());
			}

			if (swSyncToDropbox != null) {
				swSyncToDropbox.setChecked(listSettings.isListSyncedToDropbox());
				swSyncToDropbox.setTextColor(listSettings.getItemNormalTextColor());
			}

			if (swDeleteNoteUponClearingItem != null) {
				swDeleteNoteUponClearingItem.setChecked(listSettings.getDeleteNoteUponDeselectingItem());
				swDeleteNoteUponClearingItem.setTextColor(listSettings.getItemNormalTextColor());
			}

			if (rbGroupListsView != null) {
				rbGroupListsView.setBackgroundColor(listSettings.getListBackgroundColor());
				int postition = listSettings.getListSortOrder();
				switch (postition) {
					case AListUtilities.LIST_SORT_ALPHABETICAL:
						rbListsViewAlphabetical.setChecked(true);
						break;

					case AListUtilities.LIST_SORT_BY_GROUP:
						rbListsViewGroup.setChecked(true);
						break;

					case AListUtilities.LIST_SORT_MANUAL:
						rbListsViewManual.setChecked(true);
						break;

					case AListUtilities.LIST_SORT_BY_STORE_LOCATION:
						rbListsViewStoreLocation.setChecked(true);
						break;

					default:
						break;
				}
			}

			if (rbGroupMasterListsView != null) {
				rbGroupMasterListsView.setBackgroundColor(listSettings.getListBackgroundColor());
				int postition = listSettings.getMasterListSortOrder();
				switch (postition) {
					case AListUtilities.MASTER_LIST_SORT_ALPHABETICAL:
						rbMasterListViewAlphabetical.setChecked(true);
						break;

					case AListUtilities.MASTER_LIST_SORT_BY_GROUP:
						rbMasterListViewGroup.setChecked(true);
						break;

					case AListUtilities.MASTER_LIST_SORT_BY_LAST_USED:
						rbMasterListViewLastUsed.setChecked(true);
						break;

					case AListUtilities.MASTER_LIST_SORT_SELECTED_AT_TOP:
						rbMasterListViewSelectedAtTop.setChecked(true);
						break;

					case AListUtilities.MASTER_LIST_SORT_SELECTED_AT_BOTTOM:
						rbMasterListViewSelectedAtBottom.setChecked(true);
						break;

					default:
						break;
				}
			}

			/*			if (btnEditListTitle != null) {
							btnEditListTitle.setTextColor(listSettings.getItemNormalTextColor());
						}*/

			if (btnColors != null) {
				btnColors.setTextColor(listSettings.getItemNormalTextColor());
			}
		}
	}

	private void setListTitle(String newListTitle) {
		if (tvListTitle != null) {
			tvListTitle.setText(newListTitle);
		}
	}

	/*	private void setListSortOrder(int newListSortOrder) {
			if (btnListSortOrder != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("List Sort Order (");
				switch (newListSortOrder) {
							case BY_GROUP:
								sb.append("By Group)");
								break;

				case MANUAL:
					sb.append("Manual)");
					break;
				default:
					// ALPHABETICAL_LIST_SORT_ORDER
					sb.append("Alphabetical)");
					break;
				}
				btnListSortOrder.setText(sb.toString());
			}
		}*/

	/*	private void setMasterListSortOrder(int newMasterListSortOrder) {
			if (btnMasterListSortOrder != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Master List Sort Order (");
				switch (newMasterListSortOrder) {
							case BY_GROUP:
								sb.append("By Group)");
								break;

				case SELECTED_AT_TOP:
					sb.append("Selected at Top)");
					break;

				case SELECTED_AT_BOTTOM:
					sb.append("Selected at Bottom)");
					break;

				case LAST_USED:
					sb.append("Last Used)");
					break;

				default:
					// ALPHABETICAL_LIST_SORT_ORDER
					sb.append("Alphabetical)");
					break;
				}
				btnMasterListSortOrder.setText(sb.toString());
			}
		}*/

	@Override
	public void onStart() {
		checkListID("onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		checkListID("onResume");
		listSettings = new ListSettings(getActivity(), mActiveListID);
		fillListPreferencesViews();
		super.onResume();
	}

	@Override
	public void onPause() {
		checkListID("onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		checkListID("onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		checkListID("onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		checkListID("onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		checkListID("onDetach");
		super.onDetach();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		checkListID("onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

}
