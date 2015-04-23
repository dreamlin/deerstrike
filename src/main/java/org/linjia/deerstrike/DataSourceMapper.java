package org.linjia.deerstrike;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.linjia.deerstrike.model.ConnInfo;

/**
 * 需要考虑当数据库服务器关闭时，DataSource异常
 */
public class DataSourceMapper {

	protected final Logger logger = LogManager
			.getLogger(DataSourceMapper.class);
	private Map<String, ConnInfo> connInfoWriteMap;
	private Map<String, List<ConnInfo>> connInfoReadMap;
	private Map<String, DataSource> dataSourceWriteMap;
	private Map<String, DataSource> dataSourceReadMap;

	private volatile static DataSourceMapper _MeObj;

	public static DataSourceMapper create() {
		if (_MeObj == null) {
			synchronized (DataSourceMapper.class) {
				if (_MeObj == null) {
					_MeObj = new DataSourceMapper();
				}
			}
		}
		return _MeObj;
	}

	private String filePath; // 配置文件保存位置

	private DataSourceMapper() {
		URI uri = null;
		try {
			uri = new URI(this.getClass().getResource("/").toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String strFlag = uri.getPath();
		if (strFlag.indexOf("ROOT") != -1)
			strFlag = strFlag.substring(0, strFlag.indexOf("ROOT"));
		else
			strFlag = strFlag.substring(0, strFlag.indexOf("target"));

		filePath = strFlag + "etc/deerstrike/DataSource.xml";
		this.connInfoWriteMap = new HashMap<String, ConnInfo>();
		this.connInfoReadMap = new HashMap<String, List<ConnInfo>>();
		this.dataSourceReadMap = new HashMap<String, DataSource>();
		this.dataSourceWriteMap = new HashMap<String, DataSource>();
		this.doInit(filePath);
	}

	/**
	 * 通过类名取得读数据源
	 * 
	 * @param className
	 *            类名
	 * @return DataSource 数据源
	 */
	public DataSource getReadDataSource(String className) {
		return this.getDataSource(className, true);
	}

	/**
	 * 通过类名取得写数据源
	 * 
	 * @param className
	 *            类名
	 * @return DataSource 数据源
	 */
	public DataSource getWriteDataSource(String className) {
		return this.getDataSource(className, false);
	}

	/**
	 * 通过类名取得数据源
	 * 
	 * @param className
	 *            类名
	 * @param isRead
	 *            true：read false:wrtie
	 * @return
	 */
	private synchronized DataSource getDataSource(String className,
			boolean isRead) {
		// 查看DataSource的缓存中是否有，如果有，直接返回
		if (isRead) {
			if (this.dataSourceReadMap.containsKey(className)) {
				return this.dataSourceReadMap.get(className);
			}
		} else {
			if (this.dataSourceWriteMap.containsKey(className)) {
				return this.dataSourceWriteMap.get(className);
			}
		}
		ConnInfo connInfo = null;
		if (isRead) {
			if (this.connInfoReadMap.containsKey(className)) {
				List<ConnInfo> connInfos = this.connInfoReadMap.get(className);
				// 随机取连接信息
				int size = connInfos.size();
				Random r = new Random();
				int ix = r.nextInt(size);
				connInfo = connInfos.get(ix);
			}
		} else {
			if (this.connInfoReadMap.containsKey(className)) {
				connInfo = this.connInfoWriteMap.get(className);
			}
		}
		if (connInfo == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("没有相应类名为：" + className + "的数据源连接信息");
			}
			return null;
		}
		// 创建并缓存
		DataSource dataSource = createDataSource(connInfo);
		if (isRead)
			this.dataSourceReadMap.put(className, dataSource);
		else
			this.dataSourceWriteMap.put(className, dataSource);
		return dataSource;
	}

	/**
	 * 通过ConnInfo创建DataSource
	 * 
	 * @param connInfo
	 *            数据源连接信息
	 * @return DataSource 数据源
	 */
	public DataSource createDataSource(ConnInfo connInfo) {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(connInfo.getdBDriver());
		ds.setUrl(connInfo.getdBDriverUrl() + connInfo.getdBName());
		ds.setUsername(connInfo.getdBUser());
		ds.setPassword(connInfo.getdBPwd());
		ds.setMinIdle(connInfo.getMinConnSiez());
		ds.setMaxActive(connInfo.getMaxConnSize());
		ds.setConnectionProperties("characterEncoding=utf8;");
		ds.setValidationQuery("select 1");
		ds.setTestOnBorrow(true);
		ds.setTestWhileIdle(true);
		ds.setTimeBetweenEvictionRunsMillis(3600000);
		ds.setMinEvictableIdleTimeMillis(1800000);

		return ds;
	}

	/**
	 * 初始化
	 * 
	 * @param configXmlPath
	 *            配置文件目录路径
	 */
	private void doInit(String configXmlPath) {
		logger.info("DataSource配置文件目录路径:" + configXmlPath);
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File(configXmlPath));
			Element root = document.getRootElement();
			List tableNodes = root.selectNodes("connItem");

			for (Object node : tableNodes) {
				Element tableNode = (Element) node;
				String className = tableNode.attributeValue("className");
				String connType = tableNode.attributeValue("connType");

				ConnInfo connInfo = new ConnInfo();
				connInfo.setdBDriver(tableNode.element("dBDriver").getText());
				connInfo.setdBDriverUrl(tableNode.element("dBDriverUrl")
						.getText());
				connInfo.setdBName(tableNode.element("dBName").getText());
				connInfo.setdBUser(tableNode.element("dBUser").getText());
				connInfo.setdBPwd(tableNode.element("dBPwd").getText());
				connInfo.setMaxConnSize(Integer.parseInt(tableNode.element(
						"maxConnSize").getText()));
				connInfo.setMinConnSiez(Integer.parseInt(tableNode.element(
						"minConnSiez").getText()));

				if (connType.compareTo("write") == 0) {
					this.connInfoWriteMap.put(className, connInfo);
				} else {
					List<ConnInfo> connInfos = this.connInfoReadMap
							.get(className);
					if (connInfos == null)
						connInfos = new ArrayList<ConnInfo>();
					connInfos.add(connInfo);
					this.connInfoReadMap.put(className, connInfos);
				}
			}
		} catch (DocumentException e) {
			logger.error("读取解析初始化数据源信息时失败；路径为：" + configXmlPath, e);
		}
	}
}