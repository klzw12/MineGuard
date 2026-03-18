package com.klzw.service.user.vo;

import com.klzw.service.user.entity.UserAppeal;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户申诉VO
 * <p>
 * 用于返回用户申诉信息
 */
@Data
public class UserAppealVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申诉ID
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
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 申诉原因
     */
    private String appealReason;

    /**
     * 申诉状态：1-待处理，2-已通过，3-已驳回，4-已驳回并删除账号
     */
    private Integer status;

    /**
     * 申诉状态标签
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
     */
    public static UserAppealVO fromEntity(UserAppeal appeal) {
        if (appeal == null) {
            return null;
        }
        UserAppealVO vo = new UserAppealVO();
        vo.setId(appeal.getId() != null ? appeal.getId().toString() : null);
        vo.setUserId(appeal.getUserId() != null ? appeal.getUserId().toString() : null);
        vo.setUsername(appeal.getUsername());
        vo.setRealName(appeal.getRealName());
        vo.setPhone(appeal.getPhone());
        vo.setAppealReason(appeal.getAppealReason());
        vo.setStatus(appeal.getStatus());
        vo.setStatusLabel(getStatusLabel(appeal.getStatus()));
        vo.setAdminOpinion(appeal.getAdminOpinion());
        vo.setHandleTime(appeal.getHandleTime());
        vo.setHandlerId(appeal.getHandlerId() != null ? appeal.getHandlerId().toString() : null);
        vo.setHandlerName(appeal.getHandlerName());
        vo.setCreateTime(appeal.getCreateTime());
        return vo;
    }

    /**
     * 获取状态标签
     */
    private static String getStatusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待处理";
            case 2:
                return "已通过";
            case 3:
                return "已驳回";
            case 4:
                return "已驳回并删除账号";
            default:
                return "未知";
        }
    }
}
