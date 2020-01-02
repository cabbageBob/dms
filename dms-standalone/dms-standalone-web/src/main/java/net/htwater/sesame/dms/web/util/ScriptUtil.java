package net.htwater.sesame.dms.web.util;

import core.meta.table.ColumnMetadata;
import core.meta.table.TableMetadata;
import net.htwater.sesame.dms.web.entity.altertable.AlterColumn;
import net.htwater.sesame.dms.web.entity.altertable.AlterTable;

import java.util.ArrayList;
import java.util.List;

public class ScriptUtil {
    private ScriptUtil(){
    }
    public static String mySqlAlterTableScript(TableMetadata tableMetadata,AlterTable alterTable){
        List<ColumnMetadata> columnsInDB = tableMetadata.getColumns();
        List<String> primaryKeys = new ArrayList<>();
        boolean iskeyChanged =false;
        StringBuilder builder = new StringBuilder("ALTER TABLE `");
        builder.append(alterTable.getOldTableName());
        builder.append("`\n");
        if (alterTable.getTableName()!=null){
            builder.append("RENAME TO `");
            builder.append(alterTable.getTableName());
            builder.append("`,\n");
        }
        if (alterTable.getComment()!=null){
            builder.append("COMMENT='");
            builder.append(alterTable.getComment());
            builder.append("',\n");
        }
        if (alterTable.getCharacter()!=null){
            builder.append("DEFAULT CHARACTER SET=");
            builder.append(alterTable.getCharacter());
            builder.append(",\n");
        }
        if (alterTable.getCollate()!=null){
            builder.append("COLLATE=");
            builder.append(alterTable.getCollate());
            builder.append(",\n");
        }
        for (ColumnMetadata columnMetadataInDB:columnsInDB){
            String name =columnMetadataInDB.getName() ;
            int primaryKey =columnMetadataInDB.getIsPrimary();
            for (AlterColumn entity:alterTable.getColumns()){
                if (name.equals(entity.getOldName())){
                    if (!(entity.getName().equals(entity.getOldName()))){
                        builder.append("CHANGE COLUMN `");
                        builder.append(name);
                        builder.append("` ");
                        builder.append("`");
                        builder.append(entity.getName());
                    }else {
                        builder.append("MODIFY COLUMN `");
                        builder.append(name);
                    }
                    builder.append("` ");
                    builder.append(entity.getDataType());
                    if (entity.getLength()!=0){
                        builder.append("(");
                        builder.append(entity.getLength());
                        if (entity.getScale()!=0){
                            builder.append(",");
                            builder.append(entity.getScale());
                        }
                        builder.append(")");
                    }
                    if (entity.isNotNull()){
                        builder.append(" NOT NULL ");
                    }
                    if (entity.getComment()!=null){
                        builder.append(" COMMENT ");
                        builder.append("‘");
                        builder.append(entity.getComment());
                        builder.append("’");
                    }
                    if (entity.getIsPrimary()!=primaryKey){
                        iskeyChanged=true;
                        primaryKeys.add(entity.getName());
                    }
                    builder.append(",\n");
                }
            }
        }
        if (iskeyChanged){
            builder.append("DROP PRIMARY KEY,\n");
            builder.append("ADD PRIMARY KEY(");
            for (int i=0;i<primaryKeys.size();i++){
                builder.append("`");
                builder.append(primaryKeys.get(i));
                builder.append("`");
                if (i<primaryKeys.size()-1){
                    builder.append(",");
                }
            }
            builder.append(")");
        }
        String result = builder.toString();
        if (result.endsWith(",")){
            result = result.substring(0,result.length()-1);
        }else if (result.endsWith(",\n")){
            result =result.substring(0,result.length()-2);
        }
        return result;
    }

