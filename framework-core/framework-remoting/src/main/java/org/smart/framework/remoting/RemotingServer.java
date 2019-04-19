package org.smart.framework.remoting;

import java.util.concurrent.ExecutorService;

import org.smart.framework.remoting.exception.RemotingSendRequestException;
import org.smart.framework.remoting.exception.RemotingTimeoutException;
import org.smart.framework.remoting.exception.RemotingTooMuchRequestException;
import org.smart.framework.remoting.netty.NettyRequestProcessor;
import org.smart.framework.remoting.protocol.RemotingCommand;

import io.netty.channel.Channel;

public interface RemotingServer extends RemotingService {

    int localListenPort();


    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
        final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
        RemotingTimeoutException;

    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
        final InvokeCallback invokeCallback) throws InterruptedException,
        RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;
    void registerProcessor(final short requestCode, final NettyRequestProcessor processor,
            final ExecutorService executor);

        void registerDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);
}
