package com.makeandbuild.persistence;


public class PagedRequest {
	private int page;
	private int pageSize;
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public PagedRequest(int page, int pageSize) {
		super();
		this.page = page;
		this.pageSize = pageSize;
	}
	public PagedRequest() {
		this(0, 10);
	}
}
