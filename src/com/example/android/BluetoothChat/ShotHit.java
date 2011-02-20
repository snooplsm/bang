package com.example.android.BluetoothChat;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/5/11
 * Time: 1:32 PM
 */
public class ShotHit implements Serializable {

    @JsonProperty("w")
    private int whatever = (int)Math.random();

    public int getWhatever() {
        return whatever;
    }

    public void setWhatever(int whatever) {
        this.whatever = whatever;
    }
}
