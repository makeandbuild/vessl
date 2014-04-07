package com.makeandbuild.fixture;

import java.io.File;

import org.codehaus.jackson.node.ObjectNode;

@SuppressWarnings("rawtypes")
public class DumperFactoryImpl implements DumperFactory {
    private File directory;
    @Override
    public Dumper create(Class entityClass, String subtype, EntityManager manager) {
        if (!directory.exists())
            directory.mkdir();
        
        if (entityClass.equals(ObjectNode.class)){
            //todo implement
            return null;
        }else {
            return new DaoDumperImpl(entityClass, subtype, manager, directory);
        }        
    }

    @Override
    public void setDirectory(File directory) {
        this.directory = directory;
    }
}
