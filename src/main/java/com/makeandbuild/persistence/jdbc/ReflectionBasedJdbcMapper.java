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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("rawtypes")
public class ReflectionBasedJdbcMapper<T> extends BaseDomainMapper<T> implements InvocationHandler {
    @SuppressWarnings("rawtypes")
    private Class domainClass;
    
    private Map<Class, Map<String, SaveWhen>> saveWhensMap;
    private Map<Class, Map<String, String>> columnMapMap;
    private Specialize specialize;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ReflectionBasedJdbcMapper(Class c) {
        super();
        if (c.isAnnotationPresent(Table.class)){
            Table table = (Table) c.getAnnotation(Table.class);
            tablename = table.name();
        }
        for (Field f : c.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                Column column = f.getAnnotation(Column.class);
                columnMap.put(f.getName(), column.name());
                if (f.isAnnotationPresent(Id.class)) {
                    primaryKeyField = f;
                    f.setAccessible(true);
                    primaryKeyName = column.name();
                }
            }
        }
        this.domainClass = c;
        saveWhensMap = new HashMap<Class, Map<String, SaveWhen>>();
        Map<String, SaveWhen> saveWhens = new HashMap<String, SaveWhen>();
        //load base class
        if (c.isAnnotationPresent(Specialize.class)){
            specialize = (Specialize) c.getAnnotation(Specialize.class);
            columnMapMap = new HashMap<Class, Map<String, String>>();
        }
        saveWhensMap.put(c, saveWhens);
        addFields(c, saveWhens);

    }
    private Map<String, SaveWhen> getSaveWhens(Class clazz){
        if (!saveWhensMap.containsKey(clazz)){
            Map<String, SaveWhen> saveWhens = new HashMap<String, SaveWhen>();
            addFields(clazz, saveWhens);
            saveWhensMap.put(clazz, saveWhens);
        }
        return saveWhensMap.get(clazz); 
    }
    private void addFields(Class clazz, Map<String, SaveWhen> saveWhens) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(SaveWhen.class)) {
                SaveWhen saveWhen = f.getAnnotation(SaveWhen.class);
                saveWhens.put(f.getName(), saveWhen);
            }
        }
        if (this.specialize != null && !this.domainClass.equals(clazz)){
            addFields(clazz.getSuperclass(), saveWhens);
        }
    }
    private Map<String, String> getColumnMap(Class specializedClass){
        if (!this.columnMapMap.containsKey(specializedClass)){
            Map<String, String> specializedColumnMap = new HashMap<String, String>();
            specializedColumnMap.putAll(columnMap);
            columnMapMap.put(specializedClass, specializedColumnMap);
            for (Field f : specializedClass.getDeclaredFields()) {
                if (f.isAnnotationPresent(Column.class)) {
                    Column column = f.getAnnotation(Column.class);
                    specializedColumnMap.put(f.getName(), column.name());
                }
            }        
        }
        return columnMapMap.get(specializedClass);
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
    private Field getField(Class clazz, String name) throws NoSuchFieldException{
        try {
            return clazz.getDeclaredField(name);
        } catch(NoSuchFieldException e){
            if (this.domainClass.equals(clazz)){
                throw e;
            }
            return getField(clazz.getSuperclass(), name);
        }
    }
    private String getColumn(String attributeName, T item){
        if (this.specialize == null){
            return getColumn(attributeName);
        } else {
            return this.getColumnMap(item.getClass()).get(attributeName);
        }
    }
    @SuppressWarnings("rawtypes")
    @Override
    public Map<String, Object> insertParameters(T item) {
        Map<String, Object> response = new HashMap<String, Object>();
        if (this.specialize != null){
            response.put(specialize.typeColumn(), item.getClass().getName());
        }
        Map<String, SaveWhen> saveWhens = getSaveWhens(item.getClass());
        for (Map.Entry<String, SaveWhen> entry : saveWhens.entrySet()) {
            if (entry.getValue().insert()){
                String attributeName = entry.getKey();
                try {
                    Field f = getField(item.getClass(), attributeName);
                    f.setAccessible(true);
                    Object value = f.get(item);
                    String columnName = this.getColumn(attributeName, item);
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
        if (this.specialize != null){
            response.put(specialize.typeColumn(), item.getClass().getName());
        }
        Map<String, SaveWhen> saveWhens = getSaveWhens(item.getClass());
        for (Map.Entry<String, SaveWhen> entry : saveWhens.entrySet()) {
            if (entry.getValue().update()){
                String attributeName = entry.getKey();
                try {
                    String columnName = this.getColumn(attributeName, item);
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
        Field f = getField(item.getClass(), attributeName);
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
            T target = null;
            if (this.specialize != null){
                String clazzname = rs.getString(specialize.typeColumn());
                Class specializedClass = Class.forName(clazzname);
                target = (T) specializedClass.newInstance();
                Map<String, String> specializedColumnMap = this.getColumnMap(specializedClass);
                for (Map.Entry<String, String> entry : specializedColumnMap.entrySet()) {
                    String attributeName = entry.getKey();
                    String columnName = entry.getValue();
                    Field f = getField(specializedClass, attributeName);
                    Object value = getResultsetValue(columnName, f.getType(), rs);
                    f.setAccessible(true);
                    f.set(target, value);
                }
            } else {
                target = (T) this.domainClass.newInstance();        
                for (Map.Entry<String, String> entry : this.columnMap.entrySet()) {
                    String attributeName = entry.getKey();
                    String columnName = entry.getValue();
                    Field f = domainClass.getDeclaredField(attributeName);
                    Object value = getResultsetValue(columnName, f.getType(), rs);
                    f.setAccessible(true);
                    f.set(target, value);
                }
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
        } catch (ClassNotFoundException e) {
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
