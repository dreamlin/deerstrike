package org.linjia.deerstrike;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MethodSqlMapper {
	protected final Logger logger = LogManager.getLogger(MethodSqlMapper.class);
	private Map<String, String> methodSqlMapperMap;

	private volatile static MethodSqlMapper _MeObj;

	public static MethodSqlMapper create() {
		if (_MeObj == null) {
			synchronized (MethodSqlMapper.class) {
				if (_MeObj == null) {
					_MeObj = new MethodSqlMapper();
				}
			}
		}
		return _MeObj;
	}

	private String filePath; // 配置文件保存位置

	private MethodSqlMapper() {
		URI uri = null;
		try {
			uri = new URI(this.getClass().getResource("/").toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String strFlag = uri.getPath();
		if (strFlag.indexOf("ROOT") != -1)
			strFlag = strFlag.substring(0, strFlag.indexOf("ROOT"));
		else if (strFlag.indexOf("target") != -1)
			strFlag = strFlag.substring(0, strFlag.indexOf("target"));
		else
			strFlag = strFlag.substring(0, strFlag.indexOf("job_"));
		filePath = strFlag + "etc/deerstrike/methodsql/";
		doInit(filePath);
	}

	/**
	 * 取SQL句话
	 * 
	 * @param className
	 *            类名
	 * @param methodName
	 *            方法名
	 * @return
	 */
	public String getSqlText(String className, String methodName) {
		String flagStr = className + methodName;
		if (this.methodSqlMapperMap.containsKey(flagStr)) {
			return this.methodSqlMapperMap.get(flagStr);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("没有找到类名为：" + className + " 方法名为：" + methodName
						+ " 相应的sql语句。");
			}
			return null;
		}
	}

	/**
	 * 初始化，从xml文档中装载SQL句话
	 * 
	 * @param filePath
	 *            配置文件所在路径
	 */
	private void doInit(String filePath) {
		logger.info("MethodSql配置文件的绝对路径：" + filePath);
		methodSqlMapperMap = new HashMap<String, String>();
		String configXmlPath = null;
		String fileName = null;

		// 考虑是否应该做文件过滤
		// 遍历filePath目录中xml文件
		File file = new File(filePath);
		for (File fileFlag : file.listFiles()) {
			fileName = fileFlag.getName();
			if (fileName.toLowerCase().endsWith(".xml")) {
				configXmlPath = filePath + fileName;

				SAXReader reader = new SAXReader();
				try {
					Document document = reader.read(new File(configXmlPath));
					Element root = document.getRootElement();
					List tableNodes = root.selectNodes("sqlItem");
					String className = root.attributeValue("id");

					for (Object node : tableNodes) {
						Element tableNode = (Element) node;
						String id = tableNode.attributeValue("id");
						String sqlStr = tableNode.getText();
						this.methodSqlMapperMap.put(className + id, sqlStr);
					}
				} catch (DocumentException e) {
					logger.error("读取解析初始化sql信息时失败", e);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("methodSqlMapperMap:" + methodSqlMapperMap);
				}
			}
		}
	}
}