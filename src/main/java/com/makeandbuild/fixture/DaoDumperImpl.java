package com.makeandbuild.fixture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.makeandbuild.persistence.AbstractPagedRequest;
import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.jdbc.PagedResponse;

@SuppressWarnings({"rawtypes", "unused"})
public class DaoDumperImpl implements Dumper {
    protected Class entityClass;
    protected String subtype;
    protected EntityManager manager;
    protected JsonGenerator jGenerator;
    protected File file;
    protected File directory;
    protected ObjectMapper objectMapper;
    protected Object minKey;
    static Log logger = LogFactory.getLog(DaoDumperImpl.class);
    
    public DaoDumperImpl(Class entityClass, String subtype, EntityManager manager, File directory, Object minKey)  {
        this.entityClass = entityClass;
        this.subtype = subtype;
        this.manager = manager;
        this.directory = directory;
        this.minKey = minKey;
    }

    @SuppressWarnings("deprecation")
    protected void start() throws IOException{
        jGenerator.writeStartArray();
        objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    protected void writeObject(Object item) throws JsonProcessingException, IOException {
        objectMapper.writeValue(jGenerator, item);
    }

    protected void end() throws JsonGenerationException, IOException {
        jGenerator.writeEndArray();
        jGenerator.flush();
        jGenerator.close();        
    }
    @Override
    public File dump() throws IOException {
        JsonFactory jfactory = new JsonFactory();
        String filename = entityClass.getName()+".json";
        file = new File(directory, filename);
        jGenerator = jfactory.createJsonGenerator(file, JsonEncoding.UTF8);
        
        dodump();
        return file;
    }

    private void dodump() throws IOException, JsonProcessingException, JsonGenerationException {
        start();
        AbstractPagedRequest request = new AbstractPagedRequest();
        Criteria criteria = (minKey==null) ? null : new Criteria(manager.getIdName(), ">=", minKey);
        
        while (true){
            
            PagedResponse response = (criteria==null) ? (PagedResponse) manager.find(request) : (PagedResponse) manager.find(request, criteria);
            List items = (List) response.getItems();
            if (items.size() == 0)
                break;
            for (Object item : items){
                Object id = manager.getId(item);
                System.out.println("writing "+manager.getEntityClass()+ " "+id);
                writeObject(item);
            }
            request.setPage(request.getPage()+1);
        }
        end();
    }

    @Override
    public void dump(OutputStream outputStream) throws IOException {
        JsonFactory jfactory = new JsonFactory();
        String filename = entityClass.getName()+".json";
        jGenerator = jfactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);
        dodump();
    }
    
}
