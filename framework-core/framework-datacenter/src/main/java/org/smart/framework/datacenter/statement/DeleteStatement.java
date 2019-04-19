package org.smart.framework.datacenter.statement;


/**
 * 删除sql语句实现
 * @author ludd
 *
 * @param <T>
 */
public class DeleteStatement extends Statement {
	
	public DeleteStatement() {
		super();
	}
	
	
	/**
	 * 获取条件语句（xxx = ?)
	 * @return
	 */
	private String getConditonWithPlaceHolder(String[] key) {
		StringBuffer sql = new StringBuffer();
		for (int i = 0; i < key.length; i++) {
			String c = key[i];
			sql.append(c);
			sql.append(EQUATE);
			sql.append(PLACEHOLDER);
			if (i != (key.length - 1)) {
				sql.append(AND);
			}
		}
		return sql.toString();
	}

	public String toSqlString(String[] pkName, String tableName, String[] key) {
		StringBuffer sql = new StringBuffer();
		sql.append(DELETE);
		sql.append(FROM);
		sql.append(tableName);
		if (key == null || key.length <= 0){
			key = pkName;
		}
		sql.append(WHERE);
		sql.append(getConditonWithPlaceHolder(key));
		
		return sql.toString();
	}
	
}
