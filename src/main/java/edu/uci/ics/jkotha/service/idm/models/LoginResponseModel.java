package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseModel {
    @JsonProperty(required = true)
    private int resultCode;
    @JsonProperty(required = true)
    private String message;
    private String sessionID;

    @JsonCreator
    public LoginResponseModel(
            int resultCode,
            String message,
            String sessionID) {
        this.resultCode = resultCode;
        this.message = message;
        this.sessionID = sessionID;
    }

    @JsonCreator
    public LoginResponseModel(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
        this.sessionID = null;
    }

    @JsonProperty("resultCode")
    public int getResultCode() {
        return resultCode;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("sessionID")
    public String getSessionID() {
        return sessionID;
    }
}
