package org.smart.framework.net.router;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.smart.framework.net.router.annotation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by mx on 2017/6/2.
 */
public abstract class HttpRouter implements Router<FullHttpRequest> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(HttpRouter.class);
    protected Map<String,Method> mathodMap = new HashMap<>();
    protected Map<String,Object> handlerMap = new HashMap<>();
    protected Map<String,Boolean> checkMap = new HashMap<>();
    public void register(Object handler) {
        if (handler != null) {
        	Path objPath = handler.getClass().getAnnotation(Path.class);
            Method[] methods = handler.getClass().getMethods();
            for (Method m: methods ) {
            	Path c = m.getAnnotation(Path.class);
                if (c == null) {continue;}
                if (mathodMap.containsKey(c.value())) {
                    throw new RuntimeException(String.format("path:[%s] duplicated key", c.value()));
                }
                String path = "";
                if (objPath != null && objPath.value() != null) {
                	path += objPath.value();
                }
                
                path+=c.value();
                mathodMap.put(path, m);
                checkMap.put(path, c.check());
                handlerMap.put(path, handler);
                LOGGER.debug("http path {} regist", path);
            }

        }
    }

    /**
     * 路由转发
     */
    public abstract Object forward(ChannelHandlerContext ctx, FullHttpRequest request);

}
