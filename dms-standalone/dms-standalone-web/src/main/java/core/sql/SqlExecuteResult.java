package core.sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * sql执行结果
 * @author Jokki
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteResult {

    private SqlInfo sqlInfo;

    private Object result;

    private String message;

    private boolean success;
}
