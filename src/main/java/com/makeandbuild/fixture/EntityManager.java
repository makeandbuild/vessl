package com.makeandbuild.fixture;

import com.makeandbuild.persistence.DaoException;


@SuppressWarnings("rawtypes")
public interface EntityManager {
    Class getEntityClass();
    Class getIdClass();
    
    public Object save(Object item);
    public void deleteAll();
    public void delete(Object item) throws DaoException;
}
