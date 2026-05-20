package com.capstone.pethouse.domain.fan.dto.response;

public record FanStatsResponse(
        Long dailyCount,
        Double dailyOperatingHours,
        Integer averageIntensity,
        Integer autoModeRatio
) {
    public static FanStatsResponse of(Long dailyCount, Double dailyOperatingHours, Integer averageIntensity, Integer autoModeRatio) {
        return new FanStatsResponse(dailyCount, dailyOperatingHours, averageIntensity, autoModeRatio);
    }
}
