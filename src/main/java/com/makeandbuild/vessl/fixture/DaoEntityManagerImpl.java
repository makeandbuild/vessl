package com.makeandbuild.vessl.fixture;

import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.AbstractPagedResponse;
import com.makeandbuild.vessl.persistence.Criteria;
import com.makeandbuild.vessl.persistence.Dao;
import com.makeandbuild.vessl.persistence.DaoException;

@SuppressWarnings("rawtypes")
public class DaoEntityManagerImpl implements EntityManager {
    protected Dao dao;
    protected String subtype;

    public DaoEntityManagerImpl(Dao dao) {
        super();
        this.dao = dao;
    }
    public DaoEntityManagerImpl(Dao dao, String subtype) {
        super();
        this.dao = dao;
        this.subtype = subtype;
    }

    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    @Override
    public Class getEntityClass() {
        return dao.getEntityClass();
    }

    @Override
    public Class getIdClass() {
        return dao.getIdClass();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object save(Object item) {
        return getDao().save(item);
    }

    @Override
    public void deleteAll() {
        getDao().deleteAll();       
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete(Object item) throws DaoException {
        try {
            getDao().deleteById(dao.getId(item));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public Object getId(Object model){
        return dao.getId(model);
    }
    public String getSubtype() {
        return subtype;
    }
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    @Override
    public AbstractPagedResponse find(AbstractPagedRequest request, Criteria... criterias) {
        return getDao().find(request, criterias);
    }
    @Override
    public String getIdName() {
        return dao.getIdName();
    }    
}
