package com.happytap.bangbang;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class SplashScreenActivity extends Activity {
	
	private MediaPlayer music;
	
	private View findOpponent;
	private View about;
	private View tutorial;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		if(System.currentTimeMillis()>1304525814727l) {
			showDialog(DIALOG_APPLICATION_EXPIRED);
		} 
		music = MediaPlayer.create(this, R.raw.whistle_song);
		music.setLooping(true);
		findOpponent = findViewById(R.id.find_opponent);
		findOpponent.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(SplashScreenActivity.this,ConnectActivity.class);
				startActivity(intent);				
			}
			
		});
		about = findViewById(R.id.about);
		tutorial = findViewById(R.id.tutorial);
		OnClickListener all = new OnClickListener() {
			public void onClick(View v) {
				final Intent intent;
				if(v==about) {
					intent = new Intent(SplashScreenActivity.this, AboutActivity.class);
				} else {
					intent = new Intent(SplashScreenActivity.this, TutorialActivity.class);
				}
				startActivity(intent);
			}
		};
		about.setOnClickListener(all);
		tutorial.setOnClickListener(all);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		stopMusic();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		playMusic();
	}


	private static final int DIALOG_APPLICATION_EXPIRED = 1;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_APPLICATION_EXPIRED:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setCancelable(false);
			b.setTitle("Demo Expired");
			b.setMessage("The demo for Bang Bang has expired.  Check your market for an update.");
			b.setIcon(R.drawable.icon);
			b.setNeutralButton("Alright", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			return b.create();
		}
		return super.onCreateDialog(id);
	}
	
	private static final int MENU_MUTE = 1;
	private static final int MENU_UNMUTE = 2;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, MENU_MUTE, Menu.NONE, "Audio off").setIcon(android.R.drawable.ic_lock_silent_mode);
		menu.add(Menu.NONE, MENU_UNMUTE, Menu.NONE, "Audio on").setIcon(android.R.drawable.ic_lock_silent_mode_off);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem muted = menu.findItem(MENU_MUTE);
		MenuItem unmuted = menu.findItem(MENU_UNMUTE);
		boolean mutedB = getBangApplication().isMuted();
		muted.setVisible(!mutedB);
		unmuted.setVisible(mutedB);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case MENU_MUTE:
		case MENU_UNMUTE:
			getBangApplication().setMuted(!getBangApplication().isMuted());
			playMusic();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void playMusic() {
		if(music==null) return;
		if(!getBangApplication().isMuted()) {
			music.start();
		} else {
			music.pause();
		}
	}
	
	private void stopMusic() {
		if(music!=null) {
			music.pause();
		}
	}
	
	private BangBangApplication getBangApplication() {
		return (BangBangApplication)getApplication();
	}
}
