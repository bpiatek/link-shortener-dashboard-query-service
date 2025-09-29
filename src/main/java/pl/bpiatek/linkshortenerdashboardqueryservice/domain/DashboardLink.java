package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import java.time.Instant;

record DashboardLink(
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
