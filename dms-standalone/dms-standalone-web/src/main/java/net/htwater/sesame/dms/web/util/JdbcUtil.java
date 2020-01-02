package net.htwater.sesame.dms.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUtil.class);

    private JdbcUtil() {
    }

    public static String parseUrl(String dbtype,String ip,String port,String name){
        switch (dbtype.toLowerCase()){
            case "mysql" :
                if (port == null){
                    return "jdbc:mysql://"+ip+":3306/"+name+"?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&serverTimezone=Asia/Shanghai";

                }
                return "jdbc:mysql://"+ip+":"+port+"/"+name+"?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&serverTimezone=Asia/Shanghai";
            case "sqlserver" :
                if (port == null){
                    return "jdbc:sqlserver://"+ip+":1433;databaseName="+name;
                }
                return "jdbc:sqlserver://"+ip+":"+port+";databaseName="+name;
            case "oracle" :
                if (port == null){
                    return "jdbc:oracle:thin:@"+ip+":1521/"+name;
                }
                return "jdbc:oracle:thin:@"+ip+":"+port+"/"+name;
            default:
                throw new IllegalArgumentException("jdbcUrl不合法");
        }
    }

    public static boolean testConncet(String dbtype, String ip,String port,String dbname,String username,String password){
        boolean result = false;
        Connection connection = null;
        try {
            DriverManager.setLoginTimeout(10);
            Properties properties = new Properties();
            properties.put("autoReconnect", "false");
            properties.put("autoReconnectForPools", false);
            properties.put("user", username);
            properties.put("password", password);
            connection = DriverManager.getConnection(parseUrl(dbtype, ip, port, dbname), properties);
            result = true;
        } catch (SQLException e) {
            LOGGER.error("测试数据库连接失败: ", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("测试数据库连接失败: ", e);
                }
            }
        }
        return result;
    }

    public static int getDBMajorVersion(String dbtype, String ip, String port, String dbname, String username, String password){
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(JdbcUtil.parseUrl(dbtype,ip,port,dbname));
        dataSourceProperties.setUsername(username);
        dataSourceProperties.setPassword(password);
        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().build();
        Connection conn = null;
        try {
            dataSource.setLoginTimeout(20);
            conn =dataSource.getConnection();
            return conn.getMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            close(conn,dataSource);
        }
        return 0;
    }

    public static String getDatabaseName(String dbtype, String ip, String port, String dbname, String username, String password){
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(JdbcUtil.parseUrl(dbtype,ip,port,dbname));
        dataSourceProperties.setUsername(username);
        dataSourceProperties.setPassword(password);
        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().build();
        Connection conn = null;
        try {
            dataSource.setLoginTimeout(20);
            conn =dataSource.getConnection();
            return conn.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
           close(conn,dataSource);
        }
        return null;
    }

    public static DatabaseMetaData getDatabaseMetaData(DataSource dataSource){
        Connection conn = null;
        try {
            conn =dataSource.getConnection();
            return conn.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            close(conn,dataSource);
        }
        return null;
    }
    public static String getDatabaseName(DatabaseMetaData metaData){
        try {
            return metaData.getDatabaseProductName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int getDBMajorVersion(DatabaseMetaData metaData){
        try {
            return metaData.getDatabaseMajorVersion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getDBMinorVersion(DatabaseMetaData metaData){
        try {
            return metaData.getDatabaseMinorVersion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void close(Connection connection,DataSource dataSource){
        try {
            DataSourceUtils.doReleaseConnection(connection,dataSource);
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
