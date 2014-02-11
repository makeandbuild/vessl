package com.makeandbuild.persistence;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.makeandbuild.persistence.jdbc.SaveWhen;

@Table(name = "event")
public class Event {
    @Id
    @Column(name = "event_id")
    @SaveWhen(insert = true, update = false)
    private String id;

    @Column(name = "type")
    @SaveWhen(insert = true, update = true)
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
