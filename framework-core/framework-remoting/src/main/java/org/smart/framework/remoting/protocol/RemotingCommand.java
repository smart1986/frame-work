package org.smart.framework.remoting.protocol;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class RemotingCommand {
	protected static AtomicInteger requestId = new AtomicInteger(0);
	private int cmdIndex;
	private short code;
	private CommandType type;
	private int forward;
	private byte[] body;

	private RemotingCommand() {
	}

	public static RemotingCommand decode(final ByteBuffer byteBuffer) {
		int length = byteBuffer.limit();
		int oriHeaderLen = byteBuffer.getInt();
		int headerLength = getHeaderLength(oriHeaderLen);

		byte[] headerData = new byte[headerLength];
		byteBuffer.get(headerData);

		RemotingCommand cmd = headerDecode(headerData);

		int bodyLength = length - 4 - headerLength;
		byte[] bodyData = null;
		if (bodyLength > 0) {
			bodyData = new byte[bodyLength];
			byteBuffer.get(bodyData);
		}
		cmd.body = bodyData;

		return cmd;
	}

	public static int getHeaderLength(int length) {
		return length & 0xFFFFFF;
	}

	public static AtomicInteger getRequestId() {
		return requestId;
	}

	public static RemotingCommand createRequestCommand(short code) {
		RemotingCommand cmd = new RemotingCommand();
		cmd.setType(CommandType.REQUEST);
		cmd.setCode(code);
		cmd.setCmdIndex(requestId.getAndIncrement());
		return cmd;
	}

	public static RemotingCommand createRequestCommand() {
		RemotingCommand cmd = new RemotingCommand();
		cmd.setType(CommandType.REQUEST);
		cmd.setCode(RequestCode.SEND_MESSAGE);
		cmd.setCmdIndex(requestId.getAndIncrement());
		return cmd;
	}
	public static RemotingCommand createRequestForwardCommand(int target) {
		RemotingCommand cmd = new RemotingCommand();
		cmd.setType(CommandType.REQUEST);
		cmd.setCode(RequestCode.FORWARD);
		cmd.setCmdIndex(requestId.getAndIncrement());
		cmd.setForward(target);
		return cmd;
	}

	public static RemotingCommand createResponseCommand(int index, short code) {
		RemotingCommand cmd = new RemotingCommand();
		cmd.setType(CommandType.RESPONSE);
		cmd.setCode(code);
		cmd.setCmdIndex(index);
		return cmd;
	}

	public static RemotingCommand createResponseCommand(int index) {
		RemotingCommand cmd = new RemotingCommand();
		cmd.setType(CommandType.RESPONSE);
		cmd.setCode(RemotingSysResponseCode.SUCCESS);
		cmd.setCmdIndex(index);
		return cmd;
	}

	private static RemotingCommand headerDecode(byte[] headerData) {
		ByteBuffer buffer = ByteBuffer.wrap(headerData);
		RemotingCommand cmd = new RemotingCommand();
		cmd.setCmdIndex(buffer.getInt());
		cmd.setCode(buffer.getShort());
		cmd.setType(CommandType.values()[buffer.get()]);
		cmd.forward = buffer.getInt();
		return cmd;
	}

	private ByteBuffer encodeHeader() {
		ByteBuffer buffer = ByteBuffer.allocate(11);
		buffer.putInt(cmdIndex);
		buffer.putShort(code);
		buffer.put((byte) this.type.ordinal());
		buffer.putInt(this.forward);
		buffer.flip();
		return buffer;
	}

	public ByteBuffer encode() {
		// 1> header length size
		int length = 4;

		// 2> header data length
		byte[] headerData = encodeHeader().array();
		length += headerData.length;

		// 3> body data length
		if (this.body != null) {
			length += body.length;
		}

		ByteBuffer result = ByteBuffer.allocate(4 + length);

		// length
		result.putInt(length);

		// header length
		result.putInt(headerData.length);

		// header data
		result.put(headerData);

		// body data;
		if (this.body != null) {
			result.put(this.body);
		}

		result.flip();

		return result;
	}

	public int getCmdIndex() {
		return cmdIndex;
	}

	public void setCmdIndex(int cmdIndex) {
		this.cmdIndex = cmdIndex;
	}

	public short getCode() {
		return code;
	}

	public void setCode(short code) {
		this.code = code;
	}

	public CommandType getType() {
		return type;
	}

	public void setType(CommandType type) {
		this.type = type;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "RemotingCommand [code=" + code + ", cmdIndex=" + this.cmdIndex + "]";
	}

	public int getForward() {
		return forward;
	}
	public void setForward(int forward) {
		this.forward = forward;
	}

}
