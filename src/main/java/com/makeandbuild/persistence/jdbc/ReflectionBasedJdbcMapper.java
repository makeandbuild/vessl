package com.makeandbuild.persistence.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReflectionBasedJdbcMapper<T> extends BaseDomainMapper<T> implements InvocationHandler {
    private Map<String, SaveWhen> saveWhens;
    @SuppressWarnings("rawtypes")
    private Class domainClass;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ReflectionBasedJdbcMapper(Class c) {
        super();
        this.buildMap(c);
        this.domainClass = c;
        saveWhens = new HashMap<String, SaveWhen>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isAnnotationPresent(SaveWhen.class)) {
                SaveWhen saveWhen = f.getAnnotation(SaveWhen.class);
                saveWhens.put(f.getName(), saveWhen);
            }
        }

    }
    @SuppressWarnings("rawtypes")
    public static DomainMapper proxy(Class domainClass) {
        Class[] interfaces = new Class[]{DomainMapper.class};
        ReflectionBasedJdbcMapper mapper = new ReflectionBasedJdbcMapper(domainClass);
        return (DomainMapper) Proxy.newProxyInstance(DomainMapper.class.getClassLoader(), interfaces, mapper);
    }
    @Override
    public Object invoke(Object proxy, Method calledMethod, Object[] args) throws Throwable {
        Object result = calledMethod.invoke(this, args);
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<String, Object> insertParameters(T item) {
        Map<String, Object> response = new HashMap<String, Object>();
        for (Map.Entry<String, SaveWhen> entry : saveWhens.entrySet()) {
            if (entry.getValue().insert()){
                String attributeName = entry.getKey();
                try {
                    Field f = domainClass.getDeclaredField(attributeName);
                    f.setAccessible(true);
                    Object value = f.get(item);
                    String columnName = this.getColumn(attributeName);
                    if (value == null){
                        if (!getPrimaryKeyName().equals(columnName))
                            response.put(columnName, value);
                    } else {
                        Class fieldType = f.getType();
                        if (fieldType.isEnum()){
                            Enum v = (Enum)value;
                            response.put(columnName, v.name());
                        } else {
                            response.put(columnName, value);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                } catch (SecurityException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                }
            }
        }
        return response;
    }

    @Override
    public Map<String, Object> updateParameters(T item) {
        Map<String, Object> response = new HashMap<String, Object>();
        for (Map.Entry<String, SaveWhen> entry : saveWhens.entrySet()) {
            if (entry.getValue().update()){
                String attributeName = entry.getKey();
                try {
                    String columnName = this.getColumn(attributeName);
                    Object value = getBeanValue(item, attributeName);
                    response.put(columnName, value);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                } catch (SecurityException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("problem getting insert value for "+attributeName, e);
                }
            }
        }
        return response;
    }

    @SuppressWarnings("rawtypes")
    private Object getBeanValue(T item, String attributeName) throws NoSuchFieldException, IllegalAccessException {
        Field f = domainClass.getDeclaredField(attributeName);
        f.setAccessible(true);
        Object value = f.get(item);
        if (value == null){
            return null;
        } else {
            Class fieldType = f.getType();
            if (fieldType.isEnum()){
                Enum v = (Enum)value;
                return v.name();
            } else {
                return value;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            T target = (T) this.domainClass.newInstance();
            for (Map.Entry<String, String> entry : this.columnMap.entrySet()) {
                String attributeName = entry.getKey();
                String columnName = entry.getValue();
                Field f = domainClass.getDeclaredField(attributeName);
                Object value = getResultsetValue(columnName, f.getType(), rs);
                f.setAccessible(true);
                f.set(target, value);
            }
            return target;
        } catch (InstantiationException e) {
            throw new RuntimeException("problem desarializing from resultset "+this.domainClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("problem desarializing from resultset "+this.domainClass.getName(), e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("problem desarializing from resultset "+this.domainClass.getName(), e);
        } catch (SecurityException e) {
            throw new RuntimeException("problem desarializing from resultset "+this.domainClass.getName(), e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object getResultsetValue(String columnName, Class type, ResultSet rs) throws SQLException {
        if (type.isEnum()){
            String value = rs.getString(columnName);
            return Enum.valueOf(type, value);
        } else if (type.equals(Date.class)){
            return rs.getTimestamp(columnName);
        } else if (type.equals(String.class)){
            return rs.getString(columnName);
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)){
            return this.getLong(rs, columnName);
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)){
            return this.getShort(rs, columnName);
        } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)){
            return this.getInt(rs, columnName);
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)){
            return this.getDouble(rs, columnName);
        }
        throw new RuntimeException("field with type "+type.getName()+" not supported");
    }
}
