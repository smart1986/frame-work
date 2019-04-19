package org.smart.framework.net.callback;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelFutureListener;

public class OperationComplete {
	private List<ChannelFutureListener> listeners = new ArrayList<>();
	
	public List<ChannelFutureListener> getListeners() {
		return listeners;
	}
	
	public void addListenser(ChannelFutureListener listener){
		this.listeners.add(listener);
	}
}
