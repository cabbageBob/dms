package net.htwater.sesame.dms.web.entity;

import lombok.Data;

@Data
public class AlterComment {
    private String table;
    private String column;
    private String comment;
    /**
     * 类型
     */
    private String genre;
    /**
     * 长度
     * mysql需要
     */
    private int length;

    /**
     * 小数点位数
     * mysql需要
     */
    private int scale;
}
