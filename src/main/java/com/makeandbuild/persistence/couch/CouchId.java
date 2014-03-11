package com.makeandbuild.persistence.couch;

public class CouchId {
    private String id;
    private String revision;
    
    public CouchId(String id, String revision) {
        super();
        this.id = id;
        this.revision = revision;
    }
    public CouchId(String id) {
        super();
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getRevision() {
        return revision;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
    
}
