package com.happytap.bangbang;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.smile.SmileFactory;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.Toast;

public class BangBangActivity extends Activity {

	protected BluetoothAdapter mBluetoothAdapter = null;

	// Member object for the chat services
	protected BluetoothService bluetooothService = null;
	
	protected String mConnectedDeviceName = null;	
	
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_DISCONNECTED = 6;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final int MESSAGE_WRITE = 3;

	protected ObjectMapper mObjectMapper = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mObjectMapper = new ObjectMapper(new SmileFactory());
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		bluetooothService = getBangApplication().getBluetoothService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (bluetooothService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (bluetooothService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				bluetooothService.start();
			}
		}
	}
	
	public static final String TOAST = "toast";
	
	protected BangBangApplication getBangApplication() {
		return (BangBangApplication)getApplication();
	}
	
}
