package com.makeandbuild.vessl.persistence.jdbc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.Criteria;
import com.makeandbuild.vessl.persistence.DaoException;
import com.makeandbuild.vessl.persistence.ObjectNotFoundException;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseDaoImpl<T, ID> extends NamedParameterJdbcDaoSupport implements BaseDao<T, ID> {
    protected Map<String, DomainMapper> mappers = new HashMap<String, DomainMapper>();
    protected Map<String, String> innerJoins = new HashMap<String, String>();
    protected DomainMapper<T> _mapper = null;
    protected String lastSql;
    protected Class entityClass;
    protected Class idClass;
    
    Log logger = LogFactory.getLog(getClass());
    
    protected DomainMapper<T> getDomainMapper() {
        return _mapper;
    }
    public BaseDaoImpl(DomainMapper mapper, Class entityClass, Class idClass) {
        super();
        this.entityClass = entityClass;
        this.idClass = idClass;
        this._mapper = mapper;
    }
    @Override
    public ID getId(T item) {
        Field f = getIdField(item);
        f.setAccessible(true);
        Object value;
        try {
            value = f.get(item);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return (ID)value;
    }
    @Override
    public String getIdName() {
        Field f = getIdField(getEntityClass());
        return f.getName();
    }
    protected Field getIdField(Object item){
        return getIdField(getEntityClass());
    }
    protected Field getIdField(Class clazz){
        for (Field field : clazz.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)){
                return field;
            }
        }
        throw new RuntimeException("id for class "+getEntityClass() + " not found");
    }
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

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public Class getIdClass() {
        return idClass;
    }

    public void setIdClass(Class idClass) {
        this.idClass = idClass;
    }


    protected SimpleJdbcInsert inserter;
    protected SimpleJdbcInsert nonGeneratingInserter;

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
            @SuppressWarnings("deprecation")
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
        @SuppressWarnings("deprecation")
        int count = this.getJdbcTemplate().queryForInt(sql, toArray(parameters));
        return count > 0;
    }

    @Override
    public PagedResponse<T> find(AbstractPagedRequest request, Criteria criteria, List<SortBy> sortbys) {
        List<Criteria> criterias = new ArrayList<Criteria>();
        criterias.add(criteria);
        return find(request, criterias, sortbys);
    }
    @Override
    public PagedResponse<T> find(AbstractPagedRequest request, Criteria... criterias) {
        return find(request, toList(criterias), (List<SortBy>) null);
    }

    @Override
    public PagedResponse<T> find(AbstractPagedRequest request, List<Criteria> criterias) throws DaoException {
        return find(request, criterias, null);
    }

    protected Object[] toArray(List<Object> parameters) {
        Object[] args = new Object[parameters.size()];
        if (!parameters.isEmpty()) {
            parameters.toArray(args);
        }
        return args;
    }

    
    protected void addPaging(AbstractPagedRequest request, PagedResponse<T> response, List<String> sqlList, MapSqlParameterSource parameters, String sql) {
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
    protected void addPaging(AbstractPagedRequest request, PagedResponse<T> response, List<String> sqlList, ArrayList<Object> parameters, String sql) {
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

    @Override
    public void deleteAll() {
        getJdbcTemplate().update("DELETE FROM " + getDomainMapper().getTablename());
    }

    @Override
    public void deleteById(ID id) {
        cascadeDeletes(id);
        String testString = "DELETE FROM " + getDomainMapper().getTablename() + " WHERE " + getDomainMapper().getPrimaryKeyName() + " = ?";
        getJdbcTemplate().update(testString, id);
    }
    protected void cascadeDeletes(ID id){
        Class clazz = this.getClass();
        for (Field field : clazz.getDeclaredFields()){
            if (field.isAnnotationPresent(CascadeDelete.class)){
                CascadeDelete cascadeDelete = field.getAnnotation(CascadeDelete.class);
                field.setAccessible(true);
                BaseDao dao;
                try {
                    dao = (BaseDao)field.get(this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                this.cascadeDeletes(id, cascadeDelete.joinAttributeName(), dao);
            }
        }
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
    protected List<Criteria> toList(Criteria[] params) {
        List<Criteria> criterias = new ArrayList<Criteria>();
        for (Criteria c : params){
            criterias.add(c);
        }
        return criterias;
    }
    @Override
    public boolean exists(Criteria... criterias) throws DaoException {
        return exists(toList(criterias));
    }
    @Override
    public void delete(Criteria... criterias) throws DaoException {
        delete(toList(criterias));
    }
    protected void cascadeDeletes(ID id, String joinAttribute, BaseDao dao) {
        while (true){
            List items = (List)dao.find(new AbstractPagedRequest(), new Criteria(joinAttribute, id)).getItems();
            if (items.size()==0)
                break;
            for (Object item : items){
                Object itemId = dao.getId(item);
                dao.deleteById(itemId);
            }
        }
    }
    @Override
    public PagedResponse<T> find(AbstractPagedRequest request, List<Criteria> criterias, List<SortBy> sortbys) throws DaoException {
        PagedResponse<T> response = new PagedResponse<T>();
        ArrayList<Object> parameters = new ArrayList<Object>();
        List<String> sqlList = new ArrayList<String>();
    
        String innerJoins = innerJoins(sortbys, criterias);
        sqlList.add("SELECT " + getDomainMapper().getTablename() +".* FROM " + getDomainMapper().getTablename() +" " + innerJoins + " ");
    
        String where = createWhere(criterias, parameters);
        sqlList.add(where);
    
        sqlList.add(createOrderBy(sortbys));
    
        addPaging(request, response, sqlList, parameters, "SELECT COUNT(*) FROM " + getDomainMapper().getTablename() +" " + innerJoins + " " + where);
    
        String sql = StringUtils.join(sqlList, " ");
        this.lastSql = sql;
        List<T> items =  this.getJdbcTemplate().query(sql, toArray(parameters), getDomainMapper());
        response.setItems(items);
    
        return response;
    }
    protected String createWhere(List<Criteria> criterias, ArrayList<Object> parameters) {
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
            String attribute = AttributeParser.getAttribute(criteria.getAttribute());
            String domainName = AttributeParser.getDomainName(criteria.getAttribute());
            DomainMapper domainMapper = getDomainMapper(domainName);

            whereList.add(domainMapper.getTablename()+"."+domainMapper.getColumn(attribute));
            whereList.add(criteria.getOperation());
            if (criteria.getValue() != null) {
                whereList.add("?");
                parameters.add(criteria.getValue());
            }
        }

        return StringUtils.join(whereList, " ");
    }

    protected String innerJoins(List<SortBy> sortbys, List<Criteria> criterias) {
        if (sortbys == null){
            return "";
        }
        Set<String> joins = new HashSet<String>();
        for (SortBy sortBy : sortbys) {
            String domainName = AttributeParser.getDomainName(sortBy.getAttribute());
            if (domainName != null){
                joins.add(innerJoins.get(domainName));
            }
        }
        for (Criteria criteria : criterias) {
            String domainName = AttributeParser.getDomainName(criteria.getAttribute());
            if (domainName != null){
                joins.add(innerJoins.get(domainName));
            }
        }
        return StringUtils.join(joins, " ");
    }

    protected DomainMapper getDomainMapper(String domainName) {
        if (domainName == null){
            return this.getDomainMapper();
        }else {
            return mappers.get(domainName);
        }
    }

    protected String createOrderBy(List<SortBy> sortbys) {
        if (sortbys == null)
            return "";
        List<String> orderBy = new ArrayList<String>();
        for (SortBy sortBy : sortbys) {
            String asc = (sortBy.isAscending()) ? " ASC" : " DESC";
            String attribute = AttributeParser.getAttribute(sortBy.getAttribute());
            String domainName = AttributeParser.getDomainName(sortBy.getAttribute());
            DomainMapper domainMapper = getDomainMapper(domainName);
            orderBy.add(domainMapper.getTablename()+"."+domainMapper.getColumn(attribute) + asc);                
        }
        if (orderBy.size() > 0) {
            return "ORDER BY " + StringUtils.join(orderBy, ",");
        } else {
            return "";
        }
    }
    protected void addQueryJoinSupport(String domainName, String joinClause, DomainMapper mapper){
        mappers.put(domainName, mapper);
        innerJoins.put(domainName, joinClause);
    }
}