package com.klzw.common.database.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.properties.PaginationProperties;
import com.klzw.common.core.result.PageResult;

import java.util.Collections;
import java.util.List;

/**
 * 分页工具类
 * 实现 common-core 分页对象与 MyBatis-Plus 分页对象之间的转换
 */
public class PageUtils {

    /**
     * 将 common-core 的 PageRequest 转换为 MyBatis-Plus 的 Page
     *
     * @param pageRequest common-core 分页请求对象
     * @param properties  分页配置属性
     * @param <T>         数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> Page<T> toMyBatisPlusPage(PageRequest pageRequest, PaginationProperties properties) {
        if (pageRequest == null) {
            return new Page<>(properties.getDefaultPage(), properties.getDefaultPageSize());
        }

        long pageNum = pageRequest.getPage();
        long pageSize = pageRequest.getSize();

        Page<T> mpPage = new Page<>(pageNum, pageSize);

        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isDesc = "desc".equalsIgnoreCase(pageRequest.getSortOrder());
            if (isDesc) {
                mpPage.addOrder(OrderItem.desc(pageRequest.getSortField()));
            } else {
                mpPage.addOrder(OrderItem.asc(pageRequest.getSortField()));
            }
        }

        return mpPage;
    }

    /**
     * 将 common-core 的 PageRequest 转换为 MyBatis-Plus 的 Page（使用默认配置）
     *
     * @param pageRequest common-core 分页请求对象
     * @param <T>         数据类型
     * @return MyBatis-Plus 分页对象
     */
    public static <T> Page<T> toMyBatisPlusPage(PageRequest pageRequest) {
        if (pageRequest == null) {
            return new Page<>(1, 10);
        }

        long pageNum = pageRequest.getPage();
        long pageSize = pageRequest.getSize();

        Page<T> mpPage = new Page<>(pageNum, pageSize);

        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isDesc = "desc".equalsIgnoreCase(pageRequest.getSortOrder());
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
