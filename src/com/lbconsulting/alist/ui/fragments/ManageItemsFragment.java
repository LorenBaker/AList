package com.lbconsulting.alist.ui.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.GroupsSpinnerCursorAdapter;
import com.lbconsulting.alist.adapters.ManageItemsCursorAdaptor;
import com.lbconsulting.alist.classes.AListEvents.ManageItemsActiveGroupChanged;
import com.lbconsulting.alist.classes.AListEvents.ManageItemsTabPostionChange;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.dialogs.EditItemDialogFragment;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class ManageItemsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private long mActiveListID = -1;
	private int mActiveTabPosition;
	private ListSettings mListSettings;

	private TextView tvListTitle;
	private ListView itemsListView;
	private LinearLayout setGroupsLinearLayout;
	private Spinner spinGroups;
	private Button btnApplyGroup;

	private LoaderManager mLoaderManager = null;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mManageItemsFragmentCallbacks;
	private ManageItemsCursorAdaptor mManageItemsCursorAdaptor;
	private GroupsSpinnerCursorAdapter mGroupsSpinnerCursorAdapter;

	private boolean flag_FirstTimeLoadingItemDataSinceOnResume = false;

	public static final int TAB_CULL_MOVE_ITEMS = 0;
	public static final int TAB_SET_GROUPS = 1;

	public ManageItemsFragment() {
		// Empty constructor
	}

	/**
	 * Create a new instance of EditItemDialogFragment
	 * 
	 * @param itemID
	 * @return EditItemDialogFragment
	 */
	public static ManageItemsFragment newInstance(long listID, int tabPosition) {

		if (listID < 2) {
			MyLog.e("ManageItemsFragment: newInstance; listID = " + listID, " is less than 2!!!!");
			return null;
		}
		ManageItemsFragment f = new ManageItemsFragment();
		Bundle args = new Bundle();
		args.putLong("listID", listID);
		args.putLong("ActiveTabPosition", tabPosition);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MyLog.i("ManageItemsFragment", "onAttach() listID:" + mActiveListID);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.i("ManageItemsFragment", "onCreate() listID:" + mActiveListID);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong("listID", this.mActiveListID);
		outState.putLong("ActiveTabPosition", this.mActiveTabPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("ManageItemsFragment", "onCreateView() listID:" + mActiveListID);

		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", 0);
			}
		}

		SharedPreferences storedStates = getActivity().getSharedPreferences("AList", Context.MODE_PRIVATE);
		mActiveTabPosition = storedStates.getInt("ActiveTabPosition", 0);

		View view = inflater.inflate(R.layout.frag_manage_items, container, false);

		mListSettings = new ListSettings(getActivity(), mActiveListID);

		tvListTitle = (TextView) view.findViewById(R.id.tvListTitle);
		tvListTitle.setText(mListSettings.getListTitle());

		setGroupsLinearLayout = (LinearLayout) view.findViewById(R.id.setGroupsLinearLayout);

		mGroupsSpinnerCursorAdapter = new GroupsSpinnerCursorAdapter(getActivity(), null, 0);
		spinGroups = (Spinner) view.findViewById(R.id.spinGroups);
		spinGroups.setAdapter(mGroupsSpinnerCursorAdapter);
		spinGroups.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int postion, long groupID) {
				ContentValues newFieldValues = new ContentValues();
				newFieldValues.put(ListsTable.COL_MANAGE_ITEMS_GROUP_ID, groupID);
				ListsTable.UpdateListsTableFieldValues(getActivity(), mActiveListID, newFieldValues);
				mListSettings.RefreshListSettings();

				EventBus.getDefault().post(new ManageItemsActiveGroupChanged(mActiveListID, groupID));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});

		btnApplyGroup = (Button) view.findViewById(R.id.btnApplyGroup);
		btnApplyGroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ApplyGroupsToManageItems();
			}
		});

		mManageItemsCursorAdaptor = new ManageItemsCursorAdaptor(getActivity(), null, 0, mListSettings);
		itemsListView = (ListView) view.findViewById(R.id.itemsListView);
		itemsListView.setAdapter(mManageItemsCursorAdaptor);

		mManageItemsFragmentCallbacks = this;

		itemsListView.setOnItemClickListener(new OnItemClickListener() {

			// toggle check box
			@Override
			public void onItemClick(AdapterView<?> parent, View onItemClickView, int position, long itemID) {
				ItemsTable.ToggleCheckBox(getActivity(), itemID);
			}
		});

		itemsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			// edit item dialog
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View onItemLongClickView, int position, long itemID) {
				FragmentManager fm = getFragmentManager();
				Fragment prev = fm.findFragmentByTag("dialog_edit_item");
				if (prev != null) {
					FragmentTransaction ft = fm.beginTransaction();
					ft.remove(prev);
					ft.commit();
				}
				EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(mActiveListID, itemID);
				editItemDialog.show(fm, "dialog_edit_item");

				return true;
			}
		});

		return view;
	}

	protected void ApplyGroupsToManageItems() {
		long groupID = spinGroups.getSelectedItemId();
		ItemsTable.ApplyGroupToManageItems(getActivity(), mActiveListID, groupID);
	}

	private void setFragmentColors() {
		tvListTitle.setBackgroundColor(this.mListSettings.getTitleBackgroundColor());
		tvListTitle.setTextColor(this.mListSettings.getTitleTextColor());
		itemsListView.setBackgroundColor(this.mListSettings.getListBackgroundColor());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("ManageItemsFragment", "onActivityCreated() listID:" + mActiveListID);
		EventBus.getDefault().register(this);
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(AListUtilities.ITEMS_LOADER_ID, null, mManageItemsFragmentCallbacks);
		mLoaderManager.initLoader(AListUtilities.GROUPS_LOADER_ID, null, mManageItemsFragmentCallbacks);

		super.onActivityCreated(savedInstanceState);
	}

	public void onEvent(ManageItemsTabPostionChange event) {
		mActiveTabPosition = event.getTabPosition();
		selectTabPosition(mActiveTabPosition);
	}

	private void selectTabPosition(int manageItemsTabPostion) {
		if (setGroupsLinearLayout != null) {
			switch (manageItemsTabPostion) {

				case TAB_CULL_MOVE_ITEMS:
					setGroupsLinearLayout.setVisibility(View.GONE);
					break;

				case TAB_SET_GROUPS:
					setGroupsLinearLayout.setVisibility(View.VISIBLE);
					break;

				default:
					break;
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		MyLog.i("ManageItemsFragment", "onStart() listID:" + mActiveListID);
	}

	@Override
	public void onResume() {
		super.onResume();

		MyLog.i("ManageItemsFragment", "onResume() listID:" + mActiveListID);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("listID", 0);
		}

		mListSettings = new ListSettings(getActivity(), mActiveListID);
		setFragmentColors();

		SharedPreferences storedStates = getActivity().getSharedPreferences("AList", Context.MODE_PRIVATE);
		mActiveTabPosition = storedStates.getInt("ActiveTabPosition", 0);
		selectTabPosition(mActiveTabPosition);

		// Set onResume flags
		flag_FirstTimeLoadingItemDataSinceOnResume = true;
	}

	@Override
	public void onPause() {
		super.onPause();

		MyLog.i("ManageItemsFragment", "onPause() listID:" + mActiveListID);

		// save ItemsListView position
		View v = itemsListView.getChildAt(0);
		int ListViewTop = (v == null) ? 0 : v.getTop();
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(ListsTable.COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION,
				itemsListView.getFirstVisiblePosition());
		newFieldValues.put(ListsTable.COL_MASTER_LISTVIEW_TOP, ListViewTop);
		ListsTable.UpdateListsTableFieldValues(getActivity(), mActiveListID, newFieldValues);
	}

	@Override
	public void onStop() {
		super.onStop();
		MyLog.i("ManageItemsFragment", "onStop() listID:" + mActiveListID);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
		MyLog.i("ManageItemsFragment", "onDestroyView() listID:" + mActiveListID);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyLog.i("ManageItemsFragment", "onDestroy() listID:" + mActiveListID);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		MyLog.i("ManageItemsFragment", "onDetach() listID:" + mActiveListID);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		MyLog.i("ManageItemsFragment", "onViewCreated() listID:" + mActiveListID);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("ManageItemsFragment: onCreateLoader; id = " + id, "; listID = " + mActiveListID);

		CursorLoader cursorLoader = null;
		String selection = null;

		switch (id) {

			case AListUtilities.ITEMS_LOADER_ID:
				int masterListSortOrder = mListSettings.getMasterListSortOrder();
				String sortOrder = "";

				try {
					switch (masterListSortOrder) {
						case AListUtilities.MASTER_LIST_SORT_ALPHABETICAL:
							sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME;
							cursorLoader = ItemsTable.getAllItemsInList(getActivity(), mActiveListID, selection,
									sortOrder);
							break;

						case AListUtilities.MASTER_LIST_SORT_BY_GROUP:
							cursorLoader = ItemsTable.getAllItemsInListWithGroups(getActivity(), mActiveListID,
									selection);
							break;

						case AListUtilities.MASTER_LIST_SORT_BY_LAST_USED:
							sortOrder = ItemsTable.SORT_ORDER_LAST_USED;
							cursorLoader = ItemsTable.getAllItemsInList(getActivity(), mActiveListID, selection,
									sortOrder);
							break;

						case AListUtilities.MASTER_LIST_SORT_SELECTED_AT_TOP:
							sortOrder = ItemsTable.SORT_ORDER_SELECTED_AT_TOP;
							cursorLoader = ItemsTable.getAllItemsInList(getActivity(), mActiveListID, selection,
									sortOrder);
							break;

						case AListUtilities.MASTER_LIST_SORT_SELECTED_AT_BOTTOM:
							sortOrder = ItemsTable.SORT_ORDER_SELECTED_AT_BOTTOM;
							cursorLoader = ItemsTable.getAllItemsInList(getActivity(), mActiveListID, selection,
									sortOrder);
							break;

						default:
							sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME;
							cursorLoader = ItemsTable.getAllItemsInList(getActivity(), mActiveListID, selection,
									sortOrder);
							break;
					}

				} catch (SQLiteException e) {
					MyLog.e("ManageItemsFragment: onCreateLoader SQLiteException: ", e.toString());
					return null;

				} catch (IllegalArgumentException e) {
					MyLog.e("ManageItemsFragment: onCreateLoader IllegalArgumentException: ", e.toString());
					return null;
				}
				break;

			case AListUtilities.GROUPS_LOADER_ID:
				try {
					cursorLoader = GroupsTable.getAllGroupsInListIncludeDefault(getActivity(), mActiveListID,
							GroupsTable.SORT_ORDER_GROUP);
				} catch (SQLiteException e) {
					MyLog.e("ManageItemsFragment: onCreateLoader SQLiteException: ", e.toString());
					return null;

				} catch (IllegalArgumentException e) {
					MyLog.e("ManageItemsFragment: onCreateLoader IllegalArgumentException: ", e.toString());
					return null;
				}

				break;

			default:
				break;
		}

		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("ManageItemsFragment: onLoadFinished; id = " + id, "; listID = " + mActiveListID);
		// The asynchronous load is complete and the newCursor is now available for use.
		// Update the adapter to show the changed data.
		switch (loader.getId()) {
			case AListUtilities.ITEMS_LOADER_ID:
				mManageItemsCursorAdaptor.swapCursor(newCursor);
				if (flag_FirstTimeLoadingItemDataSinceOnResume) {
					itemsListView
							.setSelectionFromTop(
									mListSettings.getMasterListViewFirstVisiblePosition(),
									mListSettings.getMasterListViewTop());
					flag_FirstTimeLoadingItemDataSinceOnResume = false;
				}
				break;

			case AListUtilities.GROUPS_LOADER_ID:
				mGroupsSpinnerCursorAdapter.swapCursor(newCursor);
				long groupID = mListSettings.getManageItemsGroupID();
				int position = AListUtilities.getIndex(spinGroups, groupID);
				spinGroups.setSelection(position);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("ManageItemsFragment: onLoaderReset; id = " + id, "; listID = " + mActiveListID);

		switch (loader.getId()) {
			case AListUtilities.ITEMS_LOADER_ID:
				mManageItemsCursorAdaptor.swapCursor(null);
				break;

			case AListUtilities.GROUPS_LOADER_ID:
				mGroupsSpinnerCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}
}
