package com.klzw.service.trip.service;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.trip.dto.RouteDTO;
import com.klzw.service.trip.vo.RouteVO;

import java.util.List;

public interface RouteService {

    PageResult<RouteVO> page(PageRequest pageRequest);

    RouteVO getById(Long id);

    Long create(RouteDTO dto);

    void update(Long id, RouteDTO dto);

    void delete(Long id);

    List<RouteVO> listAll();

    void enable(Long id);

    void disable(Long id);
}
