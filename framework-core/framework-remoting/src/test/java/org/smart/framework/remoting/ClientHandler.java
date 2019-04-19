package org.smart.framework.remoting;

import org.smart.framework.net.router.annotation.Cmd;
import org.smart.framework.net.router.annotation.Module;

import io.netty.channel.Channel;

@Module(id = 1)
public class ClientHandler {

	@Cmd(id=1)
	public void refresh(Channel channel, Object data) {
		System.out.println(data);
	}
}
