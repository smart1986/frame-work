package org.smart.framework.redis;

import java.util.Map;

import org.smart.framework.datacenter.BaseJdbcTemplate;
import org.smart.framework.datacenter.DBQueue;
import org.smart.framework.datacenter.dao.BaseDao;
import org.smart.framework.util.BaseBoot;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SpringBoot extends BaseBoot {

	@Override
	protected void dbInit(ContextRefreshedEvent event) {
		DBQueue dbQueue = event.getApplicationContext().getBean(DBQueue.class);
		dbQueue.initialize();
		BaseJdbcTemplate baseJdbcTemplate = event.getApplicationContext().getBean(BaseJdbcTemplate.class);
		baseJdbcTemplate.init();
		Map<String, BaseDao> daos = event.getApplicationContext().getBeansOfType(BaseDao.class);
		for (BaseDao dao : daos.values()) {
			dao.init();
		}
	}
}
