package com.example.android.BluetoothChat;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 1/30/11
 * Time: 7:30 PM
 */
public class PropertyChanged implements Serializable {

    @JsonProperty("t")
    private long timestamp;

    @JsonProperty("i")
    private int id;

    @JsonProperty("v")
    private Object value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
