package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import java.time.Instant;

record DashboardLinkDetails(
        Long id,
        String linkId,
        String userId,
        String shortUrl,
        String longUrl,
        String title,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        long totalClicks,
        String clicksByCountry,
        String clicksByDevice,
        String clicksByOs
) {}