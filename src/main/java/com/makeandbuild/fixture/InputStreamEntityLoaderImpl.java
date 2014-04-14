package com.makeandbuild.fixture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressWarnings("rawtypes")
public class InputStreamEntityLoaderImpl implements EntityLoader {
    protected static ObjectMapper mapper;
    protected Class entityClass;
    protected String subtype=null;
    protected static Log logger = LogFactory.getLog(InputStreamEntityLoaderImpl.class);
    protected InputStream inputStream;
    
    public InputStreamEntityLoaderImpl(InputStream inputStream,  Class entityClass, String subtype) throws ClassNotFoundException{
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
        try {
//            ArrayNode node = (ArrayNode) getInstance().readTree(inputStream);
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, entityClass);
//            return getInstance().convertValue(node, type);
            return getInstance().readValue(inputStream, type);
        }catch (IOException e){
            logger.error("problem loading inputsream ", e);
            throw e;
        }

    }

    @Override
    public String getSubtype() {
        return subtype;
    }

    @Override
    public List<Object> loadReverse() throws IOException {
        List<Object> items = this.load();
        Collections.reverse(items);
        return items;
    }
}
