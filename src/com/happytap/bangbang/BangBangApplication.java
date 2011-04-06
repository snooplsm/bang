package com.happytap.bangbang;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class BangBangApplication extends Application {

	private BluetoothService bluetoothService;
	
	private SharedPreferences preferences;

	@Override
	public void onCreate() {
		super.onCreate();
		preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
	}

	BluetoothService getBluetoothService() {
		return bluetoothService;
	}

	void setBluetoothService(BluetoothService bluetoothService) {
		this.bluetoothService = bluetoothService;
	}
	
	public boolean isMuted() {
		return preferences.getBoolean("muted", true);
	}
	
	public void setMuted(boolean muted) {
		preferences.edit().putBoolean("muted", muted).commit();
	}
	
	
}
