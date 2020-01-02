package net.htwater.sesame.dms.web.dialect;

import net.htwater.sesame.dms.web.util.JdbcUtil;

import javax.persistence.PersistenceException;
import java.sql.DatabaseMetaData;

/**
 * @author 10415
 */

public enum TypeDataBase {
    /**
     * sqlserver
     */
        SQLSERVER {
            @Override
            public Class<? extends TypeDialect> latestDialect() {
                return SQLServer2012Dialect.class;
            }

            @Override
            public TypeDialect resolveDialect(DatabaseMetaData metaData) {
                String databaseName = JdbcUtil.getDatabaseName(metaData);
                assert databaseName != null;
                if (databaseName.startsWith("Microsoft SQL Server")) {
                    int majorVersion = JdbcUtil.getDBMajorVersion(metaData);
                    switch (majorVersion) {
                        case 8:
                            return new SQLServerDialect();
                        case 9:
                            return new SQLServer2005Dialect();
                        case 10:
                            return new SQLServer2008Dialect();
                        case 11:
                        case 12:
                        case 13:
                            return new SQLServer2012Dialect();
                        default:
                            return (majorVersion < 8 ? new SQLServerDialect() : TypeDataBase.latestDialectInstance(this));
                    }
                } else {
                    return null;
                }
            }

            @Override
            public TypeDialect getParent() {
                return new  SQLServerDialect();
            }
        },
        MYSQL {
            @Override
            public Class<? extends TypeDialect> latestDialect() {
                return MySQL8Dialect.class;
            }

            @Override
            public TypeDialect resolveDialect(DatabaseMetaData metaData) {
                String databaseName = JdbcUtil.getDatabaseName(metaData);
                if ("MySQL".equals(databaseName)) {
                    int majorVersion = JdbcUtil.getDBMajorVersion(metaData);
                    int minorVersion = JdbcUtil.getDBMinorVersion(metaData);
                    if (majorVersion < 5) {
                        return new MySQLDialect();
                    } else if (majorVersion == 5) {
                        if (minorVersion < 5) {
                            return new MySQL5Dialect();
                        } else {
                            return (minorVersion < 7 ? new MySQL55Dialect() : new MySQL57Dialect());
                        }
                    } else {
                        return TypeDataBase.latestDialectInstance(this);
                    }
                } else {
                    return null;
                }
            }

            @Override
            public TypeDialect getParent() {
                return new MySQLDialect();
            }
        },
        ORACLE {
            @Override
            public Class<? extends TypeDialect> latestDialect(){
                return Oracle12cDialect.class;
            }

            @Override
            public TypeDialect resolveDialect(DatabaseMetaData metaData) {
                String databaseName = JdbcUtil.getDatabaseName(metaData);
                if ("Oracle".equals(databaseName)) {
                    int majorVersion = JdbcUtil.getDBMajorVersion(metaData);
                    switch (majorVersion) {
                        case 8:
                            return new Oracle8iDialect();
                        case 9:
                            return new Oracle9iDialect();
                        case 10:
                        case 11:
                            return new Oracle10gDialect();
                        case 12:
                            return new Oracle12cDialect();
                        default:
                            return TypeDataBase.latestDialectInstance(this);
                    }
                } else {
                    return null;
                }
            }

            @Override
            public TypeDialect getParent() {
                return new Oracle8iDialect();
            }
        };
    TypeDataBase() {
    }
    public abstract Class<? extends TypeDialect> latestDialect();
    public abstract TypeDialect resolveDialect(DatabaseMetaData metaData);
    public abstract TypeDialect getParent();
    private static TypeDialect latestDialectInstance(TypeDataBase database) {
        try {
            return database.latestDialect().newInstance();
        } catch (IllegalAccessException | InstantiationException var2) {
            throw new PersistenceException(var2);
        }
    }
}
