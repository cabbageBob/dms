package net.htwater.sesame.dms.web.dataSource;

import org.hswebframework.ezorm.core.ObjectWrapper;
import org.hswebframework.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.EmptySQL;
import org.hswebframework.ezorm.rdb.executor.SQL;
import org.hswebframework.ezorm.rdb.render.support.simple.SimpleSQL;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jokki
 */

public class DefaultJdbcExecutor extends AbstractJdbcSqlExecutor {

    protected String getDatasourceId() {
        String id = DataSourceHolder.switcher().currentDataSourceId();
        return id == null ? "default" : id;
    }

    @Override
    public Connection getConnection() {
        DataSource dataSource = DataSourceHolder.currentDataSource().getNative();
            Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
        if (logger.isDebugEnabled()) {
            logger.debug("DataSource ({}) JDBC Connection [{}] will {}be managed by Spring", getDatasourceId(), connection, (isConnectionTransactional ? "" : "not "));
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing DataSource ({}) JDBC Connection [{}]", getDatasourceId(), connection);
        }
        try {
            DataSourceUtils.doReleaseConnection(connection, DataSourceHolder.currentDataSource().getNative());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            try {
                connection.close();
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
    }

    public int executeSQLBatch(List<String> sqlList) throws SQLException {
        Connection conn = getConnection();
        Statement statement = null;
        int[] i;
        try {
            conn.setAutoCommit(false);
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
            for (String sql : sqlList){
                statement.addBatch(sql);
            }
            i = statement.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            closeStatement(statement);
            releaseConnection(conn);
        }
        int result = 0;
        for (int num : i) {
            result += num;
        }
        return result;
    }

    public int executeSQLBatch(String sql, List<List<Object>> objects) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement statement = null;
        int count = 0;
        try {
            conn.setAutoCommit(false);
            for (List<Object> args : objects) {
                statement = conn.prepareStatement(sql);
                if (args != null) {
                    int i = 1;
                    for (Object obj : args) {
                        if (obj instanceof Date) {
                            statement.setTimestamp(i, new Timestamp(((Date) obj).getTime()));
                        } else {
                            statement.setObject(i, obj);
                        }
                        i++;
                    }
                }
                count += statement.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            closeStatement(statement);
            releaseConnection(conn);
        }
        return count;
    }

    public <T> List<T> list(String sql, ObjectWrapper<T> wrapper,int queryTimeOut) throws SQLException {
        return this.list(new SimpleSQL(sql),wrapper,queryTimeOut);
    }

    public <T> List<T> list(SQL sql, ObjectWrapper<T> wrapper,int queryTimeOut) throws SQLException {
        if (sql instanceof EmptySQL) {
            return new ArrayList<>();
        } else {
            AbstractJdbcSqlExecutor.SQLInfo info = this.compileSql(sql);
            printSql(info);
            Connection connection = this.getConnection();
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            List<T> datas = new ArrayList<>();

            try {
                statement = connection.prepareStatement(info.getSql());
                statement.setQueryTimeout(queryTimeOut);
                this.preparedParam(statement, info);
                resultSet = statement.executeQuery();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int count = metaData.getColumnCount();
                List<String> headers = new ArrayList<>();
                int index = 1;

                while(true) {
                    if (index > count) {
                        wrapper.setUp(headers);
                        index = 0;

                        while(resultSet.next()) {
                            T data = wrapper.newInstance();

                            for(int i = 0; i < headers.size(); ++i) {
                                Object value = resultSet.getObject(i + 1);
                                wrapper.wrapper(data, index, headers.get(i), value);
                            }

                            ++index;
                            if (wrapper.done(data)) {
                                datas.add(data);
                            }
                        }

                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("<==      total: {}", index);
                        }
                        break;
                    }

                    headers.add(metaData.getColumnLabel(index));
                    ++index;
                }
            } finally {
                this.closeResultSet(resultSet);
                this.closeStatement(statement);
                this.releaseConnection(connection);
            }

            return datas;
        }
    }

}
