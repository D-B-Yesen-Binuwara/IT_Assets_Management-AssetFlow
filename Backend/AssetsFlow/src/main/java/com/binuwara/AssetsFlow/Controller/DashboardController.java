package com.binuwara.AssetsFlow.Controller;

import com.binuwara.AssetsFlow.DTO.DashboardSummaryResponse;
import com.binuwara.AssetsFlow.DTO.LifecycleEventResponse;
import com.binuwara.AssetsFlow.DTO.ReportSummaryResponse;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import com.binuwara.AssetsFlow.Service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final ReportingService reportingService;

    public DashboardController(ReportingService reportingService) { this.reportingService = reportingService; }

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse summary(@AuthenticationPrincipal AuthenticatedUser actor) { return reportingService.dashboard(actor); }

    @GetMapping("/dashboard/activity")
    public List<LifecycleEventResponse> activity() { return reportingService.activity(); }

    @GetMapping("/reports/summary")
    public ReportSummaryResponse report(@RequestParam(required = false) String report, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) { return reportingService.report(report, from, to); }
}
