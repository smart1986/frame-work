package org.smart.framework.net.router;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.smart.framework.net.protocol.DataPacket;
import org.smart.framework.net.router.annotation.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

/**
 * 消息路由
 * @author jerry
 *
 */
public abstract class SocketRouter implements Router<DataPacket> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(SocketRouter.class);
	/**
	 * 模块id与模块句柄 关联列表
	 */
	protected Map<Byte, RouterHandler> MODULE_MAPS = new HashMap<Byte, RouterHandler>();
	
	/**
	 * 注册模块
	 * @param handler   接收handler
	 */
	@Override
	public void register(Object handler) {
		if (handler != null && handler instanceof RouterHandler) {
			RouterHandler routerHandler = (RouterHandler) handler;
			Module m = routerHandler.getHander().getClass().getAnnotation(Module.class);
			byte module = m.id();
			if (MODULE_MAPS.containsKey(module)) {
				throw new RuntimeException(String.format("module:[%d] duplicated key", module));
			}
			MODULE_MAPS.put(module, routerHandler);
		} else {
			throw new RuntimeException("regist error");
		}
	}
	
	/**
	 * 转发数据验证
	 * @param session
	 * @param dataPacket
	 * @return
	 */
	@Override
	public boolean forwardValidate(ChannelHandlerContext ctx, DataPacket dataPacket) {
		if (ctx.channel() == null || dataPacket == null) {
			return false;
		}

		byte module = dataPacket.getModule();
		if (!MODULE_MAPS.containsKey(module)) {
			LOGGER.warn("module:{} does not exist!", module);
			return false;
		}

		RouterHandler handler = MODULE_MAPS.get(module);
		byte cmd = dataPacket.getCmd();
		Method method = handler.getMethod(cmd);
		if (method == null) {
			LOGGER.warn("module:{},cmd:{} does not exist!",module, cmd);
			return false;
		}
		return true;
	}
	
	/**
	 * 路由转发
	 * @param ioSession  当前session
	 * @param msgPacket  消息包
	 */
	@Override
	public abstract Object forward(ChannelHandlerContext ctx, DataPacket dataPacket);

}