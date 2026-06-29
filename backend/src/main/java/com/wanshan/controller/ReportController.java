package com.wanshan.controller;

import com.wanshan.common.Result;
import com.wanshan.model.entity.Report;
import com.wanshan.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping
    public Result<Report> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String targetType = body.get("targetType");
            Long targetId = Long.valueOf(body.get("targetId"));
            String reason = body.get("reason");

            if (!"post".equals(targetType) && !"comment".equals(targetType)) {
                return Result.error("无效的举报类型");
            }
            if (reason == null || reason.isEmpty()) {
                return Result.error("请选择举报理由");
            }

            Report report = reportService.createReport(targetType, targetId, reason, request);
            return Result.success(report);
        } catch (NumberFormatException e) {
            return Result.error("无效的目标 ID");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
