package com.makeandbuild.fixture;

import java.io.File;

import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("rawtypes")
public class DumperFactoryImpl implements DumperFactory {
    protected File directory;
    @Override
    public Dumper create(Class entityClass, String subtype, EntityManager manager, Object minKey) {
        if (entityClass.equals(ObjectNode.class)){
            //todo implement
            return null;
        }else {
            return new DaoDumperImpl(entityClass, subtype, manager, directory, minKey);
        }        
    }

    @Override
    public void setDirectory(File directory) {
        this.directory = directory;
    }
}
