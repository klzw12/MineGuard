package com.klzw.common.database.util;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.common.database.constant.DatabaseResultCode;
import com.klzw.common.database.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量插入工具类
 * 用于处理批量插入操作，优化插入性能
 */
@Slf4j
public class BatchInsertUtils {

    /**
     * 默认每批插入的数量
     */
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private BatchInsertUtils() {
    }

    /**
     * 批量插入数据
     * @param service 业务服务类
     * @param dataList 数据列表
     * @param <T> 数据类型
     * @return 插入成功的总条数
     * @throws DatabaseException 当批量插入失败时抛出
     */
    public static <T> int batchInsert(IService<T> service, List<T> dataList) {
        return batchInsert(service, dataList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量插入数据
     * @param service 业务服务类
     * @param dataList 数据列表
     * @param batchSize 每批插入的数量
     * @param <T> 数据类型
     * @return 插入成功的总条数
     * @throws DatabaseException 当批量插入失败时抛出
     */
    public static <T> int batchInsert(IService<T> service, List<T> dataList, int batchSize) {
        if (CollectionUtils.isEmpty(dataList) || batchSize <= 0) {
            return 0;
        }

        int totalCount = 0;
        int size = dataList.size();
        int batches = (size + batchSize - 1) / batchSize;
        List<Exception> exceptions = new ArrayList<>();

        log.info("开始批量插入数据，总条数：{}, 批次：{}, 每批大小：{}", size, batches, batchSize);

        for (int i = 0; i < batches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, size);
            List<T> batchList = new ArrayList<>(dataList.subList(start, end));

            try {
                boolean success = service.saveBatch(batchList);
                if (success) {
                    totalCount += batchList.size();
                    log.info("第 {} 批插入成功，插入条数：{}", i + 1, batchList.size());
                } else {
                    log.error("第 {} 批插入失败，插入条数：{}", i + 1, batchList.size());
                    exceptions.add(new DatabaseException(
                            DatabaseResultCode.BATCH_EXECUTION_ERROR
                    ));
                }
            } catch (Exception e) {
                log.error("第 {} 批插入异常，插入条数：{}", i + 1, batchList.size(), e);
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new DatabaseException(
                    DatabaseResultCode.BATCH_EXECUTION_ERROR,
                    exceptions.getFirst()
            );
        }

        log.info("批量插入完成，总成功条数：{}", totalCount);
        return totalCount;
    }

    /**
     * 批量更新数据
     * @param service 业务服务类
     * @param dataList 数据列表
     * @param <T> 数据类型
     * @return 更新成功的总条数
     * @throws DatabaseException 当批量更新失败时抛出
     */
    public static <T> int batchUpdate(IService<T> service, List<T> dataList) {
        return batchUpdate(service, dataList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量更新数据
     * @param service 业务服务类
     * @param dataList 数据列表
     * @param batchSize 每批更新的数量
     * @param <T> 数据类型
     * @return 更新成功的总条数
     * @throws DatabaseException 当批量更新失败时抛出
     */
    public static <T> int batchUpdate(IService<T> service, List<T> dataList, int batchSize) {
        if (CollectionUtils.isEmpty(dataList) || batchSize <= 0) {
            return 0;
        }

        int totalCount = 0;
        int size = dataList.size();
        int batches = (size + batchSize - 1) / batchSize;
        List<Exception> exceptions = new ArrayList<>();

        log.info("开始批量更新数据，总条数：{}, 批次：{}, 每批大小：{}", size, batches, batchSize);

        for (int i = 0; i < batches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, size);
            List<T> batchList = new ArrayList<>(dataList.subList(start, end));

            try {
                boolean success = service.updateBatchById(batchList);
                if (success) {
                    totalCount += batchList.size();
                    log.info("第 {} 批更新成功，更新条数：{}", i + 1, batchList.size());
                } else {
                    log.error("第 {} 批更新失败，更新条数：{}", i + 1, batchList.size());
                    exceptions.add(new DatabaseException(
                            DatabaseResultCode.BATCH_EXECUTION_ERROR
                    ));
                }
            } catch (Exception e) {
                log.error("第 {} 批更新异常，更新条数：{}", i + 1, batchList.size(), e);
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new DatabaseException(
                    DatabaseResultCode.BATCH_EXECUTION_ERROR,
                    exceptions.getFirst()
            );
        }

        log.info("批量更新完成，总成功条数：{}", totalCount);
        return totalCount;
    }

    /**
     * 批量删除数据
     * @param service 业务服务类
     * @param idList ID 列表
     * @param <T> 数据类型
     * @return 删除成功的总条数
     * @throws DatabaseException 当批量删除失败时抛出
     */
    public static <T> int batchDelete(IService<T> service, List<? extends Number> idList) {
        return batchDelete(service, idList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量删除数据
     * @param service 业务服务类
     * @param idList ID 列表
     * @param batchSize 每批删除的数量
     * @param <T> 数据类型
     * @return 删除成功的总条数
     * @throws DatabaseException 当批量删除失败时抛出
     */
    public static <T> int batchDelete(IService<T> service, List<? extends Number> idList, int batchSize) {
        if (CollectionUtils.isEmpty(idList) || batchSize <= 0) {
            return 0;
        }

        int totalCount = 0;
        int size = idList.size();
        int batches = (size + batchSize - 1) / batchSize;
        List<Exception> exceptions = new ArrayList<>();

        log.info("开始批量删除数据，总条数：{}, 批次：{}, 每批大小：{}", size, batches, batchSize);

        for (int i = 0; i < batches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, size);
            List<? extends Number> batchList = idList.subList(start, end);

            try {
                boolean success = service.removeByIds(batchList);
                if (success) {
                    totalCount += batchList.size();
                    log.info("第 {} 批删除成功，删除条数：{}", i + 1, batchList.size());
                } else {
                    log.error("第 {} 批删除失败，删除条数：{}", i + 1, batchList.size());
                    exceptions.add(new DatabaseException(
                            DatabaseResultCode.BATCH_EXECUTION_ERROR
                    ));
                }
            } catch (Exception e) {
                log.error("第 {} 批删除异常，删除条数：{}", i + 1, batchList.size(), e);
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new DatabaseException(
                    DatabaseResultCode.BATCH_EXECUTION_ERROR,
                    exceptions.getFirst()
            );
        }

        log.info("批量删除完成，总成功条数：{}", totalCount);
        return totalCount;
    }

}