package org.smart.framework.datacenter;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.smart.framework.datacenter.annotation.Column;
import org.smart.framework.util.IdentiyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.jdbc.core.RowMapper;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 一对一数据库实体
 * 
 * @author smart
 */
public abstract class Entity implements RowMapper<Entity> {
	protected Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * 获取主键值
	 * 
	 * @return
	 */
	public abstract IdentiyKey findPkId();

	/**
	 * 设置主键值
	 * 
	 * @param id
	 */
	public abstract void setPkId(IdentiyKey pk);

	/**
	 * 空字符串常量
	 */
	protected static final String EMPTY_STRING = "";

	/**
	 * 是否是新实体
	 */
	@JSONField(serialize = false)
	private boolean newEntity = false;
	@JSONField(serialize = false)
	private Lock lock = new ReentrantLock();

	@Override
	public Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
		Entity entity = null;
		try {
			entity = readData(rs, rowNum);
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			LOGGER.error("{}", e);
			return null;
		}
		entity.hasReadEvent(); // 读取后执行该方法做一些初始化
		entity.disposeBlob();
		return entity;
	}

	public Map<String, Object> rowValue() throws IllegalArgumentException, IllegalAccessException {
		this.beforeWritingEvent();
		Map<String, Object> list = writeData();
		disposeBlob();
		return list;
	}

	/**
	 * 从db读取每一行记录
	 * 
	 * @param rs
	 * @param rowNum
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	protected Entity readData(ResultSet rs, int rowNum) throws SQLException, InstantiationException,
			IllegalAccessException, NoSuchFieldException, SecurityException {
		Entity entity = EntityInfo.ENTITY_CONSTRUCT_ACCESS.get(this.getClass()).newInstance();
		EntityInfo entityInfo = EntityInfo.ENTITY_INFOS.get(this.getClass());
		Map<String, String> fieldMapping = entityInfo.feildNameMapping;
		for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
			String fieldName = entry.getKey();
			String dbColName = entry.getValue().replaceAll("`", "");
			Field field = this.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			// System.out.println(field.getType().getSimpleName());
			field.set(entity, getColumeValue(field.getType(), rs, dbColName));
		}
		return entity;
	}

	private Object getColumeValue(Class<?> clz, ResultSet rs, String colName) throws SQLException {
		Object result = null;
		switch (clz.getSimpleName()) {
		case "Integer":
		case "int":
			result = rs.getInt(colName);
			break;
		case "Long":
		case "long":
			result = rs.getLong(colName);
			break;
		case "Boolean":
		case "boolean":
			result = rs.getBoolean(colName);
			break;
		case "Short":
		case "short":
			result = rs.getShort(colName);
			break;
		case "Byte":
		case "byte":
			result = rs.getByte(colName);
			break;

		case "String":
			result = rs.getString(colName);
			break;

		default:
			result = rs.getObject(colName);
			break;
		}

		return result;
	}

	/**
	 * 数据读取后的事件处理(可以处理blob字符串转对象)
	 */
	protected void hasReadEvent() {};

	/**
	 * 获取所有字段的值
	 * 
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	protected Map<String, Object> writeData()
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> list = new HashMap<>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (null != column) {

				if (column.pk()) {
					field.setAccessible(true);
					list.put(field.getName(), field.get(this));
				}
			}
		}

		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (null != column) {

				if (!column.pk()) {
					field.setAccessible(true);
					list.put(field.getName(), field.get(this));
				}
			}
		}
		return list;
	}

	/**
	 * 数据保存进db之前的事件(可以处理对象转blob字符串)
	 */
	protected void beforeWritingEvent() {};

	/**
	 * 清理blob调用
	 */
	protected void disposeBlob() {};

	@JSONField(serialize = false)
	public boolean newEntity() {
		return newEntity;
	}

	public void setNewEntity(boolean newEntity) {
		this.newEntity = newEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((findPkId() == null) ? 0 : findPkId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Entity other = (Entity) obj;
		if (findPkId() == null) {
			if (other.findPkId() != null) {
				return false;
			}
		} else if (!findPkId().equals(other.findPkId())) {
			return false;
		}
		return true;
	}

	public Entity depthClone() {

		Entity target = EntityInfo.ENTITY_CONSTRUCT_ACCESS.get(this.getClass()).newInstance();
		BeanCopier beanCopier = EntityInfo.ENTITY_BEANCOPIER.get(this.getClass());
		beanCopier.copy(this, target, null);
		disposeBlob();
		return target;
	}
}
