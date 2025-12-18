package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinkDetailsResponse;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.MetricEntryResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

class DashboardLinkDetailsDtoMapper {

    private final ObjectMapper objectMapper;

    public DashboardLinkDetailsDtoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DashboardLinkDetailsResponse toResponse(DashboardLinkDetails entity) {

        return new DashboardLinkDetailsResponse(
                entity.linkId(),
                entity.shortUrl(),
                entity.longUrl(),
                entity.title(),
                entity.isActive(),
                entity.createdAt(),
                entity.updatedAt(),
                entity.totalClicks(),
                toMetricList(entity.clicksByCountry()),
                toMetricList(entity.clicksByDevice()),
                toMetricList(entity.clicksByOs())
        );
    }

    private List<MetricEntryResponse> toMetricList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            Map<String, Long> map =
                    objectMapper.readValue(json, new TypeReference<>() {});
            return map.entrySet().stream()
                    .map(e -> new MetricEntryResponse(e.getKey(), e.getValue()))
                    .sorted(Comparator.comparing(MetricEntryResponse::value).reversed())
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid JSONB metric data", ex);
        }
    }
}
