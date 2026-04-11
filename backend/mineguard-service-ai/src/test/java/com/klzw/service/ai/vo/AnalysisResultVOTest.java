package com.klzw.service.ai.vo;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisResultVOTest {

    @Test
    void testBuilder() {
        Map<String, Object> content = Map.of("key", "value");
        Map<String, Object> analysis = Map.of("score", 85);
        Map<String, Object> cleaningReport = Map.of("removed", 2);

        AnalysisResultVO vo = AnalysisResultVO.builder()
                .status("success")
                .message("分析完成")
                .content(content)
                .analysis(analysis)
                .cleaningReport(cleaningReport)
                .build();

        assertEquals("success", vo.getStatus());
        assertEquals("分析完成", vo.getMessage());
        assertEquals(content, vo.getContent());
        assertEquals(analysis, vo.getAnalysis());
        assertEquals(cleaningReport, vo.getCleaningReport());
    }

    @Test
    void testSetterGetter() {
        AnalysisResultVO vo = new AnalysisResultVO();
        vo.setStatus("failed");
        vo.setMessage("分析失败");
        vo.setContent(Map.of("error", "timeout"));
        vo.setAnalysis(Map.of("driving_score", 60));
        vo.setCleaningReport(Map.of("quality_score", 0.8));

        assertEquals("failed", vo.getStatus());
        assertEquals("分析失败", vo.getMessage());
        assertEquals("timeout", vo.getContent().get("error"));
        assertEquals(60, vo.getAnalysis().get("driving_score"));
        assertEquals(0.8, vo.getCleaningReport().get("quality_score"));
    }

    @Test
    void testNoArgsConstructor() {
        AnalysisResultVO vo = new AnalysisResultVO();
        assertNull(vo.getStatus());
        assertNull(vo.getMessage());
        assertNull(vo.getContent());
        assertNull(vo.getAnalysis());
        assertNull(vo.getCleaningReport());
    }

    @Test
    void testAllArgsConstructor() {
        Map<String, Object> content = Map.of("result", "ok");
        AnalysisResultVO vo = new AnalysisResultVO("success", "完成", content, null, null);

        assertEquals("success", vo.getStatus());
        assertEquals("完成", vo.getMessage());
        assertEquals(content, vo.getContent());
        assertNull(vo.getAnalysis());
        assertNull(vo.getCleaningReport());
    }
}
