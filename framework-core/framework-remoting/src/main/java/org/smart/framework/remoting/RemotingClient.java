package org.smart.framework.remoting;

import java.util.concurrent.ExecutorService;

import org.smart.framework.remoting.exception.RemotingConnectException;
import org.smart.framework.remoting.exception.RemotingSendRequestException;
import org.smart.framework.remoting.exception.RemotingTimeoutException;
import org.smart.framework.remoting.exception.RemotingTooMuchRequestException;
import org.smart.framework.remoting.netty.NettyRequestProcessor;
import org.smart.framework.remoting.protocol.RemotingCommand;

public interface RemotingClient extends RemotingService {

    RemotingCommand invokeSync(final RemotingCommand request,
        final long timeoutMillis) throws InterruptedException, RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException;

    void invokeAsync(final RemotingCommand request, final long timeoutMillis,
        final InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException,
        RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;


    void setCallbackExecutor(final ExecutorService callbackExecutor);
    
    void registerProcessor(final short requestCode, final NettyRequestProcessor processor,
            final ExecutorService executor);

}
