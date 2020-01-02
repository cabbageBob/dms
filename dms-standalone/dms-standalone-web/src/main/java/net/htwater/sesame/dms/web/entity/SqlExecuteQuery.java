package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Jokki
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SqlExecuteQuery {
    private String sqlLines;

    private int pageIndex;

    private int pageSize;
}
