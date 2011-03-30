package com.happytap.bangbang;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 1/27/11
 * Time: 12:34 AM
 */
public class ShotFired implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private float x;
    private float y;
    private float z;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

}
