package com.makeandbuild.fixture;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressWarnings("rawtypes")
public class IteratedInputStreamEntityLoaderImpl implements EntityLoader, IteratedLoader {
    protected static ObjectMapper mapper;
    protected Class entityClass;
    protected String subtype=null;
    protected static Log logger = LogFactory.getLog(IteratedInputStreamEntityLoaderImpl.class);
    protected InputStream inputStream;
    protected JsonParser jp;
    protected int countRead = 0;
    protected boolean initialized = false;
    
    public IteratedInputStreamEntityLoaderImpl(InputStream inputStream,  Class entityClass, String subtype) throws ClassNotFoundException{
        super();
        this.entityClass = entityClass;
        this.subtype = subtype;
        this.inputStream = inputStream;
    }

    @Override
    public Class getEntityClass() {
        return entityClass;
    }
    protected static ObjectMapper getInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }
        return mapper;
    }
    @Override
    public List<Object> load() throws IOException {
        throw new RuntimeException("full loading not supported");
    }

    @Override
    public String getSubtype() {
        return subtype;
    }

    @Override
    public List<Object> loadReverse() throws IOException {
        throw new RuntimeException("load reverse not supported");
    }
    @SuppressWarnings("deprecation")
    protected void setup() throws JsonParseException, IOException {
        if (!initialized){
            initialized = true;
            JsonFactory jsonFactory = getInstance().getJsonFactory();
            jp = jsonFactory.createJsonParser(this.inputStream);
            countRead = 0;
        }
    }
    @SuppressWarnings({ "incomplete-switch", "unchecked" })
    @Override
    public Object read() throws JsonParseException, IOException {
        setup();
        JsonToken token;
        while ((token = jp.nextToken()) != null) {
            switch (token) {
                case START_OBJECT:
                    countRead++;
                    Object value = getInstance().readValue(jp, entityClass);
                    System.out.println("Read object" + countRead +": " + value.toString());
                    return value;
            }
        }
        return null;
    }
}
