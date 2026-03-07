package com.klzw.service.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignRoleDTO {

    @NotEmpty(message = "角色ID列表不能为空")
    private List<String> roleIds;
}
