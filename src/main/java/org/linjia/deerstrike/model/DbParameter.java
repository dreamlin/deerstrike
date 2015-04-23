package org.linjia.deerstrike.model;

public class DbParameter {
	private String	name;	// 字段名
	private Object	value;	// 值

	public DbParameter(String name) {
		this.name = name;
	}

	public DbParameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}
}
