package com.makeandbuild.persistence;

import java.util.List;

public interface Dao<T, ID, C> {
    @SuppressWarnings("rawtypes")
    Class getEntityClass();
    @SuppressWarnings("rawtypes")
    Class getIdClass();

    public T save(T item) throws DaoException;
    public boolean exists(ID id) throws DaoException;
    public boolean exists(List<Criteria> criterias) throws DaoException;
    public boolean exists(Criteria... criterias) throws DaoException;
    public T update(T item) throws DaoException;
    public T create(T item) throws DaoException;

    public AbstractPagedResponse<T, C> find(AbstractPagedRequest request, List<Criteria> criterias) throws DaoException;
    public AbstractPagedResponse<T, C> find(AbstractPagedRequest request, Criteria... criteria) throws DaoException;
    public T find(ID id) throws DaoException;

    public void deleteById(ID id) throws DaoException;
    public void delete(List<Criteria> criterias) throws DaoException;
    public void delete(Criteria... criterias) throws DaoException;
    public void deleteAll() throws DaoException;
    void delete(Criteria criteria);
    public ID getId(T object);
    public String getIdName();
}