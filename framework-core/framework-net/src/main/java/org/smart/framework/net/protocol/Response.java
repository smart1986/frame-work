package org.smart.framework.net.protocol;


public class Response extends DataPacket {
	public Response(int module, int cmd, int args) {
		super((byte)module, (byte)cmd,args, null);
	}
	public Response(byte module, byte cmd, int args) {
		super(module, cmd,args, null);
	}

	private short statusCode = 0;
	
	public short getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(short statusCode) {
		this.statusCode = statusCode;
	}
	
	public static Response valueOf(byte module, byte cmd, byte[] value) {
		Response response = new Response(module, cmd, 0);
		response.setValue(value);
		return response;
	}
	
	public static Response valueOf(byte module, byte cmd,int args) {
		Response response = new Response(module, cmd, args);
		return response;
	}

	public static Response valueOf(byte module, byte cmd,int args, short status) {
		Response response = valueOf(module, cmd, args);
		response.setStatusCode(status);
		return response;
	}
	public static Response valueOf(byte module, byte cmd, int args, byte[] value) {
		Response response = valueOf(module, cmd, args);
		response.setValue(value);
		return response;
	}
	
	@Override
	public String toString() {
		
		return String.format("module:[%s], cmd:[%s], statusCode[%s],valueLen:[%s]", this.getModule(), this.getCmd(),this.getStatusCode(),this.getValue() == null ? 0 : this.getValue().length);
	}
}
