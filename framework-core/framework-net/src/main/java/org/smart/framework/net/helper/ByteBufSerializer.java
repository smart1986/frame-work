package org.smart.framework.net.helper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ByteBufSerializer {
	
	 private static final InternalLogger logger = InternalLoggerFactory.getInstance(ByteBufSerializer.class);
	
	public static final Charset CHARSET = Charset.forName("UTF-8");
	/**
	 * 数字类型读取错误时默认返回值
	 */
	protected static byte DEFAULT_NUMBER_VALUE = 0;
	
	/**
	 * 字符类型读取错误时默认返回值
	 */
	protected static String DEFAULT_STRING_VALUE = "";
	
	protected static int BYTE_LEN = 1;
	protected static int SHORT_LEN = 2;
	protected static int INTEGER_LEN = 4;
	protected static int LONG_LEN = 8;
	protected static int FLOAT_LEN = 4;	
	protected static int DOUBLE_LEN = 8;
	
	protected ByteBuf writeBuffer;
	
	protected ByteBuf readBuffer;
	
	public ByteBufSerializer() {
	}
	
	/**
	 * 默认为读取方式
	 * @param bytes	读取的数组
	 */
	public ByteBufSerializer(byte[] bytes) {
		readBuffer = BufferFactory.getIoBuffer(bytes);
		read();
		readBuffer.clear();
		readBuffer.release();
	}


	/**
	 * 设置写buffer
	 * @param bytes	读取的数组
	 */
	protected void setReadBuffer(byte[] bytes) {
//		readBuffer = IoBuffer.wrap(bytes);
		readBuffer = BufferFactory.getIoBuffer(bytes);
		read();
		readBuffer.release();
	}
	
	/**
	 * byte数组方式读取数据流，子类要调用有参构造
	 * @param bytes
	 */
	protected void read() {
		throw new RuntimeException("override the method please");
	}
	
	/**
	 * iobuffer方式读取数值流
	 * @param buffer
	 */
	public void readBuffer(ByteBufSerializer buffer) {
		throw new RuntimeException("override the method please");
	}
	
	/**
	 * 用于
	 * @throws Exception 
	 */
	public void write() {
		
		throw new RuntimeException("override the method please");
		
	}
	
	public byte readByte() {
		if (calcCapacity() >= BYTE_LEN) {
			return readBuffer.readByte();
		}
		return DEFAULT_NUMBER_VALUE;
	}

	public short readShort() {
		if (calcCapacity() >= SHORT_LEN) {
			return readBuffer.readShort();
		}
		return DEFAULT_NUMBER_VALUE;
	}

	public int readInt() {
		if (calcCapacity() >= INTEGER_LEN) {
			return readBuffer.readInt();
		}
		return DEFAULT_NUMBER_VALUE;
	}

	public long readLong() {
		if (calcCapacity() >= LONG_LEN) {
			return readBuffer.readLong();
		}
		return DEFAULT_NUMBER_VALUE;
	}

	public float readFloat() {
		if (calcCapacity() >= FLOAT_LEN) {
			return readBuffer.readFloat();
		}
		return DEFAULT_NUMBER_VALUE;
	}

	public double readDouble() {
		if (calcCapacity() >= DOUBLE_LEN) {
			return readBuffer.readDouble();
		}
		return DEFAULT_NUMBER_VALUE;
	}

	public String readString() {
		if (calcCapacity() < SHORT_LEN) {
			return DEFAULT_STRING_VALUE;
		}

		int size = readBuffer.readShort();
		if (calcCapacity() < size) {
			return DEFAULT_STRING_VALUE;
		}

		byte[] bytes = new byte[size];
		readBuffer.readBytes(bytes);

		return new String(bytes, CHARSET);
	}

	public String readBigString() {
		if (calcCapacity() < INTEGER_LEN) {
			return DEFAULT_STRING_VALUE;
		}

		int size = readBuffer.readInt();
		if (calcCapacity() < size) {
			return DEFAULT_STRING_VALUE;
		}

		byte[] bytes = new byte[size];
		readBuffer.readBytes(bytes);

		return new String(bytes, CHARSET);
	}
	
	/**
	 * 读取一个byte数组(带长度)
	 * @return
	 */
	public byte[] readByteArray() {
		int len = readInt();
		byte[] bytes = new byte[len];
		readBuffer.readBytes(bytes);
		return bytes;
	}
	
	
	public <T> List<T> readList(Class<T> clz) {
		List<T> list = new ArrayList<>();
		if (calcCapacity() > SHORT_LEN) {
			int size = readBuffer.readShort();
			for (int i = 0; i < size; i++) {
				list.add(read(clz));
			}
		}
		return list;
	}
	
	public <K,V> Map<K,V> readMap(Class<K> keyClz, Class<V> valueClz) {
		Map<K,V> map = new HashMap<>();
		if (calcCapacity() > SHORT_LEN) {
			int size = readBuffer.readShort();
			for (int i = 0; i < size; i++) {
				K key = read(keyClz);
				V value = read(valueClz);
				map.put(key, value);
				
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T read(Class<T> clz) {
		Object t = null;
		if ( clz == int.class || clz == Integer.class) {
			t = this.readInt();
		} else if (clz == byte.class || clz == Byte.class){
			t = this.readByte();
		} else if (clz == short.class || clz == Short.class){
			t = this.readShort();
		} else if (clz == long.class || clz == Long.class){
			t = this.readLong();
		} else if (clz == float.class || clz == Float.class){
			t = readFloat();
		} else if (clz == double.class || clz == Double.class){
			t = readDouble();
		} else if (clz == String.class ){
			t = readString();
		} else if (ByteBufSerializer.class.isAssignableFrom(clz)){
			try {
				ByteBufSerializer temp = (ByteBufSerializer)clz.newInstance();
				temp.readBuffer(this);
				t = temp;
			} catch (Exception e) {
				logger.error("{}", e);
			} 
			
		} else {
			throw new RuntimeException(String.format("不支持类型:[%s]", clz));
		}
		return (T) t;
	}


	/**
	 * 计算ByteBuffer的容量
	 * 
	 * @return
	 */
	protected int calcCapacity() {
		if (readBuffer == null) {
			return 0;
		}
		return readBuffer.capacity();
	}

	public ByteBufSerializer writeByte(Byte value) {
		writeBuffer.writeByte(value);
		return this;
	}

	public ByteBufSerializer writeShort(Short value) {
		writeBuffer.writeShort(value);
		return this;
	}

	public ByteBufSerializer writeInt(Integer value) {
		writeBuffer.writeInt(value);
		return this;
	}

	public ByteBufSerializer writeLong(Long value) {
		writeBuffer.writeLong(value);
		return this;
	}

	public ByteBufSerializer writeFloat(Float value) {
		writeBuffer.writeFloat(value);
		return this;
	}

	public ByteBufSerializer writeDouble(Double value) {
		writeBuffer.writeDouble(value);
		return this;
	}

	public ByteBufSerializer writeLongList(List<Long> list) {
		if (isEmpty(list)) {
			writeBuffer.writeShort((short) 0);
			return this;
		}
		writeBuffer.writeShort((short) list.size());
		for (long item : list) {
			writeBuffer.writeLong(item);
		}
		return this;
	}

	
	public <T> ByteBufSerializer writeCollection(Collection<T> collection) {
		if (isEmpty(collection)) {
			writeBuffer.writeShort((short) 0);
			return this;
		}
		writeBuffer.writeShort((short) collection.size());
		for (T item : collection) {
			writeObject(item);
		}
		return this;
	}

	public <K,V> ByteBufSerializer writeMap(Map<K, V> map) {
		if (isEmpty(map)) {
			writeBuffer.writeShort((short) 0);
			return this;
		}
		writeBuffer.writeShort((short) map.size());
		for (Entry<K, V> entry : map.entrySet()) {
			writeObject(entry.getKey());
			writeObject(entry.getValue());
		}
		return this;
	}




	public ByteBufSerializer writeString(String value) {
		if (value == null || value.isEmpty()) {
			writeShort((short) 0);
			return this;
		}

		byte data[] = value.getBytes(CHARSET);
		short len = (short) data.length;
		writeBuffer.writeShort(len);
		writeBuffer.writeBytes(data);
		return this;
	}

	public ByteBufSerializer writeBigString(String value) {
		if (value == null || value.isEmpty()) {
			writeInt(0);
			return this;
		}

		byte data[] = value.getBytes(CHARSET);
		int len = data.length;
		writeBuffer.writeInt(len);
		writeBuffer.writeBytes(data);
		return this;
	}

	public ByteBufSerializer writeBytes(byte value[]) {
		writeBuffer.writeBytes(value);
		return this;
	}
	/**
	 * 写入数组（前面带长度）
	 * @param value
	 */
	public ByteBufSerializer writeByteAarry(byte value[]) {
		writeBuffer.writeInt(value.length);
		writeBuffer.writeBytes(value);
		return this;
	}

	public ByteBufSerializer writeObject(Object object) {
		if (object instanceof Integer) {
			writeBuffer.writeInt((int) object);
			return this;
		}

		if (object instanceof Long) {
			writeBuffer.writeLong((long) object);
			return this;
		}

		if (object instanceof Short) {
			writeBuffer.writeShort((short) object);
			return this;
		}

		if (object instanceof Byte) {
			writeBuffer.writeByte((byte) object);
			return this;
		}

		if (object instanceof String) {
			String value = (String) object;
			writeString(value);
			return this;
		}
		if (object instanceof ByteBufSerializer) {
			ByteBufSerializer value = (ByteBufSerializer) object;
			writeBytes(value.getBytes());
			return this;
		}
		logger.error("不可序列化的类型:" + object.getClass());
		
		return this;
	}

	/**
	 * 返回buffer数组
	 * 
	 * @return
	 */
	public synchronized byte[] getBytes() {
		writeBuffer = BufferFactory.getIoBuffer();
		write();
		byte[] bytes = null;
		if (writeBuffer.writerIndex() == 0) {
			bytes = new byte[0];
		} else {
			bytes = new byte[writeBuffer.writerIndex()];
			writeBuffer.readBytes(bytes);
		}
		writeBuffer.clear();
		writeBuffer.release();

		return bytes;
	}
	
	private <T> boolean isEmpty(Collection<T> c) {
		return c == null || c.size() == 0;
	}
	public <K,V> boolean isEmpty(Map<K,V> c) {
		return c == null || c.size() == 0;
	}
	
	public static byte[] int2bytes(int value){
		ByteBuf writeBuffer = BufferFactory.getIoBuffer();
		writeBuffer.writeInt(value);
		byte[] bytes = new byte[writeBuffer.writerIndex()];
		writeBuffer.readBytes(bytes);
		writeBuffer.release();
		return bytes;
	}
}
