package com.klzw.common.file.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateOcrParserTest {

    private TemplateOcrParser templateOcrParser;

    @BeforeEach
    void setUp() {
        templateOcrParser = new TemplateOcrParser();
    }

    @Test
    @DisplayName("测试提取值方法 - 标准冒号格式")
    void testExtractValue_StandardColon() {
        String text = "姓名：张三";
        String keyword = "姓名";
        String result = templateOcrParser.extractValue(text, keyword);
        assertEquals("张三", result);
    }

    @Test
    @DisplayName("测试提取值方法 - 英文冒号格式")
    void testExtractValue_EnglishColon() {
        String text = "Name: John";
        String keyword = "Name";
        String result = templateOcrParser.extractValue(text, keyword);
        assertEquals("John", result);
    }

    @Test
    @DisplayName("测试提取值方法 - 无冒号情况")
    void testExtractValue_NoColon() {
        String text = "只有文本没有冒号";
        String keyword = "只有";
        String result = templateOcrParser.extractValue(text, keyword);
        assertNull(result);
    }

    @Test
    @DisplayName("测试解析JSON方法 - 有效JSON")
    void testParseJson_ValidJson() {
        String ocrResult = "{\"words_result\":[{\"words\":\"测试\"}]}";
        JSONArray result = templateOcrParser.parseJson(ocrResult);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("测试解析JSON方法 - 无效JSON")
    void testParseJson_InvalidJson() {
        String ocrResult = "invalid json";
        JSONArray result = templateOcrParser.parseJson(ocrResult);
        assertNull(result);
    }

    @Test
    @DisplayName("测试使用关键字映射解析方法")
    void testParseWithKeywordMap() {
        String ocrResult = "{\"words_result\":[{\"words\":\"姓名：张三\"},{\"words\":\"性别：男\"}]}";
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("姓名", "name");
        keywordMap.put("性别", "gender");

        Map<String, String> result = templateOcrParser.parseWithKeywordMap(ocrResult, keywordMap);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("张三", result.get("name"));
        assertEquals("男", result.get("gender"));
    }

    @Test
    @DisplayName("测试解析驾驶证 - 成功")
    void testParseDrivingLicense_Success() {
        String ocrResult = "{\"words_result\":[{\"words\":\"姓名：张三\"},{\"words\":\"性别：男\"},{\"words\":\"国籍：中国\"},{\"words\":\"住址：北京市朝阳区\"},{\"words\":\"出生日期：1990年01月01日\"},{\"words\":\"初次领证日期：2010年01月01日\"},{\"words\":\"准驾车型：C1\"},{\"words\":\"有效期限：2010.01.01-2020.01.01\"},{\"words\":\"证号：123456789012345678\"}]}";

        Map<String, String> result = templateOcrParser.parseDrivingLicense(ocrResult);
        assertNotNull(result);
        assertEquals("张三", result.get("name"));
        assertEquals("男", result.get("gender"));
        assertEquals("中国", result.get("nationality"));
        assertEquals("北京市朝阳区", result.get("address"));
        assertEquals("1990年01月01日", result.get("birth"));
        assertEquals("2010年01月01日", result.get("firstIssueDate"));
        assertEquals("C1", result.get("准驾车型"));
        assertEquals("2010.01.01-2020.01.01", result.get("validPeriod"));
        assertEquals("123456789012345678", result.get("licenseNumber"));
    }

    @Test
    @DisplayName("测试解析驾驶证 - 空结果")
    void testParseDrivingLicense_EmptyResult() {
        String ocrResult = "{\"words_result\":[]}";
        Map<String, String> result = templateOcrParser.parseDrivingLicense(ocrResult);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试解析维修证明 - 成功")
    void testParseRepairCert_Success() {
        String ocrResult = "{\"words_result\":[{\"words\":\"车辆型号：大众帕萨特\"},{\"words\":\"车牌号：京A12345\"},{\"words\":\"维修日期：2023-01-01\"},{\"words\":\"维修项目：发动机保养\"},{\"words\":\"维修费用：1000元\"}]}";

        Map<String, String> result = templateOcrParser.parseRepairCert(ocrResult);
        assertNotNull(result);
        assertEquals("大众帕萨特", result.get("vehicleModel"));
        assertEquals("京A12345", result.get("licensePlate"));
        assertEquals("2023-01-01", result.get("repairDate"));
        assertEquals("发动机保养", result.get("repairItem"));
        assertEquals("1000元", result.get("repairCost"));
    }

    @Test
    @DisplayName("测试解析应急证明 - 成功")
    void testParseEmergencyCert_Success() {
        String ocrResult = "{\"words_result\":[{\"words\":\"申请人：李四\"},{\"words\":\"申请日期：2023-01-01\"},{\"words\":\"应急原因：车辆故障\"},{\"words\":\"救援地点：北京市海淀区\"},{\"words\":\"联系电话：13800138000\"}]}";

        Map<String, String> result = templateOcrParser.parseEmergencyCert(ocrResult);
        assertNotNull(result);
        assertEquals("李四", result.get("applicant"));
        assertEquals("2023-01-01", result.get("applicationDate"));
        assertEquals("车辆故障", result.get("emergencyReason"));
        assertEquals("北京市海淀区", result.get("rescueLocation"));
        assertEquals("13800138000", result.get("contactPhone"));
    }

    @Test
    @DisplayName("测试解析行驶证正面 - 成功")
    void testParseVehicleLicenseFront_Success() {
        String ocrResult = "{\"words_result\":[{\"words\":\"号牌号码：京A12345\"},{\"words\":\"车辆类型：小型轿车\"},{\"words\":\"所有人：张三\"},{\"words\":\"住址：北京市朝阳区\"},{\"words\":\"使用性质：非营运\"},{\"words\":\"品牌型号：大众帕萨特\"},{\"words\":\"车辆识别代号：LSVCA123456789012\"},{\"words\":\"发动机号码：1234567890\"},{\"words\":\"注册日期：2020-01-01\"},{\"words\":\"发证日期：2020-01-01\"}]}";

        Map<String, String> result = templateOcrParser.parseVehicleLicenseFront(ocrResult);
        assertNotNull(result);
        assertEquals("京A12345", result.get("licensePlate"));
        assertEquals("小型轿车", result.get("vehicleType"));
        assertEquals("张三", result.get("owner"));
        assertEquals("北京市朝阳区", result.get("address"));
        assertEquals("非营运", result.get("useNature"));
        assertEquals("大众帕萨特", result.get("brandModel"));
        assertEquals("LSVCA123456789012", result.get("vin"));
        assertEquals("1234567890", result.get("engineNumber"));
        assertEquals("2020-01-01", result.get("registerDate"));
        assertEquals("2020-01-01", result.get("issueDate"));
    }

    @Test
    @DisplayName("测试解析行驶证反面 - 成功")
    void testParseVehicleLicenseBack_Success() {
        String ocrResult = "{\"words_result\":[{\"words\":\"号牌号码：京A12345\"},{\"words\":\"核定载人数：5人\"},{\"words\":\"总质量：1800kg\"},{\"words\":\"整备质量：1400kg\"},{\"words\":\"核定载质量：400kg\"},{\"words\":\"外廓尺寸：4800×1800×1450mm\"},{\"words\":\"准牵引总质量：无\"},{\"words\":\"备注：无\"},{\"words\":\"检验记录：正常\"}]}";

        Map<String, String> result = templateOcrParser.parseVehicleLicenseBack(ocrResult);
        assertNotNull(result);
        assertEquals("京A12345", result.get("licensePlate"));
        assertEquals("5人", result.get("seatingCapacity"));
        assertEquals("1800kg", result.get("totalMass"));
        assertEquals("1400kg", result.get("curbWeight"));
        assertEquals("400kg", result.get("ratedLoad"));
        assertEquals("4800×1800×1450mm", result.get("dimensions"));
        assertEquals("无", result.get("towingCapacity"));
        assertEquals("无", result.get("remarks"));
        assertEquals("正常", result.get("inspectionRecord"));
    }
}
