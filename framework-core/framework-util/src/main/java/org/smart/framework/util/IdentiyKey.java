package org.smart.framework.util;

import java.security.InvalidParameterException;
import java.util.Arrays;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 多值主键
 * 
 * @author smart
 *
 */
public class IdentiyKey {
	private Object[] identifys;

	public IdentiyKey() {
	}

	public IdentiyKey(Object... pk) {
		for (Object object : pk) {
			if (object == null) {
				throw new InvalidParameterException();
			}
		}
		identifys = pk;
	}

	public void setIdentifys(Object[] identifys) {
		this.identifys = identifys;
	}
	public Object[] getIdentifys() {
		return identifys;
	}

	@SuppressWarnings("unchecked")
	public <PK extends Object> PK getIdentifys(int index, Class<? extends Object> PK) {
		return (PK) identifys[index];
	}

	public static IdentiyKey build(Object... pk) {
		IdentiyKey identiyKey = new IdentiyKey(pk);
		return identiyKey;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (Object object : identifys) {
			sb.append(object.toString()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}

	@JSONField(serialize = false)
	public Long getFirstLongId() {
		if (identifys == null || identifys.length == 0) {
			return 0L;
		}
		return getIdentifys(0, Long.class);
	}

	@JSONField(serialize = false)
	public Integer getFirstIntId() {
		if (identifys == null || identifys.length == 0) {
			return 0;
		}
		return getIdentifys(0, Integer.class);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(identifys);
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
		IdentiyKey other = (IdentiyKey) obj;
		if (!Arrays.equals(identifys, other.identifys)) {
			return false;
		}
		for (int i = 0; i < identifys.length; i++) {
			if (!identifys[i].equals(other.identifys[i])) {
				return false;
			}
		}
		return true;
	}

}
