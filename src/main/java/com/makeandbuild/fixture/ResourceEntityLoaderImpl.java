package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.JavaType;

public class ResourceEntityLoaderImpl implements EntityLoader {
    private static ObjectMapper mapper;
    @SuppressWarnings("rawtypes")
    private Class entityClass;
    private String resourcePath;
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
    private static ObjectMapper getInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
        }
        return mapper;
    }
    @Override
    public List<Object> load() throws IOException {
        String json = IOUtils.toString(this.getClass().getResourceAsStream(resourcePath));
        ArrayNode node = (ArrayNode) getInstance().readTree(json);
        JavaType type = mapper.getTypeFactory().
                constructCollectionType(List.class, entityClass);
        return getInstance().readValue(node, type);

    }
}
