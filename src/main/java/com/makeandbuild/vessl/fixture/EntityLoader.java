package com.makeandbuild.vessl.fixture;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("rawtypes")
public interface EntityLoader {
    Class getEntityClass();
    List<Object> load() throws IOException;
    List<Object> loadReverse() throws IOException;
    String getSubtype();
}
