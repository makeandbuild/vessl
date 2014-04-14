package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressWarnings("rawtypes")
public class ResourceEntityLoaderImpl implements EntityLoader {
    protected static ObjectMapper mapper;
    protected Class entityClass;
    protected String resourcePath;
    protected String subtype=null;
    protected static Log logger = LogFactory.getLog(ResourceEntityLoaderImpl.class);
    
    public ResourceEntityLoaderImpl(String resourcePath) throws ClassNotFoundException{
        super();
        String classname = resourcePath.replace(".json", "");
        if (classname.lastIndexOf("/") != -1){
            classname = classname.substring(classname.lastIndexOf("/")+1);
            
        }
        String[] split = classname.split("-");
        String className = split[0];
        this.entityClass = Class.forName(className);
        if (split.length>1){
            subtype = split[1];
        }
        this.resourcePath = resourcePath;
    }
    public ResourceEntityLoaderImpl(String resourcePath,  Class entityClass, String subtype) throws ClassNotFoundException{
        super();
        this.entityClass = entityClass;
        this.subtype = subtype;
        this.resourcePath = resourcePath;
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
            String json = IOUtils.toString(this.getClass().getResourceAsStream(resourcePath));
            JavaType type = getInstance().getTypeFactory().constructCollectionType(List.class, entityClass);
            return getInstance().readValue(json, type);
        }catch (IOException e){
            logger.error("problem loading resource "+resourcePath, e);
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
