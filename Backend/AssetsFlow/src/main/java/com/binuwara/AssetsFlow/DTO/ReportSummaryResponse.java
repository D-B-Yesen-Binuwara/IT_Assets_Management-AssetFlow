package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReportSummaryResponse(
        String report,
        LocalDate from,
        LocalDate to,
        Map<String, Object> metrics,
        List<Map<String, Object>> series,
        List<Map<String, Object>> breakdowns
) {
}
