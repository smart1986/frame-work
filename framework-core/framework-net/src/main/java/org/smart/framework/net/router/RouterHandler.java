package org.smart.framework.net.router;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.smart.framework.net.router.annotation.Cmd;
import org.smart.framework.net.router.annotation.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * 模块处理句柄接口
 * 
 * @author ludd
 * 
 */
public class RouterHandler {
	protected Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private Object hander;
	
	/**
	 * 方法映射列表
	 */
	private Map<Integer, Method> METHOD_MAPS = new HashMap<>();

	/**
	 * 命令映射列表
	 */
	private Map<Integer, Cmd> CMD_MAPS = new HashMap<>();
	private Map<Integer, Object> CMD_PARSE_MAPS = new HashMap<>();
	
	private MethodAccess methodAccess;
	public RouterHandler(Object hander) {
		init(hander);
		methodAccess = MethodAccess.get(hander.getClass());
		
	}
	
	/**
	 * 获取模块
	 * @param cmd
	 * @return
	 */
	public Method getMethod(int cmd) {
		return METHOD_MAPS.get(cmd);
	}

	/**
	 * 获取命令注解
	 * @param cmd
	 * @return
	 */
	public Cmd getCmd(int cmd) {
		return CMD_MAPS.get(cmd);
	}
	public Object getParser(int cmd) {
		return CMD_PARSE_MAPS.get(cmd);
	}
	
	public Object getHander() {
		return hander;
	}
	
	public MethodAccess getMethodAccess() {
		return methodAccess;
	}
	
	private void init(Object object) {
		this.hander = object;
		Method[] mList = object.getClass().getDeclaredMethods();
		Set<String> methodNames = new HashSet<>();
		for (Method m : mList) {
			Cmd c = m.getAnnotation(Cmd.class);
			if (c != null) {
				if (methodNames.contains(m.getName())) {
					throw new RuntimeException(String.format("cmd annontation:[%d] duplicated methodName:[%s], handler:[%s]", c.id(),m.getName(),this.hander.getClass().getName()));
				}
				if (METHOD_MAPS.containsKey(c.id())) {
					throw new RuntimeException(String.format("cmd annontation:[%d] duplicated key, handler:[%s]", c.id(),this.hander.getClass().getName()));
				}
				methodNames.add(m.getName());
				METHOD_MAPS.put(c.id(), m);
				CMD_MAPS.put(c.id(), c);
				Module module = this.hander.getClass().getAnnotation(Module.class);
				Class<?> clazz = c.requstClass();
				if (!clazz.equals(Object.class)) {
					try {
						 Method  mm = clazz.getMethod("parser");
						 CMD_PARSE_MAPS.put(c.id(), mm.invoke(null));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
	        	}
				LOGGER.debug("add cmd success! module:{}  cmd:{}  methodname:{}", module.id(), c.id(), m.getName());
			}
		}
	}
	
//	public void sessionWrite(final ChannelHandlerContext context, final Response response, ChannelFutureListener listener) {
//		ChannelFuture f = context.writeAndFlush(response);
//		f.addListener(listener);
//		ChannelFutureListener future = channelFutureListener(context, response);
//		if (future != null) {
//			f.addListener(future);
//		}
//	}
//	public void sessionWrite(final ChannelHandlerContext context, final Response response) {
//		ChannelFuture f = context.writeAndFlush(response);
//		f.addListener(new ChannelFutureListener() {
//			
//			@Override
//			public void operationComplete(ChannelFuture future) throws Exception {
//				LOGGER.debug("message send, module:{},cmd:{},length:{}",response.getModule(), response.getCmd(),response.getValue().length);
//				
//			}
//		});
//		ChannelFutureListener future = channelFutureListener(context, response);
//		if (future != null) {
//			f.addListener(future);
//		}
//	}
//	
//	protected ChannelFutureListener channelFutureListener(ChannelHandlerContext ctx, Response response){
//		return null;
//	} 	
//
//	/**
//	 * write
//	 * @param session
//	 * @param response
//	 * @param packet
//	 */
//	public void sessionWrite(ChannelHandlerContext context, Response response, ByteBufSerializer buffer) {
//		response.setValue(buffer.getBytes());
//		sessionWrite(context, response);
//	}
//	public void sessionWrite(ChannelHandlerContext context, Response response, byte[] bytes) {
//		response.setValue(bytes);
//		sessionWrite(context, response);
//	}
	

}