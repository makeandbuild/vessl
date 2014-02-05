package com.makeandbuild.persistence;

public class Criteria {
    public enum JoinLogic {AND, OR};
    
	private String attribute;
	private String operation;
	private Object value;
	private JoinLogic joinLogic;
	public JoinLogic getJoinLogic() {
        return joinLogic;
    }
    public void setJoinLogic(JoinLogic joinLogic) {
        this.joinLogic = joinLogic;
    }
    public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
    public Criteria(JoinLogic joinLogic, String attribute, String operation, Object value) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
        this.joinLogic = joinLogic;
    }
	public Criteria(String attribute, String operation, Object value) {
	    this(JoinLogic.AND, attribute, operation, value);
	}
	public Criteria(String attribute, Object value) {
		this(attribute, "=", value);
	}
	
}
