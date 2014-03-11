package com.makeandbuild.persistence;


public class PagedResponse<T, C> {
	private C items;
	private int totalPages;
	private long totalItems;
	
	public PagedResponse() {
		super();
	}
	public PagedResponse(C items, int totalPages, int totalItems) {
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
