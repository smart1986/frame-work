package org.smart.framework.datacenter.statement;



/**
 * 查询sql语句实现
 * @author smart
 *
 */
public class SelectStatement extends Statement {

	
	public SelectStatement() {
		super();
	}
	
	/**
	 * 获取列名（xxx,xxx,);
	 * @return
	 */
	private String getColumnNameWithComma(String[] allDbColumName){
		StringBuffer colBuff = new StringBuffer();
		for (int i = 0; i < allDbColumName.length; i++) {
			String f = allDbColumName[i];
			colBuff.append(f);
			if (i != (allDbColumName.length - 1)){
				colBuff.append(COMMA);
			}
		}
		return colBuff.toString();
	}
	
	/**
	 * 获取条件（xxx =?)
	 * @return
	 */
	private String getContionWithPlaceHolder(String[] keys){
		StringBuffer sql = new StringBuffer();
		if (keys != null && keys.length > 0){
			for (int i = 0; i < keys.length; i++) {
				StringBuffer tempKey = new StringBuffer();
				if (!keys[i].startsWith(MARK)) {
					tempKey.append(MARK);
				}
				tempKey.append(keys[i]);
				if (!keys[i].endsWith(MARK)) {
					tempKey.append(MARK);
				}
				sql.append(tempKey.toString());
				sql.append(EQUATE);
				sql.append(PLACEHOLDER);
				if (i != (keys.length - 1)){
					sql.append(AND);
				}
			}
		}
		return sql.toString();
	}
	
	public String toSqlString(String tableName, String[] allDbColumName){
		return toSqlString(tableName, allDbColumName,null);
	}
	public String toSqlString( String tableName, String[] allDbColumName,String[] keys){
		return toSqlString( tableName, allDbColumName, null, -1, -1, keys);
	}
	public String toSqlString(String tableName, String[] allDbColumName,String columnName, String[] keys){
		return toSqlString(tableName, allDbColumName, columnName, -1, -1, keys);
	}
	
	public String toSqlString(String tableName, String[] allDbColumName, String columnName, int limitBegin, int limitEnd, String[] keys) {
		String columnString = null;
		if (null != columnName) {
			columnString = columnName;
		} else {
			columnString = getColumnNameWithComma(allDbColumName);
		}
		
		StringBuffer sql = new StringBuffer();
		sql.append(SELECT);
		sql.append(columnString);
		sql.append(FROM);
		sql.append(tableName);
		if (keys != null && keys.length > 0){
			sql.append(WHERE);
			sql.append(getContionWithPlaceHolder(keys));
		}
		
		
		if (limitBegin > 0 && limitEnd > 0){
			sql.append(LIMIT);
			sql.append(PLACEHOLDER);
			sql.append(COMMA);
			sql.append(PLACEHOLDER);
		} else if (limitBegin == 0 && limitEnd > 0) {
			sql.append(LIMIT);
			sql.append(PLACEHOLDER);
		}
		
		return sql.toString();
	}

	public String toSqlString(String tableName, String[] allDbColumName, int limitBegin, int limitEnd, String[] condition) {
		return toSqlString(tableName, allDbColumName, null, limitBegin, limitEnd, condition);
	}
	

}
