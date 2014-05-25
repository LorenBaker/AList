package com.lbconsulting.alist.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.MoveItemListsSpinnerCursorAdapter;
import com.lbconsulting.alist.classes.AListEvents.ListTargetSelected;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class MoveCheckedItemsDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private Spinner spinLists;
	private Button btnApply;
	private Button btnCancel;
	private TextView tvMessage;
	private long mActiveListID = -1;
	private int mNumberOfCheckedItems = -1;

	private static final int LISTS_LOADER_ID = 1;
	private LoaderManager mLoaderManager = null;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mMoveCheckedItemsDialogCallbacks;
	private MoveItemListsSpinnerCursorAdapter mMoveItemListsSpinnerCursorAdapter;

	public MoveCheckedItemsDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Create a new instance of MoveCheckedItemsDialogFragment
	 * 
	 * @param listID
	 * @return MoveCheckedItemsDialogFragment
	 */
	public static MoveCheckedItemsDialogFragment newInstance(long listID, int numberOfCheckedItems) {
		MoveCheckedItemsDialogFragment f = new MoveCheckedItemsDialogFragment();
		// Supply listID input as an argument.
		Bundle args = new Bundle();
		args.putLong("listID", listID);
		args.putInt("numberOfCheckedItems", numberOfCheckedItems);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onSaveInstanceState");
		// Store our listID
		outState.putLong("listID", mActiveListID);
		outState.putInt("numberOfCheckedItems", mNumberOfCheckedItems);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onCreateView");

		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", -1);
			mNumberOfCheckedItems = savedInstanceState.getInt("numberOfCheckedItems", -1);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", -1);
				mNumberOfCheckedItems = bundle.getInt("numberOfCheckedItems", -1);
			}
		}

		View view = inflater.inflate(R.layout.dialog_move_item, container);
		tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		tvMessage.setText("Please select the target list.");
		spinLists = (Spinner) view.findViewById(R.id.spinLists);

		btnApply = (Button) view.findViewById(R.id.btnApply);
		btnApply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				long selectedListID = spinLists.getSelectedItemId();
				EventBus.getDefault().post(new ListTargetSelected(selectedListID));
				getDialog().dismiss();
			}
		});

		btnCancel = (Button) view.findViewById(R.id.btnCancel);
		if (btnCancel != null) {
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
			});
		}

		Resources res = getResources();
		String numberOfCheckedItemsFound = res.getQuantityString(R.plurals.numberOfCheckedItems,
				mNumberOfCheckedItems, mNumberOfCheckedItems);
		StringBuilder sb = new StringBuilder();
		sb.append("Move ");
		sb.append(numberOfCheckedItemsFound);
		sb.append("?");
		getDialog().setTitle(sb.toString());
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("listID", 0);
			mNumberOfCheckedItems = bundle.getInt("numberOfCheckedItems", -1);
		}

		// Loader to populate the lists spinner
		mMoveItemListsSpinnerCursorAdapter = new MoveItemListsSpinnerCursorAdapter(getActivity(), null, 0);
		spinLists.setAdapter(mMoveItemListsSpinnerCursorAdapter);

		mMoveCheckedItemsDialogCallbacks = this;
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(LISTS_LOADER_ID, null, mMoveCheckedItemsDialogCallbacks);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		MyLog.i("MoveCheckedItemsDialogFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		MyLog.i("MoveCheckedItemsDialogFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onDismiss");
		super.onDismiss(dialog);
	}

	@Override
	public void onStart() {
		MyLog.i("MoveCheckedItemsDialogFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		MyLog.i("MoveCheckedItemsDialogFragment", "onStop");
		super.onStop();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onCreateLoader, id = " + id);
		CursorLoader cursorLoader = null;
		try {
			cursorLoader = ListsTable.getMoveItemListSelection(getActivity(), mActiveListID);

		} catch (SQLiteException e) {
			MyLog.e("MoveCheckedItemsDialogFragment: onCreateLoader SQLiteException: ", e.toString());
			return null;

		} catch (IllegalArgumentException e) {
			MyLog.e("MoveCheckedItemsDialogFragment: onCreateLoader IllegalArgumentException: ", e.toString());
			return null;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onLoadFinished; loader id = " + loader.getId());
		mMoveItemListsSpinnerCursorAdapter.swapCursor(newCursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		MyLog.i("MoveCheckedItemsDialogFragment", "onLoaderReset; loader id = " + loader.getId());
		mMoveItemListsSpinnerCursorAdapter.swapCursor(null);

	}

}
