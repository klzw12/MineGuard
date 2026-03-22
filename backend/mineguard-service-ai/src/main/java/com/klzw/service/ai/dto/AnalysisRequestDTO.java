package com.klzw.service.ai.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AnalysisRequestDTO {

    private Map<String, Object> data;

    private String analysisType;

    private Map<String, Object> parameters;
}
