package com.klzw.service.user.vo;

import com.klzw.service.user.entity.RoleChangeApply;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色变更申请VO
 */
@Data
public class RoleChangeApplyVO {

    /**
     * 申请ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 当前角色ID
     */
    private String currentRoleId;

    /**
     * 当前角色编码
     */
    private String currentRoleCode;

    /**
     * 当前角色名称
     */
    private String currentRoleName;

    /**
     * 申请角色ID
     */
    private String applyRoleId;

    /**
     * 申请角色编码
     */
    private String applyRoleCode;

    /**
     * 申请角色名称
     */
    private String applyRoleName;

    /**
     * 申请原因
     */
    private String applyReason;

    /**
     * 申请状态：1-待处理，2-已通过，3-已拒绝
     */
    private Integer status;

    /**
     * 状态标签
     */
    private String statusLabel;

    /**
     * 管理员处理意见
     */
    private String adminOpinion;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人ID
     */
    private String handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 从实体转换为VO
     * @param apply 角色变更申请实体
     * @return 角色变更申请VO
     */
    public static RoleChangeApplyVO fromEntity(RoleChangeApply apply) {
        if (apply == null) {
            return null;
        }
        RoleChangeApplyVO vo = new RoleChangeApplyVO();
        vo.setId(apply.getId() != null ? apply.getId().toString() : null);
        vo.setUserId(apply.getUserId() != null ? apply.getUserId().toString() : null);
        vo.setUsername(apply.getUsername());
        vo.setCurrentRoleId(apply.getCurrentRoleId() != null ? apply.getCurrentRoleId().toString() : null);
        vo.setCurrentRoleCode(apply.getCurrentRoleCode());
        vo.setCurrentRoleName(apply.getCurrentRoleName());
        vo.setApplyRoleId(apply.getApplyRoleId() != null ? apply.getApplyRoleId().toString() : null);
        vo.setApplyRoleCode(apply.getApplyRoleCode());
        vo.setApplyRoleName(apply.getApplyRoleName());
        vo.setApplyReason(apply.getApplyReason());
        vo.setStatus(apply.getStatus());
        vo.setStatusLabel(getStatusLabel(apply.getStatus()));
        vo.setAdminOpinion(apply.getAdminOpinion());
        vo.setHandleTime(apply.getHandleTime());
        vo.setHandlerId(apply.getHandlerId() != null ? apply.getHandlerId().toString() : null);
        vo.setHandlerName(apply.getHandlerName());
        vo.setCreateTime(apply.getCreateTime());
        return vo;
    }

    /**
     * 获取状态标签
     * @param status 状态
     * @return 状态标签
     */
    private static String getStatusLabel(Integer status) {
        switch (status) {
            case 1:
                return "待处理";
            case 2:
                return "已通过";
            case 3:
                return "已拒绝";
            default:
                return "未知";
        }
    }
}
