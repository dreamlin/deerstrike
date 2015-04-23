package org.linjia.deerstrike;

import java.lang.reflect.Method;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.linjia.deerstrike.base.RowPagerBase;
import org.linjia.deerstrike.model.AopParameter;
import org.linjia.deerstrike.pi.IDataRow;
import org.linjia.deerstrike.pi.IDataRowSet;
import org.linjia.deerstrike.pi.IRowPager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class DaoMethodInterceptor implements MethodInterceptor {

	protected final static Logger logger = LogManager
			.getLogger(DaoMethodInterceptor.class);

	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {

		AopMethod aopMethod = new AopMethod(method);
		// 取得类名、方法名
		String className = aopMethod.getOwnerClassName();

		if (className.compareTo("java.lang.Object") == 0)
			return false;

		// 取得方法名
		String methodName = aopMethod.getOwnerMethodName();

		// 把拦截的参数值，set到对应的Parameter中去
		int ix = 0;
		for (Object object : args) {
			aopMethod.setParameter(ix, object);
			ix++;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("拦截的类名：" + className + " 方法名：" + methodName);
		}
		// 从MethodSqlMapper中取SQLText
		// FIXME
		MethodSqlMapper methodSqlMapper = MethodSqlMapper.create();
		String sqlText = methodSqlMapper.getSqlText(className, methodName);

		if (sqlText == null) {
			logger.error("类名：" + className + ",方法名：" + methodName
					+ ",SqlText不存在");
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("拦截执行的sqlText:" + sqlText);
		}
		// 通过判断方法是读是写，来相应的读取DataSource
		DataSourceMapper dsm = DataSourceMapper.create();
		DataSource ds;
		boolean boolFlag = true;// true ：读 false：写
		// 判断是读还是写
		if (!sqlText.trim().toUpperCase().startsWith("SELECT")) {
			boolFlag = false;
		}
		if (boolFlag) {
			ds = dsm.getReadDataSource(className);
		} else {
			ds = dsm.getWriteDataSource(className);
		}

		if (ds == null) {
			logger.error("数据源不存在");
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("数据源为：" + (boolFlag ? "读" : "写"));
		}

		// 是否读取单行 true 是 false 否
		boolean isDataRow = true;
		IRowPager rowPager = null;
		IDataRow dataRow = null;
		IDataRowSet dataRowSet = null;

		List<AopParameter> aopParameters = aopMethod.getParameters();
		for (AopParameter aopParameter : aopParameters) {
			if (aopParameter.getClazz().equals(IRowPager.class)) {
				rowPager = (IRowPager) aopParameter.getValue();
			} else if (aopParameter.getValue() instanceof IDataRow) {
				dataRow = (IDataRow) aopParameter.getValue();
			} else if (aopParameter.getValue() instanceof IDataRowSet) {
				dataRowSet = (IDataRowSet) aopParameter.getValue();
				isDataRow = false;
			} else if (aopParameter.getName().equals("customCondition")) {
				sqlText = sqlText + " " + aopParameter.getValue();
			}
		}

		if (!isDataRow && rowPager == null)
			rowPager = new RowPagerBase().selectAllQueryPager();

		// 创建DbCommand，传入SQLText，设置Connection
		DbCommand dbCommand = new DbCommand(sqlText);
		// 把拦截的参数从AopMethod取出，set到DbCommand中
		dbCommand.fillParamValue(aopParameters);
		dbCommand.setConnection(ds.getConnection());

		if (logger.isDebugEnabled()) {
			logger.debug("执行的dbCommand:" + dbCommand);
		}

		if (!boolFlag)// 写操作
		{
			DbExecutor.write(dbCommand);
			return true;
		}
		if (isDataRow) // 读取单行记录
		{
			DbExecutor.readRow(dataRow, dbCommand);
			return true;
		}
		if (!isDataRow)// 读取多行记录
		{
			DbExecutor.readRowSet(dataRowSet, dbCommand, rowPager);
			return true;
		}
		return false;
	}
}
