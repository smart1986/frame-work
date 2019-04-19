package org.smart.framework.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
/**
 * 序列化反序列化工具
 * @author 卢德迪
 *
 */
public class ProtostuffUtil {
	public static <T> byte[] serializer(T t) {
		@SuppressWarnings("unchecked")
		Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(t.getClass());
		return ProtostuffIOUtil.toByteArray(t, schema , LinkedBuffer.allocate());
    }

    public static <T> T deserializer(byte[] bytes, Class<T> clazz) {

        T obj = null;
        try {
            obj = clazz.newInstance();
            @SuppressWarnings("unchecked")
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(obj.getClass());
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
