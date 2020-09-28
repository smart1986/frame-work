package org.smart.framework.dataconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.dataconfig.parse.JsonDataParser;

public class ConfigTest {
    private static Logger logger = LoggerFactory.getLogger(ConfigTest.class);
    public static void main(String[] args) throws Exception {
        DataConfigImpl dataConfig = new DataConfigImpl();
        DataConfiguration dataConfiguration = new DataConfiguration();
        dataConfiguration.setPackageScan("org.smart.framework.dataconfig.bean");
        dataConfiguration.setExtension(".json");
        dataConfig.initModelAdapterList(dataConfiguration,new JsonDataParser());
        
        dataConfig.getAllConfigName().forEach(name-> System.out.println(name));

    }
}
