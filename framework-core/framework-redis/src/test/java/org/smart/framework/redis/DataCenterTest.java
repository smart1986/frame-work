package org.smart.framework.redis;

import org.smart.framework.redis.dao.ActorDao;
import org.smart.framework.redis.entity.ActorDo;
import org.smart.framework.util.IdentifyKey;
import org.smart.framework.util.StaticSpringUtils;

public class DataCenterTest {
	public static void main(String[] args) {
		ActorDao dao = StaticSpringUtils.getApplicationContext().getBean(ActorDao.class);
//		dao.createActor("12112", "redis", 1, 1, 1, 1);
		ActorDo actorDo = dao.get(IdentifyKey.build(4194306L));
		System.out.println(actorDo.getNickname());
		actorDo = dao.get(IdentifyKey.build(4194306L));
		System.out.println(actorDo.getNickname());
	}
}
