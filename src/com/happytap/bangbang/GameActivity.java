package com.happytap.bangbang;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GameActivity extends BangBangActivity implements SensorEventListener,OnClickListener, View.OnTouchListener {

	private static final int CAN_SHOOT_NEEDS_TO_BE_HOLSTERED = -1;

	private static final int CAN_SHOOT_NO = 0;

	private static byte BEGIN_GAME = 1;
	
	/**
	 * ARE WE ON PISTOL SCREEN? WHAT SCREEN ARE WE ON?
	 */

	private static final int INTERNAL_STATE_HOME_SCREEN = 0;
	// private static final int INTERNAL_STATE_GAME
	private int internalState;

	
	private static final int CAN_SHOOT_YES = 1;

	private long beganShowingWinLostOverlay;

	private BeginGame beginGame;
	
	private int canShoot;
	
	private boolean gameOver;
	
	private Sensor gyroscope;
	
	HolsterWeaponOverlayView holsterWeaponOverlay;

	private boolean isClient;

	private GunNotHolstered lastReceivedGunNotHolstered;
	
	private long lastReceivedGunNotHolsteredTimestamp;
	
	private boolean lefty = true;
	
	private AudioManager mAudioManager;
	
	private Vibrator mVibrator;

	private boolean onPistolScreen;

	private ImageView pistol;

	private RelativeLayout pistolContainer;

	private Random random;

	int sensorCount = 0;
	

	public void onAccuracyChanged(Sensor sensor, int i) {
	}

	private SensorManager sensorManager;

	private boolean sentGunHolstered;

	private boolean sentGunNotHolstered;

	private boolean showingHolsterWeaponOverlay = false;

	private boolean showingWinLostGameOverlay = false;

	private SoundPool soundPool;

	private Timer timer;

	private MediaPlayer whistleMusic;

	private boolean whistleWasPlaying;
	
	WinLostGameOverlayView winLostGameOverlay;

	private boolean won = false;
	
	private static final String TAG = "GameActivity";
	
	private SensorEvent mLastSensorEvent = null;

	
	private void sendShot() {
		// Check that we're actually connected before trying anything
		if (bluetooothService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (mLastSensorEvent != null) {
			ShotFired f = new ShotFired();

			f.setX(mLastSensorEvent.values[0]);
			f.setY(mLastSensorEvent.values[1]);
			f.setZ(mLastSensorEvent.values[2]);

			send(SHOT, f);
		}
	}
	
	public boolean onTouch(View view, MotionEvent motionEvent) {
		Log.i("bang", "trying to shoot, current canShoot value is " + canShoot);
		if (canShoot == CAN_SHOOT_YES) {
			sendShot();
		}
		return false;
	}
	
	public void onClick(View view) {
		isClient = false;
		startGame();
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		deRegisterListeners();
		whistleWasPlaying = whistleMusic.isPlaying();
		if (whistleWasPlaying) {
			whistleMusic.pause();
		}
	}

	private void send(byte instruction, Serializable object) {
		if (bluetooothService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		try {
			byte[] previewPacket = new byte[5];
			previewPacket[0] = instruction;
			byte[] data = mObjectMapper.writeValueAsBytes(object);
			int value = data.length;
			previewPacket[1] = (byte) (value >>> 24);
			previewPacket[2] = (byte) (value >> 16 & 0xff);
			previewPacket[3] = (byte) (value >> 8 & 0xff);
			previewPacket[4] = (byte) (value & 0xff);
			bluetooothService.write(previewPacket, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static byte GUN_HOLSTERED = 5;
	private static byte GUN_NOT_HOLSTERED = 4;
	private static byte GUN_NOT_HOLSTERED_ON_BEGIN_DUEL = 6;
	private static byte HIT = 3;

	private void onGunNotHolstered() {
		if (!whistleMusic.isPlaying()) {
			if (sensorCount % 3 == 0) {
				whistleMusic.start();
				if (!sentGunNotHolstered) {
					GunNotHolstered gnh = new GunNotHolstered();
					gnh.setCurrentPosition(whistleMusic.getCurrentPosition());
					send(GUN_NOT_HOLSTERED, gnh);
					sentGunNotHolstered = true;
					sentGunHolstered = false;
				}
			}
			if (sensorCount % 5 == 0) {

			}
		}
		if (!showingHolsterWeaponOverlay) {
			clearViews();
			pistolContainer.addView(getHolsterWeaponOverlayView());
			showingHolsterWeaponOverlay = true;
		}
	}
	
	private static byte SHOT = 2;
	
	private void beginGame(BeginGame game) {
		Log.i(TAG, "beginGame");
		clearViews();
		if (timer != null) {
			timer.cancel();
		}
		this.beginGame = game;
		Log.i(TAG, "setting can shoot to false, current value is " + canShoot);
		mLastSensorEvent = null;
		canShoot = CAN_SHOOT_NO;
		gameOver = false;
		won = false;
		timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Log.i(TAG, "setting can shoot to true, current value is "
						+ canShoot);
				mVibrator.vibrate(120);
				if (isPistolPointingAtGround()) {
					canShoot = CAN_SHOOT_YES;
				} else {
					canShoot = CAN_SHOOT_NEEDS_TO_BE_HOLSTERED;
				}
			}
		};
		long time = (long) (beginGame.getSecondsUntilDuel() * 1000f - (isClient ? 80
				: 0));
		timer.schedule(task, time);
		if (pistol == null) {
			setContentView(R.layout.pistol);
			pistol = (ImageView) findViewById(R.id.pistol);
			pistolContainer = (RelativeLayout) findViewById(R.id.pistol_container);
		}
		onPistolScreen = true;
		clearViews();
		registerListeners();
	}

	private void clearViews() {
		if (showingHolsterWeaponOverlay) {
			pistolContainer.removeView(holsterWeaponOverlay);
			showingHolsterWeaponOverlay = false;
		}
		if (showingWinLostGameOverlay) {
			pistolContainer.removeView(winLostGameOverlay);
		}
	}

	private void deRegisterListeners() {
		Log.i("bang", "deregistering listeners");
		sensorManager.unregisterListener(this);
	}

	private View getHolsterWeaponOverlayView() {
		if (holsterWeaponOverlay == null) {
			holsterWeaponOverlay = new HolsterWeaponOverlayView(this);
		}
		return holsterWeaponOverlay;
	}

	private Random getRandom() {
		if (random == null) {
			random = new Random();
		}
		return random;
	}

	private View getWinLostGameOverlayView(boolean won) {
		if (winLostGameOverlay == null) {
			winLostGameOverlay = new WinLostGameOverlayView(this);
			winLostGameOverlay.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (System.currentTimeMillis() - beganShowingWinLostOverlay > 3000) {
						startGame();
					}
					return true;
				}
			});
		}
		beganShowingWinLostOverlay = System.currentTimeMillis();

		if (won) {
			winLostGameOverlay.setWinLossText("You Won!");
			winLostGameOverlay
					.setBackgroundColor(Color.argb(100, 34, 122, 245));
			winLostGameOverlay.setTextColor(Color.rgb(255, 255, 255));
		} else {
			winLostGameOverlay.setWinLossText("You Died!");
			winLostGameOverlay.setBackgroundColor(Color.argb(100, 230, 41, 41));
			winLostGameOverlay.setTextColor(Color.rgb(255, 255, 255));
		}
		return winLostGameOverlay;
	}

	private boolean isPistolPointingAtGround() {
		if(mLastSensorEvent==null) {
			return false;
		}
		float y = mLastSensorEvent.values[1];
		if (y > -8.5 && y > -9.5) {
			return false;
		}
		return true;
	}

	private void iWasShot(ShotFired shot) {
		if (!won) {
			gameOver = true;
			ShotHit h = new ShotHit();
			send(HIT, h);
			mVibrator.vibrate(1000);
			deRegisterListeners();
			showGameOver(won);
		}
	}
	@Override
	protected void onResume() {
	
		super.onResume();
		
		if (onPistolScreen) {
			registerListeners();
		}
		if (whistleWasPlaying) {
			whistleMusic.start();
		}
	}

	public void onSensorChanged(SensorEvent sensorEvent) {
		mLastSensorEvent = sensorEvent;
		sensorCount++;
		if (onPistolScreen) {
			if (gameOver) {
				return;
			}
			if (canShoot == CAN_SHOOT_YES) {
				if (whistleMusic.isPlaying()) {
					whistleMusic.pause();
					return;
				}
				if (showingHolsterWeaponOverlay) {
					pistolContainer.removeView(getHolsterWeaponOverlayView());
					showingHolsterWeaponOverlay = false;
				}

			}
			boolean isPistolPointingAtGround = isPistolPointingAtGround();
			if (canShoot == CAN_SHOOT_NO && !isPistolPointingAtGround) {
				onGunNotHolstered();
			} else if ((canShoot == CAN_SHOOT_NEEDS_TO_BE_HOLSTERED || canShoot == CAN_SHOOT_NO)
					&& isPistolPointingAtGround) {
				if (canShoot == CAN_SHOOT_NEEDS_TO_BE_HOLSTERED) {
					canShoot = CAN_SHOOT_YES;
				}
				onGunHolstered();
			}
		}
	}

	private void registerListeners() {
		Log.i(TAG, "registering listeners");
		sensorManager.registerListener(this, gyroscope,
				SensorManager.SENSOR_DELAY_GAME);
		pistol.setOnTouchListener(this);
	}

	private void onGunHolstered() {
		if (sensorCount % 3 == 0) {
			whistleMusic.pause();
			if (!sentGunNotHolstered) {
				GunHolstered gh = new GunHolstered();
				send(GUN_NOT_HOLSTERED, gh);
				sentGunHolstered = true;
				sentGunNotHolstered = false;
			}
		}
		if (showingHolsterWeaponOverlay) {
			pistolContainer.removeView(getHolsterWeaponOverlayView());
			showingHolsterWeaponOverlay = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_game);
		View startGame = findViewById(R.id.start_game);
		startGame.setOnClickListener(this);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		

		whistleMusic = MediaPlayer.create(this, R.raw.whistle_song);
		whistleMusic.setLooping(true);
		
		mDataHandler.put(SHOT, new DataHandler<ShotFired>() {
			public Class<ShotFired> getDataClass() {
				return ShotFired.class;
			}

			public Byte getInstructionByte() {
				return SHOT;
			}

			public void process(ShotFired data) {
				if (data.getY() > -2.21 && data.getY() < 2.21) {
					iWasShot(data);
				}
			}
		});

		mDataHandler.put(HIT, new DataHandler<ShotHit>() {
			public Class<ShotHit> getDataClass() {
				return ShotHit.class;
			}

			public Byte getInstructionByte() {
				return HIT;
			}

			public void process(ShotHit data) {
				won = true;
				gameOver = true;
				deRegisterListeners();
				showGameOver(won);
			}
		});
		

		mDataHandler.put(GUN_NOT_HOLSTERED, new DataHandler<GunNotHolstered>() {

			public Class<GunNotHolstered> getDataClass() {
				return GunNotHolstered.class;
			}

			public Byte getInstructionByte() {
				return GUN_NOT_HOLSTERED;
			}

			public void process(GunNotHolstered data) {
				lastReceivedGunNotHolsteredTimestamp = System
						.currentTimeMillis();
				lastReceivedGunNotHolstered = data;
				long diff = System.currentTimeMillis()
						- lastReceivedGunNotHolsteredTimestamp;
				diff += lastReceivedGunNotHolstered.getCurrentPosition();
				diff += 5;
				whistleMusic.seekTo((int) diff);
			}
		});

		mDataHandler.put(GUN_HOLSTERED, new DataHandler<GunHolstered>() {
			public Class<GunHolstered> getDataClass() {
				return GunHolstered.class;
			}

			public Byte getInstructionByte() {
				return GUN_HOLSTERED;
			}

			public void process(GunHolstered data) {
			}
		});
		

		mDataHandler.put(BEGIN_GAME, new DataHandler<BeginGame>() {
			public Class<BeginGame> getDataClass() {
				return BeginGame.class;
			}

			public Byte getInstructionByte() {
				return BEGIN_GAME;
			}

			public void process(BeginGame data) {
				isClient = true;
				beginGame(data);
			}
		});

		mDataHandler.put(GUN_NOT_HOLSTERED_ON_BEGIN_DUEL,
				new DataHandler<GunNotHolsteredOnDuelStart>() {
					public Class<GunNotHolsteredOnDuelStart> getDataClass() {
						return GunNotHolsteredOnDuelStart.class;
					}

					public Byte getInstructionByte() {
						return GUN_NOT_HOLSTERED_ON_BEGIN_DUEL;
					}

					public void process(GunNotHolsteredOnDuelStart data) {
					}
				});


		bluetooothService.setmHandler(mHandler);
		
	}
	
	

	private void startGame() {
		BeginGame bg = new BeginGame();
		int index = 1;
		float seconds;
		Random random = getRandom();
		switch (index) {
		case 0:
			seconds = random.nextInt(9);
			seconds += random.nextFloat();
			break;
		case 1:
			seconds = random.nextInt(5) + 4;
			seconds += random.nextFloat();
		case 2:
			seconds = 5;
			break;
		case 3:
			seconds = 10;
			break;
		case 4:
			seconds = 0;
			break;
		default:
			seconds = 5;
			break;
		}
		bg.setSecondsUntilDuel(seconds);
		send(BEGIN_GAME, bg);
		beginGame(bg);
	}
	
	protected Map<Byte, DataHandler> mDataHandler = new HashMap<Byte, DataHandler>();

	private void showGameOver(boolean won) {
		clearViews();
		if (won == true) {
			System.out.println("I WON!");
		}
		pistolContainer.addView(getWinLostGameOverlayView(won));
		showingWinLostGameOverlay = true;
//		if (hasAd) {
//			// interstitialAd.show(this);
//		}
	}
	
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
			case MESSAGE_READ:
				BangBangMessage bbm = (BangBangMessage) msg.obj;
				byte[] readBuf = bbm.getPacket();
				// construct a string from the valid bytes in the buffer
				byte protocol = bbm.getPreviewPacket()[0];
				DataHandler<Object> dhandler = mDataHandler.get(protocol);
				try {
					dhandler.process(mObjectMapper.readValue(readBuf, 0, bbm
							.getPacketSize(), dhandler.getDataClass()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
}
