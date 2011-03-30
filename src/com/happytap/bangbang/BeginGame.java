package com.happytap.bangbang;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 1/30/11
 * Time: 2:56 PM
 */
public class BeginGame implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("sudi")
    private int secondsUntilDuelIndex;

    @JsonProperty("ad")
    private int announceDuelIndex;

    @JsonProperty("sud")
    private float secondsUntilDuel;

    public int getSecondsUntilDuelIndex() {
        return secondsUntilDuelIndex;
    }

    public void setSecondsUntilDuelIndex(int secondsUntilDuelIndex) {
        this.secondsUntilDuelIndex = secondsUntilDuelIndex;
    }

    public int getAnnounceDuelIndex() {
        return announceDuelIndex;
    }

    public void setAnnounceDuelIndex(int announceDuelIndex) {
        this.announceDuelIndex = announceDuelIndex;
    }

    public float getSecondsUntilDuel() {
        return secondsUntilDuel;
    }

    public void setSecondsUntilDuel(float secondsUntilDuel) {
        this.secondsUntilDuel = secondsUntilDuel;
    }
}
