package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import java.time.Instant;
import java.util.Map;

class TestDashboardLink {
    private final Long id;
    private final String linkId;
    private final String userId;
    private final String shortUrl;
    private final String longUrl;
    private final String title;
    private final boolean isActive;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final long totalClicks;
    private final Map<String, Long> clicksByCountry;
    private final Map<String, Long> clicksByDevice;
    private final Map<String, Long> clicksByOs;

    public TestDashboardLink(TestDashboardLinkBuilder builder) {
        this.id = builder.id;
        this.linkId = builder.linkId;
        this.userId = builder.userId;
        this.shortUrl = builder.shortUrl;
        this.longUrl = builder.longUrl;
        this.title = builder.title;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.totalClicks = builder.totalClicks;
        this.clicksByCountry = builder.clicksByCountry;
        this.clicksByDevice = builder.clicksByDevice;
        this.clicksByOs = builder.clicksByOs;
    }

    static TestDashboardLinkBuilder builder() {
        return new TestDashboardLinkBuilder();
    }

    public Long getId() {
        return id;
    }

    public String getLinkId() {
        return linkId;
    }

    public String getUserId() {
        return userId;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public String getTitle() {
        return title;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public Map<String, Long> getClicksByCountry() {
        return clicksByCountry;
    }

    public Map<String, Long> getClicksByDevice() {
        return clicksByDevice;
    }

    public Map<String, Long> getClicksByOs() {
        return clicksByOs;
    }

    static class TestDashboardLinkBuilder {
        private Long id;
        private String linkId = "link-id-1";
        private String userId = "user-id-1";
        private String shortUrl = "short-url";
        private String longUrl = "https://example.com/long-url";
        private String title = "Title";
        private boolean isActive;
        private Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        private Instant updatedAt = Instant.parse("2025-01-01T10:00:00Z");
        private long totalClicks = 0L;
        private Map<String, Long> clicksByCountry = Map.of();
        private Map<String, Long> clicksByDevice = Map.of();
        private Map<String, Long> clicksByOs = Map.of();

        public TestDashboardLinkBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TestDashboardLinkBuilder linkId(String linkId) {
            this.linkId = linkId;
            return this;
        }

        public TestDashboardLinkBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public TestDashboardLinkBuilder shortUrl(String shortUrl) {
            this.shortUrl = shortUrl;
            return this;
        }

        public TestDashboardLinkBuilder longUrl(String longUrl) {
            this.longUrl = longUrl;
            return this;
        }

        public TestDashboardLinkBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TestDashboardLinkBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public TestDashboardLinkBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TestDashboardLinkBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TestDashboardLinkBuilder totalClicks(long totalClicks) {
            this.totalClicks = totalClicks;
            return this;
        }

        public TestDashboardLinkBuilder clicksByCountry(Map<String, Long> clicksByCountry) {
            this.clicksByCountry = clicksByCountry;
            return this;
        }

        public TestDashboardLinkBuilder clicksByDevice(Map<String, Long> clicksByDevice) {
            this.clicksByDevice = clicksByDevice;
            return this;
        }

        public TestDashboardLinkBuilder clicksByOs(Map<String, Long> clicksByOs) {
            this.clicksByOs = clicksByOs;
            return this;
        }

        public TestDashboardLink build() {
            return new TestDashboardLink(this);
        }
    }
}
