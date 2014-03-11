package com.makeandbuild.persistence;

import java.util.List;


public interface BaseDao<T, ID>  extends Dao<T, ID, List<T>>{
    public PagedResponse<T, List<T>> find(PagedRequest request, List<Criteria> criterias, List<SortBy> sortbys) throws DaoException;
    public PagedResponse<T, List<T>> find(PagedRequest request, Criteria criteria, List<SortBy> sortbys) throws DaoException;
}