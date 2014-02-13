package com.makeandbuild.fixture;

import com.makeandbuild.persistence.BaseDao;

@SuppressWarnings("rawtypes")
public class DaoEntityManagerImpl implements EntityManager {
    private BaseDao dao;

    public DaoEntityManagerImpl(BaseDao dao) {
        super();
        this.dao = dao;
    }

    public BaseDao getDao() {
        return dao;
    }

    public void setDao(BaseDao dao) {
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
    
}
