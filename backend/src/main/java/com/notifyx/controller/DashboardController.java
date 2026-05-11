package com.notifyx.controller;

import com.notifyx.dto.ApiResponse;
import com.notifyx.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ReminderService reminderService;

    @GetMapping("/upcoming")
    public ApiResponse<List<Map<String, Object>>> getUpcomingReminders() {
        List<Map<String, Object>> upcoming = reminderService.getUpcomingReminders();
        return ApiResponse.success(upcoming);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = reminderService.getStats();
        return ApiResponse.success(stats);
    }
}
