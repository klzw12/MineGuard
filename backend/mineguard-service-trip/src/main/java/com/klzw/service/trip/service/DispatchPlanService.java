package com.klzw.service.trip.service;

import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.trip.dto.DispatchPlanDTO;
import com.klzw.service.trip.vo.DispatchPlanVO;

import java.time.LocalDate;
import java.util.List;

public interface DispatchPlanService {

    PageResult<DispatchPlanVO> page(PageRequest pageRequest);

    DispatchPlanVO getById(Long id);

    Long create(DispatchPlanDTO dto);

    void update(Long id, DispatchPlanDTO dto);

    void delete(Long id);

    List<DispatchPlanVO> getByDate(LocalDate date);

    void execute(Long id);

    void complete(Long id);
}
