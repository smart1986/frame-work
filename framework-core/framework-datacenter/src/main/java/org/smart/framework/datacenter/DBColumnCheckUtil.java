package org.smart.framework.datacenter;


import com.esotericsoftware.reflectasm.ConstructorAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.datacenter.annotation.Column;
import org.smart.framework.datacenter.annotation.Table;
import org.smart.framework.util.PackageScanner;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBColumnCheckUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(DBColumnCheckUtil.class);
    public static void check(String packageScan, JdbcTemplate jdbcTemplate){
        LOGGER.info("db bean package:{}", packageScan);
        String[] temp = packageScan.split(",");
        entityScan(temp,jdbcTemplate);
    }

    public static void trancateAll(JdbcTemplate jdbcTemplate,String packageScan) {
        Collection<Class<Entity>> collection = PackageScanner.scanPackages(packageScan);
        for (Class<Entity> clz : collection) {
            if (Entity.class.isAssignableFrom(clz)) {
                EntityInfo ei = new EntityInfo();
                ei.className = clz.getCanonicalName();
                Table tname = clz.getAnnotation(Table.class);
                if (null != tname) {
                    jdbcTemplate.execute("TRUNCATE TABLE " + "`" + tname.name() + "`");
                }
            }

        }
    }

    public static void entityScan(String[] packageScan, JdbcTemplate jdbcTemplate) {
        // 获取所有继承于Entity类的对象列表。
        // 扫描所有Entity对象。获取 表名，字段名,
        Collection<Class<Entity>> collection = PackageScanner.scanPackages(packageScan);
        DatabaseMetaData databaseMetaData = null;
        try {
            databaseMetaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
        } catch (SQLException e1) {
            throw new RuntimeException(e1);
        }
        for (Class<Entity> clz : collection) {
            if (Entity.class.isAssignableFrom(clz)) {
                EntityInfo ei = new EntityInfo();
                ei.className = clz.getCanonicalName();
                Table tname = clz.getAnnotation(Table.class);
                if (null != tname) {
                    ei.tableName = "`" + tname.name() + "`";
                    ei.tableType = tname.type();
                } else {
                    LOGGER.error(clz.getCanonicalName() + "未定义表名");
                    continue;
                }
                List<String> dbColumeNames = new ArrayList<>();
                try {
                    ResultSet rs = databaseMetaData.getTables(null, null, ei.tableName, new String[] { "TABLE" });
                    if (rs.next() == false) {
                        throw new RuntimeException(tname + "表不存在.");
                    }

                    rs = databaseMetaData.getColumns(null, null, ei.tableName, "%");
                    while (rs.next()) {
                        dbColumeNames.add("`" + rs.getString("COLUMN_NAME") + "`");
                    }
                    String[] tableNames = new String[dbColumeNames.size()];
                    ei.dbColumnNames = dbColumeNames.toArray(tableNames);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // Entity反射属性列表
                Field[] fields = clz.getDeclaredFields();
                ArrayList<String> entityField = new ArrayList<>();
                List<String> pkNames = new ArrayList<>();
                for (Field field : fields) {

                    Column column = field.getAnnotation(Column.class);
                    if (null != column) {

                        String dbColumName = (!("").equals(column.alias())) ? column.alias() : field.getName();
                        dbColumName = "`" + dbColumName + "`";
                        if (column.pk()) {
                            pkNames.add(dbColumName);
                        }
                        if (column.fk()) {
                            ei.fkName = dbColumName;
                        }
                        entityField.add(dbColumName);
                        ei.columnNameMapping.put(dbColumName, field.getName());
                        ei.feildNameMapping.put(field.getName(), dbColumName);
                    }
                }

                String[] pNames = new String[pkNames.size()];
                ei.pkName = pkNames.toArray(pNames);
                if (ei.pkName == null || ei.pkName.length == 0) {
                    LOGGER.error(ei.className + " 实体缺少主键");
                }
                Set<String> set = new HashSet<>(Arrays.asList(ei.dbColumnNames));
                for (String dbc : ei.columnNameMapping.keySet()) {
                    if (!set.contains(dbc)) {
                        throw new RuntimeException(
                                "entity:" + ei.className + " table: " + ei.tableName + " " + dbc + " colume not exsit");
                    }
                }

                // 实例化Entity
                try {
                    ei.entity = clz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                EntityInfo.ENTITY_INFOS.put(clz, ei);
                EntityInfo.ENTITY_BEANCOPIER.put(clz, BeanCopier.create(clz, clz, false));
                EntityInfo.ENTITY_CONSTRUCT_ACCESS.put(clz, ConstructorAccess.get(clz));
            }

        }
    }
}
