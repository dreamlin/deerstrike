# deerstrike
Java：采用AOP思想，开发的DB框架，类似于mybatis。


首先，我们来说一下Deer Strike的设计理念之一“约定优于配置”：

	1.约定可以减少大量配置。
	2.可以大大降低新人的学习成本。因为配置项少了，可以根据约定文档中的内容进行学习，不用专人详细指导，就很快就能上手。
	我们目前所在使用的Maven，它的最核心设计理念之一就是“约定优于配置”。

**约定项：**

	1.SQL语句关键字统一大写，如SELECT、FROM、WHERE、AND、OR等等。
	2.方法名为：doXXXXX格式的，以do开头，都是由生成工具生成的，请不要对这些方法进行任何修改。
	3.插入操作时，如果有自增字段，如ID，需要把SQL语句中的ID字段去掉。
	4.数据库中表命名为：tbl_goods_image，在Deer Strike中对应应转换成TblGoodsImage。
	5.数据库中字段命名为： goods_image_id，在Deer Strike中对应应转换成goodsImageId。
	6.在读取所有记录的时候请使用new RowPagerBase().selectAllQueryPager()。
	7.在实体类中有一个boolean checkLoadState()方法，可以判断，数据是否成功加载。
	8.查询中LIKE的用法。例：在XML中的SQL语句为SELECT * FROM tbl_news WHERE title LIKE #title#，输入的参数值为%title%或者%title等。
	9.IN条件查询，把IN参数放在DAO方法的参数的最后：例：SQL：SELECT * FROM tbl_news WHERE id = #id# AND title = #title# AND type in ($type$)。DAO方法为XXXX(int id , String title , List<Object> type)。IN条件传的参数类型必须为List<Object>类型。
	10.BETWEEN条件查询时，SQL写法例：SELECT * FROM tbl_news WHERE new_id BETWEEN #newIdMIN# AND # newIdMAX #，就是最小加MIN、最大加最后加MAX。
	11.当查询条件不固定，一下有一下没有的时候，SQL可以这样子写：SELECT * FROM tbl_news WHERE 1=1 AND ~id = #id#~ AND ~title = #title#~ AND ~type in ($type$)~。就是在查询条件的左右加~号，~id = #id#~，当条件不固定时，在WHERE后面一定要跟1=1。有不查询项的时候，传入NULL，如果是int，那就传入Integer.MIN_VALUE，如果是long那就传入Long.MIN_VALUE。
	12.注意点：SQL中普通条件查询参数格式为：#id#，IN查询参数格式为：$ids$，ORDER排序参数格式为：!id!。这些SQL中的参数命名，要和对应DAO方法的参数命名相同。
	13.在Tomcat容器内跑时，请将配置文件放在war包的边上etc/deerstrike目录下（war包名必须为ROOT.war）。在调试时，把配置文件放在项目的根目录etc/deerstrike下（仅支持Maven项目）。

**Dao和对应的xml中命名规则：**

	根据什么字段就“By查询条件字段名称”
	readBy查询条件字段名称：单条记录
	selectBy查询条件字段名称：多条记录
	selectCount、selectCountBy查询条件字段名称：记录条数

**deleteBy查询条件字段名称：**

	updateBy查询条件字段名称：

**Service命名规则：**

	一个模块，创建两个Service。
	例如：Read模块名Service、Write模块名Service

**Service中方法的命名规则：**

	方法名前统一加对应的表名。
	添加，例：create模块名。
	删除，例：delete模块名ByTitle。
	修改，例：update模块名ByTitle。
	查询所有记录，例：get表名ListByAllQuery。
	查询多条记录，例：get表名ListBy查询条件字段名称。
	查询单条记录，例：get表名By查询条件字段名称。
	查询记录条数：例：get表名ListCount、get表名ListCountBy查询条件字段名称。

**使用开发步骤：**

	1、创建一个XML配置文件
	2、创建一个Entitie，该类可用代码生成工具
	3、创建一个Dao，该类只用写方法体，不用实现具体代码
	4、创建一个Service，即可使用

**XML：**

	<?xml version="1.0" encoding="UTF-8"?>
	<sqlMap id="org.linjia.core.daos.AdminDao">
		<sqlItem id="doInsert">INSERT INTO admin (admin_id, real_name, nike_name, password) VALUES (#adminId#,#realName#,#nikeName#,#password#) </sqlItem>
		<sqlItem id="doUpdate">UPDATE admin SET real_name=#realName#, nike_name=#nikeName#, password=#password# WHERE admin_id=#adminId# </sqlItem>
		<sqlItem id="doDelete">DELETE FROM admin WHERE admin_id=#adminId# </sqlItem>
		<sqlItem id="doSelect">SELECT admin_id, real_name, nike_name, password FROM admin</sqlItem>
		<sqlItem id="doRead">SELECT admin_id, real_name, nike_name, password FROM admin WHERE admin_id=#adminId# </sqlItem>
	</sqlMap>

**entitie：**

	public class Admin implements IDataRow {
		private String adminId;
		private String password;
		public String getAdminId() {
			return adminId;
		}
		public void setAdminId(String adminId) {
			this.adminId = adminId;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}

**dao：**

	public class AdminDao implements ITableDao {
		public void doDelete(Admin dataRow) {}
		public void doRead(Admin dataRow) {}
		public void doSelect(IDataRowSet<Admin> dataRowSet, IRowPager rowPager) {}
		public void doInsert(Admin dataRow) {}
		public void doUpdate(Admin dataRow) {}
	}

**service：**

	public class ReadAdminService {
		public static void getActivityByID(Admin admin){
			DaoProxyFactory.createProxy(AdminDao.class).doRead(admin);
		}
	}
