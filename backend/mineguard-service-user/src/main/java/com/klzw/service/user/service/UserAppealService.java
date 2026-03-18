package com.klzw.service.user.service;

import com.klzw.service.user.dto.HandleAppealDTO;
import com.klzw.service.user.dto.UserAppealDTO;
import com.klzw.service.user.vo.UserAppealVO;

import java.util.List;

/**
 * 用户申诉服务接口
 */
public interface UserAppealService {

    /**
     * 创建申诉
     *
     * @param userId 用户ID
     * @param dto    申诉DTO
     * @return 申诉ID
     */
    String createAppeal(Long userId, UserAppealDTO dto);

    /**
     * 根据ID查询申诉
     *
     * @param id 申诉ID
     * @return 申诉信息
     */
    UserAppealVO getAppealById(Long id);

    /**
     * 根据用户ID查询申诉列表
     *
     * @param userId 用户ID
     * @return 申诉列表
     */
    List<UserAppealVO> getAppealsByUserId(Long userId);

    /**
     * 查询待处理的申诉列表
     *
     * @return 申诉列表
     */
    List<UserAppealVO> getPendingAppeals();

    /**
     * 查询所有申诉列表
     *
     * @return 申诉列表
     */
    List<UserAppealVO> getAllAppeals();

    /**
     * 处理申诉
     *
     * @param id  申诉ID
     * @param dto 处理DTO
     * @return 是否成功
     */
    boolean handleAppeal(Long id, HandleAppealDTO dto);

    /**
     * 检查用户是否有待处理的申诉
     *
     * @param userId 用户ID
     * @return true-有待处理申诉，false-无
     */
    boolean hasPendingAppeal(Long userId);
}
