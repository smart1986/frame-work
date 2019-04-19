package org.smart.framework.remoting.protocol;

import java.nio.ByteBuffer;

public class BaseDataPacket implements DataPacket<BaseDataPacket> {
	public byte module;
	public byte cmd;
	public byte[] value;
	
	public BaseDataPacket() {
	}
	
	public BaseDataPacket(byte module, byte cmd, byte[] value) {
		super();
		this.module = module;
		this.cmd = cmd;
		this.value = value;
	}
	public BaseDataPacket(int module, int cmd, byte[] value) {
		this((byte)module,(byte)cmd,value);
	}
	
	@Override
	public byte getModule() {
		return module;
	}
	@Override
	public byte getCmd() {
		return cmd;
	}
	@Override
	public void setModule(byte module) {
		this.module = module;
		
	}
	@Override
	public void setCmd(byte cmd) {
		this.cmd = cmd;
		
	}
	@Override
	public byte[] getValue() {
		return this.value;
	}
	@Override
	public void setValue(byte[] value) {
		this.value = value;
		
	}
	@Override
	public BaseDataPacket decode(byte[] bytes) {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		module = bb.get();
		cmd = bb.get();
		int len = bb.capacity() - bb.position();
		if (len > 0) {
			this.value = new byte[len];
			bb.get(this.value);
		}
		return this;
	}

	@Override
	public byte[] encode() {
		int len = 1+1 + (this.value == null?0:this.value.length);
		ByteBuffer bb = ByteBuffer.allocate(len);
		bb.put(this.module);
		bb.put(this.cmd);
		if (this.value != null) {
			bb.put(this.value);
		}
		bb.flip();
		return bb.array();
	}

}
