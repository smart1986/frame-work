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
public class IdentifyKey {
	private Object[] identifies;

	public IdentifyKey() {
	}

	public IdentifyKey(Object... pk) {
		for (Object object : pk) {
			if (object == null) {
				throw new InvalidParameterException();
			}
		}
		identifies = pk;
	}

	public void setIdentifies(Object[] identifies) {
		this.identifies = identifies;
	}
	public Object[] getIdentifies() {
		return identifies;
	}

	@SuppressWarnings("unchecked")
	public <PK extends Object> PK getIdentifys(int index, Class<? extends Object> PK) {
		return (PK) identifies[index];
	}

	public static IdentifyKey build(Object... pk) {
		IdentifyKey identiyKey = new IdentifyKey(pk);
		return identiyKey;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (Object object : identifies) {
			sb.append(object.toString()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}

	@JSONField(serialize = false)
	public Long getFirstLongId() {
		if (identifies == null || identifies.length == 0) {
			return 0L;
		}
		return getIdentifys(0, Long.class);
	}

	@JSONField(serialize = false)
	public Integer getFirstIntId() {
		if (identifies == null || identifies.length == 0) {
			return 0;
		}
		return getIdentifys(0, Integer.class);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(identifies);
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
		IdentifyKey other = (IdentifyKey) obj;
		if (!Arrays.equals(identifies, other.identifies)) {
			return false;
		}
		for (int i = 0; i < identifies.length; i++) {
			if (!identifies[i].equals(other.identifies[i])) {
				return false;
			}
		}
		return true;
	}

}
