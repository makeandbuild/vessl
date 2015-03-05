package com.makeandbuild.vessl.fixture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings("rawtypes")
public interface Fixture {
    void purge() throws IOException;
    void purge(Class entityClass, String subtype) throws IOException;
    void load() throws IOException;
    void setEntityLoaders(List<EntityLoader> entityLoaders);
    void setEntityManagers(List<EntityManager> entityManagers);
    void load(EntityLoader loader) throws ClassNotFoundException, IOException;
    void load(String resourceName) throws ClassNotFoundException, IOException;
    void load(Class entityClass, String subtype) throws IOException;
    void dump(Class entityClass, String subtype, File directory) throws IOException;
    void dump(File directory) throws IOException;
    public Object getMiniumKey(Class entityClass, String subtype) throws IOException;
    void dump(Class entityClass, String subtype, OutputStream outputStream) throws IOException;

}
