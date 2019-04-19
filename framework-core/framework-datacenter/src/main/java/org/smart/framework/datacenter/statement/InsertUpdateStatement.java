package org.smart.framework.datacenter.statement;


/**
 * Replace into ON DUPLICATE KEY UPDATE sql语句实现
 * @author ludd
 *
 */
public class InsertUpdateStatement extends Statement {
	protected static final String PARENTHESES_LEFT = " ( ";
	protected static final String PARENTHESES_RIGHT = " ) ";
	
	public InsertUpdateStatement() {
		super();
	}
	
	/**
	 * 获取占位符(?,?,)
	 * @return
	 */
	private String getPlaceHolderWithComma(String[] allDbColumName){
		StringBuffer sql = new StringBuffer();
		String[] fields = allDbColumName;
		for (int i = 0; i < fields.length; i++) {
			sql.append(PLACEHOLDER);
			if (i !=(fields.length - 1)){
				sql.append(COMMA);
			}
		}
		return sql.toString();
	}

	/**
	 * 获取条件语句（xxx = ?)
	 * @return
	 */
	private String getUpdateWithPlaceHolder(String[] key) {
		StringBuffer sql = new StringBuffer();
		for (int i = 0; i < key.length; i++) {
			String c = key[i];
			sql.append(c);
			sql.append(EQUATE);
			sql.append(PLACEHOLDER);
			if (i != (key.length - 1)) {
				sql.append(COMMA);
			}
		}
		return sql.toString();
	}
	
	public String toSqlString( String tableName,String[] columnName,String[] updateColumnName) {
		StringBuffer sql = new StringBuffer();
		sql.append(INSERT_INTO);
		sql.append(tableName);
		sql.append(PARENTHESES_LEFT);
		sql.append(getColumStrWithComma(columnName));
		sql.append(PARENTHESES_RIGHT);
		sql.append(VALUES);
		sql.append(PARENTHESES_LEFT);
		sql.append(getPlaceHolderWithComma(columnName));
		sql.append(PARENTHESES_RIGHT);
		sql.append(ON_DUPLICATE_KEY);
		sql.append(UPDATE);
		sql.append(getUpdateWithPlaceHolder(updateColumnName));
		return sql.toString();
	}
}
