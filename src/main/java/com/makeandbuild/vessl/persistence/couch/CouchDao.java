package com.makeandbuild.vessl.persistence.couch;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.makeandbuild.vessl.persistence.Dao;

public interface CouchDao extends Dao<ObjectNode, CouchId, ArrayNode>{

}
