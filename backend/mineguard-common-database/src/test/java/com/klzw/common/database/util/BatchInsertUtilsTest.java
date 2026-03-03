package com.klzw.common.database.util;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.common.database.exception.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchInsertUtils 测试")
class BatchInsertUtilsTest {

    @Mock
    private IService<String> mockService;

    @BeforeEach
    void setUp() {
        reset(mockService);
    }

    @Test
    @DisplayName("batchInsert - 正常批量插入")
    void batchInsert_NormalBatchInsert() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList);

        assertEquals(5, result);
        verify(mockService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 空列表")
    void batchInsert_EmptyList() {
        List<String> dataList = Collections.emptyList();

        int result = BatchInsertUtils.batchInsert(mockService, dataList);

        assertEquals(0, result);
        verify(mockService, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - null列表")
    void batchInsert_NullList() {
        int result = BatchInsertUtils.batchInsert(mockService, null);

        assertEquals(0, result);
        verify(mockService, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 超过默认批次大小")
    void batchInsert_ExceedDefaultBatchSize() {
        List<String> dataList = createList(1500);
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList);

        assertEquals(1500, result);
        verify(mockService, times(2)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 自定义批次大小")
    void batchInsert_CustomBatchSize() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList, 2);

        assertEquals(5, result);
        verify(mockService, times(3)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 部分批次失败")
    void batchInsert_PartialBatchFailure() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(mockService.saveBatch(anyList()))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchInsert(mockService, dataList, 2));
        verify(mockService, times(3)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 所有批次失败")
    void batchInsert_AllBatchesFailure() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3");
        when(mockService.saveBatch(anyList())).thenReturn(false);

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchInsert(mockService, dataList));
        verify(mockService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 批次异常")
    void batchInsert_BatchException() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3");
        when(mockService.saveBatch(anyList())).thenThrow(new RuntimeException("Database error"));

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchInsert(mockService, dataList));
        verify(mockService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 单个元素")
    void batchInsert_SingleElement() {
        List<String> dataList = Collections.singletonList("item1");
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList);

        assertEquals(1, result);
        verify(mockService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 正好一个批次")
    void batchInsert_ExactlyOneBatch() {
        List<String> dataList = createList(1000);
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList);

        assertEquals(1000, result);
        verify(mockService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 大批次大小")
    void batchInsert_LargeBatchSize() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3");
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList, 10000);

        assertEquals(3, result);
        verify(mockService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchUpdate - 正常批量更新")
    void batchUpdate_NormalBatchUpdate() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3");
        when(mockService.updateBatchById(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchUpdate(mockService, dataList);

        assertEquals(3, result);
        verify(mockService, times(1)).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchUpdate - 空列表")
    void batchUpdate_EmptyList() {
        List<String> dataList = Collections.emptyList();

        int result = BatchInsertUtils.batchUpdate(mockService, dataList);

        assertEquals(0, result);
        verify(mockService, never()).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchUpdate - null列表")
    void batchUpdate_NullList() {
        int result = BatchInsertUtils.batchUpdate(mockService, null);

        assertEquals(0, result);
        verify(mockService, never()).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchUpdate - 超过默认批次大小")
    void batchUpdate_ExceedDefaultBatchSize() {
        List<String> dataList = createList(1500);
        when(mockService.updateBatchById(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchUpdate(mockService, dataList);

        assertEquals(1500, result);
        verify(mockService, times(2)).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchUpdate - 自定义批次大小")
    void batchUpdate_CustomBatchSize() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(mockService.updateBatchById(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchUpdate(mockService, dataList, 2);

        assertEquals(5, result);
        verify(mockService, times(3)).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchUpdate - 部分批次失败")
    void batchUpdate_PartialBatchFailure() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(mockService.updateBatchById(anyList()))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchUpdate(mockService, dataList, 2));
        verify(mockService, times(3)).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchUpdate - 批次异常")
    void batchUpdate_BatchException() {
        List<String> dataList = Arrays.asList("item1", "item2", "item3");
        when(mockService.updateBatchById(anyList())).thenThrow(new RuntimeException("Database error"));

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchUpdate(mockService, dataList));
        verify(mockService, times(1)).updateBatchById(anyList());
    }

    @Test
    @DisplayName("batchDelete - 正常批量删除")
    void batchDelete_NormalBatchDelete() {
        List<Integer> idList = Arrays.asList(1, 2, 3, 4, 5);
        when(mockService.removeByIds(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchDelete(mockService, idList);

        assertEquals(5, result);
        verify(mockService, times(1)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - 空ID列表")
    void batchDelete_EmptyIdList() {
        List<Integer> idList = Collections.emptyList();

        int result = BatchInsertUtils.batchDelete(mockService, idList);

        assertEquals(0, result);
        verify(mockService, never()).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - null ID列表")
    void batchDelete_NullIdList() {
        int result = BatchInsertUtils.batchDelete(mockService, null);

        assertEquals(0, result);
        verify(mockService, never()).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - 超过默认批次大小")
    void batchDelete_ExceedDefaultBatchSize() {
        List<Integer> idList = createIntList(1500);
        when(mockService.removeByIds(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchDelete(mockService, idList);

        assertEquals(1500, result);
        verify(mockService, times(2)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - 自定义批次大小")
    void batchDelete_CustomBatchSize() {
        List<Integer> idList = Arrays.asList(1, 2, 3, 4, 5);
        when(mockService.removeByIds(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchDelete(mockService, idList, 2);

        assertEquals(5, result);
        verify(mockService, times(3)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - 部分批次失败")
    void batchDelete_PartialBatchFailure() {
        List<Integer> idList = Arrays.asList(1, 2, 3, 4, 5);
        when(mockService.removeByIds(anyList()))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchDelete(mockService, idList, 2));
        verify(mockService, times(3)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - 批次异常")
    void batchDelete_BatchException() {
        List<Integer> idList = Arrays.asList(1, 2, 3);
        when(mockService.removeByIds(anyList())).thenThrow(new RuntimeException("Database error"));

        assertThrows(DatabaseException.class, () -> BatchInsertUtils.batchDelete(mockService, idList));
        verify(mockService, times(1)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - Long类型ID")
    void batchDelete_LongIds() {
        List<Long> idList = Arrays.asList(1L, 2L, 3L);
        when(mockService.removeByIds(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchDelete(mockService, idList);

        assertEquals(3, result);
        verify(mockService, times(1)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchDelete - 单个ID")
    void batchDelete_SingleId() {
        List<Integer> idList = Collections.singletonList(1);
        when(mockService.removeByIds(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchDelete(mockService, idList);

        assertEquals(1, result);
        verify(mockService, times(1)).removeByIds(anyList());
    }

    @Test
    @DisplayName("batchInsert - 零批次大小")
    void batchInsert_ZeroBatchSize() {
        List<String> dataList = Arrays.asList("item1", "item2");

        int result = BatchInsertUtils.batchInsert(mockService, dataList, 0);

        assertEquals(0, result);
        verify(mockService, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 负批次大小")
    void batchInsert_NegativeBatchSize() {
        List<String> dataList = Arrays.asList("item1", "item2");

        int result = BatchInsertUtils.batchInsert(mockService, dataList, -1);

        assertEquals(0, result);
        verify(mockService, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("batchInsert - 大数据量")
    void batchInsert_LargeDataVolume() {
        List<String> dataList = createList(10000);
        when(mockService.saveBatch(anyList())).thenReturn(true);

        int result = BatchInsertUtils.batchInsert(mockService, dataList);

        assertEquals(10000, result);
        verify(mockService, times(10)).saveBatch(anyList());
    }

    private List<String> createList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> "item" + i)
                .toList();
    }

    private List<Integer> createIntList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .boxed()
                .toList();
    }
}
