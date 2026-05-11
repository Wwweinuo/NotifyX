package com.notifyx.controller;

import com.notifyx.dto.ApiResponse;
import com.notifyx.dto.SchedulerConfigDTO;
import com.notifyx.entity.SchedulerConfig;
import com.notifyx.service.SchedulerConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler-config")
@RequiredArgsConstructor
public class SchedulerConfigController {

    private final SchedulerConfigService schedulerConfigService;

    @GetMapping
    public ApiResponse<List<SchedulerConfig>> getAllConfigs() {
        List<SchedulerConfig> configs = schedulerConfigService.getAllConfigs();
        return ApiResponse.success(configs);
    }

    @PutMapping("/{id}")
    public ApiResponse<SchedulerConfig> updateConfig(
            @PathVariable Long id,
            @RequestBody SchedulerConfigDTO dto) {
        SchedulerConfig config = schedulerConfigService.updateConfig(id, dto);
        return ApiResponse.success(config);
    }
}
