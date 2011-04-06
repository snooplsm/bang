/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.happytap.bangbang;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.InterstitialAd;
import com.admob.android.ads.InterstitialAdListener;
import com.admob.android.ads.InterstitialAd.Event;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.encode.QRCodeEncoder;

/**
 * This is the main Activity that displays the current chat session.
 */
public class ConnectActivity extends BangBangActivity implements 
		 InterstitialAdListener {
	
	private static final boolean D = true;

	// Key names received from the BluetoothChatService Handler
	



	// Message types sent from the BluetoothChatService Handler
	

	private static byte PROPERTY_CHANGED = 0;

	private static final int REQUEST_ADDRESS = 3;
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private static final int SHARE_BARCODE_DIMENSION = 300;

	
	// Debugging
	private static final String TAG = "BangBang";
	
	private ImageView addressQRCode;
	

	// Name of the connected device


	// The Handler that gets information back from the BluetoothChatService
	

	
	// private EditText mOutEditText;

	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;

	// private Button mSendButton;

	


	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}




	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				bluetooothService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				drawQRCode();
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case REQUEST_ADDRESS:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getStringExtra("SCAN_RESULT");
//				String format = data.getStringExtra("SCAN_RESULT_FORMAT");
				if (BluetoothAdapter.checkBluetoothAddress(address)) {
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(address);
					getWindow().makeActive();
					bluetooothService.connect(device);
				}
			}
		}

	}


	
	private void drawQRCode() {
		if (mBluetoothAdapter.isEnabled()) {

			String address = mBluetoothAdapter.getAddress();
			Bitmap bitmap;
			try {
				DisplayMetrics m = getResources().getDisplayMetrics();
				int square = m.heightPixels / 3;
				addressQRCode = (ImageView) findViewById(R.id.address_qr_code);
				bitmap = QRCodeEncoder.encodeAsBitmap(address,
						BarcodeFormat.QR_CODE, square, square);
				LayoutParams frame = addressQRCode.getLayoutParams();
				frame.width = square;
				frame.height = square;
				addressQRCode.setLayoutParams(frame);
				addressQRCode.setImageBitmap(bitmap);
			} catch (WriterException we) {
				Log.w(TAG, we);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
//				R.layout.custom_title);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		View barcodeConnect = findViewById(R.id.connect_via_barcode);

		barcodeConnect.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {				
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.setPackage("com.google.zxing.client.android");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				final PackageManager packageManager = getPackageManager();
			    List<ResolveInfo> list =
			            packageManager.queryIntentActivities(intent,
			                    PackageManager.MATCH_DEFAULT_ONLY);
			    if(list!=null && list.size()>0) {
			    	startActivityForResult(intent, REQUEST_ADDRESS);
			    } else {
			    	showDialog(DIALOG_NEED_ZXING);			    	
			    }
			}

		});

		View barcodeText = findViewById(R.id.connect_via_barcode_text);

		AdManager
				.setTestDevices(new String[] { "49FDCD50FF5E92B922A8EDE86616B3EF" });
		interstitialAd = new InterstitialAd(Event.POST_ROLL, this);
		interstitialAd.requestAd(this);

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_NEED_ZXING:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setCancelable(true);
			b.setIcon(R.drawable.launcher_icon);
			b.setTitle("Barcode Scanner");
			b.setMessage("You need the ZXing Barcode Scanner for this feature");
			b.setPositiveButton("Install", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://search?q=pname:com.google.zxing.client.android"));
					startActivity(intent);
				}
				
			});
			return b.create();
		}
		return super.onCreateDialog(id);
	}



	private static final int DIALOG_NEED_ZXING = 1;

	public void onFailedToReceiveInterstitial(InterstitialAd ad) {

	}

	public void onReceiveInterstitial(InterstitialAd arg0) {
		hasAd = true;
	}

	private InterstitialAd interstitialAd;

	private boolean hasAd;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (bluetooothService != null) {
			bluetooothService.stop();
		}
		if (interstitialAd != null) {
			interstitialAd.setListener(null);
		}
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}


	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		if(mBluetoothAdapter.isEnabled()) {
			drawQRCode();
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);			
			startActivityForResult(intent,REQUEST_ENABLE_BT);
		}
		
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		

	}

	



	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (bluetooothService == null)
				setupChat();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}


	// private void setSpinnerValue(int id, int index) {
	// Log.i(getClass().getSimpleName(), "" + id + "," + index);
	// getSpinner(id).setSelection(index);
	// }

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:				
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					break;
				case BluetoothService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					// mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				startGameScreen();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	
	private void startGameScreen() {
		getBangApplication().setBluetoothService(bluetooothService);
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}
	
	private void setupChat() {
		Log.d(TAG, "setupChat()");
		// Initialize the BluetoothChatService to perform bluetooth connections
		bluetooothService = new BluetoothService(this, mHandler);
	}

}