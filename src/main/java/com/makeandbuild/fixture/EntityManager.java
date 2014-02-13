package com.makeandbuild.fixture;


public interface EntityManager {
    Class getEntityClass();
    Class getIdClass();
    
    public Object save(Object item);
    public void deleteAll();
}
