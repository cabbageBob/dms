package net.htwater.sesame.dms.web.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class FileUploadResponse implements Serializable {
    private static final long serialVersionUID = -2436718100662088239L;
    /**
     * 返回的消息
     */
    String msg;
    /**
    从文件中获取的字段名
     */
    List<Object> fields;
    /**
    数据库中文件对应的id
     */
    String fileId;

    String sheetName;
}
