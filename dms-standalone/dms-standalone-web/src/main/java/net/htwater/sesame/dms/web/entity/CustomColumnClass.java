package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;

/**
 * @author Jokki
 */
@Data
public class CustomColumnClass {
    private String id;

    private String datasourceId;

    private String table;

    private String column;

    private ColumnClass columnClass;

    private LinkedHashSet<Value> values;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Value {
        Object v;

        String description;
    }
}
