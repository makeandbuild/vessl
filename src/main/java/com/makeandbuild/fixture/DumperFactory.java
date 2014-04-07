package com.makeandbuild.fixture;

import java.io.File;
import java.io.OutputStream;

@SuppressWarnings("rawtypes")
public interface DumperFactory {
    void setDirectory(File directory);
    Dumper create(Class entityClass, String subtype, EntityManager manager, Object minKey);
}
