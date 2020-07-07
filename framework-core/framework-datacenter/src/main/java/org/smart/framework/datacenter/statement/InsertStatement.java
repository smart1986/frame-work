package org.smart.framework.datacenter.statement;

/**
 * 插入sql语句实现
 * @author smart
 *
 */
public class InsertStatement extends Statement {
	protected static final String PARENTHESES_LEFT = " ( ";
	protected static final String PARENTHESES_RIGHT = " ) ";
	
	public InsertStatement() {
		super();
	}
	
	/**
	 * 获取占位符(?,?,)
	 * @return
	 */
	private String getPlaceHolderWithComma(String[] allDbColumName){
		StringBuffer sql = new StringBuffer();
		String[] fields =  allDbColumName;
		for (int i = 0; i < fields.length; i++) {
			sql.append(PLACEHOLDER);
			if (i !=(fields.length - 1)){
				sql.append(COMMA);
			}
		}
		return sql.toString();
	}

	public String toSqlString(String tableName, String[] allDbColumName) {
		StringBuffer sql = new StringBuffer();
		sql.append(INSERT_INTO);
		sql.append(tableName);
		sql.append(PARENTHESES_LEFT);
		sql.append(getColumStrWithComma(allDbColumName));
		sql.append(PARENTHESES_RIGHT);
		sql.append(VALUES);
		sql.append(PARENTHESES_LEFT);
		sql.append(getPlaceHolderWithComma(allDbColumName));
		sql.append(PARENTHESES_RIGHT);
		return sql.toString();
	}
	
}
