package org.smart.framework.net.helper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

public class BufferFactory {
	private static ByteBufAllocator bufAllocator = UnpooledByteBufAllocator.DEFAULT;
	
	/**
	 * 获取一个buffer
	 * @param bytes
	 * @return
	 */
	public static ByteBuf getIoBuffer(byte[] bytes) {
		ByteBuf buffer = bufAllocator.ioBuffer();
		buffer.writeBytes(bytes);
		return buffer;
	}
	public static ByteBuf getIoBuffer(int capacity) {
		ByteBuf buffer =  bufAllocator.buffer(capacity);
		return buffer;
	}
	/**
	 * 获取一个buffer
	 * @param autoExpand
	 * @return
	 */
	public static ByteBuf getIoBuffer() {
		ByteBuf buffer = bufAllocator.buffer();
		return buffer;
	}
	
}
