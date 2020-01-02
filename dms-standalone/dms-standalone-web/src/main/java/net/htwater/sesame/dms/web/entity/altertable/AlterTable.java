package net.htwater.sesame.dms.web.entity.altertable;

import lombok.Data;

import java.util.List;
@Data
public class AlterTable {
    private String tableName;
    private String oldTableName;
    private String comment;
    private String character;
    private String collate;
    private List<AlterColumn> columns;
}
