package edu.uci.ics.jkotha.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserModel {
    private int id;
    private String email;
    private String plevel;

    @JsonCreator
    public UserModel(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "plevel", required = true) String plevel
    ) {
        this.id = id;
        this.email = email;
        this.plevel = plevel;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public String getPlevel() {
        return plevel;
    }

    @JsonProperty
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id:" + id + " email:" + email + " plevel" + plevel;
    }
}
