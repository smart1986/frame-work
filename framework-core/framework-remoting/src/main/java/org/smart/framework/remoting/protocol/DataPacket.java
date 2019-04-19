package org.smart.framework.remoting.protocol;

public interface DataPacket<T> extends RemotingSerializable<T>{
//	public DataPacket decode(byte[] data);
	public byte[] encode();
	public byte getModule();
	public byte getCmd();
	public void setModule(byte module);
	public void setCmd(byte cmd);
	public byte[] getValue();
	public void setValue(byte[] value);
}
