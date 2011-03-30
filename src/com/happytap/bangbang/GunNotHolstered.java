package com.happytap.bangbang;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/6/11
 * Time: 2:34 AM
 */
public class GunNotHolstered implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("cp")
    private int currentPosition;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
