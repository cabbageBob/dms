package core.sql;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * SQL信息
 * @author Jokki
 */
@ToString
@EqualsAndHashCode
@Data
public class SqlInfo {

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
    private String type;

//    enum SqlType {
//        /** 查询语句 **/
//        SELECT,
//        /** 插入语句 **/
//        INSERT,
//        /** 更新语句 **/
//        UPDATE,
//        /** 删除语句 **/
//        DELETE,
//        /** 其它语句 **/
//        OTHER;
//
//        static SqlType fromSql(String sql) {
//            //
//            return SqlType.SELECT;
//        }
//    }

}
