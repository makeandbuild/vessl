package com.makeandbuild.fixture;

import java.io.File;
import java.io.IOException;
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
import com.makeandbuild.persistence.jdbc.PagedResponse;

@SuppressWarnings({"rawtypes", "unused"})
public class DaoDumperImpl implements Dumper {
    private Class entityClass;
    private String subtype;
    private EntityManager manager;
    JsonGenerator jGenerator;
    File file;
    File directory;
    ObjectMapper objectMapper;
    static Log logger = LogFactory.getLog(DaoDumperImpl.class);
    
    public DaoDumperImpl(Class entityClass, String subtype, EntityManager manager, File directory)  {
        this.entityClass = entityClass;
        this.subtype = subtype;
        this.manager = manager;
        this.directory = directory;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void start() throws IOException{
        JsonFactory jfactory = new JsonFactory();
        String filename = entityClass.getName()+".json";
        file = new File(directory, filename);
        jGenerator = jfactory.createJsonGenerator(file, JsonEncoding.UTF8);
        jGenerator.writeStartArray();
        objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
//        SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
//        testModule.addSerializer(BeanValidationException.class, new BeanValidationExceptionSerializer());
//        testModule.addSerializer(ObjectNotFoundException.class, new ObjectNotFoundExceptionSerializer());
//        testModule.addSerializer(RuntimeException.class, new RuntimeExceptionSerializer());
//        addModuleSerializers(testModule);
//        objectMapper.registerModule(testModule);
    }

    @Override
    public void writeObject(Object item) throws JsonProcessingException, IOException {
        objectMapper.writeValue(jGenerator, item);
    }

    @Override
    public void end() throws JsonGenerationException, IOException {
        jGenerator.writeEndArray();
        jGenerator.flush();
        jGenerator.close();        
    }
    @Override
    public File dump() throws IOException {
        start();
        AbstractPagedRequest request = new AbstractPagedRequest();
        while (true){
            PagedResponse response = (PagedResponse) manager.find(request);
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
        return file;
    }
    
}
