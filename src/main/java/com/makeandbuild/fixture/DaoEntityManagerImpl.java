package com.makeandbuild.fixture;

import com.makeandbuild.persistence.Dao;
import com.makeandbuild.persistence.DaoException;

@SuppressWarnings("rawtypes")
public class DaoEntityManagerImpl implements EntityManager {
    private Dao dao;
    private String subtype;

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
        return dao.save(item);
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();       
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete(Object item) throws DaoException {
        try {
            dao.deleteById(dao.getId(item));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
    public String getSubtype() {
        return subtype;
    }
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    
}
