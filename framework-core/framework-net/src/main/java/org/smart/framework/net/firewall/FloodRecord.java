package org.smart.framework.net.firewall;

/**
 * 洪水攻击记录. 
 * 可以拆分出来不用塞在Session Attribute中
 * 
 */
public class FloodRecord {

	private long lastSizeTime = 0L;

	private long lastPackTime = 0L;
	private long lastPackErrorTime = 0L;

	private int lastMinutePacks = 0;
	private int lastMinutePacksErrorSize = 0;

	private int lastMinuteSizes = 0;

	private long lastAuthCodeTime = 0L;

	private int lastMinuteAuthCodes = 0;

	/**
	 * 获取 最后一次收包时间
	 * @return
	 */
	public long getLastSizeTime() {
		return this.lastSizeTime;
	}

	/**
	 * 设置 最后一次收包时间
	 * @param lastSizeTime
	 */
	public void setLastSizeTime(long lastSizeTime) {
		this.lastSizeTime = lastSizeTime;
	}

	
	public long getLastPackTime() {
		return this.lastPackTime;
	}

	public void setLastPackTime(long lastPackTime) {
		this.lastPackTime = lastPackTime;
	}
	public void setLastPackErrorTime(long lastPackTime) {
		this.lastPackErrorTime = lastPackTime;
	}
	public long getLastPackErrorTime() {
		return lastPackErrorTime;
	}

	public void addLastPackTime(long lastPackTime) {
		this.lastPackTime += lastPackTime;
	}

	public int getLastMinutePacks() {
		return this.lastMinutePacks;
	}

	public void setLastMinutePacks(int lastMinutePacks) {
		this.lastMinutePacks = lastMinutePacks;
	}

	public void addLastMinutePacks(int lastMinutePacks) {
		this.lastMinutePacks += lastMinutePacks;
	}
	public void addLastMinutePacksError(int lastMinutePacks) {
		this.lastMinutePacksErrorSize += lastMinutePacks;
	}
	
	public void setLastMinutePacksErrorSize(int lastMinutePacksErrorSize) {
		this.lastMinutePacksErrorSize = lastMinutePacksErrorSize;
	}
	
	public int getLastMinutePacksErrorSize() {
		return lastMinutePacksErrorSize;
	}

	public int getLastMinuteSizes() {
		return this.lastMinuteSizes;
	}

	public void setLastMinuteSizes(int lastMinuteSizes) {
		this.lastMinuteSizes = lastMinuteSizes;
	}

	public void addLastMinuteSizes(int lastMinuteSizes) {
		this.lastMinuteSizes += lastMinuteSizes;
	}

	public long getLastAuthCodeTime() {
		return this.lastAuthCodeTime;
	}

	public void setLastAuthCodeTime(long lastAuthCodeTime) {
		this.lastAuthCodeTime = lastAuthCodeTime;
	}

	public void addLastAuthCodeTime(long lastAuthCodeTime) {
		this.lastAuthCodeTime += lastAuthCodeTime;
	}

	public int getLastMinuteAuthCodes() {
		return this.lastMinuteAuthCodes;
	}

	public void setLastMinuteAuthCodes(int lastMinuteAuthCodes) {
		this.lastMinuteAuthCodes = lastMinuteAuthCodes;
	}

	public void addLastMinuteAuthCodes(int lastMinuteAuthCodes) {
		this.lastMinuteAuthCodes += lastMinuteAuthCodes;
	}

	public String toString() {
		return "FloodRecode [lastSizeTime=" + this.lastSizeTime + ", lastPackTime=" + this.lastPackTime + ", lastMinutePacks=" + this.lastMinutePacks
				+ ", lastMinuteSizes=" + this.lastMinuteSizes + ", lastAuthCodeTime=" + this.lastAuthCodeTime + ", lastMinuteAuthCodes="
				+ this.lastMinuteAuthCodes + "]";
	}
}