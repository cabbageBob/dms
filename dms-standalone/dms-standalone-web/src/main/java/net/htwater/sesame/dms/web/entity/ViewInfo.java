package net.htwater.sesame.dms.web.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ViewInfo {
    /**
     * 上传数量
     */
    private long uploadNum;
    private long lastUploadNum;


    /**
     * 下载数量
     */
    private long downloadNum;
    private long lastDownloadNum;

    /**
     * 更新数量
     */
    private long updateNum;
    private long lastUpdateNum;

    /**
     * 近7天的上传、下载、更新量
     */
    List<Map<Long,Long>> uploadRecords;
    List<Map<Long,Long>> downloadRecords;
    List<Map<Long,Long>> updateRecords;
}
