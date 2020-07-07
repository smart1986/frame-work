package org.smart.framework.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.datacenter.BaseJdbcTemplate;
import org.smart.framework.datacenter.DBQueue;
import org.smart.framework.datacenter.Entity;
import org.smart.framework.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RedisDBQueueImpl implements DBQueue {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private BaseJdbcTemplate jdbcTemplate;

    private String saveQueue = "SaveQueue";

    private Integer blockTime = 5000;

    private Integer eachSubmitNum = 1000;

    /**
     * 互斥同步锁
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    private final Condition condition = this.reentrantLock.newCondition();

    private Executor executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(saveQueue));


    @Override
    public void initialize() {
        executor.execute(()->{
            while (true) {
                if (blockTime > 0) {
                    try {
                        reentrantLock.lockInterruptibly();
                        condition.await(blockTime, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        LOGGER.error("{}", e);
                    } finally {
                        reentrantLock.unlock();
                    }
                }
                for (int i = 0; i <eachSubmitNum ; i++) {
                    Object o = redisTemplate.opsForList().rightPop(saveQueue);
                    if (o == null) {
                        break;
                    }
                    //System.out.println(o);
                }
            }
        });
    }

    @Override
    public void updateQueue(Entity... entity) {
        for (Entity en : entity) {
            redisTemplate.opsForList().leftPush(saveQueue, en);
        }
    }

    @Override
    public void deleteQueue(Entity... entity) {

    }

    @Override
    public void updateQueue(Collection<Entity> entities) {
        redisTemplate.opsForList().leftPush(saveQueue, entities);
    }

    @Override
    public void insertQueue(Entity... entity) {

    }

    @Override
    public void insertQueue(Collection<Entity> entities) {

    }

    @Override
    public int getTaskSize() {
        return 0;
    }

    @Override
    public int getNormalEntitySize() {
        return 0;
    }

    @Override
    public int getActorSize() {
        return 0;
    }

    @Override
    public void changeBlockTime(int flag) {

    }

    @Override
    public boolean actorInQueue(Object actorId) {
        return false;
    }

    @Override
    public void addShutdownBefore(Runnable r) {

    }

}
