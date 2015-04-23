package org.linjia.deerstrike.model;

public class AopParameter {

	private String	name;	// 字段名
	private Class	clazz;	// 类型
	private Object	value;	// 值

	public AopParameter(String name, Class clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class getClazz() {
		return this.clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String toString() {
		return "name:" + this.name + " value:" + this.value;
	}
}
