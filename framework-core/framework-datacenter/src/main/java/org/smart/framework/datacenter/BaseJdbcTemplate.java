package org.smart.framework.datacenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.util.IdentifyKey;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

/**
 * jdbc自定义类
 * 
 * @author smart
 *
 */
public class BaseJdbcTemplate extends JdbcTemplate {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());


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
	private <T extends Entity> DataKeyValue<String, Object> getDbKeyValue(Class<T> clz,Map<String,Object> values) {
		EntityInfo info = this.getEntityInfo(clz);
		DataKeyValue<String, Object> result = new DataKeyValue<>();
//		Map<String, Object> values = null;
//		try {
//			values = entity.rowValue();
//		} catch (IllegalArgumentException | IllegalAccessException e) {
//			logger.error("{}", e);
//			return result;
//		}
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()){
			String fieldName = it.next();
			String key = info.feildNameMapping.get(fieldName);
			if (key == null) {
				LOGGER.error("clz {} field:{} not found!",clz.getName(), fieldName);
				it.remove();
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
		return  _update(entity.getClass(),map);
	}

	public <T extends Entity> int update(Class<T> clz, Map<String, Object> entityValues){
		DataKeyValue<String, Object> map = getDbKeyValue(clz,entityValues);

		return _update(clz,map);
	}

	private <T extends Entity> int _update(Class<T> clz, DataKeyValue<String, Object> map){
		EntityInfo info = this.getEntityInfo(clz);
		String sql = info.getUpdateSql(map.getKeys());
		List<Object> values = new ArrayList<>(map.getValues());
		return super.update(sql, values.toArray());
	}

	/**
	 * 更新批量
	 * 
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
		if (entity instanceof MultiEntity<?>) {
			MultiEntity<?> multiEntity = (MultiEntity<?>) entity;
			tmp = new String[info.pkName.length + 1];
			ArrayList<String> list = Lists.newArrayList(info.pkName);
			list.add(info.fkName);
			list.toArray(tmp);
			sql = info.getDeleteSql(tmp);
			List<Object> valueList = Lists.newArrayList(entity.findPkId().getIdentifies());
			valueList.add(multiEntity.findFkId());
			return super.update(sql, valueList.toArray());
		} else {
			sql = info.getDeleteSql(info.pkName);
			return super.update(sql, entity.findPkId().getIdentifies());
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
	public <T extends Entity> T get(Class<T> clazz, IdentifyKey pk) {

		EntityInfo info = this.getEntityInfo(clazz);
		if (info.pkName.length != pk.getIdentifies().length) {
			throw new RuntimeException("pk number error");
		}
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		for (int i = 0; i < info.pkName.length; i++) {
			String pkName = info.pkName[i];
			Object value = pk.getIdentifies()[i];
			map.put(pkName, value);
		}

		return getFirst(clazz, map);
	}

	/**
	 * 获取首行记录
	 * 
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
	 * @param condition
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
}
