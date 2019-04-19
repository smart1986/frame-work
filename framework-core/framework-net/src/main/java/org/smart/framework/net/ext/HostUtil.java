package org.smart.framework.net.ext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Enumeration;

import org.smart.framework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

public class HostUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(HostUtil.class);
	private static String hostIP = "127.0.0.1";
	
	private static byte[] hostIPBytes;
	static {
		try {
			 for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	                NetworkInterface intf = en.nextElement();
	                String name = intf.getName();
	                if (!name.contains("docker") && !name.contains("lo")) {
	                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                        InetAddress inetAddress = enumIpAddr.nextElement();
	                        if (!inetAddress.isLoopbackAddress()) {
	                            String ipaddress = inetAddress.getHostAddress().toString();
	                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
	                            	hostIP = ipaddress;
	                            	hostIPBytes = inetAddress.getAddress();
	                                LOGGER.info("server host ip:{}", hostIP);
	                            }
	                        }
	                    }
	                }
	            }
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
	}
	
	public static String getHostIP() {
		return hostIP;
	}
	
	public static byte[] getHostIPBytes() {
		return hostIPBytes;
	}
	
	public static String getRomoteIP(ChannelHandlerContext ctx) {
		SocketAddress add = ctx.channel().remoteAddress();
		String remoteIp = null;
		if(add != null) {
			remoteIp = ((InetSocketAddress) add).getAddress().getHostAddress();
		}
		if (StringUtils.isBlank(remoteIp)) { 
			remoteIp = ((InetSocketAddress) ctx.channel().localAddress()).getAddress().getHostAddress();
		}
		return remoteIp;
	}
}
