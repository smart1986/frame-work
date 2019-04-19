package org.smart.framework.redis.dao;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.smart.framework.redis.entity.ActorDo;
import org.smart.framework.util.IdentiyKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ActorDao extends RedisSingleEntityDaoImpl<ActorDo> {

    private AtomicLong actorIdIndex;
    @Value("${server.id}")
	private int serverId;

    @Override
    protected Class<ActorDo> forClass() {
        return ActorDo.class;
    }

    @Override
    protected void initMaxId() {
        Long maxActorId = jdbc.queryForObject("SELECT max(actorId) FROM actor", Long.class);
        if (maxActorId == null || maxActorId < 0) {
            actorIdIndex = new AtomicLong(serverId<<22);
        } else {
            actorIdIndex = new AtomicLong(maxActorId);
        }

    }

    public ActorDo createActor(String uid, String nickname,int gold,int diamond,int level, int energy) {
    	long actorId = actorIdIndex.incrementAndGet();
        ActorDo actor = get(actorId);
        actor.setUid(uid);
        actor.setNickname(nickname);
        actor.setGold(gold);
        actor.setDiamond(diamond);
        actor.setLevel(level);
        actor.setEnergy(energy);
        actor.setEnergyMax(energy);
        Timestamp current = new Timestamp(System.currentTimeMillis());
        actor.setCreateTime(current);
        updateQueue(actor);
        return actor;
    }

    public ActorDo getActorByUid(String uid) {
        return getFromCacheWithCacheOtherKey(IdentiyKey.build(uid));
    }

    public ActorDo getBycondition(LinkedHashMap<String, Object> condition) {
        ActorDo actor = jdbc.getFirst(ActorDo.class, condition);
        if (actor == null || actor.newEntity()) {
            return null;
        }
        return actor;
    }

    public ActorDo get(long actorId) {
        ActorDo actor = get(IdentiyKey.build(actorId));
        return actor;
    }

    @Override
    protected ActorDo loadFromDBOtherKey(IdentiyKey key) {
        LinkedHashMap<String, Object> condition = new LinkedHashMap<>();
        condition.put("uid", key.getIdentifys(0, Long.class));
        ActorDo actor = getBycondition(condition);
        return actor;
    }



}
