package pl.bpiatek.linkshortenerdashboardqueryservice.api.dto;

import java.time.Instant;

public record DashboardLinksResponse(
        Long id,
        String linkId,
        String userId,
        String shortUrl,
        String longUrl,
        String title,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        long totalClicks
) {
}
