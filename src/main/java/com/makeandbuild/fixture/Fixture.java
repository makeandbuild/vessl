package com.makeandbuild.fixture;

import java.io.IOException;
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
}
