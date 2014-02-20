package com.makeandbuild.persistence;

import javax.persistence.Column;

import com.makeandbuild.persistence.jdbc.SaveWhen;


public class AdminUser extends User {
    @Column(name = "api_key")
    @SaveWhen(insert = true, update = true)
    private String apiKey;

    public String getApiKey(){
        return apiKey;
    }
    public void setApiKey(String apiKey){
        this.apiKey = apiKey;
    }
}
