package net.htwater.sesame.dms.web.controller;

import core.exception.SqlExecuteException;
import core.sql.SqlExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Jokki
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(SqlExecuteException.class)
    @ResponseStatus(HttpStatus.OK)
    public SqlExecuteResult handleSqlExecuteException(SqlExecuteException e) {
        log.error(e.getMessage(), e);
        SqlExecuteResult sqlExecuteResult = new SqlExecuteResult();
        sqlExecuteResult.setSuccess(false);
        if (e.getMessage().startsWith("Duplicate entry")) {
            sqlExecuteResult.setMessage("唯一键冲突: " + e.getMessage());
        } else if (e.getMessage().startsWith("Data truncated for column") ||
                e.getMessage().startsWith("Incorrect decimal value")){
            sqlExecuteResult.setMessage("数据不合法: " + e.getMessage());
        } else if (e.getMessage().contains("cannot be null")) {
            sqlExecuteResult.setMessage("字段不能为空: " + e.getMessage());
        } else if (e.getMessage().startsWith("Data truncation: Data too long for column")) {
            sqlExecuteResult.setMessage("数据长度过长: " + e.getMessage());
        } else {
            sqlExecuteResult.setMessage(e.getMessage());
        }
        return sqlExecuteResult;
    }
}
