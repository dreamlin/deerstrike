package org.linjia.deerstrike.model;

public class ConnInfo {
	private String dBDriver;//驱动类名
	private String dBDriverUrl;//驱动连接协议
	private String dBName;//数据库名
	private String dBUser;//用户名
	private String dBPwd;//密码
	private int maxConnSize;//最大连接数
	private int minConnSiez;//最小连接数
	
	public String getdBDriver() {
		return dBDriver;
	}
	public void setdBDriver(String dBDriver) {
		this.dBDriver = dBDriver;
	}
	public String getdBDriverUrl() {
		return dBDriverUrl;
	}
	public void setdBDriverUrl(String dBDriverUrl) {
		this.dBDriverUrl = dBDriverUrl;
	}
	public String getdBName() {
		return dBName;
	}
	public void setdBName(String dBName) {
		this.dBName = dBName;
	}
	public String getdBUser() {
		return dBUser;
	}
	public void setdBUser(String dBUser) {
		this.dBUser = dBUser;
	}
	public String getdBPwd() {
		return dBPwd;
	}
	public void setdBPwd(String dBPwd) {
		this.dBPwd = dBPwd;
	}
	public int getMaxConnSize() {
		return maxConnSize;
	}
	public void setMaxConnSize(int maxConnSize) {
		this.maxConnSize = maxConnSize;
	}
	public int getMinConnSiez() {
		return minConnSiez;
	}
	public void setMinConnSiez(int minConnSiez) {
		this.minConnSiez = minConnSiez;
	}	
}
