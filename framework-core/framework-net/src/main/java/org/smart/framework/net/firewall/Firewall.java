package org.smart.framework.net.firewall;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Strings;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;



/**
 * 防火墙配置类
 *
 */
public class Firewall {
	private static final Log LOGGER = LogFactory.getLog(Firewall.class);
	
	public static AttributeKey<FloodRecord> FLOOD_RECORD = AttributeKey.valueOf("FLOOD_RECORD");

	private FirewallConfig firewallConfig = new FirewallConfig();
	
	
	/**
	 * AtomicInteger 一种线程安全的加减操作接口用于给session分配自增编号
	 */
	private static final AtomicInteger ATOMIC_ID = new AtomicInteger();

	/**
	 * ip黑名单列表
	 */
	private static ConcurrentHashMap<String, Long> BLOCKED_IPS = new ConcurrentHashMap<String, Long>(1);

	/**
	 * 玩家黑名单列表
	 */
	private static ConcurrentHashMap<Long, Long> BLOCKED_PLAYER_IDS = new ConcurrentHashMap<Long, Long>(1);

	/**
	 * 可疑ip列表
	 */
	private static ConcurrentHashMap<String, AtomicInteger> SUSPICIOUS_IPS = new ConcurrentHashMap<String, AtomicInteger>(1);

	/**
	 * 可疑玩家列表
	 */
	private static ConcurrentHashMap<Long, AtomicInteger> SUSPICIOUS_PLAYERIDS = new ConcurrentHashMap<Long, AtomicInteger>(1);
	
	

	public Firewall() {
		super();
	}

	public Firewall(FirewallConfig firewallConfig) {
		super();
		this.firewallConfig = firewallConfig;
	}

	public int getClients() {
		return ATOMIC_ID.get();
	}

	/**
	 * 当前值加1
	 * @return
	 */
	public int increaseClients() {
		return ATOMIC_ID.incrementAndGet();
	}

	/**
	 * 当前值减1
	 * @return
	 */
	public int decreaseClients() {
		return ATOMIC_ID.decrementAndGet();
	}

	public boolean isMaxClientLimit(int currClients) {
		return currClients > firewallConfig.getMaxClientsLimit();
	}

	public boolean isBlocked(Channel channel) {
//		return isIpBlock(session) || isPlayerIdBlock(session);
		return isIpBlock(channel) ;
	}

	private boolean isIpBlock(Channel channel) {
		String remoteIp = null;
		SocketAddress add = channel.remoteAddress();
		if(add != null) {
			remoteIp = ((InetSocketAddress) add).getAddress().getHostAddress();
		}
		if (Strings.isNullOrEmpty(remoteIp)) { 
			remoteIp = ((InetSocketAddress) channel.localAddress()).getAddress().getHostAddress();
		}
		if (remoteIp == null || remoteIp.isEmpty()) {
			return false;
		}

		Long blockedTime = (Long) BLOCKED_IPS.get(remoteIp);
		if (blockedTime == null) {
			return false;
		}

		if (blockedTime.longValue() <= System.currentTimeMillis()) {
			BLOCKED_IPS.remove(remoteIp);
			return false;
		}
		return true;
	}

//	private boolean isPlayerIdBlock(ChannelHandlerContext session) {
//		long actorId = playerSession.getActorId(session);
//		if (actorId <= 0) {
//			return false;
//		}
//		
//		Long blockedTime = BLOCKED_PLAYER_IDS.get(actorId);
//		if (blockedTime == null) {
//			return false;
//		}
//
//		if (blockedTime.longValue() <= System.currentTimeMillis()) {
//			BLOCKED_PLAYER_IDS.remove(actorId);
//			return false;
//		}
//		return true;
//	}

	public void blockIp(String ip) {
		long currentTimeMillis = System.currentTimeMillis();
		int blockIpMillis = getBlockIpMinutesOfMilliSecond();
		BLOCKED_IPS.put(ip, Long.valueOf(currentTimeMillis + blockIpMillis));
	}

	public void unblockIp(String remoteIp) {
		BLOCKED_IPS.remove(remoteIp);
	}

	public void blockPlayer(long playerId) {
		long currTime = System.currentTimeMillis();
		int blockUserTime = getBlockMinutesOfMilliSecond();
		BLOCKED_PLAYER_IDS.put(Long.valueOf(playerId), Long.valueOf(currTime + blockUserTime));
	}

	public void unblockPlayer(long playerId) {
		BLOCKED_PLAYER_IDS.remove(Long.valueOf(playerId));
	}

	public boolean blockedByBytes(Channel channel, int bytes) {
		return checkBlock(channel, FirewallType.BYTE, bytes);
	}

