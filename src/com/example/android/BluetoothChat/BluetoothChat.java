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

package com.example.android.BluetoothChat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.smile.SmileFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity implements SensorEventListener {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
//    private TextView mTitle;
//    private View mTopLevelTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;


    private SoundPool soundPool;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private SensorEvent mLastSensorEvent = null;

    private ObjectMapper mObjectMapper = null;

    private SensorManager sensorManager;
    private Sensor gyroscope;

    private Vibrator mVibrator;

    private Map<Byte, DataHandler> mDataHandler = new HashMap<Byte, DataHandler>();

    private boolean lefty = true;

    private boolean canShoot;

    private boolean onPistolScreen;

    private MediaPlayer whistleMusic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        //mTitle = (TextView) findViewById(R.id.title_left_text);
        //mTitle.setText(R.string.app_name);
        //mTitle = (TextView) findViewById(R.id.title_right_text);
        //mTopLevelTitle = findViewById(R.id.top_level_title);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mObjectMapper = new ObjectMapper(new SmileFactory());

        whistleMusic = MediaPlayer.create(this, R.raw.whistle_song);
        whistleMusic.setLooping(true);
    }

    private AudioManager mAudioManager;

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
        registerListeners();
        if(whistleWasPlaying) {
            whistleMusic.start();
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setOnTouchListener(new android.view.View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(canShoot) {
                    sendShot();
                }
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        mDataHandler.put(SHOT, new DataHandler<ShotFired>() {
            public Class<ShotFired> getDataClass() {
                return ShotFired.class;
            }

            public Byte getInstructionByte() {
                return SHOT;
            }

            public void process(ShotFired data) {
                if(data.getY() > -2.21 && data.getY() < 2.21) {
                    iWasShot(data);
                }
            }
        });

        mDataHandler.put(HIT, new DataHandler<ShotHit>() {
            public Class<ShotHit> getDataClass() {
                return ShotHit.class;
            }

            public void process(ShotHit data) {
                won = true;
                showGameOver(won);
            }

            public Byte getInstructionByte() {
                return HIT;
            }
        });



        mDataHandler.put(GUN_NOT_HOLSTERED, new DataHandler<GunNotHolstered>() {

            public Class<GunNotHolstered> getDataClass() {
                return GunNotHolstered.class;
            }

            public void process(GunNotHolstered data) {
                lastReceivedGunNotHolsteredTimestamp = System.currentTimeMillis();
                lastReceivedGunNotHolstered = data;
                long diff = System.currentTimeMillis() - lastReceivedGunNotHolsteredTimestamp;
                diff+=lastReceivedGunNotHolstered.getCurrentPosition();
                diff+=5;
                whistleMusic.seekTo((int)diff);
            }

            public Byte getInstructionByte() {
                return GUN_NOT_HOLSTERED;
            }
        });

        mDataHandler.put(GUN_HOLSTERED, new DataHandler<GunHolstered>() {
            public Class<GunHolstered> getDataClass() {
                return GunHolstered.class;
            }

            public void process(GunHolstered data) {
            }

            public Byte getInstructionByte() {
                return GUN_HOLSTERED;
            }
        });

        //mDataHandler.put(GUN_NOT_HOLSTERED, new DataHandler<GunHolstered>)

        mDataHandler.put(PROPERTY_CHANGED, new DataHandler<PropertyChanged>() {
            public Class<PropertyChanged> getDataClass() {
                return PropertyChanged.class;
            }

            public void process(PropertyChanged data) {
                if(R.id.choose_seconds==data.getId() || R.id.choose_announce==data.getId()) {
                    setSpinnerValue(data.getId(), (Integer)data.getValue());
                }
            }

            public Byte getInstructionByte() {
                return PROPERTY_CHANGED;
            }
        });

        mDataHandler.put(BEGIN_GAME, new DataHandler<BeginGame>() {
            public Class<BeginGame> getDataClass() {
                return BeginGame.class;
            }

            public void process(BeginGame data) {
                isClient = true;
                beginGame(data);
            }

            public Byte getInstructionByte() {
                return BEGIN_GAME;
            }
        });

        mDataHandler.put(GUN_NOT_HOLSTERED_ON_BEGIN_DUEL, new DataHandler<GunNotHolsteredOnDuelStart>() {
            public Class<GunNotHolsteredOnDuelStart> getDataClass() {
                return GunNotHolsteredOnDuelStart.class;
            }

            public void process(GunNotHolsteredOnDuelStart data) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public Byte getInstructionByte() {
                return GUN_NOT_HOLSTERED_ON_BEGIN_DUEL;
            }
        });
    }

    private GunNotHolstered lastReceivedGunNotHolstered;
    private long lastReceivedGunNotHolsteredTimestamp;

    private boolean isClient;

    private static byte SHOT = 2;

    private static byte HIT = 3;

    private static byte BEGIN_GAME = 1;

    private static byte PROPERTY_CHANGED = 0;

    private static byte GUN_NOT_HOLSTERED = 4;

    private static byte GUN_HOLSTERED = 5;

    private static byte GUN_NOT_HOLSTERED_ON_BEGIN_DUEL=6;

    private boolean whistleWasPlaying;

    @Override
    public synchronized void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        whistleWasPlaying = whistleMusic.isPlaying();
        if(whistleWasPlaying) {
            whistleMusic.pause();
        }
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    private void iWasShot(ShotFired shot) {
        if(!won) {
            ShotHit h = new ShotHit();
            send(HIT,h);
            mVibrator.vibrate(1000);
            showGameOver(won);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void setSpinnerValue(int id, int index) {
        Log.i(getClass().getSimpleName(),""+id+","+index);
        getSpinner(id).setSelection(index);
    }

    private Spinner getSpinner(int id) {
        return (Spinner)findViewById(id);
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private BeginGame beginGame;

    private ImageView pistol;
    private RelativeLayout pistolContainer;

    private void beginGame(BeginGame game) {
        this.beginGame = game;
        Thread waitThread = new Thread() {
            @Override
            public void run() {
                long time = (long)(beginGame.getSecondsUntilDuel() * 1000f - (isClient ? 80 : 0));
                try {
                    //Toast.makeText(BluetoothChat.this,"GunSling in " + time, Toast.LENGTH_LONG).show();
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                canShoot = true;
                Log.i("bluetooth","vibrating");
                mVibrator.vibrate(90);
            }
        };
        waitThread.start();
        setContentView(R.layout.pistol);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        pistol = (ImageView) findViewById(R.id.pistol);
        pistolContainer = (RelativeLayout) findViewById(R.id.pistol_container);
        registerListeners();
        pistol.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sendShot();
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        onPistolScreen = true;
    }



    private void send(byte instruction, Serializable object) {
         if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] previewPacket = new byte[5];
            previewPacket[0] = instruction;
            byte[] data = mObjectMapper.writeValueAsBytes(object);
            int value = data.length;
            previewPacket[1] = (byte)(value >>> 24);
            previewPacket[2] = (byte)(value >> 16 & 0xff);
            previewPacket[3]= (byte)(value >> 8 & 0xff);
            previewPacket[4] = (byte)(value & 0xff);
            mChatService.write(previewPacket,data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean won = false;

    private void sendShot() {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        ShotFired f = new ShotFired();

        f.setX(mLastSensorEvent.values[0]);
        f.setY(mLastSensorEvent.values[1]);
        f.setZ(mLastSensorEvent.values[2]);

        send(SHOT,f);
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    //mTitle.setText(R.string.title_connected_to);
                    //mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    //mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    //mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = ((byte[][]) msg.obj)[1];
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                BangBangMessage bbm = (BangBangMessage) msg.obj;
                byte[] readBuf = bbm.getPacket();
                // construct a string from the valid bytes in the buffer
                byte protocol = bbm.getPreviewPacket()[0];
                DataHandler dhandler = mDataHandler.get(protocol);
                try {
                    dhandler.process(mObjectMapper.readValue(readBuf, 0, bbm.getPacketSize(), dhandler.getDataClass()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //String readMessage = new String(readBuf, 0, msg.arg1);
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                initializeNewGameMenu();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    private void initializeNewGameMenu() {
        setContentView(R.layout.new_game);
        final Spinner chooseSeconds = getSpinner(R.id.choose_seconds);
        final Spinner chooseAnnounce = getSpinner(R.id.choose_announce);

        ArrayAdapter<CharSequence> chooseSecondsAdapter = ArrayAdapter.createFromResource(BluetoothChat.this,R.array.seconds_array, android.R.layout.simple_spinner_item);
        chooseSecondsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseSeconds.setAdapter(chooseSecondsAdapter);
        AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PropertyChanged c = new PropertyChanged();
                c.setId(adapterView.getId());
                c.setValue(i);
                c.setTimestamp(System.currentTimeMillis());
                send(PROPERTY_CHANGED,c);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        chooseSeconds.setOnItemSelectedListener(selectedListener);
        final ArrayAdapter<CharSequence> chooseAnnounceAdapter = ArrayAdapter.createFromResource(BluetoothChat.this,R.array.announce_array, android.R.layout.simple_spinner_item);
        chooseAnnounceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseAnnounce.setAdapter(chooseAnnounceAdapter);
        chooseAnnounce.setOnItemSelectedListener(selectedListener);

        View startGame = findViewById(R.id.start_game);
        startGame.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BeginGame bg = new BeginGame();
                bg.setAnnounceDuelIndex(chooseAnnounce.getSelectedItemPosition());
                //int index = chooseSeconds.getSelectedItemPosition();
                int index = 1;
                float seconds;
                Random random = getRandom();
                switch(index) {
                    case 0:
                        seconds = random.nextInt(9);
                        seconds+= random.nextFloat();
                        break;
                    case 1:
                        seconds = random.nextInt(5) + 4;
                        seconds+= random.nextFloat();
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
        });
    }

    private Random random;

    private Random getRandom() {
        if(random==null) {
            random = new Random();
        }
        return random;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
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

    int sensorCount = 0;

    public void onSensorChanged(SensorEvent sensorEvent) {
        mLastSensorEvent = sensorEvent;
        sensorCount++;
        if(onPistolScreen) {
            float y = sensorEvent.values[1];
            if(canShoot) {
                whistleMusic.pause();
                return;
            }
            if(y>-8.5 && y>-9.5) {
               onGunNotHolstered();
            } else {
               onGunHolstered();
            }
        }
    }

    private boolean sentGunNotHolstered;
    private boolean sentGunHolstered;

    private Toast holsterWeapon;

    private void onGunNotHolstered() {
        if(!whistleMusic.isPlaying()) {
            if(sensorCount%3==0) {
                whistleMusic.start();
                if(!sentGunNotHolstered) {
                    GunNotHolstered gnh = new GunNotHolstered();
                    gnh.setCurrentPosition(whistleMusic.getCurrentPosition());
                    send(GUN_NOT_HOLSTERED, gnh);
                    sentGunNotHolstered=true;
                    sentGunHolstered=false;
                }
            }
            if(sensorCount%5==0) {

            }
        }
        if(!showingHolsterWeaponOverlay) {
            clearViews();
            pistolContainer.addView(getHolsterWeaponOverlayView());
            showingHolsterWeaponOverlay = true;
        }
    }

    private boolean showingHolsterWeaponOverlay = false;

    HolsterWeaponOverlayView holsterWeaponOverlay;

    WinLostGameOverlayView winLostGameOverlay;

    private boolean showingWinLostGameOverlay = false;

    private View getHolsterWeaponOverlayView() {
        if(holsterWeaponOverlay==null) {
            holsterWeaponOverlay = new HolsterWeaponOverlayView(this);
        }
        return holsterWeaponOverlay;
    }

    private View getWinLostGameOverlayView(boolean won) {
        if(winLostGameOverlay==null) {
            winLostGameOverlay = new WinLostGameOverlayView(this);
        }
        if(won) {
            winLostGameOverlay.setWinLossText("You Won!");
        } else {
            winLostGameOverlay.setWinLossText("Loser!");
        }
        return winLostGameOverlay;
    }

    private void onGunHolstered() {
        if(sensorCount%3==0) {
            whistleMusic.pause();
            if(!sentGunNotHolstered) {
                GunHolstered gh = new GunHolstered();
                send(GUN_NOT_HOLSTERED,gh);
                sentGunHolstered = true;
                sentGunNotHolstered = false;
            }
        }
        if(showingHolsterWeaponOverlay) {
            pistolContainer.removeView(getHolsterWeaponOverlayView());
            showingHolsterWeaponOverlay=false;
        }
    }

    private void clearViews() {
        if(showingHolsterWeaponOverlay) {
            pistolContainer.removeView(holsterWeaponOverlay);
            showingHolsterWeaponOverlay=false;
        }
        if(showingWinLostGameOverlay) {
            pistolContainer.removeView(winLostGameOverlay);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void showGameOver(boolean won) {
        if(!showingWinLostGameOverlay) {
            clearViews();
            pistolContainer.addView(getWinLostGameOverlayView(won));
            showingWinLostGameOverlay = true;
        }
    }

    private void registerListeners() {
         sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
    }
}