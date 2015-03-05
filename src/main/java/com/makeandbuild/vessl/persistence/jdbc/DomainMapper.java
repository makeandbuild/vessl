package com.makeandbuild.vessl.persistence.jdbc;

import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public interface DomainMapper<T> extends RowMapper<T>{

	public String getColumn(String attributeName);

	public Map<String, Object> insertParameters(T item);

	public Map<String, Object> updateParameters(T item);

	public String getTablename();

	public String getPrimaryKeyName();
	public Object getPrimaryKeyValue(T item);

	public void setPrimaryKey(Object newId, T item);

}