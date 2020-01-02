package net.htwater.sesame.dms.web.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jokki
 */
public class JdbcUtilTest {
    @Test
    public void testConncet() throws Exception {
        Assert.assertTrue(JdbcUtil.testConncet("MYSQL", "172.16.35.52", "3306", "htbus", "htbus",
                "bus@htwater"));
        Assert.assertTrue(JdbcUtil.testConncet("SQLSERVER", "172.16.35.13", "1433", "test", "test",
                "test"));
        Assert.assertTrue(JdbcUtil.testConncet("ORACLE", "172.16.35.51", "1521", "orcl", "user_nj",
                "htwater"));
    }

}