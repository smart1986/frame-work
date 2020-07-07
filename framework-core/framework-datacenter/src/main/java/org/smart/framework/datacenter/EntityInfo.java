package org.smart.framework.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.smart.framework.datacenter.annotation.DBQueueType;
import org.smart.framework.datacenter.statement.DeleteStatement;
import org.smart.framework.datacenter.statement.InsertStatement;
import org.smart.framework.datacenter.statement.InsertUpdateStatement;
import org.smart.framework.datacenter.statement.SelectStatement;
import org.smart.framework.datacenter.statement.UpdateStatement;
import org.springframework.cglib.beans.BeanCopier;

import com.esotericsoftware.reflectasm.ConstructorAccess;


/**
 * 实体信息类,用于记录反射后的一些常用信息
 * @author smart
 *
 */
public class EntityInfo {

	/**
	 * 类名
	 */
	public String className;
	
	/**
	 * 表名
	 */
	public String tableName;
	
	/**
	 * 表类型
	 */
	public DBQueueType tableType;
	
	/**
	 * 主键名
	 */
	public String[] pkName;
	
	/**
	 * 外键名
	 */
	public String fkName;
	
	/**
	 * 数据库字段名
	 */
	public String[] dbColumnNames;
	
	/**
	 * key:数据库字段名
	 * value：实体字段名
	 */
	public Map<String,String> columnNameMapping = new TreeMap<>();
	/**
	 * key:实体字段名
	 * value：数据库字段名
	 */
	public Map<String,String> feildNameMapping = new TreeMap<>();
	
	private static DeleteStatement deleteStatement = new DeleteStatement();
	private static SelectStatement selectStatement = new SelectStatement();
	private static InsertUpdateStatement insertUpdateStatement = new InsertUpdateStatement();
	private static UpdateStatement updateStatement = new UpdateStatement();
	private static InsertStatement insertStatement = new InsertStatement();
	
	public static Map<Class<?>, EntityInfo> ENTITY_INFOS = new HashMap<Class<?>, EntityInfo>();
	public static Map<Class<?>, BeanCopier> ENTITY_BEANCOPIER = new HashMap<Class<?>, BeanCopier>();
	public static Map<Class<?>, ConstructorAccess<? extends Entity>> ENTITY_CONSTRUCT_ACCESS = new HashMap<Class<?>, ConstructorAccess<? extends Entity>>();
	
	/**
	 * 实体实例对象(反射时的)
	 */
	public Entity entity;
	
	/**
	 * 无条件查询
	 * @return
	 */
	public String getSelectSql() {
		feildNameMapping.values().toArray();
		return selectStatement.toSqlString(tableName, feildNameMapping.values().toArray(new String[feildNameMapping.values().size()]));
	}
//	
	/**
	 * 条件查询
	 * @param condition 条件
	 * @return
	 */
	public String getSelectSql( String... condition) {
		return selectStatement.toSqlString(tableName, feildNameMapping.values().toArray(new String[feildNameMapping.values().size()]), condition);
	}
	
	/**
	 * 条件查询目标字段值
	 * @param columName 目标字段
	 * @param condition 条件字段
	 * @return
	 */
	public String getSelectSql(String columName, String[] condition) {
		return selectStatement.toSqlString( tableName, feildNameMapping.values().toArray(new String[feildNameMapping.values().size()]), columName, condition);
	}

	public String getSelectSql(int limitBegin, int limitEnd, String... condition) {
		return selectStatement.toSqlString( tableName, feildNameMapping.values().toArray(new String[feildNameMapping.values().size()]), limitBegin, limitEnd, condition);
	}

	public String getSelectSql(String targetColum, int limitBegin, int limitEnd, String[] condition) {
		return selectStatement.toSqlString( tableName, feildNameMapping.values().toArray(new String[feildNameMapping.values().size()]), targetColum, limitBegin, limitEnd, condition);
	}
	
	/**
	 * 条件更新
	 * @param condition 条件字段
	 * @return
	 */
	public String getInsertUpdateSql(List<String> sqlKeys) {
		String[] insert = sqlKeys.toArray(new String[sqlKeys.size()]);
		List<String> tmp = new ArrayList<>();
		tmp.addAll(sqlKeys);
		for (String pk : pkName) {
			tmp.remove(pk);
		}
		String[] update = tmp.toArray(new String[tmp.size()]);
		return insertUpdateStatement.toSqlString( tableName, insert, update);
	}
	public String getUpdateSql(List<String> sqlKeys) {
		return updateStatement.toSqlString( pkName,tableName, sqlKeys.toArray(new String[sqlKeys.size()]));
	}
	
	public String getInsertIntoSql(List<String> sqlKeys){
		return insertStatement.toSqlString(tableName, sqlKeys.toArray(new String[sqlKeys.size()]));
	}
	
	
	
	
	/**
	 * 条件删除
	 * @param condition 条件字段
	 * @return
	 */
	public String getDeleteSql(String... condition) {
		return deleteStatement.toSqlString(pkName, tableName, condition);
	}

	
}
