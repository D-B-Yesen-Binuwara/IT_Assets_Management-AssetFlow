package com.binuwara.AssetsFlow.DTO;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalAssets,
        long availableAssets,
        long assignedAssets,
        long assetsUnderMaintenance,
        long retiredOrDisposedAssets,
        long warrantiesExpiring,
        BigDecimal totalAssetValue,
        long departments,
        long openMaintenanceTickets,
        long unreadNotifications,
        Map<String, Long> assetsByStatus,
        Map<String, Long> assetsByCategory,
        Map<String, Long> assetsByDepartment
) {
}
