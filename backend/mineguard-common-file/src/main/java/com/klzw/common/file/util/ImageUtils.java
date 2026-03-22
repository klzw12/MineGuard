package com.klzw.common.file.util;

import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.constant.FileResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * 图片处理工具类
 * <p>
 * 提供 Base64 图片解码、验证等公共方法
 */
@Slf4j
public class ImageUtils {

    private ImageUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 将 Base64 图片字符串解码为字节数组
     * <p>
     * 支持处理带数据 URI 前缀的 Base64 字符串（如：data:image/jpeg;base64,/9j/...）
     *
     * @param base64Image Base64 编码的图片字符串
     * @return 解码后的字节数组
     * @throws FileException 当 Base64 解码失败时抛出
     */
    public static byte[] decodeBase64Image(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new FileException(FileResultCode.PARAM_ERROR, "图片数据不能为空");
        }

        try {
            String base64Data = base64Image;
            // 移除数据 URI 前缀（如果存在）
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            log.error("Base64 解码失败", e);
            throw new FileException(FileResultCode.PARAM_ERROR, "图片格式错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("图片处理失败", e);
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "图片处理失败：" + e.getMessage());
        }
    }

    /**
     * 将 MultipartFile 转换为 Base64 编码字符串
     *
     * @param file 上传的图片文件
     * @return Base64 编码的图片字符串
     * @throws IOException IO 异常
     */
    public static String encodeToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileException(FileResultCode.PARAM_ERROR, "上传的文件不能为空");
        }
        byte[] imageBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 将字节数组转换为 Base64 编码字符串
     *
     * @param imageBytes 图片字节数组
     * @return Base64 编码的图片字符串
     */
    public static String encodeToBase64(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new FileException(FileResultCode.PARAM_ERROR, "图片数据不能为空");
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 将 Base64 编码字符串转换为数据 URI 格式
     *
     * @param base64Image Base64 编码的图片字符串
     * @param mimeType    MIME 类型（如：image/jpeg, image/png）
     * @return 数据 URI 格式的图片字符串
     */
    public static String toDataUri(String base64Image, String mimeType) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new FileException(FileResultCode.PARAM_ERROR, "图片数据不能为空");
        }
        return "data:" + mimeType + ";base64," + base64Image;
    }

    /**
     * 从数据 URI 格式中提取 MIME 类型
     *
     * @param dataUri 数据 URI 格式的图片字符串
     * @return MIME 类型，如果无法解析则返回 null
     */
    public static String extractMimeType(String dataUri) {
        if (dataUri == null || !dataUri.startsWith("data:")) {
            return null;
        }

        int commaIndex = dataUri.indexOf(',');
        if (commaIndex == -1) {
            return null;
        }

        String prefix = dataUri.substring(0, commaIndex);
        int semicolonIndex = prefix.indexOf(';');
        if (semicolonIndex == -1) {
            return null;
        }

        return prefix.substring(5, semicolonIndex);
    }

    /**
     * 从数据 URI 格式中提取 Base64 数据部分
     *
     * @param dataUri 数据 URI 格式的图片字符串
     * @return Base64 数据部分（不包含前缀）
     */
    public static String extractBase64Data(String dataUri) {
        if (dataUri == null || !dataUri.contains(",")) {
            return dataUri;
        }

        String[] parts = dataUri.split(",", 2);
        return parts.length > 1 ? parts[1] : parts[0];
    }

    /**
     * 验证 Base64 图片字符串是否有效
     *
     * @param base64Image Base64 编码的图片字符串
     * @return true-有效，false-无效
     */
    public static boolean isValidBase64Image(String base64Image) {
        try {
            byte[] decoded = decodeBase64Image(base64Image);
            return decoded != null && decoded.length > 0;
        } catch (Exception e) {
            log.debug("图片验证失败：{}", e.getMessage());
            return false;
        }
    }
}
