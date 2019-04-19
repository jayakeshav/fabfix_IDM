package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionsRequestModel {
    private String email;
    private String sessionID;

    @JsonCreator
    public SessionsRequestModel(
            @JsonProperty(value = "email",required = true) String email,
            @JsonProperty(value = "sessionID",required = true) String sessionID) {
        this.email = email;
        this.sessionID = sessionID;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public String getSessionID() {
        return sessionID;
    }
}
