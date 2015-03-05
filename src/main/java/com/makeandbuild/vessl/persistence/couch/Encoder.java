package com.makeandbuild.vessl.persistence.couch;

public interface Encoder<T> {
    String encode(T... input);
}