    public static String sqlServerAlterTableScript(TableMetadata tableMetadata, AlterTable alterTable){
        List<ColumnMetadata> columnsInDB = tableMetadata.getColumns();
        List<String> primaryKeys = new ArrayList<>();
        boolean iskeyChanged =false;
        StringBuilder builder = new StringBuilder("BEGIN TRANSACTION\nGO\n");
        if (alterTable.getTableName()!=null&&!alterTable.getTableName().equals(tableMetadata.getName())){
            builder.append("EXEC sp_rename '");
            builder.append(alterTable.getOldTableName());
            builder.append("', '");
            builder.append(alterTable.getTableName());
            builder.append("' ;\nGO\n");

        }
        if (alterTable.getComment()!=null&&!alterTable.getComment().equals(tableMetadata.getComment())){
            if (tableMetadata.getComment()==null){
                builder.append("EXEC sp_addextendedproperty ");
            }else {
                builder.append("EXEC sp_updateextendedproperty");
            }
            builder.append("  N'MS_Description', N'");
            builder.append(alterTable.getComment());
            builder.append("', ");
            builder.append("N'SCHEMA', N'dbo', N'TABLE', N'");
            builder.append(alterTable.getTableName());
            builder.append("', NULL, NULL; \nGO\n");
        }
        for (ColumnMetadata columnMetadataInDB:columnsInDB){
            String name =columnMetadataInDB.getName() ;
            String comment = columnMetadataInDB.getComment();
            String dataType = columnMetadataInDB.getDataType();
            int length = columnMetadataInDB.getLength();
            int scale = columnMetadataInDB.getScale();
            int primaryKey =columnMetadataInDB.getIsPrimary();
            for (AlterColumn entity:alterTable.getColumns()){
                if (name.equals(entity.getOldName())){
                    if (!(entity.getName().equals(entity.getOldName()))){
                        if (entity.getIsPrimary()!=0){
                            dropPKStringForMSSQL(builder,alterTable);
                            iskeyChanged = true;
                        }
                        builder.append("EXEC sp_rename N'");
                        builder.append(alterTable.getTableName());
                        builder.append(".");
                        builder.append(entity.getOldName());
                        builder.append("', ");
                        builder.append("N'");
                        builder.append(entity.getName());
                        builder.append("'\nGO\n");
                    }
                    if (entity.getComment()!=null&&!entity.getComment().equals(comment)){
                        if (entity.getComment()==null){
                            builder.append("EXEC sp_addextendedproperty ");
                        }else {
                            builder.append("EXEC sp_updateextendedproperty");
                        }
                        builder.append("  N'MS_Description', N'");
                        builder.append(entity.getComment());
                        builder.append("', ");
                    builder.append("N'SCHEMA', N'dbo', N'TABLE', N'");
                    builder.append(alterTable.getTableName());
                        builder.append("', 'COLUMN', N'");
                        builder.append(entity.getName());
                        builder.append("'; \nGO\n");
                    }
                    if (!entity.getDataType().equals(dataType)||entity.getLength()!=length||entity.getScale()!=scale){
                        builder.append("ALTER TABLE ");
                        builder.append(alterTable.getTableName());
                        builder.append(" ALTER COLUMN ");
                        builder.append(entity.getName());
                        builder.append(" ");
                        builder.append(entity.getDataType());
                        if (entity.getLength()!=0){
                            builder.append("(");
                            builder.append(entity.getLength());
                            if (entity.getScale()!=0){
                                builder.append(",");
                                builder.append(entity.getScale());
                            }
                            builder.append(")");
                        }
                        if (entity.isNotNull()){
                            builder.append(" NOT NULL ");
                        }else {
                            builder.append(" NULL");
                        }
                        builder.append("\nGO\n");
                    }
                    if (entity.getIsPrimary()!=primaryKey&&entity.getIsPrimary()!=0){
                        iskeyChanged=true;
                        primaryKeys.add(entity.getName());
                    }
                }
            }
        }
        if (iskeyChanged){
            dropPKStringForMSSQL(builder,alterTable);
            builder.append("ALTER TABLE ");
            builder.append(alterTable.getTableName());
            builder.append(" ADD PRIMARY KEY ");
            builder.append("(");
            for (int i=0;i<primaryKeys.size();i++){
                builder.append(primaryKeys.get(i));
                if (i<primaryKeys.size()-1){
                    builder.append(",");
                }
            }
            builder.append(");\nGO\n");
        }
        builder.append("COMMIT");
        return builder.toString();
    }