	public boolean blockedByPacks(Channel channel, int packs) {
		return checkBlock(channel, FirewallType.PACK, packs);
	}
	public boolean blockedByPacksError(Channel channel, int packs) {
		return checkBlock(channel, FirewallType.ERROR_PACK, packs);
	}

	public boolean blockedByAuthCodeErrors(Channel channel, int errors) {
		return checkBlock(channel, FirewallType.AUTHCODE, errors);
	}
	
	public boolean isEnableFirewall() {
		return this.firewallConfig.getEnableFirewall() != null && this.firewallConfig.getEnableFirewall();
	}

	/**
	 * 收包检查
	 * @param session
	 * @param type
	 * @param amount
	 * @return
	 */
	private boolean checkBlock(Channel channel, FirewallType type, int amount) {
		if (channel == null) {
			return false;
		}

		if (isBlocked(channel)) {
			return true;
		}

		if (amount <= 0) {
			return false;
		}

		FloodRecord floodRecord = channel.attr(FLOOD_RECORD).get();
		if (floodRecord == null) {
			floodRecord = new FloodRecord();
			channel.attr(FLOOD_RECORD).set(floodRecord);
		}

		boolean suspicious = false;
		if (type == FirewallType.BYTE)
			suspicious = avalidateWithBytes(amount, floodRecord);
		else if (type == FirewallType.PACK)
			suspicious = avalidateWithPackages(amount, floodRecord);
		else if (type == FirewallType.AUTHCODE) 
			suspicious = avalidateWithAuthcode(amount, floodRecord);
		else if (type == FirewallType.ERROR_PACK)
			suspicious = avalidateWithErrorPackages(amount, floodRecord);
		

		boolean isBlack = false;
		if (suspicious) {
			String remoteIp = getRemoteIp(channel);
//			Long playerId = playerSession.getActorId(session);
			Long playerId = 0L;
			//没登陆则进行停封ip处理
			if (playerId.longValue() <= 0L) {
				AtomicInteger blocks = (AtomicInteger) SUSPICIOUS_IPS.get(remoteIp);
				if (blocks == null) {
					SUSPICIOUS_IPS.put(remoteIp, new AtomicInteger());
					blocks = (AtomicInteger) SUSPICIOUS_IPS.get(remoteIp);
				}

				if (blocks.incrementAndGet() >= firewallConfig.getBlockDetectCount()) {
					blocks.set(0);
					isBlack = true;
					blockIp(remoteIp);
				}
			} else {
				AtomicInteger blocks = (AtomicInteger) SUSPICIOUS_PLAYERIDS.get(playerId);
				if (blocks == null) {
					SUSPICIOUS_PLAYERIDS.putIfAbsent(playerId, new AtomicInteger());
					blocks = (AtomicInteger) SUSPICIOUS_PLAYERIDS.get(playerId);
				}

				if (blocks.incrementAndGet() >= firewallConfig.getBlockDetectCount()) {
					blocks.set(0);
					isBlack = true;
					blockPlayer(playerId.longValue());
				}
			}

			LOGGER.warn(String.format("{%s}", floodRecord.toString()));
			LOGGER.warn(String.format("ip: %s, playerId: %d, block: %s", remoteIp, playerId.longValue(), String.valueOf(isBlack)));
		}

		return isBlack;
	}

