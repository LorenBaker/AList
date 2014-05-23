package com.lbconsulting.alist.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.ui.activities.StoresActivity;
import com.lbconsulting.alist.utilities.MyLog;

public class StoresDialogFragment extends DialogFragment {

	public static final int EDIT_STORE_NAME = 35;
	public static final int NEW_STORE = 45;
	public static final int DELETE_STORE = 55;

	private Button btnApply;
	private Button btnCancel;
	private static EditText txtStoreName;

	private long mActiveListID;
	private long mActiveStoreID;
	private int mDialogType;

	public StoresDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Creates a new instance of the StoresDialogFragment
	 * 
	 * @param listID
	 * @param storeID
	 * @param dialogType
	 * @return StoresDialogFragment
	 */
	public static StoresDialogFragment newInstance(long listID, long storeID, int dialogType) {
		if (listID < 1) {
			if (dialogType != EDIT_STORE_NAME) {
				MyLog.e("StoresDialogFragment", "newInstance: invalid listID:" + listID);
			}
		}

		if (storeID < 1) {
			if (dialogType != NEW_STORE) {
				MyLog.e("StoresDialogFragment", "newInstance: invalid storeID:" + storeID);
			}
		}

		StoresDialogFragment f = new StoresDialogFragment();
		// Supply itemID input as an argument.
		Bundle args = new Bundle();
		args.putLong("ActiveListID", listID);
		args.putLong("ActiveStoreID", storeID);
		args.putInt("dialogType", dialogType);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("StoresDialogFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Store our state
		outState.putLong("ActiveListID", this.mActiveListID);
		outState.putLong("ActiveStoreID", this.mActiveStoreID);
		outState.putLong("dialogType", this.mDialogType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey("mActiveStoreID")) {
			mActiveListID = savedInstanceState.getLong("ActiveListID", 0);
			mActiveStoreID = savedInstanceState.getLong("ActiveStoreID", 0);
			mDialogType = savedInstanceState.getInt("dialogType", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("ActiveListID", 0);
				mActiveStoreID = bundle.getLong("ActiveStoreID", 0);
				mDialogType = bundle.getInt("dialogType", 0);
			}
		}

		// inflate view
		View view = inflater.inflate(R.layout.dialog_store_create_edit_delete, container);

		switch (mDialogType) {

			case EDIT_STORE_NAME:
				MyLog.i("StoresDialogFragment", "onCreateView: Edit Store Name");
				getDialog().setTitle("Edit Store Name?");
				break;

			case NEW_STORE:
				MyLog.i("StoresDialogFragment", "onCreateView: New Store");
				getDialog().setTitle("Create New Store?");
				break;

			case DELETE_STORE:
				MyLog.i("StoresDialogFragment", "onCreateView: Delete Store");
				getDialog().setTitle("Delete Store?");
				break;

			default:
				break;
		}

		if (view != null) {
			btnApply = (Button) view.findViewById(R.id.btnApply);
			btnApply.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					switch (mDialogType) {

						case EDIT_STORE_NAME:
							String storeNewName = txtStoreName.getText().toString();
							storeNewName = storeNewName.trim();
							if (!storeNewName.isEmpty()) {
								ContentValues newFieldValues = new ContentValues();
								newFieldValues.put(StoresTable.COL_STORE_NAME, storeNewName);
								StoresTable.UpdateStoreTableFieldValues(getActivity(), mActiveStoreID, newFieldValues);
							}
							break;

						case NEW_STORE:
							String newStoreName = txtStoreName.getText().toString();
							newStoreName = newStoreName.trim();
							if (!newStoreName.isEmpty()) {
								mActiveStoreID = StoresTable.CreateNewStore(getActivity(), mActiveListID, newStoreName);
								/*ContentValues newFieldValues = new ContentValues();
								newFieldValues.put(ListsTable.COL_ACTIVE_STORE_ID, mActiveStoreID);
								ListsTable.UpdateListsTableFieldValues(getActivity(), mActiveListID, newFieldValues);*/
							}
							break;

						case DELETE_STORE:
							StoresTable.DeleteStore(getActivity(), mActiveStoreID);
							// TODO figure out what store to go to.
							mActiveStoreID = -1;
							break;

						default:
							break;
					}
					SendRestartStoresActivityBroadcast();
					getDialog().dismiss();
				}
			});

			btnCancel = (Button) view.findViewById(R.id.btnCancel);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
			});

			txtStoreName = (EditText) view.findViewById(R.id.txtStoreName);
			if (txtStoreName != null) {
				switch (mDialogType) {
					case EDIT_STORE_NAME:
						// We're displaying the Edit Store Name dialog
						String storeName = StoresTable.getStoreName(getActivity(), mActiveStoreID);
						txtStoreName.setText(storeName);
						break;

					case NEW_STORE:
						// We're displaying the New Store dialog
						txtStoreName.setText("");
						break;

					case DELETE_STORE:
						// We're displaying the Delete Store dialog
						storeName = StoresTable.getStoreName(getActivity(), mActiveStoreID);
						txtStoreName.setText(storeName);
						break;

					default:
						break;
				}

				if (txtStoreName.requestFocus()) {
					getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				}
			}
		}

		return view;
	}

	protected void SendRestartStoresActivityBroadcast() {
		String restartStoresActivityKey = String.valueOf(mActiveListID)
				+ StoresActivity.RESTART_STORES_ACTIVITY_BROADCAST_KEY;
		Intent restartStoresActivityIntent = new Intent(restartStoresActivityKey);
		restartStoresActivityIntent.putExtra("ActiveStoreID", mActiveStoreID);
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(restartStoresActivityIntent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("StoresDialogFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("ActiveListID", 0);
			mActiveStoreID = bundle.getLong("ActiveStoreID", 0);
			mDialogType = bundle.getInt("dialogType", 0);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("StoresDialogFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.i("StoresDialogFragment", "onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		MyLog.i("StoresDialogFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		MyLog.i("StoresDialogFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		MyLog.i("StoresDialogFragment", "onDismiss");
		super.onDismiss(dialog);
	}

	@Override
	public void onStart() {
		MyLog.i("StoresDialogFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		MyLog.i("StoresDialogFragment", "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		MyLog.i("StoresDialogFragment", "onDestroy");
		super.onDestroy();
	}

}
