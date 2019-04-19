package org.smart.framework.remoting;

import org.smart.framework.remoting.netty.ResponseFuture;

public interface InvokeCallback {
    void operationComplete(final ResponseFuture responseFuture);
}
