package com.makeandbuild.persistence.couch;

public interface Encoder<T> {
    String encode(T... input);
}
