package net.htwater.sesame.dms.web.entity;

import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @author Jokki
 */
@Getter
@Setter
@ToString
public class UpdatedDataSourceConfig {
    @NotNull
    private String id;
    @NotNull
    private String ip;
    private String port;
    @ApiParam("数据库名")
    @NotNull
    private String dbname;
    @NotNull
    private String name;
    @NotNull
    private String username;
    @ApiParam("密码")
    @NotNull
    private String password;
    @ApiParam("数据库类型")
    @NotNull
    private String dbtype;
    private String describe;
    private String databaseGenre;
    private int isUpdatePassword;
}
