package org.smart.framework.redis.entity;

import java.sql.Timestamp;
import java.util.List;

import org.smart.framework.datacenter.SingleEntity;
import org.smart.framework.datacenter.annotation.Column;
import org.smart.framework.datacenter.annotation.DBQueueType;
import org.smart.framework.datacenter.annotation.Table;
import org.smart.framework.util.IdentifyKey;

import com.google.common.collect.Lists;

@Table(name = "actor", type = DBQueueType.IMPORTANT)
public class ActorDo extends SingleEntity {

	@Column(pk = true)
	private Long actorId;
	@Column
	private String uid;
	@Column
	private String nickname;
	@Column
	private Integer gold;
	@Column
	private Integer diamond;
	@Column
	private Integer energy;
	@Column
	private Integer energyMax;
	@Column
	private Integer level;
	@Column
	private Integer vip;
	
	@Column
	private Timestamp createTime;
	
	public ActorDo() {
	}


	@Override
	public List<IdentifyKey> keyLists() {
		return Lists.newArrayList(IdentifyKey.build(this.actorId), IdentifyKey.build(uid));
	}

	@Override
	public IdentifyKey findPkId() {
		return IdentifyKey.build(this.actorId);
	}

	@Override
	public void setPkId(IdentifyKey pk) {
		this.actorId = pk.getIdentifys(0, Long.class);
	}

	@Override
	protected void hasReadEvent() {
	}

	@Override
	protected void beforeWritingEvent() {
	}


	@Override
	protected void disposeBlob() {

	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Integer getGold() {
		return gold;
	}

	public void setGold(Integer gold) {
		this.gold = gold;
	}

	public Integer getDiamond() {
		return diamond;
	}

	public void setDiamond(Integer diamond) {
		this.diamond = diamond;
	}

	public Integer getEnergy() {
		return energy;
	}

	public void setEnergy(Integer energy) {
		this.energy = energy;
	}

	public Integer getEnergyMax() {
		return energyMax;
	}

	public void setEnergyMax(Integer energyMax) {
		this.energyMax = energyMax;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getVip() {
		return vip;
	}

	public void setVip(Integer vip) {
		this.vip = vip;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}


	



}
