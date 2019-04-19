package org.smart.framework.remoting.protocol;

import java.nio.ByteBuffer;

public class Response extends BaseDataPacket {
	public short statusCode;


	public Response() {
		super();
	}


	public Response(byte module, byte cmd, byte[] value) {
		super(module, cmd, value);
	}


	@Override
	public Response decode(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(data);
		module = bb.get();
		cmd = bb.get();
		statusCode = bb.getShort();
		this.value = new byte[bb.capacity() - bb.position()];
		bb.get(this.value);
		return this;
	}
	
	@Override
	public byte[] encode() {
		int len = 1+1 +2 + (this.value == null?0:this.value.length);
		ByteBuffer bb = ByteBuffer.allocate(len);
		bb.put(this.module);
		bb.put(this.cmd);
		bb.putShort(this.statusCode);
		if (this.value != null) {
			bb.put(this.value);
		}
		bb.flip();
		return bb.array();
	}
	
	
	public short getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(short statusCode) {
		this.statusCode = statusCode;
	}
	
	public static Response valueOf(byte module, byte cmd, byte[] value) {
		Response response = new Response(module, cmd,value);
		return response;
	}
	

	
	@Override
	public String toString() {
		
		return String.format("module:[%s], cmd:[%s], statusCode[%s],valueLen:[%s]", this.getModule(), this.getCmd(),this.getStatusCode(),this.getValue() == null ? 0 : this.getValue().length);
	}
}
