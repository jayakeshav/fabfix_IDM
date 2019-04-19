package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResponseModel {
    private int resultCode;
    private String message;
    private UserModel[] user;

    @JsonCreator
    public QueryResponseModel(
            @JsonProperty(value ="resultCode",required = true) int resultCode,
            @JsonProperty(value = "message",required = true) String message,
            @JsonProperty(value = "user",required = true) UserModel[] user) {
        this.resultCode = resultCode;
        this.message = message;
        this.user = user;
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
    public UserModel[] getUser() {
        return user;
    }
}
