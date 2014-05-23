package com.lbconsulting.alist.ui.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.utilities.MyLog;

public class StoresFragment extends Fragment {

	private long mActiveListID = -1;
	private long mActiveStoreID = -1;
	private Cursor mActiveStoreCursor;

	private TextView tvStoreName;
	private EditText txtStreet1;
	private EditText txtStreet2;
	private EditText txtCity;
	private EditText txtState;
	private EditText txtZipCode;
	private EditText txtGPSLatitude;
	private EditText txtGPSLongitude;
	private EditText txtWebsiteURL;
	private EditText txtPhoneNumber;
	private Button btnApplyStoreChanges;

	public static StoresFragment newInstance(long listID, long newStoreID) {
		if (listID < 2) {
			MyLog.e("StoresFragment: newInstance; listID = " + listID, " is less than 2!!!!");
			return null;

		} else {
			StoresFragment f = new StoresFragment();
			// Supply listID and newStoreID input as arguments.
			Bundle args = new Bundle();
			args.putLong("listID", listID);
			args.putLong("storeID", newStoreID);
			f.setArguments(args);
			return f;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("StoresFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("StoresFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Store our listID and storeID
		outState.putLong("listID", this.mActiveListID);
		outState.putLong("storeID", this.mActiveStoreID);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("StoresFragment", "onCreateView. ");
		if (savedInstanceState != null && savedInstanceState.containsKey("listID")) {
			mActiveListID = savedInstanceState.getLong("listID", 0);
			mActiveStoreID = savedInstanceState.getLong("storeID", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("listID", 0);
				mActiveStoreID = bundle.getLong("storeID", 0);
			}
		}

		View view = inflater.inflate(R.layout.frag_stores, container, false);
		if (view != null) {

			tvStoreName = (TextView) view.findViewById(R.id.tvStoreName);
			txtStreet1 = (EditText) view.findViewById(R.id.txtStreet1);
			txtStreet2 = (EditText) view.findViewById(R.id.txtStreet2);
			txtCity = (EditText) view.findViewById(R.id.txtCity);
			txtState = (EditText) view.findViewById(R.id.txtState);
			txtZipCode = (EditText) view.findViewById(R.id.txtZipCode);
			txtGPSLatitude = (EditText) view.findViewById(R.id.txtGPSLatitude);
			txtGPSLongitude = (EditText) view.findViewById(R.id.txtGPSLongitude);
			txtWebsiteURL = (EditText) view.findViewById(R.id.txtWebsiteURL);
			txtPhoneNumber = (EditText) view.findViewById(R.id.txtPhoneNumber);
			btnApplyStoreChanges = (Button) view.findViewById(R.id.btnApplyStoreChanges);

			mActiveStoreCursor = StoresTable.getStore(getActivity(), mActiveStoreID);
			if (mActiveStoreCursor != null & mActiveStoreCursor.getColumnCount() > 0) {
				mActiveStoreCursor.moveToFirst();
				String item = "";
				if (tvStoreName != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_STORE_NAME));
					if (item == null) {
						item = "";
					}
					tvStoreName.setText(item);
				}

				if (txtStreet1 != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_STREET1));
					if (item == null) {
						item = "";
					}
					txtStreet1.setText(item);
				}

				if (txtStreet2 != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_STREET2));
					if (item == null) {
						item = "";
					}
					txtStreet2.setText(item);
				}

				if (txtCity != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_CITY));
					if (item == null) {
						item = "";
					}
					txtCity.setText(item);
				}

				if (txtState != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_STATE));
					if (item == null) {
						item = "";
					}
					txtState.setText(item);
				}

				if (txtZipCode != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_ZIP));
					if (item == null) {
						item = "";
					}
					txtZipCode.setText(item);
				}

				if (txtGPSLatitude != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_GPS_LATITUDE));
					if (item == null) {
						item = "";
					}
					txtGPSLatitude.setText(item);
				}

				if (txtGPSLongitude != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_GPS_LONGITUDE));
					if (item == null) {
						item = "";
					}
					txtGPSLongitude.setText(item);
				}

				if (txtWebsiteURL != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_WEBSITE_URL));
					if (item == null) {
						item = "";
					}
					txtWebsiteURL.setText(item);
				}

				if (txtPhoneNumber != null) {
					item = mActiveStoreCursor.getString(mActiveStoreCursor
							.getColumnIndexOrThrow(StoresTable.COL_PHONE_NUMBER));
					if (item == null) {
						item = "";
					}
					txtPhoneNumber.setText(item);
				}

			}
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("StoresFragment", "onActivityCreated");
		getActivity().getActionBar().setTitle("Manage Stores");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		MyLog.i("StoresFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		MyLog.i("StoresFragment", "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("StoresFragment", "onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		MyLog.i("StoresFragment", "onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		MyLog.i("StoresFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		MyLog.i("StoresFragment", "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		MyLog.i("StoresFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public View getView() {
		MyLog.i("StoresFragment", "getView");
		return super.getView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		MyLog.i("StoresFragment", "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

}
