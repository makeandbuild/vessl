package com.makeandbuild.vessl.persistence.couch;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface CouchDbJackson {

    public ArrayNode listDatabases() throws JsonProcessingException, IOException;
    public boolean createDatabase() throws JsonProcessingException, IOException;
    public boolean deleteDatabase() throws JsonProcessingException, IOException;
}