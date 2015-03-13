package com.makeandbuild.vessl.persistence;


public class AbstractPagedResponse<T, C> {
	private C items;
	
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
}
