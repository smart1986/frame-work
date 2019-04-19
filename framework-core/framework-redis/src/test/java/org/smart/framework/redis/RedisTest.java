package org.smart.framework.redis;

import java.util.concurrent.TimeUnit;

import org.smart.framework.redis.dao.ActorDao;
import org.smart.framework.redis.entity.ActorDo;
import org.smart.framework.util.IdentiyKey;
import org.smart.framework.util.StaticSpringUtils;
import org.smart.framework.util.UUIDUtils;

public class RedisTest {

	public static void main(String[] args) throws Exception{
//		RedisMessageSender sender = StaticSpringUtils.getApplicationContext().getBean(RedisMessageSender.class);
//		sender.sendMessage("talk", "talk111");
		
		ActorDao actrDao = StaticSpringUtils.getApplicationContext().getBean(ActorDao.class);
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true){
					String requestId = UUIDUtils.getUUID();
					
					boolean flag = actrDao.tryLock(IdentiyKey.build(4194305L), requestId);
					if (flag) {
						System.out.println("t1 lock...");
						ActorDo actorDo = actrDao.get(4194305L);
						System.out.println(actorDo.getActorId());
						actrDao.unlock(IdentiyKey.build(4194305L), requestId);
						System.out.println("t1 unlock...");
						try {
							TimeUnit.SECONDS.sleep(2);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						};
						
					}
				}
			}
		});
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					String requestId = UUIDUtils.getUUID();
					boolean flag = actrDao.tryLock(IdentiyKey.build(4194305L), requestId);
					System.out.println(flag);
					if (flag) {
						System.out.println("t2 lock...");
						ActorDo actorDo = actrDao.get(4194305L);
						System.out.println(actorDo.getActorId());
						actrDao.unlock(IdentiyKey.build(4194305L), requestId);
						System.out.println("t2 unlock...");
						try {
							TimeUnit.SECONDS.sleep(4);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						};
					}
				}
			}
		});
		t1.start(); 
		TimeUnit.SECONDS.sleep(4);
		t2.start();
		
	}

}
