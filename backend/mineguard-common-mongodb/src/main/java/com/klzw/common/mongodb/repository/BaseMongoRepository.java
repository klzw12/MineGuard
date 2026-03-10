package com.klzw.common.mongodb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB 基础 Repository 接口
 * 封装通用的 CRUD 操作
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseMongoRepository<T, ID extends Serializable> {

    /**
     * 获取 MongoTemplate
     * @return MongoTemplate 实例
     */
    MongoTemplate getMongoTemplate();

    /**
     * 保存实体
     * @param entity 实体对象
     * @return 保存后的实体
     */
    T save(T entity);

    /**
     * 批量保存实体
     * @param entities 实体集合
     * @return 保存后的实体集合
     */
    List<T> saveAll(List<T> entities);

    /**
     * 根据 ID 查询实体
     * @param id 主键
     * @return 实体 Optional
     */
    Optional<T> findById(ID id);

    /**
     * 根据 ID 查询实体
     * @param id 主键
     * @return 实体对象，不存在则返回 null
     */
    T findByIdOrNull(ID id);

    /**
     * 查询所有实体
     * @return 实体集合
     */
    List<T> findAll();

    /**
     * 分页查询所有实体
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<T> findAll(Pageable pageable);

    /**
     * 根据条件查询实体
     * @param query 查询条件
     * @return 实体集合
     */
    List<T> findAll(Query query);

    /**
     * 分页根据条件查询实体
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<T> findAll(Query query, Pageable pageable);

    /**
     * 根据条件查询单个实体
     * @param query 查询条件
     * @return 实体 Optional
     */
    Optional<T> findOne(Query query);

    /**
     * 根据条件查询单个实体
     * @param query 查询条件
     * @return 实体对象，不存在则返回 null
     */
    T findOneOrNull(Query query);

    /**
     * 根据 ID 删除实体
     * @param id 主键
     */
    void deleteById(ID id);

    /**
     * 删除实体
     * @param entity 实体对象
     */
    void delete(T entity);

    /**
     * 批量删除实体
     * @param entities 实体集合
     */
    void deleteAll(List<T> entities);

    /**
     * 删除所有实体
     */
    void deleteAll();

    /**
     * 根据条件删除实体
     * @param query 查询条件
     * @return 删除的记录数
     */
    long delete(Query query);

    /**
     * 更新实体
     * @param query 查询条件
     * @param update 更新操作
     * @return 更新的记录数
     */
    long update(Query query, Update update);

    /**
     * 根据 ID 更新实体
     * @param id 主键
     * @param update 更新操作
     * @return 更新的记录数
     */
    long updateById(ID id, Update update);

    /**
     * 统计实体数量
     * @return 实体数量
     */
    long count();

    /**
     * 根据条件统计实体数量
     * @param query 查询条件
     * @return 实体数量
     */
    long count(Query query);

    /**
     * 检查实体是否存在
     * @param id 主键
     * @return 是否存在
     */
    boolean existsById(ID id);

    /**
     * 根据条件检查实体是否存在
     * @param query 查询条件
     * @return 是否存在
     */
    boolean exists(Query query);

}