package com.gysoft.jdbc.jdbctest;

import com.gysoft.jdbc.bean.GyjdbcException;
import com.gysoft.jdbc.dao.EntityDao;
import com.gysoft.jdbc.dao.EntityDaoImpl;
import com.gysoft.jdbc.entity.MemberEntity;
import com.gysoft.jdbc.entity.OrderEntity;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.reflect.Field;

/**
 * H2 内存库（MODE=MySQL）集成测试基类。
 * <p>用 H2 的 MySQL 兼容模式模拟真实 MySQL，无需 Docker/外部数据库：
 * 覆盖实体映射、分页、条件查询、多数据源路由等与真实库强相关的行为。
 * DATABASE_TO_LOWER 统一未加引号标识符为小写，与 BeanPropertyRowMapper
 * 的属性名小写匹配策略兼容。</p>
 *
 * @author 周宁
 */
public abstract class AbstractJdbcIT {

    /** H2 内存库连接串，DB_CLOSE_DELAY=-1 保证库在测试进程内一直存活 */
    public static final String H2_URL =
            "jdbc:h2:mem:gyjdbc;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    /** tb_member 建表 DDL，列名与 MemberEntity 字段按驼峰/下划线规则匹配 */
    public static final String CREATE_MEMBER_TABLE =
            "CREATE TABLE IF NOT EXISTS tb_member (" +
                    " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    " name VARCHAR(64)," +
                    " createTime TIMESTAMP," +
                    " email_addr VARCHAR(128)," +
                    " age INTEGER," +
                    " active BOOLEAN," +
                    " score DECIMAL(10,2)," +
                    " updateTime TIMESTAMP)";

    /** tb_order 建表 DDL，列名与 OrderEntity 字段一致（camelCase，不区分大小写匹配） */
    public static final String CREATE_ORDER_TABLE =
            "CREATE TABLE IF NOT EXISTS tb_order (" +
                    " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    " memberId INTEGER," +
                    " orderNo VARCHAR(64)," +
                    " amount DECIMAL(10,2)," +
                    " status VARCHAR(16)," +
                    " createTime TIMESTAMP," +
                    " updateTime TIMESTAMP)";

    protected JdbcTemplate jdbcTemplate;

    protected EntityDao<MemberEntity, Integer> memberDao;

    protected EntityDao<OrderEntity, Integer> orderDao;

    @Before
    public void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(H2_URL, "sa", "");
        dataSource.setDriverClassName("org.h2.Driver");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(CREATE_MEMBER_TABLE);
        jdbcTemplate.execute(CREATE_ORDER_TABLE);
        // 每个测试前清空，保证用例隔离
        jdbcTemplate.update("DELETE FROM tb_member");
        jdbcTemplate.update("DELETE FROM tb_order");
        // 匿名子类携带具体泛型，供 EntityDaoImpl 构造器泛型解析；反射注入 jdbcTemplate
        EntityDaoImpl<MemberEntity, Integer> memberDaoImpl = new EntityDaoImpl<MemberEntity, Integer>() {
        };
        injectJdbcTemplate(memberDaoImpl);
        memberDao = memberDaoImpl;
        EntityDaoImpl<OrderEntity, Integer> orderDaoImpl = new EntityDaoImpl<OrderEntity, Integer>() {
        };
        injectJdbcTemplate(orderDaoImpl);
        orderDao = orderDaoImpl;
    }

    protected void injectJdbcTemplate(EntityDaoImpl<?, ?> dao) {
        try {
            Field field = EntityDaoImpl.class.getDeclaredField("jdbcTemplate");
            field.setAccessible(true);
            field.set(dao, jdbcTemplate);
        } catch (Exception e) {
            throw new GyjdbcException("注入 jdbcTemplate 失败", e);
        }
    }

    /**
     * 构造一条完整字段的测试数据。
     */
    protected static MemberEntity newMember(int id, String name, int age) {
        MemberEntity member = new MemberEntity();
        member.setId(id);
        member.setName(name);
        member.setAge(age);
        member.setActive(true);
        member.setScore(new java.math.BigDecimal("99.50"));
        member.setCreateTime(java.time.LocalDateTime.of(2026, 8, 12, 10, 30));
        member.setEmailAddr(name + "@example.com");
        member.setUpdateTime(new java.util.Date());
        return member;
    }

    /**
     * 构造一条订单测试数据。
     */
    protected static OrderEntity newOrder(int id, int memberId, String orderNo, String amount, String status) {
        OrderEntity order = new OrderEntity();
        order.setId(id);
        order.setMemberId(memberId);
        order.setOrderNo(orderNo);
        order.setAmount(new java.math.BigDecimal(amount));
        order.setStatus(status);
        order.setCreateTime(java.time.LocalDateTime.of(2026, 8, 12, 10, 30));
        order.setUpdateTime(new java.util.Date());
        return order;
    }
}
