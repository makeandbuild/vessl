package com.makeandbuild.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.makeandbuild.persistence.BaseDao;
import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.DaoException;
import com.makeandbuild.persistence.ObjectNotFoundException;
import com.makeandbuild.persistence.PagedRequest;
import com.makeandbuild.persistence.PagedResponse;
import com.makeandbuild.persistence.SortBy;

public abstract class BaseDaoImpl<T, ID> extends NamedParameterJdbcDaoSupport implements BaseDao<T, ID> {
    protected DomainMapper<T> _mapper = null;
    @SuppressWarnings("unused")
    private String lastSql;
    @SuppressWarnings("rawtypes")
    private Class entityClass;
    @SuppressWarnings("rawtypes")
    private Class idClass;
    
    Log logger = LogFactory.getLog(getClass());
    
    protected DomainMapper<T> getDomainMapper() {
        return _mapper;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public BaseDaoImpl(DomainMapper mapper, Class entityClass, Class idClass) {
        super();
        this.entityClass = entityClass;
        this.idClass = idClass;
        this._mapper = mapper;
    }

    @SuppressWarnings("rawtypes")
    public BaseDaoImpl(Class<? extends DomainMapper<T>> c, Class entityClass, Class idClass) {
        super();
        this.entityClass = entityClass;
        this.idClass = idClass;
        try {
            _mapper = (DomainMapper<T>) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
        return entityClass;
    }

    @SuppressWarnings("rawtypes")
    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    @SuppressWarnings("rawtypes")
    public Class getIdClass() {
        return idClass;
    }

    @SuppressWarnings("rawtypes")
    public void setIdClass(Class idClass) {
        this.idClass = idClass;
    }


    private SimpleJdbcInsert inserter;
    private SimpleJdbcInsert nonGeneratingInserter;

    protected SimpleJdbcInsert getInserter() {
        if (inserter == null) {
            this.inserter = new SimpleJdbcInsert(this.getDataSource()).withTableName(getDomainMapper().getTablename())
                    .usingGeneratedKeyColumns(getDomainMapper().getPrimaryKeyName());
        }
        return inserter;
    }
    protected SimpleJdbcInsert getNonGeneratingInserter() {
        if (nonGeneratingInserter == null) {
            this.nonGeneratingInserter = new SimpleJdbcInsert(this.getDataSource()).withTableName(getDomainMapper().getTablename());
        }
        return nonGeneratingInserter;
    }

    @Override
    public T update(T item) {
        List<String> sqlList = new ArrayList<String>();
        sqlList.add("UPDATE " + getDomainMapper().getTablename() + " SET");

        List<Object> values = new ArrayList<Object>();
        Map<String, Object> params = getDomainMapper().updateParameters(item);
        List<String> sets = new ArrayList<String>();

        for (Entry<String, Object> entry : params.entrySet()) {
            sets.add(entry.getKey() + " = ?");
            values.add(entry.getValue());
        }
        sqlList.add(StringUtils.join(sets, ","));
        sqlList.add("WHERE " + getDomainMapper().getPrimaryKeyName() + "=?");
        values.add(getDomainMapper().getPrimaryKeyValue(item));
        String sql = StringUtils.join(sqlList, " ");
        Object[] args = toArray(values);

        this.getJdbcTemplate().update(sql, args);
        return item;
    }

    @Override
    public T create(T item) {
        Object pk = getDomainMapper().getPrimaryKeyValue(item);
        Map<String, Object> params = getDomainMapper().insertParameters(item);
        if (pk == null) {
            Object newId = getInserter().executeAndReturnKey(params);
            getDomainMapper().setPrimaryKey(newId, item);
        } else {
            getNonGeneratingInserter().execute(params);
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T save(T item) {
        try {
            ID id = (ID) getDomainMapper().getPrimaryKeyValue(item);
            if (id == null){
                return create(item);
            } else {
                if (this.exists(id)){
                    return update(item);
                } else {
                    return create(item);
                }
            }
        }catch (RuntimeException e) {
            logger.error("problem saving "+item, e);
            throw e;
        }
    }

    @Override
    public T find(ID id) throws DaoException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM " + getDomainMapper().getTablename() + " WHERE " + getDomainMapper().getPrimaryKeyName() + " = ?",
                    new Object[] { id }, getDomainMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("could not find " + getDomainMapper().getTablename() + " with id " + id, e);
        }
    }

    @Override
    public boolean exists(ID id) throws DaoException {
        try {
            int count = getJdbcTemplate().queryForInt(
                    "SELECT count(*) FROM " + getDomainMapper().getTablename() + " WHERE " + getDomainMapper().getPrimaryKeyName() + " = ?",
                    new Object[] { id });
            return count > 0;
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("could not find " + getDomainMapper().getTablename() + " with id " + id, e);
        }
    }
    @Override
    public boolean exists(List<Criteria> criterias) {
        ArrayList<Object> parameters = new ArrayList<Object>();
        List<String> sqlList = new ArrayList<String>();

        sqlList.add("SELECT COUNT(*) FROM " + getDomainMapper().getTablename());

        String where = createWhere(criterias, parameters);
        sqlList.add(where);

        String sql = StringUtils.join(sqlList, " ");
        int count = this.getJdbcTemplate().queryForInt(sql, toArray(parameters));
        return count > 0;
    }

    @Override
    public PagedResponse<T> find(PagedRequest request, Criteria criteria, List<SortBy> sortbys) {
        List<Criteria> criterias = new ArrayList<Criteria>();
        criterias.add(criteria);
        return find(request, criterias, sortbys);
    }

    @Override
    public PagedResponse<T> find(PagedRequest request, List<Criteria> criterias, List<SortBy> sortbys) {
        PagedResponse<T> response = new PagedResponse<T>();
        ArrayList<Object> parameters = new ArrayList<Object>();
        List<String> sqlList = new ArrayList<String>();

        sqlList.add("SELECT * FROM " + getDomainMapper().getTablename());

        String where = createWhere(criterias, parameters);
        sqlList.add(where);

        sqlList.add(createOrderBy(sortbys));

        addPaging(request, response, sqlList, parameters, "SELECT COUNT(*) FROM " + getDomainMapper().getTablename() + " " + where);

        String sql = StringUtils.join(sqlList, " ");
        this.lastSql = sql;
        List<T> items = this.getJdbcTemplate().query(sql, toArray(parameters), getDomainMapper());
        response.setItems(items);

        return response;
    }

    protected Object[] toArray(List<Object> parameters) {
        Object[] args = new Object[parameters.size()];
        if (!parameters.isEmpty()) {
            parameters.toArray(args);
        }
        return args;
    }

    
    protected void addPaging(PagedRequest request, PagedResponse<T> response, List<String> sqlList, MapSqlParameterSource parameters, String sql) {
        MapSqlParameterSource countArgs = new MapSqlParameterSource();
        countArgs.addValues(parameters.getValues());
        
        if (request.getPage() >= 0 && request.getPageSize() >= 0) {
            Long rowCount = getNamedParameterJdbcTemplate().queryForObject(sql, countArgs, Long.class);
            double totalPages = Math.ceil(rowCount.doubleValue() / request.getPageSize());
            response.setTotalPages((int) totalPages);
            response.setTotalItems(rowCount);
            sqlList.add("LIMIT :limit OFFSET :offset");
            parameters.addValue("limit", request.getPageSize());
            parameters.addValue("offset", request.getPage() * request.getPageSize());
        }
        
    }
    protected void addPaging(PagedRequest request, PagedResponse<T> response, List<String> sqlList, ArrayList<Object> parameters, String sql) {
        Object[] countArgs = toArray(parameters);
        if (request.getPage() >= 0 && request.getPageSize() >= 0) {
            Long rowCount = this.getJdbcTemplate().queryForObject(sql, countArgs, Long.class);
            double totalPages = Math.ceil(rowCount.doubleValue() / request.getPageSize());
            response.setTotalPages((int) totalPages);
            response.setTotalItems(rowCount);
            sqlList.add("LIMIT ? OFFSET ?");
            parameters.add(request.getPageSize());
            parameters.add(request.getPage() * request.getPageSize());
        }
    }

    private String createWhere(List<Criteria> criterias, ArrayList<Object> parameters) {
        List<String> whereList = new ArrayList<String>();
        if (criterias == null || criterias.isEmpty()){
            return "";
        }
        whereList.add("WHERE");
        for (int i = 0; i < criterias.size(); i++) {
            Criteria criteria = criterias.get(i);
            if (i != 0) {
                whereList.add(criteria.getJoinLogic().name());
            }
            whereList.add(getDomainMapper().getColumn(criteria.getAttribute()));
            whereList.add(criteria.getOperation());
            if (criteria.getValue() != null) {
                whereList.add("?");
                parameters.add(criteria.getValue());
            }
        }

        return StringUtils.join(whereList, " ");
    }

    protected String createOrderBy(List<SortBy> sortbys) {
        if (sortbys == null)
            return "";
        List<String> orderBy = new ArrayList<String>();
        for (SortBy sortBy : sortbys) {
            String asc = (sortBy.isAscending()) ? " ASC" : " DESC";
            orderBy.add(getDomainMapper().getColumn(sortBy.getAttribute()) + asc);
        }
        if (orderBy.size() > 0) {
            return "ORDER BY " + StringUtils.join(orderBy, ",");
        } else {
            return "";
        }
    }

    @Override
    public void deleteAll() {
        getJdbcTemplate().update("DELETE FROM " + getDomainMapper().getTablename());
    }

    @Override
    public void deleteById(ID id) {
        String testString = "DELETE FROM " + getDomainMapper().getTablename() + " WHERE " + getDomainMapper().getPrimaryKeyName() + " = ?";
        getJdbcTemplate().update(testString, id);
    }
    @Override
    public void delete(Criteria criteria) {
        List<Criteria> criterias = new ArrayList<Criteria>();
        criterias.add(criteria);
        delete(criterias);
    }    
    @Override
    public void delete(List<Criteria> criterias) {
        ArrayList<Object> parameters = new ArrayList<Object>();
        List<String> sqlList = new ArrayList<String>();

        sqlList.add("DELETE FROM " + getDomainMapper().getTablename());

        String where = createWhere(criterias, parameters);
        sqlList.add(where);

        String sql = StringUtils.join(sqlList, " ");
        this.getJdbcTemplate().update(sql, toArray(parameters));
    }
}