package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.WarningCreateDTO;
import com.klzw.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "warning-service")
public interface WarningClient {

    @PostMapping("/api/warning/record")
    Result<Long> createWarning(@RequestBody WarningCreateDTO dto);
}
