package org.linjia.deerstrike;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.linjia.deerstrike.model.AopParameter;
import org.linjia.deerstrike.model.DbCommandText;
import org.linjia.deerstrike.model.DbParameter;
import org.linjia.deerstrike.pi.IDataRow;
import org.linjia.deerstrike.pi.IDataRowSet;
import org.linjia.deerstrike.pi.IRowPager;

public class DbCommand {
	protected final Logger logger = LogManager.getLogger(DbCommand.class);

	public DbCommand(String sqlText) {
		DbCommandText dbCommandText = DbCommandUtil.getDbCommandText(sqlText);
		this.commandText = dbCommandText.getCommandText();
		this.parameters = new ArrayList<DbParameter>();
		for (String parameter : dbCommandText.getParameters()) {
			this.parameters.add(new DbParameter(parameter));
		}

	}

	private Connection connection;
	private String commandText;
	private List<DbParameter> parameters;

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public String getCommandText() {
		return commandText;
	}

	public List<DbParameter> getParameters() {
		return parameters;
	}

	/**
	 * 把AOP拦截的参数填充到DbParameter中
	 * 
	 * @param aopParameters
	 */
	public void fillParamValue(List<AopParameter> aopParameters) {
		Object objFlag = null;
		for (AopParameter aopParameter : aopParameters) {
			objFlag = aopParameter.getValue();
			if (objFlag instanceof IDataRowSet)
				continue;
			if (objFlag instanceof IRowPager)
				continue;
			if (objFlag == null)
				continue;
			if (objFlag instanceof Long)
				if ((Long) objFlag == Long.MIN_VALUE)
					continue;
			if (objFlag instanceof Integer)
				if ((Integer) objFlag == Integer.MIN_VALUE)
					continue;
			// 判断此参数是否是IDataRow的实现类，如果是就取它里面的属性名和值，进行设置，如果不是，就直接进行设置
			if (objFlag instanceof IDataRow) {
				Field[] fields = objFlag.getClass().getDeclaredFields();
				for (int i = 0, len = fields.length; i < len; i++) {
					// 对于每个属性，获取属性名
					String varName = fields[i].getName();
					try {
						// 获取原来的访问控制权限
						boolean accessFlag = fields[i].isAccessible();
						// 修改访问控制权限
						fields[i].setAccessible(true);
						// 获取在对象f中属性fields[i]对应的对象中的变量
						Object value = fields[i].get(objFlag);
						if (value != null) {
							setParamValue(varName, value);
						}
						// 恢复访问控制权限
						fields[i].setAccessible(accessFlag);
					} catch (IllegalArgumentException ex) {
						ex.printStackTrace();
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
					}
				}
			} else if (objFlag instanceof List) {
				String indexStr = "$" + aopParameter.getName() + "$";
				if (this.commandText.indexOf(indexStr) != -1) {
					List<Object> list = (List<Object>) objFlag;
					String wenHaoStr = "?";
					for (int i = 1; i < list.size(); i++) {
						wenHaoStr = wenHaoStr + ",?";
					}
					this.commandText = this.commandText.replace(indexStr,
							wenHaoStr);
					setParamValue(aopParameter.getName(), objFlag);
				}
			} else {
				if (!setParamValue(aopParameter.getName(), objFlag)) {
					String indexStr = "!" + aopParameter.getName() + "!";
					if (this.commandText.indexOf(indexStr) != -1) {
						if (objFlag.toString().toUpperCase().equals("DESC")) {
							this.commandText = this.commandText.replace(
									indexStr, "DESC");
						} else {
							this.commandText = this.commandText.replace(
									indexStr, "ASC");
						}
					}
				}
			}
		}
		// 判断是否有需要去除的查询条件
		if (this.commandText.indexOf("~") != -1) {
			for (DbParameter p : this.parameters) {
				if (p.getValue() == null) {
					// BETWEEN条件查询时,去掉MIN和MAX标识
					String parameterName = p.getName().replace("MIN", "")
							.replace("MAX", "");
					String sqlParameterName = replaceStr(parameterName);
					replaceStr(parameterName, sqlParameterName);
				}
			}
			// 去除换行符
			this.commandText = this.commandText.replace("\n\t\t", " ")
					.replaceAll("((AND)|(OR))[ ]+[(][ ]+[)]", " ");
			// 去除排序
			this.commandText = this.commandText.replaceAll("![A-Za-z0-9_]*!",
					" ");
			// 去除~符
			this.commandText = this.commandText.replace("~", "");
		}
	}

	/**
	 * 用正则去除不需要的查询条件
	 * 
	 * @param parameterName
	 * @param sqlParameterName
	 */
	private void replaceStr(String parameterName, String sqlParameterName) {
		String regex = "((AND)|(OR))[ ]+~[ ]*"
				+ sqlParameterName
				+ "[ ]*((IN)|(NOT IN)|(LIKE)|(BETWEEN)|(=)|(>)|(<)|(>=)|(<=)|(<>)|(!=))[ ]*[(]{0,1}[ ]*((\\?){1}|"
				+ "((\\?){1}[ ]*AND[ ]*(\\?){1})|('%\\?%')|(\\$"
				+ parameterName + "\\$))[ ]*[)]{0,1}[ ]*~";
		this.commandText = this.commandText.replace("\n\t\t", " ").replaceAll(
				regex, " ");
	}

	/**
	 * 转化，例：把userId转化成user_id
	 * 
	 * @param parameterName
	 * @return
	 */
	private String replaceStr(String parameterName) {
		List<String> words = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[A-Z]");
		Matcher matcher = pattern.matcher(parameterName);
		while (matcher.find()) {
			words.add(matcher.group());
		}
		for (String flagStr : words)
			parameterName = parameterName.replace(flagStr,
					"_" + flagStr.toLowerCase());
		return parameterName;
	}

	/**
	 * 修改Param的Value
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public boolean setParamValue(String name, Object value) {
		// 后期需要修改，当传入参数在parameters中不存在，要记录到错误日志中

		// 过滤掉自定义查询条件
		if (name.equals("customCondition")) {
			return true;
		}

		boolean flag = false;
		for (DbParameter parameter : this.parameters) {
			if (parameter.getName().equals(name)) {
				parameter.setValue(value);
				flag = true;
				break;
			}
		}

		if (logger.isDebugEnabled()) {
			if (!flag) {
				logger.warn("没有找到DbParameter相应列明为" + name + "列数据");
			}
		}
		return flag;
	}

	/**
	 * 为方便日志查看，容易看到那个sql出问题
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("执行sql语句为：");
		sb.append(commandText);
		return sb.toString();
	}
}
