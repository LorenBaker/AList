package com.lbconsulting.alist.ui.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.ItemsCursorAdaptor;
import com.lbconsulting.alist.adapters.StoresSpinnerCursorAdapter;
import com.lbconsulting.alist.classes.DynamicListView;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class ListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private long mActiveListID = -1;
	private long mActiveStoreID = -1;

	private ListSettings mListSettings;

	private TextView mListTitle;
	private ListView mItemsListView;
	private Spinner mStoreSpinner;

	private LoaderManager mLoaderManager = null;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mListsFragmentCallbacks;
	private ItemsCursorAdaptor mItemsCursorAdaptor;

	private StoresSpinnerCursorAdapter mStoresSpinnerCursorAdapter;

	private boolean flag_FirstTimeLoadingItemDataSinceOnResume = false;

	public ListsFragment() {
		// Empty constructor
	}

	public static ListsFragment newInstance(long newListID) {

		if (newListID < 2) {
			MyLog.e("ListsFragment: newInstance; listID = " + newListID, " is less than 2!!!!");
			return null;
		}

		ListsFragment f = new ListsFragment();

		// Supply listID input as an argument.
		Bundle args = new Bundle();
		args.putLong("listID", newListID);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MyLog.i("ListsFragment", "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.i("ListsFragment", "onCreate");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		MyLog.i("ListsFragment", "onSaveInstanceState");
		// Store our listID
		outState.putLong("listID", this.mActiveListID);
		outState.putLong("storeID", this.mActiveStoreID);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("ListsFragment", "onCreateView");

		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", 0);
			mActiveStoreID = savedInstanceState.getLong("storeID", -1);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", 0);
				mActiveStoreID = bundle.getLong("storeID", -1);
			}
		}

		View view = inflater.inflate(R.layout.frag_lists, container, false);

		mListSettings = new ListSettings(getActivity(), mActiveListID);

		mListTitle = (TextView) view.findViewById(R.id.tvListTitle);
		if (mListTitle != null) {
			mListTitle.setText(mListSettings.getListTitle());
		}

		mStoreSpinner = (Spinner) view.findViewById(R.id.spinStores);
		if (mStoreSpinner != null) {
			mStoresSpinnerCursorAdapter = new StoresSpinnerCursorAdapter(getActivity(), null, 0, mListSettings);
			mStoreSpinner.setAdapter(mStoresSpinnerCursorAdapter);
			if (mListSettings.getShowStores()) {
				mStoreSpinner.setVisibility(View.VISIBLE);
			} else {
				mStoreSpinner.setVisibility(View.GONE);
			}
		}

		mItemsListView = (DynamicListView) view.findViewById(R.id.itemsListView);
		if (mItemsListView != null) {
			mItemsCursorAdaptor = new ItemsCursorAdaptor(getActivity(), null, 0, mListSettings);
			mItemsListView.setAdapter(mItemsCursorAdaptor);
		}

		setViewColors();

		mListsFragmentCallbacks = this;

		mItemsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				ItemsTable.ToggleStrikeOut(getActivity(), id);
			}
		});

		mStoreSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long storeID) {
				// show the list showing the selected store
				mLoaderManager.restartLoader(AListUtilities.ITEMS_LOADER_ID, null, mListsFragmentCallbacks);

				// update the lists table with the selected store ID
				ContentValues newFieldValues = new ContentValues();
				newFieldValues.put(ListsTable.COL_ACTIVE_STORE_ID, storeID);
				mListSettings.updateListsTableFieldValues(newFieldValues);
				mActiveStoreID = storeID;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Nothing to do
			}
		});

		return view;
	}

	private void setViewColors() {
		mListTitle.setBackgroundColor(this.mListSettings.getTitleBackgroundColor());
		mListTitle.setTextColor(this.mListSettings.getTitleTextColor());
		mItemsListView.setBackgroundColor(this.mListSettings.getListBackgroundColor());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("ListsFragment", "onActivityCreated");

		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(AListUtilities.ITEMS_LOADER_ID, null, mListsFragmentCallbacks);
		mLoaderManager.initLoader(AListUtilities.STORES_LOADER_ID, null, mListsFragmentCallbacks);

		super.onActivityCreated(savedInstanceState);
	}

	public void DeleteList() {
		ListsTable.DeleteList(getActivity(), mActiveListID);
	}

	@Override
	public void onStart() {
		super.onStart();
		MyLog.i("ListsFragment", "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();

		MyLog.i("ListsFragment", "onResume()");

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("listID", 0);
			// mActiveStoreID = bundle.getLong("storeID", -1);
		}

		mListSettings.RefreshListSettings();
		if (mListSettings.getShowStores()) {
			mActiveStoreID = mListSettings.getActiveStoreID();
			int position = AListUtilities.getIndex(mStoreSpinner, mActiveStoreID);
			mStoreSpinner.setSelection(position);
		}
		setViewColors();

		// Set onResume flags
		flag_FirstTimeLoadingItemDataSinceOnResume = true;
		mLoaderManager.restartLoader(AListUtilities.ITEMS_LOADER_ID, null, mListsFragmentCallbacks);
	}

	@Override
	public void onPause() {
		super.onPause();

		MyLog.i("ListsFragment", "onPause()");

		// save ItemsListView position
		View v = mItemsListView.getChildAt(0);
		int ListViewTop = (v == null) ? 0 : v.getTop();
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(ListsTable.COL_LISTVIEW_FIRST_VISIBLE_POSITION, mItemsListView.getFirstVisiblePosition());
		newFieldValues.put(ListsTable.COL_LISTVIEW_TOP, ListViewTop);
		ListsTable.UpdateListsTableFieldValues(getActivity(), mActiveListID, newFieldValues);
	}

	@Override
	public void onStop() {
		super.onStop();
		MyLog.i("ListsFragment", "onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyLog.i("ListsFragment", "onDestroyView");
	}

	@Override
	public void onDestroy() {
		MyLog.i("ListsFragment", "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		MyLog.i("ListsFragment", "onDetach");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		MyLog.i("ListsFragment", "onViewCreated");
	}

	public void showListTitle(String newListTitle) {
		mListTitle.setText(newListTitle);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("ListsFragment", "onCreateLoader. LoaderId = " + id + "; listID = " + mActiveListID);

		CursorLoader cursorLoader = null;

		switch (id) {

			case AListUtilities.ITEMS_LOADER_ID:

				int listSortOrder = mListSettings.getListSortOrder();
				String sortOrder = "";
				try {
					switch (listSortOrder) {
						case AListUtilities.LIST_SORT_ALPHABETICAL:
							sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME;
							cursorLoader = ItemsTable.getAllSelectedItemsInList(getActivity(), mActiveListID, true,
									sortOrder);
							break;

						case AListUtilities.LIST_SORT_BY_GROUP:
							cursorLoader = ItemsTable.getAllSelectedItemsInListWithGroups(getActivity(), mActiveListID,
									true);
							break;

						case AListUtilities.LIST_SORT_MANUAL:
							sortOrder = ItemsTable.SORT_ORDER_MANUAL;
							cursorLoader = ItemsTable.getAllSelectedItemsInList(getActivity(), mActiveListID, true,
									sortOrder);
							break;

						case AListUtilities.LIST_SORT_BY_STORE_LOCATION:
							cursorLoader = ItemsTable.getAllSelectedItemsInListWithLocations(getActivity(),
									mActiveListID,
									mStoreSpinner.getSelectedItemId(), true);
							break;

						default:
							sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME;
							cursorLoader = ItemsTable.getAllSelectedItemsInList(getActivity(), mActiveListID, true,
									sortOrder);
							break;
					}

				} catch (SQLiteException e) {
					MyLog.e("ListsFragment: onCreateLoader SQLiteException: ", e.toString());
					return null;

				} catch (IllegalArgumentException e) {
					MyLog.e("ListsFragment: onCreateLoader IllegalArgumentException: ", e.toString());
					return null;
				}
				break;

			case AListUtilities.STORES_LOADER_ID:
				try {
					cursorLoader = StoresTable.getAllStoresInListExcludeDefaultStore(getActivity(), mActiveListID,
							StoresTable.SORT_ORDER_STORE_NAME);

				} catch (SQLiteException e) {
					MyLog.e("ListsFragment: onCreateLoader SQLiteException: ", e.toString());
					return null;

				} catch (IllegalArgumentException e) {
					MyLog.e("ListsFragment: onCreateLoader IllegalArgumentException: ", e.toString());
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
		MyLog.i("ListsFragment", "onLoadFinished. LoaderID = " + id + "; listID = " + mActiveListID);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (loader.getId()) {
			case AListUtilities.ITEMS_LOADER_ID:
				mItemsCursorAdaptor.swapCursor(newCursor);

				if (flag_FirstTimeLoadingItemDataSinceOnResume) {
					mItemsListView.setSelectionFromTop(mListSettings.getListViewFirstVisiblePosition(),
							mListSettings.getListViewTop());
					flag_FirstTimeLoadingItemDataSinceOnResume = false;
				}

				if (newCursor != null) {
					newCursor.moveToFirst();
					int count = newCursor.getCount();
					if (count > 0) {
						String itemName = newCursor
								.getString(newCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
						int temp = 0;
					}
				}

				break;

			case AListUtilities.STORES_LOADER_ID:
				mStoresSpinnerCursorAdapter.swapCursor(newCursor);
				mActiveStoreID = mListSettings.getActiveStoreID();
				mStoreSpinner.setSelection(AListUtilities.getIndex(mStoreSpinner, mActiveStoreID));
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("ListsFragment", "onLoaderReset. LoaderID = " + id + "; listID = " + mActiveListID);

		switch (loader.getId()) {
			case AListUtilities.ITEMS_LOADER_ID:
				mItemsCursorAdaptor.swapCursor(null);
				break;

			case AListUtilities.STORES_LOADER_ID:
				mStoresSpinnerCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}
}
