package org.smart.framework.datacenter;

import java.util.Collections;
import java.util.List;

import org.smart.framework.util.IdentifyKey;

public abstract class SingleEntity extends Entity {

	public List<IdentifyKey> keyLists(){
		return Collections.emptyList();
	}

}
