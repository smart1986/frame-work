package org.smart.framework.net.firewall;

/**
 * 防火墙bean配置.gateway.firewallconfig.xml
 * 
 * 
 */
public class FirewallConfig {
	
	/**
	 * 单个连接每分钟最大包数量. 1分钟最大发送3500个包
	 */
	private Integer maxPacksPerMinute = 3500;
	private Integer maxPacksErrorPerMinute = 3500;

	/**
	 * 单个连接每分钟最大发送包内容数量. 1分钟则为600K,默认:614400=600K
	 */
	private Integer maxBytesPerMinute = 600;
	
	/**
	 * 单个连接每分钟最大验证码错误次数. 如果超过则加入防火墙,默认:10
	 */
	private Integer maxAuthCodeErrorsPerMinute = 10;

	/**
	 * 检测到多少次洪水包后,禁止链接,默认:5
	 */
	private Integer blockDetectCount = 5;

	/**
	 * 禁止IP连接的分钟数,默认:5,单位:分钟
	 */
	private Integer blockIpMinutes = 5;

	/**
	 * 禁止用户连接的分钟数,默认:10,单位:分钟
	 */
	private Integer blockUserMinutes = 10;

	/**
	 * 允许的最大连接,默认:10000
	 */
	private Integer maxClientsLimit = 10000;

	/**
	 * 是否开启防火墙,默认是ture
	 */
	private Boolean enableFirewall = true;

	public Integer getMaxPacksPerMinute() {
		return maxPacksPerMinute;
	}

	public void setMaxPacksPerMinute(Integer maxPacksPerMinute) {
		this.maxPacksPerMinute = maxPacksPerMinute;
	}

	public Integer getMaxPacksErrorPerMinute() {
		return maxPacksErrorPerMinute;
	}

	public void setMaxPacksErrorPerMinute(Integer maxPacksErrorPerMinute) {
		this.maxPacksErrorPerMinute = maxPacksErrorPerMinute;
	}

	public Integer getMaxBytesPerMinute() {
		return maxBytesPerMinute;
	}

	public void setMaxBytesPerMinute(Integer maxBytesPerMinute) {
		this.maxBytesPerMinute = maxBytesPerMinute;
	}

	public Integer getMaxAuthCodeErrorsPerMinute() {
		return maxAuthCodeErrorsPerMinute;
	}

	public void setMaxAuthCodeErrorsPerMinute(Integer maxAuthCodeErrorsPerMinute) {
		this.maxAuthCodeErrorsPerMinute = maxAuthCodeErrorsPerMinute;
	}

	public Integer getBlockDetectCount() {
		return blockDetectCount;
	}

	public void setBlockDetectCount(Integer blockDetectCount) {
		this.blockDetectCount = blockDetectCount;
	}

	public Integer getBlockIpMinutes() {
		return blockIpMinutes;
	}

	public void setBlockIpMinutes(Integer blockIpMinutes) {
		this.blockIpMinutes = blockIpMinutes;
	}

	public Integer getBlockUserMinutes() {
		return blockUserMinutes;
	}

	public void setBlockUserMinutes(Integer blockUserMinutes) {
		this.blockUserMinutes = blockUserMinutes;
	}

	public Integer getMaxClientsLimit() {
		return maxClientsLimit;
	}

	public void setMaxClientsLimit(Integer maxClientsLimit) {
		this.maxClientsLimit = maxClientsLimit;
	}

	public Boolean getEnableFirewall() {
		return enableFirewall;
	}

	public void setEnableFirewall(Boolean enableFirewall) {
		this.enableFirewall = enableFirewall;
	}
	
	

}
