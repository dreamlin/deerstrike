package org.linjia.deerstrike;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.linjia.deerstrike.model.DbParameter;
import org.linjia.deerstrike.pi.IDataRow;
import org.linjia.deerstrike.pi.IDataRowSet;
import org.linjia.deerstrike.pi.IRowPager;

public class DbExecutor {
	protected final static Logger logger = LogManager
			.getLogger(DbExecutor.class);

	/**
	 * 插入、修改、删除操作。
	 * 
	 * @param dbCommand
	 */
	public static void write(DbCommand dbCommand) {
		Connection conn = dbCommand.getConnection();
		String cmdText = dbCommand.getCommandText();
		List<DbParameter> parameters = dbCommand.getParameters();
		try {
			PreparedStatement pstmt = conn.prepareStatement(cmdText);
			int ix = 1;
			for (DbParameter p : parameters) {
				Object fieldValue = p.getValue();
				if (fieldValue instanceof List) {
					List<Object> list = (List<Object>) fieldValue;
					for (Object obj : list) {
						pstmt.setObject(ix, obj);
						ix++;
					}
				} else {
					pstmt.setObject(ix, fieldValue);
					ix++;
				}
			}
			pstmt.executeUpdate();
			pstmt.close();

			if (logger.isDebugEnabled()) {
				logger.info("写数据成功：" + dbCommand);
			}

		} catch (SQLException ex) {
			logger.error("执行 插入、修改、删除操作时sql出现异常" + dbCommand, ex);
			throw new RuntimeException(ex);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("执行 插入、修改、删除操作时关闭数据连接出现异常", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 读取单行记录
	 * 
	 * @param dataRow
	 * @param dbCommand
	 * @throws SQLException
	 */
	public static void readRow(IDataRow dataRow, DbCommand dbCommand) {
		Connection conn = dbCommand.getConnection();
		String cmdText = dbCommand.getCommandText();
		List<DbParameter> parameters = dbCommand.getParameters();

		try {
			// 构建 PreparedStatement 并填充参数对象
			PreparedStatement pstmt = conn.prepareStatement(cmdText);
			int ix = 1;
			for (DbParameter p : parameters) {
				Object fieldValue = p.getValue();
				if (fieldValue != null) {
					if (fieldValue instanceof List) {
						List<Object> list = (List<Object>) fieldValue;
						for (Object obj : list) {
							pstmt.setObject(ix, obj);
							ix++;
						}
					} else {
						pstmt.setObject(ix, fieldValue);
						ix++;
					}
				}
			}

			// 执行查询
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				copyResultSetToDataRow(dataRow, rs);
			}
			pstmt.close();
			rs.close();
			if (logger.isDebugEnabled()) {
				logger.info("读取数据成功：" + dbCommand);
			}
		} catch (SQLException ex) {
			logger.warn("执行读取单行记录时sql出现异常" + dbCommand, ex);
			throw new RuntimeException(ex);
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				logger.warn("执行读取单行记录时关闭数据连接出现异常", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 读取行集合记录
	 * 
	 * @param <T>
	 * @param dataRowSet
	 * @param dbCommand
	 * @param rowPager
	 * @throws SQLException
	 */
	public static <T extends IDataRow> void readRowSet(
			IDataRowSet<IDataRow> dataRowSet, DbCommand dbCommand,
			IRowPager rowPager) {
		Connection conn = dbCommand.getConnection();
		String cmdText = dbCommand.getCommandText();
		List<DbParameter> parameters = dbCommand.getParameters();

		Class<?> entityClass = getSuperClassGenericType(dataRowSet.getClass());
		IDataRow dataRow = null;
		try {
			// 构造命令对象 并执行查询
			PreparedStatement pstmt = conn.prepareStatement(cmdText,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			int ix = 1;
			for (DbParameter p : parameters) {
				Object fieldValue = p.getValue();
				if (fieldValue != null) {
					if (fieldValue instanceof List) {
						List<Object> list = (List<Object>) fieldValue;
						for (Object obj : list) {
							pstmt.setObject(ix, obj);
							ix++;
						}
					} else {
						pstmt.setObject(ix, fieldValue);
						ix++;
					}
				}
			}

			ResultSet rs = pstmt.executeQuery();

			int rowIx = 0;
			int beginIx = rowPager.getBeginIx();
			if (rowPager.getPageIndex() != 1) {
				beginIx = beginIx + 1;
			}
			int endIx = rowPager.getEndIx();
			int maxLoop = rowPager.getMaxLoop();

			// 枚举返回结果集
			while (rs.next()) {
				rowIx++;
				if (rowIx < beginIx)
					continue;
				// 统计所有读取行的数量，统计完后退出
				if (rowIx > maxLoop) {
					rowIx = rowIx - 1;
					break;
				}
				if (rowIx > endIx)
					continue;

				try {
					dataRow = (IDataRow) entityClass.newInstance();
				} catch (InstantiationException e) {
					logger.error("实例化读取行集合记录出错：", e);
				} catch (IllegalAccessException e) {
					logger.error("实例化读取行集合记录出错：", e);
				}

				copyResultSetToDataRow(dataRow, rs);

				dataRowSet.add(dataRow);

				if (logger.isDebugEnabled()) {
					logger.debug("本条成功读取" + dataRow);
				}
			}

			rowPager.setHitRows(rowIx);

			pstmt.close();
			rs.close();
			if (logger.isDebugEnabled()) {
				logger.info("读取多条数据成功：" + dbCommand);
			}
		} catch (SQLException ex) {
			logger.warn("执行readRowSet时sql出现异常" + dbCommand, ex);
			throw new RuntimeException(ex);
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				logger.warn("执行readRowSet时关闭数据连接出现异常", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 把ResultSet行数据填充到IDataRow中
	 * 
	 * @param dataRow
	 * @param rs
	 */
	private static void copyResultSetToDataRow(IDataRow dataRow, ResultSet rs) {
		if (logger.isDebugEnabled()) {
			logger.debug("对应的对象" + dataRow + "  列结果集：" + rs);
		}
		try {
			ResultSetMetaData md = rs.getMetaData();
			Class<?> dataRowClass = dataRow.getClass();
			Object fieldValue;
			boolean isInvoke;
			// 从MetaData中取列名和数据
			for (int i = 1; i <= md.getColumnCount(); i++) {
				isInvoke = false;

				String columnName = md.getColumnName(i);

				fieldValue = rs.getObject(columnName);

				String setColumnName = "set" + changeString(columnName, false);

				if (logger.isDebugEnabled()) {
					logger.debug("设置对应的方法：" + setColumnName);
				}
				try {
					Method[] methods = dataRowClass.getMethods();
					for (Method method : methods) {
						if (method.getName().compareTo(setColumnName) == 0) {
							method.invoke(dataRow, fieldValue);
							isInvoke = true;
							break;
						}
					}
					if (!isInvoke) {
						for (Method method : methods) {
							if (method.getName().compareTo("setValue") == 0) {
								method.invoke(dataRow, columnName, fieldValue);
								break;
							}
						}
					}
				} catch (Exception ex) {
					logger.error("放入对应的列出错，设置的方法名：" + columnName
							+ " 数据库里查询出来的类型：" + fieldValue.getClass(), ex);
				}
			}
			dataRow.loadStateToTrue();
		} catch (SQLException e) {
			logger.error("放入对应的列出错：", e);
		}
	}

	/**
	 * 把数据库中的列表转成属性名
	 * 
	 * @param paramString
	 * @return
	 */
	private static String changeString(String paramString, boolean boolFlag) {

		String flagsString = null;
		if (boolFlag)
			flagsString = paramString.substring(0, 1).toLowerCase();
		else
			flagsString = paramString.substring(0, 1).toUpperCase();
		paramString = flagsString + paramString.substring(1);
		int flagInt = paramString.indexOf('_');
		while (flagInt != -1) {
			String gangString = paramString.substring(flagInt, flagInt + 2);
			String sdString = paramString.substring(flagInt + 1, flagInt + 2)
					.toUpperCase();

			paramString = paramString.replace(gangString, sdString);
			flagInt = paramString.indexOf('_');
		}
		return paramString;
	}

	/**
	 * 获取父类的参数类型
	 * 
	 * @param clazz
	 * @param index
	 *            指泛型引索
	 * @return
	 */
	private static Class getSuperClassGenericType(Class clazz, int index) {
		Type type = clazz.getGenericSuperclass();
		if (!(type instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] types = ((ParameterizedType) type).getActualTypeArguments();
		if (index >= types.length || index < 0) {
			return Object.class;
		}
		return (Class) types[index];
	}

	private static Class getSuperClassGenericType(Class clazz) {
		return getSuperClassGenericType(clazz, 0);
	}

}
