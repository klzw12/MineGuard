package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.AbstractMongoDbIntegrationTest;
import com.klzw.common.mongodb.constant.MongoDbConstants;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TtlIndexUtil 集成测试
 */
@DisplayName("TtlIndexUtil集成测试")
class TtlIndexUtilIntegrationTest extends AbstractMongoDbIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("创建TTL索引应成功")
    void createTtlIndex_shouldSucceed() {
        String collectionName = "test_ttl_" + UUID.randomUUID().toString().replace("-", "");
        String fieldName = "expireAt";
        long expireAfterSeconds = 3600L;

        assertDoesNotThrow(() -> 
            TtlIndexUtil.createTtlIndex(mongoTemplate, collectionName, fieldName, expireAfterSeconds)
        );

        boolean indexExists = false;
        for (Document index : mongoTemplate.getDb().getCollection(collectionName).listIndexes()) {
            Document key = (Document) index.get("key");
            if (key.containsKey(fieldName)) {
                indexExists = true;
                Number expireAfter = index.get("expireAfterSeconds", Number.class);
                assertEquals(expireAfterSeconds, expireAfter.longValue());
                break;
            }
        }
        assertTrue(indexExists, "TTL索引应存在");

        mongoTemplate.dropCollection(collectionName);
    }

    @Test
    @DisplayName("重复创建TTL索引应不会报错")
    void createTtlIndex_duplicateShouldNotFail() {
        String collectionName = "test_ttl_dup_" + UUID.randomUUID().toString().replace("-", "");
        String fieldName = "expireAt";
        long expireAfterSeconds = 3600L;

        assertDoesNotThrow(() -> 
            TtlIndexUtil.createTtlIndex(mongoTemplate, collectionName, fieldName, expireAfterSeconds)
        );

        assertDoesNotThrow(() -> 
            TtlIndexUtil.createTtlIndex(mongoTemplate, collectionName, fieldName, expireAfterSeconds)
        );

        mongoTemplate.dropCollection(collectionName);
    }

    @Test
    @DisplayName("使用常量创建TTL索引应成功")
    void createTtlIndex_withConstants_shouldSucceed() {
        String collectionName = MongoDbConstants.COLLECTION_TRIP_HISTORY;
        String fieldName = MongoDbConstants.FIELD_CREATE_TIME;
        long expireAfterSeconds = MongoDbConstants.TTL_1_YEAR;

        assertDoesNotThrow(() -> 
            TtlIndexUtil.createTtlIndex(mongoTemplate, collectionName, fieldName, expireAfterSeconds)
        );

        mongoTemplate.dropCollection(collectionName);
    }

}
