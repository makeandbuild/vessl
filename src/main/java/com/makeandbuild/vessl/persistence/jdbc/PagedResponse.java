package com.makeandbuild.vessl.persistence.jdbc;

import java.util.List;

import com.makeandbuild.vessl.persistence.AbstractPagedResponse;

public class PagedResponse<T> extends AbstractPagedResponse<T, List<T>> {
	private int totalPages;
	private long totalItems;
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
