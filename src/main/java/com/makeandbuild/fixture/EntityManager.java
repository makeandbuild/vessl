package com.makeandbuild.fixture;


@SuppressWarnings("rawtypes")
public interface EntityManager {
    Class getEntityClass();
    Class getIdClass();
    
    public Object save(Object item);
    public void deleteAll();
}
