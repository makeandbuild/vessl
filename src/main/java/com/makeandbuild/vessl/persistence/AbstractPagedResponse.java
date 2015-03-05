package com.makeandbuild.vessl.persistence;


public class AbstractPagedResponse<T, C> {
	private C items;
	private int totalPages;
	private long totalItems;
	
	public AbstractPagedResponse() {
		super();
	}
	public AbstractPagedResponse(C items, int totalPages, int totalItems) {
		super();
		this.items = items;
	}
	public C getItems() {
		return items;
	}
	public void setItems(C items) {
		this.items = items;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public long getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}
	
}
