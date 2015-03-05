package com.makeandbuild.vessl.rest.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;

public abstract class BaseSerializer<T> extends JsonSerializer<T> {
    protected void writeObjectField(JsonGenerator jgen, String field, Object value) throws JsonProcessingException, IOException{
        if (value != null)
            jgen.writeObjectField(field, value);
    }
}
