package org.smart.framework.datacenter;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.util.ExceptionUtil;
import org.smart.framework.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RedisDataStoreImpl implements DataStore {
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
                    if (o instanceof  Entity){

                        try {
                            jdbcTemplate.update((Entity) o);
                        } catch (Exception e) {
                            LOGGER.error("{}", ExceptionUtil.getStackTrace(e));
                            LOGGER.error("entity save fail,entity:{}", JSON.toJSONString(o));
                            redisTemplate.opsForList().rightPush(saveQueue, o);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void update(Entity... entity) {
        for (Entity en : entity) {
            redisTemplate.opsForList().leftPush(saveQueue, en);
        }
    }

    @Override
    public void delete(Entity... entity) {

    }

    @Override
    public void update(Collection<Entity> entities) {
        redisTemplate.opsForList().leftPush(saveQueue, entities);
    }

    @Override
    public void insert(Entity... entity) {

    }

    @Override
    public void insert(Collection<Entity> entities) {

    }


}
