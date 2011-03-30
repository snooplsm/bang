package com.happytap.bangbang;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 1/27/11
 * Time: 8:00 PM
 */
public class BangBangMessage {

    public BangBangMessage(byte[] previewPacket, int packetSize, byte[] packet) {
        this.previewPacket = previewPacket;
        this.packetSize = packetSize;
        this.packet = packet;
    }

    private byte[] previewPacket;

    private int packetSize;

    private byte[] packet;

    public byte[] getPreviewPacket() {
        return previewPacket;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public byte[] getPacket() {
        return packet;
    }
}
