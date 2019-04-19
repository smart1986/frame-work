package org.smart.framework.redis.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
public class RedisMessageListener implements MessageListener {

	@Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static Logger logger = LoggerFactory.getLogger(RedisMessageListener.class);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();// 请使用valueSerializer
        byte[] channel = message.getChannel();
        // 请参考配置文件，本例中key，value的序列化方式均为string。
        // 其中key必须为stringSerializer。和redisTemplate.convertAndSend对应
        String msgContent = (String) redisTemplate.getValueSerializer().deserialize(body);
        String topic = (String) redisTemplate.getStringSerializer().deserialize(channel);
        logger.info("redis--topic:" + topic + "  body:" + msgContent);
    }

}
