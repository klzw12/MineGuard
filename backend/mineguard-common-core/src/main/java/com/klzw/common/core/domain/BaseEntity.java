package com.klzw.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer createBy;
    private Integer updateBy;
    private Integer delFlag;
}
