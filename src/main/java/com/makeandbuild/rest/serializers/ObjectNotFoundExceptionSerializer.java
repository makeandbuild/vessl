package com.makeandbuild.rest.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.makeandbuild.persistence.ObjectNotFoundException;

public class ObjectNotFoundExceptionSerializer extends BaseSerializer<ObjectNotFoundException> {
    @Override
    public void serialize(ObjectNotFoundException value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        writeObjectField(jgen, "message", value.getMessage());
        jgen.writeEndObject();
    }

}
