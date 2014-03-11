package com.makeandbuild.persistence.couch;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public interface CouchDbJackson {

    public ArrayNode listDatabases() throws JsonProcessingException, IOException;
    public boolean createDatabase() throws JsonProcessingException, IOException;
    public boolean deleteDatabase() throws JsonProcessingException, IOException;
}