package net.htwater.sesame.dms.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author jokki
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "file_info")
public class FileInfo {

    @Id
    String id;
    String filename;
    String filepath;
    public FileInfo(String fileName,String filePath){
        this.id = id;
        this.filename=fileName;
        this.filepath=filePath;
    }
}
