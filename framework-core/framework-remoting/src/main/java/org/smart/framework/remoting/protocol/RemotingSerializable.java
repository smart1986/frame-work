package org.smart.framework.remoting.protocol;

public interface RemotingSerializable<T> {
	byte[] encode();
	T decode(byte[] bytes);
}
