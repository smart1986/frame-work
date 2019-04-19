package org.smart.framework.redis.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisMessageSender {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	public void sendMessage(String channel, String message) {
	    redisTemplate.convertAndSend(channel, message);
	}
}
