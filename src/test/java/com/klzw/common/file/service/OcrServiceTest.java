package com.klzw.common.file.service;

import com.klzw.common.file.impl.OcrServiceImpl;
import com.klzw.common.file.handler.BaiduOcrParserFactory;
import com.klzw.common.file.util.BaiduOcrUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrServiceTest {

    @Mock
    private BaiduOcrUtils baiduOcrUtils;

    @Mock
    private BaiduOcrParserFactory baiduOcrParserFactory;

    private OcrServiceImpl ocrService;

    @Test
    @DisplayName("测试解析驾驶证识别结果 - 成功")
    void testParseDrivingLicense_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "name", "张三",
                "gender", "男",
                "nationality", "中国",
                "address", "北京市朝阳区",
                "birth", "1990年01月01日",
                "firstIssueDate", "2010年01月01日",
                "准驾车型", "C1",
                "validPeriod", "2010.01.01-2020.01.01",
                "licenseNumber", "123456789012345678"
        );

        when(baiduOcrParserFactory.parseDrivingLicense(anyString())).thenReturn(expectedResult);

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        String ocrResult = "{\"words_result\":[{\"words\":\"姓名：张三\"},{\"words\":\"性别：男\"},{\"words\":\"国籍：中国\"},{\"words\":\"住址：北京市朝阳区\"},{\"words\":\"出生日期：1990年01月01日\"},{\"words\":\"初次领证日期：2010年01月01日\"},{\"words\":\"准驾车型：C1\"},{\"words\":\"有效期限：2010.01.01-2020.01.01\"},{\"words\":\"证号：123456789012345678\"}]}";
        Map<String, String> result = ocrService.parseDrivingLicense(ocrResult);

        assertNotNull(result);
        assertEquals("张三", result.get("name"));
        assertEquals("C1", result.get("准驾车型"));
    }

    @Test
    @DisplayName("测试解析维修证明 - 成功")
    void testParseRepairCert_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "vehicleModel", "大众帕萨特",
                "licensePlate", "京A12345",
                "repairDate", "2023-01-01",
                "repairItem", "发动机保养",
                "repairCost", "1000元"
        );

        when(baiduOcrParserFactory.parseRepairCert(anyString())).thenReturn(expectedResult);

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        String ocrResult = "{\"words_result\":[{\"words\":\"车辆型号：大众帕萨特\"},{\"words\":\"车牌号：京A12345\"},{\"words\":\"维修日期：2023-01-01\"},{\"words\":\"维修项目：发动机保养\"},{\"words\":\"维修费用：1000元\"}]}";
        Map<String, String> result = ocrService.parseRepairCert(ocrResult);

        assertNotNull(result);
        assertEquals("大众帕萨特", result.get("vehicleModel"));
        assertEquals("京A12345", result.get("licensePlate"));
    }

    @Test
    @DisplayName("测试解析应急证明 - 成功")
    void testParseEmergencyCert_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "applicant", "李四",
                "applicationDate", "2023-01-01",
                "emergencyReason", "车辆故障",
                "rescueLocation", "北京市海淀区",
                "contactPhone", "13800138000"
        );

        when(baiduOcrParserFactory.parseEmergencyCert(anyString())).thenReturn(expectedResult);

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        String ocrResult = "{\"words_result\":[{\"words\":\"申请人：李四\"},{\"words\":\"申请日期：2023-01-01\"},{\"words\":\"应急原因：车辆故障\"},{\"words\":\"救援地点：北京市海淀区\"},{\"words\":\"联系电话：13800138000\"}]}";
        Map<String, String> result = ocrService.parseEmergencyCert(ocrResult);

        assertNotNull(result);
        assertEquals("李四", result.get("applicant"));
        assertEquals("车辆故障", result.get("emergencyReason"));
    }

    @Test
    @DisplayName("测试解析行驶证正面 - 成功")
    void testParseVehicleLicenseFront_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "licensePlate", "京A12345",
                "vehicleType", "小型轿车",
                "owner", "张三",
                "address", "北京市朝阳区",
                "useNature", "非营运",
                "brandModel", "大众帕萨特",
                "vin", "LSVCA123456789012",
                "engineNumber", "1234567890",
                "registerDate", "2020-01-01",
                "issueDate", "2020-01-01"
        );

        when(baiduOcrParserFactory.parseVehicleLicenseFront(anyString())).thenReturn(expectedResult);

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        String ocrResult = "{\"words_result\":[{\"words\":\"号牌号码：京A12345\"},{\"words\":\"车辆类型：小型轿车\"},{\"words\":\"所有人：张三\"},{\"words\":\"住址：北京市朝阳区\"},{\"words\":\"使用性质：非营运\"},{\"words\":\"品牌型号：大众帕萨特\"},{\"words\":\"车辆识别代号：LSVCA123456789012\"},{\"words\":\"发动机号码：1234567890\"},{\"words\":\"注册日期：2020-01-01\"},{\"words\":\"发证日期：2020-01-01\"}]}";
        Map<String, String> result = ocrService.parseVehicleLicenseFront(ocrResult);

        assertNotNull(result);
        assertEquals("京A12345", result.get("licensePlate"));
        assertEquals("大众帕萨特", result.get("brandModel"));
    }

    @Test
    @DisplayName("测试解析行驶证反面 - 成功")
    void testParseVehicleLicenseBack_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "licensePlate", "京A12345",
                "seatingCapacity", "5人",
                "totalMass", "1800kg",
                "curbWeight", "1400kg",
                "ratedLoad", "400kg",
                "dimensions", "4800×1800×1450mm",
                "towingCapacity", "无",
                "remarks", "无",
                "inspectionRecord", "正常"
        );

        when(baiduOcrParserFactory.parseVehicleLicenseBack(anyString())).thenReturn(expectedResult);

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        String ocrResult = "{\"words_result\":[{\"words\":\"号牌号码：京A12345\"},{\"words\":\"核定载人数：5人\"},{\"words\":\"总质量：1800kg\"},{\"words\":\"整备质量：1400kg\"},{\"words\":\"核定载质量：400kg\"},{\"words\":\"外廓尺寸：4800×1800×1450mm\"},{\"words\":\"准牵引总质量：无\"},{\"words\":\"备注：无\"},{\"words\":\"检验记录：正常\"}]}";
        Map<String, String> result = ocrService.parseVehicleLicenseBack(ocrResult);

        assertNotNull(result);
        assertEquals("京A12345", result.get("licensePlate"));
        assertEquals("5人", result.get("seatingCapacity"));
    }

    @Test
    @DisplayName("测试解析驾驶证 - 空输入")
    void testParseDrivingLicense_EmptyInput() {
        when(baiduOcrParserFactory.parseDrivingLicense(anyString())).thenReturn(new HashMap<>());

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        Map<String, String> result = ocrService.parseDrivingLicense("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试解析驾驶证 - null输入")
    void testParseDrivingLicense_NullInput() {
        when(baiduOcrParserFactory.parseDrivingLicense(null)).thenReturn(new HashMap<>());

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        Map<String, String> result = ocrService.parseDrivingLicense(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试解析维修证明 - 空输入")
    void testParseRepairCert_EmptyInput() {
        when(baiduOcrParserFactory.parseRepairCert(anyString())).thenReturn(new HashMap<>());

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        Map<String, String> result = ocrService.parseRepairCert("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试解析应急证明 - 空输入")
    void testParseEmergencyCert_EmptyInput() {
        when(baiduOcrParserFactory.parseEmergencyCert(anyString())).thenReturn(new HashMap<>());

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        Map<String, String> result = ocrService.parseEmergencyCert("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试解析行驶证正面 - 空输入")
    void testParseVehicleLicenseFront_EmptyInput() {
        when(baiduOcrParserFactory.parseVehicleLicenseFront(anyString())).thenReturn(new HashMap<>());

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        Map<String, String> result = ocrService.parseVehicleLicenseFront("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试解析行驶证反面 - 空输入")
    void testParseVehicleLicenseBack_EmptyInput() {
        when(baiduOcrParserFactory.parseVehicleLicenseBack(anyString())).thenReturn(new HashMap<>());

        ocrService = new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);

        Map<String, String> result = ocrService.parseVehicleLicenseBack("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
