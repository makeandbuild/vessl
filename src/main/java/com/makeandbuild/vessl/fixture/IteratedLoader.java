package com.makeandbuild.vessl.fixture;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;

public interface IteratedLoader {
    Object read() throws JsonParseException, IOException;
}
