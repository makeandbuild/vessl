package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("rawtypes")
public interface EntityLoader {
    Class getEntityClass();
    List<Object> load() throws IOException;
    String getSubtype();
}
