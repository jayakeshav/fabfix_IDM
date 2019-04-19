package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefaultResponseModel {
    private int resultCode;
    private String message;

    @JsonCreator
    public DefaultResponseModel(
            @JsonProperty(value = "resultCode",required = true) int resultCode,
            @JsonProperty(value = "message",required = true) String message) {
        this.resultCode = resultCode;
        this.message = message;
    }

    @JsonProperty
    public int getResultCode() {
        return resultCode;
    }
    @JsonProperty
    public String getMessage() {
        return message;
    }
}
