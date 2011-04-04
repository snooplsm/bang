package com.happytap.bangbang;

import android.app.Application;

public class BangBangApplication extends Application {

	private BluetoothService bluetoothService;

	BluetoothService getBluetoothService() {
		return bluetoothService;
	}

	void setBluetoothService(BluetoothService bluetoothService) {
		this.bluetoothService = bluetoothService;
	}
	
	
	
}
