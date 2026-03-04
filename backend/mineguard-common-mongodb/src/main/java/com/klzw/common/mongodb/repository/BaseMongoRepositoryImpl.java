package com.klzw.common.mongodb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * MongoDB 基础 Repository 实现类
 * 实现通用的 CRUD 操作
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public abstract class BaseMongoRepositoryImpl<T, ID extends Serializable> implements BaseMongoRepository<T, ID> {

    /**
     * 实体类型
     */
    private final Class<T> entityClass;

    /**
     * MongoTemplate 实例
     */
    private final MongoTemplate mongoTemplate;

    /**
     * 构造方法
     * @param entityClass 实体类型
     * @param mongoTemplate MongoTemplate 实例
     */
    public BaseMongoRepositoryImpl(Class<T> entityClass, MongoTemplate mongoTemplate) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass cannot be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "mongoTemplate cannot be null");
    }

    /**
     * 获取 MongoTemplate
     * @return MongoTemplate 实例
     */
    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    /**
     * 保存实体
     * @param entity 实体对象
     * @return 保存后的实体
     */
    @Override
    public T save(T entity) {
        try {
            Objects.requireNonNull(entity, "entity cannot be null");
            return mongoTemplate.save(entity);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.INSERT_DOCUMENT_ERROR.getCode(),
                    "保存实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 批量保存实体
     * @param entities 实体集合
     * @return 保存后的实体集合
     */
    @Override
    public List<T> saveAll(List<T> entities) {
        try {
            Objects.requireNonNull(entities, "entities cannot be null");
            mongoTemplate.insert(entities, entityClass);
            return entities;
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.INSERT_DOCUMENT_ERROR.getCode(),
                    "批量保存实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据 ID 查询实体
     * @param id 主键
     * @return 实体 Optional
     */
    @Override
    public Optional<T> findById(ID id) {
        try {
            Objects.requireNonNull(id, "id cannot be null");
            T entity = mongoTemplate.findById(id, entityClass);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据ID查询实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据 ID 查询实体
     * @param id 主键
     * @return 实体对象，不存在则返回 null
     */
    @Override
    public T findByIdOrNull(ID id) {
        try {
            Objects.requireNonNull(id, "id cannot be null");
            return mongoTemplate.findById(id, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据ID查询实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 查询所有实体
     * @return 实体集合
     */
    @Override
    public List<T> findAll() {
        try {
            return mongoTemplate.findAll(entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "查询所有实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 分页查询所有实体
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        try {
            Objects.requireNonNull(pageable, "pageable cannot be null");
            Query query = new Query();
            long count = mongoTemplate.count(query, entityClass);
            List<T> list = mongoTemplate.find(query.with(pageable), entityClass);
            return new PageImpl<>(list, pageable, count);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "分页查询实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据条件查询实体
     * @param query 查询条件
     * @return 实体集合
     */
    @Override
    public List<T> findAll(Query query) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            return mongoTemplate.find(query, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据条件查询实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 分页根据条件查询实体
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<T> findAll(Query query, Pageable pageable) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            Objects.requireNonNull(pageable, "pageable cannot be null");
            long count = mongoTemplate.count(query, entityClass);
            List<T> list = mongoTemplate.find(query.with(pageable), entityClass);
            return new PageImpl<>(list, pageable, count);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "分页根据条件查询实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据条件查询单个实体
     * @param query 查询条件
     * @return 实体 Optional
     */
    @Override
    public Optional<T> findOne(Query query) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            T entity = mongoTemplate.findOne(query, entityClass);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据条件查询单个实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据条件查询单个实体
     * @param query 查询条件
     * @return 实体对象，不存在则返回 null
     */
    @Override
    public T findOneOrNull(Query query) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            return mongoTemplate.findOne(query, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据条件查询单个实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据 ID 删除实体
     * @param id 主键
     */
    @Override
    public void deleteById(ID id) {
        try {
            Objects.requireNonNull(id, "id cannot be null");
            Query query = new Query(Criteria.where("_id").is(id));
            mongoTemplate.remove(query, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode(),
                    "根据ID删除实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 删除实体
     * @param entity 实体对象
     */
    @Override
    public void delete(T entity) {
        try {
            Objects.requireNonNull(entity, "entity cannot be null");
            mongoTemplate.remove(entity);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode(),
                    "删除实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 批量删除实体
     * @param entities 实体集合
     */
    @Override
    public void deleteAll(List<T> entities) {
        try {
            if (entities != null && !entities.isEmpty()) {
                Query query = new Query();
                List<Object> ids = entities.stream()
                        .map(mongoTemplate::getId)
                        .filter(Objects::nonNull)
                        .toList();
                if (!ids.isEmpty()) {
                    query.addCriteria(Criteria.where("_id").in(ids));
                    mongoTemplate.remove(query, entityClass);
                }
            }
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode(),
                    "批量删除实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 删除所有实体
     */
    @Override
    public void deleteAll() {
        try {
            mongoTemplate.remove(new Query(), entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode(),
                    "删除所有实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据条件删除实体
     * @param query 查询条件
     * @return 删除的记录数
     */
    @Override
    public long delete(Query query) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            return mongoTemplate.remove(query, entityClass).getDeletedCount();
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode(),
                    "根据条件删除实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 更新实体
     * @param query 查询条件
     * @param update 更新操作
     * @return 更新的记录数
     */
    @Override
    public long update(Query query, Update update) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            Objects.requireNonNull(update, "update cannot be null");
            return mongoTemplate.updateMulti(query, update, entityClass).getModifiedCount();
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.UPDATE_DOCUMENT_ERROR.getCode(),
                    "更新实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据 ID 更新实体
     * @param id 主键
     * @param update 更新操作
     * @return 更新的记录数
     */
    @Override
    public long updateById(ID id, Update update) {
        try {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(update, "update cannot be null");
            Query query = new Query(Criteria.where("_id").is(id));
            return mongoTemplate.updateFirst(query, update, entityClass).getModifiedCount();
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.UPDATE_DOCUMENT_ERROR.getCode(),
                    "根据ID更新实体失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 统计实体数量
     * @return 实体数量
     */
    @Override
    public long count() {
        try {
            return mongoTemplate.count(new Query(), entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "统计实体数量失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据条件统计实体数量
     * @param query 查询条件
     * @return 实体数量
     */
    @Override
    public long count(Query query) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            return mongoTemplate.count(query, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据条件统计实体数量失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 检查实体是否存在
     * @param id 主键
     * @return 是否存在
     */
    @Override
    public boolean existsById(ID id) {
        try {
            Objects.requireNonNull(id, "id cannot be null");
            Query query = new Query(Criteria.where("_id").is(id));
            return mongoTemplate.exists(query, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "检查实体是否存在失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 根据条件检查实体是否存在
     * @param query 查询条件
     * @return 是否存在
     */
    @Override
    public boolean exists(Query query) {
        try {
            Objects.requireNonNull(query, "query cannot be null");
            return mongoTemplate.exists(query, entityClass);
        } catch (Exception e) {
            throw new com.klzw.common.mongodb.exception.MongoDbException(
                    com.klzw.common.mongodb.constant.MongoDbResultCode.QUERY_ERROR.getCode(),
                    "根据条件检查实体是否存在失败: " + e.getMessage(),
                    e
            );
        }
    }

}