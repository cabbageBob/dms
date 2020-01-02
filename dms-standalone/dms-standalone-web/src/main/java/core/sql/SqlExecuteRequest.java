package core.sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * sql执行请求
 * @author Jokki
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteRequest {

    private List<SqlInfo> sql;
}
