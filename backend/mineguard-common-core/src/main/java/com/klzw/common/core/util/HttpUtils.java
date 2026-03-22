package com.klzw.common.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HttpUtils {

    /**
     * 发送 GET 请求
     * @param urlStr 请求 URL
     * @return 响应结果
     * @throws Exception 异常
     */
    public static String get(String urlStr) throws Exception {
        URI uri = new URI(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } else {
            throw new Exception("HTTP GET 请求失败，状态码: " + responseCode);
        }
    }

    /**
     * 发送 POST 请求
     * @param urlStr 请求 URL
     * @param params 请求参数
     * @return 响应结果
     * @throws Exception 异常
     */
    public static String post(String urlStr, Map<String, Object> params) throws Exception {
        URI uri = new URI(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (postData.length() > 0) {
                postData.append("&");
            }
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            postData.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } else {
            throw new Exception("HTTP POST 请求失败，状态码: " + responseCode);
        }
    }

    /**
     * 发送 JSON POST 请求
     * @param urlStr 请求 URL
     * @param jsonData JSON 数据
     * @return 响应结果
     * @throws Exception 异常
     */
    public static String postJson(String urlStr, String jsonData) throws Exception {
        URI uri = new URI(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } else {
            throw new Exception("HTTP POST 请求失败，状态码: " + responseCode);
        }
    }

    /**
     * 发送 JSON POST 请求并返回字节数组
     * @param urlStr 请求 URL
     * @param jsonData JSON 数据
     * @return 响应字节数组
     * @throws Exception 异常
     */
    public static byte[] postJsonForBytes(String urlStr, String jsonData) throws Exception {
        URI uri = new URI(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream is = connection.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            }
        } else {
            throw new Exception("HTTP POST 请求失败，状态码: " + responseCode);
        }
    }
}
