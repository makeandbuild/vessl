package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.JavaType;

public class ResourceEntityLoaderImpl implements EntityLoader {
    private static ObjectMapper mapper;
    @SuppressWarnings("rawtypes")
    private Class entityClass;
    private String resourcePath;
    private static Log logger = LogFactory.getLog(ResourceEntityLoaderImpl.class);
    
    public ResourceEntityLoaderImpl(String resourcePath) throws ClassNotFoundException{
        super();
        String classname = resourcePath.replace(".json", "");
        if (classname.lastIndexOf("/") != -1){
            classname = classname.substring(classname.lastIndexOf("/")+1);
            
        }
        this.entityClass = Class.forName(classname);
        this.resourcePath = resourcePath;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getEntityClass() {
        return entityClass;
    }
    @SuppressWarnings("deprecation")
    private static ObjectMapper getInstance() {
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
            String json = IOUtils.toString(this.getClass().getResourceAsStream(resourcePath));
            ArrayNode node = (ArrayNode) getInstance().readTree(json);
            JavaType type = mapper.getTypeFactory().
                    constructCollectionType(List.class, entityClass);
            return getInstance().readValue(node, type);
        }catch (IOException e){
            logger.error("problem loading resource "+resourcePath, e);
            throw e;
        }

    }
}
