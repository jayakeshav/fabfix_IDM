package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionResponseModel {
    private int resultCode;
    private String message;
    private String sessionID;

    @JsonCreator
    public SessionResponseModel(
            @JsonProperty(value = "resultCode",required = true) int resultCode,
            @JsonProperty(value = "message",required = true) String message,
            @JsonProperty(value = "sessionID",required = true) String sessionID) {
        this.resultCode = resultCode;
        this.message = message;
        this.sessionID = sessionID;
    }

    @JsonProperty
    public int getResultCode() {
        return resultCode;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public String getSessionID() {
        return sessionID;
    }
}