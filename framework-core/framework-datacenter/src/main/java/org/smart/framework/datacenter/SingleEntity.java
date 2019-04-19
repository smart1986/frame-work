package org.smart.framework.datacenter;

import java.util.Collections;
import java.util.List;

import org.smart.framework.util.IdentiyKey;

public abstract class SingleEntity extends Entity {

	public List<IdentiyKey> keyLists(){
		return Collections.emptyList();
	}

}
