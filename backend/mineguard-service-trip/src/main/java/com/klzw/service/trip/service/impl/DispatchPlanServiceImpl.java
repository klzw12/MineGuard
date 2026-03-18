package com.klzw.service.trip.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.trip.client.UserServiceClient;
import com.klzw.service.trip.client.VehicleServiceClient;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.dto.DispatchPlanDTO;
import com.klzw.service.trip.entity.DispatchPlan;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.DispatchPlanMapper;
import com.klzw.service.trip.service.DispatchPlanService;
import com.klzw.service.trip.vo.DispatchPlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchPlanServiceImpl implements DispatchPlanService {

    private final DispatchPlanMapper dispatchPlanMapper;
    private final UserServiceClient userServiceClient;
    private final VehicleServiceClient vehicleServiceClient;

    @Override
    public PageResult<DispatchPlanVO> page(PageRequest pageRequest) {
        Page<DispatchPlan> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<DispatchPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DispatchPlan::getCreateTime);
        
        Page<DispatchPlan> result = dispatchPlanMapper.selectPage(page, wrapper);
        
        List<DispatchPlanVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public DispatchPlanVO getById(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_NOT_FOUND);
        }
        return convertToVO(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DispatchPlanDTO dto) {
        validateVehicleAndDriver(dto.getVehicleId(), dto.getDriverId());
        
        DispatchPlan plan = new DispatchPlan();
        BeanUtils.copyProperties(dto, plan);
        plan.setPlanNo(generatePlanNo());
        plan.setStatus(1);
        
        dispatchPlanMapper.insert(plan);
        log.info("创建调度计划成功，计划ID：{}，计划编号：{}", plan.getId(), plan.getPlanNo());
        
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DispatchPlanDTO dto) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_NOT_FOUND);
        }
        
        if (plan.getStatus() != 1) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_EXECUTED, "只有待执行的计划可以修改");
        }
        
        validateVehicleAndDriver(dto.getVehicleId(), dto.getDriverId());
        
        BeanUtils.copyProperties(dto, plan);
        plan.setId(id);
        
        dispatchPlanMapper.updateById(plan);
        log.info("更新调度计划成功，计划ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_NOT_FOUND);
        }
        
        if (plan.getStatus() == 2) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_EXECUTED, "执行中的计划不能删除");
        }
        
        dispatchPlanMapper.deleteById(id);
        log.info("删除调度计划成功，计划ID：{}", id);
    }

    @Override
    public List<DispatchPlanVO> getByDate(LocalDate date) {
        LambdaQueryWrapper<DispatchPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchPlan::getPlanDate, date)
               .orderByAsc(DispatchPlan::getStartTimeSlot);
        
        List<DispatchPlan> plans = dispatchPlanMapper.selectList(wrapper);
        return plans.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_NOT_FOUND);
        }
        
        if (plan.getStatus() != 1) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_EXECUTED, "只有待执行的计划可以执行");
        }
        
        plan.setStatus(2);
        dispatchPlanMapper.updateById(plan);
        log.info("执行调度计划成功，计划ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        DispatchPlan plan = dispatchPlanMapper.selectById(id);
        if (plan == null) {
            throw new TripException(TripResultCode.DISPATCH_PLAN_NOT_FOUND);
        }
        
        plan.setStatus(3);
        dispatchPlanMapper.updateById(plan);
        log.info("完成调度计划成功，计划ID：{}", id);
    }

    private void validateVehicleAndDriver(Long vehicleId, Long driverId) {
        Boolean vehicleExists = vehicleServiceClient.existsById(vehicleId).block();
        if (vehicleExists == null || !vehicleExists) {
            throw new TripException(TripResultCode.VEHICLE_NOT_AVAILABLE);
        }
        
        Boolean driverExists = userServiceClient.existsById(driverId).block();
        if (driverExists == null || !driverExists) {
            throw new TripException(TripResultCode.DRIVER_NOT_AVAILABLE);
        }
    }

    private String generatePlanNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "DP" + dateStr + IdUtil.randomUUID().substring(0, 4).toUpperCase();
    }

    private DispatchPlanVO convertToVO(DispatchPlan plan) {
        DispatchPlanVO vo = new DispatchPlanVO();
        BeanUtils.copyProperties(plan, vo);
        return vo;
    }
}
