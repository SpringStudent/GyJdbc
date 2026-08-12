package com.gysoft.jdbc.jdbctest;

import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.GyjdbcException;
import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.entity.MemberEntity;
import com.gysoft.jdbc.multi.DataSourceContext;
import com.gysoft.jdbc.multi.JdbcRoutingDataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 多数据源编程式路由（DataSourceContext.withDataSource + JdbcRoutingDataSource）集成测试。
 * <p>用两个独立的 H2 内存库模拟 master/slave，验证：
 * 1. 默认路由（未绑定）落到 defaultLookUpKey 对应的库；
 * 2. 绑定 key 后读写均路由到指定库；
 * 3. 两个库数据互相隔离。</p>
 *
 * @author 周宁
 */
public class JdbcRoutingDataSourceIT extends AbstractJdbcIT {

    private static final String MASTER_URL =
            "jdbc:h2:mem:master;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String SLAVE_URL =
            "jdbc:h2:mem:slave;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private JdbcTemplate masterTemplate;

    private JdbcTemplate slaveTemplate;

    private EntityDao<MemberEntity, Integer> routingDao;

    @Before
    public void setUpRouting() {
        DriverManagerDataSource masterDataSource = new DriverManagerDataSource(MASTER_URL, "sa", "");
        DriverManagerDataSource slaveDataSource = new DriverManagerDataSource(SLAVE_URL, "sa", "");
        masterTemplate = new JdbcTemplate(masterDataSource);
        slaveTemplate = new JdbcTemplate(slaveDataSource);
        masterTemplate.execute(CREATE_MEMBER_TABLE);
        slaveTemplate.execute(CREATE_MEMBER_TABLE);
        masterTemplate.update("DELETE FROM tb_member");
        slaveTemplate.update("DELETE FROM tb_member");

        JdbcRoutingDataSource routingDataSource = new JdbcRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);
        targetDataSources.put("slave", slaveDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultLookUpKey("master");
        routingDataSource.afterPropertiesSet();
        // 匿名子类携带具体泛型，供 EntityDaoImpl 构造器泛型解析；反射注入路由 jdbcTemplate
        EntityDaoImpl<MemberEntity, Integer> dao = new EntityDaoImpl<MemberEntity, Integer>() {
        };
        try {
            Field field = EntityDaoImpl.class.getDeclaredField("jdbcTemplate");
            field.setAccessible(true);
            field.set(dao, new JdbcTemplate(routingDataSource));
        } catch (Exception e) {
            throw new GyjdbcException("注入 jdbcTemplate 失败", e);
        }
        routingDao = dao;
    }

    @Test
    public void defaultRouteGoesToMaster() {
        routingDao.save(newMember(1, "default", 20));
        assertEquals(1, count(masterTemplate));
        assertEquals(0, count(slaveTemplate));
    }

    @Test
    public void scopedRouteWritesAndReadsFromSlave() {
        DataSourceContext.withDataSource("slave", () -> routingDao.save(newMember(1, "slave-user", 20)));
        DataSourceContext.withDataSource("slave", () -> {
            List<MemberEntity> all = routingDao.queryAll();
            assertEquals(1, all.size());
            assertEquals("slave-user", all.get(0).getName());
            return null;
        });
        // 写与读都落在 slave，master 无数据
        assertEquals(0, count(masterTemplate));
        assertEquals(1, count(slaveTemplate));
    }

    @Test
    public void masterAndSlaveDataAreIsolated() {
        routingDao.save(newMember(1, "m", 20));
        DataSourceContext.withDataSource("slave", () -> routingDao.save(newMember(1, "s", 20)));
        assertEquals("m", masterTemplate.queryForObject(
                "SELECT name FROM tb_member WHERE id = 1", String.class));
        assertEquals("s", slaveTemplate.queryForObject(
                "SELECT name FROM tb_member WHERE id = 1", String.class));
        // 路由到 slave 查询，看不到 master 的数据
        DataSourceContext.withDataSource("slave", () -> {
            assertTrue(routingDao.queryWithCriteria(Criteria.newCriteria().where("name", "m")).isEmpty());
            assertEquals(1, routingDao.queryWithCriteria(Criteria.newCriteria().where("name", "s")).size());
            return null;
        });
    }

    @Test
    public void scopedRouteFallsBackAfterLeavingScope() {
        DataSourceContext.withDataSource("slave", () -> routingDao.save(newMember(1, "slave-user", 20)));
        // 离开 withDataSource 作用域后回到默认路由（master）
        assertEquals(0, count(masterTemplate));
        assertEquals(1, count(slaveTemplate));
        routingDao.save(newMember(2, "master-user", 30));
        assertEquals(1, count(masterTemplate));
        assertEquals(1, count(slaveTemplate));
    }

    private static int count(JdbcTemplate template) {
        Integer count = template.queryForObject("SELECT COUNT(*) FROM tb_member", Integer.class);
        return count == null ? 0 : count;
    }
}
