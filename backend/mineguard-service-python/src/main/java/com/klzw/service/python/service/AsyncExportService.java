package com.klzw.service.python.service;

import com.klzw.service.python.vo.ExportTaskVO;

import java.util.Map;

public interface AsyncExportService {
    
    String submitExportTask(String exportType, Map<String, Object> exportRequest);
    
    ExportTaskVO getTaskStatus(String taskId);
    
    byte[] getTaskResult(String taskId);
    
    void cancelTask(String taskId);
}
