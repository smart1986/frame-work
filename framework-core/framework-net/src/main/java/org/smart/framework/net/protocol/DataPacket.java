package org.smart.framework.net.protocol;


public class DataPacket {
	/**
	 * 模块id
	 */
	private byte module;

	/**
	 * 命令id
	 */
	private byte cmd;
	/**
	 * 透传参数
	 */
	private int args;

	/**
	 * 消息具体数据
	 */
	private byte[] value;
	
	private boolean crypt = false;
	
	

	public DataPacket(byte module, byte cmd,int args, byte[] value) {
		super();
		this.module = module;
		this.cmd = cmd;
		this.args = args;
		this.value = value;
	}

	public byte getModule() {
		return this.module;
	}

	public void setModule(byte module) {
		this.module = module;
	}

	public byte getCmd() {
		return this.cmd;
	}

	public void setCmd(byte cmd) {
		this.cmd = cmd;
	}

	public byte[] getValue() {
		return this.value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	
	public boolean isCrypt() {
		return crypt;
	}
	
	public void setCrypt(boolean crypt) {
		this.crypt = crypt;
	}
	
	
	
	public int getArgs() {
		return args;
	}

	public void setArgs(int args) {
		this.args = args;
	}

	@Override
	public String toString() {
		return String.format("module:[%s], cmd:[%s], valueLen:[%s], crypt:[%s]", this.module,this.cmd,this.value == null ? 0 : this.value.length, this.crypt);
	}
}
