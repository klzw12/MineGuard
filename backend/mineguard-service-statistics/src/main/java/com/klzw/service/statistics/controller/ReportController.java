package com.klzw.service.statistics.controller;

import com.klzw.common.core.client.PythonClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/statistic/report")
public class ReportController {

    private final PythonClient pythonClient;

    @Autowired
    public ReportController(PythonClient pythonClient) {
        this.pythonClient = pythonClient;
    }

    @PostMapping("/export/statistics")
    public ResponseEntity<byte[]> exportStatistics(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出统计报表：{}", exportRequest);
        byte[] data = pythonClient.exportStatistics(exportRequest);
        String filename = generateFilename("statistics", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/trip-report")
    public ResponseEntity<byte[]> exportTripReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出行程报表：{}", exportRequest);
        byte[] data = pythonClient.exportTripReport(exportRequest);
        String filename = generateFilename("trip_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/cost-report")
    public ResponseEntity<byte[]> exportCostReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出成本报表：{}", exportRequest);
        byte[] data = pythonClient.exportCostReport(exportRequest);
        String filename = generateFilename("cost_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/vehicle-report")
    public ResponseEntity<byte[]> exportVehicleReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出车辆报表：{}", exportRequest);
        byte[] data = pythonClient.exportVehicleReport(exportRequest);
        String filename = generateFilename("vehicle_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    @PostMapping("/export/driver-report")
    public ResponseEntity<byte[]> exportDriverReport(@RequestBody Map<String, Object> exportRequest) {
        log.debug("导出司机报表：{}", exportRequest);
        byte[] data = pythonClient.exportDriverReport(exportRequest);
        String filename = generateFilename("driver_report", exportRequest);
        return buildExcelResponse(data, filename);
    }

    private String generateFilename(String prefix, Map<String, Object> exportRequest) {
        String filename = (String) exportRequest.get("filename");
        if (filename != null && !filename.isEmpty()) {
            return filename;
        }
        String format = (String) exportRequest.get("format");
        if (format == null || format.isEmpty()) {
            format = "xlsx";
        }
        return prefix + "_" + System.currentTimeMillis() + "." + format;
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, String filename) {
        if (data == null || data.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length));

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
