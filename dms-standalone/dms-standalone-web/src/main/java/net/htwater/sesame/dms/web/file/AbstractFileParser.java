package net.htwater.sesame.dms.web.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 10415
 */
public abstract class AbstractFileParser implements FileParser {
    public AbstractFileParser(FileType... fileType){
        supportFile.addAll(Arrays.asList(fileType));
    }
    private Set<FileType> supportFile = new HashSet<>();
    public boolean isSupport(FileType type) {
        return supportFile.contains(type);
    }
    public AbstractFileParser get(){return this ;}
}
