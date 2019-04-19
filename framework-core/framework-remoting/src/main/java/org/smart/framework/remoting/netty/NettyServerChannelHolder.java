package org.smart.framework.remoting.netty;

import org.smart.framework.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class NettyServerChannelHolder {

	public static final Logger LOGGER = LoggerFactory.getLogger(NettyServerChannelHolder.class);

	public static ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
	
	public static AttributeKey<Integer> CLIENT_ID = AttributeKey.valueOf("CLIENT_ID");
    
    
    public static void regist(int clientId, Channel channel) {
    	if (channelGroup.contains(channel)) {
    		return;
    	}
    	channel.attr(CLIENT_ID).set(clientId);
    	channelGroup.add(channel);
    	LOGGER.info("client regist, clientId:{},addr:{}",clientId,RemotingHelper.parseChannelRemoteAddr(channel));
    }
    public static void unregist(Channel channel) {
    	channelGroup.remove(channel);
    	
    }
    
    public static Channel findChannel(int clientId) {
        for (Channel channel : channelGroup) {
            if (channel.hasAttr(CLIENT_ID) && channel.attr(CLIENT_ID).get() == clientId) {
                return channel;
            }
        }
        return null;
    }
    
    public static Integer findClientId(Channel channel) {
    	if (channel.hasAttr(CLIENT_ID) ) {
            return channel.attr(CLIENT_ID).get();
        }
    	return null;
    }
}
