package com.example.android.BluetoothChat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/6/11
 * Time: 10:46 PM
 */
public class GunActivityDebug extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pistol);

        RelativeLayout l = (RelativeLayout) findViewById(R.id.pistol_container);

        //HolsterWeaponOverlayView v = new HolsterWeaponOverlayView(this);
//        WinLostGameOverlayView v = new WinLostGameOverlayView(this);
//        v.setWinLossText("You Won!");
//        l.addView(v);
    }
}
