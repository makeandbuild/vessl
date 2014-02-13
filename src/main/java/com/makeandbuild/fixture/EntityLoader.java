package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

public interface EntityLoader {
    Class getEntityClass();
    List<Object> load() throws IOException;
}
