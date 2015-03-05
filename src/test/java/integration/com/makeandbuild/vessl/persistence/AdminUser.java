package com.makeandbuild.vessl.persistence;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import com.makeandbuild.vessl.persistence.jdbc.SaveWhen;


public class AdminUser extends User {
    @Column(name = "api_key")
    @SaveWhen(insert = true, update = true)
    @NotNull(message = "You must provide a APIKey for an admin user")
    private String apiKey;

    public String getApiKey(){
        return apiKey;
    }
    public void setApiKey(String apiKey){
        this.apiKey = apiKey;
    }
}
