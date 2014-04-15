package com.makeandbuild.rest.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.makeandbuild.validation.exception.BeanValidationException;

public class BeanValidationExceptionSerializer extends BaseSerializer<BeanValidationException> {
    @Override
    public void serialize(BeanValidationException value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        writeObjectField(jgen, "errors", value.getErrors());
        writeObjectField(jgen, "localizedMessage", value.getLocalizedMessage());
        writeObjectField(jgen, "message", value.getMessage());
        writeObjectField(jgen, "validatedBean", value.getValidatedBean());
        jgen.writeEndObject();
    }

}
