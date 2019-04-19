package org.smart.framework.remoting;

import org.smart.framework.net.router.annotation.Cmd;
import org.smart.framework.net.router.annotation.Module;
import org.smart.framework.remoting.protocol.Response;

import io.netty.channel.Channel;

@Module(id = 1)
public class ServerHandler {

	@Cmd(id=1)
	public void refresh(Channel channel, Object data,Response response) {
		System.out.println(data);
	}
}
