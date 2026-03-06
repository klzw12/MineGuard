package com.klzw.common.file.service;

import com.klzw.common.file.handler.BaiduOcrParserFactory;
import com.klzw.common.file.util.BaiduOcrUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OcrService 单元测试
 * <p>
 * 使用Mockito模拟BaiduOcrUtils和BaiduOcrParser，测试OCR服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OCR服务单元测试")
class OcrServiceTest {

    @Mock
    private BaiduOcrUtils baiduOcrUtils;
    
    @Mock
    private BaiduOcrParserFactory baiduOcrParserFactory;

    private OcrService ocrService;

    @Test
    @DisplayName("测试识别身份证正面 - 成功")
    void testRecognizeIdCard_Success() {
        when(baiduOcrUtils.recognizeIdCard(any(MultipartFile.class), eq("front")))
                .thenReturn("{\"name\":\"张三\",\"idcard\":\"123456789012345678\"}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeIdCard(file);
        
        assertNotNull(result);
        assertTrue(result.contains("张三"));
    }

    @Test
    @DisplayName("测试识别身份证背面 - 成功")
    void testRecognizeIdCardBack_Success() {
        when(baiduOcrUtils.recognizeIdCard(any(MultipartFile.class), eq("back")))
                .thenReturn("{\"issue_authority\":\"某某公安局\",\"valid_period\":\"2020.01.01-2040.01.01\"}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeIdCardBack(file);
        
        assertNotNull(result);
        assertTrue(result.contains("issue_authority"));
    }

    @Test
    @DisplayName("测试识别驾驶证 - 成功")
    void testRecognizeDrivingLicense_Success() {
        when(baiduOcrUtils.recognizeDrivingLicense(any(MultipartFile.class)))
                .thenReturn("{\"name\":\"李四\",\"license_no\":\"C123456789\"}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeDrivingLicense(file);
        
        assertNotNull(result);
        assertTrue(result.contains("李四"));
    }

    @Test
    @DisplayName("测试识别行驶证正面 - 成功")
    void testRecognizeVehicleLicense_Success() {
        when(baiduOcrUtils.recognizeVehicleLicense(any(MultipartFile.class), eq("front")))
                .thenReturn("{\"plate_no\":\"京A12345\",\"vehicle_type\":\"小型轿车\"}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeVehicleLicense(file);
        
        assertNotNull(result);
        assertTrue(result.contains("京A12345"));
    }

    @Test
    @DisplayName("测试识别行驶证背面 - 成功")
    void testRecognizeVehicleLicenseBack_Success() {
        when(baiduOcrUtils.recognizeVehicleLicense(any(MultipartFile.class), eq("back")))
                .thenReturn("{\"inspection_record\":\"检验合格\"}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeVehicleLicenseBack(file);
        
        assertNotNull(result);
        assertTrue(result.contains("inspection_record"));
    }

    @Test
    @DisplayName("测试识别车牌 - 成功")
    void testRecognizeLicensePlate_Success() {
        when(baiduOcrUtils.recognizeLicensePlate(any(MultipartFile.class)))
                .thenReturn("{\"number\":\"京A12345\"}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeLicensePlate(file);
        
        assertNotNull(result);
        assertTrue(result.contains("京A12345"));
    }

    @Test
    @DisplayName("测试识别紧急救援资格证 - 成功")
    void testRecognizeEmergencyCert_Success() {
        when(baiduOcrUtils.recognizeGeneral(any(MultipartFile.class)))
                .thenReturn("{\"words_result\":[{\"words\":\"紧急救援资格证\"}]}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeEmergencyCert(file);
        
        assertNotNull(result);
        assertTrue(result.contains("紧急救援资格证"));
    }

    @Test
    @DisplayName("测试识别维修资格证 - 成功")
    void testRecognizeRepairCert_Success() {
        when(baiduOcrUtils.recognizeGeneral(any(MultipartFile.class)))
                .thenReturn("{\"words_result\":[{\"words\":\"维修资格证\"}]}");
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", "test".getBytes());
        
        String result = ocrService.recognizeRepairCert(file);
        
        assertNotNull(result);
        assertTrue(result.contains("维修资格证"));
    }

    @Test
    @DisplayName("测试解析身份证识别结果 - 成功")
    void testParseIdCard_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "name", "李桂英",
                "gender", "女",
                "nation", "裕固族",
                "birth", "1998年12月1日",
                "address", "福建省秀英县安次呼和浩特街1座253537",
                "idNumber", "120103199812012782"
        );
        
        when(baiduOcrParserFactory.parseIdCard(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"中华人民共和国居民身份证\"},{\"words\":\"姓名：李桂英\"},{\"words\":\"性别：女\"},{\"words\":\"民族：裕固族\"},{\"words\":\"出生日期：1998年12月1日\"},{\"words\":\"住址：福建省秀英 县安次呼和浩特街1座253537\"},{\"words\":\"公民身份号码 ：120103199812012782\"}]}";
        Map<String, String> result = ocrService.parseIdCard(ocrResult);
        
        assertNotNull(result);
        assertEquals("李桂英", result.get("name"));
        assertEquals("女", result.get("gender"));
        assertEquals("120103199812012782", result.get("idNumber"));
    }

    @Test
    @DisplayName("测试解析车牌识别结果 - 成功")
    void testParseLicensePlate_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "plateNumber", "桂S3455"
        );
        
        when(baiduOcrParserFactory.parseLicensePlate(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"桂RGSCUE援\"},{\"words\":\"组\"},{\"words\":\"桂S3455\"},{\"words\":\"豆 包AI生成\"}]}";
        Map<String, String> result = ocrService.parseLicensePlate(ocrResult);
        
        assertNotNull(result);
        assertEquals("桂S3455", result.get("plateNumber"));
    }

    @Test
    @DisplayName("测试解析通用文字识别结果 - 成功")
    void testParseGeneral_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "text", "维修资格证书\n证书编号：WX547006\n姓名：黄亮\n身份证号：411502195009166999\n资格等级：中级\n维修类别：机械维修\n发证日期：2019年2月6日\n有效期至：2031年3月4日",
                "wordCount", "8"
        );
        
        when(baiduOcrParserFactory.parseGeneral(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"维修资格证书\"},{\"words\":\"证书编号：WX547006\"},{\"words\":\"姓名：黄亮\"},{\"words\":\"身份证号：411502195009166999\"},{\"words\":\"资格等级：中级\"},{\"words\":\"维修类别：机械维修\"},{\"words\":\"发证日期：2019年2月6日\"},{\"words\":\"有效期至：2031年3月4日\"}]}";
        Map<String, String> result = ocrService.parseGeneral(ocrResult);
        
        assertNotNull(result);
        assertTrue(result.get("text").contains("维修资格证书"));
        assertEquals("8", result.get("wordCount"));
    }

    @Test
    @DisplayName("测试解析所有类型的证件识别结果 - 成功")
    void testParse_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "name", "李桂英",
                "idNumber", "120103199812012782"
        );
        
        when(baiduOcrParserFactory.parse(anyString(), eq("idcard"))).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"姓名：李桂英\"},{\"words\":\"公民身份号码 ：120103199812012782\"}]}";
        Map<String, String> result = ocrService.parse(ocrResult, "idcard");
        
        assertNotNull(result);
        assertEquals("李桂英", result.get("name"));
        assertEquals("120103199812012782", result.get("idNumber"));
    }

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
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"姓名：张三\"},{\"words\":\"性别：男\"},{\"words\":\"国籍：中国\"},{\"words\":\"住址：北京市朝阳区\"},{\"words\":\"出生日期：1990年01月01日\"},{\"words\":\"初次领证日期：2010年01月01日\"},{\"words\":\"准驾车型：C1\"},{\"words\":\"有效期限：2010.01.01-2020.01.01\"},{\"words\":\"证号：123456789012345678\"}]}";
        Map<String, String> result = ocrService.parseDrivingLicense(ocrResult);
        
        assertNotNull(result);
        assertEquals("张三", result.get("name"));
        assertEquals("C1", result.get("准驾车型"));
    }

    @Test
    @DisplayName("测试解析维修资格证识别结果 - 成功")
    void testParseRepairCert_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "certificateNumber", "WX547006",
                "name", "黄亮",
                "idNumber", "411502195009166999",
                "level", "中级",
                "category", "机械维修",
                "issueDate", "2019年2月6日",
                "validUntil", "2031年3月4日"
        );
        
        when(baiduOcrParserFactory.parseRepairCert(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"维修资格证书\"},{\"words\":\"证书编号：WX547006\"},{\"words\":\"姓名：黄亮\"},{\"words\":\"身份证号：411502195009166999\"},{\"words\":\"资格等级：中级\"},{\"words\":\"维修类别：机械维修\"},{\"words\":\"发证日期：2019年2月6日\"},{\"words\":\"有效期至：2031年3月4日\"}]}";
        Map<String, String> result = ocrService.parseRepairCert(ocrResult);
        
        assertNotNull(result);
        assertEquals("黄亮", result.get("name"));
        assertEquals("中级", result.get("level"));
    }

    @Test
    @DisplayName("测试解析应急救援证识别结果 - 成功")
    void testParseEmergencyCert_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "certificateNumber", "EM123456",
                "name", "李四",
                "idNumber", "110101199001011234",
                "trainingItem", "消防救援",
                "validPeriod", "2020.01.01-2025.01.01"
        );
        
        when(baiduOcrParserFactory.parseEmergencyCert(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"应急救援资格证\"},{\"words\":\"证书编号：EM123456\"},{\"words\":\"持证人姓名：李四\"},{\"words\":\"身份证号：110101199001011234\"},{\"words\":\"培训项目：消防救援\"},{\"words\":\"有效期：2020.01.01-2025.01.01\"}]}";
        Map<String, String> result = ocrService.parseEmergencyCert(ocrResult);
        
        assertNotNull(result);
        assertEquals("李四", result.get("name"));
        assertEquals("消防救援", result.get("trainingItem"));
    }

    @Test
    @DisplayName("测试解析行驶证正面识别结果 - 成功")
    void testParseVehicleLicenseFront_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "plateNumber", "京A12345",
                "owner", "张三",
                "address", "北京市朝阳区",
                "brandModel", "大众 朗逸",
                "vehicleModel", "SVW7167NSD",
                "engineNumber", "12345678",
                "vin", "LSVAF6A46GN000001",
                "useNature", "非营运",
                "registerDate", "2020-01-01",
                "issueDate", "2020-01-01"
        );
        
        when(baiduOcrParserFactory.parseVehicleLicenseFront(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"中华人民共和国机动车行驶证\"},{\"words\":\"号牌号码：京A12345\"},{\"words\":\"所有人：张三\"},{\"words\":\"住址：北京市朝阳区\"},{\"words\":\"品牌型号：大众 朗逸\"},{\"words\":\"车辆型号：SVW7167NSD\"},{\"words\":\"发动机号：12345678\"},{\"words\":\"车辆识别代号：LSVAF6A46GN000001\"},{\"words\":\"使用性质：非营运\"},{\"words\":\"注册日期：2020-01-01\"},{\"words\":\"发证日期：2020-01-01\"}]}";
        Map<String, String> result = ocrService.parseVehicleLicenseFront(ocrResult);
        
        assertNotNull(result);
        assertEquals("京A12345", result.get("plateNumber"));
        assertEquals("张三", result.get("owner"));
    }

    @Test
    @DisplayName("测试解析行驶证背面识别结果 - 成功")
    void testParseVehicleLicenseBack_Success() {
        // 模拟解析结果
        Map<String, String> expectedResult = Map.of(
                "seatingCapacity", "5",
                "totalMass", "1500kg",
                "curbWeight", "1200kg",
                "ratedLoad", "300kg",
                "dimensions", "4600×1700×1400",
                "remarks", "无",
                "inspectionRecord", "检验合格"
        );
        
        when(baiduOcrParserFactory.parseVehicleLicenseBack(anyString())).thenReturn(expectedResult);
        
        ocrService = new com.klzw.common.file.impl.OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
        
        String ocrResult = "{\"words_result\":[{\"words\":\"核定载人数：5\"},{\"words\":\"总质量：1500kg\"},{\"words\":\"整备质量：1200kg\"},{\"words\":\"核定载质量：300kg\"},{\"words\":\"外廓尺寸：4600×1700×1400\"},{\"words\":\"备注：无\"},{\"words\":\"检验记录：检验合格\"}]}";
        Map<String, String> result = ocrService.parseVehicleLicenseBack(ocrResult);
        
        assertNotNull(result);
        assertEquals("5", result.get("seatingCapacity"));
        assertEquals("1500kg", result.get("totalMass"));
    }

}
