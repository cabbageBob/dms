package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.FileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jokki
 */
@Repository
public interface FileInfoRepository extends MongoRepository<FileInfo, String> {
}
