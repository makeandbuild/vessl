package com.makeandbuild.vessl.persistence.jdbc;

import java.util.List;

import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.Criteria;
import com.makeandbuild.vessl.persistence.Dao;
import com.makeandbuild.vessl.persistence.DaoException;


public interface BaseDao<T, ID>  extends Dao<T, ID, List<T>>{
    public PagedResponse<T> find(AbstractPagedRequest request, List<Criteria> criterias, List<SortBy> sortbys) throws DaoException;
    public PagedResponse<T> find(AbstractPagedRequest request, Criteria criteria, List<SortBy> sortbys) throws DaoException;
}