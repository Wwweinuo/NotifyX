package com.notifyx.controller;

import com.notifyx.dto.ApiResponse;
import com.notifyx.entity.ReminderRecord;
import com.notifyx.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ApiResponse<Page<ReminderRecord>> getReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<ReminderRecord> reminders = reminderService.getReminders(status, PageRequest.of(page, size));
        return ApiResponse.success(reminders);
    }

    @PostMapping("/trigger")
    public ApiResponse<String> triggerReminders() {
        reminderService.checkAndSendReminders();
        return ApiResponse.success("提醒检查已触发");
    }
}
