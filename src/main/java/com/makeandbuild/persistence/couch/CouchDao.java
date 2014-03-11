package com.makeandbuild.persistence.couch;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.makeandbuild.persistence.Dao;

public interface CouchDao extends Dao<ObjectNode, CouchId, ArrayNode>{

}
