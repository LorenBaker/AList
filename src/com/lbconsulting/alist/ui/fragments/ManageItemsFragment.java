package com.lbconsulting.alist.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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
import com.lbconsulting.alist.adapters.ManageItemsCursorAdaptor;
import com.lbconsulting.alist.adapters.GroupsSpinnerCursorAdapter;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.dialogs.EditItemDialogFragment;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class ManageItemsFragment extends Fragment
		implements LoaderManager.LoaderCallbacks<Cursor> {

	private long mActiveListID = -1;

	private ListSettings mListSettings;

	private TextView tvListTitle;
	private ListView itemsListView;
	private LinearLayout setGroupsLinearLayout;
	private Spinner spinGroups;
	private Button btnApplyGroup;

	private LoaderManager mLoaderManager = null;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mCheckItemsFragmentCallbacks;
	private ManageItemsCursorAdaptor mCheckItemsCursorAdaptor;
	private GroupsSpinnerCursorAdapter mGroupsSpinnerCursorAdapter;

	private boolean flag_FirstTimeLoadingItemDataSinceOnResume = false;

	public static final String REQUEST_CHECK_ITEMS_TAB_POSITION_BROADCAST_KEY = "requestCheckItemsTabPosition";
	public static final String CHECK_ITEMS_TAB_BROADCAST_KEY = "CheckItemsTabBroadcastKey";

	public static final String ACTIVE_GROUP_ID_BROADCAST_KEY = "activeGroupIdBroadcastKey";
	public static final String REQUEST_ACTIVE_GROUP_ID_BROADCAST_KEY = "requestActiveGroupIdBroadcastKey";

	private BroadcastReceiver mCheckItemsTabBroadcastReceiver;
	private BroadcastReceiver mRequestActiveGroupIdBroadcastReceiver;

	public static final int CHECK_ITEMS_TAB_CULL_MOVE_ITEMS = 0;
	public static final int CHECK_ITEMS_TAB_SET_GROUPS = 1;

	public ManageItemsFragment() {
		// Empty constructor
	}

	/**
	 * Create a new instance of EditItemDialogFragment
	 * 
	 * @param itemID
	 * @return EditItemDialogFragment
	 */
	public static ManageItemsFragment newInstance(long newListID) {

		if (newListID < 2) {
			MyLog.e("CheckItemsFragment: newInstance; listID = " + newListID, " is less than 2!!!!");
			return null;
		}
		ManageItemsFragment f = new ManageItemsFragment();
		// Supply listID input as an argument.
		Bundle args = new Bundle();
		args.putLong("listID", newListID);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MyLog.i("CheckItemsFragment", "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.i("CheckItemsFragment", "onCreate");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Store our listID
		outState.putLong("listID", this.mActiveListID);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("CheckItemsFragment", "onCreateView");

		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null)
				mActiveListID = bundle.getLong("listID", 0);
		}

		View view = inflater.inflate(R.layout.frag_check_items, container, false);

		mListSettings = new ListSettings(getActivity(), mActiveListID);

		tvListTitle = (TextView) view.findViewById(R.id.tvListTitle);
		tvListTitle.setText(mListSettings.getListTitle());

		setGroupsLinearLayout = (LinearLayout) view.findViewById(R.id.setGroupsLinearLayout);

		mGroupsSpinnerCursorAdapter = new GroupsSpinnerCursorAdapter(getActivity(), null, 0);
		spinGroups = (Spinner) view.findViewById(R.id.spinGroups);
		spinGroups.setAdapter(mGroupsSpinnerCursorAdapter);
		spinGroups.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				SendActiveGroupID();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				String activeGroupIdReceiverKey = String.valueOf(mActiveListID)
						+ ManageItemsFragment.ACTIVE_GROUP_ID_BROADCAST_KEY;
				Intent activeGroupIdReceiverIntent = new Intent(activeGroupIdReceiverKey);
				activeGroupIdReceiverIntent.putExtra("ActiveGroupID", -1);
				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(activeGroupIdReceiverIntent);
			}

		});

		btnApplyGroup = (Button) view.findViewById(R.id.btnApplyGroup);
		btnApplyGroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ApplyGroupsToCheckedItems();
			}
		});

		mCheckItemsCursorAdaptor = new ManageItemsCursorAdaptor(getActivity(), null, 0, mListSettings);
		itemsListView = (ListView) view.findViewById(R.id.itemsListView);
		itemsListView.setAdapter(mCheckItemsCursorAdaptor);

		mCheckItemsFragmentCallbacks = this;

		itemsListView.setOnItemClickListener(new OnItemClickListener() {

			// toggle check box
			@Override
			public void onItemClick(AdapterView<?> parent, View onItemClickView, int position, long id) {
				ItemsTable.ToggleCheckBox(getActivity(), id);
			}
		});

		itemsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			// edit item dialog
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View onItemLongClickView, int position,
					long activeItemID) {
				FragmentManager fm = getFragmentManager();
				Fragment prev = fm.findFragmentByTag("dialog_edit_item");
				if (prev != null) {
					FragmentTransaction ft = fm.beginTransaction();
					ft.remove(prev);
					ft.commit();
				}
				EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(mActiveListID, activeItemID);
				editItemDialog.show(fm, "dialog_edit_item");

				return true;
			}
		});

		return view;
	}

	protected void SendActiveGroupID() {
		String activeGroupIdReceiverKey = String.valueOf(mActiveListID)
				+ ManageItemsFragment.ACTIVE_GROUP_ID_BROADCAST_KEY;
		Intent activeGroupIdReceiverIntent = new Intent(activeGroupIdReceiverKey);
		activeGroupIdReceiverIntent.putExtra("ActiveGroupID", spinGroups.getSelectedItemId());
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(activeGroupIdReceiverIntent);
	}

	protected void ApplyGroupsToCheckedItems() {
		long groupID = spinGroups.getSelectedItemId();
		ItemsTable.ApplyGroupToCheckedItems(getActivity(), mActiveListID, groupID);
	}

	private void setFragmentColors() {
		tvListTitle.setBackgroundColor(this.mListSettings.getTitleBackgroundColor());
		tvListTitle.setTextColor(this.mListSettings.getTitleTextColor());
		itemsListView.setBackgroundColor(this.mListSettings.getListBackgroundColor());
	}

	private void RequestCheckItemsTabPosition() {
		String requestCheckItemsTabPositionReceiverKey = String.valueOf(mActiveListID)
				+ ManageItemsFragment.REQUEST_CHECK_ITEMS_TAB_POSITION_BROADCAST_KEY;
		Intent requestCheckItemsTabPositionIntent = new Intent(requestCheckItemsTabPositionReceiverKey);
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(requestCheckItemsTabPositionIntent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("CheckItemsFragment", "onActivityCreated");
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(AListUtilities.ITEMS_LOADER_ID, null, mCheckItemsFragmentCallbacks);
		mLoaderManager.initLoader(AListUtilities.GROUPS_LOADER_ID, null, mCheckItemsFragmentCallbacks);

		mCheckItemsTabBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra("checkItemsTabPosition")) {
					int checkItemsTabPosition = intent.getExtras().getInt("checkItemsTabPosition", 0);
					ShowSetGroupsLinearLayout(checkItemsTabPosition);
				}
			}
		};

		mRequestActiveGroupIdBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				SendActiveGroupID();
			}
		};

		// Register local broadcast receivers.
		String applyCheckItemsTabPositionKey = String.valueOf(mActiveListID) + CHECK_ITEMS_TAB_BROADCAST_KEY;
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCheckItemsTabBroadcastReceiver,
				new IntentFilter(applyCheckItemsTabPositionKey));

		String requestActiveGroupIdBroadcastKey = String.valueOf(mActiveListID) + REQUEST_ACTIVE_GROUP_ID_BROADCAST_KEY;
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRequestActiveGroupIdBroadcastReceiver,
				new IntentFilter(requestActiveGroupIdBroadcastKey));

		super.onActivityCreated(savedInstanceState);
	}

	private void ShowSetGroupsLinearLayout(int checkItemsTabPosition) {
		switch (checkItemsTabPosition) {

			case CHECK_ITEMS_TAB_CULL_MOVE_ITEMS:
				setGroupsLinearLayout.setVisibility(View.GONE);
				break;

			case CHECK_ITEMS_TAB_SET_GROUPS:
				setGroupsLinearLayout.setVisibility(View.VISIBLE);
				break;

			default:
				break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		MyLog.i("CheckItemsFragment", "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();

		MyLog.i("CheckItemsFragment", "onResume()");

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("listID", 0);
		}
		mListSettings = new ListSettings(getActivity(), mActiveListID);
		RequestCheckItemsTabPosition();
		setFragmentColors();

		// Set onResume flags
		flag_FirstTimeLoadingItemDataSinceOnResume = true;
	}

	@Override
	public void onPause() {
		super.onPause();

		MyLog.i("CheckItemsFragment", "onPause()");

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
		MyLog.i("CheckItemsFragment", "onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyLog.i("CheckItemsFragment", "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyLog.i("CheckItemsFragment", "onDestroy");
		// Unregister local broadcast receivers
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mCheckItemsTabBroadcastReceiver);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRequestActiveGroupIdBroadcastReceiver);

	}

	@Override
	public void onDetach() {
		super.onDetach();
		MyLog.i("CheckItemsFragment", "onDetach");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		MyLog.i("CheckItemsFragment", "onViewCreated");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("CheckItemsFragment: onCreateLoader; id = " + id, "; listID = " + mActiveListID);

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
					MyLog.e("CheckItemsFragment: onCreateLoader SQLiteException: ", e.toString());
					return null;

				} catch (IllegalArgumentException e) {
					MyLog.e("CheckItemsFragment: onCreateLoader IllegalArgumentException: ", e.toString());
					return null;
				}
				break;

			case AListUtilities.GROUPS_LOADER_ID:
				try {
					cursorLoader = GroupsTable.getAllGroupsInListIncludeDefault(getActivity(), mActiveListID,
							GroupsTable.SORT_ORDER_GROUP);
				} catch (SQLiteException e) {
					MyLog.e("CheckItemsFragment: onCreateLoader SQLiteException: ", e.toString());
					return null;

				} catch (IllegalArgumentException e) {
					MyLog.e("CheckItemsFragment: onCreateLoader IllegalArgumentException: ", e.toString());
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
		MyLog.i("CheckItemsFragment: onLoadFinished; id = " + id, "; listID = " + mActiveListID);
		// The asynchronous load is complete and the newCursor is now available for use.
		// Update the adapter to show the changed data.
		switch (loader.getId()) {
			case AListUtilities.ITEMS_LOADER_ID:
				mCheckItemsCursorAdaptor.swapCursor(newCursor);
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
				break;

			default:
				break;
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("CheckItemsFragment: onLoaderReset; id = " + id, "; listID = " + mActiveListID);

		switch (loader.getId()) {
			case AListUtilities.ITEMS_LOADER_ID:
				mCheckItemsCursorAdaptor.swapCursor(null);
				break;

			case AListUtilities.GROUPS_LOADER_ID:
				mGroupsSpinnerCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}
}
