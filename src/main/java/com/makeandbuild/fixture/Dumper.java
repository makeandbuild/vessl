package com.makeandbuild.fixture;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonProcessingException;


public interface Dumper {
    public void start() throws IOException;
    public void writeObject(Object modelObject) throws JsonProcessingException, IOException;
    public void end() throws JsonGenerationException, IOException;
    File dump() throws IOException;    
}
