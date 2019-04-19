package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateRequestModel {
    private String email;
    private int plevel;
    private char[] password;

    @JsonCreator
    public CreateRequestModel(
            @JsonProperty(value = "email",required = true) String email,
            @JsonProperty(value = "plevel", required = true) int plevel,
            @JsonProperty(value = "password", required = true) char[] password) {
        this.email = email;
        this.plevel = plevel;
        this.password = password;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public int getPlevel() {
        return plevel;
    }

    @JsonProperty
    public char[] getPassword() {
        return password;
    }
}
