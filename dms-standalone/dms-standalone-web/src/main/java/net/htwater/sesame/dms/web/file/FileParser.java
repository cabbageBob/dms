package net.htwater.sesame.dms.web.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileParser {
    /**
     * 从文件中获取所有字段名
     * @param inputStream
     * @param fileName
     * @return
     */
    List<Object> getFields(InputStream inputStream,String fileName);

    /**
     *解析文件
     * @param inputStream
     * @param fileName
     * @return
     */
    List<List<Object>> parseFile(InputStream inputStream,String fileName);
}
