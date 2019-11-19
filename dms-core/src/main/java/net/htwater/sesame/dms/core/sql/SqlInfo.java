package net.htwater.sesame.dms.core.sql;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * SQL信息
 * @author Jokki
 */
@ToString
@EqualsAndHashCode
@Getter
public class SqlInfo {

    private SqlInfo(String sql, String datasourceId) {
        this.sql = sql;
        this.datasourceId = datasourceId;
    }

    public static SqlInfo of(String sql, String datasourceId) {
        return new SqlInfo(sql, datasourceId);
    }
    /**
     * SQL
     */
    private String sql;
    /**
     * 数据库唯一ID
     */
    private String datasourceId;
    /**
     * SQL类型
     */
    private SqlType sqlType;

    enum SqlType {
        /** 查询语句 **/
        SELECT,
        /** 插入语句 **/
        INSERT,
        /** 更新语句 **/
        UPDATE,
        /** 删除语句 **/
        DELETE,
        /** 其它语句 **/
        OTHER;

        static SqlType fromSql(String sql) {
            //
            return SqlType.SELECT;
        }
    }

}
