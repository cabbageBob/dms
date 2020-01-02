package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Jokki
 */
@Getter
@Setter
@AllArgsConstructor
public class BatchSql {
    private String sql;

    private List<IndexAndColumnClass> objects;
}
