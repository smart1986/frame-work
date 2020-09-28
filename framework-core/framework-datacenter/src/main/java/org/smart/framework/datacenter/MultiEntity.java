package org.smart.framework.datacenter;
/**
 * 一对多数据库实体
 * @author smart
 *
 * @param <FK>
 */
public abstract class MultiEntity<FK> extends Entity {
	public abstract FK findFkId();
	public abstract void setFkId(FK fk);
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((findFkId() == null) ? 0 : findFkId().hashCode());
		result = prime * result + ((findPkId() == null) ? 0 : findPkId().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		MultiEntity<FK> other = (MultiEntity<FK>) obj;
		if (findFkId() == null) {
			if (other.findFkId() != null)
				return false;
		} else if (!findFkId().equals(other.findFkId()))
			return false;
		if (findPkId() == null) {
			if (other.findPkId() != null)
				return false;
		} else if (!findPkId().equals(other.findPkId()))
			return false;
		return true;
	}
	
	
	
	
}
