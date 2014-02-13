package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

public interface Fixture {
    void purge();
    void load() throws IOException;
    void setEntityLoaders(List<EntityLoader> entityLoaders);
    void setEntityManagers(List<EntityManager> entityManagers);
}
