package com.kaplandroid.smsforwarder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * 
 * @author KAPLANDROID
 * 
 */
public class SMSSettingActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {

	Button btnRehber, btnSave;
	EditText etPhoneNo;
	RadioButton rbOpen, rbClose;
	RelativeLayout rlForwardLayout;

	String DEBUG_TAG = "SMS Forwarder";

	private static final int PICK_CONTACT = 1001;
	public static final String KEY_NUMBER = "KEY_NUMBER";
	public static final String KEY_SHAREDNAME = "smsForward";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		etPhoneNo = (EditText) findViewById(R.id.etPhoneNo);
		btnRehber = (Button) findViewById(R.id.btnRehber);
		btnSave = (Button) findViewById(R.id.btnSave);
		rbClose = (RadioButton) findViewById(R.id.rbClose);
		rbOpen = (RadioButton) findViewById(R.id.rbOpen);
		rlForwardLayout = (RelativeLayout) findViewById(R.id.rlForwardLayout);

		if (isActive()) {
			rbOpen.setChecked(true);
			rlForwardLayout.setVisibility(View.VISIBLE);
			etPhoneNo.setText(getCurrentAddress());
		} else {
			rlForwardLayout.setVisibility(View.GONE);
			rbClose.setChecked(true);
		}

		btnRehber.setOnClickListener(this);
		btnSave.setOnClickListener(this);

		rbClose.setOnCheckedChangeListener(this);
		rbOpen.setOnCheckedChangeListener(this);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PICK_CONTACT:

				etPhoneNo.setText("");

				Uri contactData = data.getData();
				@SuppressWarnings("deprecation")
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {

					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + id, null, null);
						phones.moveToFirst();
						String cNumber = phones.getString(phones
								.getColumnIndex("data1"));

						cNumber = cNumber.replace("(", "");
						cNumber = cNumber.replace(")", "");
						cNumber = cNumber.replace("-", "");
						cNumber = cNumber.replace(" ", "");

						etPhoneNo.setText(cNumber);
					}

				}
				break;
			}
		} else {

			Log.w(DEBUG_TAG, getString(R.string.smsErrorGetNumber));
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btnRehber) {
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		} else if (v == btnSave) {
			if (rbOpen.isChecked()) {
				if (!etPhoneNo.getText().toString().equalsIgnoreCase("")) {
					saveNumber();
					etPhoneNo.setText(getCurrentAddress());
				} else {

					Toast.makeText(SMSSettingActivity.this,
							R.string.smsNumberEmpty, Toast.LENGTH_LONG).show();
				}
			} else {
				etPhoneNo.setText("");
				removeNumber();
			}
		}

	}

	private void removeNumber() {
		SharedPreferences prefs = getSharedPreferences(KEY_SHAREDNAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_NUMBER, null);
		editor.commit();

		Toast.makeText(SMSSettingActivity.this, R.string.smsDisabled,
				Toast.LENGTH_LONG).show();
	}

	private void saveNumber() {
		SharedPreferences prefs = getSharedPreferences(KEY_SHAREDNAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_NUMBER, etPhoneNo.getText().toString());
		editor.commit();

		Toast.makeText(SMSSettingActivity.this, R.string.smsEnabled,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if (v == rbClose) {
			if (isChecked) {
				rlForwardLayout.setVisibility(View.GONE);
			}
		} else if (v == rbOpen) {
			if (isChecked) {
				rlForwardLayout.setVisibility(View.VISIBLE);
				etPhoneNo.setText(getCurrentAddress());
			}
		}
	}

	private String getCurrentAddress() {
		SharedPreferences prefs = getSharedPreferences(KEY_SHAREDNAME, 0);
		String mNumber = prefs.getString(KEY_NUMBER, null);

		if (mNumber != null) {
			return mNumber;
		} else {
			return "";
		}
	}

	private boolean isActive() {
		SharedPreferences prefs = getSharedPreferences(KEY_SHAREDNAME, 0);
		String mNumber = prefs.getString(KEY_NUMBER, null);

		if (mNumber != null) {
			return true;
		} else {
			return false;
		}
	}

}