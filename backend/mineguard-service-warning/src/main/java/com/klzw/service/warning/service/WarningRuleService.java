package com.klzw.service.warning.service;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.warning.dto.WarningRuleDTO;
import com.klzw.service.warning.vo.WarningRuleVO;

import java.util.List;

public interface WarningRuleService {

    PageResult<WarningRuleVO> page(PageRequest pageRequest);

    WarningRuleVO getById(Long id);

    WarningRuleVO getByCode(String ruleCode);

    Long create(WarningRuleDTO dto);

    void update(Long id, WarningRuleDTO dto);

    void delete(Long id);

    List<WarningRuleVO> listAll();

    void enable(Long id);

    void disable(Long id);
}
