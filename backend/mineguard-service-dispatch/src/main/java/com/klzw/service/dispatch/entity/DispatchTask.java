package com.klzw.service.dispatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispatch_task")
public class DispatchTask {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String taskNo;

    private Integer taskType;

    private Integer status;

    private Long vehicleId;

    private Long driverId;

    private String startLocation;

    private String endLocation;

    private LocalDateTime scheduledTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String priority;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}