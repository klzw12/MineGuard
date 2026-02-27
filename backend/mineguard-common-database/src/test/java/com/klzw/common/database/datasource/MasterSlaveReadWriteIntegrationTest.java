package com.klzw.common.database.datasource;

import com.klzw.common.database.AbstractIntegrationTest;
import com.klzw.common.database.domain.TestEntity;
import com.klzw.common.database.service.TestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 主从分离集成测试
 * <p>
 * 此测试类用于验证主从分离功能：
 * 1. 主库插入数据
 * 2. 从库读取数据
 * 3. 读写分离自动切换
 * 4. 批量操作测试
 * <p>
 * 注意：完整的主从分离测试需要配置真实的主从数据库
 */
@DisplayName("主从分离集成测试")
class MasterSlaveReadWriteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestService testService;

    /**
     * 测试主库插入数据
     * <p>
     * 验证写操作是否正确使用主数据源
     */
    @Test
    @DisplayName("测试主库插入数据")
    void testMasterInsertData() {
        // 插入测试数据
        boolean result = testService.insertData("测试用户", 25);
        assertTrue(result, "数据插入应该成功");
        
        // 查询所有数据，验证数据已插入
        List<TestEntity> entities = testService.queryAll();
        assertFalse(entities.isEmpty(), "数据库中应该有数据");
        
        // 验证最后一条数据是否为刚插入的数据
        TestEntity lastEntity = entities.getLast();
        assertEquals("测试用户", lastEntity.getName(), "用户名应该匹配");
        assertEquals(25, lastEntity.getAge(), "年龄应该匹配");
    }

    /**
     * 测试从库读取数据
     * <p>
     * 验证读操作是否正确使用从数据源
     */
    @Test
    @DisplayName("测试从库读取数据")
    void testSlaveReadData() {
        // 先插入一条测试数据
        testService.insertData("读取测试用户", 30);
        
        // 查询所有数据
        List<TestEntity> entities = testService.queryAll();
        assertFalse(entities.isEmpty(), "数据库中应该有数据");
        
        // 验证数据包含刚插入的数据
        boolean found = entities.stream()
                .anyMatch(entity -> "读取测试用户".equals(entity.getName()) && entity.getAge() == 30);
        assertTrue(found, "应该能找到刚插入的数据");
        
        // 测试根据ID查询
        TestEntity firstEntity = entities.getFirst();
        TestEntity queriedEntity = testService.getById(firstEntity.getId());
        assertNotNull(queriedEntity, "根据ID查询应该返回数据");
        assertEquals(firstEntity.getId(), queriedEntity.getId(), "ID应该匹配");
        assertEquals(firstEntity.getName(), queriedEntity.getName(), "用户名应该匹配");
    }

    /**
     * 测试读写分离自动切换
     * <p>
     * 验证读写操作是否自动切换到正确的数据源
     */
    @Test
    @DisplayName("测试读写分离自动切换")
    void testReadWriteSplitAutoSwitch() {
        // 测试写操作（插入）
        boolean insertResult = testService.insertData("自动切换测试", 35);
        assertTrue(insertResult, "插入操作应该成功");
        
        // 测试读操作（查询）
        List<TestEntity> entities = testService.queryAll();
        assertFalse(entities.isEmpty(), "查询操作应该返回数据");
        
        // 测试写操作（更新）
        TestEntity lastEntity = entities.getLast();
        boolean updateResult = testService.updateData(lastEntity.getId(), "更新测试", 40);
        assertTrue(updateResult, "更新操作应该成功");
        
        // 测试读操作（根据ID查询）
        TestEntity updatedEntity = testService.getById(lastEntity.getId());
        assertNotNull(updatedEntity, "更新后应该能查询到数据");
        assertEquals("更新测试", updatedEntity.getName(), "更新后用户名应该匹配");
        assertEquals(40, updatedEntity.getAge(), "更新后年龄应该匹配");
        
        // 测试写操作（删除）
        boolean deleteResult = testService.deleteById(lastEntity.getId());
        assertTrue(deleteResult, "删除操作应该成功");
        
        // 测试读操作（根据ID查询已删除的数据）
        TestEntity deletedEntity = testService.getById(lastEntity.getId());
        assertNull(deletedEntity, "删除后应该查询不到数据");
    }

    /**
     * 测试批量操作
     * <p>
     * 验证批量操作是否正确使用主数据源
     */
    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() {
        // 准备批量插入的数据
        List<TestEntity> batchEntities = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TestEntity entity = new TestEntity();
            entity.setName("批量测试用户" + i);
            entity.setAge(20 + i);
            batchEntities.add(entity);
        }
        
        // 批量插入
        boolean batchInsertResult = testService.saveBatch(batchEntities);
        assertTrue(batchInsertResult, "批量插入应该成功");
        
        // 查询所有数据，验证批量插入结果
        List<TestEntity> allEntities = testService.queryAll();
        assertFalse(allEntities.isEmpty(), "数据库中应该有数据");
        
        // 验证批量插入的数据
        long batchCount = allEntities.stream()
                .filter(entity -> entity.getName().startsWith("批量测试用户"))
                .count();
        assertEquals(5, batchCount, "应该插入了5条批量测试数据");
        
        // 准备批量更新的数据
        List<TestEntity> updateEntities = new ArrayList<>();
        for (TestEntity entity : allEntities) {
            if (entity.getName().startsWith("批量测试用户")) {
                entity.setName(entity.getName() + "-更新");
                entity.setAge(entity.getAge() + 10);
                updateEntities.add(entity);
            }
        }
        
        // 批量更新
        boolean batchUpdateResult = testService.updateBatchById(updateEntities);
        assertTrue(batchUpdateResult, "批量更新应该成功");
        
        // 查询所有数据，验证批量更新结果
        List<TestEntity> updatedEntities = testService.queryAll();
        long updatedCount = updatedEntities.stream()
                .filter(entity -> entity.getName().endsWith("-更新"))
                .count();
        assertEquals(5, updatedCount, "应该更新了5条批量测试数据");
        
        // 准备批量删除的ID列表
        List<Integer> deleteIds = updatedEntities.stream()
                .filter(entity -> entity.getName().startsWith("批量测试用户"))
                .map(TestEntity::getId)
                .toList();
        
        // 批量删除
        boolean batchDeleteResult = testService.removeByIds(deleteIds);
        assertTrue(batchDeleteResult, "批量删除应该成功");
        
        // 查询所有数据，验证批量删除结果
        List<TestEntity> finalEntities = testService.queryAll();
        long finalCount = finalEntities.stream()
                .filter(entity -> entity.getName().startsWith("批量测试用户"))
                .count();
        assertEquals(0, finalCount, "批量测试数据应该被全部删除");
    }
}
