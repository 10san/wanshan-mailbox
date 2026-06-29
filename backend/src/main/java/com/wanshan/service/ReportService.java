package com.wanshan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.IpUtil;
import com.wanshan.mapper.ReportMapper;
import com.wanshan.model.entity.Report;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;

    public Report createReport(String targetType, Long targetId, String reason, HttpServletRequest request) {
        String ipHash = IpUtil.hashIp(IpUtil.getClientIp(request));

        // 去重检查
        LambdaQueryWrapper<Report> dupCheck = new LambdaQueryWrapper<>();
        dupCheck.eq(Report::getTargetType, targetType)
                .eq(Report::getTargetId, targetId)
                .eq(Report::getIpHash, ipHash)
                .eq(Report::getStatus, 0);
        if (reportMapper.selectCount(dupCheck) > 0) {
            throw new IllegalArgumentException("您已举报过该内容");
        }

        Report report = Report.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .ipHash(ipHash)
                .status(0)
                .build();

        reportMapper.insert(report);
        log.info("Report created: type={}, targetId={}, reason={}", targetType, targetId, reason);
        return report;
    }

    public List<Report> getPendingReports() {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, 0)
                .orderByAsc(Report::getCreatedAt);
        return reportMapper.selectList(wrapper);
    }
}
