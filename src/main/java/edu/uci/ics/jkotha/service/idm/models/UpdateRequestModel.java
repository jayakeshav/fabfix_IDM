package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateRequestModel {
    private int id;
    private String email;
    private String plevel;

    @JsonCreator
    public UpdateRequestModel(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "email") String email,
            @JsonProperty(value = "plevel") String plevel) {
        this.id = id;
        this.email = email;
        this.plevel = plevel;
    }

    @JsonProperty
    public int getId() {
        return id;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public String getPlevel() {
        return plevel;
    }
}
