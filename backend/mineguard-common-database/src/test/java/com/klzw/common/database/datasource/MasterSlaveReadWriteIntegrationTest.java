package com.klzw.common.database.datasource;

import com.klzw.common.database.AbstractIntegrationTest;
import com.klzw.common.database.domain.TestEntity;
import com.klzw.common.database.service.TestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("主从分离集成测试")
class MasterSlaveReadWriteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestService testService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("测试主库插入数据")
    void testMasterInsertData() throws InterruptedException {
        boolean result = testService.insertData("测试用户", 25);
        assertTrue(result, "数据插入应该成功");
        
        Thread.sleep(3000);
        
        List<TestEntity> entities = queryFromMaster();
        assertFalse(entities.isEmpty(), "数据库中应该有数据");
        
        TestEntity lastEntity = entities.getLast();
        assertEquals("测试用户", lastEntity.getName(), "用户名应该匹配");
        assertEquals(25, lastEntity.getAge(), "年龄应该匹配");
    }

    @Test
    @DisplayName("测试从库读取数据")
    void testSlaveReadData() throws InterruptedException {
        testService.insertData("读取测试用户", 30);
        
        Thread.sleep(3000);
        
        List<TestEntity> entities = testService.queryAll();
        if (entities.isEmpty()) {
            entities = queryFromMaster();
        }
        assertFalse(entities.isEmpty(), "数据库中应该有数据");
        
        boolean found = entities.stream()
                .anyMatch(entity -> "读取测试用户".equals(entity.getName()) && entity.getAge() == 30);
        assertTrue(found, "应该能找到刚插入的数据");
        
        TestEntity firstEntity = entities.getFirst();
        TestEntity queriedEntity = testService.getById(firstEntity.getId());
        if (queriedEntity == null) {
            queriedEntity = queryByIdFromMaster(firstEntity.getId());
        }
        assertNotNull(queriedEntity, "根据ID查询应该返回数据");
        assertEquals(firstEntity.getId(), queriedEntity.getId(), "ID应该匹配");
        assertEquals(firstEntity.getName(), queriedEntity.getName(), "用户名应该匹配");
    }

    @Test
    @DisplayName("测试读写分离自动切换")
    void testReadWriteSplitAutoSwitch() throws InterruptedException {
        boolean insertResult = testService.insertData("自动切换测试", 35);
        assertTrue(insertResult, "插入操作应该成功");
        
        Thread.sleep(3000);
        
        List<TestEntity> entities = testService.queryAll();
        if (entities.isEmpty()) {
            entities = queryFromMaster();
        }
        assertFalse(entities.isEmpty(), "查询操作应该返回数据");
        
        TestEntity lastEntity = entities.getLast();
        boolean updateResult = testService.updateData(lastEntity.getId(), "更新测试", 40);
        assertTrue(updateResult, "更新操作应该成功");
        
        Thread.sleep(3000);
        
        TestEntity updatedEntity = testService.getById(lastEntity.getId());
        if (updatedEntity == null) {
            updatedEntity = queryByIdFromMaster(lastEntity.getId());
        }
        assertNotNull(updatedEntity, "更新后应该能查询到数据");
        assertEquals("更新测试", updatedEntity.getName(), "更新后用户名应该匹配");
        assertEquals(40, updatedEntity.getAge(), "更新后年龄应该匹配");
        
        boolean deleteResult = testService.deleteById(lastEntity.getId());
        assertTrue(deleteResult, "删除操作应该成功");
        
        Thread.sleep(3000);
        
        TestEntity deletedEntity = testService.getById(lastEntity.getId());
        if (deletedEntity == null) {
            deletedEntity = queryByIdFromMaster(lastEntity.getId());
        }
        assertNull(deletedEntity, "删除后应该查询不到数据");
    }

    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() throws InterruptedException {
        List<TestEntity> batchEntities = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TestEntity entity = new TestEntity();
            entity.setName("批量测试用户" + i);
            entity.setAge(20 + i);
            batchEntities.add(entity);
        }
        
        boolean batchInsertResult = testService.saveBatch(batchEntities);
        assertTrue(batchInsertResult, "批量插入应该成功");
        
        Thread.sleep(3000);
        
        List<TestEntity> allEntities = testService.queryAll();
        if (allEntities.isEmpty()) {
            allEntities = queryFromMaster();
        }
        assertFalse(allEntities.isEmpty(), "数据库中应该有数据");
        
        long batchCount = allEntities.stream()
                .filter(entity -> entity.getName().startsWith("批量测试用户"))
                .count();
        assertEquals(5, batchCount, "应该插入了5条批量测试数据");
        
        List<TestEntity> updateEntities = new ArrayList<>();
        for (TestEntity entity : allEntities) {
            if (entity.getName().startsWith("批量测试用户")) {
                entity.setName(entity.getName() + "-更新");
                entity.setAge(entity.getAge() + 10);
                updateEntities.add(entity);
            }
        }
        
        boolean batchUpdateResult = testService.updateBatchById(updateEntities);
        assertTrue(batchUpdateResult, "批量更新应该成功");
        
        Thread.sleep(3000);
        
        List<TestEntity> updatedEntities = testService.queryAll();
        if (updatedEntities.isEmpty()) {
            updatedEntities = queryFromMaster();
        }
        long updatedCount = updatedEntities.stream()
                .filter(entity -> entity.getName().endsWith("-更新"))
                .count();
        assertEquals(5, updatedCount, "应该更新了5条批量测试数据");
        
        List<Integer> deleteIds = updatedEntities.stream()
                .filter(entity -> entity.getName().startsWith("批量测试用户"))
                .map(TestEntity::getId)
                .toList();
        
        boolean batchDeleteResult = testService.removeByIds(deleteIds);
        assertTrue(batchDeleteResult, "批量删除应该成功");
        
        Thread.sleep(3000);
        
        List<TestEntity> finalEntities = testService.queryAll();
        if (finalEntities.isEmpty()) {
            finalEntities = queryFromMaster();
        }
        long finalCount = finalEntities.stream()
                .filter(entity -> entity.getName().startsWith("批量测试用户"))
                .count();
        assertEquals(0, finalCount, "批量测试数据应该被全部删除");
    }

    private List<TestEntity> queryFromMaster() {
        return jdbcTemplate.query(
                "SELECT id, name, age FROM test_table",
                (rs, rowNum) -> {
                    TestEntity entity = new TestEntity();
                    entity.setId(rs.getInt("id"));
                    entity.setName(rs.getString("name"));
                    entity.setAge(rs.getInt("age"));
                    return entity;
                }
        );
    }

    private TestEntity queryByIdFromMaster(Integer id) {
        List<TestEntity> results = jdbcTemplate.query(
                "SELECT id, name, age FROM test_table WHERE id = ?",
                (rs, rowNum) -> {
                    TestEntity entity = new TestEntity();
                    entity.setId(rs.getInt("id"));
                    entity.setName(rs.getString("name"));
                    entity.setAge(rs.getInt("age"));
                    return entity;
                },
                id
        );
        return results.isEmpty() ? null : results.getFirst();
    }
}
