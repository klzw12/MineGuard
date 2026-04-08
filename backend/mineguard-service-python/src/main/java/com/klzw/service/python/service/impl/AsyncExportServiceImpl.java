package com.klzw.service.python.service.impl;

import com.klzw.service.python.service.AsyncExportService;
import com.klzw.service.python.service.PythonService;
import com.klzw.service.python.vo.ExportTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncExportServiceImpl implements AsyncExportService {

    private final PythonService pythonService;
    
    private final Map<String, ExportTaskVO> taskStore = new ConcurrentHashMap<>();
    private final Map<String, byte[]> resultStore = new ConcurrentHashMap<>();

    @Override
    public String submitExportTask(String exportType, Map<String, Object> exportRequest) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        
        ExportTaskVO task = ExportTaskVO.builder()
                .taskId(taskId)
                .status("PENDING")
                .progress(0)
                .message("任务已提交，等待处理")
                .createTime(System.currentTimeMillis())
                .build();
        
        taskStore.put(taskId, task);
        
        executeExportAsync(taskId, exportType, exportRequest);
        
        log.info("导出任务已提交：taskId={}, type={}", taskId, exportType);
        return taskId;
    }

    @Async
    public void executeExportAsync(String taskId, String exportType, Map<String, Object> exportRequest) {
        ExportTaskVO task = taskStore.get(taskId);
        if (task == null) {
            return;
        }
        
        try {
            task.setStatus("PROCESSING");
            task.setProgress(10);
            task.setMessage("正在导出数据...");
            taskStore.put(taskId, task);
            
            byte[] result = doExport(exportType, exportRequest);
            
            task.setProgress(90);
            task.setMessage("正在生成文件...");
            taskStore.put(taskId, task);
            
            resultStore.put(taskId, result);
            
            task.setStatus("COMPLETED");
            task.setProgress(100);
            task.setMessage("导出完成");
            task.setDownloadUrl("/python/export/result/" + taskId);
            task.setFinishTime(System.currentTimeMillis());
            taskStore.put(taskId, task);
            
            log.info("导出任务完成：taskId={}, size={}bytes", taskId, result.length);
            
        } catch (Exception e) {
            log.error("导出任务失败：taskId={}, error={}", taskId, e.getMessage(), e);
            
            task.setStatus("FAILED");
            task.setProgress(0);
            task.setMessage("导出失败: " + e.getMessage());
            task.setFinishTime(System.currentTimeMillis());
            taskStore.put(taskId, task);
        }
    }

    private byte[] doExport(String exportType, Map<String, Object> exportRequest) {
        switch (exportType) {
            case "statistics":
                return pythonService.exportStatistics(exportRequest);
            case "trip":
                return pythonService.exportTripReport(exportRequest);
            case "cost":
                return pythonService.exportCostReport(exportRequest);
            case "vehicle":
                return pythonService.exportVehicleReport(exportRequest);
            case "driver":
                return pythonService.exportDriverReport(exportRequest);
            default:
                throw new IllegalArgumentException("不支持的导出类型: " + exportType);
        }
    }

    @Override
    public ExportTaskVO getTaskStatus(String taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public byte[] getTaskResult(String taskId) {
        ExportTaskVO task = taskStore.get(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new RuntimeException("任务未完成: " + task.getStatus());
        }
        return resultStore.get(taskId);
    }

    @Override
    public void cancelTask(String taskId) {
        ExportTaskVO task = taskStore.get(taskId);
        if (task != null && ("PENDING".equals(task.getStatus()) || "PROCESSING".equals(task.getStatus()))) {
            task.setStatus("CANCELLED");
            task.setMessage("任务已取消");
            task.setFinishTime(System.currentTimeMillis());
            taskStore.put(taskId, task);
            resultStore.remove(taskId);
            log.info("导出任务已取消：taskId={}", taskId);
        }
    }
}
