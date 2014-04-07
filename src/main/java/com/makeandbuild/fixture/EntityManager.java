package com.makeandbuild.fixture;

import com.makeandbuild.persistence.AbstractPagedRequest;
import com.makeandbuild.persistence.AbstractPagedResponse;
import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.DaoException;


@SuppressWarnings("rawtypes")
public interface EntityManager {
    Class getEntityClass();
    Class getIdClass();
    String getSubtype();
    
    public Object save(Object item);
    public void deleteAll();
    public void delete(Object item) throws DaoException;
    public AbstractPagedResponse find(AbstractPagedRequest request, Criteria... criteria);
    Object getId(Object model);
    String getIdName();
}
