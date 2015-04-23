package org.linjia.deerstrike.base;

import java.util.HashMap;
import java.util.Map;

import org.linjia.deerstrike.model.DbParameter;
import org.linjia.deerstrike.pi.IDataRow;

public class DataRowBase implements IDataRow {
	private Map<String, Object>	dataRowMap;

	public DataRowBase() {
		this.dataRowMap = new HashMap<String, Object>();
	}

	public void addParameter(DbParameter dbParameter) {
		this.dataRowMap.put(dbParameter.getName(), dbParameter.getValue());
	}

	public void setValue(String name, Object value) {
		this.dataRowMap.put(name, value);
	}

	/**
	 * 根据字段名字获取相应的字段值对像
	 * @param name
	 * @return
	 */
	public Object getValue(String name) {
		return this.dataRowMap.get(name);
	}

	/**
	 * 根据字段名字获取相应的字段值并返回一个字符串值
	 * @param name
	 * @return
	 */
	public String getString(String name) {
		Object value = this.getValue(name);
		return String.valueOf(value);
	}
	
	/**
	 * 从数据库装载数据的状态标记 
	 * true 表示装载成功 false 表示装载失败
	 */
	private boolean loadState = false;  
	public boolean checkLoadState() {		
		return this.loadState;
	}
	
	public void loadStateToTrue() {
		this.loadState = true;
	}
	
	public void loadStateToFalse() {
		this.loadState = false;
	}
}
