package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("rawtypes")
public interface Fixture {
    void purge();
    void purge(Class entityClass);
    void load() throws IOException;
    void setEntityLoaders(List<EntityLoader> entityLoaders);
    void setEntityManagers(List<EntityManager> entityManagers);
    void load(String resourceName) throws ClassNotFoundException, IOException;
}