    public static String oracleAlterTableScript(TableMetadata tableMetadata, AlterTable alterTable){
        List<ColumnMetadata> columnsInDB = tableMetadata.getColumns();
        List<String> primaryKeys = new ArrayList<>();
        boolean iskeyChanged =false;
        String newTableName = alterTable.getTableName();
        String newTableComment = alterTable.getComment();
        StringBuilder builder = new StringBuilder("ALTER TABLE `");
        builder.append(alterTable.getOldTableName());
        builder.append("`\n");
        if (newTableName!=null&&!newTableName.equals(tableMetadata.getName())){
            builder.append("RENAME TO `");
            builder.append(newTableName);
            builder.append("`;\n");
        }
        if (newTableComment!=null&&!newTableComment.equals(tableMetadata.getComment())){
            builder.append("COMMENT  ON TABLE ");
            builder.append(newTableName);
            builder.append(" is '");
            builder.append(alterTable.getComment());
            builder.append("';\n");
        }
        for (ColumnMetadata columnMetadataInDB:columnsInDB){
            for (AlterColumn entity:alterTable.getColumns()){
                String name = columnMetadataInDB.getName();
                if (name.equals(entity.getOldName())){
                    int primaryKey =columnMetadataInDB.getIsPrimary();
                    int length = columnMetadataInDB.getLength();
                    int scale = columnMetadataInDB.getScale();
                    boolean isNotNull = columnMetadataInDB.isNotNull();
                    String comment = columnMetadataInDB.getComment();
                    if (!(entity.getName().equals(entity.getOldName()))){
                        builder.append("ALTER TABLE ");
                        builder.append(newTableName);
                        builder.append(" REMANE COLUMN ");
                        builder.append(name);
                        builder.append(" to ");
                        builder.append(entity.getName());
                        builder.append(" ;\n");
                    }
                    if (entity.getLength()!=0&&entity.getLength()!=length){
                        builder.append("ALTER TABLE ");
                        builder.append(newTableName);
                        builder.append(" MODIFY ");
                        builder.append(entity.getName());
                        builder.append(" ");
                        builder.append(entity.getDataType());
                        builder.append(" (");
                        builder.append(entity.getLength());
                        if (entity.getScale()!=0&&entity.getScale()!=scale){
                            builder.append(".");
                            builder.append(scale);
                        }
                        builder.append(");");
                    }
                    if (entity.isNotNull()&&!isNotNull){
                        builder.append("ALTER TABLE ");
                        builder.append(newTableName);
                        builder.append(" MODIFY ");
                        builder.append(entity.getName());
                        builder.append(" NOT NULL ;");
                    }
                    if (entity.getComment()!=null&&!entity.getComment().equals(comment)){
                        builder.append(" COMMENT ON COLUMN ");
                        builder.append(newTableName);
                        builder.append(".");
                        builder.append(entity.getName());
                        builder.append(" is ");
                        builder.append("'");
                        builder.append(entity.getComment());
                        builder.append("';");
                    }
                    if (entity.getIsPrimary()!=primaryKey){
                        iskeyChanged=true;
                        primaryKeys.add(entity.getName());
                    }
                    builder.append(",\n");
                }
            }
        }
        if (iskeyChanged){
            builder.append("ALTER TABLE ");
            builder.append(newTableName);
            builder.append("DROP PRIMARY KEY,\n");
            builder.append("ALTER TABLE ");
            builder.append(newTableName);
            builder.append("ADD PRIMARY KEY(");
            for (int i=0;i<primaryKeys.size();i++){
                builder.append("`");
                builder.append(primaryKeys.get(i));
                builder.append("`");
                if (i<primaryKeys.size()-1){
                    builder.append(",");
                }
            }
            builder.append(")");
        }
        String result = builder.toString();
        if (result.endsWith(",")){
            result = result.substring(0,result.length()-1);
        }else if (result.endsWith(",\n")){
            result =result.substring(0,result.length()-2);
        }
        return result;
    }

    private static void dropPKStringForMSSQL(StringBuilder builder, AlterTable alterTable){
        builder.append("DECLARE @NAME SYSNAME\nDECLARE @TB_NAME SYSNAME\n");
        builder.append("SET @TB_NAME = '");
        builder.append(alterTable.getTableName());
        builder.append("'\nSELECT TOP 1 @NAME = NAME FROM SYS.OBJECTS WITH(NOLOCK)\n");
        builder.append("WHERE TYPE_DESC ='PRIMARY_KEY_CONSTRAINT'\n");
        builder.append("AND PARENT_OBJECT_ID = (SELECT OBJECT_ID\n");
        builder.append("FROM SYS.OBJECTS WITH(NOLOCK)\n");
        builder.append("WHERE NAME = @TB_NAME )\n");
        builder.append("DECLARE @ALTERSQL NVARCHAR(MAX)\n");
        builder.append("SET @ALTERSQL=N'ALTER TABLE '+@TB_NAME+' DROP CONSTRAINT ['+@NAME+']'\n");
        builder.append("EXEC SP_EXECUTESQL @ALTERSQL;\nGO\n");
    }

}
