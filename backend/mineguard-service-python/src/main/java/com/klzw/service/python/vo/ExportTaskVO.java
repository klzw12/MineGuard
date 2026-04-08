package com.klzw.service.python.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskVO {
    private String taskId;
    private String status;
    private Integer progress;
    private String message;
    private String downloadUrl;
    private Long createTime;
    private Long finishTime;
}
