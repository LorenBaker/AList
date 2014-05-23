package com.lbconsulting.alist.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.GroupsSpinnerCursorAdapter;
import com.lbconsulting.alist.classes.ItemSettings;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class EditItemDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	// private EditItemDialogListener mFinishEditItemDialogCallback;

	private EditText txtEditItemName;
	private EditText txtEditItemNote;
	private Spinner spinEditItemGroup;
	private Button btnApply;
	private Button btnCancel;
	private long mActiveItemID;
	private long mActiveListID;
	private ItemSettings itemSettings;

	private LoaderManager mLoaderManager = null;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mEditItemDialogCallbacks;
	private GroupsSpinnerCursorAdapter mGroupsSpinnerCursorAdapter;

	public interface EditItemDialogListener {

		void onApplyEditItemDialog(String newItemName, String newItemNote, long newItemGroupID);

		void onCancelEditItemDialog();
	}

	public EditItemDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Create a new instance of EditItemDialogFragment
	 * 
	 * @param itemID
	 * @return EditItemDialogFragment
	 */
	public static EditItemDialogFragment newInstance(long listID, long itemID) {
		EditItemDialogFragment f = new EditItemDialogFragment();
		// Supply itemID input as an argument.
		Bundle args = new Bundle();
		args.putLong("listID", listID);
		args.putLong("itemID", itemID);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("EditItemDialogFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		MyLog.i("EditItemDialogFragment", "onSaveInstanceState");
		// Store our itemID
		outState.putLong("listID", this.mActiveListID);
		outState.putLong("itemID", this.mActiveItemID);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("EditItemDialogFragment", "onCreateView");

		if (savedInstanceState != null && savedInstanceState.containsKey("itemID")) {
			mActiveListID = savedInstanceState.getLong("listID", -1);
			mActiveItemID = savedInstanceState.getLong("itemID", -1);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", -1);
				mActiveItemID = bundle.getLong("itemID", -1);
			}
		}

		View view = inflater.inflate(R.layout.dialog_edit_item, container);
		txtEditItemName = (EditText) view.findViewById(R.id.txtEditItemName);
		txtEditItemNote = (EditText) view.findViewById(R.id.txtEditItemNote);
		spinEditItemGroup = (Spinner) view.findViewById(R.id.spinEditItemGroup);

		btnApply = (Button) view.findViewById(R.id.btnApply);
		btnApply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String newItemName = txtEditItemName.getText().toString().trim();
				String newItemNote = txtEditItemNote.getText().toString().trim();
				long newItemGroupID = spinEditItemGroup.getSelectedItemId();

				ItemsTable.UpdateItem(getActivity(), mActiveItemID, newItemName, newItemNote, newItemGroupID);

				/*				String itemChangedReceiverKey = String.valueOf(mActiveListID) + ItemsTable.ITEM_CHANGED_BROADCAST_KEY;
								Intent intent = new Intent(itemChangedReceiverKey);
								intent.putExtra("itemID", mActiveItemID);
								LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);*/

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
		getDialog().setTitle(R.string.dialog_title_edit_item);

		// Show soft keyboard automatically
		txtEditItemName.requestFocus();
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("EditItemDialogFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("listID", 0);
			mActiveItemID = bundle.getLong("itemID", 0);
		}
		if (mActiveItemID > 0) {
			itemSettings = new ItemSettings(getActivity(), mActiveItemID);
		}

		txtEditItemName.setText(itemSettings.getItemName());
		txtEditItemNote.setText(itemSettings.getItemNote());

		// Loader to populate the groups spinner
		mGroupsSpinnerCursorAdapter = new GroupsSpinnerCursorAdapter(getActivity(), null, 0);
		spinEditItemGroup.setAdapter(mGroupsSpinnerCursorAdapter);

		mEditItemDialogCallbacks = this;
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(AListUtilities.GROUPS_LOADER_ID, null, mEditItemDialogCallbacks);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("EditItemDialogFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.i("EditItemDialogFragment", "onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		MyLog.i("EditItemDialogFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		MyLog.i("EditItemDialogFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		MyLog.i("EditItemDialogFragment", "onDismiss");
		super.onDismiss(dialog);
	}

	@Override
	public void onStart() {
		MyLog.i("EditItemDialogFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		MyLog.i("EditItemDialogFragment", "onStop");
		super.onStop();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("EditItemDialogFragment", "onCreateLoader, id = " + id);
		CursorLoader cursorLoader = null;
		try {
			cursorLoader = GroupsTable.getAllGroupsInList(getActivity(), itemSettings.getListID(),
					GroupsTable.SORT_ORDER_GROUP);

		} catch (SQLiteException e) {
			MyLog.e("EditItemDialogFragment: onCreateLoader SQLiteException: ", e.toString());
			return null;

		} catch (IllegalArgumentException e) {
			MyLog.e("EditItemDialogFragment: onCreateLoader IllegalArgumentException: ", e.toString());
			return null;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		MyLog.i("EditItemDialogFragment", "onLoadFinished; loader id = " + loader.getId());
		mGroupsSpinnerCursorAdapter.swapCursor(newCursor);
		spinEditItemGroup.setSelection(AListUtilities.getIndex(spinEditItemGroup, itemSettings.getGroupID()));

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		MyLog.i("EditItemDialogFragment", "onLoaderReset; loader id = " + loader.getId());
		mGroupsSpinnerCursorAdapter.swapCursor(null);

	}

}
