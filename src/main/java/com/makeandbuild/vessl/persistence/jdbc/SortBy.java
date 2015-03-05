package com.makeandbuild.vessl.persistence.jdbc;

public class SortBy {
	private String attribute;
	private boolean ascending;
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public boolean isAscending() {
		return ascending;
	}
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	public SortBy(String attribute, boolean ascending) {
		super();
		this.attribute = attribute;
		this.ascending = ascending;
	}
	public SortBy(String attribute) {
		this(attribute, true);
	}
	
}
