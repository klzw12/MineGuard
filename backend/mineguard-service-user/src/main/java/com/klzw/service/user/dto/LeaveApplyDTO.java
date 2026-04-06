package com.klzw.service.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveApplyDTO {
    /**
     * 请假类型：1-事假 2-病假 3-年假 4-调休
     */
    private Integer leaveType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 请假原因
     */
    private String reason;
}
