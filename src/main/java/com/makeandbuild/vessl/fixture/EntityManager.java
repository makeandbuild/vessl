package com.makeandbuild.vessl.fixture;

import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.AbstractPagedResponse;
import com.makeandbuild.vessl.persistence.Criteria;
import com.makeandbuild.vessl.persistence.DaoException;


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
