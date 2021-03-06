package org.smart.framework.datacenter;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.smart.framework.datacenter.annotation.Column;
import org.smart.framework.datacenter.annotation.Table;
import org.smart.framework.util.IdentiyKey;
import org.smart.framework.util.PackageScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.jdbc.core.JdbcTemplate;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.google.common.collect.Lists;

/**
 * jdbc自定义类
 * 
 * @author smart
 *
 */
public class BaseJdbcTemplate extends JdbcTemplate {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected String packageScan;

	public void entityScan(String[] packageScan) {
		// 获取所有继承于Entity类的对象列表。
		// 扫描所有Entity对象。获取 表名，字段名,
		Collection<Class<Entity>> collection = PackageScanner.scanPackages(packageScan);
		DatabaseMetaData databaseMetaData = null;
		try {
			databaseMetaData = this.getDataSource().getConnection().getMetaData();
		} catch (SQLException e1) {
			throw new RuntimeException(e1);
		}
		for (Class<Entity> clz : collection) {
			if (Entity.class.isAssignableFrom(clz)) {
				EntityInfo ei = new EntityInfo();
				ei.className = clz.getCanonicalName();
				Table tname = clz.getAnnotation(Table.class);
				if (null != tname) {
					ei.tableName = "`" + tname.name() + "`";
					ei.tableType = tname.type();
				} else {
					LOGGER.error(clz.getCanonicalName() + "未定义表名");
					continue;
				}
				List<String> dbColumeNames = new ArrayList<>();
				try {
					ResultSet rs = databaseMetaData.getTables(null, null, ei.tableName, new String[] { "TABLE" });
					if (rs.next() == false) {
						throw new RuntimeException(tname + "表不存在.");
					}

					rs = databaseMetaData.getColumns(null, null, ei.tableName, "%");
					while (rs.next()) {
						dbColumeNames.add("`" + rs.getString("COLUMN_NAME") + "`");
					}
					String[] tableNames = new String[dbColumeNames.size()];
					ei.dbColumnNames = dbColumeNames.toArray(tableNames);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}

				// Entity反射属性列表
				Field[] fields = clz.getDeclaredFields();
				ArrayList<String> entityField = new ArrayList<>();
				List<String> pkNames = new ArrayList<>();
				for (Field field : fields) {

					Column column = field.getAnnotation(Column.class);
					if (null != column) {

						String dbColumName = (!("").equals(column.alias())) ? column.alias() : field.getName();
						dbColumName = "`" + dbColumName + "`";
						if (column.pk()) {
							pkNames.add(dbColumName);
						}
						if (column.fk()) {
							ei.fkName = dbColumName;
						}
						entityField.add(dbColumName);
						ei.columnNameMapping.put(dbColumName, field.getName());
						ei.feildNameMapping.put(field.getName(), dbColumName);
					}
				}

				String[] pNames = new String[pkNames.size()];
				ei.pkName = pkNames.toArray(pNames);
				if (ei.pkName == null || ei.pkName.length == 0) {
					LOGGER.error(ei.className + " 实体缺少主键");
				}
				Set<String> set = new HashSet<>(Arrays.asList(ei.dbColumnNames));
				for (String dbc : ei.columnNameMapping.keySet()) {
					if (!set.contains(dbc)) {
						throw new RuntimeException(
								"entity:" + ei.className + " table: " + ei.tableName + " " + dbc + " colume not exsit");
					}
				}

				// 实例化Entity
				try {
					ei.entity = clz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				EntityInfo.ENTITY_INFOS.put(clz, ei);
				EntityInfo.ENTITY_BEANCOPIER.put(clz, BeanCopier.create(clz, clz, false));
				EntityInfo.ENTITY_CONSTRUCT_ACCESS.put(clz, ConstructorAccess.get(clz));
			}

		}
	}
	
	public void trancateAll() {
		Collection<Class<Entity>> collection = PackageScanner.scanPackages(packageScan);
		for (Class<Entity> clz : collection) {
			if (Entity.class.isAssignableFrom(clz)) {
				EntityInfo ei = new EntityInfo();
				ei.className = clz.getCanonicalName();
				Table tname = clz.getAnnotation(Table.class);
				if (null != tname) {
					this.execute("TRUNCATE TABLE " + "`" + tname.name() + "`");
				} 
			}

		}
	}

	public void init() {
		LOGGER.info("db bean package:{}", packageScan);
		String[] temp = packageScan.split(",");
		entityScan(temp);
	}

	public EntityInfo getEntityInfo(Class<? extends Entity> clz) {
		if (!EntityInfo.ENTITY_INFOS.containsKey(clz)) {
			throw new RuntimeException("table name not exsit");
		}
		return EntityInfo.ENTITY_INFOS.get(clz);
	}

	private <T extends Entity> DataKeyValue<String, Object> getDbKeyValue(T entity) {
		EntityInfo info = this.getEntityInfo(entity.getClass());
		DataKeyValue<String, Object> result = new DataKeyValue<>();
		Map<String, Object> values = null;
		try {
			values = entity.rowValue();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("{}", e);
			return result;
		}

		for (String dbCname : info.dbColumnNames) {
			String key = info.columnNameMapping.get(dbCname);
			if (values.containsKey(key)) {
				result.add(dbCname, values.get(key));
			}
		}
		return result;
	}
	private <T extends Entity> DataKeyValue<String, Object> getDbKeyValue(T entity,String... fieldNames) {
		EntityInfo info = this.getEntityInfo(entity.getClass());
		DataKeyValue<String, Object> result = new DataKeyValue<>();
		Map<String, Object> values = null;
		try {
			values = entity.rowValue();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("{}", e);
			return result;
		}
		
		for (String fieldName : fieldNames) {
			String key = info.feildNameMapping.get(fieldName);
			if (values.containsKey(fieldName)) {
				result.add(key, values.get(fieldName));
			}
		}
		if (info.fkName != null) {
			String key = info.columnNameMapping.get(info.fkName);
			if (values.containsKey(key)) {
				result.add(info.fkName, values.get(key));
			}
		}
		for (String pk : info.pkName) {
			String key = info.columnNameMapping.get(pk);
			if (values.containsKey(key)) {
				result.add(pk, values.get(key));
			}
		}
		return result;
	}

	// private <T extends Entity> List<Object> getRowValue(T entity, boolean update) {
	// EntityInfo info = this.getEntityInfo(entity.getClass());
	// Map<String, Object> values;
	// try {
	// values = entity.getRowValue();
	// } catch (IllegalArgumentException | IllegalAccessException e) {
	// logger.error("{}", e);
	// return Collections.emptyList();
	// }
	// List<Object> list = new ArrayList<>();
	// for (String dbCname : info.columnName) {
	// String key = info.columnNameMapping.get(dbCname);
	// if (values.containsKey(key)) {
	// list.add(values.get(key));
	// }
	// }
	// if (update){
	// list.addAll(list);
	// }
	// return list;
	// }

	/**
	 * 更新
	 * 
	 * @param entity
	 * @return
	 */
	public <T extends Entity> int update(T entity) {
		DataKeyValue<String, Object> map = getDbKeyValue(entity);
		EntityInfo info = this.getEntityInfo(entity.getClass());
		String sql = info.getInsertUpdateSql(map.getKeys());
		List<Object> valuses = new ArrayList<>(map.getValues());
		for (String pk : info.pkName) {
			map.remove(pk);
		}
		valuses.addAll(map.getValues());
		return super.update(sql, valuses.toArray());
	}
	/**
	 * 更新
	 * 
	 * @param entity
	 * @return
	 */
	public <T extends Entity> int update(T entity, String... fieldNames) {
		DataKeyValue<String, Object> map = getDbKeyValue(entity,fieldNames);
		EntityInfo info = this.getEntityInfo(entity.getClass());
		String sql = info.getUpdateSql(map.getKeys());
		List<Object> valuses = new ArrayList<>(map.getValues());
		return super.update(sql, valuses.toArray());
	}

	/**
	 * 更新批量
	 * 
	 * @param entity
	 * @return 返回数据库每条语句影响行数
	 */
	public <T extends Entity> int[] update(Collection<T> entitys) {
		// 分組
		Map<String, List<T>> map = groupEntity(entitys);

		List<Integer> resultArr = new ArrayList<>();// 结果
		for (String str : map.keySet()) {
			// 获取同组的sql
			T param = map.get(str).get(0);
			EntityInfo info = this.getEntityInfo(param.getClass());
			String updateSql = null;

			ArrayList<Object[]> arr = new ArrayList<>();
			for (T object : map.get(str)) {
				DataKeyValue<String, Object> rowValue = getDbKeyValue(object);
				if (updateSql == null) {
					updateSql = info.getInsertUpdateSql(rowValue.getKeys());
				}

				List<Object> valuses = new ArrayList<>(rowValue.getValues());
				valuses.addAll(rowValue.getValues());
				Object[] oneObjectValue = valuses.toArray();
				arr.add(oneObjectValue);
			}

			int[] result = this.batchUpdate(updateSql, arr);
			for (int i : result) {
				resultArr.add(i);
			}
		}
		int[] rs = new int[resultArr.size()];
		for (int i : rs) {
			rs[i] = resultArr.get(i);
		}
		return rs;
	}

	/**
	 * 删除实体
	 * 
	 * @param entity
	 * @return
	 */
	public <T extends Entity> int delete(T entity) {
		EntityInfo info = this.getEntityInfo(entity.getClass());
		String sql = null;
		String[] tmp = null;
		if (entity instanceof MutiEntity<?>) {
			MutiEntity<?> mutiEntity = (MutiEntity<?>) entity;
			tmp = new String[info.pkName.length + 1];
			ArrayList<String> list = Lists.newArrayList(info.pkName);
			list.add(info.fkName);
			list.toArray(tmp);
			sql = info.getDeleteSql(tmp);
			List<Object> valueList = Lists.newArrayList(entity.findPkId().getIdentifys());
			valueList.add(mutiEntity.findFkId());
			return super.update(sql, valueList.toArray());
		} else {
			sql = info.getDeleteSql(info.pkName);
			return super.update(sql, entity.findPkId().getIdentifys());
		}
	}

	public <T extends Entity> int delete(Class<T> clazz, LinkedHashMap<String, Object> condition) {
		EntityInfo info = this.getEntityInfo(clazz);
		String[] key = new String[condition.keySet().size()];
		String sql = info.getDeleteSql(condition.keySet().toArray(key));
		return super.update(sql, condition.values().toArray());
	}

	/**
	 * 获取实体
	 * 
	 * @param clazz
	 * @param pk
	 * @return
	 */
	public <T extends Entity> T get(Class<T> clazz, IdentiyKey pk) {

		EntityInfo info = this.getEntityInfo(clazz);
		if (info.pkName.length != pk.getIdentifys().length) {
			throw new RuntimeException("pk number error");
		}
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		for (int i = 0; i < info.pkName.length; i++) {
			String pkName = info.pkName[i];
			Object value = pk.getIdentifys()[i];
			map.put(pkName, value);
		}

		return getFirst(clazz, map);
	}

	/**
	 * 获取首行记录
	 * 
	 * @param clazz
	 *            查询实体类
	 * @param params
	 *            查询条件 key:字段名 value:查询值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entity> T getFirst(Class<T> clazz, LinkedHashMap<String, Object> condition) {
		EntityInfo info = this.getEntityInfo(clazz);
		String[] key = new String[condition.keySet().size()];
		String sql = info.getSelectSql(condition.keySet().toArray(key));

		List<T> result = (List<T>) super.query(sql, info.entity, condition.values().toArray());
		if (result.size() > 0) {
			return result.get(0);

		} else {
			return null;
		}
	}

	/**
	 * 获取表中所有实体(基本不会用,偶尔有几条数据的表，例如Id表)
	 * 
	 * @param clazz
	 *            查询实体类
	 * @return
	 */
	public <T extends Entity> List<T> getList(Class<T> clazz) {
		return getList(clazz, null, 0, 0);
	}

	/**
	 * 根据条件查询
	 * 
	 * @param clazz
	 *            查询实体类
	 * @param params
	 *            查询条件 key:字段名 value:查询值
	 * @return
	 */
	public <T extends Entity> List<T> getList(Class<T> clazz, LinkedHashMap<String, Object> condition) {
		return getList(clazz, condition, 0, 0);
	}

	/**
	 * 分页查询
	 * 
	 * @param clazz
	 * @param params
	 * @param columName
	 * @param limitBegin
	 * @param limitEnd
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entity> List<T> getList(Class<T> clazz, LinkedHashMap<String, Object> condition, int limitBegin,
			int limitEnd) {
		EntityInfo info = this.getEntityInfo(clazz);

		String sql;
		if (condition == null || condition.size() < 1) {
			sql = info.getSelectSql();
			return (List<T>) super.query(sql, info.entity);
		} else {
			String[] key = new String[condition.keySet().size()];
			sql = info.getSelectSql(limitBegin, limitEnd, condition.keySet().toArray(key));
			if (limitBegin > 0) {
				condition.put("limintBegin", limitBegin);
			}
			if (limitEnd > 0 && limitEnd > limitBegin) {
				condition.put("limitEnd", limitEnd);
			}
			return (List<T>) super.query(sql, condition.values().toArray(), info.entity);
		}
	}

	/**
	 * 自定义sql查询
	 * 
	 * @param sql
	 *            sql语句
	 * @param condition
	 *            条件值
	 * @param clazz
	 *            对应实体类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entity> List<T> getList(String sql, Object[] condition, Class<T> clazz) {
		EntityInfo info = this.getEntityInfo(clazz);

		if (condition == null || condition.length < 1) {
			sql = info.getSelectSql();
			return (List<T>) super.query(sql, info.entity);
		} else {
			return (List<T>) super.query(sql, condition, info.entity);
		}
	}
	@SuppressWarnings("unchecked")
	public <T extends Entity> List<T> getList(String sql,  Class<T> clazz) {
		EntityInfo info = this.getEntityInfo(clazz);
		
		return (List<T>) super.query(sql, info.entity);
	}

	/**
	 * 保存
	 * 
	 * @param entity
	 * @return 返回主键
	 */
	public <T extends Entity> void save(T entity) {
		EntityInfo info = this.getEntityInfo(entity.getClass());
		DataKeyValue<String, Object> value = getDbKeyValue(entity);
		String sql = info.getInsertIntoSql(value.getKeys()); // 插入无条件的。
		super.update(sql, value.getValues().toArray());
	}

	/**
	 * 分组entity
	 * 
	 * @param entitys
	 * @return
	 */
	protected <T extends Entity> Map<String, List<T>> groupEntity(Collection<T> entitys) {
		Map<String, List<T>> map = new LinkedHashMap<String, List<T>>();
		for (T t : entitys) {
			EntityInfo info = this.getEntityInfo(t.getClass());
			String tableName = info.tableName;
			List<T> list = null;
			if (map.containsKey(tableName)) {
				list = map.get(tableName);
			} else {
				list = new ArrayList<>();
				map.put(tableName, list);
			}
			list.add(t);
		}
		return map;
	}
	
	public String getPackageScan() {
		return packageScan;
	}
	public void setPackageScan(String packageScan) {
		this.packageScan = packageScan;
	}

}
