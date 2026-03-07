package com.klzw.common.map.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.klzw.common.map.exception.MapException;
import com.klzw.common.map.properties.GaodeMapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class GaodeMapClient {
    private final GaodeMapProperties properties;
    private final RestTemplate restTemplate;

    public GaodeMapClient(GaodeMapProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public JSONObject request(String path, Map<String, String> params) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                params.put("key", properties.getApiKey());
                String url = properties.getApiUrl() + path;
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/x-www-form-urlencoded");
                
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, String.class);
                
                String body = response.getBody();
                JSONObject result = JSON.parseObject(body);
                
                if (result == null) {
                    throw new MapException("API response is null");
                }
                
                String status = result.getString("status");
                if (!"1".equals(status)) {
                    String info = result.getString("info");
                    throw new MapException("API error: " + info);
                }
                
                return result;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    if (e instanceof MapException) {
                        throw (MapException) e;
                    }
                    log.error("Gaode API request failed after " + maxRetries + " retries", e);
                    throw new MapException("Failed to call Gaode API", e);
                }
                log.warn("Gaode API request failed, retrying (" + retryCount + "/" + maxRetries + ")", e);
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new MapException("Failed to call Gaode API");
    }
}
