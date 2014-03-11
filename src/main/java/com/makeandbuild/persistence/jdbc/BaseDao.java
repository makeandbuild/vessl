package com.makeandbuild.persistence.jdbc;

import java.util.List;

import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.Dao;
import com.makeandbuild.persistence.DaoException;
import com.makeandbuild.persistence.AbstractPagedRequest;


public interface BaseDao<T, ID>  extends Dao<T, ID, List<T>>{
    public PagedResponse<T> find(AbstractPagedRequest request, List<Criteria> criterias, List<SortBy> sortbys) throws DaoException;
    public PagedResponse<T> find(AbstractPagedRequest request, Criteria criteria, List<SortBy> sortbys) throws DaoException;
}