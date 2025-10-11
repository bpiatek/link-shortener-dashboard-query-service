package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinkDto;

import java.util.Collections;
import java.util.Map;

public class DashboardFacade {

    private final Logger logger = LoggerFactory.getLogger(DashboardFacade.class);

    private final DashboardLinkRepository dashboardLinkRepository;
    private final ObjectMapper objectMapper;

    DashboardFacade(DashboardLinkRepository dashboardLinkRepository, ObjectMapper objectMapper) {
        this.dashboardLinkRepository = dashboardLinkRepository;
        this.objectMapper = objectMapper;
    }

    public Page<DashboardLinkDto> getUserLinks(String userId, Pageable pageable) {
        return dashboardLinkRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    private DashboardLinkDto toDto(DashboardLink entity) {
        return new DashboardLinkDto(
                entity.id(),
                entity.linkId(),
                entity.userId(),
                entity.shortUrl(),
                entity.longUrl(),
                entity.title(),
                entity.isActive(),
                entity.createdAt(),
                entity.updatedAt(),
                entity.totalClicks()
        );
    }

    private Map<String, Integer> parseJsonbToMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSONB: {}", json, e);
            return Collections.emptyMap();
        }
    }
}
