package com.makeandbuild.fixture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.JavaType;

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
    @SuppressWarnings("deprecation")
    protected static ObjectMapper getInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
            mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        }
        return mapper;
    }
    @Override
    public List<Object> load() throws IOException {
        try {
            ArrayNode node = (ArrayNode) getInstance().readTree(inputStream);
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, entityClass);
            return getInstance().readValue(node, type);
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
