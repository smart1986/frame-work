package org.smart.framework.datacenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.smart.framework.datacenter.dao.BaseDao;
import org.smart.framework.util.common.GameInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


/**
 * spring 启动完成 开始执行业务
 *
 * @author jerry
 */
@Component
public class SpringBoot implements ApplicationListener<ContextRefreshedEvent> {
    private static Logger logger = LoggerFactory.getLogger(SpringBoot.class);

    @Autowired
    private BaseJdbcTemplate baseJdbcTemplate;


    @Autowired
    private DBQueue dbQueue;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext().getParent() == null) {
            // 执行业务
            logger.info("spring boot complete!");

            try {
                dbQueue.initialize();
                baseJdbcTemplate.init();
                Map<String, BaseDao> daos = event.getApplicationContext().getBeansOfType(BaseDao.class);
                for (BaseDao dao : daos.values()) {
                    dao.init();
                }
                Map<String, GameInit> gameInits = event.getApplicationContext().getBeansOfType(GameInit.class);
                List<GameInit> list = new ArrayList<>(gameInits.values());
                Collections.sort(list, new Comparator<GameInit>() {

                    @Override
                    public int compare(GameInit o1, GameInit o2) {
                        if (o1.index().ordinal() > o2.index().ordinal()) {
                            return 1;
                        } else if (o1.index().ordinal() < o2.index().ordinal()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                for (GameInit gameInit : list) {
                    gameInit.gameInit();
                }


            } catch (Exception e) {
                logger.error("{}", e);
                System.exit(0);
            }
        }

    }

}
