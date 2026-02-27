package com.klzw.common.database.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.constant.PaginationConstants;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;

import java.util.Collections;
import java.util.List;

/**
 * 分页工具类
 * 实现 common-core 分页对象与 MyBatis-Plus 分页对象之间的转换
 * <p>
 * 主要功能：
 * 1. 将 common-core 的 PageRequest 转换为 MyBatis-Plus 的 Page
 * 2. 将 MyBatis-Plus 的 IPage 转换为 common-core 的 PageResult
 * 3. 提供分页相关的工具方法
 */
public class PageUtils {

    /**
     * 将 common-core 的 PageRequest 转换为 MyBatis-Plus 的 Page
     * <p>
     * 用于在数据访问层使用 MyBatis-Plus 进行分页查询
     * 
     * @param pageRequest common-core 分页请求对象
     * @param <T>         数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> Page<T> toMyBatisPlusPage(PageRequest pageRequest) {
        if (pageRequest == null) {
            return new Page<>((long) PaginationConstants.DEFAULT_PAGE, (long) PaginationConstants.DEFAULT_PAGE_SIZE);
        }

        long pageNum = pageRequest.getPage() != null ? (long) pageRequest.getPage() : (long) PaginationConstants.DEFAULT_PAGE;
        long pageSize = pageRequest.getSize() != null ? (long) pageRequest.getSize() : (long) PaginationConstants.DEFAULT_PAGE_SIZE;

        Page<T> mpPage = new Page<>(pageNum, pageSize);

        // 设置排序
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isDesc = PaginationConstants.SORT_DESC.equalsIgnoreCase(pageRequest.getSortOrder());
            if (isDesc) {
                mpPage.addOrder(OrderItem.desc(pageRequest.getSortField()));
            } else {
                mpPage.addOrder(OrderItem.asc(pageRequest.getSortField()));
            }
        }

        return mpPage;
    }

    /**
     * 将 MyBatis-Plus 的 IPage 转换为 common-core 的 PageResult
     * <p>
     * 用于将数据访问层的分页结果转换为业务层和控制层使用的分页结果
     * 
     * @param iPage MyBatis-Plus 分页结果对象
     * @param <T>   数据类型
     * @return common-core 分页结果对象
     */
    public static <T> PageResult<T> toPageResult(IPage<T> iPage) {
        if (iPage == null) {
            return PageResult.of(0L, 1, 10, Collections.emptyList());
        }

        return PageResult.of(
                iPage.getTotal(),
                (int) iPage.getCurrent(),
                (int) iPage.getSize(),
                iPage.getRecords()
        );
    }

    /**
     * 将 MyBatis-Plus 的 IPage 转换为 common-core 的 PageResult
     * <p>
     * 重载方法，允许传入自定义的记录列表
     * 
     * @param iPage   MyBatis-Plus 分页结果对象
     * @param records 自定义记录列表
     * @param <T>     数据类型
     * @return common-core 分页结果对象
     */
    public static <T> PageResult<T> toPageResult(IPage<?> iPage, List<T> records) {
        if (iPage == null) {
            return PageResult.of(0L, 1, 10, records != null ? records : Collections.emptyList());
        }

        return PageResult.of(
                iPage.getTotal(),
                (int) iPage.getCurrent(),
                (int) iPage.getSize(),
                records != null ? records : Collections.emptyList()
        );
    }
}
