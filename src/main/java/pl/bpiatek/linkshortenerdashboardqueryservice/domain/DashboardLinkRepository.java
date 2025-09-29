package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

interface DashboardLinkRepository {
    void create(DashboardLink link);
    void update(DashboardLink link);
    void delete(String linkId);
    void incrementClickCounters(String linkId, String countryCode, String deviceType, String osName);
}
