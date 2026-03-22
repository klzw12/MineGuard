package com.klzw.service.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultVO {

    private String status;

    private String message;

    private Map<String, Object> content;

    private Map<String, Object> analysis;

    private Map<String, Object> cleaningReport;
}
