package com.lbconsulting.alist.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.LocationsTable;
import com.lbconsulting.alist.utilities.MyLog;

public class LocationsDialogFragment extends DialogFragment {

	public static final int EDIT_LOCATION_NAME = 35;
	public static final int NEW_LOCATION = 45;
	public static final int DELETE_LOCATION = 55;

	private Button btnApply;
	private Button btnCancel;
	private static EditText txtLocationName;

	// private long mActiveListID;
	private long mActiveLocationID;
	private int mDialogType;

	public LocationsDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Creates a new instance of the LocationsDialogFragment
	 * 
	 * @param listID
	 * @param locationID
	 * @param dialogType
	 * @return LocationsDialogFragment
	 */
	public static LocationsDialogFragment newInstance(long listID, long locationID, int dialogType) {
		if (listID < 1) {
			if (dialogType != EDIT_LOCATION_NAME) {
				MyLog.e("LocationsDialogFragment", "newInstance: invalid listID:" + listID);
			}
		}

		if (locationID < 1) {
			if (dialogType != NEW_LOCATION) {
				MyLog.e("LocationsDialogFragment", "newInstance: invalid locationID:" + locationID);
			}
		}

		LocationsDialogFragment f = new LocationsDialogFragment();
		// Supply itemID input as an argument.
		Bundle args = new Bundle();
		args.putLong("ActiveListID", listID);
		args.putLong("ActiveLocationID", locationID);
		args.putInt("dialogType", dialogType);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("LocationsDialogFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Store our mActiveLocationID
		// outState.putLong("ActiveListID", this.mActiveListID);
		outState.putLong("ActiveLocationID", this.mActiveLocationID);
		outState.putLong("dialogType", this.mDialogType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey("mActiveLocationID")) {
			// mActiveListID = savedInstanceState.getLong("ActiveListID", 0);
			mActiveLocationID = savedInstanceState.getLong("ActiveLocationID", 0);
			mDialogType = savedInstanceState.getInt("dialogType", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				// mActiveListID = bundle.getLong("ActiveListID", 0);
				mActiveLocationID = bundle.getLong("ActiveLocationID", 0);
				mDialogType = bundle.getInt("dialogType", 0);
			}
		}

		// inflate view
		View view = inflater.inflate(R.layout.dialog_location_create_edit_delete, container);

		switch (mDialogType) {

			case EDIT_LOCATION_NAME:
				MyLog.i("LocationsDialogFragment", "onCreateView: Edit Location Name");
				getDialog().setTitle("Edit Location Name?");
				break;

			case NEW_LOCATION:
				MyLog.i("LocationsDialogFragment", "onCreateView: New Location");
				getDialog().setTitle("Create New Location?");
				break;

			case DELETE_LOCATION:
				MyLog.i("LocationsDialogFragment", "onCreateView: Delete Location");
				getDialog().setTitle("Delete Location?");
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

						case EDIT_LOCATION_NAME:
							String newLocationName = txtLocationName.getText().toString();
							newLocationName = newLocationName.trim();
							if (!newLocationName.isEmpty()) {
								LocationsTable.UpdateLocationName(getActivity(), mActiveLocationID, newLocationName);
							}
							break;

						case NEW_LOCATION:
							newLocationName = txtLocationName.getText().toString();
							newLocationName = newLocationName.trim();
							if (!newLocationName.isEmpty()) {
								LocationsTable.CreateNewLocation(getActivity(), newLocationName);
							}
							break;

						case DELETE_LOCATION:
							LocationsTable.DeleteLocation(getActivity(), mActiveLocationID);
							break;

						default:
							break;
					}
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

			txtLocationName = (EditText) view.findViewById(R.id.txtLocationName);
			if (txtLocationName != null) {
				switch (mDialogType) {
					case EDIT_LOCATION_NAME:
						// We're displaying the Edit Location Name dialog
						String locationName = LocationsTable.getLocationName(getActivity(), mActiveLocationID);
						txtLocationName.setText(locationName);
						break;

					case NEW_LOCATION:
						// We're displaying the New Location dialog
						txtLocationName.setText("New Location");
						break;

					case DELETE_LOCATION:
						// We're displaying the Delete Location dialog
						locationName = LocationsTable.getLocationName(getActivity(), mActiveLocationID);
						txtLocationName.setText(locationName);
						break;

					default:
						break;

				}

				if (txtLocationName.requestFocus()) {
					getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				}
			}
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("LocationsDialogFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			// mActiveListID = bundle.getLong("ActiveListID", 0);
			mActiveLocationID = bundle.getLong("ActiveLocationID", 0);
			mDialogType = bundle.getInt("dialogType", 0);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("LocationsDialogFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.i("LocationsDialogFragment", "onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		MyLog.i("LocationsDialogFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		MyLog.i("LocationsDialogFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		MyLog.i("LocationsDialogFragment", "onDismiss");
		super.onDismiss(dialog);
	}

	@Override
	public void onStart() {
		MyLog.i("LocationsDialogFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		MyLog.i("LocationsDialogFragment", "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		MyLog.i("LocationsDialogFragment", "onDestroy");
		super.onDestroy();
	}

}
