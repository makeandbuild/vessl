package com.makeandbuild.fixture;

import java.lang.reflect.Field;

import javax.persistence.Id;

import com.makeandbuild.persistence.BaseDao;
import com.makeandbuild.persistence.DaoException;

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

    @SuppressWarnings("unchecked")
    @Override
    public void delete(Object item) throws DaoException {
        try {
            dao.deleteById(getId(item));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private Object getId(Object item) throws IllegalArgumentException, IllegalAccessException{
        Field f = getIdField(item);
        f.setAccessible(true);
        Object value = f.get(item);
        return value;
    }
    private Field getIdField(Object item){
        for (Field field : getEntityClass().getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                return field;
            }
        }
        throw new RuntimeException("id for class "+getEntityClass() + " not found");
    }
}
