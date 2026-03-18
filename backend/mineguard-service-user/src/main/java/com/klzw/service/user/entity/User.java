package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String realName;

    private String idCard;

    private Integer gender;

    private String nation;

    private String birthDate;

    private String address;

    private String phone;

    private String email;

    private String avatarUrl;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private Integer status;

    private Long roleId;

    public UserStatusEnum getStatusEnum() {
        return UserStatusEnum.getByValue(status);
    }

    public void setStatusEnum(UserStatusEnum statusEnum) {
        this.status = statusEnum != null ? statusEnum.getValue() : null;
    }
}
