package com.klzw.common.mongodb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB 基础 Repository 实现类
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public class BaseMongoRepositoryImpl<T, ID extends Serializable> extends SimpleMongoRepository<T, ID> implements BaseMongoRepository<T, ID> {

    private final MongoTemplate mongoTemplate;
    private final Class<T> domainClass;

    public BaseMongoRepositoryImpl(MongoEntityInformation<T, ID> entityInformation, MongoTemplate mongoTemplate) {
        super(entityInformation, mongoTemplate);
        this.mongoTemplate = mongoTemplate;
        this.domainClass = entityInformation.getJavaType();
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public T findByIdOrNull(ID id) {
        return findById(id).orElse(null);
    }

    @Override
    public List<T> findAll(Query query) {
        return mongoTemplate.find(query, domainClass);
    }

    @Override
    public Page<T> findAll(Query query, Pageable pageable) {
        List<T> content = mongoTemplate.find(query.with(pageable), domainClass);
        long count = mongoTemplate.count(query, domainClass);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, count);
    }

    @Override
    public Optional<T> findOne(Query query) {
        return Optional.ofNullable(mongoTemplate.findOne(query, domainClass));
    }

    @Override
    public T findOneOrNull(Query query) {
        return mongoTemplate.findOne(query, domainClass);
    }

    @Override
    public long delete(Query query) {
        return mongoTemplate.remove(query, domainClass).getDeletedCount();
    }

    @Override
    public long update(Query query, Update update) {
        return mongoTemplate.updateMulti(query, update, domainClass).getModifiedCount();
    }

    @Override
    public long updateById(ID id, Update update) {
        Query query = new Query();
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id));
        return update(query, update);
    }

    @Override
    public long count(Query query) {
        return mongoTemplate.count(query, domainClass);
    }

    @Override
    public boolean exists(Query query) {
        return mongoTemplate.exists(query, domainClass);
    }
}