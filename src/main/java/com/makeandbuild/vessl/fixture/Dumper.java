package com.makeandbuild.vessl.fixture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


public interface Dumper {
    File dump() throws IOException;    
    void dump(OutputStream outputStream) throws IOException;    
}