	/**
	 * 
	 * @param session
	 */
	public void removeBlockCounter(Channel channel) {
		if (channel != null) {
			try {
				SUSPICIOUS_IPS.remove(getRemoteIp(channel));
//				SUSPICIOUS_PLAYERIDS.remove(playerSession.getActorId(session));
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
	}

	private boolean avalidateWithAuthcode(int amount, FloodRecord floodCheck) {
		long currentMillis = System.currentTimeMillis();
		long currMinue = currentMillis / 60000L;
		long lastMin = floodCheck.getLastAuthCodeTime() / 60000L;
		floodCheck.setLastAuthCodeTime(currentMillis);
		if (lastMin == currMinue) {
			floodCheck.addLastMinuteAuthCodes(amount);
		} else {
			floodCheck.setLastMinuteAuthCodes(amount);
		}

		int lastMinuteAuthCodes = floodCheck.getLastMinuteAuthCodes();
		if (lastMinuteAuthCodes >= firewallConfig.getMaxAuthCodeErrorsPerMinute().intValue()) {
			floodCheck.setLastMinuteAuthCodes(0);
			LOGGER.error(String.format("AuthCode errors overflow: lastMinuteAuthCodes[%d], maxAuthCodeErrorsPerMinute[%d]", lastMinuteAuthCodes,
					firewallConfig.getMaxAuthCodeErrorsPerMinute()));
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param amount
	 * @param floodCheck
	 * @return
	 */
	private boolean avalidateWithPackages(int amount, FloodRecord floodCheck) {
		long currentMillis = System.currentTimeMillis()/1000;
		long currMinue = currentMillis / 60L;
		long lastMin = floodCheck.getLastPackTime() / 60L;
		floodCheck.setLastPackTime(currentMillis);
		if (lastMin == currMinue) {
			floodCheck.addLastMinutePacks(amount);
		} else {
			floodCheck.setLastMinutePacks(amount);
		}

		int lastMinutePacks = floodCheck.getLastMinutePacks();
		if (lastMinutePacks >= firewallConfig.getMaxPacksPerMinute().intValue()) {
			floodCheck.setLastMinutePacks(0);
			LOGGER.error(String.format("Packs overflow: lastMinutePacks[%d], maxPacksPerMinute[%d]", lastMinutePacks,firewallConfig.getMaxPacksPerMinute()));
			return true;
		}
		return false;
	}
	private boolean avalidateWithErrorPackages(int amount, FloodRecord floodCheck) {
		long currentMillis = System.currentTimeMillis()/1000;
		long currMinue = currentMillis / 60L;
		long lastMin = floodCheck.getLastPackErrorTime() / 60L;
		floodCheck.setLastPackErrorTime(currentMillis);
		if (lastMin == currMinue) {
			floodCheck.addLastMinutePacksError(amount);
		} else {
			floodCheck.setLastMinutePacksErrorSize(amount);
		}
		
		int lastMinutePacks = floodCheck.getLastMinutePacksErrorSize();
		if (lastMinutePacks >= firewallConfig.getMaxPacksErrorPerMinute().intValue()) {
			floodCheck.setLastMinutePacksErrorSize(0);
			LOGGER.error(String.format("Packs error overflow: lastMinutePacks[%d], maxPacksErrorPerMinute[%d]", lastMinutePacks,firewallConfig.getMaxPacksErrorPerMinute()));
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param amount
	 * @param floodCheck
	 * @return
	 */
	private boolean avalidateWithBytes(int amount, FloodRecord floodCheck) {
		long currentMillis = System.currentTimeMillis()/1000;
		long currMinue = currentMillis / 60000L;
		long lastMinue = floodCheck.getLastSizeTime() / 60000L;
		floodCheck.setLastSizeTime(currentMillis);
		if (lastMinue == currMinue) {
			floodCheck.addLastMinuteSizes(amount);
		} else {
			floodCheck.setLastMinuteSizes(amount);
		}

		int lastMinuteSizes = floodCheck.getLastMinuteSizes();
		if (lastMinuteSizes >= firewallConfig.getMaxBytesPerMinute().intValue()) {
			floodCheck.setLastMinuteSizes(0);
			LOGGER.error(String.format("Bytes overflow: lastMinuteSizes[%d], maxBytesPerMinute[%d]", lastMinuteSizes,firewallConfig.getMaxBytesPerMinute()));
			return true;
		}

		return false;
	}

	public int getMaxPacksPerMinute() {
		return firewallConfig.getMaxPacksPerMinute().intValue();
	}

	public int getMaxBytesPerMinute() {
		return firewallConfig.getMaxBytesPerMinute().intValue();
	}

	public int getMaxAuthCodeErrorsPerMinute() {
		return firewallConfig.getMaxAuthCodeErrorsPerMinute().intValue();
	}

	public int getBlockDetectCount() {
		return firewallConfig.getBlockDetectCount().intValue();
	}

	public int getBlockIpMinutesOfMilliSecond() {
		return firewallConfig.getBlockIpMinutes().intValue() * 60000;
	}

	public int getBlockMinutesOfMilliSecond() {
		return firewallConfig.getBlockUserMinutes().intValue() * 60000;
	}

	public int getMaxClientsLimit() {
		return firewallConfig.getMaxClientsLimit().intValue();
	}
	
	
	
	public FirewallConfig getFirewallConfig() {
		return firewallConfig;
	}

	public void setFirewallConfig(FirewallConfig firewallConfig) {
		this.firewallConfig = firewallConfig;
	}

	public String getRemoteIp(Channel channel) {
		String remoteIp = null;
		SocketAddress add = channel.remoteAddress();
		if(add != null) {
			remoteIp = ((InetSocketAddress) add).getAddress().getHostAddress();
		}
		if (Strings.isNullOrEmpty(remoteIp)) { 
			remoteIp = ((InetSocketAddress) channel.localAddress()).getAddress().getHostAddress();
		}
		return remoteIp;
	}

	static enum FirewallType {
		PACK,
		ERROR_PACK,
		BYTE,

		AUTHCODE;
	}
}