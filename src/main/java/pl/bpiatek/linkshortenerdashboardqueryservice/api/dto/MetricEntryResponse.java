package pl.bpiatek.linkshortenerdashboardqueryservice.api.dto;

public record MetricEntryResponse(
        String key,
        long value
) {}