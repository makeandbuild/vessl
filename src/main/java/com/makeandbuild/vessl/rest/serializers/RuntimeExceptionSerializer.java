package com.makeandbuild.vessl.rest.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RuntimeExceptionSerializer extends BaseSerializer<RuntimeException> {
    @Override
    public void serialize(RuntimeException value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        writeObjectField(jgen, "message", value.getMessage());
        writeObjectField(jgen, "localizedMessage", value.getLocalizedMessage());
        writeObjectField(jgen, "class", value.getClass().getName());
        jgen.writeEndObject();
    }

}
