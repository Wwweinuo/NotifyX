package com.notifyx.controller;

import com.notifyx.dto.ApiResponse;
import com.notifyx.dto.HolidayDTO;
import com.notifyx.entity.Holiday;
import com.notifyx.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ApiResponse<List<Holiday>> getAllHolidays() {
        List<Holiday> holidays = holidayService.getAllHolidays();
        return ApiResponse.success(holidays);
    }

    @PostMapping
    public ApiResponse<Holiday> createHoliday(@RequestBody HolidayDTO dto) {
        Holiday holiday = holidayService.createHoliday(dto);
        return ApiResponse.success(holiday);
    }

    @PutMapping("/{id}")
    public ApiResponse<Holiday> updateHoliday(@PathVariable Long id, @RequestBody HolidayDTO dto) {
        Holiday holiday = holidayService.updateHoliday(id, dto);
        return ApiResponse.success(holiday);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ApiResponse.success();
    }
}
