package com.klzw.service.dispatch.service;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.dispatch.dto.RouteTemplateDTO;
import com.klzw.service.dispatch.vo.RouteTemplateVO;

import java.util.List;

public interface RouteTemplateService {

    PageResult<RouteTemplateVO> page(PageRequest pageRequest);

    RouteTemplateVO getById(Long id);

    Long create(RouteTemplateDTO dto);

    void update(Long id, RouteTemplateDTO dto);

    void delete(Long id);

    List<RouteTemplateVO> listAll();

    void enable(Long id);

    void disable(Long id);
}
